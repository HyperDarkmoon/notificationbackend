package org.hyper.notificationbackend.services;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SensorDataCacheService {
    
    private final Map<String, Object> cachedSensorData = new ConcurrentHashMap<>();
    private long lastUpdateTimestamp = 0;
    
    public void updateSensorData(Map<String, Object> sensorData) {
        cachedSensorData.clear();
        cachedSensorData.putAll(sensorData);
        lastUpdateTimestamp = System.currentTimeMillis();
        
        System.out.println("=== SENSOR DATA CACHE UPDATED ===");
        System.out.println("Temperature: " + cachedSensorData.get("temperature") + " " + cachedSensorData.get("temperature_unit"));
        System.out.println("Pressure: " + cachedSensorData.get("pressure") + " " + cachedSensorData.get("pressure_unit"));
        System.out.println("Humidity: " + cachedSensorData.get("humidity") + " " + cachedSensorData.get("humidity_unit"));
        System.out.println("Cache updated at: " + new java.util.Date(lastUpdateTimestamp));
    }
    
    public Map<String, Object> getCachedSensorData() {
        Map<String, Object> result = new ConcurrentHashMap<>(cachedSensorData);
        result.put("cache_timestamp", lastUpdateTimestamp);
        result.put("data_age_seconds", (System.currentTimeMillis() - lastUpdateTimestamp) / 1000);
        return result;
    }
    
    public boolean hasCachedData() {
        return !cachedSensorData.isEmpty() && lastUpdateTimestamp > 0;
    }
    
    public long getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }
    
    public long getDataAgeInSeconds() {
        return (System.currentTimeMillis() - lastUpdateTimestamp) / 1000;
    }
}
