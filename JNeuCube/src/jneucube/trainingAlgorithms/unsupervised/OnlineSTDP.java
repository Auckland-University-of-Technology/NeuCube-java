/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.trainingAlgorithms.unsupervised;

import java.util.ArrayList;
import java.util.Date;
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
 * @author em9403
 */
public class OnlineSTDP extends LearningAlgorithm {

    private double tauPos = 10; // tau: time windows determining the range of spike interval over which the STDP occurs  
    private double tauNeg = 10; // Time window    
    private double Apos = 0.07; // Positive synaptic modifications
    private double Aneg = 0.07; // Negative synaptic modifications
    private double upperBound = 2.0; // Maximum synaptic weight value
    private double lowerBound = 0.0; // Minimum synaptic weight value
    private boolean saveSpikes = false;
    private boolean saveWeights = false;



    @Override
    public void train(Network network, ArrayList<DataSample> trainingData) {
        //Consider saving spiking activity and weights 
        Matrix sample = trainingData.get(0).getSpikeData();
        long startTime = System.nanoTime();
        int elapsedTime = this.getTrainingTime();
        LOGGER.debug("   - " + this.getClass().getName() + " executing.[" + new Date() + "] ");
        LOGGER.debug("     - Getting data from buffer [ size " +trainingData.size() + "]");
        trainingData.remove(0);
        LOGGER.debug("     - Removing data from buffer [ size " + trainingData.size() + "]");

        for (int i = 0; i < sample.getRows(); i++) {
            LOGGER.debug("      - Learning data " + i);
            this.train(network, sample.getVecRow(i), elapsedTime);
            LOGGER.debug("      - Complete " + i);
            elapsedTime++;
        }
        this.setTrainingTime(elapsedTime);
        LOGGER.debug("   -" + this.getClass().getName() + " complete.[" + new Date() + "] duration " + ((System.nanoTime() - startTime) / 1000000) + " milliseconds ");
    }

    /**
     * The main training process. Here we do not save the firing times.
     * @param network The network to be stimulated
     * @param spikes  Vector of spikes at time elapsedTime
     * @param elapsedTime The current training time
     */    
    private void train(Network network, double[] spikes, int elapsedTime) {
//        ArrayList<SpikingNeuron> firedNeurons;
//        network.setInputSpikes(spikes, elapsedTime);
//        LOGGER.debug("         - Stimulating network ");
//        firedNeurons = network.stimulateNetwork(elapsedTime);
//        LOGGER.debug("         - Fired neurons at time " + elapsedTime + ": " + firedNeurons.size());
//        LOGGER.debug("         - Adapting syanapses ");
//        this.adaptWeights(network, firedNeurons);
    }

    /**
     * Adapt the synaptic weights according to the STDP learning rule. This
     * method does not save the weights.
     *
     * @param network The reservoir
     * @param firedNeurons The neurons that fired when they were stimulated
     */
    private void adaptWeights(Network network, ArrayList<SpikingNeuron> firedNeurons) {
        for (SpikingNeuron firedNeuron : firedNeurons) {
            for (Synapse synapse : firedNeuron.getOutputSynapses()) {  // this neuron is the presynaptic                    
                if (synapse.getLastUpdate() != network.getReservoir().get(synapse.getTargetNeuronIdx()).getLastSpikeTime()) {
                    synapse.setWeight(synapse.getWeight() + getWeightGain(firedNeuron.getLastSpikeTime(), network.getReservoir().get(synapse.getTargetNeuronIdx()).getLastSpikeTime()));
                    synapse.setLastUpdate(network.getReservoir().get(synapse.getTargetNeuronIdx()).getLastSpikeTime());
                }
            }
            for (Synapse synapse : firedNeuron.getInputSynapses()) { // this neuron is the post synaptic                
                if (synapse.getLastUpdate() != network.getReservoir().get(synapse.getSourceNeuronIdx()).getLastSpikeTime()) {
                    synapse.setWeight(synapse.getWeight() + getWeightGain(network.getReservoir().get(synapse.getSourceNeuronIdx()).getLastSpikeTime(), firedNeuron.getLastSpikeTime()));
                    synapse.setLastUpdate(network.getReservoir().get(synapse.getSourceNeuronIdx()).getLastSpikeTime());
                }
            }
        }
    }

    /**
     *
     * @param tPre The last firing time of the presynaptic neuron
     * @param tPos The last firing time of the postsynaptic neuron
     * @return
     */
    private double getWeightGain(int tPre, int tPos) {
        double f = 0;
        int delta = tPre - tPos;
        //f = (delta < 0) ? this.aPos * Math.exp(delta / this.tauPos) : -this.aNeg * Math.exp(-delta / this.tauNeg);
        if (delta < 0) {
            f = this.getApos() * Math.exp(delta / this.tauPos);
        } else if (delta > 0) {
            f = -this.getAneg() * Math.exp(-delta / this.tauNeg);
        }
        return f;
    }

    @Override
    public void updateSynapticWeights(ArrayList<SpikingNeuron> firedNeurons, int elapsedTime) {

    }

    @Override
    public void validate(Network network, SpatioTemporalData std) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void resetFieldsForTraining() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void resetFieldsForSample() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }    

    /**
     * @return the tauPos
     */
    public double getTauPos() {
        return tauPos;
    }

    /**
     * @param tauPos the tauPos to set
     */
    public void setTauPos(double tauPos) {
        this.tauPos = tauPos;
    }

    /**
     * @return the tauNeg
     */
    public double getTauNeg() {
        return tauNeg;
    }

    /**
     * @param tauNeg the tauNeg to set
     */
    public void setTauNeg(double tauNeg) {
        this.tauNeg = tauNeg;
    }

    /**
     * @return the Apos
     */
    public double getApos() {
        return Apos;
    }

    /**
     * @param Apos the Apos to set
     */
    public void setApos(double Apos) {
        this.Apos = Apos;
    }

    /**
     * @return the Aneg
     */
    public double getAneg() {
        return Aneg;
    }

    /**
     * @param Aneg the Aneg to set
     */
    public void setAneg(double Aneg) {
        this.Aneg = Aneg;
    }

    /**
     * @return the saveSpikes
     */
    public boolean isSaveSpikes() {
        return saveSpikes;
    }

    /**
     * @param saveSpikes the saveSpikes to set
     */
    public void setSaveSpikes(boolean saveSpikes) {
        this.saveSpikes = saveSpikes;
    }

    /**
     * @return the saveWeights
     */
    public boolean isSaveWeights() {
        return saveWeights;
    }

    /**
     * @param saveWeights the saveWeights to set
     */
    public void setSaveWeights(boolean saveWeights) {
        this.saveWeights = saveWeights;
    }

    @Override
    public String toString() {
        return "Online STDP";
    }

    /**
     * @return the upperBound
     */
    public double getUpperBound() {
        return upperBound;
    }

    /**
     * @param upperBound the upperBound to set
     */
    public void setUpperBound(double upperBound) {
        this.upperBound = upperBound;
    }

    /**
     * @return the lowerBound
     */
    public double getLowerBound() {
        return lowerBound;
    }

    /**
     * @param lowerBound the lowerBound to set
     */
    public void setLowerBound(double lowerBound) {
        this.lowerBound = lowerBound;
    }

}
