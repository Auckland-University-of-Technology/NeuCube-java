/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.encodingAlgorithms;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import jneucube.data.DataSample;
import jneucube.util.Matrix;
import static jneucube.log.Log.LOGGER;

/**
 * This object encodes temporal data into spike trains using the Temporal
 * Contrast (thresholding base) algorithm. It takes the changes in temporal data
 * and determines a spike event according to a threshold value between the
 * average and the standard deviation of temporal data changes.
 *
 * @author Josafath Israel Espinosa Ramos (Centro de Investigacion en
 * Computacion, Instituto Politecnico Nacional, Mexico 2015)
 *
 * @see "Fast sensory motor control based on event-based hybrid
 * neuromorphic-procedural system"
 * https://www.researchgate.net/publication/224714447_Fast_sensory_motor_control_based_on_event-based_hybrid_neuromorphic-procedural_system
 */
public class AERalgorithm extends EncodingAlgorithm {

    private double spikeThresholdValue = 0.5; // The distance between the average and the standard deviation of temporal data changes
    private double spikeRate = 0.0;
    private Matrix thresholdMatrix; // Contains the threshold values for each feature. All rows have the same values for faster computations.

    public AERalgorithm() {

    }

    public AERalgorithm(double spikeThresholdValue) {
        this.spikeThresholdValue = spikeThresholdValue;
    }

    /**
     * This function encodes raw data into spike trains. The statistics for
     * encoding the data (targetData) are taken from the reference data
     * (referenceData).
     *
     * @param referenceData The data to compute the threshold to produce a
     * spike. Usually the training data
     * @param targetData The data to convert into spike trains. Both, training
     * and validation data
     * @param startTime Start point to encode
     * @param endTime End point to encode
     */
    @Override
    public void encode(ArrayList<DataSample> referenceData, ArrayList<DataSample> targetData, int startTime, int endTime) {
        LOGGER.info("Encoding data set using " + this.toString() + " algorithm ");
        Matrix thresholdVector = this.computeThreshold(referenceData);  // Calculate the threshold value for each feature of the data            
        this.thresholdMatrix = thresholdVector.repmat(referenceData.get(0).getNumRecords(), 1);  // Replicates the threshold vector n-row times (for easy calculations in next procedures)            
        this.encode(targetData, this.thresholdMatrix, startTime, endTime); // encodes the data using the threshold matrix
        this.spikeRate = this.thresholdMatrix.meanValue();
        LOGGER.info("Encoding complete ");
    }

    /**
     * Encodes a single data sample into spike trains using the statistics
     * previously calculated from a set of samples. The spike trains are stored
     * in the {@link jneucube.data.DataSample#spikeData} field of the sample.
     *
     * @param dataSamples The data (list of samples) to be encoded into spike
     * trains
     * @param thresholdVector The threshold vector
     * @param startTime Start point to encode
     * @param endTime End point to encode
     */
    private void encode(ArrayList<DataSample> samples, Matrix thresholdMatrix, int startTime, int endTime) {
        LOGGER.debug("  - Encoding data");
        samples.forEach((sample) -> {
            Matrix spikeData = signal2spike(sample.getData(), thresholdMatrix, 0, sample.getNumRecords()); // Get the spike trains
            sample.setSpikeData(spikeData);
            sample.setEncoded(true);
        });
        LOGGER.debug("  - Encoding complete");
    }

    /**
     * Encodes a single data sample into spike trains. The spike trains are
     * stored in the {@link jneucube.data.DataSample#spikeData} field of the
     * sample.
     *
     * @param sample the data sample to encode
     */
    @Override
    public void encode(DataSample sample) {
//        Matrix thresholdVector = this.computeThreshold(sample);
//        this.thresholdMatrix = thresholdVector.repmat(sample.getNumRecords(), 1);  // Replicates the threshold vector n-row times (for easy calculations in next procedures)            
        Matrix spikeData = signal2spike(sample.getData(), thresholdMatrix, 0, sample.getNumRecords()); // Get the spike trains
        sample.setSpikeData(spikeData);
        sample.setEncoded(true);
    }

