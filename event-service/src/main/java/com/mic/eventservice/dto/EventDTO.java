package com.mic.eventservice.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;
import java.util.List;

@ApiModel(description = "EventDTO ")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventDTO {

    @Null(message = "Id must be null")
    @ApiModelProperty(notes = "The database generated event ID")
    private Long id;

    @Null(message = "User ID user must be null")
    @ApiModelProperty(notes = "User ID")
    private Long userId;

    @ApiModelProperty(notes = "Event name")
    @NotEmpty(message = "Content is required.")
    @Size(min = 4, max = 25, message = "Name must be between 4 and 25 characters long")
    private String name;

    @ApiModelProperty(notes = "Event description")
    @NotEmpty(message = "Content is required.")
    @Size(min = 10, max = 255, message = "Description must be between 10 and 255 characters long")
    private String description;

    @ApiModelProperty(notes = "Event date and time. Format 'yyyy-MM-dd HH:mm'")
    @NotNull(message = "Content is required.")
    private String dateTime;

    @Null(message = "List of user must be null")
    @ApiModelProperty(notes = "List of user ids.")
    private List<Long> userIds;

    @Valid
    @Null(message = "Event photos must be null")
    @ApiModelProperty(notes = "Event photos")
    private List<PhotoDTO> photos;
}

