package com.mic.post.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "POST_PHOTO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Photo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", unique = true, nullable = false)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "POST_ID", nullable = false)
    private Post post;
    @Column(name = "URL", nullable = false)
    private String url;
}
