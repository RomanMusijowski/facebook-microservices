package com.mic.eventservice.repository;

import com.mic.eventservice.domain.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {
    List<Photo> getAllByIdIn(List<Long> id);
}
