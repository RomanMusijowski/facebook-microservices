package com.mic.post.dto;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel(description = "PhotoDTO ")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PhotoDTO {
    private Long id;
    private String url;
}
