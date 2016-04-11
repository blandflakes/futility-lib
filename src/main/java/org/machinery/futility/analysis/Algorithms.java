package org.machinery.futility.analysis;

import org.machinery.futility.analysis.stats.MultinomialDistribution;
import org.machinery.futility.analysis.structs.Control;
import org.machinery.futility.analysis.structs.SequenceMeasurements;
import org.machinery.futility.analysis.structs.Experiment;
import org.machinery.futility.analysis.structs.Gene;
import org.machinery.futility.analysis.structs.GeneFeatureMeasurements;
import org.machinery.futility.analysis.structs.Genome;
import org.machinery.futility.analysis.structs.IgvRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Entry point for analyzing various data sets. Input is an InputStream?
 */
@SuppressWarnings("unused")
public final class Algorithms
{
    private static final Comparator<IgvRecord> IGV_RECORD_COMPARATOR = new Comparator<IgvRecord>()
    {
        @Override
        public int compare(final IgvRecord o1, final IgvRecord o2)
        {
            if (o1.getReads() < o2.getReads())
            {
                return -1;
            }
            if (o1.getReads() > o2.getReads())
            {
                return 1;
            }
            return 0;
        }
    };

    private Algorithms()
    {
    }

    private static <T> void updateIndex(final Map<Integer, T> index, final T value, final int start,
                                        final int end)
    {
        for (int i = start; i < end; ++i)
        {
            index.put(i, value);
        }
    }

