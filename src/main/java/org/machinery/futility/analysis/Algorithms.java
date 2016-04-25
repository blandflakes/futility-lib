package org.machinery.futility.analysis;

import org.apache.commons.math3.stat.inference.MannWhitneyUTest;
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
import java.util.Arrays;
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

    private static final MannWhitneyUTest MWU_TEST = new MannWhitneyUTest();


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
        final int thresholdIndex = (int) (percentile * data.size());
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

    private static int[][] performSampling(final SequenceMeasurements normalizee, final SequenceMeasurements normalizer)
    {
        final int normalizerCount = normalizer.getStats().get("siteHits");
        final int normalizeeCount = normalizee.getStats().get("siteHits");
        final int normalizerTotalSiteReads = normalizer.getStats().get("totalSiteReads");
        final int normalizeeTotalSiteReads = normalizee.getStats().get("totalSiteReads");

        final double proportion = normalizerCount / (double) normalizeeCount;
        final List<IgvRecord> normalizeeRawData = normalizee.getRawData();

        final double[] probabilityVector = new double[normalizeeRawData.size() + 1];
        int index = 0;
        for (final IgvRecord record : normalizeeRawData)
        {
            probabilityVector[index] = (proportion * record.getReads()) / normalizeeTotalSiteReads;
            ++index;
        }
        probabilityVector[probabilityVector.length - 1] = 1 - proportion;
        final int numSamples = 100;
        final int[][] samples = new int[numSamples][];
        final MultinomialDistribution multinomial = new MultinomialDistribution(probabilityVector);
        for (int i = 0; i < numSamples; ++i)
        {
            samples[i] = multinomial.sample(normalizerTotalSiteReads);
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
            // We use row.length - 1 to skip the extra element that we added during sampling (1 - proportion).
            correctedData[i] = new double[row.length - 1];
            for (int j = 0; j < row.length - 1; ++j)
            {
                correctedData[i][j] = row[j] * correction;
            }
        }
        return correctedData;
    }

    /**
     * Takes a list of samples, all the same length, and averages each column
     * to create a new, one-dimensional sample of the same length
     * @param correctedSamples array of samples to combine
     * @return single sample whtat is the average of correctedSamples
     */
    private static double[] collapseAndAverage(final double[][] correctedSamples)
    {
        final int numSamples = correctedSamples.length;
        final int lengthOfSamples = correctedSamples[0].length;
        final double[] averaged = new double[lengthOfSamples];
        for (final double[] row : correctedSamples)
        {
            for (int colNum = 0; colNum < lengthOfSamples; ++colNum)
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
            normalizedData.add(new IgvRecord(record.getStart(), record.getEnd(), averaged[index], record.getGeneName()));
            ++index;
        }
        return normalizedData;
    }

    /**
     * Iterates over the raw data. For each position, if it corresponds to a gene, we add the number to the list of
     * reads in the map for that gene's name. We return the total number of reads added.
     * @param rawData igv data to iterate over
     * @param readsMap initialize map of gene name to empty list
     * @param genomeMap map of gene name to gene objects
     * @return the total number of reads added
     */
    private static double groupReadsByGene(final List<IgvRecord> rawData, final Map<String, List<Double>> readsMap,
                                         final Map<String, Gene> genomeMap)
    {
        double totalCount = 0;
        for (final IgvRecord record : rawData)
        {
            final String geneName = record.getGeneName();
            if (null != geneName && genomeMap.containsKey(geneName))
            {
                final Gene gene = genomeMap.get(geneName);
                final int site = record.getStart();
                final int start = gene.getStart();
                final int end = gene.getEnd();
                final int geneLength = end - start;
                if (start + (0.03 * geneLength) <= site && site <= (end - (0.03 * geneLength)))
                {
                    readsMap.get(geneName).add(record.getReads());
                    totalCount += record.getReads();
                }
            }
        }
        return totalCount;
    }

    private static double sum(final List<Double> toSum)
    {
        double sum = 0;
        for (final double num : toSum)
        {
            sum += num;
        }
        return sum;
    }

    private static double[] toDoubleArray(final List<Double> doubles)
    {
        final double[] array = new double[doubles.size()];
        int index = 0;
        for (final double num : doubles)
        {
            array[index] = num;
            ++index;
        }
        return array;
    }

    private static void replacePWithBhq(final GeneFeatureMeasurements.Builder[] arr)
    {
        Arrays.sort(arr, new Comparator<GeneFeatureMeasurements.Builder>()
        {
            @Override
            public int compare(GeneFeatureMeasurements.Builder o1, GeneFeatureMeasurements.Builder o2)
            {
                if (o1.getP() > o2.getP())
                {
                    return 1;
                }
                if (o1.getP() < o2.getP())
                {
                    return -1;
                }
                return 0;
            }
        });

        double minCoeff = arr[arr.length - 1].getP();
        double coeff;
        for (int i = arr.length - 2; i >= 0; --i)
        {
            coeff = arr.length * arr[i].getP() / (i + 1);
            minCoeff = Math.min(coeff, minCoeff);
            arr[i].withP(minCoeff);
        }
    }

    private static void correctIndex(final GeneFeatureMeasurements.Builder[] arr)
    {
        Arrays.sort(arr, new Comparator<GeneFeatureMeasurements.Builder>()
        {
            @Override
            public int compare(GeneFeatureMeasurements.Builder o1, GeneFeatureMeasurements.Builder o2)
            {
                if (o1.getFitness() > o2.getFitness())
                {
                    return 1;
                }
                if (o1.getFitness() < o2.getFitness())
                {
                    return -1;
                }
                return 0;
            }
        });
        for (int i = 0; i < arr.length; ++i)
        {
            arr[i].withFitness(i / (double) arr.length);
        }
    }

    private static Map<String, GeneFeatureMeasurements> deriveFeatures(final Genome genome,
                                                                       final Control control,
                                                                       final String name,
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

        final Map<String, List<Double>> controlReads = new HashMap<>();
        final Map<String, List<Double>> experimentReads = new HashMap<>();
        for (final String geneName : genome.getMap().keySet())
        {
            controlReads.put(geneName, new ArrayList<Double>());
            experimentReads.put(geneName, new ArrayList<Double>());
        }

        final double totalControlReads = groupReadsByGene(rawControlData, controlReads, genome.getMap());
        final double totalExperimentReads = groupReadsByGene(rawExperimentData, experimentReads, genome.getMap());
        final double minControlReads = totalControlReads / 10000;
        final double minExperimentReads = totalExperimentReads / 10000;

        // We'll make a map of builders first, as we need to modify some values after accumulating the list
        // but before creating the immutable feature measurements
        final Map<String, GeneFeatureMeasurements.Builder> featureBuilders = new HashMap<>();
        for (final String geneName : genome.getMap().keySet())
        {
            final Gene gene = genome.getMap().get(geneName);
            final int geneLength = gene.getEnd() - gene.getStart();
            final List<Double> controlReadsList = controlReads.get(geneName);
            final List<Double> experimentReadsList = experimentReads.get(geneName);

            final double p;
            // Both lists should be of the same length, so just check one
            if (controlReadsList.isEmpty())
            {
                p = 0.0;
            }
            else
            {
                p = MWU_TEST.mannWhitneyUTest(toDoubleArray(controlReadsList),
                        toDoubleArray(experimentReadsList));
            }
            final GeneFeatureMeasurements.Builder featureBuilder = new GeneFeatureMeasurements.Builder()
                    .withCondition(name)
                    .withNumTASites(controlReadsList.size())
                    .withNumControlReads(sum(controlReadsList))
                    .withNumExperimentReads(sum(experimentReadsList))
                    .withGeneLength(geneLength)
                    .withP(p);

            final double significantControlReads = Math.min(featureBuilder.getNumControlReads(), minControlReads);
            final double significantExperimentReads = Math.min(featureBuilder.getNumExperimentReads(), minExperimentReads);
            final double correctedRatio = significantControlReads == 0 ? 0 : significantExperimentReads / significantControlReads;
            final double essentialityIndex = significantExperimentReads / geneLength;
            featureBuilder.withModifiedRatio(correctedRatio);
            featureBuilder.withEssentialityIndex(essentialityIndex);
            featureBuilder.withFitness(correctedRatio * essentialityIndex);

            featureBuilders.put(geneName, featureBuilder);
        }
        GeneFeatureMeasurements.Builder[] featuresArray =
                featureBuilders.values().toArray(new GeneFeatureMeasurements.Builder[featureBuilders.size()]);
        replacePWithBhq(featuresArray);
        correctIndex(featuresArray);
        // We updated the objects through references, so now we can null out the large arrays while we do the final copy
        featuresArray = null;
        final Map<String, GeneFeatureMeasurements> features = new HashMap<>();
        for (final String geneName : featureBuilders.keySet())
        {
            features.put(geneName, featureBuilders.get(geneName).build());
        }
        return features;
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
        final Map<String, GeneFeatureMeasurements> features = deriveFeatures(genome, control, name, sequenceMeasurements);
        return new Experiment(name, genome.getName(), control.getName(), sequenceMeasurements, features);
    }
}
