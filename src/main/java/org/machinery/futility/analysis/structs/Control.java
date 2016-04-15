package org.machinery.futility.analysis.structs;

public final class Control
{
    private final String name;
    private final String genomeName;
    private final SequenceMeasurements sequenceMeasurements;
    private final String type = "control";

    public Control(final String name, final String genomeName, final SequenceMeasurements sequenceMeasurements)
    {
        this.name = name;
        this.genomeName = genomeName;
        this.sequenceMeasurements = sequenceMeasurements;
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
    public SequenceMeasurements getSequenceMeasurements()
    {
        return sequenceMeasurements;
    }

    @SuppressWarnings("unused")
    public String getType()
    {
        return type;
    }
}
