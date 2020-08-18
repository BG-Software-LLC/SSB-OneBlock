package com.bgsoftware.ssboneblock.phases;

public final class IslandPhaseData {

    private int phaseLevel;
    private int phaseBlock;

    public IslandPhaseData(int phaseLevel, int phaseBlock){
        this.phaseLevel = phaseLevel;
        this.phaseBlock = phaseBlock;
    }

    public int getPhaseLevel() {
        return phaseLevel;
    }

    public int getPhaseBlock() {
        return phaseBlock;
    }

    public void setPhaseLevel(int phaseLevel) {
        this.phaseLevel = phaseLevel;
    }

    public void setPhaseBlock(int phaseBlock) {
        this.phaseBlock = phaseBlock;
    }
}
