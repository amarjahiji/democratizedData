package io.democratizedData.PollManagement.model.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PollResponse {

    private String id;
    private String title;
    private String question;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean active;
    private List<PollOptionResponse> options;
}
