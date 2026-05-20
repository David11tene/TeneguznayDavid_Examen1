package com.inventario.tec.teneguznaydavid_examen1.repository;

import com.inventario.tec.teneguznaydavid_examen1.domain.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeviceRepository extends JpaRepository<Device, Long> {

    List<Device> findAllByDeletedFalse();

    List<Device> findByCategoriaContainingIgnoreCaseAndDeletedFalse(String categoria);

    long countByAvailableTrueAndDeletedFalse();

    long countByAvailableFalseAndDeletedFalse();

    long countByDeletedFalse();
    
    boolean existsBySerial(String serial);

    List<Device> findByStockLessThanAndDeletedFalse(Integer stock);
}
