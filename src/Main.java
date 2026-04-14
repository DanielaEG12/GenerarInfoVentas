import java.io.*;
import java.util.*;

/**
 * Módulo: Conceptos Fundamentales de Programación
 * Entrega 2 (Semana 5) - Clase Principal de Procesamiento
 * Esta clase lee los archivos generados, procesa totales y genera reportes CSV.
 * This class reads generated files, processes totals, and generates CSV reports.
 * @author Daniela Escobar, Alvaro Enrique moreno, Freddy Ruiz
 */
public class Main {

        // Constantes para evitar repetir nombres de archivos.
    private static final String ARCHIVO_PRODUCTOS = "productos_info.txt";
    private static final String ARCHIVO_VENDEDORES = "vendedores_info.txt";

    public static void main(String[] args) {
        // ES: Mapas para almacenamiento global de datos en memoria
        // EN: Maps for global data storage in memory
        Map<Integer, String[]> infoProductos = new HashMap<>();
        Map<Long, Double> recaudadoPorVendedor = new HashMap<>();
        Map<Long, String> nombresVendedores = new HashMap<>();
        Map<Integer, Integer> cantidadesPorProducto = new HashMap<>();

        try {
            // ES: Carga inicial de archivos maestros
            // EN: Initial load of master files
            cargarProductos(infoProductos, cantidadesPorProducto);
            cargarVendedores(nombresVendedores, recaudadoPorVendedor);
            // ES: Procesamiento de archivos de ventas
            // EN: Sales files processing
            procesarVentas(recaudadoPorVendedor, infoProductos, cantidadesPorProducto);

            // ES: Generación de archivos de salida
            // EN: Output files generation
            generarReporteVendedores(nombresVendedores, recaudadoPorVendedor);
            generarReporteProductos(infoProductos, cantidadesPorProducto);

            System.out.println("Finalización exitosa: Los reportes han sido generados sin errores.");
        } catch (Exception e) {
            System.err.println("Error en el procesamiento: " + e.getMessage());
            e.printStackTrace(); // Mejora: muestra detalle del error.
        }
    }

