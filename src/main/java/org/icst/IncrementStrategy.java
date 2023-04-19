package org.icst;


public class IncrementStrategy implements MiningStrategy {



    @Override
    public long nextNonce(long currentNonce) {
        return 1;
    }
}