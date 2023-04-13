package org.icst;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class NodeTest {


    @Test
    void nodeCreationTest() {
        Node firstNode = new Node(8080);
        List<Block> blocks = firstNode.getBlockchain().getBlocks();
        assertFalse(blocks.isEmpty());
    }

    @Test
    void nodeGettingDataTest() {
        Node firstNode = new Node(8080);
        firstNode.start();
        try (Socket socket = new Socket("localhost", 8080);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            Message request = new Message(MessageType.REQUEST_BLOCKCHAIN);
            out.writeObject(request);
            Message response = (Message) in.readObject();
            Blockchain blockchain = response.getBlockchain();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        try (Socket socket = new Socket("localhost", 8080);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            Message request = new Message(MessageType.REQUEST_PEERS);
            out.writeObject(request);
            Message response = (Message) in.readObject();
            List<Integer> nodes = response.getPeers();

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }


    }


    @Test
    void nodeIntegrationTest() {
        Node firstNode = new Node(8080);
        firstNode.start();

        Node secondNode = new Node(8081);
        secondNode.start();
        secondNode.broadcastPort();

        Node thirdNode = new Node(8082);
        thirdNode.start();
        thirdNode.broadcastPort();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println(firstNode.getPeers().size());

    }
}
