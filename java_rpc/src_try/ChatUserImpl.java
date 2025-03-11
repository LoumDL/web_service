import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class ChatUserImpl {
    private final String title = "Logiciel de discussion en ligne";
    private String pseudo;
    private XmlRpcClient client;

    private final JFrame window = new JFrame(this.title);
    private final JTextArea txtOutput = new JTextArea();
    private final JTextField txtMessage = new JTextField();
    private final JButton btnSend = new JButton("Envoyer");

    public ChatUserImpl() {
        try {
            // Connexion au serveur XML-RPC
            client = new XmlRpcClient("http://localhost:1950");
            System.out.println("Connexion au serveur XML-RPC réussie.");
            this.createIHM();
            this.requestPseudo();
            this.subscribeToChat();
            this.startMessagePolling();  // Démarrer la récupération des messages
        } catch (IOException e) {
            showErrorAndExit("Erreur de communication avec le serveur : " + e.getMessage());
        } catch (Exception e) {
            showErrorAndExit("Erreur inattendue : " + e.getMessage());
        }
    }

    private void createIHM() {
        JPanel panel = (JPanel) window.getContentPane();
        JScrollPane sclPane = new JScrollPane(txtOutput);
        panel.add(sclPane, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(txtMessage, BorderLayout.CENTER);
        southPanel.add(btnSend, BorderLayout.EAST);
        panel.add(southPanel, BorderLayout.SOUTH);

        // Gestion des événements
        window.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                unsubscribeFromChat();
                System.exit(0);
            }
        });

        btnSend.addActionListener(e -> sendMessage());

        txtMessage.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent event) {
                if (event.getKeyChar() == '\n') {
                    sendMessage();
                }
            }
        });

        txtOutput.setBackground(new Color(220, 220, 220));
        txtOutput.setEditable(false);
        window.setSize(500, 400);
        window.setVisible(true);
        txtMessage.requestFocus();
    }

    private void requestPseudo() {
        while (true) {
            pseudo = JOptionPane.showInputDialog(window, "Entrez votre pseudo :", title, JOptionPane.OK_OPTION);
            if (pseudo != null && !pseudo.trim().isEmpty()) {
                break;
            }
            JOptionPane.showMessageDialog(window, "Le pseudo ne peut pas être vide.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void subscribeToChat() {
        try {
            Vector<String> params = new Vector<>();
            params.add(pseudo);
            String response = (String) client.execute("chatroom.subscribe", params);
            txtOutput.append(response + "\n");
        } catch (XmlRpcException e) {
            showError("Erreur lors de l'inscription : " + e.getMessage());
        } catch (IOException e) {
            showError("Erreur de communication avec le serveur : " + e.getMessage());
        }
    }

    private void unsubscribeFromChat() {
        try {
            Vector<String> params = new Vector<>();
            params.add(pseudo);
            String response = (String) client.execute("chatroom.unsubscribe", params);
            System.out.println(response);
        } catch (XmlRpcException e) {
            System.err.println("Erreur lors de la désinscription : " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Erreur de communication avec le serveur : " + e.getMessage());
        }
    }

    private void sendMessage() {
        try {
            String message = txtMessage.getText().trim();
            if (!message.isEmpty()) {
                Vector<String> params = new Vector<>();
                params.add(pseudo);
                params.add(message);
                String response = (String) client.execute("chatroom.postMessage", params);
                txtOutput.append(response + "\n");
                txtMessage.setText("");
            }
        } catch (XmlRpcException e) {
            showError("Erreur lors de l'envoi du message : " + e.getMessage());
        } catch (IOException e) {
            showError("Erreur de communication avec le serveur : " + e.getMessage());
        }
    }

    private void startMessagePolling() {
        new Thread(() -> {
            while (true) {
                try {
                    Vector<String> params = new Vector<>();
                    Object result = client.execute("chatroom.getMessages", params);

                    if (result instanceof List<?>) {
                        List<?> resultList = (List<?>) result;
                        List<String> newMessages = new ArrayList<>();
                        for (Object obj : resultList) {
                            if (obj instanceof String) {
                                newMessages.add((String) obj);
                            }
                        }

                        // Afficher les nouveaux messages
                        for (String message : newMessages) {
                            txtOutput.append(message + "\n");
                        }
                    } else {
                        System.err.println("Le résultat n'est pas une liste de messages.");
                    }

                    // Attendre 2 secondes avant de récupérer de nouveaux messages
                    Thread.sleep(5000);
                } catch (XmlRpcException | IOException | InterruptedException e) {
                    System.err.println("Erreur de récupération des messages : " + e.getMessage());
                }
            }
        }).start();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(window, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    private void showErrorAndExit(String message) {
        showError(message);
        System.exit(1);
    }

    public static void main(String[] args) {
        new ChatUserImpl();
    }
}
