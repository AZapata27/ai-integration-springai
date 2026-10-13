package com.example.openiaintegration.controller;

import com.example.openiaintegration.service.IAuthorService;
import com.example.openiaintegration.service.IBookService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.audio.transcription.AudioTranscription;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TranscriptController.class)
class TranscriptControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TranscriptionModel transcriptionModel;

    @MockitoBean
    private IAuthorService authorService;
    @MockitoBean
    private IBookService bookService;

    @Test
    void transcribesSpanishSample() throws Exception {
        when(transcriptionModel.call(any(AudioTranscriptionPrompt.class)))
                .thenReturn(new AudioTranscriptionResponse(new AudioTranscription("hola mundo")));

        mockMvc.perform(get("/transcripts/es"))
                .andExpect(status().isOk())
                .andExpect(content().string("hola mundo"));

        AudioTranscriptionPrompt prompt = capturePrompt();
        assertThat(prompt.getInstructions().getFilename()).isEqualTo("es_audio1.flac");
        assertThat(prompt.getInstructions().exists()).isTrue();
        OpenAiAudioTranscriptionOptions options = (OpenAiAudioTranscriptionOptions) prompt.getOptions();
        assertThat(options.getLanguage()).isEqualTo("es");
        assertThat(options.getResponseFormat()).isEqualTo(OpenAiAudioApi.TranscriptResponseFormat.TEXT);
    }

    @Test
    void transcribesEnglishSample() throws Exception {
        when(transcriptionModel.call(any(AudioTranscriptionPrompt.class)))
                .thenReturn(new AudioTranscriptionResponse(new AudioTranscription("hello world")));

        mockMvc.perform(get("/transcripts/en"))
                .andExpect(status().isOk())
                .andExpect(content().string("hello world"));

        AudioTranscriptionPrompt prompt = capturePrompt();
        assertThat(prompt.getInstructions().getFilename()).isEqualTo("en_audio2.mp3");
        assertThat(((OpenAiAudioTranscriptionOptions) prompt.getOptions()).getLanguage()).isEqualTo("en");
    }

    private AudioTranscriptionPrompt capturePrompt() {
        ArgumentCaptor<AudioTranscriptionPrompt> captor = ArgumentCaptor.forClass(AudioTranscriptionPrompt.class);
        verify(transcriptionModel).call(captor.capture());
        return captor.getValue();
    }
}
