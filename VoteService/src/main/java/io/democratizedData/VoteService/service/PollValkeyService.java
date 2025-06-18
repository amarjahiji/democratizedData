package io.democratizedData.VoteService.service;

import org.redisson.api.RMap;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PollValkeyService extends AbstractValkeyService {
    protected PollValkeyService() {
        super("poll");
    }

    public void savePollVote(String pollId, String option) {
        String mapKey = entityName + ":" + pollId;
        RMap<String, Integer> votes = getRMap(mapKey);
        votes.addAndGet(option, 1);
    }

    public Map<String, Integer> getPollResults(String pollId) {
        String mapKey = entityName + ":" + pollId;
        return get(mapKey);
    }
}