    /**
     * ES: Carga productos desde el archivo plano. El ID es la clave del mapa.
     * EN: Loads products from the flat file. ID is the map key.
     */
    private static void cargarProductos(Map<Integer, String[]> productos, Map<Integer, Integer> cantidades) throws IOException {
        File file = new File(ARCHIVO_PRODUCTOS); // Uso de constante.
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(";");
                // ES: datos[0]=ID, datos[1]=Nombre, datos[2]=Precio
                // EN: datos[0]=ID, datos[1]=Name, datos[2]=Price
                productos.put(Integer.parseInt(datos[0]), new String[]{datos[1], datos[2]});
                cantidades.put(Integer.parseInt(datos[0]), 0);

                if (datos.length < 3) continue; // Validación básica.

                int id = Integer.parseInt(datos[0]);
                productos.put(id, new String[]{datos[1], datos[2]});
                cantidades.put(id, 0);
            }
        }
    }

    /**
     * ES: Mapea IDs de vendedores con sus nombres y prepara el acumulador de ventas.
     * EN: Maps salesman IDs with their names and prepares the sales accumulator.
     */
    private static void cargarVendedores(Map<Long, String> nombres, Map<Long, Double> totales) throws IOException {
        File file = new File(ARCHIVO_VENDEDORES); // Uso de constante.
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(";");
                if (datos.length < 4) continue; // Validación.

                long id = Long.parseLong(datos[1]);
                nombres.put(id, datos[2] + " " + datos[3]);
                totales.put(id, 0.0);
            }
        }
    }

    /**
     * ES: Itera sobre todos los archivos que empiezan con "ventas_" para calcular totales.
     * EN: Iterates through all files starting with "ventas_" to calculate totals.
     */
    private static void procesarVentas(Map<Long, Double> totales, Map<Integer, String[]> productos, Map<Integer, Integer> cantidades) {
        File carpetaActual = new File(".");
        // ES: Filtro para identificar solo archivos de ventas relevantes
        // EN: Filter to identify only relevant sales files
        File[] archivosVentas = carpetaActual.listFiles((dir, name) -> name.startsWith("ventas_") && name.endsWith(".txt"));
        if (archivosVentas == null) return;

        for (File archivo : archivosVentas) {
            try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
                String primeraLinea = br.readLine();
                if (primeraLinea == null) continue;
                // ES: Obtiene el ID del vendedor desde el encabezado del archivo
                // EN: Gets the salesman ID from the file header

                String[] encabezado = primeraLinea.split(";");
                if (encabezado.length < 2) continue; // Validación.
                
                long idVendedor = Long.parseLong(primeraLinea.split(";")[1]);
                double totalVentaVendedor = 0;
                String lineaVenta;
                while ((lineaVenta = br.readLine()) != null) {
                    String[] datos = lineaVenta.split(";");
                    
                    if (datos.length < 2) continue; // Validación.

                    int idProd = Integer.parseInt(datos[0]);
                    int cant = Integer.parseInt(datos[1]);
                    if (productos.containsKey(idProd)) {
                        double precio = Double.parseDouble(productos.get(idProd)[1]);
                        totalVentaVendedor += (precio * cant);
                        // ES: Actualiza el conteo global de unidades por producto
                        // EN: Updates the global unit count per product
                        cantidades.put(idProd, cantidades.get(idProd) + cant);
                    }
                }
                // ES: Suma el resultado al total acumulado del vendedor
                // EN: Adds the result to the salesman's accumulated total
                totales.put(idVendedor, totales.getOrDefault(idVendedor, 0.0) + totalVentaVendedor);
            } catch (Exception e) { }
        }
    }

    /**
     * ES: Exporta el reporte de vendedores ordenado por mayor recaudación.
     * EN: Exports the salesman report sorted by highest revenue.
     */
    private static void generarReporteVendedores(Map<Long, String> nombres, Map<Long, Double> totales) throws IOException {
        List<Map.Entry<Long, Double>> lista = new ArrayList<>(totales.entrySet());
        lista.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        // ES: Especificamos UTF-8 al crear el PrintWriter
        // EN: We specify UTF-8 when creating the PrintWriter
        try (PrintWriter writer = new PrintWriter(new File("reporte_vendedores.csv"), "UTF-8")) {
            
            // ES: BOM para compatibilidad de caracteres especiales (tildes, ñ) en Excel
            // EN: BOM for special character compatibility (accents, ñ) in Excel
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

    /**
     * ES: Exporta el reporte de productos ordenado por cantidad total vendida.
     * EN: Exports the product report sorted by total quantity sold.
     */
    private static void generarReporteProductos(Map<Integer, String[]> productos, Map<Integer, Integer> cantidades) throws IOException {
        List<Map.Entry<Integer, Integer>> lista = new ArrayList<>(cantidades.entrySet());
        // ES: Ordenar de mayor a menor cantidad vendida
        // EN: Sort from highest to lowest quantity sold
        lista.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        // ES: 1. Forzamos UTF-8 aquí también
        // EN: 1. We force UTF-8 here too
        try (PrintWriter writer = new PrintWriter(new File("reporte_productos.csv"), "UTF-8")) {
            
            // ES: 2. Escribimos el carácter BOM para que Excel no se confunda
            // EN: 2. We write the BOM character so that Excel doesn't get confused
            writer.write('\ufeff');

            for (Map.Entry<Integer, Integer> entrada : lista) {
                
                if (!productos.containsKey(entrada.getKey())) continue; // Validación.
                
                String nombre = productos.get(entrada.getKey())[0];
                String precio = productos.get(entrada.getKey())[1];
                
                // ES: Formato de salida: Nombre;Precio;Cantidad
                // EN: Output format: Name;Price;Quantity
                writer.println(nombre + ";" + precio + ";" + entrada.getValue());
            }
        }
    } 
}
