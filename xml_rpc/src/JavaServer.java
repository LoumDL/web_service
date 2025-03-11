import org.apache.xmlrpc.*;


public class JavaServer {

    // Méthode pour effectuer la somme
    public Integer sum(int x,int y) { 
        return x + y; 
    }

    public static void main(String[] args) {

        try {
            System.out.println("Attempting to start XML-RPC Server...");

            // Démarrage du serveur XML-RPC sur le port 8080
            WebServer server = new WebServer(8080); // Le port 8080 peut nécessiter des permissions d'administrateur
            server.addHandler("sample", new JavaServer());
            server.start();

            System.out.println("Started successfully.");
            System.out.println("Accepting requests. (Halt program to stop.)");
        } catch (Exception exception) {
            System.err.println("JavaServer: " + exception.getMessage());
            exception.printStackTrace();
        }
    }
}
