package org.icst;

public enum GenesisBlockData {

    INSTANCE(0, "", "This is the genesis block", "b10c677d4a7743974c3fca6df6ea728db49f6933fc056d72698a925d637ade57", 0);

    private final long index;
    private final String previousHash;
    private final String data;
    private final String hash;
    private final long nonce;

    GenesisBlockData(int index, String previousHash, String data, String hash, long nonce) {
        this.index = index;
        this.previousHash = previousHash;
        this.data = data;
        this.hash = hash;
        this.nonce = nonce;
    }

    public long getIndex() {
        return index;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public String getData() {
        return data;
    }

    public long getNonce() {
        return nonce;
    }

    public String getHash() {
        return hash;
    }
}
