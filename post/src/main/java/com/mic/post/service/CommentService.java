package com.mic.post.service;

import com.mic.post.payload.UserInfo;
import com.mic.post.domain.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentService {
    Page<Comment> getAllPostComments(Pageable pageable, Long postId);

    void addComment(Long postId, String content);

    Comment updateComment(Long postId, Long commentId, String newContent);

    void deleteComment(Long postId, Long commentId);

    void likeComment(Long postId, Long commentId);

    void unlikeComment(Long postId, Long commentId);

    Page<UserInfo> getLikingUserInfo(Pageable pageable, Long postId, Long commentId);
}
