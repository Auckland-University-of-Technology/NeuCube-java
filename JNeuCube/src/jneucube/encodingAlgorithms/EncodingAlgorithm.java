/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.encodingAlgorithms;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import jneucube.data.DataSample;
import jneucube.util.Matrix;


/**
 *
 * @author Josafath Israel Espinosa Ramos (Centro de Investigacion en
 * Computacion, Instituto Politecnico Nacional, Mexico 2015)
 */
public abstract class EncodingAlgorithm {

    public static final AERalgorithm AER = new AERalgorithm();
    public static final OnlineAERalgorithm ONLINE_AER = new OnlineAERalgorithm();
    public static final BSAalgorithm BSA=new BSAalgorithm();
    public static final StepForwardAlgorithm SF= new StepForwardAlgorithm();
    public static final TBRalgorithm TBR= new TBRalgorithm();
    public static final ArrayList<EncodingAlgorithm> ENCODING_ALGORITHM_LIST = new ArrayList<>(Arrays.asList(AER, ONLINE_AER, BSA, SF, TBR));
    
    private boolean encodingSatus = false;    // True if the training data was encoded, otherwise false. The validation data might utilize information from the training process    

    /**
     * Encodes a data set into spike trains.
     *
     * @param referenceData The array list of samples to compute the threshold
     * to produce a spike. Usually the training data
     * @param targetData The array list of samples to convert into spike trains.
     * Both, training and validation data
     * @param startTime Start point to encode
     * @param endTime End point to encode
     */
    public abstract void encode(ArrayList<DataSample> referenceData, ArrayList<DataSample> targetData, int startTime, int endTime);

    /**
     * Encodes a single data sample into spike trains. The spike trains are
     * stored in the {@link jneucube.data.DataSample#spikeData} field of the
     * sample.
     *
     * @param sample the data sample to encode
     */
    public abstract void encode(DataSample sample);

    /**
     * Encodes a vector of double values into a spike train.
     *
     * @param signal the signal to be encoded
     * @return a vector of double values with ones and zeros
     */
    public abstract double[] encode(double[] signal);

    public abstract void clear();

    public double getSpikeRate(ArrayList<DataSample> samples) {
        Matrix spikes[] = new Matrix[samples.size()];
        for (int i = 0; i < samples.size(); i++) {
            spikes[i] = samples.get(i).getSpikeData();
        }
        Matrix temp = Matrix.merge(spikes);
        temp = temp.operation('!', 0.0);
        return new Double(new DecimalFormat("#,###,###,##0.00").format(temp.sum() / temp.numel()));
    }

    /**
     * @return the encodingSatus
     */
    public boolean isEncodingSatus() {
        return encodingSatus;
    }

    /**
     * @param encodingSatus the encodingSatus to set
     */
    public void setEncodingSatus(boolean encodingSatus) {
        this.encodingSatus = encodingSatus;
    }

}
