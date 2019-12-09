package com.mic.post.service;

import com.mic.post.domain.Post;
import com.mic.post.domain.PostLike;
import com.mic.post.payload.UserInfo;
import com.mic.post.repository.PostLikeRepository;
import com.mic.post.client.AuthClient;
import com.mic.post.repository.CommentRepository;
import com.mic.post.repository.PostRepository;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@AllArgsConstructor
@Log4j2
public class PostServiceImpl implements PostService {

    private final AuthClient authClient;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostServiceCache postServiceCache;

    @Override
    public List<Post> getLatestFriendsPosts() {
        UserInfo userInfo = authClient.getCurrentUserInfo();
        List<Long> friendIdList = authClient.getAllUserFriendsId(userInfo.getId());
        List<Post> posts = new ArrayList<>();
        friendIdList.forEach(id -> posts.addAll(postServiceCache.getTopUserPosts(id)));
        posts.forEach(post ->
                post.setComments(commentRepository.findTop10ByPostIdOrderByCreatedDateDesc(post.getId())));
        posts.sort(Comparator.comparing(Post::getId).reversed());
        return posts;
    }

    @Override
    public Page<Post> getUserPosts(Pageable pageable, Long userId) {
        return postRepository.getAllByUserId(pageable, userId);
    }

    @Override
    public void createPost(String content, MultipartFile[] multipartFiles) {
        UserInfo userInfo = authClient.getCurrentUserInfo();
        Post post = new Post(null, userInfo.getId(), content, new ArrayList<>(), new ArrayList<>(), 0);
        postServiceCache.createPost(post, userInfo.getId(), multipartFiles);
        log.info("User " + userInfo.getUsername() + " added a post.");
    }

    @Override
    public Post editPost(Long postId, String newContent, MultipartFile[] multipartFiles, List<Long> photosIdToDelete) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found!"));
        UserInfo userInfo = authClient.getCurrentUserInfo();
        if (!post.getUserId().equals(userInfo.getId())) {
            throw new SecurityException("You are not authorized for this action!");
        }
        log.info("User " + userInfo.getUsername() + " edited a post.");
        return postServiceCache.editPost(post, userInfo.getId(), newContent, multipartFiles, photosIdToDelete);
    }


    @Override
    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("Post not found!"));
        UserInfo userInfo = authClient.getCurrentUserInfo();
        if (!post.getUserId().equals(userInfo.getId())) {
            throw new SecurityException("You are not authorized for this action!");
        }
        postServiceCache.deletePost(post, userInfo.getId());
        log.info("User " + userInfo.getUsername() + " deleted a post.");

    }

    @Override
    public void likePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found!"));
        UserInfo userInfo = authClient.getCurrentUserInfo();
        if (postLikeRepository.existsByPostIdAndUserId(postId, userInfo.getId())) {
            throw new DataIntegrityViolationException("You already liked this post!");
        }
        postServiceCache.likePost(post, userInfo.getId(), post.getUserId());
        log.info("User " + userInfo.getUsername() + " liked a post: " + post.getId());
    }

    @Override
    public void unlikePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found!"));
        UserInfo userInfo = authClient.getCurrentUserInfo();
        PostLike postLike = postLikeRepository.findByPostIdAndUserId(postId, userInfo.getId())
                .orElseThrow(() -> new EntityNotFoundException("Like not found!"));
        postServiceCache.unlikePost(post, postLike, post.getUserId());
        log.info("User " + userInfo.getUsername() + " unliked a post: " + post.getId());
    }

    @Override
    public Page<UserInfo> getLikingUserInfo(Pageable pageable, Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new EntityNotFoundException("Post not found!");
        }
        List<UserInfo> userInfoList = authClient.getUserProfiles(pageable,
                postLikeRepository.getAllByPostId(pageable, postId).map(PostLike::getUserId).getContent());
        return new PageImpl<>(userInfoList, pageable, userInfoList.size());
    }
}
