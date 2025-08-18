package org.hyper.notificationbackend.controllers;

import org.hyper.notificationbackend.services.DeviceServiceNew;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DeviceController {

    private final DeviceServiceNew deviceService;

    public DeviceController(DeviceServiceNew deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping("/api/device-data")
    public Map<String, Object> getDeviceData() {
        return deviceService.getDeviceData();
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
}
