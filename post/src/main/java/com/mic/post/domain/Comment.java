package com.mic.post.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "COMMENT")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Comment extends Auditable<String> implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", unique = true, nullable = false)
    private Long id;
    @Column(name = "USER_ID", nullable = false)
    private Long userId;
    @ManyToOne
    @JoinColumn(name = "POST_ID", nullable = false)
    private Post post;
    @Column(name = "CONTENT", nullable = false)
    @JsonIgnore
    private String content;
    @Column(name = "LIKES", nullable = false)
    private Integer likes;
}
