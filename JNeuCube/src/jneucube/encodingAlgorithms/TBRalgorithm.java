/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.encodingAlgorithms;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import jneucube.data.DataSample;
import static jneucube.log.Log.LOGGER;
import jneucube.util.Matrix;
import jneucube.util.Util;

/**
 * The {@code TBRalgorthm} class allows to encode temporal variable into spike
 * train. The simplest implementation of threshold base representation algorithm
 * is based on tracking temporal changes in the signal. The absolute value
 * change between consecutive signal values is compared to a threshold; if large
 * enough, a positive/negative spike is emitted (based on the sign of change).
 * To calculate this threshold, the whole sample length is taken into account.
 * The first derivative is calculated, then the standard deviation of this
 * derivative is multiplied by a factor to obtain the encoding threshold {
 *
 * @ jneucube.encodingAlgorithms.TBRalgorithm#encode(double[] signal, double[]
 * spikeTrain, double threshold)}. The parameter of this encoding is this factor
 * which is independent of the signal amplitude but is determined by the signal
 * characteristics. Decoding of the signal is straightforward: the reconstructed
 * signal is given by a summation of positive and negative spikes multiplied by
 * the encoding threshold. The initial reconstruction value should match the
 * initial signal value.
 *
 * @author em9403
 */
public class TBRalgorithm extends EncodingAlgorithm {

    /**
     * The distance between the average and the standard deviation of temporal
     * data changes
     */
    private double spikeThresholdValue = 0.5;
    /**
     * Defines whether the algorithm will search for the optimal threshold
     * value or use the threshold value defined by the user for encoding the
     * data.
     */
    private boolean searchOptimalThreshold = false;

    @Override
    public void encode(ArrayList<DataSample> referenceData, ArrayList<DataSample> targetData, int startTime, int endTime) {
        LOGGER.info("Encoding data set using " + this.toString() + " algorithm ");
        for (int i = 0; i < referenceData.size(); i++) {
            LOGGER.info("Encoding sample " + i + ":  " + referenceData.get(i).getSampleLabel());
            this.encode(referenceData.get(i));
        }
        LOGGER.info("Encoding complete ");
    }

    @Override
    public void encode(DataSample sample) {
        sample.setSpikeData(this.encode(sample.getData()));
    }

    public Matrix encode(Matrix rawData) {
        Matrix spikeData = new Matrix(rawData.getRows(), rawData.getCols());
        double[] spikeTrain;
        for (int i = 0; i < rawData.getCols(); i++) {
            spikeTrain = encode(rawData.getVecCol(i));
            spikeData.setCol(i, spikeTrain);
        }
        return spikeData;
    }

    @Override
    public double[] encode(double[] signal) {
        double threshold = this.spikeThresholdValue;
        if (this.searchOptimalThreshold) {
            threshold = this.getOptimalThreshold(signal);
        }
        return encode(signal, threshold);
    }

    /**
     * Encodes a signal into a spike train given a threshold value.
     *
     * @param signal the signal to be encoded
     * @param threshold the relative threshold value between the mean (0) and
     * the standard deviation (1) of the signal. The threshold value can be
     * grater than 1.
     * @return the spike train
     */
    public double[] encode(double[] signal, double threshold) {
        double[] spikeTrain = new double[signal.length];
        this.encode(signal, spikeTrain, threshold);
        return spikeTrain;
    }

    /**
     * Encodes a signal into a spike train given a threshold value. 1.
     * Calculates the derivative of the signal, i.e. the differences between
     * each consecutive pair of temporal points. 2. Gets a vector with the sign
     * of the changes. This vector will be used for defining the positive or
     * negative spikes. 3. Gets the absolute value of the differences. 4.
     * Calculates the mean of the differences. 5. Calculates the standard
     * deviation of the differences. 6. Calculates the real threshold value. 7.
     * Gets a vector of spikes where all the values that surpasses the real
     * threshold value are set to one otherwise to zero. 8. Gets a spike train
     * by multiplying (not the dot product) the vector of spikes by the vector
     * of signs.
     *
     * @param signal the signal to be encoded
     * @param spikeTrain the spike train
     * @param threshold
     * @return the real threshold value between the mean and standard deviation
     * of the signal
     */
    public double encode(double[] signal, double[] spikeTrain, double threshold) {
        double[] absDiff = new double[signal.length];
        System.arraycopy(Util.getVecDiff(signal), 0, absDiff, 1, signal.length - 1);
        double[] signs = Util.getVecBoolan(absDiff, ">=", 0, 1, -1);
        absDiff = Util.getVecAbs(absDiff);
        double meanSignal = Util.mean(absDiff);
        double stdSignal = Util.std(absDiff);
        double thValue = meanSignal + (stdSignal * threshold);
        double spikes[] = Util.getVecBoolan(absDiff, ">=", thValue, 1, 0);
        System.arraycopy(Util.getVecMult(spikes, signs), 0, spikeTrain, 0, signal.length);
        return thValue;
    }

