package com.mic.eventservice.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "EVENT_PHOTO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", unique = true, nullable = false)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "EVENT_ID", nullable = false)
    private Event event;
    @Column(name = "URL", nullable = false)
    private String url;
}
