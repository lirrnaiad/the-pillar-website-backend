package com.uep.pillar.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uep.pillar.model.User;
import com.uep.pillar.repository.UserRepository;
import com.uep.pillar.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User testUser;
    private String authToken;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        testUser = User.builder()
                .email("test@example.com")
                .password(passwordEncoder.encode("Test1234!"))
                .firstName("Test")
                .lastName("User")
                .build();
        testUser = userRepository.save(testUser);
        authToken = jwtTokenProvider.generateToken(testUser);
    }

    @Test
    void testGetCurrentUser() throws Exception {
        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void testGetCurrentUserUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isForbidden()); // 403 is also valid for unauthenticated requests
    }

    @Test
    void testChangePassword() throws Exception {
        UserController.ChangePasswordRequest request = new UserController.ChangePasswordRequest();
        request.setOldPassword("Test1234!");
        request.setNewPassword("NewPassword123!");

        mockMvc.perform(post("/api/users/{id}/change-password", testUser.getId())
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    void testChangePasswordWithWrongOldPassword() throws Exception {
        UserController.ChangePasswordRequest request = new UserController.ChangePasswordRequest();
        request.setOldPassword("WrongPassword!");
        request.setNewPassword("NewPassword123!");

        mockMvc.perform(post("/api/users/{id}/change-password", testUser.getId())
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}

