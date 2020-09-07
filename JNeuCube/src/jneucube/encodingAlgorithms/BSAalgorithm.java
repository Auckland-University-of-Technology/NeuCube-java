/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.encodingAlgorithms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import jneucube.data.DataSample;
import jneucube.util.Matrix;
import jneucube.util.NeuCubeRuntimeException;
import jneucube.util.Util;
import static jneucube.log.Log.LOGGER;


/**
 *
 * @author Josafath Israel Espinosa Ramos
 */
public class BSAalgorithm extends EncodingAlgorithm {

    /**
     * This field indicates the number of filter to be created. If this value is
     * set to 1 then all the features will use the same filter and threshold. If
     * it is greater than it must coincide with the number of features of the
     * data set to create a specific filter for each feature.
     */
    private int numFilters = 0;
    /**
     * This field indicates the threshold used in the BSA algorithm
     */
    private double threshold;
    /**
     * This field contains the threshold values that the BSA uses for encoding
     * every feature of the data set.
     */
    private double[] thresholdVec;
    /**
     * This field contains the order to create the low pass filters that the BSA
     * uses for encoding every feature of the data set.
     */
    private int[] filterOrderVec;
    /**
     * This field contains the cutoff frequency to create the filters that the
     * BSA uses for encoding every feature of the data set.
     */
    private double[] filterCutoffVec;
    /**
     * This field contains the filters that the BSA uses for encoding every
     * feature of the data set.
     */
    double[][] filters;
    /**
     * This field contains the coefficients of the filter (the filter) that the
     * BSA uses for encoding one feature of the data set.
     */
    private double[] filter;
    /**
     * This field is used to validate that a filter was created before encoding
     * the signal.
     */
    private boolean filterCreated = false;

    

    @Override
    public void encode(ArrayList<DataSample> referenceData, ArrayList<DataSample> targetData, int startTime, int endTime) {
        LOGGER.info(" Encoding the data set using " + this.toString() + " algorithm ");
        long processTime = System.nanoTime();
        // validating that the arrays of the parameters are the same otherwise throws an exception        
        int numFeatures = referenceData.get(0).getNumFeatures();
        if (numFilters == 0) {
            throw new NeuCubeRuntimeException("The number of filters must be one or the same as the number of features.");
        } else if (numFilters == 1) {   // All the featrues will use the same filter and threshold.
            filters = new double[thresholdVec.length][];
            filters[0] = this.fir1(filterOrderVec[0], filterCutoffVec[0]);
            this.filterCreated = true;
        } else if ((numFilters > 1) && (numFilters == numFeatures) && (numFilters == thresholdVec.length)) {   // The number of filters must be the same as the number of features
            if ((thresholdVec.length != filterOrderVec.length) || (thresholdVec.length != filterCutoffVec.length) || (filterOrderVec.length != filterCutoffVec.length)) {
                throw new NeuCubeRuntimeException("The threshold, filter order, and filter cutoff arrays must have the same length.");
            } else { // Creating the set of filters to be used for every feature
                filters = new double[thresholdVec.length][];
                for (int i = 0; i < thresholdVec.length; i++) {
                    filters[i] = this.fir1(filterOrderVec[i], filterCutoffVec[i]);
                }
                this.filterCreated = true;
            }
        } else if (numFilters != thresholdVec.length) {
            throw new NeuCubeRuntimeException("The variable numFilters is different from the number of elements in the threshold, filter order, or filter cutoff arrays.");
        }

        targetData.forEach((sample) -> {
            this.encode(sample);
        });
        LOGGER.info(" Encoding complete (time " + ((System.nanoTime() - processTime) / 1000000) + " milliseconds seconds) ");
    }

    /**
     * The filter or set of filters must be created before.
     *
     * @param sample
     */
    @Override
    public void encode(DataSample sample) {
        LOGGER.debug("- Encoding sample " + sample.getSampleId());
        long processTime = System.nanoTime();
        Matrix sampleData = sample.getData();
        Matrix spikeData = new Matrix(sampleData.getRows(), sampleData.getCols());
        
        double[] spikeTrain;
        for (int i = 0; i < sampleData.getCols(); i++) {
            // Choosing the filter and the threshold for encoding the data
            if (this.numFilters == 1) {
                filter = filters[0];
                this.threshold = this.thresholdVec[0];
            } else if (this.numFilters > 1) {
                filter = filters[i];
                this.threshold = this.thresholdVec[i];
            }
            spikeTrain = this.encode(sampleData.getVecCol(i));
            spikeData.setCol(i, spikeTrain);
        }
        sample.setSpikeData(spikeData);
        sample.setEncoded(true);
        LOGGER.debug("- Encoding complete (time " + ((System.nanoTime() - processTime) / 1000000) + " milliseconds seconds) ");
    }

