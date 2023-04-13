package org.icst;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class BlockMiningTest {

    @Test
    void chainStateTest() {
        Blockchain blockchain = new Blockchain(new ArrayList<>(), 8080);
        Block block = blockchain.genesisBlockGeneration();
        assertFalse(blockchain.getBlocks().isEmpty());
        assertEquals(1, blockchain.getBlocks().size());
    }

    @Test
    void hashCheckTest() {
        Blockchain blockchain = new Blockchain(new ArrayList<>(), 8080);
        Block block = blockchain.genesisBlockGeneration();
        assertFalse(blockchain.hashCheck(block));
    }

    @Test
    void mineTest() {
        Blockchain blockchain = new Blockchain(new ArrayList<>(), 8080);
        long seed = 123L;
        Block block = blockchain.blockMining(blockchain.genesisBlockGeneration());
        assertEquals("d5fed2033d95b7e951b4a69b57a93cd297b53a157da4cc45851875c7a7b90000", block.getHash());
    }


    @Test
    void newBlockTest() {
        Blockchain blockchain = new Blockchain(new ArrayList<>(), 8080);
        long seed = 123L;
        Block block = blockchain.genesisBlockGeneration();
        Block newBlock = blockchain.createNewBlock(block, seed);
        assertEquals(1, newBlock.getIndex());
    }

    @Test
    void newBlockFromMiningTest() {
        Blockchain blockchain = new Blockchain(new ArrayList<>(), 8080);
        long seed = 123L;
        Block block = blockchain.genesisBlockGeneration();
        Block newBlock = blockchain.createNewBlock(blockchain.blockMining(block), seed);
        assertEquals(1, newBlock.getIndex());
    }

}