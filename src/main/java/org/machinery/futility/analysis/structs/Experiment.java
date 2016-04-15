package org.machinery.futility.analysis.structs;

import java.util.Map;

public final class Experiment
{
    private final String name;
    private final String genomeName;
    private final String controlName;
    private final SequenceMeasurements sequenceMeasurements;
    private final Map<String, GeneFeatureMeasurements> geneFeatureMeasurements;
    private final String type = "experiment";

    public Experiment(final String name, final String genomeName, final String controlName,
                      final SequenceMeasurements sequenceMeasurements,
                      final Map<String, GeneFeatureMeasurements> geneFeatureMeasurements)
    {
        this.name = name;
        this.genomeName = genomeName;
        this.controlName = controlName;
        this.sequenceMeasurements = sequenceMeasurements;
        this.geneFeatureMeasurements = geneFeatureMeasurements;
    }

    @SuppressWarnings("unused")
    public String getName()
    {
        return name;
    }

    @SuppressWarnings("unused")
    public String getGenomeName()
    {
        return genomeName;
    }

    @SuppressWarnings("unused")
    public String getControlName()
    {
        return controlName;
    }

    @SuppressWarnings("unused")
    public SequenceMeasurements getSequenceMeasurements()
    {
        return sequenceMeasurements;
    }

    @SuppressWarnings("unused")
    public Map<String, GeneFeatureMeasurements> getGeneFeatureMeasurements()
    {
        return geneFeatureMeasurements;
    }

    @SuppressWarnings("unused")
    public String getType()
    {
        return type;
    }
}
