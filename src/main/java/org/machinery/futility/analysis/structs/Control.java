package org.machinery.futility.analysis.structs;

public final class Control
{
    private final String name;
    private final String genomeName;
    private final SequenceMeasurements sequenceMeasurements;

    public Control(final String name, final String genomeName, final SequenceMeasurements sequenceMeasurements)
    {
        this.name = name;
        this.genomeName = genomeName;
        this.sequenceMeasurements = sequenceMeasurements;
    }

    public String getName()
    {
        return name;
    }

    public String getGenomeName()
    {
        return genomeName;
    }

    public SequenceMeasurements getSequenceMeasurements()
    {
        return sequenceMeasurements;
    }
}
