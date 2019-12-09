package com.mic.post.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@ApiModel(description = "CommentDTO ")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentDTO {

    @ApiModelProperty(notes = "The database generated comment ID")
    private Long id;

    @ApiModelProperty(notes = "User id")
    @NotNull(message = "User id is required.")
    private Long userId;

    @ApiModelProperty(notes = "Comment content")
    @NotNull(message = "Content is required.")
    private String content;

    @ApiModelProperty(notes = "Comment likes")
    private Integer likes;

    @ApiModelProperty(notes = "Comment create date")
    private LocalDateTime createdDate;
}
