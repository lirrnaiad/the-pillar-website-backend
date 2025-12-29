package com.uep.pillar.controller;

import com.uep.pillar.model.Media;
import com.uep.pillar.model.User;
import com.uep.pillar.model.enums.MediaType;
import com.uep.pillar.repository.MediaRepository;
import com.uep.pillar.repository.UserRepository;
import com.uep.pillar.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
class MediaUploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MediaRepository mediaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User testUser;
    private String authToken;

    @BeforeEach
    void setUp() {
        mediaRepository.deleteAll();
        userRepository.deleteAll();
        testUser = User.builder()
                .email("uploader@test.com")
                .password(passwordEncoder.encode("Test1234!"))
                .firstName("Test")
                .lastName("Uploader")
                .build();
        testUser = userRepository.save(testUser);
        authToken = jwtTokenProvider.generateToken(testUser);
    }

    @Test
    void testGetAllMedia() throws Exception {
        Media media = Media.builder()
                .url("https://example.com/image.jpg")
                .publicId("test-public-id")
                .type(MediaType.IMAGE)
                .uploadedBy(testUser)
                .build();
        mediaRepository.save(media);

        mockMvc.perform(get("/api/media"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testGetMediaById() throws Exception {
        Media media = Media.builder()
                .url("https://example.com/image.jpg")
                .publicId("test-public-id")
                .type(MediaType.IMAGE)
                .uploadedBy(testUser)
                .build();
        media = mediaRepository.save(media);

        mockMvc.perform(get("/api/media/{id}", media.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(media.getId()));
    }

    @Test
    void testUploadMediaUnauthenticated() throws Exception {
        byte[] imageContent = new byte[]{(byte)0x89, (byte)0x50, (byte)0x4E, (byte)0x47}; // PNG header

        mockMvc.perform(multipart("/api/media/upload")
                .file("file", imageContent)
                .param("type", "IMAGE"))
                .andExpect(status().isForbidden()); // 403 is acceptable for unauthenticated requests
    }

    @Test
    void testDeleteMedia() throws Exception {
        Media media = Media.builder()
                .url("https://example.com/image.jpg")
                .publicId("test-public-id")
                .type(MediaType.IMAGE)
                .uploadedBy(testUser)
                .build();
        media = mediaRepository.save(media);

        mockMvc.perform(delete("/api/media/{id}", media.getId())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());
    }
}

