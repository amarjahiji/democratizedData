package io.democratizedData.VoteService.service;

import org.redisson.api.RMap;
import org.redisson.api.RSet;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PollValkeyService extends AbstractValkeyService {
    protected PollValkeyService() {
        super("poll");
    }

    public void savePollVote(String pollId, String option, String userId) {
        // Check if user has already voted for this poll
        String userVotesKey = entityName + ":user_votes:" + pollId;
        RSet<String> userVotes = getClient().getSet(userVotesKey);

        // If user has already voted, don't count their vote again
        if (userVotes.add(userId)) {
            // User hasn't voted before, count their vote
            String mapKey = entityName + ":" + pollId;
            RMap<String, Integer> votes = getRMap(mapKey);
            votes.fastPutIfAbsent(option, 0);
            votes.addAndGet(option, 1);
        }
    }

    public Map<String, Integer> getPollResults(String pollId) {
        String mapKey = entityName + ":" + pollId;
        return get(mapKey);
    }
}
