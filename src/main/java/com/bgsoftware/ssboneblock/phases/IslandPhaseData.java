package com.bgsoftware.ssboneblock.phases;

public final class IslandPhaseData {

    private final int phaseLevel;
    private final int phaseBlock;

    public IslandPhaseData(int phaseLevel, int phaseBlock) {
        this.phaseLevel = phaseLevel;
        this.phaseBlock = phaseBlock;
    }

    public int getPhaseLevel() {
        return phaseLevel;
    }

    public int getPhaseBlock() {
        return phaseBlock;
    }

    public IslandPhaseData nextBlock() {
        return new IslandPhaseData(phaseLevel, phaseBlock + 1);
    }

    public IslandPhaseData nextPhase() {
        return new IslandPhaseData(phaseLevel + 1, 0);
    }

}
