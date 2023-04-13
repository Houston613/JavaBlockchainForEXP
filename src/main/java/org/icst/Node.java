package org.icst;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class Node implements Serializable {


    private final int port;
    private Blockchain blockchain;



    private Block latestBlock;
    private List<Integer> peers;

    public Node(int port) {
        List<Integer> currentPeers = new ArrayList<>();
        Blockchain currentBlockchain = new Blockchain(new ArrayList<>(), port );
        this.port = port;
        currentPeers.add(port);
        while (port > 8080) {
            port -= 1;
            currentPeers.add(port);
        }
        this.peers = currentPeers;
        this.blockchain = currentBlockchain;
        this.latestBlock = currentBlockchain.getBlock();
    }

    public void start() {
        new Thread(this::listenForConnections).start();
        new Thread(this::mining).start();
    }

    private void mining(){
        while (true) {
            Block minedBlock = blockchain.blockMining(latestBlock);
            System.out.println("My port is "+ getPort() +" and index of hash that i mined is "+ minedBlock.getIndex());
            System.out.println();

            latestBlock = blockchain.createNewBlock(minedBlock,1);
        }
    }

    private void listenForConnections() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(() -> handleConnection(socket)).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleConnection(Socket socket) {
        try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
            System.out.println("connected to"+ socket);
            Message message = (Message) in.readObject();
            MessageType messageType = message.getType();
            switch (messageType) {
                case REQUEST_BLOCKCHAIN -> {
                    Message messageToSend = new Message(MessageType.SEND_BLOCKCHAIN);
                    messageToSend.setBlockchain(getBlockchain());
                    out.writeObject(messageToSend);
                }
                case SEND_BLOCKCHAIN -> blockchain = message.getBlockchain();

                case REQUEST_PEERS -> {
                    Message messageToSend = new Message(MessageType.SEND_PEERS);
                    messageToSend.setPeers(getPeers());
                    out.writeObject(messageToSend);
                }
                case SEND_PEERS -> setPeers(message.getPeers());

                case REQUEST_PORT -> {
                    Message messageToSend = new Message(MessageType.SEND_PORT);
                    messageToSend.setPort(getPort());
                    out.writeObject(messageToSend);
                    System.out.println("My port is  "+ getPort()+  "going to");
                    System.out.println("Port to " + socket);
                }
                case SEND_PORT -> {
                    if (!peers.contains(message.getPort())) {
                        System.out.println("My port in handleConnection "+ getPort());
                        peers.add(message.getPort());
                        System.out.println(getPort() +" add port "+ message.getPort());
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    public void broadcastPort() {
        List<Integer> listForIteration = new ArrayList<>(peers);
        for (Integer peer : listForIteration) {
            if (peer != port) {
                try (Socket socket = new Socket("localhost", peer);
                     ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
                    System.out.println("My port is "+ getPort());
                    System.out.println("Trying to send port to " + socket);
                    Message messageToSend = new Message(MessageType.SEND_PORT);
                    messageToSend.setPort(getPort());
                    out.writeObject(messageToSend);
                } catch (IOException e) {
                    System.err.println("Failed to send block to peer " + peer);
                }
            }
        }
    }


    private void setPeers(List<Integer> peers) {
        this.peers = peers;
    }

    public void broadcastBlock(Blockchain blockchain) {
        for (Integer peer : peers) {
            try (Socket socket = new Socket("localhost", peer);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
                out.writeObject(MessageType.SEND_BLOCKCHAIN);
                out.writeObject(blockchain);
            } catch (IOException e) {
                System.err.println("Failed to send block to peer " + peer);
            }
        }
    }


    public Blockchain getBlockchain() {
        return blockchain;
    }

    public List<Integer> getPeers() {
        return peers;
    }

    public int getPort() {
        return port;
    }

    public void setBlockchain(Blockchain blockchain) {
        this.blockchain = blockchain;
    }

    public Block getLatestBlock() {
        return latestBlock;
    }

    public void setLatestBlock(Block latestBlock) {
        this.latestBlock = latestBlock;
    }
}