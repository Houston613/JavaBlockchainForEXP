package org.icst;

import org.junit.jupiter.api.Test;


import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class NodeTest {


    @Test
    void nodeCreationTest() {
        Node firstNode = new Node("localhost",8080,8080);
        List<Block> blocks = firstNode.getBlockchain().getBlocks();
        assertFalse(blocks.isEmpty());
    }

    @Test
    void nodeStopTest() {
        Node firstNode = new Node("localhost",8080,8080);
        firstNode.start();

        firstNode.stop();
    }


    @Test
    void nodeIntegrationTest() {
        Node firstNode = new Node("localhost",8080,8080);
        firstNode.start();

        Node secondNode = new Node("localhost",8081,8080);
        secondNode.start();
        secondNode.broadcastPort();

        Node thirdNode = new Node("localhost",8082,8080);;
        thirdNode.start();
        thirdNode.broadcastPort();
        try {

            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        firstNode.stop();
        secondNode.stop();
        thirdNode.stop();

        System.out.println(firstNode.getBlockchain().getBlocks().size());

        System.out.println(" first node hashes " + "\n" +
                "---------------------------------------");
        for (int i = 0; i <= firstNode.getBlockchain().getBlocks().size()-1; i++) {
            System.out.println(i + ")" + firstNode.getBlockchain().getBlocks().get(i).getHash() + "\n");

        }
        System.out.println(secondNode.getBlockchain().getBlocks().size());
        System.out.println(" second node hashes " + "\n" +
                "---------------------------------------");
        for (int i = 0; i <= secondNode.getBlockchain().getBlocks().size()-1; i++) {
            System.out.println(i + ")" + secondNode.getBlockchain().getBlocks().get(i).getHash() + "\n");

        }

        System.out.println(thirdNode.getBlockchain().getBlocks().size());
        System.out.println(" second node hashes " + "\n" +
                "---------------------------------------");
        for (int i = 0; i <= thirdNode.getBlockchain().getBlocks().size()-1; i++) {
            System.out.println(i + ")" + thirdNode.getBlockchain().getBlocks().get(i).getHash() + "\n");
        }

        try {

            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("stopped all!!!" + "\n" +
                "---------------------------------------");
        assertEquals(firstNode.getBlockchain().getBlocks(),(firstNode.getBlockchain().getBlocks()));


        assertEquals(firstNode.getBlockchain().getBlocks().size(), secondNode.getBlockchain().getBlocks().size());
        assertEquals(secondNode.getBlockchain().getBlocks().size(), thirdNode.getBlockchain().getBlocks().size());

        for (int i = 0; i < firstNode.getBlockchain().getBlocks().size(); i++) {
            assertEquals(firstNode.getBlockchain().getBlocks().get(i), firstNode.getBlockchain().getBlocks().get(i));
            assertEquals(firstNode.getBlockchain().getBlocks().get(i), firstNode.getBlockchain().getBlocks().get(i));
        }
    }
}