    /**
     *
     * @param samples The data (list of sample) to calculate the threshold to
     * produce a spike. Usually training data.
     * @return Threshold vector
     */
//    private Matrix computeThreshold(ArrayList<DataSample> samples) {
//        LOGGER.debug("  - Calculating threshold");
//        Matrix temp;
//        Matrix th = new Matrix(1, samples.get(0).getData().getCols(), 0.0);
//        for (DataSample sample : samples) {
//            temp = sample.getData().diff().abs();   // obtains a vector with the differences
//            //temp.show();
//            // Calculates the mean and the standard deviation multiplyed by the spike threshold value. and sums to the
//            th = th.operation('+', temp.mean().operation('+', temp.std(0).operation('*', this.spikeThresholdValue)));
//        }
//        th = th.operation('/', samples.size());
//        //th.show();
//        LOGGER.debug("  -Calculation complete");
//        return th;
//    }
    private Matrix computeThreshold(ArrayList<DataSample> samples) {
        LOGGER.debug("  - Calculating threshold");
        Matrix temp;
        Matrix th = new Matrix(1, samples.get(0).getData().getCols(), 0.0);
        for (DataSample sample : samples) {
            th = th.operation('+', this.computeThreshold(sample));
            //temp = sample.getData().diff().abs();   // obtains a vector with the differences
            //th = th.operation('+', temp.mean().operation('+', temp.std(0).operation('*', this.spikeThresholdValue)));
        }
        th = th.operation('/', samples.size());
        //th.show();
        LOGGER.debug("  -Calculation complete");
        return th;
    }

    /**
     * This function calculates the threshold
     *
     * @param sample The data (list of sample) to calculate the threshold to
     * produce a spike. Usually training data.
     * @return Threshold vector
     */
    private Matrix computeThreshold(DataSample sample) {
        Matrix temp = sample.getData().diff().abs();   // obtains a vector with the differences        
        temp.mean().operation('+', temp.std(0).operation('*', this.spikeThresholdValue));
        return temp;
    }

    /**
     *
     * @param data The data to be encoded into spikes
     * @param thresholdMatrix
     * @param startTime
     * @param endTime
     * @return
     */
    private Matrix signal2spike(Matrix data, Matrix thresholdMatrix, int startTime, int endTime) {
        Matrix eegSpike;
        Matrix base;
        Matrix excSpike;
        Matrix inhSpike;
        //base = data.getRow(0).vertInsert(data.get(startTime, endTime - 1, 0, data.getCols() - 1));
        base = data.getRow(0).vertInsert(data.get(startTime, endTime, 0, data.getCols()));
        excSpike = data.operation('-', base).operation('>', thresholdMatrix);
        inhSpike = data.operation('-', base).operation('<', thresholdMatrix.operation('*', -1.0));
        eegSpike = excSpike.operation('-', inhSpike);
        return eegSpike;
    }

    @Override
    public String toString() {
        return "AER";
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
     * @return the spikeRate
     */
    public double getSpikeRate() {
        return spikeRate;
    }

    /**
     * @param spikeRate the spikeRate to set
     */
    public void setSpikeRate(double spikeRate) {
        this.spikeRate = spikeRate;
    }

    @Override
    public double[] encode(double[] newRecord) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void clear() {
        if (this.thresholdMatrix != null) {
            this.thresholdMatrix.clear();
        }
        this.thresholdMatrix = null;
        this.spikeThresholdValue = 0.5; // The distance between the average and the standard deviation of temporal data changes
        this.spikeRate = 0.0;
    }

    public static void main(String args[]) {
        try {
            Matrix dataSample = new Matrix("C:\\DataSets\\GSignals.csv", ",");
            DataSample sample = new DataSample();
            sample.setSampleId(0);
            sample.setData(dataSample);
            sample.setNumFeatures(dataSample.getCols());
            sample.setNumRecords(dataSample.getRows());
            ArrayList<DataSample> samples = new ArrayList<>();
            samples.add(sample);
            AERalgorithm algorithm = new AERalgorithm(0.5);
            algorithm.encode(samples, samples, 0, sample.getNumRecords());
            sample.getSpikeData().export("C:\\DataSets\\GSignals_encoded.csv", ",");
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(AERalgorithm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
