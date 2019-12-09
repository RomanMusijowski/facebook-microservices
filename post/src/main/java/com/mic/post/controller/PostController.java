package com.mic.post.controller;

import com.mic.post.dto.CommentDTO;
import com.mic.post.dto.PostDTO;
import com.mic.post.payload.UserInfo;
import com.mic.post.service.CommentServiceImpl;
import com.mic.post.service.PostServiceImpl;
import com.mic.post.payload.ContentPayload;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Api(value = "PostController")
@AllArgsConstructor
@RequestMapping("/api/post")
@RestController
public class PostController {

    private final PostServiceImpl postService;
    private final CommentServiceImpl commentService;
    private final ModelMapper modelMapper;

    @ApiOperation(value = "Get all friends posts endpoint")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved post.")
    })
    @GetMapping
    public List<PostDTO> getAllPosts() {
        return postService.getLatestFriendsPosts().stream()
                .map(post -> modelMapper.map(post, PostDTO.class)).collect(Collectors.toList());
    }

    @ApiOperation(value = "Get all user posts endpoint")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved post.")
    })
    @GetMapping("/user/{userId}")
    public Page<PostDTO> getAllUserPosts(Pageable pageable,
                                         @PathVariable("userId") Long userId) {
        return postService.getUserPosts(pageable, userId)
                .map(post -> modelMapper.map(post, PostDTO.class));
    }

    @ApiOperation(value = "Create post endpoint")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created post.")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public void createPost(@RequestPart(value = "content") @Valid String content,
                           @RequestPart(value = "files") MultipartFile[] files) {
        postService.createPost(content, files);
    }

    @ApiOperation(value = "Update post endpoint")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successfully updated post."),
            @ApiResponse(code = 403, message = "You are not authorized for this action!.")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/{postId}")
    public PostDTO editPost(@PathVariable("postId") Long postId,
                            @RequestParam(value = "content", required = false) @Valid String content,
                            @RequestPart(value = "files", required = false) MultipartFile[] files,
                            @RequestParam(value = "photoIdToDelete", required = false) List<Long> photoIdToDelete
    ) {
        return modelMapper.map(postService.editPost(postId, content, files, photoIdToDelete), PostDTO.class);
    }

    @ApiOperation(value = "Delete post endpoint")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successfully deleted post."),
            @ApiResponse(code = 403, message = "You are not authorized for this action!.")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deletePost(@PathVariable("id") Long id) {
        postService.deletePost(id);
    }

    @ApiOperation(value = "Get all post comments endpoint")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved  comments."),
    })
    @GetMapping("/{id}/comment")
    public Page<CommentDTO> getAllPostComments(Pageable pageable, @PathVariable("id") Long postId) {
        return commentService.getAllPostComments(pageable, postId)
                .map(comment -> modelMapper.map(comment, CommentDTO.class));
    }

    @ApiOperation(value = "Add comment endpoint")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully added  comment."),
    })
    @PostMapping("/{id}/comment")
    public void addComment(@PathVariable("id") Long postId, @RequestBody @Valid ContentPayload contentPayload) {
        commentService.addComment(postId, contentPayload.getContent());
    }

    @ApiOperation(value = "Update comment endpoint")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully updated  comment."),
            @ApiResponse(code = 403, message = "You are not authorized for this action!.")
    })
    @PutMapping("/{postId}/comment/{commentId}")
    public CommentDTO editComment(@PathVariable("postId") Long postId,
                                  @PathVariable("commentId") Long commentId,
                                  @RequestBody @Valid ContentPayload contentPayload) {
        return modelMapper.map(commentService.updateComment(postId, commentId, contentPayload.getContent()), CommentDTO.class);
    }

    @ApiOperation(value = "Delete comment endpoint")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successfully deleted  comment."),
            @ApiResponse(code = 403, message = "You are not authorized for this action!")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{postid}/comment/{commentId}")
    public void deleteComment(@PathVariable("postid") Long postId, @PathVariable("commentId") Long commentId) {
        commentService.deleteComment(postId, commentId);
    }

    @ApiOperation(value = "Liked post endpoint")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved list of users who liked post."),
    })
    @GetMapping("/{postId}/liked")
    public Page<UserInfo> likedPost(Pageable pageable,
                                    @PathVariable("postId") Long postId) {
        return postService.getLikingUserInfo(pageable, postId);
    }

    @ApiOperation(value = "Like post endpoint")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully liked post."),
            @ApiResponse(code = 409, message = "You already liked this post!")
    })
    @GetMapping("/{postId}/like")
    public void likePost(@PathVariable("postId") Long postId) {
        postService.likePost(postId);
    }

    @ApiOperation(value = "Unlike post endpoint")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully unliked post."),
            @ApiResponse(code = 400, message = "Post not found!")
    })
    @GetMapping("/{postId}/unlike")
    public void unlikePost(@PathVariable("postId") Long postId) {
        postService.unlikePost(postId);
    }

    @ApiOperation(value = "Liked comment endpoint")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved list of users who liked comment."),
    })
    @GetMapping("/{postId}/comment/{commentId}/liked")
    public Page<UserInfo> likedComment(Pageable pageable,
                                       @PathVariable("postId") Long postId,
                                       @PathVariable("commentId") Long commentId) {
        return commentService.getLikingUserInfo(pageable, postId, commentId);
    }

    @ApiOperation(value = "Like comment endpoint")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully liked comment."),
            @ApiResponse(code = 409, message = "You already liked this comment!")
    })
    @GetMapping("/{postId}/comment/{commentId}/like")
    public void likeComment(@PathVariable("postId") Long postId,
                            @PathVariable("commentId") Long commentId) {
        commentService.likeComment(postId, commentId);
    }

    @ApiOperation(value = "Unlike comment endpoint")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully unliked comment."),
            @ApiResponse(code = 400, message = "Comment not found!")
    })
    @GetMapping("/{postId}/comment/{commentId}/unlike")
    public void unlikeComment(@PathVariable("postId") Long postId,
                              @PathVariable("commentId") Long commentId) {
        commentService.unlikeComment(postId, commentId);
    }


}
