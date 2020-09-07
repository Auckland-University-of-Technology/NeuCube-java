/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.trainingAlgorithms.supervised;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import jneucube.classifiers.Classifier;
import jneucube.data.DataSample;
import jneucube.data.SpatioTemporalData;
import jneucube.network.Network;
import jneucube.spikingNeurons.SpikingNeuron;
import jneucube.spikingNeurons.Synapse;
import jneucube.trainingAlgorithms.LearningAlgorithm;
import jneucube.util.Matrix;
import static jneucube.log.Log.LOGGER;

/**
 *
 * @author Josafath Israel Espinosa Ramos (Centro de Investigacion en
 * Computacion, Instituto Politecnico Nacional, Mexico 2015)
 */
public class deSNNs extends LearningAlgorithm {

    // Configuration fields
    private double positiveDrift = 0.005;
    private double negativeDrift = 0.005;
    private double mod = 0.8;       // modulation factor, that defines how important the order of the first spike is mod=[0,1]
    private double alpha = 1.0;     // is a learning parameter (in a partial case it is equal to 1);
    private Classifier classifier = Classifier.WKNN;

    /**
     * Set of neurons that are activated when a set of spike trains is passed
     * through the network. They are the "useful" neurons that create spiking
     * activity paths for the supervised training algorithm to classify a
     * sample.
     */
    private Set<SpikingNeuron> activeNeurons = new HashSet<>();
    // Functional fields
    /**
     * The counter of the spike order. This value is increased at every time
     * point when there is a neuron that emitted a spike for the first time.
     */
    private int intSpikeOrder = 0;

    //Logger LOGGER = LoggerFactory.getLogger(deSNNs.class);
    public deSNNs() {

    }

    public deSNNs(double drift, double mod, double alpha) {
        this.positiveDrift = drift;
        this.negativeDrift = drift;
        this.mod = mod;
        this.alpha = alpha;
    }

    @Override
    public void train(Network network, ArrayList<DataSample> trainingData) {
        LOGGER.info("------- Supervised training using deSNNs algorithm -------");

//        ArrayList<SpikingNeuron> trainingOutputNeurons = this.run(network, trainingData);
//        network.setOutputNeurons(trainingOutputNeurons);
        LOGGER.info("------- Supervised training complete -------");
    }

    /**
     *
     * @param network
     * @param sample
     */
    @Override
    public void train(Network network, DataSample sample) {
        this.updateWeights(network, sample);
    }

    @Override
    public void validate(Network network, SpatioTemporalData std) {

    }

    public Matrix getNeuronWeights(SpikingNeuron neuron) {
        Matrix matrix = new Matrix(1, neuron.getInputSynapses().size(), 0.0);
        for (int i = 0; i < neuron.getInputSynapses().size(); i++) {
            matrix.set(0, i, neuron.getInputSynapses().get(i).getWeight());
        }
        return matrix;
    }

