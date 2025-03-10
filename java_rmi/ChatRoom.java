import java.rmi.*;

public interface ChatRoom extends Remote {
    void subscribe(ChatUser user, String pseudo) throws RemoteException;
    void unsubscribe(String pseudo) throws RemoteException;
    void postMessage(String pseudo, String message) throws RemoteException;
}

