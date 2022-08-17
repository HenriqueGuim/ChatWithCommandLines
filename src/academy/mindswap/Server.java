package academy.mindswap;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Server {
    private ServerSocket server;
    private ExecutorService threadPool;
    private CopyOnWriteArrayList<ClientHandler> clientList;

    public static void main(String[] args) {
        Server server1 = new Server();
        server1.startServer(8080);
        server1.accepotclients();

    }


    private void createServerPool() {
        threadPool = Executors.newCachedThreadPool();
    }

    private void accepotclients() {
        try {
            System.out.println("Accepting clients");
            Socket clientSocket = server.accept();
            ClientHandler clientHandler = new ClientHandler(clientSocket);
            System.out.println("New Client arrived");
            clientHandler.welcomeMessage();
            clientList.add(clientHandler);
            threadPool.submit(clientHandler);

            accepotclients();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void startServer(int portNumber) {
        try {
            server = new ServerSocket(portNumber);
            createServerPool();
            clientList = new CopyOnWriteArrayList();
            System.out.println("Server initialized");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void broadCast(ClientHandler client, String message){
        clientList.stream().filter(x -> x != client).forEach( x -> x.sendMessage(client.getName() + ": " + message));
    }


    private class ClientHandler implements Runnable{
        private static int clientNumber = 0;
        private String name;
        private Socket clientSocket;
        private BufferedWriter writer;
        private BufferedReader reader;



        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
            this.name = "Client " + ++clientNumber;
            startBuffers();

        }

        private void welcomeMessage(){
            BufferedReader fileReader = null;
            try {
                fileReader = new BufferedReader(new FileReader(new File("resources/welcome.txt")));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            String message = "";

            try {
                while ((message = fileReader.readLine()) != null) {
                    sendMessage(message);
                }
                fileReader.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            }


        public String getName() {
            return name;
        }

        private void sendMessage(String message){
            try {
                writer.write(message);
                writer.newLine();
                writer.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        public void run() {
         //   startBuffers();
            listening();

        }

        private void listening() {
            String message;
            try {
                message =reader.readLine();

                if(message == null){
                    clientSocket.close();
                }

                if (!isCommand(message)){
                    broadCast(this , message);
                }


            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            listening();
        }

        private void startBuffers() {
            try {
                writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private boolean isCommand(String message){
            if(message.startsWith("/")){
                if(message.toLowerCase().startsWith("/help")){
                    sendMessage(Messages.helpDescription());
                    sendMessage(Messages.listDescription());
                    sendMessage(Messages.shoutDescription());
                    sendMessage(Messages.whispererDescription());
                    return true;
                }
                if(message.toLowerCase().startsWith("/list")){
                    clientList.forEach(client -> this.sendMessage(client.getName()));
                    return true;
                }
                if(message.toLowerCase().startsWith("/shout")){

                    String finalMessage = message.replace("/shout ", "").toUpperCase();

                    broadCast(this, finalMessage);
                    return true;
                }
                if ((message.toLowerCase().startsWith("/whisperer"))){
                    String clientToSend = message.split(" ")[1] + " " + message.split(" ")[2];

                    List<String> messageList = Arrays.stream(message.split(" ")).toList();

                    String finalMessage = getName() + " whispered: " + messageList.subList(3, messageList.size()).stream().collect(Collectors.joining(" "));

                    clientList.stream().filter(clientHandler -> clientHandler.getName().toLowerCase().equals(clientToSend)).forEach(clientHandler -> clientHandler.sendMessage(finalMessage));

                    return true;
                }

                sendMessage("Command not recognised:");

            }
            return false;
        }
    }


}
