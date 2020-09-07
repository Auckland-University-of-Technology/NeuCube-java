/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.crossValidation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import jneucube.data.DataSample;

/**
 *
 * @author Josafath Israel Espinosa Ramos
 */
public class LeavePOut extends CrossValidation {

    private double trainingRate = 0.0;
    private int numExperiments = 1;
    

    @Override
    public void split(HashMap<Double, ArrayList<DataSample>> dataClasses) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * @return the trainingRate
     */
    public double getTrainingRate() {
        return trainingRate;
    }

    /**
     * @param trainingRate the trainingRate to set
     */
    public void setTrainingRate(double trainingRate) {
        this.trainingRate = trainingRate;
    }

    public void run(HashMap<Double, ArrayList<DataSample>> trainingDataClasses, int numTrainingSamples, HashMap<Double, ArrayList<DataSample>> validationDataClasses, int numValidationSamples) {
        this.folds.clear();
//        this.setNumClasses(trainingDataClasses.size());
        ArrayList<DataSample> trainingSamples;
        ArrayList<DataSample> validationSamples;
        this.setNumTrainingSamples(numTrainingSamples * trainingDataClasses.size());
        this.setNumValidationSamples(numValidationSamples * validationDataClasses.size());
        for (int i = 0; i < this.numExperiments; i++) {
            trainingSamples = this.selectRandomSamples(trainingDataClasses, numTrainingSamples);
            validationSamples = this.selectRandomSamples(validationDataClasses, numValidationSamples);
            trainingSamples.addAll(validationSamples);
            this.folds.put(i, trainingSamples);
        }
    }

    public ArrayList<DataSample> selectRandomSamples(HashMap<Double, ArrayList<DataSample>> dataClasses, int samplesPerClass) {
        Random random = new Random();
        ArrayList<DataSample> samples = new ArrayList<>();
        ArrayList<DataSample> temp = new ArrayList<>();
        int idx;
        for (Map.Entry<Double, ArrayList<DataSample>> entry : dataClasses.entrySet()) {
            temp = entry.getValue();
            for (int i = 0; i < samplesPerClass; i++) {
                idx = random.nextInt(temp.size());
                samples.add(temp.get(idx));
            }
        }
        return samples;
    }

    /**
     * Selects the most recent events
     *
     * @param dataClasses
     * @param samplesPerClass
     * @return
     */
    public ArrayList<DataSample> selectRecentEventSamples(HashMap<Double, ArrayList<DataSample>> dataClasses, int samplesPerClass) {
        ArrayList<DataSample> samples = new ArrayList<>();
        ArrayList<DataSample> temp = new ArrayList<>();
        for (Map.Entry<Double, ArrayList<DataSample>> entry : dataClasses.entrySet()) {
            temp = entry.getValue();
            samples.addAll(temp.subList(temp.size() - samplesPerClass, temp.size()));
        }
        return samples;
    }

    /**
     *
     * @param dataClasses
     * @param samplesPerClass
     * @return
     */
    public ArrayList<DataSample> selectEarlierEventSamples(HashMap<Double, ArrayList<DataSample>> dataClasses, int samplesPerClass) {
        ArrayList<DataSample> samples = new ArrayList<>();
        ArrayList<DataSample> temp = new ArrayList<>();
        for (Map.Entry<Double, ArrayList<DataSample>> entry : dataClasses.entrySet()) {
            temp = entry.getValue();
            samples.addAll(temp.subList(0, samplesPerClass));
        }
        return samples;
    }

    @Override
    public ArrayList<DataSample> getTrainingData(int numFold) {
        ArrayList<DataSample> samples = new ArrayList<>();
        ArrayList<DataSample> temp = this.folds.get(numFold);
        samples.addAll(temp.subList(0, this.getNumTrainingSamples()));
        return samples;
    }

    @Override
    public ArrayList<DataSample> getValidationData(int numFold) {
        ArrayList<DataSample> samples = new ArrayList<>();
        ArrayList<DataSample> temp = this.folds.get(numFold);
        samples.addAll(temp.subList(this.getNumTrainingSamples(), temp.size()));
        return samples;
    }

    /**
     * @return the numExperiments
     */
    public int getNumExperiments() {
        return numExperiments;
    }

    /**
     * @param numExperiments the numExperiments to set
     */
    public void setNumExperiments(int numExperiments) {
        this.numExperiments = numExperiments;
    }

    @Override
    public ArrayList<DataSample> getTrainingData() {
        return this.getTrainingData(this.getCurrentFold());
    }

    @Override
    public ArrayList<DataSample> getValidationData() {
        return this.getValidationData(this.getCurrentFold());
    }
}
