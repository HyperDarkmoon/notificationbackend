package org.hyper.notificationbackend.controllers;

import org.hyper.notificationbackend.services.DeviceService;
import org.hyper.notificationbackend.services.SensorDataCacheService;
import org.hyper.notificationbackend.services.SensorDataSchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DeviceController {

    private final DeviceService deviceService;
    
    @Autowired
    private SensorDataCacheService cacheService;
    
    @Autowired
    private SensorDataSchedulerService schedulerService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping("/api/device-data")
    public Map<String, Object> getDeviceData() {
        // Return cached data if available, otherwise fetch fresh data
        if (cacheService.hasCachedData()) {
            Map<String, Object> cachedData = cacheService.getCachedSensorData();
            cachedData.put("data_source", "cache");
            System.out.println("=== RETURNING CACHED DATA (Age: " + cacheService.getDataAgeInSeconds() + " seconds) ===");
            return cachedData;
        } else {
            System.out.println("=== NO CACHED DATA - FETCHING FRESH DATA ===");
            Map<String, Object> freshData = deviceService.getDeviceData();
            if (freshData != null && Boolean.TRUE.equals(freshData.get("success"))) {
                cacheService.updateSensorData(freshData);
            }
            freshData.put("data_source", "live");
            return freshData;
        }
    }

    @PostMapping("/api/device-data/refresh")
    public Map<String, Object> forceRefresh() {
        System.out.println("=== MANUAL REFRESH TRIGGERED ===");
        schedulerService.triggerImmediateUpdate();
        
        // Wait a moment for the update to complete, then return fresh cached data
        try {
            Thread.sleep(2000); // Wait 2 seconds for update to start
            int maxWait = 30; // Maximum 30 seconds wait
            while (schedulerService.isCurrentlyUpdating() && maxWait > 0) {
                Thread.sleep(1000);
                maxWait--;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return cacheService.getCachedSensorData();
    }

    @PostMapping("/api/device-data/reauth")
    public Map<String, Object> forceReauth() {
        return deviceService.forceReauth();
    }

    @PostMapping("/api/device-data/close-session")
    public Map<String, Object> closeSession() {
        deviceService.closeBrowserSession();
        return Map.of("message", "Browser session closed", "success", true);
    }
    
    @GetMapping("/api/device-data/status")
    public Map<String, Object> getDataStatus() {
        return Map.of(
            "has_cached_data", cacheService.hasCachedData(),
            "last_update_timestamp", cacheService.getLastUpdateTimestamp(),
            "data_age_seconds", cacheService.getDataAgeInSeconds(),
            "currently_updating", schedulerService.isCurrentlyUpdating(),
            "cache_data", cacheService.getCachedSensorData()
        );
    }
}
