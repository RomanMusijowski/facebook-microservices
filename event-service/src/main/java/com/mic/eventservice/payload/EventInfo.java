package com.mic.eventservice.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventInfo {

    @NotBlank
    private Long id;
    @NotBlank
    private Long userId;
    @NotBlank
    private String name;
    @NotBlank
    private String description;
    @NotBlank
    private LocalDateTime dateTime;
    @NotBlank
    private List<Long> userIds;
    @NotBlank
    private String lastModifiedBy;
    @NotBlank
    private LocalDateTime lastModifiedDate;
    @NotBlank
    private LocalDateTime createdDate;
}
