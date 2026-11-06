package com.example.Hospital.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FaqUpdateRequestDTO {


    @Pattern(regexp = ".*\\S.*", message = "Question cannot be blank")
    @Size(max = 500, message = "Question must be at most 500 characters")
    private String question;

    @Pattern(regexp = ".*\\S.*", message = "Answer cannot be blank")
    @Size(max = 5000, message = "Answer must be at most 5000 characters")
    private String answer;

    @Size(max = 100, message = "Category must be at most 100 characters")
    private String category;
}