    public static Genome analyzeGenome(final String name, final InputStream inputStream) throws IOException
    {
        final Map<String, Gene> geneMap = new HashMap<>();
        final Map<Integer, String> index = new HashMap<>();
        String line;
        Gene gene;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream)))
        {
            do
            {
                line = reader.readLine();
                if (null != line)
                {
                    gene = Gene.parseRecord(line);
                    geneMap.put(gene.getName(), gene);
                    updateIndex(index, gene.getName(), gene.getStart(), gene.getEnd());
                }
            }
            while (null != line);
        }
        return new Genome(name, geneMap, index);
    }

    /**
     * Finds the item at the provided percentile. Might make sense to lump this into the regular ingestion run,
     * saving a traversal of the data. However, this is still a ton better than the javascript version
     * @param percentile percentile to return item from
     * @param data source of item
     * @param comparator method of sorting
     * @param <T> the type of data we're dealing with
     * @return the item located at the given percentile, according to the comparator.
     */
    private static <T> T atPercentile(final double percentile, final List<T> data, final Comparator<T> comparator)
    {
        final int thresholdIndex = (int) percentile * data.size();
        final int queueSize = data.size() - thresholdIndex;
        // Construct a max queue
        final PriorityQueue<T> queue = new PriorityQueue<>(queueSize, comparator);
        for (final T datum : data)
        {
            if (queue.size() < queueSize)
            {
                queue.add(datum);
            }
            else
            {
                final T head = queue.peek();
                if (comparator.compare(head, datum) <= 0) {
                    queue.poll();
                    queue.add(datum);
                }
            }
        }
        // So, now the first item in the queue is the item at the given percentile.
        return queue.peek();
    }

    private static void consumeDataSet(final List<IgvRecord> rawData, final Map<Integer, Integer> index,
                                       final Map<String, Integer> stats, final InputStream inputStream)
            throws IOException
    {
        int count = 0;
        int totalSiteReads = 0;
        int siteHits = 0;

        String line;
        IgvRecord record;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream)))
        {
            do
            {
                line = reader.readLine();
                if (null != line)
                {
                    record = IgvRecord.parseRecord(line);
                    rawData.add(record);
                    updateIndex(index, count, record.getStart(), record.getEnd());
                    ++count;
                    if (record.getReads() > 0)
                    {
                        ++siteHits;
                        totalSiteReads += record.getReads();
                    }
                }
            }
            while (null != line);
        }
        final int maxPosition = rawData.get(rawData.size() - 1).getEnd();
        final int redThreshold = (int) atPercentile(0.99999, rawData, IGV_RECORD_COMPARATOR).getReads();
        stats.put("linesRead", count);
        stats.put("siteHits", siteHits);
        stats.put("totalSiteReads", totalSiteReads);
        stats.put("maxPosition", maxPosition);
        stats.put("redThreshold", redThreshold);
    }

    public static Control analyzeControl(final String name, final String genomeName, final InputStream inputStream)
            throws IOException
    {
        final List<IgvRecord> rawData = new ArrayList<>();
        final Map<Integer, Integer> index = new HashMap<>();
        final Map<String, Integer> stats = new HashMap<>();
        consumeDataSet(rawData, index, stats, inputStream);
        final SequenceMeasurements sequenceMeasurements = new SequenceMeasurements(rawData, index, stats);
        return new Control(name, genomeName, sequenceMeasurements);
    }

    private static int[][] performSampling(final SequenceMeasurements normalizee,
                                    final SequenceMeasurements normalizer)
    {
        final int normalizerCount = normalizer.getStats().get("siteHits");
        final int normalizeeCount = normalizee.getStats().get("siteHits");

        final double proportion = normalizerCount / (double) normalizeeCount;
        final List<IgvRecord> normalizeeRawData = normalizee.getRawData();

        final double[] probabilityVector = new double[normalizeeRawData.size() + 1];
        int index = 0;
        for (final IgvRecord record : normalizeeRawData)
        {
            probabilityVector[index] = (proportion * record.getReads()) / normalizeeCount;
            ++index;
        }
        probabilityVector[probabilityVector.length - 1] = 1 - proportion;
        final int numSamples = 100;
        final int[][] samples = new int[numSamples][];
        final MultinomialDistribution multinomial = new MultinomialDistribution(probabilityVector);
        for (int i = 0; i < numSamples; ++i)
        {
            samples[i] = multinomial.sample(normalizerCount);
        }
        return samples;
    }

    private static double[][] performCorrection(final int[][] samples, final int numExperiments)
    {
        // The original algorithm transposes the sample and goes to the end of the first
        // column. We haven't transposed, so we go to the end of the first row. Still feels arbitrary, but...
        // Original algorithm also made a matrix of this identical value, which I shouldn't need to do.
        final int difference = numExperiments - samples[0][samples[0].length - 1];
        final double correction = numExperiments / (double) difference;
        final double[][] correctedData = new double[samples.length][];
        for (int i = 0; i < samples.length; ++i)
        {
            final int[] row = samples[i];
            correctedData[i] = new double[row.length];
            for (int j = 0; j < row.length; ++j)
            {
                correctedData[i][j] = row[j] * correction;
            }
        }
        return correctedData;
    }

    private static double[] collapseAndAverage(final double[][] correctedSamples)
    {
        final int numSamples = correctedSamples[0].length;
        final double[] averaged = new double[numSamples];
        for (final double[] row : correctedSamples)
        {
            for (int colNum = 0; colNum < row.length; ++colNum)
            {
                final double value = row[colNum] / numSamples;
                averaged[colNum] += value;
            }
        }
        return averaged;
    }

    private static List<IgvRecord> normalize(final SequenceMeasurements normalizee,
                                             final SequenceMeasurements normalizer)
    {
        int[][] samples = performSampling(normalizee, normalizer);
        double[][] correctedSamples = performCorrection(samples, normalizer.getStats().get("totalSiteReads"));
        samples = null; // There's a lot of memory going on here, and this operation takes a bit.
        double[] averaged = collapseAndAverage(correctedSamples);
        correctedSamples = null; // There's a lot of memory going on here, and this operation takes a bit.
        final List<IgvRecord> normalizedData = new ArrayList<>(averaged.length);
        int index = 0;
        for (final IgvRecord record : normalizee.getRawData())
        {
            normalizedData.add(new IgvRecord(record.getStart(), record.getEnd(), averaged[index]));
        }
        return normalizedData;
    }

    private static Map<String, GeneFeatureMeasurements> deriveFeatures(final Genome genome,
                                                                       final Control control,
                                                                       final SequenceMeasurements experimentMeasurements)
    {
        final SequenceMeasurements controlMeasurements = control.getSequenceMeasurements();
        final List<IgvRecord> rawControlData, rawExperimentData;
        if (controlMeasurements.getStats().get("siteHits") >= experimentMeasurements.getStats().get("siteHits"))
        {
            rawControlData = normalize(controlMeasurements, experimentMeasurements);
            rawExperimentData = experimentMeasurements.getRawData();
        }
        else
        {
            rawControlData = controlMeasurements.getRawData();
            rawExperimentData = normalize(experimentMeasurements, controlMeasurements);
        }
        // line 279 of analysis.js
    }

    public static Experiment analyzeExperiment(final String name, final Genome genome, final Control control,
                                               final InputStream inputStream)
            throws IOException
    {
        final List<IgvRecord> rawData = new ArrayList<>();
        final Map<Integer, Integer> index = new HashMap<>();
        final Map<String, Integer> stats = new HashMap<>();
        consumeDataSet(rawData, index, stats, inputStream);
        final SequenceMeasurements sequenceMeasurements = new SequenceMeasurements(rawData, index, stats);
        final Map<String, GeneFeatureMeasurements> features = deriveFeatures(genome, control, sequenceMeasurements);
        return new Experiment(name, genome.getName(), control.getName(), sequenceMeasurements, features);
    }
}
