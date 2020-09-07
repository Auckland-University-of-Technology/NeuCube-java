/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.trainingAlgorithms;

import java.util.ArrayList;
import java.util.Arrays;
import jneucube.data.DataSample;
import jneucube.data.SpatioTemporalData;
import jneucube.network.Network;
import jneucube.spikingNeurons.SpikingNeuron;
import jneucube.trainingAlgorithms.unsupervised.NRDP;
import jneucube.trainingAlgorithms.unsupervised.STDP;
import jneucube.trainingAlgorithms.supervised.SPAN;
import jneucube.trainingAlgorithms.supervised.deSNNm;
import jneucube.trainingAlgorithms.supervised.deSNNs;
import jneucube.trainingAlgorithms.supervised.deSNNsAdaptive;
import jneucube.trainingAlgorithms.unsupervised.EPUSSS;
import jneucube.trainingAlgorithms.unsupervised.OnlineSTDP;
import jneucube.trainingAlgorithms.unsupervised.STDPH;
import jneucube.trainingAlgorithms.unsupervised.STDP_DELAYS;
import jneucube.trainingAlgorithms.unsupervised.STDP_IMAGES;
import jneucube.trainingAlgorithms.unsupervised.STDP_MATLAB;

/**
 *
 * @author Josafath Israel Espinosa Ramos (Centro de Investigacion en
 * Computacion, Instituto Politecnico Nacional, Mexico 2015)
 */
public abstract class LearningAlgorithm {

    public static final STDP STDP = new STDP();
    public static final OnlineSTDP ONLINE_STDP = new OnlineSTDP();
    public static final STDPH STDPH = new STDPH();
    public static final STDP_DELAYS STDP_DELAYS = new STDP_DELAYS();
    public static final STDP_MATLAB STDP_MATLAB = new STDP_MATLAB();
    public static final EPUSSS EPUSSS = new EPUSSS();
    public static final STDP_IMAGES STDP_IMAGES = new STDP_IMAGES();

    public static final NRDP NRDP = new NRDP();
    public static final deSNNs DE_SNN_S = new deSNNs();
    public static final deSNNm DE_SNN_M = new deSNNm();
    public static final deSNNsAdaptive DE_SNN_ADAPTIVE = new deSNNsAdaptive();
    public static final SPAN SPAN = new SPAN();

    public static final ArrayList<LearningAlgorithm> UNSUPERVISED_LEARNING_ALGORITHM_LIST = new ArrayList<>(Arrays.asList(STDP));
    public static final ArrayList<LearningAlgorithm> ONLINE_UNSUPERVISED_LEARNING_ALGORITHM_LIST = new ArrayList<>(Arrays.asList(ONLINE_STDP, STDP_IMAGES));
    public static final ArrayList<LearningAlgorithm> SUPERVISED_LEARNING_ALGORITHM_LIST = new ArrayList<>(Arrays.asList(DE_SNN_S, DE_SNN_ADAPTIVE));

    private int trainingTime;
    private boolean savingWeightMode = false;
    private int trainingRounds = 1;
    private boolean executed = false;

    private int weightChanges = 0;  // Indicates the number of weigths that have changed during one time step of the learning algorithm
    private int sampleWeightChanges = 0;    // Indicates the number of weigths that have changed after training one data sample
    private int trainingWeightChanges = 0;  // Indicates the number of weigths that have changed during the whole trainig process (all data samples).
    private ArrayList<Integer> weightChangeList = new ArrayList<>();
    private boolean saveWeightChanges = false;

    private LearningAlgorithmStatistics statistics = new LearningAlgorithmStatistics();

    public abstract void train(Network network, ArrayList<DataSample> trainingData);

    public void train(Network network, DataSample sample) {
    }

    public abstract void updateSynapticWeights(ArrayList<SpikingNeuron> firedNeurons, int elapsedTime);

    public abstract void validate(Network network, SpatioTemporalData std);

    public abstract void resetFieldsForTraining();

