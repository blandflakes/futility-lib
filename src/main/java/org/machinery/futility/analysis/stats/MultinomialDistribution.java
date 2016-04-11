package org.machinery.futility.analysis.stats;

public final class MultinomialDistribution
{
    private final double[] probabilityIntervals;

    public MultinomialDistribution(final double[] probabilities)
    {
        probabilityIntervals = new double[probabilities.length];
        double sum = 0;
        for (int i = 0; i < probabilities.length; ++i)
        {
            sum += probabilities[i];
            probabilityIntervals[i] = sum;
        }
    }

    private int indexOfContainingInterval(final double p)
    {
        // We could optimize this by doing a binary search. Should be fast enough, though.
        for (int i = 0; i < probabilityIntervals.length; ++i)
        {
            if (probabilityIntervals[i] > p)
            {
                return i;
            }
        }
        return probabilityIntervals.length - 1;
    }

    public int[] sample(final int numExperiments)
    {
        final int[] results = new int[probabilityIntervals.length];
        for (int i = numExperiments; i > 0; --i)
        {
            final double p = Math.random();
            ++results[indexOfContainingInterval(p)];
        }
        return results;
    }
}
