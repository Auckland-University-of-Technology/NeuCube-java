/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.network;

import java.util.ArrayList;
import jneucube.spikingNeurons.SpikingNeuron;
import jneucube.spikingNeurons.Synapse;


/**
 *
 * @author Josafath Israel Espinosa Ramos
 */
public class Network {

    /* Functional properties */    
    private ArrayList<SpikingNeuron> inputNeurons = new ArrayList<>();    // Some neurons of the reservoir representing the features
    private ArrayList<SpikingNeuron> outputNeurons = new ArrayList<>();
    private ArrayList<SpikingNeuron> reservoir = new ArrayList<>();
    private ArrayList<SpikingNeuron> inputNeuronsPositive = new ArrayList<>(); // Added in June 2016
    private ArrayList<SpikingNeuron> inputNeuronsNegative = new ArrayList<>();  // Added in June 2016    
    private ArrayList<SpikingNeuron> firedNeurons = new ArrayList<>();    // Once the network is stimulated, the fired neurons are listed here
    private ArrayList<Integer> changedWeights = new ArrayList<>();
    private ArrayList<Synapse> synapses = new ArrayList<>();
    private boolean train = false;      // This flag determines whether the SNN should be trained or just propagating spike trains. This flag should be tunred on or off before calling the "train" or "propagateSample" of the NetworkController class
    
    
    /* Configuration properties */
    private SpikingNeuron spikingNeuron = new SpikingNeuron();
    private boolean allowInhibitoryInputNeurons = true; // If the value of this field is set to true then the network allow negative input neurons that process negative spikes
    private int numVariables = 1;
    private int numInputs = 0;      // Depends on the data 
    private int numNeuronsX = 5;
    private int numNeuronsY = 5;
    private int numNeuronsZ = 5;    

    /**
     * @return the spikingNeuron
     */
    public SpikingNeuron getSpikingNeuron() {
        return spikingNeuron;
    }

    /**
     * @param spikingNeuron the spikingNeuron to set
     */
    public void setSpikingNeuron(SpikingNeuron spikingNeuron) {
        this.spikingNeuron = spikingNeuron;
    }

    /**
     * @return the inputNeurons
     */
    public ArrayList<SpikingNeuron> getInputNeurons() {
        return inputNeurons;
    }

    /**
     * @param inputNeurons the inputNeurons to set
     */
    public void setInputNeurons(ArrayList<SpikingNeuron> inputNeurons) {
        this.inputNeurons = inputNeurons;
    }

    /**
     * @return the outputNeurons
     */
    public ArrayList<SpikingNeuron> getOutputNeurons() {
        return outputNeurons;
    }

    /**
     * @param outputNeurons the outputNeurons to set
     */
    public void setOutputNeurons(ArrayList<SpikingNeuron> outputNeurons) {
        this.outputNeurons = outputNeurons;
    }

    /**
     * @return the reservoir
     */
    public ArrayList<SpikingNeuron> getReservoir() {
        return reservoir;
    }

    /**
     * @param reservoir the reservoir to set
     */
    public void setReservoir(ArrayList<SpikingNeuron> reservoir) {
        this.reservoir = reservoir;
    }

    /**
     * @return the inputNeuronsPositive
     */
    public ArrayList<SpikingNeuron> getInputNeuronsPositive() {
        return inputNeuronsPositive;
    }

    /**
     * @param inputNeuronsPositive the inputNeuronsPositive to set
     */
    public void setInputNeuronsPositive(ArrayList<SpikingNeuron> inputNeuronsPositive) {
        this.inputNeuronsPositive = inputNeuronsPositive;
    }

    /**
     * @return the inputNeuronsNegative
     */
    public ArrayList<SpikingNeuron> getInputNeuronsNegative() {
        return inputNeuronsNegative;
    }

    /**
     * @param inputNeuronsNegative the inputNeuronsNegative to set
     */
    public void setInputNeuronsNegative(ArrayList<SpikingNeuron> inputNeuronsNegative) {
        this.inputNeuronsNegative = inputNeuronsNegative;
    }

    /**
     * @return the firedNeurons
     */
    public ArrayList<SpikingNeuron> getFiredNeurons() {
        return firedNeurons;
    }

    /**
     * @param firedNeurons the firedNeurons to set
     */
    public void setFiredNeurons(ArrayList<SpikingNeuron> firedNeurons) {
        this.firedNeurons = firedNeurons;
    }

    /**
     * @return the changedWeights
     */
    public ArrayList<Integer> getChangedWeights() {
        return changedWeights;
    }

    /**
     * @param changedWeights the changedWeights to set
     */
    public void setChangedWeights(ArrayList<Integer> changedWeights) {
        this.changedWeights = changedWeights;
    }

    /**
     * @return the synapses
     */
    public ArrayList<Synapse> getSynapses() {
        return synapses;
    }

    /**
     * @param synapses the synapses to set
     */
    public void setSynapses(ArrayList<Synapse> synapses) {
        this.synapses = synapses;
    }


    /**
     * @return the allowInhibitoryInputNeurons
     */
    public boolean isAllowInhibitoryInputNeurons() {
        return allowInhibitoryInputNeurons;
    }

    /**
     * @param allowInhibitoryInputNeurons the allowInhibitoryInputNeurons to set
     */
    public void setAllowInhibitoryInputNeurons(boolean allowInhibitoryInputNeurons) {
        this.allowInhibitoryInputNeurons = allowInhibitoryInputNeurons;
    }

    /**
     * @return the numVariables
     */
    public int getNumVariables() {
        return numVariables;
    }

    /**
     * @param numVariables the numVariables to set
     */
    public void setNumVariables(int numVariables) {
        this.numVariables = numVariables;
    }

    /**
     * @return the numInputs
     */
    public int getNumInputs() {
        return numInputs;
    }

    /**
     * @param numInputs the numInputs to set
     */
    public void setNumInputs(int numInputs) {
        this.numInputs = numInputs;
    }

    /**
     * @return the numNeuronsX
     */
    public int getNumNeuronsX() {
        return numNeuronsX;
    }

    /**
     * @param numNeuronsX the numNeuronsX to set
     */
    public void setNumNeuronsX(int numNeuronsX) {
        this.numNeuronsX = numNeuronsX;
    }

    /**
     * @return the numNeuronsY
     */
    public int getNumNeuronsY() {
        return numNeuronsY;
    }

    /**
     * @param numNeuronsY the numNeuronsY to set
     */
    public void setNumNeuronsY(int numNeuronsY) {
        this.numNeuronsY = numNeuronsY;
    }

    /**
     * @return the numNeuronsZ
     */
    public int getNumNeuronsZ() {
        return numNeuronsZ;
    }

    /**
     * @param numNeuronsZ the numNeuronsZ to set
     */
    public void setNumNeuronsZ(int numNeuronsZ) {
        this.numNeuronsZ = numNeuronsZ;
    }

    /**
     * @return the numSynapses
     */
    public int getNumSynapses() {
        return this.synapses.size();
    }

    /**
     * @return the train
     */
    public boolean isTrain() {
        return train;
    }

    /**
     * @param train the train to set
     */
    public void setTrain(boolean train) {
        this.train = train;
    }

    public int getNumberOfNeurons() {
        return this.reservoir.size();
    }
}
