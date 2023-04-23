package org.icst;

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

    public String getAddress() {
        return address;
    }

    private final String address;

    private final Blockchain blockchain;


    private Block latestBlock;
    private final List<Integer> peers;
    private final List<String> addresses;
    private final ExecutorService threadPool;
    private ExecutorService miningExecutor;
    private BlockingDeque<Message> messageDeque;
    Socket socket;
    private Message message = null;

    public Node(String address, int port, int initPort) {
        this.address = address;
        this.port = port;

        List<Integer> currentPeers = new ArrayList<>();
        List<String> currentAddresses = new ArrayList<>();
        Blockchain currentBlockchain = new Blockchain(new ArrayList<>(), port);

        currentPeers.add(port);
        currentAddresses.add(address);

        while (port > initPort) {
            int numb = port - initPort;
            String addr = Objects.equals(address, "localhost") ? address : "node" + numb;
            currentAddresses.add(addr);
            port -= 1;
            currentPeers.add(port);
        }

        this.peers = currentPeers;
        this.addresses = currentAddresses;
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
                    port + "\n" +
                    "---------------------------------------");
            System.out.println("Blockchain size in port " + port
                    + " is " + blockchain.getBlocks().size() + "\n" +
                    "---------------------------------------");
            while (!(miningExecutor.isShutdown())) {
                Block block = blockMiningInNode(blockchain);
                if (block == null) {
                    System.out.println("stopped mining PROCESS port " +
                            port + "\n" +
                            "---------------------------------------");
                    miningExecutor.shutdownNow();
                    break;
                } else {
                    System.out.println("Port " + port + " mined block with index " + block.getIndex() + "\n" +
                            "Index: " + block.getIndex() + "\n" +
                            "prevHash: " + block.getPrevHash() + "\n" +
                            "hash: " + block.getHash() + "\n" +
                            "data: " + block.getData() + "\n" +
                            "nonce: " + block.getNonce() + "\n" +
                            "---------------------------------------");
                    if (block.getIndex() == blockchain.getBlocks().size() - 2) {
                        System.out.println("im to late..." + "\n" +
                                "---------------------------------------");
                        break;
                    } else {

                        Block newBlock = blockchain.createNewBlock(block);
                        Block forMessageBlock = new Block(block.getIndex(),
                                block.getPrevHash(), block.getData(), block.getHash(), block.getNonce());
                        if (message == null){

                        }

                        message = new Message(MessageType.SEND_BLOCKCHAIN);
                        message.setPort(getPort());
                        message.setBlock(forMessageBlock);

                        blockchain.addLastBlock(newBlock);
                        for (int i = 0; i <= peers.size() - 1; i++) {
                            Integer currentPeer = peers.get(i);
                            String currentAddress = addresses.get(i);
                            if (peers.get(i) != port) {

                                threadPool.submit(() ->
                                        messageBroadcast(currentAddress, currentPeer, port, message));
                            }
                        }
                        System.out.println("Broadcast New block and add it to Blockchain list with size " + blockchain.getBlocks().size() + "\n" +
                                "---------------------------------------");
                        //
                        latestBlock = blockchain.getLatestBlock();
                    }
                }
            }
            System.out.println("stopped mining port " +
                    port + "\n" +
                    "---------------------------------------");
        };
        System.out.println("restarting thread in port " +
                port + "\n" +
                "---------------------------------------");
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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

                    //List<Block> blocks = blockchain.getBlocks();

                    if (message.getBlock().getIndex() + 1 == blockchain.getLatestBlock().getIndex()) {

                        System.out.println("Check POW");
                        System.out.println(message.getBlock().getNonce());
                        System.out.println(blockchain.getBlocks().get(blockchain.getBlocks().size() - 2).getNonce());

                        if (message.getBlock().getNonce() > blockchain.getBlocks().get(blockchain.getBlocks().size() - 2).getNonce()) {
                            System.out.println("POW in received block is better, need to recreate");
                            miningExecutor.shutdownNow();
                            System.out.println("Trying to interrupt " + getPort() + "  " + miningExecutor.isShutdown() + "\n" +
                                    "---------------------------------------");
                            blockchain.getBlocks().remove(blockchain.getBlocks().size() - 1);
                            blockchain.replaceLastBlock(message.getBlock());
                            Block newBlock = blockchain.createNewBlock(message.getBlock());
                            blockchain.addLastBlock(newBlock);
                            System.out.println("Received block has index " + message.getBlock().getIndex() + "\n" +
                                    "Created new block with index " + newBlock.getIndex() + "\n" +
                                    "adding block to list in port " + getPort() + "\n" +
                                    "now blockchain size is " + blockchain.getBlocks().size() + "\n" +
                                    "---------------------------------------");

                            /*if (blockchain.getBlocks().size() - 1 > blockchain.getLatestBlock().getIndex() + 1)
                                System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");

                             */
                            latestBlock = blockchain.getLatestBlock();
                            for (int i = 0; i <= peers.size() - 1; i++) {
                                Integer currentPeer = peers.get(i);
                                String currentAddress = addresses.get(i);
                                if ((peers.get(i) != getPort()) && (!Objects.equals(peers.get(i), message.getPort()))) {
                                    threadPool.submit(() ->
                                            messageBroadcast(currentAddress, currentPeer, getPort(), message));
                                }
                            }
                            in.close();


                            startMiningExec();
                        } else {
                            System.out.println("Our block is better then received" + "\n" +
                                    "---------------------------------------");
                        }
                    } else {
                        blockchain.replaceLastBlock(message.getBlock());
                        Block newBlock = blockchain.createNewBlock(message.getBlock());
                        blockchain.addLastBlock(newBlock);
                        miningExecutor.shutdownNow();
                        System.out.println("Trying to interrupt " + getPort() + "  " + miningExecutor.isShutdown() + "\n" +
                                "---------------------------------------");
                        System.out.println("Received block has index " + message.getBlock().getIndex() + "\n" +
                                "Created new block with index " + newBlock.getIndex() + "\n" +
                                "adding block to list in port " + getPort() + "\n" +
                                "now blockchain size is " + blockchain.getBlocks().size() + "\n" +
                                "---------------------------------------");
                        latestBlock = blockchain.getLatestBlock();
                        in.close();
                        startMiningExec();
                    }

                }
                case SEND_PORT -> {
                    if (!peers.contains(message.getPort())) {
                        peers.add(message.getPort());
                        addresses.add(message.getAddress());
                        System.out.println(getPort() + " add port " + message.getPort() + "\n" +
                                "---------------------------------------");
                    }
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void messageBroadcast(String address, Integer destinationPort, Integer currentPort, Message message) {
        if (!Objects.equals(destinationPort, currentPort)) {

            try (Socket socket = new Socket(address, destinationPort);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
                System.out.println("My port is " + port + "\n" +
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
        List<String> listForAddresses = new ArrayList<>(addresses);
        for (int i = 0; i < listForIteration.size(); i++) {
            if (listForIteration.get(i) != port) {
                try (Socket socket = new Socket(listForAddresses.get(i), listForIteration.get(i));
                     ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
                    System.out.println("My port is " + port + "\n" +
                            "Trying to send port to " + socket + "\n" +
                            "---------------------------------------");
                    Message messageToSend = new Message(MessageType.SEND_PORT);
                    messageToSend.setPort(port);
                    messageToSend.setAddress(getAddress());
                    out.writeObject(messageToSend);
                    Thread.sleep(200);
                } catch (IOException e) {
                    System.err.println("Failed to send PORT to peer " + listForIteration.get(i));
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