/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.spikingNeurons;

import java.util.ArrayList;
import java.util.Random;


/**
 *
 * @author Josafath Israel Espinosa Ramos (Centro de Investigacion en
 * Computacion, Instituto Politecnico Nacional, Mexico 2015)
 */
public class Synapse {

    public static int EXCITATORY = 1;     // 2017/04/11, JIER, Defines the type of synapse (change id 1)
    public static int INHIBITORY = -1;    // 2017/04/11, JIER, Defines the type of synapse (change id 1)

    private int targetNeuronIdx = -1;     // The index of the presynaptic neuron
    private int sourceNeuronIdx = -1;     // The index of the postsynaptic neuron
    private double weight;          // The current synaptic weight
    private ArrayList<Double> weights = new ArrayList<>();
    private int lastUpdate = 0;     // The last time the synapse was modified or updated
    private boolean withinRange = false;  // This is used for visiualization
    private int idx = 0;            // The index of the synapse
    private int displayIdx = 0;     // The index of the synapse in the display  // AUT
    private NeuroReceptor AMPAR;
    private NeuroReceptor NMDAR;
    private NeuroReceptor GABAAR;
    private NeuroReceptor GABABR;
    private int stimuli = 0;
    private int type = EXCITATORY;    // 2017/04/11, JIER, Defines the type of synapse (change id 1)
    private int delay = 1;

    //Random rGABA = new Random();
    public Synapse() {

    }

    /**
     * Create a connection (pre or post synapse) between two neurons
     *
     * @param sourceNeuron The pre-synaptic neuron for pre-synapses and the
     * post-synaptic neuron in post-synapses
     * @param targetNeuron The end of the synapse opposite to the source neuron
     * @param weight The initial weight
     */
    public Synapse(SpikingNeuron sourceNeuron, SpikingNeuron targetNeuron, Double weight) {        
        this.sourceNeuronIdx = sourceNeuron.getIdx();
        this.targetNeuronIdx = targetNeuron.getIdx();
        this.weight = weight;
        this.weights.add(weight);
    }

    /**
     * Create a connection (pre or post synapse) between to neurons It randomly
     * initializes the synaptic weight between 0 and 1
     *
     * @param sourceNeuron The pre-synaptic neuron for pre-synapses and the
     * post-synaptic neuron in post-synapses
     * @param targetNeuron
     */
    public Synapse(SpikingNeuron sourceNeuron, SpikingNeuron targetNeuron) {
        this.sourceNeuronIdx = sourceNeuron.getIdx();
        this.targetNeuronIdx = targetNeuron.getIdx();
        this.weight = new Random().nextDouble();
        this.weights.add(weight);
    }

    /**
     * This function sets the functional properties of the synapse to their
     * initial state, i.e. after creation of the connection and before training.
     */
    public void reset() {
        this.weight = this.weights.get(0);
        this.getWeights().clear();
        this.addWeight(this.weight);
        this.lastUpdate = 0;
        this.withinRange = false;
        this.stimuli = 0;
    }



    public void addStimuli() {
        this.stimuli++;
    }

    /**
     * The target neuron connected to this synapse receives a spike. The amount
     * of current "released" in that neurons will increase (more positive or
     * more negative) depending on the type of synapse (excitatory or
     * inhibitory).
     *
     * @param reservoir All the neurons in the spiking neural network
     */
    public void releaseSpike(ArrayList<SpikingNeuron> reservoir) {
        //System.out.println(this.sourceNeuronIdx+"->"+this.targetNeuronIdx+": "+this.weight);
        reservoir.get(this.getTargetNeuronIdx()).receiveSpike(this.weight * this.type); // After 
    }

    /**
     * Adds the current weight to the list
     */
    public void addWeight() {
        this.weights.add(this.weight);
    }

    public void addWeight(double weight) {
        this.weights.add(weight);
    }

    public double getNextRandomGABAA() {
        return new Random().nextDouble();
        //return rGABA.nextDouble();
    }

    /**
     * @return the weights
     */
    public ArrayList<Double> getWeights() {
        return weights;
    }

    /**
     * @param weights the weight to set
     */
    public void setWeights(ArrayList<Double> weights) {
        this.weights = weights;
    }

    /**
     * @return the lastUpdate
     */
    public int getLastUpdate() {
        return lastUpdate;
    }

