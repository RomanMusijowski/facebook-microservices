package com.mic.post.service;

import com.mic.post.domain.Post;
import com.mic.post.payload.UserInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PostService {

    List<Post> getLatestFriendsPosts();

    Page<Post> getUserPosts(Pageable pageable, Long userId);

    void createPost(String content, MultipartFile[] files);

    Post editPost(Long postId, String newContent, MultipartFile[] multipartFiles, List<Long> photosIdToDelete);

    void deletePost(Long id);

    void likePost(Long postId);

    void unlikePost(Long postId);

    Page<UserInfo> getLikingUserInfo(Pageable pageable, Long postId);
}