    /**
     * This function is executed at every time point and it updates the weights
     * between the reservoir and the output neuron according to the deSNNs
     * learning rule.
     *
     * @param network
     * @param sample
     */
    public void updateWeights(Network network, DataSample sample) {
        // the output neurons are created previously, the process is over the last neuron created
        SpikingNeuron outputNeuron = network.getOutputNeurons().get(network.getOutputNeurons().size() - 1);
        double weight;
        boolean orderFlag = true;
        this.resetWeightChanges();

        SpikingNeuron neuron;
        for (Synapse synapse : outputNeuron.getInputSynapses()) {
            neuron = network.getReservoir().get(synapse.getSourceNeuronIdx());
            weight = synapse.getWeight();
            if (neuron.isFired()) {
                if (neuron.getNumSpikesEmitted() == 1) {                // If it is the first spike of the presynaptic neuron
                    if (orderFlag) {
                        this.intSpikeOrder++;
                        orderFlag = false;
                    }
                    synapse.addStimuli();                   // Adds a stimuli to the output neuron
                    weight = alpha * Math.pow(mod, this.intSpikeOrder); // Rank order learning rule 
//                    this.activeNeurons.add(neuron);                     // Add a useful neuron from the reservoir
                    this.increaseWeightChanges();                       // It represents the number of neurons that fired for the first time, then the useful neurons                    
                    this.increaseSampleWeightChanges();
                } else {                                                // If it is not the first spike, then increase the synaptic weight
                    weight += positiveDrift;                            // spike driven synaptic plasticity
                }
            } else {                                                    // If no spikes emiteed, then decrease the synpatic weight                
                if(neuron.getNumSpikesEmitted()> 0){
                    weight-=negativeDrift;              // negative spike driven synaptic plasticity
                }                
            }
            synapse.setWeight(weight);  // Updates the weight of the synapse
            outputNeuron.receiveSpike(weight);      // Acumulates the injected current of the neuron.
            //System.out.println(weight);
        }

//        for (SpikingNeuron neuron : network.getReservoir()) {
//            Synapse outputNeuronSynapse = outputNeuron.getInputSynapses().get(neuron.getIdx());
//            weight = outputNeuronSynapse.getWeight();
//            if (neuron.isFired()) {
//                if (neuron.getNumSpikesEmitted() == 1) {                // If it is the first spike of the presynaptic neuron
//                    if (orderFlag) {
//                        this.intSpikeOrder++;
//                        orderFlag = false;
//                    }
//                    outputNeuronSynapse.addStimuli();                   // Adds a stimuli to the output neuron
//                    weight = alpha * Math.pow(mod, this.intSpikeOrder); // Rank order learning rule 
//                    this.activeNeurons.add(neuron);                     // Add a useful neuron from the reservoir
//                    this.increaseWeightChanges();                       // It represents the number of neurons that fired for the first time, then the useful neurons                    
//                    this.increaseSampleWeightChanges();
//                } else {                                                // If it is not the first spike, then increase the synaptic weight
//                    weight += positiveDrift;                            // spike driven synaptic plasticity
//                }
//            } else {                                                    // If no spikes emiteed, then decrease the synpatic weight
//                //weight = (outputNeuronSynapse.getStimuli() == 0) ? 0 : weight - negativeDrift; // spike driven synaptic plasticity
//                weight = (neuron.getNumSpikesEmitted() > 0) ? 0 : weight - negativeDrift; // spike driven synaptic plasticity                
//            }
//            outputNeuronSynapse.setWeight(weight);  // Updates the weight of the synapse
//            outputNeuron.receiveSpike(weight);      // Acumulates the injected current of the neuron.
//        }
    }

    @Override
    public void updateSynapticWeights(ArrayList<SpikingNeuron> firedNeurons, int elapsedTime) {

    }

    @Override
    public void resetFieldsForTraining() {

    }

    @Override
    public void resetFieldsForSample() {
        this.intSpikeOrder = 0;
        this.setWeightChanges(0);
        this.setSampleWeightChanges(0);
        this.getWeightChangeList().clear();
    }

    @Override
    public String toString() {
        return "deSNNs";
    }

    /**
     * @return the positiveDrift
     */
    public double getPositiveDrift() {
        return positiveDrift;
    }

    /**
     * @param positiveDrift the positiveDrift to set
     */
    public void setPositiveDrift(double positiveDrift) {
        this.positiveDrift = positiveDrift;
    }

    /**
     * @return the mod
     */
    public double getMod() {
        return mod;
    }

    /**
     * @param mod the mod to set
     */
    public void setMod(double mod) {
        this.mod = mod;
    }

    /**
     * @return the negativeDrift
     */
    public double getNegativeDrift() {
        return negativeDrift;
    }

    /**
     * @param negativeDrift the negativeDrift to set
     */
    public void setNegativeDrift(double negativeDrift) {
        this.negativeDrift = negativeDrift;
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

    /**
     * @return the classifier
     */
    public Classifier getClassifier() {
        return classifier;
    }

    /**
     * @param classifier the classifier to set
     */
    public void setClassifier(Classifier classifier) {
        this.classifier = classifier;
    }

//    /**
//     * @return the activeNeurons
//     */
//    public Set<SpikingNeuron> getActiveNeurons() {
//        return activeNeurons;
//    }
//
//    /**
//     * @param activeNeurons the activeNeurons to set
//     */
//    public void setActiveNeurons(Set<SpikingNeuron> activeNeurons) {
//        this.activeNeurons = activeNeurons;
//    }

    public static void main(String args[]) {

    }

}
