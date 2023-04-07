package org.icst;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Block {

    private long index;
    private String prevHash;
    private String hash;
    private String data;
    private long nonce;

    public Block(long index, String prevHash, String data, long nonce) {
        this.index = index;
        this.prevHash = prevHash;
        this.data = data;
        this.nonce = nonce;
    }

    public String getCurrentState() {

        return getIndex() + getPrevHash() + getData() + getNonce();

    }

    public String hashCalculate() {
        MessageDigest messageDigest;
        String resultHash;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hash = messageDigest.digest(getCurrentState().getBytes());

            resultHash = IntStream.range(0, hash.length)
                    .mapToObj(i -> String.format("%02x", hash[i]))
                    .collect(Collectors.joining());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return resultHash;
    }


    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public String getPrevHash() {
        return prevHash;
    }

    public void setPrevHash(String prevHash) {
        this.prevHash = prevHash;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public long getNonce() {
        return nonce;
    }

    public void setNonce(long nonce) {
        this.nonce = nonce;
    }
}
