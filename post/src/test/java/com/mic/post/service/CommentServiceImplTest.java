package com.mic.post.service;

import com.mic.post.domain.Comment;
import com.mic.post.domain.CommentLike;
import com.mic.post.domain.Post;
import com.mic.post.payload.UserInfo;
import com.mic.post.repository.CommentLikeRepository;
import com.mic.post.client.AuthClient;
import com.mic.post.repository.CommentRepository;
import com.mic.post.repository.PostRepository;
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

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class CommentServiceImplTest {

    @InjectMocks
    private CommentServiceImpl commentService;
    @Mock
    private AuthClient authClient;
    @Mock
    private PostRepository postRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private CommentLikeRepository commentLikeRepository;

    private Post post;
    private Comment comment;
    private Pageable pageable;
    private UserInfo userInfo;
    private CommentLike commentLike;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        post = new Post(1L, 1L, "content", new ArrayList<>(), new ArrayList<>(), 0);
        comment = new Comment(1L, 1L, post, "content", 0);
        post.getComments().add(comment);
        pageable = PageRequest.of(0, 3);
        LocalDateTime localDateTime = LocalDateTime.now();
        userInfo = new UserInfo(1L, "username", "email",
                "firstname", "lastname", "phone",
                "gender", Collections.emptyList(), true, new ArrayList<>(),
                localDateTime, "user", localDateTime);
        commentLike = new CommentLike(null, comment.getId(), userInfo.getId());
    }

    @Test
    public void shouldReturnAllPostCommentsPage() {
        Mockito.when(commentRepository.getAllByPostId(pageable, post.getId()))
                .thenReturn(new PageImpl<>(Collections.singletonList(comment)));
        Page<Comment> out = commentService.getAllPostComments(pageable, 1L);
        Assert.assertEquals(comment.getId(), out.getContent().get(0).getId());
        Assert.assertEquals(comment.getContent(), out.getContent().get(0).getContent());
    }

    @Test
    public void shouldAddCommentToPost() {
        post.setComments(new ArrayList<>());
        comment.setId(null);
        Mockito.when(authClient.getCurrentUserInfo())
                .thenReturn(userInfo);
        Mockito.when(postRepository.findById(post.getId()))
                .thenReturn(Optional.of(post));
        Mockito.when(commentRepository.save(Mockito.any(Comment.class)))
                .thenReturn(comment);
        commentService.addComment(1L, comment.getContent());
        verify(commentRepository, times(1)).save(comment);
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldThrowEntityNotFoundExceptionDuringAddingCommentToNonExistingPost() {
        Mockito.when(authClient.getCurrentUserInfo())
                .thenReturn(userInfo);
        Mockito.when(postRepository.findById(post.getId()))
                .thenReturn(Optional.empty());
        commentService.addComment(1L, comment.getContent());
    }

    @Test
    public void shouldUpdateComment() {
        Mockito.when(authClient.getCurrentUserInfo())
                .thenReturn(userInfo);
        Mockito.when(postRepository.findById(post.getId()))
                .thenReturn(Optional.of(post));
        Mockito.when(commentRepository.getAllByIdAndPostId(comment.getId(), post.getId()))
                .thenReturn(Optional.of(comment));
        Mockito.when(commentRepository.save(Mockito.any(Comment.class)))
                .thenReturn(comment);
        commentService.updateComment(1L, 1L, "newContent");
        verify(commentRepository, times(1)).save(comment);
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldThrowEntityNotFoundExceptionDuringUpdatingCommentWhenPostDoNotExist() {
        Mockito.when(authClient.getCurrentUserInfo())
                .thenReturn(userInfo);
        Mockito.when(postRepository.findById(post.getId()))
                .thenReturn(Optional.empty());
        commentService.updateComment(1L, 1L, "newContent");
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldThrowEntityNotFoundExceptionDuringUpdatingCommentWhenPostDontHaveThisComment() {
        Mockito.when(authClient.getCurrentUserInfo())
                .thenReturn(userInfo);
        Mockito.when(postRepository.findById(post.getId()))
                .thenReturn(Optional.of(post));
        commentService.updateComment(1L, 1L, "newContent");
    }

    @Test(expected = SecurityException.class)
    public void shouldThrowSecurityExceptionDuringUpdatingCommentWhenUserIsNotCommentAuthor() {
        comment.setUserId(2L);
        Mockito.when(authClient.getCurrentUserInfo())
                .thenReturn(userInfo);
        Mockito.when(commentRepository.getAllByIdAndPostId(comment.getId(), post.getId()))
                .thenReturn(Optional.of(comment));
        Mockito.when(postRepository.findById(post.getId()))
                .thenReturn(Optional.of(post));
        commentService.updateComment(1L, 1L, "newContent");
    }

    @Test
    public void shouldDeleteComment() {
        Mockito.when(authClient.getCurrentUserInfo())
                .thenReturn(userInfo);
        Mockito.when(postRepository.findById(post.getId()))
                .thenReturn(Optional.of(post));
        Mockito.when(commentRepository.findById(comment.getId()))
                .thenReturn(Optional.of(comment));
        commentService.deleteComment(1L, 1L);
        verify(commentRepository, times(1)).delete(comment);
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldThrowEntityNotFoundExceptionDuringDeletingCommentWhenPostDoNotExist() {
        Mockito.when(authClient.getCurrentUserInfo())
                .thenReturn(userInfo);
        Mockito.when(postRepository.findById(post.getId()))
                .thenReturn(Optional.empty());
        commentService.deleteComment(1L, 1L);
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldThrowEntityNotFoundExceptionDuringDeletingCommentWhenCommentDoNotExist() {
        Mockito.when(authClient.getCurrentUserInfo())
                .thenReturn(userInfo);
        Mockito.when(postRepository.findById(post.getId()))
                .thenReturn(Optional.of(post));
        Mockito.when(commentRepository.findById(comment.getId()))
                .thenReturn(Optional.empty());
        commentService.deleteComment(1L, 1L);
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldEntityNotFoundExceptionDuringDeletingCommentWhenPostDontHaveThatComment() {
        post.setComments(Collections.emptyList());
        Mockito.when(authClient.getCurrentUserInfo())
                .thenReturn(userInfo);
        Mockito.when(postRepository.findById(post.getId()))
                .thenReturn(Optional.of(post));
        Mockito.when(commentRepository.findById(comment.getId()))
                .thenReturn(Optional.of(comment));
        commentService.deleteComment(1L, 1L);
    }

    @Test(expected = SecurityException.class)
    public void shouldThrowSecurityExceptionDuringDeletingCommentWhenUserIsNotCommentAuthor() {
        comment.setUserId(2L);
        Mockito.when(authClient.getCurrentUserInfo())
                .thenReturn(userInfo);
        Mockito.when(postRepository.findById(post.getId()))
                .thenReturn(Optional.of(post));
        Mockito.when(commentRepository.findById(comment.getId()))
                .thenReturn(Optional.of(comment));
        commentService.deleteComment(1L, 1L);
    }

    @Test
    public void shouldLikeComment() {
        Mockito.when(postRepository.existsById(post.getId()))
                .thenReturn(true);
        Mockito.when(commentRepository.findById(comment.getId()))
                .thenReturn(Optional.of(comment));
        Mockito.when(authClient.getCurrentUserInfo())
                .thenReturn(userInfo);
        Mockito.when(commentLikeRepository.existsByCommentIdAndUserId(comment.getId(), userInfo.getId()))
                .thenReturn(false);
        commentService.likeComment(post.getId(), comment.getId());
        verify(commentLikeRepository, times(1)).save(commentLike);
        verify(commentRepository, times(1)).save(comment);
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldThrowEntityNotFoundExceptionDuringLikeCommentWhenPostNotFound() {
        Mockito.when(postRepository.existsById(post.getId()))
                .thenReturn(false);
        commentService.likeComment(post.getId(), comment.getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldThrowEntityNotFoundExceptionDuringLikeCommentWhenCommentNotFound() {
        Mockito.when(postRepository.existsById(post.getId()))
                .thenReturn(true);
        Mockito.when(commentRepository.findById(comment.getId()))
                .thenReturn(Optional.empty());
        commentService.likeComment(post.getId(), comment.getId());
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void shouldThrowDataIntegrityViolationExceptionDuringLikeCommentWhenAlreadyLiked() {
        Mockito.when(postRepository.existsById(post.getId()))
                .thenReturn(true);
        Mockito.when(commentRepository.findById(comment.getId()))
                .thenReturn(Optional.of(comment));
        Mockito.when(authClient.getCurrentUserInfo())
                .thenReturn(userInfo);
        Mockito.when(commentLikeRepository.existsByCommentIdAndUserId(comment.getId(), userInfo.getId()))
                .thenReturn(true);
        commentService.likeComment(post.getId(), comment.getId());
    }

    @Test
    public void shouldUnlikeComment() {
        Mockito.when(postRepository.existsById(post.getId()))
                .thenReturn(true);
        Mockito.when(commentRepository.findById(comment.getId()))
                .thenReturn(Optional.of(comment));
        Mockito.when(authClient.getCurrentUserInfo())
                .thenReturn(userInfo);
        Mockito.when(commentLikeRepository.findByCommentIdAndUserId(comment.getId(), userInfo.getId()))
                .thenReturn(Optional.of(commentLike));
        commentService.unlikeComment(post.getId(), comment.getId());
        verify(commentLikeRepository, times(1)).delete(commentLike);
        verify(commentRepository, times(1)).save(comment);
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldThrowEntityNotFoundExceptionDuringUnlikeCommentWhenPostNotFound() {
        Mockito.when(postRepository.existsById(post.getId()))
                .thenReturn(false);
        commentService.unlikeComment(post.getId(), comment.getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldThrowEntityNotFoundExceptionDuringUnlikeCommentWhenCommentNotFound() {
        Mockito.when(commentRepository.findById(comment.getId()))
                .thenReturn(Optional.empty());
        commentService.unlikeComment(post.getId(), comment.getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldThrowEntityNotFoundExceptionDuringUnlikeCommentWhenLikeNotFound() {
        Mockito.when(postRepository.existsById(post.getId()))
                .thenReturn(true);
        Mockito.when(commentRepository.findById(comment.getId()))
                .thenReturn(Optional.of(comment));
        Mockito.when(authClient.getCurrentUserInfo())
                .thenReturn(userInfo);
        Mockito.when(commentLikeRepository.findByCommentIdAndUserId(comment.getId(), userInfo.getId()))
                .thenReturn(Optional.empty());
        commentService.unlikeComment(post.getId(), comment.getId());
    }

    @Test
    public void shouldReturnLikingUserInfo() {
        Mockito.when(postRepository.existsById(post.getId()))
                .thenReturn(true);
        Mockito.when(commentRepository.existsById(comment.getId()))
                .thenReturn(true);
        Mockito.when(authClient.getUserProfiles(pageable, Collections.singletonList(userInfo.getId())))
                .thenReturn(Collections.singletonList(userInfo));
        Mockito.when(commentLikeRepository.getAllByCommentId(comment.getId()))
                .thenReturn(Collections.singletonList(commentLike));
        Page<UserInfo> out = commentService.getLikingUserInfo(pageable, post.getId(), comment.getId());
        Assert.assertEquals(userInfo.getId(), out.getContent().get(0).getId());
        Assert.assertEquals(userInfo.getUsername(), out.getContent().get(0).getUsername());
        Assert.assertEquals(userInfo.getEmail(), out.getContent().get(0).getEmail());
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldThrowEntityNotFoundExceptionDuringReturnLikingUserInfoWhenPostNotFound() {
        Mockito.when(postRepository.existsById(post.getId()))
                .thenReturn(false);
        commentService.getLikingUserInfo(pageable, post.getId(), comment.getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldThrowEntityNotFoundExceptionDuringReturnLikingUserInfoWhenCommentNotFound() {
        Mockito.when(postRepository.existsById(post.getId()))
                .thenReturn(true);
        Mockito.when(commentRepository.existsById(comment.getId()))
                .thenReturn(false);
        commentService.getLikingUserInfo(pageable, post.getId(), comment.getId());
    }

}
