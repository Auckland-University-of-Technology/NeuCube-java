/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.encodingAlgorithms;

import java.io.IOException;
import java.util.ArrayList;

import jneucube.data.DataSample;
import jneucube.util.Matrix;


/**
 *
 * @author em9403
 */
public class OnlineAERalgorithm extends EncodingAlgorithm {

    private double[] spikes;
    private int numFeatures = 0;
    private double[][] preValues;   // rows 0=previous recorded values, 1=differences, 2=m1, 3=m2    
    private double alpha = 0.5; // The distance between the average and the standard deviation of temporal data changes (the same as the spikeThresholdValue in the AER algorithm)
    private long time = 1;
    double threshold;

    @Override
    public void encode(ArrayList<DataSample> referenceData, ArrayList<DataSample> targetData, int startTime, int endTime) {

    }

    @Override
    public double[] encode(double[] newRecord) {
        if (time == 1) {
            this.preValues[0] = newRecord;
        } else {
            for (int i = 0; i < this.numFeatures; i++) {
                this.preValues[1][i] = Math.abs(newRecord[i] - this.preValues[0][i]);   // Difference
                this.preValues[2][i] = 1.0 / this.time * (this.preValues[1][i] + (time - 1) * this.preValues[2][i]); // Calculating the first recursive moment (mean)
                this.preValues[3][i] = 1.0 / this.time * ((this.preValues[1][i] * this.preValues[1][i]) + (time - 1) * this.preValues[3][i]); // Calculating the second recursive moment (variance)            
                threshold = (this.preValues[2][i] + Math.sqrt(this.preValues[3][i])) * alpha;
                if ((newRecord[i] - this.preValues[0][i]) > threshold) {
                    spikes[i] = 1;
                } else if ((newRecord[i] - this.preValues[0][i]) < -threshold) {
                    spikes[i] = -1;
                } else {
                    spikes[i] = 0;
                }
            }
        }
        this.time++;
        this.preValues[0] = newRecord;

        return spikes;
    }

    /**
     * @return the numFeatures
     */
    public int getNumFeatures() {
        return numFeatures;
    }

    /**
     * @param numFeatures the numFeatures to set
     */
    public void setNumFeatures(int numFeatures) {
        this.numFeatures = numFeatures;
        this.preValues = new double[4][numFeatures];   // rows 0=previous recorded values, 1=differences, 2=m1, 3=m2
        this.spikes = new double[numFeatures];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < numFeatures; j++) {
                this.preValues[i][j] = 0;
            }
        }
    }

    /**
     * @return the time
     */
    public long getTime() {
        return time;
    }

    /**
     * @param time the time to set
     */
    public void setTime(long time) {
        this.time = time;
    }

    /**
     * @return the alpha
     */
    public double getAlpha() {
        return alpha;
    }

    /**
     * @param alpha the alpha to set
     */
    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    @Override
    public String toString() {
        return "Online thresholding representation";
    }

    public static void main(String args[]) {
        try {
            OnlineAERalgorithm encoder = new OnlineAERalgorithm();
            String file = "E:/D/KEDRI Projects/Seismic/data/seismic_52ch_2010-2013/sam1_2010.csv";
            encoder.setNumFeatures(52);
            Matrix data = new Matrix(file, ",");
            double[] spikes;
            int posCounter = 0;
            int negCounter = 0;
            for (int i = 0; i < data.getRows(); i++) {
                spikes = encoder.encode(data.getVecRow(i));

                if (spikes[3] == 1) {
                    posCounter++;
                }
                if (spikes[3] == -1) {
                    negCounter++;
                }
                for (int j = 0; j < spikes.length; j++) {
                    System.out.print(spikes[j] + " ");
                }
                System.out.println("");
            }
            System.out.println("Positive spikes " + posCounter);
            System.out.println("Negative spikes " + negCounter);
        } catch (IOException ex) {
            System.out.println("Error " + ex.getMessage());
        }
    }

    @Override
    public void clear() {
        this.spikes = null;
        this.preValues = null;
        this.time = 1;
    }

    @Override
    public void encode(DataSample sample) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
