package io.democratizedData.VoteService.model.dto;

import lombok.Data;

@Data
public class AgeGroupCount {
    public final String ageGroup;
    public final long votes;

    public AgeGroupCount(String ageGroup, long votes) {
        this.ageGroup = ageGroup;
        this.votes = votes;
    }
}
