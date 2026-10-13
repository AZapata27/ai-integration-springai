package com.example.openiaintegration.controller;

import com.example.openiaintegration.service.IAuthorService;
import com.example.openiaintegration.service.IBookService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.example.openiaintegration.support.AiTestResponses.chatResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FunctionCallingController.class)
class FunctionCallingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatClient chatClient;

    @MockBean
    private IAuthorService authorService;
    @MockBean
    private IBookService bookService;

    @Test
    void weatherEndpointEnablesWeatherInfoFunction() throws Exception {
        when(chatClient.call(any(Prompt.class))).thenReturn(chatResponse("30C everywhere"));

        mockMvc.perform(get("/functions"))
                .andExpect(status().isOk())
                .andExpect(content().string("30C everywhere"));

        Prompt prompt = capturePrompt();
        assertThat(prompt.getContents()).contains("San Francisco", "Tokyo", "Paris");
        assertThat(prompt.getOptions()).isInstanceOf(OpenAiChatOptions.class);
        assertThat(((OpenAiChatOptions) prompt.getOptions()).getFunctions()).containsExactly("WeatherInfo");
    }

    @Test
    void bookEndpointEnablesBookInfoFunctionWithBookName() throws Exception {
        when(chatClient.call(any(Prompt.class))).thenReturn(chatResponse("A great novel"));

        mockMvc.perform(get("/functions/book").param("bookName", "La ciudad y los perros"))
                .andExpect(status().isOk())
                .andExpect(content().string("A great novel"));

        Prompt prompt = capturePrompt();
        assertThat(prompt.getContents()).contains("La ciudad y los perros");
        assertThat(((OpenAiChatOptions) prompt.getOptions()).getFunctions()).containsExactly("BookInfo");
    }

    @Test
    void bookEndpointRequiresBookName() throws Exception {
        mockMvc.perform(get("/functions/book")).andExpect(status().isBadRequest());
    }

    private Prompt capturePrompt() {
        ArgumentCaptor<Prompt> captor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatClient).call(captor.capture());
        return captor.getValue();
    }
}
