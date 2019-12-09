package com.mic.post.payload;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ContentPayload {
    @NotBlank
    String content;
}
