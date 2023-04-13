package org.icst;

public class FibonacciStrategy implements MiningStrategy {
    private int prev;
    private int curr;

    public FibonacciStrategy() {
        this.prev = 0;
        this.curr = 1;
    }

    @Override
    public long nextNonce(long currentNonce) {
        int next = prev + curr;
        prev = curr;
        curr = next;
        return next;
    }
}
