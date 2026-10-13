package com.example.openiaintegration.controller;

import com.example.openiaintegration.service.IAuthorService;
import com.example.openiaintegration.service.IBookService;
import com.example.openiaintegration.util.ChatHistory;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.example.openiaintegration.support.AiTestResponses.chatResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
@Import(ChatHistory.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ChatHistory chatHistory;

    @MockBean
    private ChatClient chatClient;

    // Required by the application runner that seeds data on startup
    @MockBean
    private IAuthorService authorService;
    @MockBean
    private IBookService bookService;

    @Test
    void generateSendsUserMessageAndWrapsAnswer() throws Exception {
        when(chatClient.call(any(Prompt.class))).thenReturn(chatResponse("Hello from the model"));

        mockMvc.perform(get("/chats/generate").param("message", "Hi there"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data").value("Hello from the model"));

        Prompt prompt = capturePrompt();
        assertThat(prompt.getInstructions()).hasSize(1);
        assertThat(prompt.getInstructions().get(0).getMessageType()).isEqualTo(MessageType.USER);
        assertThat(prompt.getContents()).isEqualTo("Hi there");
    }

    @Test
    void generateRequiresMessageParam() throws Exception {
        mockMvc.perform(get("/chats/generate")).andExpect(status().isBadRequest());
    }

    @Test
    void generateWithPromptRendersTemplateVariables() throws Exception {
        when(chatClient.call(any(Prompt.class))).thenReturn(chatResponse("answer"));

        mockMvc.perform(get("/chats/generate/prompt").param("param", "Java").param("topic", "Spring AI"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("answer"));

        assertThat(capturePrompt().getContents()).isEqualTo("Tell me about Java and Spring AI");
    }

    @Test
    void generateOutputParsesModelJsonIntoAuthorBook() throws Exception {
        when(chatClient.call(any(Prompt.class))).thenReturn(chatResponse(
                "{\"author\":\"Gabriel Garcia Marquez\",\"books\":[\"Cien anos de soledad\",\"El otono del patriarca\"]}"));

        mockMvc.perform(get("/chats/generate/output").param("author", "Gabriel Garcia Marquez"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.author").value("Gabriel Garcia Marquez"))
                .andExpect(jsonPath("$.books.length()").value(2));

        String sent = capturePrompt().getContents();
        assertThat(sent).contains("Tell me book titles of Gabriel Garcia Marquez");
        // The BeanOutputParser format instructions (JSON schema) must be appended to the prompt
        assertThat(sent).contains("JSON").contains("\"books\"");
    }

    @Test
    void generateBookOutputParsesAuthor() throws Exception {
        when(chatClient.call(any(Prompt.class))).thenReturn(chatResponse("{\"author\":\"Mario Vargas Llosa\"}"));

        mockMvc.perform(get("/chats/generate/book/output").param("name", "La ciudad y los perros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.author").value("Mario Vargas Llosa"));

        assertThat(capturePrompt().getContents())
                .contains("Tell me the author of this book which name is La ciudad y los perros");
    }

    @Test
    void generateConversationAccumulatesUserMessagesInHistory() throws Exception {
        when(chatClient.call(any(Prompt.class))).thenReturn(chatResponse("ok"));

        mockMvc.perform(get("/chats/generateConversation").param("message", "first"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/chats/generateConversation").param("message", "second"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("ok"));

        List<Message> history = chatHistory.getAll("1");
        assertThat(history).extracting(Message::getContent).containsExactly("first", "second");
    }

    private Prompt capturePrompt() {
        ArgumentCaptor<Prompt> captor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatClient).call(captor.capture());
        return captor.getValue();
    }
}
