package org.icst;

import javax.sound.midi.Soundbank;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;


public class Node implements Serializable {


    private final int port;
    private final Blockchain blockchain;


    private Block latestBlock;
    private final List<Integer> peers;
    private final ExecutorService threadPool;
    private ExecutorService miningExecutor;
    private BlockingDeque<Message> messageDeque;
    List<Integer> peersToAck;
    Socket socket;
    Message message = null;
    private final String address;

    public Node(String address, int port, int initPort) {
        this.address = address;
        List<Integer> currentPeers = new ArrayList<>();
        Blockchain currentBlockchain = new Blockchain(new ArrayList<>(), port);
        this.port = port;
        currentPeers.add(port);
        while (port > initPort) {
            port -= 1;
            currentPeers.add(port);
        }

        this.peers = currentPeers;
        this.peersToAck = new ArrayList<>(peers);
        this.blockchain = currentBlockchain;
        this.latestBlock = currentBlockchain.getLatestBlock();
        this.threadPool = Executors.newFixedThreadPool(10);
        this.messageDeque = new LinkedBlockingDeque<Message>();
    }

    public void start() {

        threadPool.execute(this::listenForConnections);
        startMiningExec();
    }

    public void stop() {
        miningExecutor.shutdown();
        threadPool.shutdownNow();
    }

    protected Block blockMiningInNode(Blockchain blockchain) {
        Block block = blockchain.getLatestBlock();
        while (!blockchain.hashCheck(block)) {
            if (miningExecutor.isShutdown()) {
                System.out.println(" mining process stopped, executor is shutdown " +
                        getPort() + "\n" +
                        "---------------------------------------");
                return null;
            }
            if (!blockchain.getLatestBlock().equals(block)) {
                System.out.println("we received new block, need to stop " +
                        getPort() + "\n" +
                        "---------------------------------------");
                return null;
            }

            block.setNonce(block.getNonce() + blockchain.getMiningStrategy().nextNonce(block.getNonce()));
            block.setHash(block.hashCalculate());
        }
        return block;
    }

    private void startMiningExec() {
        miningExecutor = Executors.newSingleThreadExecutor();
        Runnable runnable = () -> {
            System.out.println("Mining Started " +
                    getPort() + "\n" +
                    "---------------------------------------");
            System.out.println("Blockchain size in port " + getPort()
                    + " is " + getBlockchain().getBlocks().size() + "\n" +
                    "---------------------------------------");
            while (!(miningExecutor.isShutdown())) {
                Block block = blockMiningInNode(blockchain);
                if (block == null) {
                    System.out.println("stopped mining PROCESS port " +
                            getPort() + "\n" +
                            "---------------------------------------");
                    break;
                } else {
                    System.out.println("Port " + getPort() + " mined block with index " + block.getIndex() + "\n" +
                            "Index: " + block.getIndex() + "\n" +
                            "prevHash: " + block.getPrevHash() + "\n" +
                            "hash: " + block.getHash() + "\n" +
                            "data: " + block.getData() + "\n" +
                            "nonce: " + block.getNonce() + "\n" +
                            "---------------------------------------");
                    if (block.getIndex() == getBlockchain().getBlocks().size() - 2) {
                        System.out.println("im to late..." + "\n" +
                                "---------------------------------------");
                        break;
                    } else {

                        Block newBlock = blockchain.createNewBlock(block, 1);
                        Block forMessageBlock = new Block(block.getIndex(),
                                block.getPrevHash(), block.getData(), block.getHash(), block.getNonce());
                        message = new Message(MessageType.SEND_BLOCKCHAIN);
                        message.setPort(getPort());
                        message.setBlock(forMessageBlock);

                        for (int i = 0; i <= peers.size() - 1; i++) {
                            Integer currentPeer = peers.get(i);
                            if (peers.get(i) != getPort()) {
                                threadPool.submit(() ->
                                        messageBroadcast(currentPeer, getPort(), message));
                            }
                        }
                        blockchain.addLastBlock(newBlock);
                        System.out.println("Broadcast New block and add it to Blockchain list with size " + blockchain.getBlocks().size() + "\n" +
                                "---------------------------------------");
                        latestBlock = blockchain.getLatestBlock();
                    }
                }
            }
            System.out.println("stopped mining port " +
                    getPort() + "\n" +
                    "---------------------------------------");
        };
        System.out.println("restarting thread in port " +
                getPort() + "\n" +
                "---------------------------------------");

        miningExecutor.execute(runnable);
    }


