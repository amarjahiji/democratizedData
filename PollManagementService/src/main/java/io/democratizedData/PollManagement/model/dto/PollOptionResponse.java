package io.democratizedData.PollManagement.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PollOptionResponse {

    private String id;
    private String optionText;
}
