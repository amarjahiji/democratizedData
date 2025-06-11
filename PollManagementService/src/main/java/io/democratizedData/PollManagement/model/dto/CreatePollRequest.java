package io.democratizedData.PollManagement.model.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreatePollRequest {
    private String title;
    private String question;
    private String createdBy;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<String> options;
}
