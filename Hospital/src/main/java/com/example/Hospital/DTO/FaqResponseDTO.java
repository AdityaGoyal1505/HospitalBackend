package com.example.Hospital.dto;


import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FaqResponseDTO {
    private Long faqId;
    private String question;
    private String answer;
    private String category;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}