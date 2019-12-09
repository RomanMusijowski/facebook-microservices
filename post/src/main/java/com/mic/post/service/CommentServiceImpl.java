package com.mic.post.service;

import com.mic.post.domain.CommentLike;
import com.mic.post.payload.UserInfo;
import com.mic.post.repository.CommentLikeRepository;
import com.mic.post.client.AuthClient;
import com.mic.post.domain.Comment;
import com.mic.post.domain.Post;
import com.mic.post.repository.CommentRepository;
import com.mic.post.repository.PostRepository;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Log4j2
public class CommentServiceImpl implements CommentService {
    private final AuthClient authClient;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;

    @Override
    public Page<Comment> getAllPostComments(Pageable pageable, Long postId) {
        return commentRepository.getAllByPostId(pageable, postId);
    }

    @Override
    public void addComment(Long postId, String content) {
        UserInfo userInfo = authClient.getCurrentUserInfo();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found!"));
        commentRepository.save(new Comment(null, userInfo.getId(), post, content, 0));
        log.info("User " + userInfo.getUsername() + " added a comment to post with id = " + post.getId());
    }

    @Override
    public Comment updateComment(Long postId, Long commentId, String newContent) {
        UserInfo userInfo = authClient.getCurrentUserInfo();
        postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found!"));
        Comment comment = commentRepository.getAllByIdAndPostId(commentId, postId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found!"));
        if (comment.getUserId().equals(userInfo.getId())) {
            comment.setContent(newContent);
            return commentRepository.save(comment);
        } else {
            throw new SecurityException("You are not authorized for this action!");
        }
    }

    @Override
    public void deleteComment(Long postId, Long commentId) {
        UserInfo userInfo = authClient.getCurrentUserInfo();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found!"));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found!"));
        if (comment.getUserId().equals(userInfo.getId())) {
            if (!post.getComments().contains(comment)) {
                throw new EntityNotFoundException("Comment not found!");
            }
            commentRepository.delete(comment);
        } else {
            throw new SecurityException("You are not authorized for this action!");
        }
    }

    @Override
    public void likeComment(Long postId, Long commentId) {
        if (!postRepository.existsById(postId)) {
            throw new EntityNotFoundException("Post not found!");
        }
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found!"));
        UserInfo userInfo = authClient.getCurrentUserInfo();
        if (commentLikeRepository.existsByCommentIdAndUserId(commentId, userInfo.getId())) {
            throw new DataIntegrityViolationException("You already liked this comment!");
        }
        CommentLike commentLike = new CommentLike(null, postId, userInfo.getId());
        commentLikeRepository.save(commentLike);
        comment.setLikes(commentLikeRepository.countByCommentId(commentId));
        commentRepository.save(comment);
    }

    @Override
    public void unlikeComment(Long postId, Long commentId) {
        if (!postRepository.existsById(postId)) {
            throw new EntityNotFoundException("Post not found!");
        }
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found!"));
        UserInfo userInfo = authClient.getCurrentUserInfo();
        CommentLike commentLike = commentLikeRepository.findByCommentIdAndUserId(commentId, userInfo.getId())
                .orElseThrow(() -> new EntityNotFoundException("Like not found!"));
        commentLikeRepository.delete(commentLike);
        comment.setLikes(commentLikeRepository.countByCommentId(commentId));
        commentRepository.save(comment);
    }

    @Override
    public Page<UserInfo> getLikingUserInfo(Pageable pageable, Long postId, Long commentId) {
        if (!postRepository.existsById(postId)) {
            throw new EntityNotFoundException("Post not found!");
        }
        if (!commentRepository.existsById(commentId)) {
            throw new EntityNotFoundException("Comment not found!");
        }
        List<UserInfo> userInfoList = authClient.getUserProfiles(pageable,
                commentLikeRepository.getAllByCommentId(commentId).stream().map(CommentLike::getUserId)
                        .collect(Collectors.toList()));
        return new PageImpl<>(userInfoList, pageable, userInfoList.size());
    }
}
