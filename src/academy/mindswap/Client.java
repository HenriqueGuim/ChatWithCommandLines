package academy.mindswap;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
    Socket serverSocket;
    BufferedReader reader;
    BufferedWriter writer;
    BufferedReader consoleReader;
    ExecutorService threadPool;

    public static void main(String[] args) {
        Client client = new Client();
        client.connectToServer();
        client.startComms();
    }

    private void startComms() {
        startBuffers();
        Listen startListing = new Listen();
        threadPool.submit(startListing);
        startTalking();

    }

    private void startBuffers() {
        try {
            consoleReader = new BufferedReader(new InputStreamReader(System.in));
            reader = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(serverSocket.getOutputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void startTalking() {
        try {
            writer.write(consoleReader.readLine());
            writer.newLine();
            writer.flush();
            startTalking();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void connectToServer() {
        try {
            serverSocket = new Socket("localhost", 8080);
            threadPool = Executors.newFixedThreadPool(1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private class Listen implements Runnable {

        @Override
        public void run() {
            listening();
        }

        private void listening() {
            try {
                String message = reader.readLine();
                System.out.println(message);
                listening();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
