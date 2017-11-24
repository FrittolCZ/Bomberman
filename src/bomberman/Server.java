/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bomberman;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author fanda
 */
public class Server implements Runnable {

    /**
     * Jméno vlastníka serveru
     */
    private final String owner;
    /**
     * Maximální počet hráčů
     */
    private final int maxPlayers = 4;
    /**
     * Socket
     */
    private ServerSocket serverSocket;
    private final int port = 4242;

    /**
     * List všech klientů připojených na server
     */
    private final ArrayList<ClientThread> clients;

    /**
     * Příznak běžící hry, do připojené hry se nemůžou připojit další hráči
     */
    private boolean gameRunning = false;

    public Server(String owner) {
        this.owner = owner;
        clients = new ArrayList<>();
    }

    private synchronized String getClientsNames() {
        StringBuilder names = new StringBuilder();
        clients.forEach((client) -> {
            names.append(client.getUsername()).append(" ");
        });
        return names.toString();
    }

    private void stop() throws IOException {
        serverSocket.close();
    }

    private synchronized void remove(String username) {
        // Procházej clienty dokud nenajdeš toho jehož jméno je username
        for (int i = 0; i < clients.size(); ++i) {
            ClientThread ct = clients.get(i);
            if (ct.getUsername().equals(username)) {
                clients.remove(i);
                ct.close();
                return;
            }
        }
    }

    /**
     * Vyšle zprávu všem klientům
     *
     * @param message - zpráva pro klienty
     */
    private synchronized void broadcast(String msg) {
        //Projde všechny klienty a zašle jim zprávu
        clients.forEach((ct) -> {
            try {
                ct.writeMsg(msg);
            } catch (IOException ex) {
            }
        });
    }

    /**
     * spustí server
     */
    @Override
    public void run() {
        // Vytvoř socket a čekej na žádosti o připojení 
        try {
            serverSocket = new ServerSocket(port);

            // čekej dokud klienti 
            while (true) {
                // Pokud je dosaženo maxima hráčů server již další nepřijímá 
                if (clients.size() < maxPlayers && !gameRunning) {
                    System.out.println("Server: Čekám na hráče");
                    Socket socket = serverSocket.accept(); // přijmi připojení
                    ClientThread t = new ClientThread(socket);  // vytvoř nové vlákno klienta
                    clients.add(t);
                    t.start();
                }
            }
        } catch (IOException e) {
            System.out.println("Server: Chyba při vytváření socketu: " + e);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }

        for (int i = 0; i < clients.size(); ++i) {
            ClientThread tc = clients.get(i);
            tc.close();
        }
    }

    /**
     * Validace přijaté zprávy
     *
     * @param msg - text zprávy
     * @return - True pokud je zpráva validní
     */
    private boolean validMsg(String msg) {
        // rozdělí zprávu 
        String[] parts = msg.split(" ");

        // druhé slovo vždy musí být jméno odesílatele a odesílatel musí být klient připojený na server
        if (getClientsNames().contains(parts[1])) {
            // dále rozděluje podle prvního slova (příkazu)
            if (parts[0].equals("MOVE")) {
                if (parts.length == 3 && validDir(parts[2])) {
                    return true;
                }
            } else if (parts[0].equals("PLANT") && parts.length == 2) {
                return true;
            } else if (parts[0].equals("START") && parts.length == 1) {
                return true;
            } else if (parts[0].equals("MSG")) {
                return true;
            } else if (parts[0].equals("STOP") && parts.length == 2) {
                return true;
            } else if (parts[0].equals("STARTGAME") && parts.length == 2) {
                return true;
            } else if (parts[0].equals("DISCONNECT") && parts.length == 2) {
                return true;
            }
        }
        return false;
    }

    /**
     * Ověří dle stringu zda se jedná o validní směr pohybu
     *
     * @param dir
     * @return
     */
    private boolean validDir(String dir) {
        for (Direction d : Direction.values()) {
            if (d.getName().equals(dir)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Aktualizuje stav hry nebo serveru dle přijaté zprávy
     *
     * @param msg zpráva
     * @throws IOException
     */
    private void updateGame(String msg) throws IOException {
        // Rozdělí zprávu
        String[] parts = msg.split(" ");
        switch (parts[0]) {
            case "MOVE":
                broadcast(msg);
                break;
            case "PLANT":
                broadcast(msg);
                break;
            case "STARTGAME":
                broadcast("START");
                gameRunning = true;
                break;
            case "MSG":
                // přepošle všem hráčům novou chatovou zprávu
                broadcast(msg);
                break;
            case "STOP":
                // ukončí běh serveru (pouze pokud zpráva přišla od tvůrce serveru)
                if (parts[1].equals(owner)) {
                    stop();
                }
                break;
            case "DISCONNECT":
                remove(parts[1]);
                broadcast("DISCONNECTED " + parts[1]);
                break;
            default:
                break;

        }
    }

    /**
     * Vlákno na kterém server poslouchá zprávy od klienta
     */
    class ClientThread extends Thread {

        private Socket socket;
        private ObjectInputStream sInput;
        private ObjectOutputStream sOutput;

        // Vlákno je identifikováno dle jména klienta
        private String username;

        // Konstruktor vlákna hráče
        ClientThread(Socket socket) throws ClassNotFoundException {
            this.socket = socket;
            System.out.println("Server: Vytvářím Input/Output Streams");
            try {
                // vytvoří streamy ze socketu
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput = new ObjectInputStream(socket.getInputStream());
                // Přečte ze vstupu uživatelské jméno
                username = (String) sInput.readObject();
                System.err.println("Server: Hráč " + username + " se připojil");
            } catch (IOException e) {
                System.out.println("Chyba při vytváření Input/output Streams: " + e);
            }
        }

        /**
         *
         * @return Vrací jméno klienta na tomto vlákně
         */
        public String getUsername() {
            return username;
        }

        /**
         * V neustále smyčce čte zprávy od klienta
         */
        @Override
        public void run() {
            // pošle všem hráčům zprávu o nově připojeném hráči
            broadcast("PLAYERS_CONNECTED " + getClientsNames());
            // čte zprávy dokud se hráč neodhlasí
            try {
                while (true) {
                    //Přečtu zprávu od klienta
                    String msg = (String) sInput.readObject();
                    System.out.println("SERVER: " + msg);
                    if (validMsg(msg)) {
                        updateGame(msg);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
            }
        }

        /**
         * Ukončí spojení
         */
        public void close() {
            try {
                if (sOutput != null) {
                    sOutput.close();
                }
            } catch (IOException e) {
            }
            try {
                if (sInput != null) {
                    sInput.close();
                }
            } catch (IOException e) {
            }
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
            }

        }

        /**
         * Pošle zprávu klientovi
         *
         * @param msg
         * @throws IOException
         */
        public void writeMsg(String msg) throws IOException {
            if (!socket.isConnected()) {
                close();
            }
            sOutput.writeObject(msg);
        }
    }
}