    /**
     * Gets the best threshold value according to the RMSE (root mean square
     * error) between the original signal and the reconstructed spike train.
     *
     * @param signal
     * @return the best threshold value
     */
    public double getOptimalThreshold(double[] signal) {
        double thr = 0.0;
        double tempTh;
        double[] recon;
        double[] spikes = new double[signal.length];
        double rmse;
        double bestRmse = Double.POSITIVE_INFINITY;
        for (double i = 0.001; i < 0.8; i += 0.001) {
            tempTh = this.encode(signal, spikes, i);
            recon = this.decode(spikes, tempTh, signal[0]);
//            Util.printVertArray(spikes);
//            Util.printVertArray(recon);
            rmse = Util.getRMSE(signal, recon);
            if (rmse < bestRmse) {
                bestRmse = rmse;
                thr = i;
            }
        }
        return thr;
    }

    /**
     * Decodes the spike trains into a signal given a threshold value and the
     * first value of the original signal.
     *
     * @param spikes the spike train to decode
     * @param threshold the threshold value
     * @param start the first value of the original signal
     * @return a reconstructed signal
     */
    public double[] decode(double[] spikes, double threshold, double start) {
        double[] recon = new double[spikes.length];
        recon[0] = start;
        for (int i = 1; i < spikes.length; i++) {
            recon[i] = recon[i - 1] + (spikes[i] * threshold);
        }
        return recon;
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * @return the spikeThresholdValue
     */
    public double getSpikeThresholdValue() {
        return spikeThresholdValue;
    }

    /**
     * @param spikeThresholdValue the spikeThresholdValue to set
     */
    public void setSpikeThresholdValue(double spikeThresholdValue) {
        this.spikeThresholdValue = spikeThresholdValue;
    }

    /**
     * @return the searchOptimalThreshold
     */
    public boolean isSearchOptimalThreshold() {
        return searchOptimalThreshold;
    }

    /**
     * @param searchOptimalThreshold the searchOptimalThreshold to set
     */
    public void setSearchOptimalThreshold(boolean searchOptimalThreshold) {
        this.searchOptimalThreshold = searchOptimalThreshold;
    }

    @Override
    public String toString() {
        return "Threshold Base Representation";
    }

    public static void main(String[] args) throws IOException {
        TBRalgorithm algorithm = new TBRalgorithm();
        algorithm.setSearchOptimalThreshold(true);
        //String dir = "H:\\KEDRI Projects\\MetOcean\\Data\\radar_satellite\\2016_sample\\warped\\lightning_selected\\albedo_03\\centroidImages\\data\\selected\\";
        String dir="C:\\DataSets\\pigeons\\Data\\Experiment 6\\Cond1\\Pigeons\\samples_csv\\";
        String outputDir = dir + "TBR\\";
        String fileName;
        String spikeFileName;
        Matrix data;

        File path = new File(dir);        
        Path sourceDirectory = Paths.get(dir+"tar_class.csv");
        Path targetDirectory = Paths.get(outputDir+"tar_class.csv");        
        File[] sampleFiles;
        
        if (path.isDirectory()) {
            sampleFiles = path.listFiles((File directory, String name) -> name.matches("sam[a-zA-Z]*[0-9].*\\.csv"));
            File outputDirectory = new File(outputDir);
            if (!outputDirectory.exists()) {
                outputDirectory.mkdir();
            }
            Files.copy(sourceDirectory, targetDirectory);
            for (File sampleFile : sampleFiles) {
                fileName = sampleFile.getName();
                spikeFileName = outputDir + fileName;
                data = new Matrix(path.getPath() + File.separator + fileName, ",");
                Matrix spikeTrain = algorithm.encode(data);
                spikeTrain.export(spikeFileName, ",");
                System.out.println(fileName);
            }
        }

//        fileName = "C:\\DataSets\\wrist_movement_eeg\\sam1_eeg.csv";
//        spikeFileName = "C:\\DataSets\\wrist_movement_eeg\\encodedSF\\sam1_eeg.csv";
//        data = new Matrix(fileName, ",");
//        Matrix spikeTrain=algorithm.encode(data);
//        spikeTrain.export(spikeFileName, ",");
    }
}
