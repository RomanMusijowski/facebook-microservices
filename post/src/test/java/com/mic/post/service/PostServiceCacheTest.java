package com.mic.post.service;

import com.mic.post.domain.Comment;
import com.mic.post.domain.Post;
import com.mic.post.domain.PostLike;
import com.mic.post.payload.UserInfo;
import com.mic.post.repository.PostLikeRepository;
import com.mic.post.repository.PostRepository;
import com.mic.post.repository.PhotoRepository;
import com.mic.s3client.AmazonClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class PostServiceCacheTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private AmazonClient amazonClient;
    @Mock
    private PhotoRepository photoRepository;
    @Mock
    private PostLikeRepository postLikeRepository;
    @InjectMocks
    private PostServiceCache postServiceCache;

    private Post post;
    private UserInfo userInfo;
    private File file;
    private MockMultipartFile mockMultipartFile;
    private MockMultipartFile[] files;
    private PostLike postLike;
    private List<Comment> comments;
    private Pageable pageable;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        post = new Post(1L, 1L, "content", new ArrayList<>(), new ArrayList<>(), 0);
        LocalDateTime localDateTime = LocalDateTime.now();
        userInfo = new UserInfo(1L, "username", "email",
                "firstname", "lastname", "phone",
                "gender", Collections.emptyList(), true, new ArrayList<>(),
                localDateTime, "user", localDateTime);
        postLike = new PostLike(null, post.getId(), userInfo.getId());
        comments = Collections.singletonList(new Comment(1L, 1L, post, "content", 0));
        pageable = PageRequest.of(0, 3);
        String fileName = "test.txt";
        file = new File("FileUploadController.targetFolder + fileName");
        mockMultipartFile = new MockMultipartFile("user-file", fileName,
                "photo", "test data".getBytes());
        files = new MockMultipartFile[]{mockMultipartFile};
    }

    @Test
    public void shouldCreatePost() {
        post.setId(null);
        Mockito.when(amazonClient.uploadFile(file))
                .thenReturn("link");
        Mockito.when(postRepository.save(Mockito.any(Post.class)))
                .thenReturn(post);
        postServiceCache.createPost(post, userInfo.getId(), files);
        verify(postRepository, times(1)).save(post);
    }

    @Test
    public void shouldUpdatePost() {
        Mockito.when(amazonClient.prepareFiles(mockMultipartFile))
                .thenReturn(file);
        Mockito.when(amazonClient.uploadFile(file))
                .thenReturn("link");
        Mockito.when(postRepository.save(Mockito.any(Post.class)))
                .thenReturn(post);
        postServiceCache.editPost(post, userInfo.getId(), "newContent", files, Collections.singletonList(1L));
        verify(postRepository, times(1)).save(post);
    }

    @Test
    public void shouldDeletePost() {
        postServiceCache.deletePost(post, userInfo.getId());
        verify(postRepository, times(1)).delete(post);
    }

    @Test
    public void shouldLikePost() {
        postServiceCache.likePost(post, userInfo.getId(), post.getId());
        verify(postLikeRepository, times(1)).save(postLike);
        verify(postRepository, times(1)).save(post);
    }

    @Test
    public void shouldUnlikePost() {
        postServiceCache.unlikePost(post, postLike, userInfo.getId());
        verify(postLikeRepository, times(1)).delete(postLike);
        verify(postRepository, times(1)).save(post);
    }
}