    public abstract void resetFieldsForSample();

    /**
     * @return the trainingTime
     */
    public int getTrainingTime() {
        return trainingTime;
    }

    /**
     * @param trainingTime the trainingTime to set
     */
    public void setTrainingTime(int trainingTime) {
        this.trainingTime = trainingTime;
    }

    public void increaseTrainingTime() {
        this.trainingTime++;
    }

    public void increaseTrainingTime(int steps) {
        this.trainingTime += steps;
    }

    /**
     * @return the trainingRounds
     */
    public int getTrainingRounds() {
        return trainingRounds;
    }

    /**
     * @param trainingRounds the trainingRounds to set
     */
    public void setTrainingRounds(int trainingRounds) {
        this.trainingRounds = trainingRounds;
    }

    /**
     * @return the executed
     */
    public boolean isExecuted() {
        return executed;
    }

    /**
     * @param executed the executed to set
     */
    public void setExecuted(boolean executed) {
        this.executed = executed;
    }

    /**
     * @return the statistics
     */
    public LearningAlgorithmStatistics getStatistics() {
        return statistics;
    }

    /**
     * @param statistics the statistics to set
     */
    public void setStatistics(LearningAlgorithmStatistics statistics) {
        this.statistics = statistics;
    }

    /**
     * @return the savingWeightMode
     */
    public boolean isSavingWeightMode() {
        return savingWeightMode;
    }

    /**
     * @param savingWeightMode the savingWeightMode to set
     */
    public void setSavingWeightMode(boolean savingWeightMode) {
        this.savingWeightMode = savingWeightMode;
    }

    /**
     * @return the weightChangeList
     */
    public ArrayList<Integer> getWeightChangeList() {
        return weightChangeList;
    }

    /**
     * @param weightChangeList the weightChangeList to set
     */
    public void setWeightChangeList(ArrayList<Integer> weightChangeList) {
        this.weightChangeList = weightChangeList;
    }

    /**
     * @return the weightChanges
     */
    public int getWeightChanges() {
        return weightChanges;
    }

    /**
     * @param weightChanges the weightChanges to set
     */
    public void setWeightChanges(int weightChanges) {
        this.weightChanges = weightChanges;
    }

    public void increaseWeightChanges() {
        this.weightChanges++;
    }
    
    public void resetWeightChanges(){
        this.weightChanges=0;
    }

    /**
     * @return the saveWeightChanges
     */
    public boolean isSaveWeightChanges() {
        return saveWeightChanges;
    }

    /**
     * @param saveWeightChanges the saveWeightChanges to set
     */
    public void setSaveWeightChanges(boolean saveWeightChanges) {
        this.saveWeightChanges = saveWeightChanges;
    }

    /**
     * @return the sampleWeightChanges
     */
    public int getSampleWeightChanges() {
        return sampleWeightChanges;
    }

    /**
     * @param sampleWeightChanges the sampleWeightChanges to set
     */
    public void setSampleWeightChanges(int sampleWeightChanges) {
        this.sampleWeightChanges = sampleWeightChanges;
    }

    public void increaseSampleWeightChanges() {
        this.sampleWeightChanges++;
    }

    public void increaseSampleWeightChanges(int numChanges) {
        this.sampleWeightChanges += numChanges;
    }
    
    public void resetSampleWeightChanges(){
        this.sampleWeightChanges=0;
    }

    /**
     * @return the trainingWeightChanges
     */
    public int getTrainingWeightChanges() {
        return trainingWeightChanges;
    }

    /**
     * @param trainingWeightChanges the trainingWeightChanges to set
     */
    public void setTrainingWeightChanges(int trainingWeightChanges) {
        this.trainingWeightChanges = trainingWeightChanges;
    }
    
    public void increaseTrainingWeightChanges() {
        this.trainingWeightChanges++;
    }

    public void increaseTrainingWeightChanges(int numChanges) {
        this.trainingWeightChanges += numChanges;
    }    

}
