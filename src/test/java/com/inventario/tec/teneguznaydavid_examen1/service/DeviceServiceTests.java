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
    @DisplayName("Prueba 1: Evitar registro de dispositivos con serial duplicado")
    void testPrueba1_AvoidDuplicateSerial() {
        System.out.println("  [ACCION] Registrando dispositivo inicial con serial ABC-001");
        Device device1 = new Device();
        device1.setNombre("Dispositivo 1");
        device1.setSerial("ABC-001");
        device1.setCategoria("Categoria 1");
        device1.setStock(10);
        deviceService.create(device1);

        System.out.println("  [VALIDACION] Intentando registrar segundo dispositivo con el mismo serial...");
        Device device2 = new Device();
        device2.setNombre("Dispositivo 2");
        device2.setSerial("ABC-001");
        device2.setCategoria("Categoria 2");
        device2.setStock(5);

        assertThrows(ConflictException.class, () -> deviceService.create(device2));
        
        long count = deviceRepository.count();
        assertEquals(1, count);
        System.out.println("  [OK] ConflictException capturada y base de datos sin duplicados");
    }

    @Test
    @DisplayName("Prueba 2: No permitir el registro de stock negativo")
    void testPrueba2_NoNegativeStock() {
        System.out.println("  [ACCION] Configurando dispositivo con stock -1");
        Device device = new Device();
        device.setNombre("Dispositivo Negativo");
        device.setSerial("NEG-001");
        device.setCategoria("Test");
        device.setStock(-1);

        System.out.println("  [VALIDACION] Verificando que el servicio rechaza stock negativo...");
        assertThrows(IllegalArgumentException.class, () -> deviceService.create(device));
        System.out.println("  [OK] IllegalArgumentException lanzada correctamente");
    }

    @Test
    @DisplayName("Prueba 3: Desactivar un dispositivo correctamente")
    void testPrueba3_DeactivateDevice() {
        System.out.println("  [ACCION] Creando dispositivo activo...");
        Device device = new Device();
        device.setNombre("Laptop");
        device.setSerial("LAP-001");
        device.setCategoria("Computo");
        device.setStock(5);
        device.setAvailable(true);
        Device saved = deviceService.create(device);

        System.out.println("  [VALIDACION] Desactivando dispositivo ID: " + saved.getId());
        deviceService.deactivate(saved.getId());

        Device updated = deviceRepository.findById(saved.getId()).get();
        assertFalse(updated.getAvailable());
        assertEquals("Laptop", updated.getNombre());
        System.out.println("  [OK] Dispositivo desactivado manteniendo sus datos originales");
    }

    @Test
    @DisplayName("Prueba 4: Obtener estadísticas correctas del inventario")
    void testPrueba4_InventoryStatistics() {
        System.out.println("  [ACCION] Registrando 2 dispositivos disponibles y 1 no disponible...");
        Device a1 = new Device(); a1.setNombre("A1"); a1.setSerial("S1"); a1.setCategoria("C1"); a1.setStock(1); a1.setAvailable(true);
        deviceService.create(a1);
        Device a2 = new Device(); a2.setNombre("A2"); a2.setSerial("S2"); a2.setCategoria("C2"); a2.setStock(1); a2.setAvailable(true);
        deviceService.create(a2);
        Device a3 = new Device(); a3.setNombre("A3"); a3.setSerial("S3"); a3.setCategoria("C3"); a3.setStock(1); a3.setAvailable(false);
        deviceService.create(a3);

        System.out.println("  [VALIDACION] Calculando estadísticas...");
        Map<String, Long> stats = deviceService.getStatistics();
        assertEquals(3L, stats.get("total"));
        assertEquals(2L, stats.get("available"));
        assertEquals(1L, stats.get("unavailable"));
        System.out.println("  [OK] Estadísticas: Total=3, Disponibles=2, No Disponibles=1");
    }

    @Test
    @DisplayName("Prueba 5: Verificar la eliminación lógica (soft delete)")
    void testPrueba5_LogicalDeletion() {
        System.out.println("  [ACCION] Creando dispositivo y aplicando soft delete...");
        Device device = new Device();
        device.setNombre("Eliminar");
        device.setSerial("DEL-001");
        device.setCategoria("Test");
        device.setStock(1);
        Device saved = deviceService.create(device);

        deviceService.softDelete(saved.getId());

        System.out.println("  [VALIDACION] Verificando estado 'deleted' y visibilidad en consultas...");
        Device inDb = deviceRepository.findById(saved.getId()).get();
        assertTrue(inDb.getDeleted());
        
        List<Device> activeOnes = deviceService.findAllActive();
        assertFalse(activeOnes.stream().anyMatch(a -> a.getId().equals(saved.getId())));
        System.out.println("  [OK] Dispositivo marcado como eliminado y oculto en consultas activas");
    }

    @Test
    @DisplayName("Prueba 6: Búsqueda parcial por categoría (case insensitive)")
    void testPrueba6_PartialCategorySearch() {
        System.out.println("  [ACCION] Registrando dispositivos: Laptop, Laptop Gamer, Router");
        Device a1 = new Device(); a1.setNombre("L1"); a1.setSerial("SER-1"); a1.setCategoria("Laptop"); a1.setStock(1);
        deviceService.create(a1);
        Device a2 = new Device(); a2.setNombre("L2"); a2.setSerial("SER-2"); a2.setCategoria("Laptop Gamer"); a2.setStock(1);
        deviceService.create(a2);
        Device a3 = new Device(); a3.setNombre("R1"); a3.setSerial("SER-3"); a3.setCategoria("Router"); a3.setStock(1);
        deviceService.create(a3);

        System.out.println("  [VALIDACION] Buscando categoría con término 'lap'...");
        List<Device> results = deviceService.searchByCategoria("lap");

        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(a -> a.getCategoria().equals("Laptop")));
        System.out.println("  [OK] Se encontraron 2 resultados que contienen 'lap'");
    }

    @Test
    @DisplayName("BONUS: Consultar dispositivos con stock bajo (< 5)")
    void testBonus_LowStock() {
        System.out.println("  [ACCION] Registrando dispositivos con stock 2, 4 y 10...");
        Device a1 = new Device(); a1.setNombre("Bajo1"); a1.setSerial("LOW-1"); a1.setCategoria("C1"); a1.setStock(2);
        deviceService.create(a1);
        Device a2 = new Device(); a2.setNombre("Bajo2"); a2.setSerial("LOW-2"); a2.setCategoria("C2"); a2.setStock(4);
        deviceService.create(a2);
        Device a3 = new Device(); a3.setNombre("Alto1"); a3.setSerial("HIGH-1"); a3.setCategoria("C3"); a3.setStock(10);
        deviceService.create(a3);

        System.out.println("  [VALIDACION] Consultando dispositivos con stock < 5...");
        List<Device> lowStock = deviceService.getLowStock(5);

        assertEquals(2, lowStock.size());
        assertTrue(lowStock.stream().allMatch(d -> d.getStock() < 5));
        System.out.println("  [OK] Se retornaron correctamente los 2 dispositivos con bajo stock");
    }
}
