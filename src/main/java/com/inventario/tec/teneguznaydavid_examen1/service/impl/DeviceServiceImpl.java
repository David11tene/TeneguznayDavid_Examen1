package com.inventario.tec.teneguznaydavid_examen1.service.impl;

import com.inventario.tec.teneguznaydavid_examen1.domain.Device;
import com.inventario.tec.teneguznaydavid_examen1.repository.DeviceRepository;
import com.inventario.tec.teneguznaydavid_examen1.service.DeviceService;
import com.inventario.tec.teneguznaydavid_examen1.web.advice.ConflictException;
import com.inventario.tec.teneguznaydavid_examen1.web.advice.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;

    public DeviceServiceImpl(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @Override
    public Device create(Device device) {
        if (device.getStock() != null && device.getStock() < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
        if (deviceRepository.existsBySerial(device.getSerial())) {
            throw new ConflictException("Serial already exists");
        }
        return deviceRepository.save(device);
    }

    @Override
    public Device update(Long id, Device device) {
        Device existing = deviceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Device not found"));
        
        if (device.getStock() != null && device.getStock() < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
        
        existing.setNombre(device.getNombre());
        existing.setSerial(device.getSerial());
        existing.setCategoria(device.getCategoria());
        existing.setStock(device.getStock());
        existing.setAvailable(device.getAvailable());
        
        return deviceRepository.save(existing);
    }

    @Override
    public void deactivate(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Device not found"));
        device.setAvailable(false);
        deviceRepository.save(device);
    }

    @Override
    public void softDelete(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Device not found"));
        device.setDeleted(true);
        deviceRepository.save(device);
    }

    @Override
    public List<Device> searchByCategoria(String categoria) {
        return deviceRepository.findByCategoriaContainingIgnoreCaseAndDeletedFalse(categoria);
    }

    @Override
    public List<Device> findAllActive() {
        return deviceRepository.findAllByDeletedFalse();
    }

    @Override
    public Map<String, Long> getStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", deviceRepository.countByDeletedFalse());
        stats.put("available", deviceRepository.countByAvailableTrueAndDeletedFalse());
        stats.put("unavailable", deviceRepository.countByAvailableFalseAndDeletedFalse());
        return stats;
    }

    @Override
    public List<Device> getLowStock(Integer limit) {
        return deviceRepository.findByStockLessThanAndDeletedFalse(limit);
    }
}
