package com.mic.post.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "COMMENT_LIKE")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", unique = true, nullable = false)
    private Long id;

    @Column(name = "COMMENT_ID", nullable = false)
    private Long commentId;

    @Column(name = "USER_ID", nullable = false)
    private Long userId;
}
