package com.mic.s3client;

import com.amazonaws.services.devicefarm.model.ArgumentException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
@Log4j2
public class AmazonClient {

    private AmazonS3 s3client;
    private String region;
    private String bucketName;

    public AmazonClient(String region, String bucketName) {
        this.region = region;
        this.bucketName = bucketName;
    }

    @PostConstruct
    private void initializeAmazon() {
        this.s3client = AmazonS3ClientBuilder
                .standard()
                .withRegion(region)
                .build();
    }

    public File prepareFiles(MultipartFile file) {
        List<String> fileExtensions = Arrays.asList("JPEG", "JPG", "PNG", "GIF", "TIFF", "PSD", "PDF", "EPS", "AI", "INDD", "RAW");
        if (fileExtensions.stream().noneMatch(f -> f.equalsIgnoreCase(FilenameUtils.getExtension(file.getOriginalFilename())))) {
            throw new ArgumentException("Wrong file extension");
        }
        File convFile = new File(file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convFile)) {
            fos.write(file.getBytes());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return convFile;
    }

    private String generateFileName(File file) {
        return new Date().getTime() + "-" + file.getName().replace(" ", "_");
    }

    private void uploadFileTos3bucket(String fileName, File file) {
        s3client.putObject(new PutObjectRequest(bucketName, fileName, file)
                .withCannedAcl(CannedAccessControlList.PublicRead));
    }

    public String uploadFile(File file) {
        String fileName = generateFileName(file);
        String fileUrl = "https://s3." + region + ".amazonaws.com/" + bucketName + "/" + fileName;
        uploadFileTos3bucket(fileName, file);
        if (!file.delete()) {
            log.error("File " + fileName + " has not been removed");
        }
        return fileUrl;
    }

    public void deleteFileFromS3Bucket(String fileUrl) {
        String fileName = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
        s3client.deleteObject(new DeleteObjectRequest(bucketName, fileName));
    }

}