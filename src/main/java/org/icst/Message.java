package org.icst;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class Message implements Serializable {
    private  MessageType type;

    private Blockchain blockchain;

    private List<Integer> peers;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    private String address;
    private Integer port;


    private  Block block;

    public Message(MessageType type) {
        this.type = type;
    }


    public MessageType getType() {
        return type;
    }


    public void setBlock(Block block) {
        this.block = block;
    }

    public Blockchain getBlockchain() {
        return blockchain;
    }

    public List<Integer> getPeers() {
        return peers;
    }

    public Block getBlock() {
        return block;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }
}