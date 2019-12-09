package com.mic.post.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "POST")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Post extends Auditable<String> implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", unique = true, nullable = false)
    private Long id;
    @Column(name = "USER_ID", nullable = false)
    private Long userId;
    @Column(name = "CONTENT", nullable = false)
    private String content;
    @OneToMany(mappedBy = "post")
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<Comment> comments;
    @OneToMany(mappedBy = "post")
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<Photo> photos;
    @Column(name = "LIKES", nullable = false)
    private Integer likes;
}
