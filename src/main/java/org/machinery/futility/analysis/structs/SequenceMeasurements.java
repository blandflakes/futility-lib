package org.machinery.futility.analysis.structs;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Represents an analyzed IGV file. We retain the original IGV data , which we need for controls when analyzing
 * experiments. We also have an index, which is used to quickly look up mappings relative to a position in the entire
 * genome.
 */
public final class SequenceMeasurements implements Serializable
{
    private static final long serialVersionUID = -6934551457216078346L;
    private final List<IgvRecord> rawData;
    private final Map<Integer, Integer> index;
    private final Map<String, Integer> stats;

    public SequenceMeasurements(final List<IgvRecord> rawData, final Map<Integer, Integer> index, final Map<String, Integer> stats)
    {
        this.rawData = rawData;
        this.index = index;
        this.stats = stats;
    }

    @SuppressWarnings("unused")
    public List<IgvRecord> getRawData()
    {
        return rawData;
    }

    @SuppressWarnings("unused")
    public Map<Integer, Integer> getIndex() {
        return index;
    }

    @SuppressWarnings("unused")
    public Map<String, Integer> getStats()
    {
        return stats;
    }
}
