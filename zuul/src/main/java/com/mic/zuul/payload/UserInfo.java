package com.mic.zuul.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo {
    @NotBlank
    private Long id;

    @NotBlank
    @Size(min = 3, max = 15)
    private String username;

    @NotBlank
    @Size(max = 40)
    @Email
    private String email;

    @NotBlank
    @Size(min = 2, max = 40)
    private String firstName;

    @NotBlank
    @Size(min = 2, max = 40)
    private String lastName;

    @NotBlank
    @Size(min = 9, max = 9)
    private String phoneNumber;

    @NotBlank
    @Size(min = 4, max = 6)
    private String gender;

    @NotBlank
    private List<UserInfo> friends;

    @NotBlank
    private boolean isActive;

    @NotBlank
    private List<String> authorities;

    @NotBlank
    private LocalDateTime createdDate;

    @NotBlank
    private String lastModifiedBy;

    @NotBlank
    private LocalDateTime lastModifiedDate;
}
