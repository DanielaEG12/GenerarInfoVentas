import java.io.*;
import java.util.*;

/**
 * Módulo: Conceptos Fundamentales de Programación
 * Entrega 2 (Semana 5) - Clase Principal de Procesamiento
 * Esta clase lee los archivos generados, procesa totales y genera reportes CSV.
 * @author Daniela Escobar, Alvaro Enrique moreno
 */
public class Main {

    public static void main(String[] args) {
        Map<Integer, String[]> infoProductos = new HashMap<>();
        Map<Long, Double> recaudadoPorVendedor = new HashMap<>();
        Map<Long, String> nombresVendedores = new HashMap<>();
        Map<Integer, Integer> cantidadesPorProducto = new HashMap<>();

        try {
            cargarProductos(infoProductos, cantidadesPorProducto);
            cargarVendedores(nombresVendedores, recaudadoPorVendedor);
            procesarVentas(recaudadoPorVendedor, infoProductos, cantidadesPorProducto);

            generarReporteVendedores(nombresVendedores, recaudadoPorVendedor);
            generarReporteProductos(infoProductos, cantidadesPorProducto);

            System.out.println("Finalización exitosa: Los reportes han sido generados sin errores.");
        } catch (Exception e) {
            System.err.println("Error en el procesamiento: " + e.getMessage());
        }
    }

    private static void cargarProductos(Map<Integer, String[]> productos, Map<Integer, Integer> cantidades) throws IOException {
        File file = new File("productos_info.txt");
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(";");
                productos.put(Integer.parseInt(datos[0]), new String[]{datos[1], datos[2]});
                cantidades.put(Integer.parseInt(datos[0]), 0);
            }
        }
    }

    private static void cargarVendedores(Map<Long, String> nombres, Map<Long, Double> totales) throws IOException {
        File file = new File("vendedores_info.txt");
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(";");
                long id = Long.parseLong(datos[1]);
                nombres.put(id, datos[2] + " " + datos[3]);
                totales.put(id, 0.0);
            }
        }
    }

    private static void procesarVentas(Map<Long, Double> totales, Map<Integer, String[]> productos, Map<Integer, Integer> cantidades) {
        File carpetaActual = new File(".");
        File[] archivosVentas = carpetaActual.listFiles((dir, name) -> name.startsWith("ventas_") && name.endsWith(".txt"));
        if (archivosVentas == null) return;

        for (File archivo : archivosVentas) {
            try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
                String primeraLinea = br.readLine();
                if (primeraLinea == null) continue;
                long idVendedor = Long.parseLong(primeraLinea.split(";")[1]);
                double totalVentaVendedor = 0;
                String lineaVenta;
                while ((lineaVenta = br.readLine()) != null) {
                    String[] datos = lineaVenta.split(";");
                    int idProd = Integer.parseInt(datos[0]);
                    int cant = Integer.parseInt(datos[1]);
                    if (productos.containsKey(idProd)) {
                        double precio = Double.parseDouble(productos.get(idProd)[1]);
                        totalVentaVendedor += (precio * cant);
                        cantidades.put(idProd, cantidades.get(idProd) + cant);
                    }
                }
                totales.put(idVendedor, totales.getOrDefault(idVendedor, 0.0) + totalVentaVendedor);
            } catch (Exception e) { }
        }
    }

    private static void generarReporteVendedores(Map<Long, String> nombres, Map<Long, Double> totales) throws IOException {
        List<Map.Entry<Long, Double>> lista = new ArrayList<>(totales.entrySet());
        lista.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        // Especificamos UTF-8 al crear el PrintWriter
        try (PrintWriter writer = new PrintWriter(new File("reporte_vendedores.csv"), "UTF-8")) {
            
            // ESCRIBIR EL BOM: Esto evita el error de "Ana SÃ¡nchez" en Excel
            writer.write('\ufeff'); 

            for (Map.Entry<Long, Double> entrada : lista) {
                Long idVendedor = entrada.getKey();
                String nombreCompleto = nombres.get(idVendedor);
                Double totalRecaudado = entrada.getValue();

                if (nombreCompleto != null) {
                    writer.println(nombreCompleto + ";" + idVendedor + ";" + totalRecaudado);
                }
            }
        }
    }

    private static void generarReporteProductos(Map<Integer, String[]> productos, Map<Integer, Integer> cantidades) throws IOException {
        List<Map.Entry<Integer, Integer>> lista = new ArrayList<>(cantidades.entrySet());
        // Ordenar de mayor a menor cantidad vendida
        lista.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        // 1. Forzamos UTF-8 aquí también
        try (PrintWriter writer = new PrintWriter(new File("reporte_productos.csv"), "UTF-8")) {
            
            // 2. Escribimos el carácter BOM para que Excel no se confunda
            writer.write('\ufeff');

            for (Map.Entry<Integer, Integer> entrada : lista) {
                String nombre = productos.get(entrada.getKey())[0];
                String precio = productos.get(entrada.getKey())[1];
                
                // Formato: Nombre;Precio;CantidadVendida
                writer.println(nombre + ";" + precio + ";" + entrada.getValue());
            }
        }
    } 
}