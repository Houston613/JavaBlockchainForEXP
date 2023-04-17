package org.icst;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Blockchain implements Serializable {


    GenesisBlockData genesisBlockData = GenesisBlockData.INSTANCE;

    private final List<Block> blocks;

    public MiningStrategy getMiningStrategy() {
        return miningStrategy;
    }

    private final MiningStrategy miningStrategy;


    public Blockchain(List<Block> chain, Integer port) {
        if (chain.isEmpty()) {
            chain.add(genesisBlockGeneration());
            this.blocks = chain;
        } else {
            this.blocks = new ArrayList<>();
        }
        this.miningStrategy = chooseMeaningStrategy(port);
    }

    private MiningStrategy chooseMeaningStrategy(Integer port) {
        if (port == 8080)
            return new IncrementStrategy();
        else if (port == 8081)
            return new RandomStrategy();
        else return new FibonacciStrategy();
    }


    protected Block genesisBlockGeneration() {
        return new Block(genesisBlockData.getIndex(), genesisBlockData.getPreviousHash(),
                genesisBlockData.getData(), genesisBlockData.getHash(), genesisBlockData.getNonce());
    }

    protected Block blockMining(Block block) {
        while (!hashCheck(block)) {
            block.setNonce(block.getNonce() + miningStrategy.nextNonce(block.getNonce()));
            block.setHash(block.hashCalculate());
        }
        return block;
    }

    protected boolean hashCheck(Block block) {
        return block.getHash().endsWith("0000");
    }

    protected Block createNewBlock(Block block, long seed) {

        Block newBlock = new Block(block.getIndex() + 1, block.getHash(), dataGenerator(seed), 0);
        newBlock.setHash(newBlock.hashCalculate());
        return newBlock;
    }

    private String dataGenerator(long seed) {
        int leftLimit = 97;
        int rightLimit = 122;
        int targetStringLength = 256;

        Random random = new Random();
        random.setSeed(seed);
        return random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }


    public List<Block> getBlocks() {
        return blocks;
    }

    public void addLastBlock(Block block){
        blocks.add(block);
    }

    public void replaceLastBlock(Block block){
        getBlocks().remove(getBlocks().size() - 1);
        addLastBlock(block);
    }

    public Block getLatestBlock() {
        return getBlocks().get(getBlocks().size() - 1);
    }

}
