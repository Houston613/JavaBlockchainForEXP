package org.icst;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BlockTest {

    @Test
    void currentStateTest() {
        long index = 0L;
        String prevHash = "00000000000000000000000000000000";
        String data = "data";
        long nonce = 0L;
        Block block = new Block(index, prevHash, data, nonce);
        assertEquals(block.getCurrentState(), "000000000000000000000000000000000data0");
    }
    @Test
    void currentStateWithEmptyData() {
        long index = 0L;
        String prevHash = "00000000000000000000000000000000";
        String data = "data";
        long nonce = 0L;
        Block block = new Block(index, prevHash, data, nonce);
        assertEquals(block.getCurrentState(), "000000000000000000000000000000000data0");
    }
    @Test
    void hashTest() {
        long index = 0L;
        String prevHash = "00000000000000000000000000000000";
        String data = "data";
        long nonce = 0L;
        Block block = new Block(index, prevHash, data, nonce);
        assertEquals(block.getCurrentState(), "000000000000000000000000000000000data0");
        String expectedHash = "1bab61677c31715b9b9415a8f2016402f07202f4de8e3d91e3bc4d4528ba1fc2";
        assertEquals(block.hashCalculate(),expectedHash);
    }
}