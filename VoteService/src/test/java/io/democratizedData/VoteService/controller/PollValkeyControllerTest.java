package io.democratizedData.VoteService.controller;

import io.democratizedData.VoteService.service.PollValkeyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

        mockMvc.perform(post("/poll/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        verify(pollValkeyService).savePollVote("poll123", "OptionA");
    }

    @Test
    void getPollVote() {
    }
}