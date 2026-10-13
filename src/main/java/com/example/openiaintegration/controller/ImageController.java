package com.example.openiaintegration.controller;

import com.example.openiaintegration.dto.ResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.image.ImageModel;
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
    private final ImageModel imageModel;

    //DALL·E 3	HD	1024×1024	$0.080 per image
    //DALL·E 3	SD	1024×1024	$0.040 per image
    @GetMapping("/generate")
    public ResponseEntity<ResponseDTO<String>> generateImage(@RequestParam("param") String param) {
         ImageResponse response = imageModel.call(new ImagePrompt(param,
                 OpenAiImageOptions.builder()
                         .model("dall-e-3")
                         .quality("hd")
                         .N(1)
                         .height(1024)
                         .width(1024)
                         .build()
                 ));

         String url = response.getResult().getOutput().getUrl();

         return ResponseEntity.ok(new ResponseDTO<>(200, "sucess", url));
    }

    @GetMapping(value = "/generateBytes")//, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<ResponseDTO<String>> generateBytes(@RequestParam("param") String param) {
        ImageResponse response = imageModel.call(new ImagePrompt(param,
                OpenAiImageOptions.builder()
                        .model("dall-e-3")
                        .quality("standard")
                        .N(1)
                        .height(1024)
                        .width(1024)
                        .responseFormat("b64_json") // ['b64_json', 'url'], por defecto es url asi no se indique
                        .build()
        ));

        String base64String = response.getResult().getOutput().getB64Json();

        return ResponseEntity.ok(new ResponseDTO<>(200, "sucess", base64String));
    }

    @GetMapping("/generate/dalle2")
    public ResponseEntity<?> generateImageDallE2(@RequestParam("param") String param){
        ImageResponse response = imageModel.call(
                new ImagePrompt(param,
                        OpenAiImageOptions.builder()
                                .model("dall-e-2")
                                .quality("standard")
                                .N(1)
                                .height(256)
                                .width(256)
                                .build()
                )
        );

        return ResponseEntity.ok(new ResponseDTO<>(200, "success", response.getResult().getOutput().getUrl()));

    }
}
