package org.machinery.futility.analysis.structs;

import java.io.Serializable;
import java.util.Map;

/**
 * We use a Genome for two things - for analysis of experiments (this provides a mapping for gene name to various
 * reads) and for displaying labels on the visualizer screen. Since we need both approaches, this structure
 * has an index that maps positional information to the name of the gene.
 */
public final class Genome implements Serializable
{

    private static final long serialVersionUID = -5914141664925486739L;
    private final String name;
    private final Map<String, Gene> geneMap;
    private final Map<Integer, String> index;


    public Genome(final String name, final Map<String, Gene> geneMap, final Map<Integer, String> index)
    {
        this.name = name;
        this.geneMap = geneMap;
        this.index = index;
    }

    @SuppressWarnings("unused")
    public String getName()
    {
        return name;
    }

    @SuppressWarnings("unused")
    public Map<String, Gene> getMap()
    {
        return geneMap;
    }

    @SuppressWarnings("unused")
    public Map<Integer, String> getIndex()
    {
        return index;
    }
}
