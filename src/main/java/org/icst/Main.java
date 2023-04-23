package org.icst;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Main {
    public static void main(String[] args) throws UnknownHostException {

        System.out.println("Hello World");

        InetAddress address = InetAddress.getLocalHost();
        String hostname = address.getHostName();
        int port = Integer.parseInt(args[0]);
        int initialPort = 8080;
        System.out.println(hostname);
        Node node = new Node(hostname, port, initialPort);
        node.start();
        node.broadcastPort();
    }


}