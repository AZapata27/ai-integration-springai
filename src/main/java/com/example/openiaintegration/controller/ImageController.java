package com.example.openiaintegration.controller;

import com.example.openiaintegration.dto.ResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.image.ImageClient;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ImageController {

    //Spring AI supports DALL-E OpenAI
    private final ImageClient imageClient;

    //DALL·E 3	HD	1024×1024	$0.080 per image
    //DALL·E 3	SD	1024×1024	$0.040 per image
    @GetMapping("/generate")
    public ResponseEntity<ResponseDTO<String>> generateImage(@RequestParam("param") String param) {
         ImageResponse response = imageClient.call(new ImagePrompt(param,
                 OpenAiImageOptions.builder()
                         .withModel("dall-e-3")
                         .withQuality("hd")
                         .withN(1)
                         .withHeight(1024)
                         .withWidth(1024)
                         .build()
                 ));

         String url = response.getResult().getOutput().getUrl();

         return ResponseEntity.ok(new ResponseDTO<>(200, "sucess", url));
    }

    @GetMapping(value = "/generateBytes")//, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<ResponseDTO<String>> generateBytes(@RequestParam("param") String param) {
        ImageResponse response = imageClient.call(new ImagePrompt(param,
                OpenAiImageOptions.builder()
                        .withModel("dall-e-3")
                        .withQuality("standard")
                        .withN(1)
                        .withHeight(1024)
                        .withWidth(1024)
                        .withResponseFormat("b64_json") // ['b64_json', 'url'], por defecto es url asi no se indique
                        .build()
        ));

        String base64String = response.getResult().getOutput().getB64Json();

        return ResponseEntity.ok(new ResponseDTO<>(200, "sucess", base64String));
    }

    @GetMapping("/generate/dalle2")
    public ResponseEntity<?> generateImageDallE2(@RequestParam("param") String param){
        ImageResponse response = imageClient.call(
                new ImagePrompt(param,
                        OpenAiImageOptions.builder()
                                .withModel("dall-e-2")
                                .withQuality("standard")
                                .withN(1)
                                .withHeight(256)
                                .withWidth(256)
                                .build()
                )
        );

        return ResponseEntity.ok(new ResponseDTO<>(200, "success", response.getResult().getOutput().getUrl()));

    }
}
