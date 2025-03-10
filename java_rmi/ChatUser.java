import java.rmi.*;

public interface ChatUser extends Remote {
    void displayMessage(String message) throws RemoteException;
}

