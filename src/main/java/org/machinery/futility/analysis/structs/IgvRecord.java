package org.machinery.futility.analysis.structs;

/**
 * Represents a record within an IGV file. These records, are parsed from
 * tab delimited lines in a file.
 */
public final class IgvRecord
{
    private final int start;
    private final int end;
    // Reads can be normalized, therefore they may be a double
    private final double reads;

    public IgvRecord(final int start, final int end, final double reads)
    {
        this.start = start;
        this.end = end;
        this.reads = reads;
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

    public static IgvRecord parseRecord(final String line)
    {
        final String[] values = line.split("\\t");
        final int start, end;
        final double reads;
        start = Integer.parseInt(values[1]);
        end = Integer.parseInt(values[2]);
        reads = Double.parseDouble(values[3]);
        return new IgvRecord(start, end, reads);
    }
}
