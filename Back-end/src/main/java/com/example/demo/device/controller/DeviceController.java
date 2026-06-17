package com.example.demo.device.controller;

import com.example.demo.device.dto.CapacityRequest;
import com.example.demo.device.dto.DeviceStatusResponse;
import com.example.demo.device.entity.Device;
import com.example.demo.device.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceRepository deviceRepository;

    @PostMapping("/{deviceId}/capacity")
    public ResponseEntity<String> updateCapacity(
            @PathVariable Long deviceId,
            @RequestBody DeviceStatusResponse request) {

        Optional<Device> optionalDevice = deviceRepository.findById(deviceId);
        if (optionalDevice.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Device not found");
        }

        Device device = optionalDevice.get();
        device.setCapacity(request.getCapacity());
        device.setLastUpdate(LocalDateTime.now());

        deviceRepository.save(device);
        return ResponseEntity.ok("Updated");
    }

    @GetMapping("/{deviceId}/status")
    public ResponseEntity<DeviceStatusResponse> getStatus(@PathVariable Long deviceId) {
        return deviceRepository.findById(deviceId)
                .map(device -> ResponseEntity.ok(new DeviceStatusResponse(
                        device.getDeviceId(),
                        device.getCapacity(),
                        device.getLastUpdate()
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/reset-load")
    public ResponseEntity<String> resetLoadRate(
            @RequestParam Long deviceId,
            @RequestParam Long adminId) {

        Optional<Device> optionalDevice = deviceRepository.findById(deviceId);
        if (optionalDevice.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Device not found");
        }

        Device device = optionalDevice.get();
        device.setCapacity(0);
        device.setLastUpdate(LocalDateTime.now());
        deviceRepository.save(device);

        return ResponseEntity.ok("Load rate reset to 0%");
    }
}
