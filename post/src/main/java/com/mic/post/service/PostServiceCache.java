package com.mic.post.service;

import com.mic.post.domain.Photo;
import com.mic.post.domain.Post;
import com.mic.post.domain.PostLike;
import com.mic.post.repository.PostLikeRepository;
import com.mic.post.repository.PhotoRepository;
import com.mic.post.repository.PostRepository;
import com.mic.s3client.AmazonClient;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Log4j2
public class PostServiceCache {

    private final PostRepository postRepository;
    private final AmazonClient amazonClient;
    private final PhotoRepository photoRepository;
    private final PostLikeRepository postLikeRepository;

    @Cacheable(cacheNames = "UserTopPosts", key = "#userId")
    public List<Post> getTopUserPosts(Long userId) {
        log.info("user " + userId + " top posts not from cache");
        return postRepository.getTop20ByUserIdOrderByCreatedDateDesc(userId);
    }


    @CacheEvict(value = "UserTopPosts", key = "#userId")
    public void createPost(Post newPost, Long userId, MultipartFile[] multipartFiles) {
        addPhotos(multipartFiles, postRepository.save(newPost));
    }

    @CacheEvict(value = "UserTopPosts", key = "#userId")
    public Post editPost(Post post, Long userId, String newContent, MultipartFile[] multipartFiles, List<Long> photosIdToDelete) {
        setPostContent(post, newContent);
        addPhotosToPost(post, multipartFiles);
        deletePhotosFromPost(photosIdToDelete);
        return postRepository.save(post);

    }

    private void setPostContent(Post post, String content) {
        if (content != null) {
            post.setContent(content);
        }
    }

    private void addPhotosToPost(Post post, MultipartFile[] multipartFiles) {
        if (multipartFiles != null) {
            addPhotos(multipartFiles, post);
        }
    }

    private void deletePhotosFromPost(List<Long> photosIdToDelete) {
        if (photosIdToDelete != null) {
            photoRepository.getAllByIdIn(photosIdToDelete)
                    .forEach(photo -> {
                        amazonClient.deleteFileFromS3Bucket(photo.getUrl());
                        photoRepository.delete(photo);
                    });
        }
    }

    private void addPhotos(MultipartFile[] multipartFiles, Post post) {
        Set<File> files = Arrays.stream(multipartFiles).map(amazonClient::prepareFiles).collect(Collectors.toSet());
        List<String> url = files.stream().map(amazonClient::uploadFile).collect(Collectors.toList());
        url.forEach(s -> photoRepository.save(new Photo(null, post, s)));
    }

    @CacheEvict(value = "UserTopPosts", key = "#userId")
    public void deletePost(Post post, Long userId) {
        post.getPhotos().forEach(photo -> {
            amazonClient.deleteFileFromS3Bucket(photo.getUrl());
            photoRepository.delete(photo);
        });
        postRepository.delete(post);
    }

    @CacheEvict(value = "UserTopPosts", key = "#postOwnerId")
    public void likePost(Post post, Long userId, Long postOwnerId) {
        PostLike postLike = new PostLike(null, post.getId(), userId);
        postLikeRepository.save(postLike);
        post.setLikes(postLikeRepository.countByPostId(post.getId()));
        postRepository.save(post);
    }

    @CacheEvict(value = "UserTopPosts", key = "#postOwnerId")
    public void unlikePost(Post post, PostLike postLike, Long postOwnerId) {
        postLikeRepository.delete(postLike);
        post.setLikes(postLikeRepository.countByPostId(post.getId()));
        postRepository.save(post);
    }

}
