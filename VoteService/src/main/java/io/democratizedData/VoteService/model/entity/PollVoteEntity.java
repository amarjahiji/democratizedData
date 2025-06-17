package io.democratizedData.VoteService.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "poll_stats")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PollVoteEntity {
    @jakarta.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "poll_id", nullable = false)
    private String pollId;

    @Column(name = "option", nullable = false)
    private String option;

    @Column(name = "gender")
    private String gender;

    @Column(name = "age")
    private int age;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "vote_date", nullable = false)
    private Date voteDate;

}
