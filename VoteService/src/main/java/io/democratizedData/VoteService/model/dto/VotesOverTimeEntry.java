package io.democratizedData.VoteService.model.dto;

import lombok.Data;

import java.util.Date;

@Data
public class VotesOverTimeEntry {
    private final String option;
    private final Date date;
    private final long votes;

    public VotesOverTimeEntry(String option, Date date, long votes) {
        this.option = option;
        this.date = date;
        this.votes = votes;
    }
}
