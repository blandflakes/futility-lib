package org.machinery.futility.analysis.structs;

import java.io.Serializable;

public final class GeneFeatureMeasurements implements Serializable
{
    private static final long serialVersionUID = 8720504952949642156L;
    private final String condition;
    private final int numTASites;
    private final int geneLength;
    private final double numControlReads;
    private final double numExperimentReads;
    private final double modifiedRatio;
    private final double p;
    private final double essentialityIndex;
    private final double fitness;


    public GeneFeatureMeasurements(final String condition, final int numTASites, final int geneLength,
                                   final double numControlReads, final double numExperimentReads,
                                   final double modifiedRatio, final double p,  final double essentialityIndex,
                                   final double fitness)
    {
        this.condition = condition;
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
    public String getCondition()
    {
        return condition;
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
    public double getNumControlReads()
    {
        return numControlReads;
    }

    @SuppressWarnings("unused")
    public double getNumExperimentReads()
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

    /**
     * Created because feature stuff gets updated a fair amount. We want to keep features immutable, but allow this
     * stuff to be changed while we're calculating it.
     */
    public static final class Builder
    {
        private String condition;
        private int numTASites;
        private int geneLength;
        private double numControlReads;
        private double numExperimentReads;
        private double modifiedRatio;
        private double p;
        private double essentialityIndex;
        private double fitness;

        public String getCondition()
        {
            return condition;
        }

        public Builder withCondition(final String condition)
        {
            this.condition = condition;
            return this;
        }

        public int getNumTASites()
        {
            return numTASites;
        }

        public Builder withNumTASites(int numTASites)
        {
            this.numTASites = numTASites;
            return this;
        }

        public int getGeneLength()
        {
            return geneLength;
        }

        public Builder withGeneLength(int geneLength)
        {
            this.geneLength = geneLength;
            return this;
        }

        public double getNumControlReads()
        {
            return numControlReads;
        }

        public Builder withNumControlReads(double numControlReads)
        {
            this.numControlReads = numControlReads;
            return this;
        }

        public double getNumExperimentReads()
        {
            return numExperimentReads;
        }

        public Builder withNumExperimentReads(double numExperimentReads)
        {
            this.numExperimentReads = numExperimentReads;
            return this;
        }

        public double getModifiedRatio()
        {
            return modifiedRatio;
        }

        public Builder withModifiedRatio(double modifiedRatio)
        {
            this.modifiedRatio = modifiedRatio;
            return this;
        }

        public double getP()
        {
            return p;
        }

        public Builder withP(double p)
        {
            this.p = p;
            return this;
        }

        public double getEssentialityIndex()
        {
            return essentialityIndex;
        }

        public Builder withEssentialityIndex(double essentialityIndex)
        {
            this.essentialityIndex = essentialityIndex;
            return this;
        }

        public double getFitness()
        {
            return fitness;
        }

        public Builder withFitness(double fitness)
        {
            this.fitness = fitness;
            return this;
        }

        public GeneFeatureMeasurements build()
        {
            return new GeneFeatureMeasurements(condition, numTASites, geneLength, numControlReads, numExperimentReads,
                    modifiedRatio, p, essentialityIndex, fitness);
        }
    }
}
