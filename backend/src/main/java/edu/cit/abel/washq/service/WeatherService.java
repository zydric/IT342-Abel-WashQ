package edu.cit.abel.washq.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@Service
public class WeatherService {

    @Value("${openweathermap.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    private Map<String, Object> cachedWeather;
    private long lastFetchTime = 0;
    private static final long CACHE_DURATION_MS = 10 * 60 * 1000; // 10 minutes

    public WeatherService() {
        this.restTemplate = new RestTemplate();
    }

    public Map<String, Object> getCurrentWeather() {
        if (cachedWeather != null && (System.currentTimeMillis() - lastFetchTime) < CACHE_DURATION_MS) {
            return cachedWeather;
        }

        // Hardcode Cebu City, PH as per requirements
        String url = "https://api.openweathermap.org/data/2.5/weather?q=Cebu%20City,PH&appid=" + apiKey + "&units=metric";
        
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> body = response.getBody();
            
            if (body != null) {
                Map<String, Object> main = (Map<String, Object>) body.get("main");
                List<Map<String, Object>> weatherList = (List<Map<String, Object>>) body.get("weather");
                Map<String, Object> weather = (weatherList != null && !weatherList.isEmpty()) ? weatherList.get(0) : null;
                
                String condition = weather != null ? (String) weather.get("main") : "Unknown";
                String description = weather != null ? (String) weather.get("description") : "Unknown";
                String icon = weather != null ? (String) weather.get("icon") : "01d";
                String iconUrl = "https://openweathermap.org/img/wn/" + icon + "@2x.png";
                
                Double temperature = main != null ? ((Number) main.get("temp")).doubleValue() : 0.0;
                Integer humidity = main != null ? ((Number) main.get("humidity")).intValue() : 0;
                
                // OpenWeatherMap usually maps 'name' to the city
                String city = body.containsKey("name") ? (String) body.get("name") : "Cebu City";
                
                Map<String, Object> result = new HashMap<>();
                result.put("city", city + ", PH");
                result.put("temperature", temperature);
                result.put("condition", condition);
                result.put("description", description);
                result.put("humidity", humidity);
                result.put("iconUrl", iconUrl); // Added for compatibility with standard reqs
                result.put("icon", iconUrl); // User prompt mentioned "icon"
                
                cachedWeather = result;
                lastFetchTime = System.currentTimeMillis();
                
                return result;
            }
        } catch (Exception e) {
            // Log error
            System.err.println("Error fetching weather data: " + e.getMessage());
            
            // Fallback for development if API key is invalid/unactivated
            System.err.println("Returning mock weather data for development purposes.");
            Map<String, Object> result = new HashMap<>();
            result.put("city", "Cebu City, PH");
            result.put("temperature", 31.5);
            result.put("condition", "Partly Cloudy");
            result.put("description", "partly cloudy");
            result.put("humidity", 78);
            result.put("iconUrl", "https://openweathermap.org/img/wn/02d@2x.png");
            return result;
        }
        return null;
    }
}
