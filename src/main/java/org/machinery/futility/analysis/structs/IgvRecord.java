package org.machinery.futility.analysis.structs;

import java.io.Serializable;

/**
 * Represents a record within an IGV file. These records, are parsed from
 * tab delimited lines in a file.
 */
public final class IgvRecord implements Serializable
{
    private static final long serialVersionUID = 5023223237667098932L;
    private final int start;
    private final int end;
    // Reads can be normalized, therefore they may be a double
    private final double reads;
    // Can be null
    private final String geneName;

    public IgvRecord(final int start, final int end, final double reads, final String geneName)
    {
        this.start = start;
        this.end = end;
        this.reads = reads;
        this.geneName = geneName;
    }

    public int getStart()
    {
        return start;
    }

    public int getEnd()
    {
        return end;
    }

    public double getReads()
    {
        return reads;
    }

    public String getGeneName()
    {
        return geneName;
    }

    public static IgvRecord parseRecord(final String line)
    {
        final String[] values = line.trim().split("\\s+");
        final int start, end;
        final double reads;
        start = Integer.parseInt(values[1]);
        end = Integer.parseInt(values[2]);
        reads = Double.parseDouble(values[3]);
        final String geneName;
        if (values.length > 4)
        {
            geneName = values[4];
        }
        else
        {
            geneName = null;
        }
        return new IgvRecord(start, end, reads, geneName);
    }
}
