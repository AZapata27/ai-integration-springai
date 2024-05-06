package com.example.openiaintegration.controller;

import com.mitocode.dto.ResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.openai.OpenAiAudioTranscriptionClient;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.openai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.openai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/transcripts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TranscriptController {

    //Se usa el modelo Whisper
    private final OpenAiAudioTranscriptionClient transcriptionClient;

    @GetMapping("/es")
    public ResponseEntity<String> transcriptES() {
        OpenAiAudioTranscriptionOptions transcriptionOptions = OpenAiAudioTranscriptionOptions.builder()
                .withLanguage("es")
                .withResponseFormat(OpenAiAudioApi.TranscriptResponseFormat.TEXT)
                .withTemperature(0f)
                .build();

        ClassPathResource audioFile = new ClassPathResource("/audios/es_audio1.flac");

        AudioTranscriptionPrompt transcriptionRequest = new AudioTranscriptionPrompt(audioFile, transcriptionOptions);
        AudioTranscriptionResponse response = transcriptionClient.call(transcriptionRequest);

        return ResponseEntity.ok(response.getResult().getOutput());
    }

    @GetMapping("/en")
    public ResponseEntity<String> transcriptEN(){
        OpenAiAudioTranscriptionOptions transcriptionOptions = OpenAiAudioTranscriptionOptions.builder()
                .withLanguage("en")
                .withResponseFormat(OpenAiAudioApi.TranscriptResponseFormat.TEXT)
                .withTemperature(0f)
                .build();

        ClassPathResource audioFile = new ClassPathResource("/audios/en_audio2.mp3");

        AudioTranscriptionPrompt transcriptionRequest = new AudioTranscriptionPrompt(audioFile, transcriptionOptions);
        AudioTranscriptionResponse response = transcriptionClient.call(transcriptionRequest);

        return ResponseEntity.ok(response.getResult().getOutput());
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDTO<String>> handleAudioUpload(@RequestParam("audio") MultipartFile audioFile) {

        try{
            String uploadDirPath = "src/main/resources/audios/uploads/";

            // Crear el directorio de uploads si no existe
            Path uploadPath = Paths.get(uploadDirPath);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Guardar el archivo de audio en el servidor
            String fileName = "audio_" + System.currentTimeMillis() + ".wav";
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(audioFile.getInputStream(), filePath);

            // Realizar cualquier procesamiento adicional necesario
            OpenAiAudioTranscriptionOptions transcriptionOptions = OpenAiAudioTranscriptionOptions.builder()
                    .withLanguage("es")
                    .withResponseFormat(OpenAiAudioApi.TranscriptResponseFormat.TEXT)
                    .withTemperature(0f)
                    .build();

            Resource audioFileUploaded = new FileSystemResource(uploadDirPath + fileName);

            AudioTranscriptionPrompt transcriptionRequest = new AudioTranscriptionPrompt(audioFileUploaded, transcriptionOptions);
            AudioTranscriptionResponse response = transcriptionClient.call(transcriptionRequest);

            return ResponseEntity.ok(new ResponseDTO<String>(200, "success", response.getResult().getOutput()));

        }catch (Exception e){
            return ResponseEntity.internalServerError().build();
        }

    }

}
