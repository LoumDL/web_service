import org.apache.xmlrpc.WebServer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

public class Chatroom {
    private final Set<String> users = new HashSet<>();
    private final List<String> messages = new ArrayList<>();

    // Méthode pour s'abonner
    public synchronized String subscribe(String pseudo) {
        if (users.contains(pseudo)) {
            return "Pseudo déjà pris !";
        }
        users.add(pseudo);
        System.out.println("[SERVER] " + pseudo + " a rejoint la salle.");
        return pseudo + " a rejoint la salle.";
    }

    // Méthode pour se désabonner
    public synchronized String unsubscribe(String pseudo) {
        if (users.remove(pseudo)) {
            System.out.println("[SERVER] " + pseudo + " a quitté la salle.");
            return pseudo + " a quitté la salle.";
        }
        return "Utilisateur non trouvé !";
    }

    // Méthode pour envoyer un message
    public synchronized String postMessage(String pseudo, String message) {
        if (!users.contains(pseudo)) {
            return "Erreur : utilisateur non trouvé.";
        }
        String fullMessage = pseudo + " dit : " + message;
        messages.add(fullMessage);  // Ajouter le message à la liste des messages
        System.out.println("[CHAT] " + fullMessage);
        return fullMessage;
    }

    // Méthode pour récupérer les messages
    public synchronized Vector<String> getMessages() {
        return new Vector<>(messages); // Convertit ArrayList en Vector .
    }

    public static void main(String[] args) {
        try {
            System.out.println("Démarrage du serveur XML-RPC...");
            WebServer server = new WebServer(1950);
            Chatroom chatroom = new Chatroom();
            server.addHandler("chatroom", chatroom);
            server.start();
            System.out.println("Serveur prêt sur le port 1950.");
        } catch (Exception e) {
            System.err.println("Erreur serveur : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
