package com.uep.pillar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserInput {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String avatarUrl;
    private String bio;
    private Integer roleId;
}
