package edu.cit.abel.washq.feature.weather;

import edu.cit.abel.washq.shared.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/weather")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentWeather() {
        Map<String, Object> weather = weatherService.getCurrentWeather();
        if (weather != null) {
            return ResponseEntity.ok(ApiResponse.success(weather));
        } else {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("WEATHER_ERROR", "Failed to fetch weather data", null));
        }
    }
}
