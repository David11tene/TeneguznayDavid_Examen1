package com.inventario.tec.teneguznaydavid_examen1.web.controller;

import com.inventario.tec.teneguznaydavid_examen1.domain.Device;
import com.inventario.tec.teneguznaydavid_examen1.dto.DeviceCreateRequest;
import com.inventario.tec.teneguznaydavid_examen1.dto.DeviceResponse;
import com.inventario.tec.teneguznaydavid_examen1.service.DeviceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/davidteneguznay/devices")
public class DeviceController {
    private final DeviceService service;

    public DeviceController(DeviceService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<DeviceResponse> create(@jakarta.validation.Valid @RequestBody DeviceCreateRequest request){
        Device device = new Device();
        device.setNombre(request.getNombre());
        device.setSerial(request.getSerial());
        device.setCategoria(request.getCategoria());
        device.setStock(request.getStock());
        device.setAvailable(request.getAvailable());
        
        Device saved = service.create(device);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(saved));
    }

    @GetMapping
    public ResponseEntity<List<DeviceResponse>> getAllActive() {
        return ResponseEntity.ok(service.findAllActive().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList()));
    }

    @GetMapping("/search")
    public ResponseEntity<List<DeviceResponse>> searchByCategoria(@RequestParam String categoria) {
        return ResponseEntity.ok(service.searchByCategoria(categoria).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList()));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        service.deactivate(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable Long id) {
        service.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeviceResponse> update(@PathVariable Long id, @jakarta.validation.Valid @RequestBody DeviceCreateRequest request) {
        Device device = new Device();
        device.setNombre(request.getNombre());
        device.setSerial(request.getSerial());
        device.setCategoria(request.getCategoria());
        device.setStock(request.getStock());
        device.setAvailable(request.getAvailable());
        
        Device updated = service.update(id, device);
        return ResponseEntity.ok(mapToResponse(updated));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStatistics() {
        return ResponseEntity.ok(service.getStatistics());
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<DeviceResponse>> getLowStock() {
        return ResponseEntity.ok(service.getLowStock(5).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList()));
    }

    private DeviceResponse mapToResponse(Device device) {
        DeviceResponse response = new DeviceResponse();
        response.setId(device.getId());
        response.setNombre(device.getNombre());
        response.setSerial(device.getSerial());
        response.setCategoria(device.getCategoria());
        response.setStock(device.getStock());
        response.setAvailable(device.getAvailable());
        return response;
    }
}