    private void listenForConnections() {

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            Runtime.getRuntime().addShutdownHook(new Thread(threadPool::shutdown));
            Runtime.getRuntime().addShutdownHook(new Thread(miningExecutor::shutdown));
            while (!Thread.currentThread().isInterrupted()) {
                socket = serverSocket.accept();
                threadPool.submit(() -> handleConnection(socket));

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void handleConnection(Socket socket) {
        try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            message = (Message) in.readObject();
            MessageType messageType = message.getType();
            System.out.println("Connected to " + socket + "\n" +
                    "---------------------------------------");
            switch (messageType) {

                case SEND_BLOCKCHAIN -> {
                    System.out.println("In " + port + " block received");
                    System.out.println(message.getBlock().getIndex() + 1);
                    System.out.println(blockchain.getLatestBlock().getIndex());

                    if (message.getBlock().getIndex() + 1 == blockchain.getLatestBlock().getIndex()) {
                        System.out.println("Check POW");
                        System.out.println(message.getBlock().getNonce());
                        System.out.println(blockchain.getBlocks().get(blockchain.getBlocks().size() - 2).getNonce());
                        if (message.getBlock().getNonce() > blockchain.getBlocks().get(blockchain.getBlocks().size() - 2).getNonce()) {
                            System.out.println("POW in received block is better, need to recreate");
                            blockchain.getBlocks().remove(blockchain.getBlocks().size() - 1);
                            blockchain.replaceLastBlock(message.getBlock());

                            for (int i = 0; i <= peers.size() - 1; i++) {
                                Integer currentPeer = peers.get(i);
                                if ((peers.get(i) != getPort()) && (!Objects.equals(peers.get(i), message.getPort()))) {
                                    threadPool.submit(() ->
                                            messageBroadcast(currentPeer, getPort(), message));
                                }
                            }
                            Block newBlock = blockchain.createNewBlock(message.getBlock(), message.getBlock().getNonce());
                            blockchain.addLastBlock(newBlock);
                            System.out.println("Received block has index " + message.getBlock().getIndex() + "\n" +
                                    "Created new block with index " + newBlock.getIndex() + "\n" +
                                    "adding block to list in port " + getPort() + "\n" +
                                    "now blockchain size is " + blockchain.getBlocks().size() + "\n" +
                                    "---------------------------------------");
                            latestBlock = blockchain.getLatestBlock();
                            in.close();
                            miningExecutor.shutdownNow();
                            System.out.println("Trying to interrupt " + getPort() + "  " + miningExecutor.isShutdown() + "\n" +
                                    "---------------------------------------");

                            startMiningExec();
                        } else {
                            System.out.println("Our block is better then received" + "\n" +
                                    "---------------------------------------");
                        }
                    } else {
                        blockchain.replaceLastBlock(message.getBlock());
                        Block newBlock = blockchain.createNewBlock(message.getBlock(), message.getBlock().getNonce());
                        blockchain.addLastBlock(newBlock);
                        System.out.println("Received block has index " + message.getBlock().getIndex() + "\n" +
                                "Created new block with index " + newBlock.getIndex() + "\n" +
                                "adding block to list in port " + getPort() + "\n" +
                                "now blockchain size is " + blockchain.getBlocks().size() + "\n" +
                                "---------------------------------------");
                        latestBlock = blockchain.getLatestBlock();
                        in.close();
                        miningExecutor.shutdownNow();
                        System.out.println("Trying to interrupt " + getPort() + "  " + miningExecutor.isShutdown() + "\n" +
                                "---------------------------------------");

                        startMiningExec();
                    }

                }
                case SEND_PORT -> {
                    if (!peers.contains(message.getPort())) {
                        peers.add(message.getPort());
                        System.out.println(getPort() + " add port " + message.getPort() + "\n" +
                                "---------------------------------------");
                    }
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void messageBroadcast(Integer destinationPort, Integer currentPort, Message message) {
        if (!Objects.equals(destinationPort, currentPort)) {
            try (Socket socket = new Socket("localhost", destinationPort);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
                System.out.println("My port is " + getPort() + "\n" +
                        "Trying to send BLOCK to " + socket + "\n" +
                        "---------------------------------------");
                out.writeObject(message);
            } catch (IOException e) {
                System.out.println("Failed to send block to peer " + destinationPort);
            }
        }
    }

    public void broadcastPort() {
        List<Integer> listForIteration = new ArrayList<>(peers);
        for (Integer destinationPort : listForIteration) {
            if (destinationPort != port) {
                try (Socket socket = new Socket("localhost", destinationPort);
                     ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
                    System.out.println("My port is " + getPort() + "\n" +
                            "Trying to send port to " + socket + "\n" +
                            "---------------------------------------");
                    Message messageToSend = new Message(MessageType.SEND_PORT);
                    messageToSend.setPort(getPort());
                    out.writeObject(messageToSend);
                    Thread.sleep(200);
                } catch (IOException e) {
                    System.err.println("Failed to send PORT to peer " + destinationPort);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public Blockchain getBlockchain() {
        return blockchain;
    }


    public int getPort() {
        return port;
    }

}