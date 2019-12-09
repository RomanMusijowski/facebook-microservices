package com.mic.post.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

@ApiModel(description = "PostDTO ")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostDTO {
    @ApiModelProperty(notes = "The database generated post ID")
    private Long id;
    @ApiModelProperty(notes = "User ID")
    @NotNull(message = "User id is required.")
    private Long userId;
    @ApiModelProperty(notes = "Post content")
    @NotNull(message = "Content is required.")
    private String content;
    @ApiModelProperty(notes = "Post likes")
    private Integer likes;
    @ApiModelProperty(notes = "Post comments")
    private List<CommentDTO> comments;
    @ApiModelProperty(notes = "Post photos")
    private List<PhotoDTO> photos;
}
