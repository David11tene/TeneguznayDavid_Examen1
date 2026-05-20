package com.inventario.tec.teneguznaydavid_examen1.service;

import com.inventario.tec.teneguznaydavid_examen1.domain.Device;
import com.inventario.tec.teneguznaydavid_examen1.repository.DeviceRepository;
import com.inventario.tec.teneguznaydavid_examen1.web.advice.ConflictException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class DeviceServiceTests {

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private DeviceRepository deviceRepository;

    @Test
    @DisplayName("P1 Evitar registro de dispositivos con serial duplicado")
    void testPrueba1_AvoidDuplicateSerial() {
        // 1. Registrar dispositivo: ABC-001
        Device device1 = new Device();
        device1.setNombre("Dispositivo 1");
        device1.setSerial("ABC-001");
        device1.setCategoria("Categoria 1");
        device1.setStock(10);
        deviceService.create(device1);

        // 2. Intentar registrar otro igual
        Device device2 = new Device();
        device2.setNombre("Dispositivo 2");
        device2.setSerial("ABC-001");
        device2.setCategoria("Categoria 2");
        device2.setStock(5);

        // 3. Verificar: excepción y no duplicación
        assertThrows(ConflictException.class, () -> deviceService.create(device2));
        
        long count = deviceRepository.count();
        assertEquals(1, count, "No debe haber duplicación en la BD");
    }

    @Test
    @DisplayName("P2 No permitir el registro de stock negativo")
    void testPrueba2_NoNegativeStock() {
        // Validar stock >= 0
        Device device = new Device();
        device.setNombre("Dispositivo Negativo");
        device.setSerial("NEG-001");
        device.setCategoria("Test");
        device.setStock(-1);

        // Verificar: excepción o validación fallida
        assertThrows(IllegalArgumentException.class, () -> deviceService.create(device));
    }

    @Test
    @DisplayName("P3 Desactivar un dispositivo correctamente")
    void testPrueba3_DeactivateDevice() {
        // 1. Crear dispositivo activo
        Device device = new Device();
        device.setNombre("Laptop");
        device.setSerial("LAP-001");
        device.setCategoria("Computo");
        device.setStock(5);
        device.setAvailable(true);
        Device saved = deviceService.create(device);

        // 2. Desactivarlo
        deviceService.deactivate(saved.getId());

        // 3. Verificar: available = false y mantener nombre, serial, categoría
        Device updated = deviceRepository.findById(saved.getId()).get();
        assertFalse(updated.getAvailable());
        assertEquals("Laptop", updated.getNombre());
        assertEquals("LAP-001", updated.getSerial());
        assertEquals("Computo", updated.getCategoria());
    }

    @Test
    @DisplayName("P4 Obtener estadisticas correctas del inventario")
    void testPrueba4_InventoryStatistics() {
        // Crear: 2 disponibles, 1 no disponible
        Device a1 = new Device();
        a1.setNombre("A1"); a1.setSerial("S1"); a1.setCategoria("C1"); a1.setStock(1); a1.setAvailable(true);
        deviceService.create(a1);

        Device a2 = new Device();
        a2.setNombre("A2"); a2.setSerial("S2"); a2.setCategoria("C2"); a2.setStock(1); a2.setAvailable(true);
        deviceService.create(a2);

        Device a3 = new Device();
        a3.setNombre("A3"); a3.setSerial("S3"); a3.setCategoria("C3"); a3.setStock(1); a3.setAvailable(false);
        deviceService.create(a3);

        // Validar: total = 3, available = 2, unavailable = 1
        Map<String, Long> stats = deviceService.getStatistics();
        assertEquals(3L, stats.get("total"));
        assertEquals(2L, stats.get("available"));
        assertEquals(1L, stats.get("unavailable"));
    }

    @Test
    @DisplayName("P5 Verificar la eliminacion logica (soft delete)")
    void testPrueba5_LogicalDeletion() {
        // 1. Marcar dispositivo como eliminado
        Device device = new Device();
        device.setNombre("Eliminar");
        device.setSerial("DEL-001");
        device.setCategoria("Test");
        device.setStock(1);
        Device saved = deviceService.create(device);

        deviceService.softDelete(saved.getId());

        // 2. Verificar: sigue en BD, deleted = true, no aparece en consultas normales
        Device inDb = deviceRepository.findById(saved.getId()).get();
        assertTrue(inDb.getDeleted(), "Debe estar marcado como eliminado");
        
        List<Device> activeOnes = deviceService.findAllActive();
        assertFalse(activeOnes.stream().anyMatch(a -> a.getId().equals(saved.getId())), "No debe aparecer en consultas normales");
    }

    @Test
    @DisplayName("P6 Busqueda parcial por categoria")
    void testPrueba6_PartialCategorySearch() {
        // Registrar: Laptop, Laptop Gamer, Router
        Device a1 = new Device();
        a1.setNombre("L1"); a1.setSerial("SER-1"); a1.setCategoria("Laptop"); a1.setStock(1);
        deviceService.create(a1);

        Device a2 = new Device();
        a2.setNombre("L2"); a2.setSerial("SER-2"); a2.setCategoria("Laptop Gamer"); a2.setStock(1);
        deviceService.create(a2);

        Device a3 = new Device();
        a3.setNombre("R1"); a3.setSerial("SER-3"); a3.setCategoria("Router"); a3.setStock(1);
        deviceService.create(a3);

        // Buscar: lap
        List<Device> results = deviceService.searchByCategoria("lap");

        // Validar: retorna Laptop y Laptop Gamer, NO Router
        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(a -> a.getCategoria().equals("Laptop")));
        assertTrue(results.stream().anyMatch(a -> a.getCategoria().equals("Laptop Gamer")));
        assertFalse(results.stream().anyMatch(a -> a.getCategoria().equals("Router")));
    }
}
