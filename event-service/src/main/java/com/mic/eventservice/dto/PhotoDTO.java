package com.mic.eventservice.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel(description = "PhotoDTO ")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PhotoDTO {

    @ApiModelProperty(notes = "Photo id.")
    private Long id;
    @ApiModelProperty(notes = "Photo url.")
    private String url;
}
