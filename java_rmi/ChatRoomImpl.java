import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.util.*;

public class ChatRoomImpl extends UnicastRemoteObject implements ChatRoom {
    private Map<String, ChatUser> users = new HashMap<>();

    public ChatRoomImpl() throws RemoteException {
        super();
    }

    @Override
    public synchronized void subscribe(ChatUser user, String pseudo) throws RemoteException {
        users.put(pseudo, user);
        broadcastMessage("Système", pseudo + " a rejoint la salle.");
        System.out.println(pseudo + " s'est abonné.");
    }

    @Override
    public synchronized void unsubscribe(String pseudo) throws RemoteException {
        if (users.remove(pseudo) != null) {
            broadcastMessage("Système", pseudo + " a quitté la salle.");
            System.out.println(pseudo + " s'est désabonné.");
        }
    }

    @Override
    public synchronized void postMessage(String pseudo, String message) throws RemoteException {
        broadcastMessage(pseudo, message);
        System.out.println(pseudo + ": " + message);
    }

    private synchronized void broadcastMessage(String sender, String message) throws RemoteException {
        for (ChatUser user : users.values()) {
            user.displayMessage(sender + ": " + message);
        }
    }

    public static void main(String[] args) {
        try {
            // Création du registre RMI
            LocateRegistry.createRegistry(1099); // Port RMI par défaut
            System.out.println("Registre RMI démarré sur le port 1099.");

            // Enregistrement de l'objet distant
            ChatRoomImpl chatRoom = new ChatRoomImpl();
            Naming.rebind("ChatRoom", chatRoom);
            System.out.println("Serveur de discussion en ligne prêt.");

        } catch (Exception e) {
            System.err.println("Erreur lors du démarrage du serveur :");
            e.printStackTrace();
        }
    }
}