    /**
     * This function uses a filter previously created (see
     * {@link #createFilter(int, double)}) for pre processing the signal and
     * encoding it into spike trains. Before executing this function, a filter
     * must be created before calling it (Note: the coefficients of the filter
     * are multiplied by two). The following operations are executed before
     * encoding the signal into spike trains: 1 the signal is normalised between
     * 0 and 1. 3 To avoid lost of data due to the convolution a vector with
     * n-filter order elements is added at the beginning and at the end of the
     * signal. All the elements of the vector at the beginning will be equal to
     * the first value of the signal. The elements of the vector at the end of
     * the signal will be equal to the last value of the signal. 4 encodes the
     * signal into spike trains. 5 Reconstruction of the data from the spike
     * train. 6 Removing the extra information from the reconstructed data and
     * from the spike train that is related to the convolution processes. 7
     * Calculate the root mean square error between the normalised data and the
     * reconstructed data after removing unnecessary information. Steps 5,6,and
     * 7 are used for assessing the accuracy of the encoding process.
     *
     * @param vecSignal the signal to encode into spike trains
     * @return a vector of one and zeros representing a spike train
     */
    @Override
    public double[] encode(double[] vecSignal) {
        if (!this.filterCreated) {
            throw new NeuCubeRuntimeException("No filter has been created.");
        }
        Matrix signal = new Matrix(vecSignal.length, 1);
        signal.setCol(0, vecSignal);

        // Step 1 normalisation of the signal
        double min = signal.min();
        Matrix normal = signal.operation('-', min);
        normal = normal.operation('/', normal.max());

        // Step 3 avoiding lost of data due to the convolution
        Matrix startVector = new Matrix(filter.length, 1, normal.get(0, 0));
        Matrix endVector = new Matrix(filter.length, 1, normal.get(normal.getRows() - 1, 0));
        Matrix extendedVector = Matrix.vertcat(startVector, normal, endVector);

        // Step 4 encoding the normalised and extended signal
        double[] spikeTrain = this.getEncodedSignal(extendedVector.getVecCol(0), filter, threshold);

        // Step 5 reconstruction of the data using the resultant spike train after encoding the normalised and extended signal
        double[] reconstructedSignal = this.reconstructSignal(spikeTrain, filter);
        // Step 7a Removing the data related to the extension of the normalised data
        reconstructedSignal = Arrays.copyOfRange(reconstructedSignal, filter.length, reconstructedSignal.length - ((2 * filter.length) - 1));
        // Step 7b Removing the spike train data related to the extension of the normalised data
        spikeTrain = Arrays.copyOfRange(spikeTrain, filter.length, spikeTrain.length - filter.length);
        // Step 7 calculating the root mean square error with the original normalised data 
        double rmse = Util.getRMSE(normal.getVecCol(0), reconstructedSignal);
        //System.out.println("RMSE " + rmse);
        LOGGER.info("RMSE between the original signal and the reconstructed: " + rmse);
        return spikeTrain;
    }

