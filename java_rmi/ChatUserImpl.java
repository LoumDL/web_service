import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.rmi.*;
import java.rmi.server.*;

public class ChatUserImpl extends UnicastRemoteObject implements ChatUser {
    private String title = "Logiciel de discussion en ligne";
    private String pseudo = null;

    private JFrame window = new JFrame(this.title);
    private JTextArea txtOutput = new JTextArea();
    private JTextField txtMessage = new JTextField();
    private JButton btnSend = new JButton("Envoyer");

    private ChatRoom chatRoom; // Référence vers le serveur RMI

    public ChatUserImpl(ChatRoom chatRoom) throws RemoteException {
        super();
        this.chatRoom = chatRoom;
        this.createIHM();
        this.requestPseudo();
        chatRoom.subscribe(this, pseudo); // S'abonner au serveur
    }

    public void createIHM() {
        // Assemblage des composants
        JPanel panel = (JPanel) this.window.getContentPane();
        JScrollPane sclPane = new JScrollPane(txtOutput);
        panel.add(sclPane, BorderLayout.CENTER);
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(this.txtMessage, BorderLayout.CENTER);
        southPanel.add(this.btnSend, BorderLayout.EAST);
        panel.add(southPanel, BorderLayout.SOUTH);

        // Gestion des événements
        window.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                try {
                    chatRoom.unsubscribe(pseudo); // Se désabonner avant de fermer
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
                System.exit(0);
            }
        });
        btnSend.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btnSend_actionPerformed(e);
            }
        });
        txtMessage.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent event) {
                if (event.getKeyChar() == '\n')
                    btnSend_actionPerformed(null);
            }
        });

        // Initialisation des attributs
        this.txtOutput.setBackground(new Color(220, 220, 220));
        this.txtOutput.setEditable(false);
        this.window.setSize(500, 400);
        this.window.setVisible(true);
        this.txtMessage.requestFocus();
    }

    public void requestPseudo() {
        this.pseudo = JOptionPane.showInputDialog(
                this.window, "Entrez votre pseudo : ",
                this.title, JOptionPane.OK_OPTION
        );
        if (this.pseudo == null) System.exit(0);
    }

    public void btnSend_actionPerformed(ActionEvent e) {
        try {
            chatRoom.postMessage(pseudo, this.txtMessage.getText());
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
        this.txtMessage.setText("");
        this.txtMessage.requestFocus();
    }

    @Override
    public void displayMessage(String message) throws RemoteException {
        SwingUtilities.invokeLater(() -> txtOutput.append(message + "\n"));
    }

    public static void main(String[] args) {
        try {
            ChatRoom chatRoom = (ChatRoom) Naming.lookup("rmi://localhost/ChatRoom");
            new ChatUserImpl(chatRoom);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
