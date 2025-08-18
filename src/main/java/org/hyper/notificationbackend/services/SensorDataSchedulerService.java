package org.hyper.notificationbackend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class SensorDataSchedulerService {
    
    @Autowired
    private DeviceService deviceService;
    
    @Autowired
    private SensorDataCacheService cacheService;
    
    private boolean isUpdating = false;
    // Run every 15 seconds (15000 milliseconds)
    @Scheduled(fixedRate = 15000, initialDelay = 10000)
    public void updateSensorDataScheduled() {
        if (isUpdating) {
            System.out.println("=== SKIPPING SCHEDULED UPDATE (already in progress) ===");
            return;
        }
        
        try {
            isUpdating = true;
            System.out.println("=== SCHEDULED SENSOR DATA UPDATE STARTED ===");
            
            // Fetch fresh sensor data
            Map<String, Object> freshData = deviceService.getDeviceData();
            
            if (freshData != null && Boolean.TRUE.equals(freshData.get("success"))) {
                // Update the cache with fresh data
                cacheService.updateSensorData(freshData);
                System.out.println("=== SCHEDULED UPDATE COMPLETED SUCCESSFULLY ===");
            } else {
                System.err.println("=== SCHEDULED UPDATE FAILED - No valid data received ===");
                if (freshData != null && freshData.get("error") != null) {
                    System.err.println("Error: " + freshData.get("error"));
                }
            }
            
        } catch (Exception e) {
            System.err.println("=== SCHEDULED UPDATE ERROR ===");
            System.err.println("Error during scheduled sensor data update: " + e.getMessage());
            e.printStackTrace();
        } finally {
            isUpdating = false;
        }
    }
    
    // Manual trigger for immediate update (used by API if needed)
    public void triggerImmediateUpdate() {
        if (isUpdating) {
            System.out.println("Update already in progress, skipping manual trigger");
            return;
        }
        
        System.out.println("=== MANUAL SENSOR DATA UPDATE TRIGGERED ===");
        updateSensorDataScheduled();
    }
    
    public boolean isCurrentlyUpdating() {
        return isUpdating;
    }
}
