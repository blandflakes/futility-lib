package org.machinery.futility.analysis.structs;

public final class GeneFeatureMeasurements
{
    private final int numTASites;
    private final int geneLength;
    private final int numControlReads;
    private final int numExperimentReads;
    private final double modifiedRatio;
    private final double p;
    private final double essentialityIndex;
    private final double fitness;

    public GeneFeatureMeasurements(final int numTASites, final int geneLength, final int numControlReads,
                                   final int numExperimentReads, final double modifiedRatio, final double p,
                                   final double essentialityIndex, final double fitness)
    {
        this.numTASites = numTASites;
        this.geneLength = geneLength;
        this.numControlReads = numControlReads;
        this.numExperimentReads = numExperimentReads;
        this.modifiedRatio = modifiedRatio;
        this.p = p;
        this.essentialityIndex = essentialityIndex;
        this.fitness = fitness;
    }

    @SuppressWarnings("unused")
    public int getNumTASites()
    {
        return numTASites;
    }

    @SuppressWarnings("unused")
    public int getGeneLength()
    {
        return geneLength;
    }

    @SuppressWarnings("unused")
    public int getNumControlReads()
    {
        return numControlReads;
    }

    @SuppressWarnings("unused")
    public int getNumExperimentReads()
    {
        return numExperimentReads;
    }

    @SuppressWarnings("unused")
    public double getModifiedRatio()
    {
        return modifiedRatio;
    }

    @SuppressWarnings("unused")
    public double getP()
    {
        return p;
    }

    @SuppressWarnings("unused")
    public double getEssentialityIndex()
    {
        return essentialityIndex;
    }

    @SuppressWarnings("unused")
    public double getFitness()
    {
        return fitness;
    }
}
