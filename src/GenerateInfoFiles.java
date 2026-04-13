import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Random;

/**
 * Module: Fundamental Programming Concepts
 * Assignment 1 - GenerateInfoFiles (Week 3)
 * This class generates pseudo-random plain text files as input for the sales project.
 * * @author Daniela Escobar, Alvaro Enrique moreno
 */
public class GenerateInfoFiles {

  // Data to generate consistent information
  private static final String[] NOMBRES = {"Juan", "Maria", "Carlos", "Ana", "Luis", "Elena"};
  private static final String[] APELLIDOS = {"Perez", "Gomez", "Rodriguez", "Lopez", "Garcia", "Sanchez"};
  // Change: The accent mark was removed from "Sánchez" to avoid encoding problems, Freddy Ruiz
  
  private static final String[] PRODUCTOS = {"Laptop", "Mouse", "Teclado", "Monitor", "Webcam", "Impresora"};

  // Change: A single reusable Random object is created. Freddy Ruiz
  private static final Random rand = new Random();


  /**
     * Main method: The engine that starts the generation process.
     * Displays a success message or an error message if something goes wrong
     * * @param args Command line arguments
     */
  public static void main(String[] args) {
    try {
      // Generate 10 products and 5 salesmen as required
      createProductsFile(10);
      createSalesManInfoFile(5);

      System.out.println("Finalización exitosa: Los archivos planos han sido generados.");
    } catch (Exception e) {
      System.err.println("Error en la ejecución: " + e.getMessage());
    }
  }

  /**
     * Generates a file containing the available products information
     * Format: i;nombre;precio.
     * * @param productsCount Number of products to generate
     */
  public static void createProductsFile(int productsCount) {
    try (PrintWriter writer = new PrintWriter(new File("productos_info.txt"))) {

      // Repeated creation of Random was eliminated. Freddy Ruiz
      //Random rand = new Random();   "Este estaba lo deje como comentario por si acaso"

      
      for (int i = 1; i <= productsCount; i++) {
        String nombre = PRODUCTOS[rand.nextInt(PRODUCTOS.length)] + " Mod-" + i;
        int precio = 5000 + rand.nextInt(95000);
        writer.println(i + ";" + nombre + ";" + precio);
      }
    } catch (IOException e) {
      System.err.println("Error al crear productos: " + e.getMessage());
    }
  }

  /**
     * Generates a file with the personal information of the salesmen
     * Format: CC;documento;nombre;apellido
     * * @param salesmanCount Number of salesmen to generate
     */
  public static void createSalesManInfoFile(int salesmanCount) {
    try (PrintWriter writer = new PrintWriter(new File("vendedores_info.txt"))) {
      Random rand = new Random();
      for (int i = 0; i < salesmanCount; i++) {
        long documento = 1000000000L + rand.nextInt(900000000);
        String nombre = NOMBRES[rand.nextInt(NOMBRES.length)];
        String apellido = APELLIDOS[rand.nextInt(APELLIDOS.length)];

        // Write salesman data line by line
        writer.println("CC;" + documento + ";" + nombre + ";" + apellido);

        // Automatically create a specific sales file for this salesman
        createSalesMenFile(rand.nextInt(5) + 1, "CC", documento);
      }
    } catch (IOException e) {
      System.err.println("Error al crear vendedores: " + e.getMessage());
    }
  }

  /**
     * Creates an individual sales file for a specific salesman
     * Format: 
     * Line 1: TypeId;id
     * Other lines: idProd;cant
     * * @param salesCount Number of sales records to generate
     * @param typeId Document type (e.g., CC)
     * @param id Document number
     */
  public static void createSalesMenFile(int randomSalesCount, String typeId, long id) {
    String fileName = "ventas_" + id + ".txt";
    try (PrintWriter writer = new PrintWriter(new File(fileName))) {
      writer.println(typeId + ";" + id);
      Random rand = new Random();
      for (int i = 0; i < randomSalesCount; i++) {
        int idProd = rand.nextInt(10) + 1;
        int cant = rand.nextInt(20) + 1;
        writer.println(idProd + ";" + cant);
// The unnecessary final semicolon was removed. Freddy Ruiz
        
      }
    } catch (IOException e) {
      System.err.println("Error al crear archivo de ventas: " + e.getMessage());
    }
  }
}
