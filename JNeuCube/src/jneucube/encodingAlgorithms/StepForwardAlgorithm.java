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
 * The {@code StepForwardAlgorithm} class encodes temporal data into spike
 * trains using an additive threshold (automatically optimised) that depends on
 * the signal scale. This algorithm is described in the paper "Evolving
 * spatio-temporal data machines based on the NeuCube neuromorphic framework:
 * Design methodology and selected applications" Neural Networks, Volume 78,
 * 2016, Pages 1-14, ISSN 0893-6080.
 *
 * @author Josafath Israel Espinosa Ramos
 */
public class StepForwardAlgorithm extends EncodingAlgorithm {

    private double spikeThresholdValue = 0.5; // The distance between the average and the standard deviation of temporal data changes
    private boolean searchOptimalThreshold = true;

    /**
     * This function encodes a set of data samples (targetData) into spike
     * trains using the information from the reference data.
     *
     * @param referenceData The data samples used as reference to encode the
     * target data
     * @param targetData The data samples to encode based on the information
     * provided by the reference data
     * @param startTime The starting point to encode the data
     * @param endTime The end point to encode the data
     */
    @Override
    public void encode(ArrayList<DataSample> referenceData, ArrayList<DataSample> targetData, int startTime, int endTime) {
        LOGGER.info("Encoding data set using " + this.toString() + " algorithm ");
        for (int i = 0; i < referenceData.size(); i++) {
            LOGGER.info("Encoding sample " + i + ":  " + referenceData.get(i).getSampleLabel());
            this.encode(referenceData.get(i));
        }
        LOGGER.info("Encoding complete ");
    }

    /**
     * This function encodes a data sample (matrix) into spike trains. For each
     * feature an optimal threshold is calculated and used for encoding.
     *
     * @param sample the data sample to encode into spike trains
     */
    @Override
    public void encode(DataSample sample) {
        sample.setSpikeData(encode(sample.getData()));
    }

//    public void encode(DataSample sample) {
//        sample.setSpikeData(encode(sample.getData()));
//    }
    public Matrix encode(Matrix rawData) {
        Matrix spikeData = new Matrix(rawData.getRows(), rawData.getCols());
        double[] signal;
        double[] spikes;
        for (int i = 0; i < rawData.getCols(); i++) {
            signal = rawData.getVecCol(i);
            spikes = this.encode(signal);
            spikeData.setCol(i, spikes);
        }
        return spikeData;
    }

    /**
     * This function encodes a signal into spike trains. It calculates an
     * optimal additive threshold value and uses it for encoding.
     *
     * @param signal the signal to encode
     * @return a spike train
     */
    @Override
    public double[] encode(double[] signal) {
        double threshold = this.spikeThresholdValue;
        if (this.searchOptimalThreshold) {
            threshold = this.getOptimalThreshold(signal);
        }
        return encode(signal, threshold);
    }

    /**
     * This function encodes a signal using a specified threshold value.
     *
     * @param signal the signal to encode
     * @param threshold the additive threshold value
     * @return a spike train
     */
    public double[] encode(double[] signal, double threshold) {
        double[] spikeTrain = new double[signal.length];
        double base = signal[0];
        for (int i = 0; i < spikeTrain.length; i++) {
            spikeTrain[i] = 0;
        }
        for (int t = 1; t < signal.length; t++) {
            if (signal[t] > base + threshold) {
                spikeTrain[t] = 1;
                base = base + threshold;
            } else if (signal[t] < base - threshold) {
                spikeTrain[t] = -1;
                base = base - threshold;
            }
        }
        return spikeTrain;
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        for (int t = 1; t < spikes.length; t++) {
            switch ((int) spikes[t]) {
                case 1:
                    recon[t] = recon[t - 1] + threshold;
                    break;
                case -1:
                    recon[t] = recon[t - 1] - threshold;
                    break;
                default:
                    recon[t] = recon[t - 1];
                    break;
            }
        }
        return recon;
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
        double range = Util.range(signal);
        double[] recon;
        double[] spikes;
        double rmse;
        double bestRmse = Double.POSITIVE_INFINITY;
        for (double i = 0.001; i < 0.8; i += 0.001) {
            spikes = this.encode(signal, i * range);
            recon = this.decode(spikes, i * range, signal[0]);
            rmse = Util.getRMSE(signal, recon);
            if (rmse < bestRmse) {
                bestRmse = rmse;
                thr = i * range;
            }
        }
        return thr;
    }

