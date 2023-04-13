package org.icst;

import java.util.Random;

public class IncrementStrategy implements MiningStrategy {



    @Override
    public long nextNonce(long currentNonce) {
        return ++currentNonce;
    }
}