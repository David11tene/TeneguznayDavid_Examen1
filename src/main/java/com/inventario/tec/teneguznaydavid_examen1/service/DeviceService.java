package com.inventario.tec.teneguznaydavid_examen1.service;

import com.inventario.tec.teneguznaydavid_examen1.domain.Device;
import java.util.List;
import java.util.Map;

public interface DeviceService {
    Device create(Device device);
    Device update(Long id, Device device);
    void deactivate(Long id);
    void softDelete(Long id);
    List<Device> searchByCategoria(String categoria);
    List<Device> findAllActive();
    Map<String, Long> getStatistics();
}
