package io.democratizedData.VoteService.model.dto;

import lombok.Data;

@Data
public class OptionVotes {
    public final String option;
    public final long votes;

    public OptionVotes(String option, long votes) {
        this.option = option;
        this.votes = votes;
    }
}
