package org.academiadecodigo.hexallents.party.server;

import org.academiadecodigo.bootcamp.Prompt;
import org.academiadecodigo.bootcamp.scanners.menu.MenuInputScanner;
import org.academiadecodigo.bootcamp.scanners.string.StringInputScanner;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Creates a Server
 */
public class Server {

    private ServerSocket serverSocket;
    private Map<String, PlayerWorker> playerWorkerMap;
    private static final int MAX_PLAYERS = 3;
    private final int PORT_NUMBER = 7070;
    public static final int ROUNDS = 10;
    private ExecutorService executor;
    private String answer = "";


    public Server() {

        try {
            serverSocket = new ServerSocket(PORT_NUMBER);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        executor = Executors.newFixedThreadPool(MAX_PLAYERS);

        playerWorkerMap = new HashMap<>();
    }

    /**
     * Creates Player Workers
     *
     * @throws IOException
     */
    public void listen() throws IOException {

        Socket socket = serverSocket.accept();
        PlayerWorker playerWorker = new PlayerWorker(socket);

        playerWorker.send("What is your name?");
        String name = playerWorker.read();
        playerWorker.setName(name);

        playerWorkerMap.put(playerWorker.name, playerWorker);
        executor.submit(playerWorker);

        if (playerWorkerMap.size() < MAX_PLAYERS) {
            sendAll("There are " + playerWorkerMap.size() + " players. Wait for more players");
            listen();
        }

    }

    /**
     * Ends Server Socket and Game
     */
    public void endGame() {
        executor.shutdown();
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void makeThreadsWait() {

        synchronized (this) {
            while (playerWorkerMap.size() < MAX_PLAYERS) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            notifyAll();
        }
    }

    /**
     * Sends a message to all Player Workers
     *
     * @param string Message to send
     */
    public void sendAll(String string) {

        for (PlayerWorker p : playerWorkerMap.values()) {

            p.send(string);
        }
    }


    /**
     * Gets player's names
     *
     * @return List of Players Names
     */
    public List<String> getPlayerNames() {

        ArrayList<String> list = new ArrayList<>();

        list.addAll(playerWorkerMap.keySet());

        return list;
    }


    private synchronized void setAnswer(String answer) {

        System.out.println("NO SET: " + answer);
        this.answer = answer;
    }


    public String getAnswer() {
        String sent = answer;
        answer = "";
        return sent;
    }

    private class PlayerWorker implements Runnable {

        private final Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String name;
        private Prompt prompt;

        private PlayerWorker(Socket socket) {

            this.socket = socket;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream());
                prompt = new Prompt(socket.getInputStream(), new PrintStream(socket.getOutputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        @Override
        public void run() {
            makeThreadsWait();
            System.out.println("GAME STARTED");
            inGame();
            closeSocket();
        }

        private void inGame() {

            StringBuilder userInput = new StringBuilder();

            while (true) {
                userInput.append(name + ":" + read());
                System.out.println("NO INGAME: " + userInput.toString());
                setAnswer(userInput.toString());
                userInput.delete(0, userInput.length());
            }
        }

        private void closeSocket() {
            try {
                out.println("The end!");
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void send(String string) {
            out.println(string);
            out.flush();
        }

        private String read() {

            try {
                return in.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return "";
        }

        @Override
        public String toString() {
            return name;
        }

        private void setName(String name) {
            this.name = name;
        }
    }
}