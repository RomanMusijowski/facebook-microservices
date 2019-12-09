package com.mic.post.repository;

import com.mic.post.domain.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> getAllByPostId(Pageable pageable, Long postId);

    Optional<Comment> getAllByIdAndPostId(Long commentId, Long postId);

    List<Comment> findTop10ByPostIdOrderByCreatedDateDesc(Long postId);
}
