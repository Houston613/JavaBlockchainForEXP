package org.icst;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Block implements Serializable {

    private final long index;
    private final String prevHash;
    private String hash;
    private final String data;
    private long nonce;

    public Block(long index, String prevHash, String data, long nonce) {
        this.index = index;
        this.prevHash = prevHash;
        this.data = data;
        this.nonce = nonce;
    }


    public Block(long index, String prevHash, String data, String hash, long nonce) {
        this.index = index;
        this.prevHash = prevHash;
        this.data = data;
        this.hash = hash;
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

    public String getPrevHash() {
        return prevHash;
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


    public long getNonce() {
        return nonce;
    }

    public void setNonce(long nonce) {
        this.nonce = nonce;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Block block = (Block) o;
        return index == block.index && nonce == block.nonce && Objects.equals(prevHash, block.prevHash) && Objects.equals(hash, block.hash) && Objects.equals(data, block.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, prevHash, hash, data, nonce);
    }
}