    /**
     * Creates a FIR low pass filter using a specified filter order and a filter
     * cutoff value, and set the current filter.
     *
     * @param filterOrder the order of the low pass filter
     * @param filterCutoff the cutoff frequency between 0 (exclusive) and 1
     * (inclusive)
     */
    public void createFilter(int filterOrder, double filterCutoff) {
        this.filter = this.fir1(filterOrder, filterCutoff);
        this.filterCreated = true;
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Encodes the signal using the BSA algorithm.
     *
     * @param inputSignal
     * @param filter
     * @param threshold
     * @return
     */
    public double[] getEncodedSignal(double[] inputSignal, double[] filter, double threshold) {
        int filterSize = filter.length;
        int inputSize = inputSignal.length;
        double[] encodedSignal = new double[inputSize];

        for (int i = 0; i < inputSize; i++) {
            double error1 = 0.0;
            double error2 = 0.0;
            for (int j = 0; j < filterSize; j++) {
                if ((i + j) < inputSize) {
                    error1 = error1 + Math.abs(inputSignal[i + j] - filter[j]);
                    error2 = error2 + Math.abs(inputSignal[i + j]);
                }
            }
            if (error1 <= (error2 - threshold)) {
                encodedSignal[i] = 1.0;
                for (int j = 0; j < filterSize; j++) {
                    if ((i + j) < inputSize) {
                        inputSignal[i + j] = inputSignal[i + j] - filter[j];
                    }
                }
            } else {
                encodedSignal[i] = 0.0;
            }
        }
        return encodedSignal;
    }

    /**
     * This function reconstruct the original by convolving a spike train and
     * the corresponding filter.
     *
     * @param spikeTrain the spike train to reconstruct
     * @param filter the coefficients of the Window-based FIR filter (lowpass
     * with Hamming window)
     * @return the reconstructed data
     */
    public double[] reconstructSignal(double[] spikeTrain, double[] filter) {
        double[] signal = Util.conv(spikeTrain, filter);
        return signal;
    }

    /**
     * This function uses a Hamming window to design an nth-order lowpass FIR
     * filter with linear phase with cutoff frequency fc. The cutoff frequency
     * is the frequency at which the normalized gain of the filter is –6 dB. Cut
     * off frequency, it must be strictly greater than 0 and strictly smaller
     * than 0.5, where 0.5 corresponds to the Nyquist frequency: 0 < Wn < 0.5.
     * The Nyquist frequency is half the sample rate or π rad/sample.
     * See http://www.labbookpages.co.uk/audio/firWindowing.html
     *
     * @param order Filter order, specified as an integer scalar.
     * @param fc Filter cutoff frequency (0,0.5).
     * @return the filter's coefficients
     */
    public double[] fir1(int order, double fc) {
        if ((fc <= 0) || (fc >= 0.5)) {
            throw new IllegalArgumentException("The filter cutoff frequency must between 0 (exclusive) and 0.5 (inclusive).");
        }
        double fs = 1.0;  // sampling frequency 
        double[] fir = this.getLowPassFIR(order, fc, fs);
        double[] windowingFIR = this.windowHamming(fir);
        return windowingFIR;
    }

    /**
     * Creates a low pass filter impulse response.
     * http://www.labbookpages.co.uk/audio/firWindowing.html
     *
     * @param order Filter order.
     * @param fc Filter cutoff frequency.
     * @param fs Filter sampling rate.
     * @return The FIR filter.
     */
    public double[] getLowPassFIR(final int order, final double fc, final double fs) {
        final double ft = fc / fs;  // normalised transition frequency
        final double[] fir = new double[order + 1];
        final double half = order / 2.0;
        for (int x = 0; x < fir.length; x++) {
            if (x != half) {
                fir[x] = Math.sin(2 * Math.PI * ft * (x - half)) / (Math.PI * (x - half));
            } else {
                fir[x] = 2.0 * ft;
            }
        }
        return fir;
    }

    /**
     * Applies a Hamming window to the given FIR.
     *
     * @param fir The FIR filter.
     * @return The windowed FIR filter.
     */
    public double[] windowHamming(final double[] fir) {
        final int m = fir.length - 1;
        for (int x = 0; x < fir.length; x++) {
            fir[x] *= 0.54 - 0.46 * Math.cos((2.0 * Math.PI * x) / m);
        }
        return fir;
    }

    /**
     * @return the numFilters
     */
    public int getNumFilters() {
        return numFilters;
    }

    /**
     * This function set the number of filters to be used for encoding a data
     * sample. If the number of filters is equals to 1 then all the features of
     * the sample will be encoded using the same filter, otherwise, if the
     * number of filters is greater than 1 then the number of filters must be
     * the same as the number of features of the data.
     *
     * @param numFilters the numFilters to set
     */
    public void setNumFilters(int numFilters) {
        this.numFilters = numFilters;
    }

    /**
     * @return the thresholdVec
     */
    public double[] getThresholdVec() {
        return thresholdVec;
    }

    /**
     * @param thresholdVec the thresholdVec to set
     */
    public void setThresholdVec(double[] thresholdVec) {
        this.thresholdVec = thresholdVec;
        this.threshold = thresholdVec[0];
    }

    /**
     * @return the filterOrderVec
     */
    public int[] getFilterOrderVec() {
        return filterOrderVec;
    }

    /**
     * @param filterOrderVec the filterOrderVec to set
     */
    public void setFilterOrderVec(int[] filterOrderVec) {
        this.filterOrderVec = filterOrderVec;
    }

    /**
     * @return the filterCutoffVec
     */
    public double[] getFilterCutoffVec() {
        return filterCutoffVec;
    }

    /**
     * @param filterCutoffVec the filterCutoffVec to set
     */
    public void setFilterCutoffVec(double[] filterCutoffVec) {
        this.filterCutoffVec = filterCutoffVec;
    }

    @Override
    public String toString() {
        return "BSA algorithm";
    }
    
    public static void main(String args[]) {
        BSAalgorithm bsa = new BSAalgorithm();
        bsa.setThresholdVec(new double[]{1.51});
        bsa.setFilterOrderVec(new int[]{29});
        bsa.setFilterCutoffVec(new double[]{0.06275}); //0.1255      
        bsa.createFilter(20, 0.23);
        //bsa.createFilter(20, 0.23);
        Util.printHorzArray(bsa.filter);
        try {
            //Matrix signal = new Matrix("C:\\DataSets\\Earthquake_precursor_data\\bubble_temperature\\tar_class.csv", ",");
            Matrix signal = new Matrix("C:\\DataSets\\Earthquake_precursor_data\\bubble_temperature\\sample1796.csv", ",");
            double[] spikeTrain = bsa.encode(signal.getVecCol(0));
            for (int i = 0; i < spikeTrain.length; i++) {
                System.out.println(spikeTrain[i]);
            }
        } catch (IOException ex) {
            System.out.println("Error "+ex.getMessage());            
        }
    }

}