    /**
     * @param lastUpdate the lastUpdate to set
     */
    public void setLastUpdate(int lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    /**
     * @return the weight
     */
    public double getWeight() {
        return weight;
    }

    /**
     * @param weight the weight to set
     */
    public void setWeight(double weight) {
        this.weight = weight;
    }

    /**
     * @return the withinRange
     */
    public boolean isWithinRange() {
        return withinRange;
    }

    /**
     * @param withinRange the withinRange to set
     */
    public void setWithinRange(boolean withinRange) {
        this.withinRange = withinRange;
    }

    /**
     * @return the idx
     */
    public int getIdx() {
        return idx;
    }

    /**
     * @param idx the idx to set
     */
    public void setIdx(int idx) {
        this.idx = idx;
    }

    /**
     * @return the AMPAR
     */
    public NeuroReceptor getAMPAR() {
        return AMPAR;
    }

    /**
     * @param AMPAR the AMPAR to set
     */
    public void setAMPAR(NeuroReceptor AMPAR) {
        this.AMPAR = AMPAR;
    }

    /**
     * @return the NMDAR
     */
    public NeuroReceptor getNMDAR() {
        return NMDAR;
    }

    /**
     * @param NMDAR the NMDAR to set
     */
    public void setNMDAR(NeuroReceptor NMDAR) {
        this.NMDAR = NMDAR;
    }

    /**
     * @return the GABAAR
     */
    public NeuroReceptor getGABAAR() {
        return GABAAR;
    }

    /**
     * @param GABAAR the GABAAR to set
     */
    public void setGABAAR(NeuroReceptor GABAAR) {
        this.GABAAR = GABAAR;
    }

    /**
     * @return the GABABR
     */
    public NeuroReceptor getGABABR() {
        return GABABR;
    }

    /**
     * @param GABABR the GABABR to set
     */
    public void setGABABR(NeuroReceptor GABABR) {
        this.GABABR = GABABR;
    }

    public void printMolecules() {
        System.out.print(this.sourceNeuronIdx + " -> " + this.targetNeuronIdx + " : w=" + this.getWeight());
        System.out.print(" A=" + this.getAMPAR().getLevel());
        System.out.print(" N=" + this.getNMDAR().getLevel());
        System.out.print(" GA=" + this.getGABAAR().getLevel());
        System.out.print(" GB=" + this.getGABABR().getLevel());
        System.out.println("");
    }

    /**
     * @return the stimuli
     */
    public int getStimuli() {
        return stimuli;
    }

    /**
     * @param stimuli the stimuli to set
     */
    public void setStimuli(int stimuli) {
        this.stimuli = stimuli;
    }

    /**
     * Increases the weight
     *
     * @param value
     */
    public void increaseWeight(double value) {
        this.weight += value;
    }

    public void decreaseWeight(double value) {
        this.weight -= value;
    }

    /**
     * @return the displayIdx
     */
    public int getDisplayIdx() {
        return displayIdx;
    }

    /**
     * @param displayIdx the displayIdx to set
     */
    public void setDisplayIdx(int displayIdx) {
        this.displayIdx = displayIdx;
    }

    /**
     * @return the targetNeuronIdx
     */
    public int getTargetNeuronIdx() {
        return targetNeuronIdx;
    }

    /**
     * @param targetNeuronIdx the targetNeuronIdx to set
     */
    public void setTargetNeuronIdx(int targetNeuronIdx) {
        this.targetNeuronIdx = targetNeuronIdx;
    }

    /**
     * @return the sourceNeuronIdx
     */
    public int getSourceNeuronIdx() {
        return sourceNeuronIdx;
    }

    /**
     * @param sourceNeuronIdx the sourceNeuronIdx to set
     */
    public void setSourceNeuronIdx(int sourceNeuronIdx) {
        this.sourceNeuronIdx = sourceNeuronIdx;
    }

    /**
     * @return the type
     */
    public int getType() {
        return type;
    }

    /**
     * @param type the type of synapse Synapse.EXCITATORY (1),
     * Synapse.INHIBITORY (-1)
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * @return the delay
     */
    public int getDelay() {
        return delay;
    }

    /**
     * @param delay the delay to set
     */
    public void setDelay(int delay) {
        this.delay = delay;
    }

}
