package edu.cit.abel.washq.controller;

import edu.cit.abel.washq.dto.ApiResponse;
import edu.cit.abel.washq.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/weather")
public class WeatherController {

    @Autowired
    private WeatherService weatherService;

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
