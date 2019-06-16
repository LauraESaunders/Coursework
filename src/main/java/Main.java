import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.sqlite.SQLiteConfig;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Main {

    public static Connection db = null;

    public static void main(String[] args) {
        System.out.println("hi" );
        openDatabase("Inventory.db");

        ResourceConfig config = new ResourceConfig();
        config.packages("Controllers");
        config.register(MultiPartFeature.class);
        ServletHolder servlet = new ServletHolder(new ServletContainer(config));

        Server server = new Server(8081);
        ServletContextHandler context = new ServletContextHandler(server, "/");
        context.addServlet(servlet, "/*");

        try {
            server.start();
            System.out.println("Server successfully started.");
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        }

// code to get data from, write to the database etc goes here!
        //listBooks();
        //insertBook(11, "The Gruffalo Returns.", 2);
        //listBooks();
        //updateBook(11, "James and the Giant Peach", 1);
        //listBooks();
        //deleteBook(11);
        //listBooks();
        closeDatabase();
    }
//Open the database
    private static void openDatabase(String dbFile) {
        try  {
            Class.forName("org.sqlite.JDBC");
            SQLiteConfig config = new SQLiteConfig();
            config.enforceForeignKeys(true);
            db = DriverManager.getConnection("jdbc:sqlite:resources/" + dbFile, config.toProperties());
            System.out.println("Database connection successfully established.");
        } catch (Exception exception) {
            System.out.println("Database connection error: " + exception.getMessage());
        }

    }
//Close the database
    private static void closeDatabase(){
        try {
            db.close();
            System.out.println("Disconnected from database!");
        } catch (Exception exception) {
            System.out.println("Database disconnection error: " + exception.getMessage());
        }
    }
    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    //$ curl -s localhost:8081/listBooks
    public static String listBooks(){
        System.out.println("things/list");
        JSONArray list = new JSONArray();
        try {
            PreparedStatement ps = db.prepareStatement("SELECT BookID, Title, AuthorID FROM Books ");
            ResultSet results = ps.executeQuery();
            while (results.next()){

                JSONObject item = new JSONObject();
                item.put("BookID", results.getInt(1));
                item.put("Title", results.getString(2));
                item.put("AuthorID", results.getInt(3));
                list.add(item);
            }
            return list.toString();

        } catch (Exception ex){
            System.out.println("Database error: " + ex.getMessage());
            return "{\"error\": \"Unable to list items, please see server console for more info.\"}";

        }

    }

    public static void insertBook(int bookId, String bookTitle, int authorId){
        try {
            PreparedStatement ps = db.prepareStatement("INSERT INTO Books (BookID, Title, AuthorID) VALUES (?, ?, ?)");
            ps.setInt(1, bookId);
            ps.setString(2, bookTitle);
            ps.setInt(3, authorId);
            ps.executeUpdate();
            System.out.println("Added record");
        } catch (Exception ex){
            System.out.println("Error " + ex.getMessage());
        }

    }

    public static void updateBook(int bookId, String bookTitle, int authorId){
        try {
            PreparedStatement ps = db.prepareStatement("UPDATE Books SET Title = ?, AuthorId = ? WHERE BookId = ?");
            ps.setString(1, bookTitle);
            ps.setInt(2, authorId);
            ps.setInt(3, bookId);
            ps.executeUpdate();
        } catch (Exception ex){
            System.out.println("Error in update: " + ex.getMessage());
        }
    }

    public static void deleteBook (int bookId){
        try {
            PreparedStatement ps = db.prepareStatement("DELETE FROM Books WHERE BookId = ?");
            ps.setInt(1, bookId);
            ps.executeUpdate();
        } catch (Exception ex){
            System.out.println("Error in delete: " + ex.getMessage());
        }
    }

}

