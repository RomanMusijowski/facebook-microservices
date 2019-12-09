package com.mic.post.service;

import com.mic.post.domain.Comment;
import com.mic.post.domain.Post;
import com.mic.post.domain.PostLike;
import com.mic.post.payload.UserInfo;
import com.mic.post.repository.PostLikeRepository;
import com.mic.post.client.AuthClient;
import com.mic.post.repository.CommentRepository;
import com.mic.post.repository.PhotoRepository;
import com.mic.post.repository.PostRepository;
import com.mic.s3client.AmazonClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;

import javax.persistence.EntityNotFoundException;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class PostServiceImplTest {
    @InjectMocks
    private PostServiceImpl postService;
    @Mock
    private AuthClient authClient;
    @Mock
    private PostRepository postRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private AmazonClient amazonClient;
    @Mock
    private PhotoRepository photoRepository;
    @Mock
    private PostLikeRepository postLikeRepository;
    @Mock
    private PostServiceCache postServiceCache;

    private Post post;
    private UserInfo userInfo;
    private UserInfo userInfo2;
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
        userInfo2 = new UserInfo(2L, "username2", "email2",
                "firstname2", "lastname2", "phone2",
                "gender2", Collections.emptyList(), true, new ArrayList<>(),
                localDateTime, "user", localDateTime);
        userInfo.setFriends(Collections.singletonList(userInfo2));
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
    public void shouldReturnAllFriendPostsPage() {
        post.setUserId(userInfo2.getId());
        Mockito.when(authClient.getCurrentUserInfo())
                .thenReturn(userInfo);
        Mockito.when(authClient.getAllUserFriendsId(userInfo.getId()))
                .thenReturn(Collections.singletonList(userInfo2.getId()));
        Mockito.when(postRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(Collections.singletonList(post)));
        Mockito.when(commentRepository.findTop10ByPostIdOrderByCreatedDateDesc(post.getId()))
                .thenReturn(comments);
        Mockito.when(postServiceCache.getTopUserPosts(userInfo2.getId()))
                .thenReturn(Collections.singletonList(post));
        List<Post> out = postService.getLatestFriendsPosts();
        Assert.assertEquals(post.getId(), out.get(0).getId());
        Assert.assertEquals(post.getUserId(), out.get(0).getUserId());
        Assert.assertEquals(post.getContent(), out.get(0).getContent());
    }

    @Test
    public void shouldCreatePost() {
        post.setId(null);
        Mockito.when(authClient.getCurrentUserInfo())
                .thenReturn(userInfo);
        Mockito.when(amazonClient.prepareFiles(mockMultipartFile))
                .thenReturn(file);
        postService.createPost(post.getContent(), files);
        verify(postServiceCache, times(1)).createPost(post, userInfo.getId(), files);
    }

    @Test
    public void shouldUpdatePost() {
        Mockito.when(authClient.getCurrentUserInfo())
                .thenReturn(userInfo);
        Mockito.when(postRepository.findById(post.getId()))
                .thenReturn(Optional.of(post));
        postService.editPost(post.getId(), "newContent", files, Collections.singletonList(1L));
        verify(postServiceCache, times(1))
                .editPost(post, userInfo.getId(), "newContent", files, Collections.singletonList(1L));
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldReturnEntityNotFoundExceptionDuringEditingPost() {
        post.setUserId(2L);
        Mockito.when(authClient.getCurrentUserInfo())
                .thenReturn(userInfo);
        Mockito.when(postRepository.save(Mockito.any(Post.class)))
                .thenReturn(post);
        postService.editPost(2L, "newContent", files, Collections.singletonList(1L));
    }

    @Test(expected = SecurityException.class)
    public void shouldReturnSecurityExceptionDuringEditingPost() {
        post.setUserId(2L);
        Mockito.when(authClient.getCurrentUserInfo())
                .thenReturn(userInfo);
        Mockito.when(postRepository.findById(post.getId()))
                .thenReturn(Optional.of(post));
        Mockito.when(postRepository.save(Mockito.any(Post.class)))
                .thenReturn(post);
        postService.editPost(post.getId(), "newContent", files, Collections.singletonList(1L));
    }

    @Test
    public void shouldDeletePost() {
        Mockito.when(authClient.getCurrentUserInfo())
                .thenReturn(userInfo);
        Mockito.when(postRepository.findById(post.getId()))
                .thenReturn(Optional.of(post));
        postService.deletePost(post.getId());
        verify(postServiceCache, times(1)).deletePost(post, userInfo.getId());
    }

    @Test(expected = SecurityException.class)
    public void shouldReturnSecurityExceptionDuringDeleting() {
        post.setUserId(2L);
        Mockito.when(authClient.getCurrentUserInfo())
                .thenReturn(userInfo);
        Mockito.when(postRepository.findById(post.getId()))
                .thenReturn(Optional.of(post));
        postService.deletePost(post.getId());
    }

    @Test
    public void shouldLikePost() {
        Mockito.when(authClient.getCurrentUserInfo())
                .thenReturn(userInfo);
        Mockito.when(postRepository.findById(post.getId()))
                .thenReturn(Optional.of(post));
        Mockito.when(postLikeRepository.existsByPostIdAndUserId(post.getId(), userInfo.getId()))
                .thenReturn(false);
        postService.likePost(post.getId());
        verify(postServiceCache, times(1)).likePost(post, userInfo.getId(), post.getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldThrowEntityNotFoundExceptionDuringLikePost() {
        Mockito.when(authClient.getCurrentUserInfo())
                .thenReturn(userInfo);
        Mockito.when(postRepository.findById(post.getId()))
                .thenReturn(Optional.of(post));
        Mockito.when(postLikeRepository.existsByPostIdAndUserId(post.getId(), userInfo.getId()))
                .thenReturn(false);
        post.setId(2L);
        postService.likePost(post.getId());
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void shouldThrowDataIntegrityViolationExceptionDuringLikePost() {
        Mockito.when(authClient.getCurrentUserInfo())
                .thenReturn(userInfo);
        Mockito.when(postRepository.findById(post.getId()))
                .thenReturn(Optional.of(post));
        Mockito.when(postLikeRepository.existsByPostIdAndUserId(post.getId(), userInfo.getId()))
                .thenReturn(true);
        postService.likePost(post.getId());
    }

    @Test
    public void shouldUnlikePost() {
        Mockito.when(authClient.getCurrentUserInfo())
                .thenReturn(userInfo);
        Mockito.when(postRepository.findById(post.getId()))
                .thenReturn(Optional.of(post));
        Mockito.when(postLikeRepository.findByPostIdAndUserId(post.getId(), userInfo.getId()))
                .thenReturn(Optional.of(postLike));
        postService.unlikePost(post.getId());
        verify(postServiceCache, times(1)).unlikePost(post, postLike, userInfo.getId());

    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldThrowEntityNotFoundExceptionDuringUnlikePostWhenPostNotFound() {
        Mockito.when(authClient.getCurrentUserInfo())
                .thenReturn(userInfo);
        Mockito.when(postRepository.findById(post.getId()))
                .thenReturn(Optional.empty());
        Mockito.when(postLikeRepository.findByPostIdAndUserId(post.getId(), userInfo.getId()))
                .thenReturn(Optional.of(postLike));
        postService.unlikePost(post.getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldThrowEntityNotFoundExceptionDuringUnlikePostWhenLikeNotFound() {
        Mockito.when(authClient.getCurrentUserInfo())
                .thenReturn(userInfo);
        Mockito.when(postRepository.findById(post.getId()))
                .thenReturn(Optional.of(post));
        Mockito.when(postLikeRepository.findByPostIdAndUserId(post.getId(), userInfo.getId()))
                .thenReturn(Optional.empty());
        postService.unlikePost(post.getId());
    }

    @Test
    public void shouldReturnLikingUserInfo() {
        Mockito.when(postRepository.existsById(post.getId()))
                .thenReturn(true);
        Mockito.when(authClient.getUserProfiles(pageable, Collections.singletonList(userInfo.getId())))
                .thenReturn(Collections.singletonList(userInfo));
        Mockito.when(postLikeRepository.getAllByPostId(pageable, post.getId()))
                .thenReturn(new PageImpl<>(Collections.singletonList(postLike)));
        Page<UserInfo> out = postService.getLikingUserInfo(pageable, post.getId());
        Assert.assertEquals(userInfo.getId(), out.getContent().get(0).getId());
        Assert.assertEquals(userInfo.getUsername(), out.getContent().get(0).getUsername());
        Assert.assertEquals(userInfo.getEmail(), out.getContent().get(0).getEmail());
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldThrowEntityNotFoundExceptionDuringReturnLikingUserInfoWhenPostNotFound() {
        Mockito.when(postRepository.existsById(post.getId()))
                .thenReturn(false);
        postService.getLikingUserInfo(pageable, post.getId());
    }

}
