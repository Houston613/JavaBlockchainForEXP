package org.icst;

public class Main {
    public static void main(String[] args) {

        System.out.println("Hello World");

        String address = "localhost";
        int port = Integer.parseInt(args[0]);
        int initialPort = 8080;

        Node node = new Node(address, port, initialPort);
        node.start();
        node.broadcastPort();
    }


}