    @Override
    public String toString() {
        return "Step Forward";
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

    public static void main(String args[]) throws IOException {
        StepForwardAlgorithm algorithm = new StepForwardAlgorithm();
        algorithm.setSearchOptimalThreshold(true);
        
        String fileName;
        String spikeFileName;
        Matrix data;
        File[] sampleFiles;
        
        String sourceDirE = "H:\\DataSets\\pigeons\\Data\\Experiment 6\\Cond6\\Pigeons\\Generalisation_3_5days\\samples_csv_over_total\\";
//        String sourceDirE = "H:\\DataSets\\pigeons\\Data\\Experiment 6\\Cond6\\Pigeons\\prediction_set\\samples_csv_ratio\\";
//        
//        String sourceDirE = "H:\\DataSets\\pigeons\\Data\\Experiment 6\\Cond6\\Pigeons\\samples_csv_over_total\\";        
        //String sourceDirE = "H:\\DataSets\\pigeons\\Data\\Experiment 6\\Cond6\\Pigeons\\prediction_set\\samples_csv_over_total\\";
//        String sourceDirE="H:\\KEDRI Projects\\MetOcean\\Data\\radar_satellite\\2016\\warped\\lightning\\tbb_14\\maxImages\\data\\selected\\";
//        String targetDirE = sourceDirE + "SF\\";
        
//        String sourceDirE = "E:\\DataSets\\MetOcean\\Data\\radar_satellite\\2016_sample\\warped\\lightning_selected\\albedo_03\\centroidImages\\data\\selected\\";
//        String sourceDirH = "H:\\KEDRI Projects\\MetOcean\\Data\\radar_satellite\\2016_sample\\warped\\lightning_selected\\albedo_03\\centroidImages\\data\\selected\\";
        String targetDirE = sourceDirE + "SF\\";
//        String targetDirH = sourceDirH + "SF\\";


        
//        Path sourceTarClass = Paths.get(sourceDirE+"tar_class.csv");
//        Path targetTarClassE = Paths.get(targetDirE+"tar_class.csv");
//        Path targetTarClassH = Paths.get(targetDirH+"tar_class.csv");
//        
        File path = new File(sourceDirE);        
//        
//        if (path.isDirectory()) {
            sampleFiles = path.listFiles((File directory, String name) -> name.matches("sam[a-zA-Z]*[0-9].*\\.csv"));
//            File outputDirectoryE = new File(targetDirE);
//            File outputDirectoryH = new File(targetDirH);
//            
//            if (!outputDirectoryE.exists()) {
//                outputDirectoryE.mkdir();
//            }
//            if (!outputDirectoryH.exists()) {
//                outputDirectoryH.mkdir();
//            }
//            
//            Files.copy(sourceTarClass, targetTarClassE);
//            Files.copy(sourceTarClass, targetTarClassH);
            for (File sampleFile : sampleFiles) {
                fileName = sampleFile.getName();
                //spikeFileName = outputDir + fileName;
                data = new Matrix(path.getPath() + File.separator + fileName, ",");
                Matrix spikeTrain = algorithm.encode(data);
                spikeTrain.export(targetDirE + fileName, ",");
                //spikeTrain.export(targetDirH + fileName, ",");
                System.out.println(fileName);
            }
//        }

//        fileName = "C:\\DataSets\\wrist_movement_eeg\\sam1_eeg.csv";
//        spikeFileName = "C:\\DataSets\\wrist_movement_eeg\\encodedSF\\sam1_eeg.csv";
//        data = new Matrix(fileName, ",");
//        Matrix spikeTrain=algorithm.encode(data);
//        spikeTrain.export(spikeFileName, ",");
    }
}
