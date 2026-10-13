package com.example.openiaintegration.config;

import com.example.openiaintegration.model.Book;
import com.example.openiaintegration.repo.IBookRepo;
import com.example.openiaintegration.service.impl.MockWeatherService;
import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Exercises the function-calling callbacks exactly as the model would invoke them:
 * a JSON argument string in, the converted response string out. No model involved.
 */
class FunctionConfigTest {

    private final FunctionConfig config = new FunctionConfig();

    @Test
    void weatherCallbackIsRegisteredUnderWeatherInfo() {
        ToolCallback callback = config.weatherFunctionInfo();

        assertThat(callback.getToolDefinition().name()).isEqualTo("WeatherInfo");
        assertThat(callback.getToolDefinition().description()).isEqualTo("Get the weather in location");
        assertThat(callback.getToolDefinition().inputSchema()).contains("location").contains("unit");
    }

    @Test
    void weatherCallbackConvertsResponseToTemperatureAndUnit() {
        String result = config.weatherFunctionInfo().call("{\"location\":\"Paris\",\"unit\":\"C\"}");

        assertThat(result).isEqualTo("30.0C");
    }

    @Test
    void weatherFunctionBeanIsTheMockService() {
        MockWeatherService.Response response = config.weatherFunction()
                .apply(new MockWeatherService.Request("Tokyo", MockWeatherService.Unit.F));

        assertThat(response.temp()).isEqualTo(30.0);
        assertThat(response.unit()).isEqualTo(MockWeatherService.Unit.C);
    }

    @Test
    void bookCallbackLooksUpReviewByName() {
        IBookRepo repo = mock(IBookRepo.class);
        Book book = new Book();
        book.setName("La ciudad y los perros");
        book.setReview("Una novela sobre el colegio Leoncio Prado");
        when(repo.findByName("La ciudad y los perros")).thenReturn(List.of(book));

        ToolCallback callback = config.bookInfoFunction(repo);
        String result = callback.call("{\"bookName\":\"La ciudad y los perros\"}");

        assertThat(callback.getToolDefinition().name()).isEqualTo("BookInfo");
        assertThat(callback.getToolDefinition().inputSchema()).contains("bookName");
        assertThat(result).isEqualTo("Una novela sobre el colegio Leoncio Prado");
        verify(repo).findByName("La ciudad y los perros");
    }
}
