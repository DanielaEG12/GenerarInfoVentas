import java.io.*;
import java.util.*;

/**
 * Módule: Conceptos Fundamentales de Programación
 * Entrega 2 (Semana 5) - Clase Principal de Procesamiento
 * This Class reads generated files, processes totals and creates CSV reports.
 * @author Daniela Escobar, Alvaro Enrique moreno
 */
public class Main {

    public static void main(String[] args) {
        
        // Map storing product information:
        // Key: Product ID | Value: [Name, Price]
        Map<Integer, String[]> infoProductos = new HashMap<>();
        
        // Map storing total revenue per seller
        Map<Long, Double> recaudadoPorVendedor = new HashMap<>();
        
        // Map storing seller names
        Map<Long, String> nombresVendedores = new HashMap<>();
        
        // Map storing quantity sold per product
        Map<Integer, Integer> cantidadesPorProducto = new HashMap<>();

        try {
            // Load initial data from files
            cargarProductos(infoProductos, cantidadesPorProducto);
            cargarVendedores(nombresVendedores, recaudadoPorVendedor);

            // Process all sales files
            procesarVentas(recaudadoPorVendedor, infoProductos, cantidadesPorProducto);

            // Generate final reports
            generarReporteVendedores(nombresVendedores, recaudadoPorVendedor);
            generarReporteProductos(infoProductos, cantidadesPorProducto);

            System.out.println("Finalización exitosa: Los reportes han sido generados sin errores.");
        } catch (Exception e) {
            // General error handling
            System.err.println("Error en el procesamiento: " + e.getMessage());
        }
    }
    
    /**
     * Reads product file and fills corresponding maps
     */
    private static void cargarProductos(Map<Integer, String[]> productos, Map<Integer, Integer> cantidades) throws IOException {
        File file = new File("productos_info.txt");
        // If file does not exist, stop execution
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String linea;

            // Read each line of the file
            while ((linea = br.readLine()) != null) {
                // Split data by semicolon
                String[] datos = linea.split(";");
                // Store product: ID -> [Name, Price]
                productos.put(Integer.parseInt(datos[0]), new String[]{datos[1], datos[2]});

                // Initialize sold quantity to 0
                cantidades.put(Integer.parseInt(datos[0]), 0);
            }
        }
    }

    /**
     * Reads sellers file and loads names and initializes totals
     */
    private static void cargarVendedores(Map<Long, String> nombres, Map<Long, Double> totales) throws IOException {
        File file = new File("vendedores_info.txt");
        // Validate file existence
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(";");
                // Get seller ID
                long id = Long.parseLong(datos[1]);
                // Build full name
                nombres.put(id, datos[2] + " " + datos[3]);
                // Initialize total to 0
                totales.put(id, 0.0);
            }
        }
    }
    /**
     * Processes all sales files and accumulates results
     */
    private static void procesarVentas(Map<Long, Double> totales, Map<Integer, String[]> productos, Map<Integer, Integer> cantidades) {
        // Current project directory
        File carpetaActual = new File(".");
        // Filter files matching pattern ventas_*.txt
        File[] archivosVentas = carpetaActual.listFiles((dir, name) -> name.startsWith("ventas_") && name.endsWith(".txt"));
        if (archivosVentas == null) return;

        // Iterate through each sales file
        for (File archivo : archivosVentas) {
            try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
                String primeraLinea = br.readLine();
                if (primeraLinea == null) continue;
                long idVendedor = Long.parseLong(primeraLinea.split(";")[1]);
                double totalVentaVendedor = 0;
                String lineaVenta;
                // Read each sale line
                while ((lineaVenta = br.readLine()) != null) {
                    String[] datos = lineaVenta.split(";");
                    int idProd = Integer.parseInt(datos[0]);
                    int cant = Integer.parseInt(datos[1]);
                    // Validate product existence
                    if (productos.containsKey(idProd)) {
                        double precio = Double.parseDouble(productos.get(idProd)[1]);
                        
                        // Calculate sale total
                        totalVentaVendedor += (precio * cant);
                        // Accumulate quantity sold
                        cantidades.put(idProd, cantidades.get(idProd) + cant);
                    }
                }
                // Accumulate total per seller
                totales.put(idVendedor, totales.getOrDefault(idVendedor, 0.0) + totalVentaVendedor);
            } catch (Exception e) {
                // Ignore individual errors to avoid stopping execution
            }
        }
    }
    /**
     * Generates CSV report of sellers sorted by sales
     */
    private static void generarReporteVendedores(Map<Long, String> nombres, Map<Long, Double> totales) throws IOException {
        // Convert map to list for sorting
        List<Map.Entry<Long, Double>> lista = new ArrayList<>(totales.entrySet());
        // Sort descending by total sales
        lista.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        // We specify UTF-8 when creating the PrintWriter
        try (PrintWriter writer = new PrintWriter(new File("reporte_vendedores.csv"), "UTF-8")) {
            
            // BOM for Excel compatibility (accents)
            writer.write('\ufeff'); 

            for (Map.Entry<Long, Double> entrada : lista) {
                Long idVendedor = entrada.getKey();
                String nombreCompleto = nombres.get(idVendedor);
                Double totalRecaudado = entrada.getValue();

                // Validate name existence
                if (nombreCompleto != null) {
                    writer.println(nombreCompleto + ";" + idVendedor + ";" + totalRecaudado);
                }
            }
        }
    }

    /**
     * Generates CSV report of products sorted by quantity sold
     */
    private static void generarReporteProductos(Map<Integer, String[]> productos, Map<Integer, Integer> cantidades) throws IOException {
        List<Map.Entry<Integer, Integer>> lista = new ArrayList<>(cantidades.entrySet());
        // Sort descending by quantity sold
        lista.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        // We're forcing UTF-8 here too
        try (PrintWriter writer = new PrintWriter(new File("reporte_productos.csv"), "UTF-8")) {
            
            // BOM to prevent encoding issues in Excel
            writer.write('\ufeff');

            for (Map.Entry<Integer, Integer> entrada : lista) {
                // Get product name and price
                String nombre = productos.get(entrada.getKey())[0];
                String precio = productos.get(entrada.getKey())[1];
                
                // CSV format: Name;Price;QuantitySold
                writer.println(nombre + ";" + precio + ";" + entrada.getValue());
            }
        }
    } 
}
