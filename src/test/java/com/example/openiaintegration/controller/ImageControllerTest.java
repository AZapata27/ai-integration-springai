package com.example.openiaintegration.controller;

import com.example.openiaintegration.service.IAuthorService;
import com.example.openiaintegration.service.IBookService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.image.Image;
import org.springframework.ai.image.ImageClient;
import org.springframework.ai.image.ImageGeneration;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ImageController.class)
class ImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ImageClient imageClient;

    @MockBean
    private IAuthorService authorService;
    @MockBean
    private IBookService bookService;

    @Test
    void generateUsesDallE3HdAndReturnsUrl() throws Exception {
        when(imageClient.call(any(ImagePrompt.class))).thenReturn(image("https://img.example/cat.png", null));

        mockMvc.perform(get("/images/generate").param("param", "a cat"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("https://img.example/cat.png"));

        ImagePrompt prompt = capturePrompt();
        assertThat(prompt.getInstructions().get(0).getText()).isEqualTo("a cat");
        OpenAiImageOptions options = (OpenAiImageOptions) prompt.getOptions();
        assertThat(options.getModel()).isEqualTo("dall-e-3");
        assertThat(options.getQuality()).isEqualTo("hd");
        assertThat(options.getWidth()).isEqualTo(1024);
        assertThat(options.getHeight()).isEqualTo(1024);
    }

    @Test
    void generateBytesRequestsBase64Payload() throws Exception {
        when(imageClient.call(any(ImagePrompt.class))).thenReturn(image(null, "aGVsbG8="));

        mockMvc.perform(get("/images/generateBytes").param("param", "a dog"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("aGVsbG8="));

        OpenAiImageOptions options = (OpenAiImageOptions) capturePrompt().getOptions();
        assertThat(options.getResponseFormat()).isEqualTo("b64_json");
        assertThat(options.getQuality()).isEqualTo("standard");
    }

    @Test
    void generateDallE2UsesSmallSize() throws Exception {
        when(imageClient.call(any(ImagePrompt.class))).thenReturn(image("https://img.example/small.png", null));

        mockMvc.perform(get("/images/generate/dalle2").param("param", "a bird"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("https://img.example/small.png"));

        OpenAiImageOptions options = (OpenAiImageOptions) capturePrompt().getOptions();
        assertThat(options.getModel()).isEqualTo("dall-e-2");
        assertThat(options.getWidth()).isEqualTo(256);
    }

    private static ImageResponse image(String url, String b64) {
        return new ImageResponse(List.of(new ImageGeneration(new Image(url, b64))));
    }

    private ImagePrompt capturePrompt() {
        ArgumentCaptor<ImagePrompt> captor = ArgumentCaptor.forClass(ImagePrompt.class);
        verify(imageClient).call(captor.capture());
        return captor.getValue();
    }
}
