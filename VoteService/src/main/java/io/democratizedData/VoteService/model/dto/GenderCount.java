package io.democratizedData.VoteService.model.dto;

import lombok.Data;

@Data
public class GenderCount {
    public final String gender;
    public final long votes;

    public GenderCount(String gender, long votes) {
        this.gender = gender;
        this.votes = votes;
    }
}
