package io.democratizedData.VoteService.controller;

import io.democratizedData.VoteService.service.PollValkeyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PollValkeyController.class)
class PollValkeyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PollValkeyService pollValkeyService;


    @Test
    void savePoll() throws Exception {
        String payload = """
                        {
                          "pollId":"abcdef",
                          "option":"yes"
                        }
                """;
        String userId = "user123";

        mockMvc.perform(post("/valkey/poll/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", userId)
                        .content(payload))
                .andExpect(status().isOk());

        verify(pollValkeyService).savePollVote("abcdef", "yes", userId);
    }

    @Test
    void getPollVote() throws Exception {
        String pollId = "abcdef";
        Map<String, Integer> results = Map.of("yes", 5, "no", 3);

        when(pollValkeyService.getPollResults(pollId)).thenReturn(results);

        mockMvc.perform(get("/valkey/poll/{poll_id}/get", pollId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.yes").value(5))
                .andExpect(jsonPath("$.no").value(3));

        verify(pollValkeyService).getPollResults(pollId);
    }
}
