package org.machinery.futility.analysis.structs;

import java.io.Serializable;

public final class Gene implements Serializable
{
    private static final long serialVersionUID = 2117680833317804366L;
    private final String name;
    private final int start;
    private final int end;

    private Gene(final String name, final int start, final int end)
    {
        this.name = name;
        this.start = start;
        this.end = end;
    }

    public String getName()
    {
        return name;
    }

    public int getStart()
    {
        return start;
    }

    public int getEnd()
    {
        return end;
    }

    public static Gene parseRecord(final String line)
    {
        final String[] values = line.split("\\s+");
        final String name;
        final int start, end;
        name = values[0];
        start = Integer.parseInt(values[1]);
        end = Integer.parseInt(values[2]);
        return new Gene(name, start, end);
    }
}
