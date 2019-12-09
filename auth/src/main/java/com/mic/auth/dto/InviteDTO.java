package com.mic.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InviteDTO {

    private Long id;

    private Long byUser;

    private Long eventId;
}
