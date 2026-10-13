package com.example.openiaintegration.config;

import com.example.openiaintegration.repo.IBookRepo;
import com.example.openiaintegration.service.impl.BookFunctionServiceImpl;
import com.example.openiaintegration.service.impl.MockWeatherService;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.Function;

/**
 * Tools the model can call. Each {@link ToolCallback} bean is resolved by its name
 * ({@code WeatherInfo}, {@code BookInfo}) when a request enables it with {@code toolNames(...)}.
 */
@Configuration
public class FunctionConfig {

    @Bean
    @Description("Get the weather in location")
    public Function<MockWeatherService.Request, MockWeatherService.Response> weatherFunction(){
        return new MockWeatherService();
    }

    @Bean
    public ToolCallback weatherFunctionInfo(){
        return FunctionToolCallback.builder("WeatherInfo", new MockWeatherService())
                .description("Get the weather in location")
                .inputType(MockWeatherService.Request.class)
                .toolCallResultConverter((result, returnType) -> {
                    MockWeatherService.Response response = (MockWeatherService.Response) result;
                    return "" + response.temp() + response.unit();
                })
                .build();
    }

    ////////////////////////
    @Bean
    public ToolCallback bookInfoFunction(IBookRepo repo){
        return FunctionToolCallback.builder("BookInfo", new BookFunctionServiceImpl(repo))
                .description("Get book info from book name")
                .inputType(BookFunctionServiceImpl.Request.class)
                .toolCallResultConverter((result, returnType) -> ((BookFunctionServiceImpl.Response) result).review())
                .build();
    }
}
