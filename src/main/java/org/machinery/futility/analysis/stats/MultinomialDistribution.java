package org.machinery.futility.analysis.stats;

public final class MultinomialDistribution
{
    private final double[] probabilityIntervals;

    public MultinomialDistribution(final double[] probabilities)
    {
        // We could optimize this more if we only include significant windows, AKA thresholds where something actually
        // changed. If we make a list of those and store a tuple of <threshold, index> we'll be better off.
        probabilityIntervals = new double[probabilities.length];
        double sum = 0;
        for (int i = 0; i < probabilities.length; ++i)
        {
            sum += probabilities[i];
            probabilityIntervals[i] = sum;
        }
    }

    private int firstLargerBinarySearch(final int start, final int end, final double target)
    {
        // If start equals end, we're as close as we can get.
        if (start == end)
        {
            return start;
        }
        final int mid = (start + end) / 2;
        if (probabilityIntervals[mid] >= target)
        {
            // include mid, in case it's the actual first element greater than or equal to target
            return firstLargerBinarySearch(start, mid, target);
        }
        // Mid is less than target, so we exclude it
        return firstLargerBinarySearch(mid + 1, end, target);
    }

    public int[] sample(final int numExperiments)
    {
        final int[] results = new int[probabilityIntervals.length];
        for (int i = numExperiments; i > 0; --i)
        {
            final double p = Math.random();
            ++results[firstLargerBinarySearch(0, probabilityIntervals.length - 1, p)];
        }
        return results;
    }
}
