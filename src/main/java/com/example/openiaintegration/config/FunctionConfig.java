package com.example.openiaintegration.config;

import com.example.openiaintegration.repo.IBookRepo;
import com.example.openiaintegration.service.impl.BookFunctionServiceImpl;
import com.example.openiaintegration.service.impl.MockWeatherService;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.model.function.FunctionCallbackWrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.Function;

@Configuration
public class FunctionConfig {

    @Bean
    @Description("Get the weather in location")
    public Function<MockWeatherService.Request, MockWeatherService.Response> weatherFunction(){
        return new MockWeatherService();
    }

    @Bean
    public FunctionCallback weatherFunctionInfo(){
        return FunctionCallbackWrapper.builder(new MockWeatherService())
                .withName("WeatherInfo")
                .withDescription("Get the weather in location")
                .withResponseConverter(response -> "" + response.temp() + response.unit())
                .build();
    }

    ////////////////////////
    @Bean
    public FunctionCallback bookInfoFunction(IBookRepo repo){

        return FunctionCallbackWrapper.builder(new BookFunctionServiceImpl(repo))
                .withName("BookInfo")
                .withDescription("Get book info from book name")
                .withResponseConverter(response -> "" + response.review())
                .build();
    }
}
