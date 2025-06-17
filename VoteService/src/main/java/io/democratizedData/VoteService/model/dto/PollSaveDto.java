package io.democratizedData.VoteService.model.dto;

import lombok.Data;

@Data
public class PollSaveDto {

    private String pollId;
    private String option;
    private String gender;
    private int age;

}
