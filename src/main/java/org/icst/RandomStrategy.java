package org.icst;

import java.util.Random;

public class RandomStrategy implements MiningStrategy {
    private final Random random;

    public RandomStrategy() {
        this.random = new Random();
    }

    @Override
    public long nextNonce(long currentNonce) {
        return random.nextInt(Integer.MAX_VALUE);
    }
}