/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.trainingAlgorithms.unsupervised;

import java.util.ArrayList;
import jneucube.data.DataSample;
import jneucube.data.SpatioTemporalData;
import jneucube.network.Network;
import jneucube.network.NetworkController;
import jneucube.spikingNeurons.SpikingNeuron;
import jneucube.spikingNeurons.Synapse;
import jneucube.trainingAlgorithms.LearningAlgorithm;


/**
 *
 * @author Josafath Israel Espinosa Ramos
 */
public class STDPH extends LearningAlgorithm {

    private double tauPos = 10; // tau: time windows determining the range of spike interval over which the STDP occurs for POSITIVE modifications 
    private double tauNeg = 10; // tau: time windows determining the range of spike interval over which the STDP occurs for NEGATIVE modifications   
    private double Apos = 0.001; // POSITIVE synaptic modifications  
    private double Aneg = 0.001; // NEGATIVE synaptic modifications 
    private double upperBound = 2.0; // Maximum synaptic weight value
    private double lowerBound = 0.0; // Minimum synaptic weight value
    private int deltaWindow = 10;   // Time interval for counting the number of response spikes (The time steps of the shifting window)    
    private double tagetFiringRate = 35;    //Target firing rate of the postsynaptic neuron. Design choice (may depend on the type of the neuron, e.g. 40Hz = 40/1000 ms, 4/100 ms, 0.4/10 ms) 
    private double alpha = 0.1;
    private double beta = 1.0;
    private double gamma = 50;     // Tuning factor  

    //Logger _log = LoggerFactory.getLogger(STDPH.class);

    //It is worth noting that alpha and beta must be chosen so as to suppress runaway synaptic dynamics but still allow learning to proceed at a reasonable rate.
    @Override
    public void train(Network network, ArrayList<DataSample> trainingData) {
//        int elapsedTime = 0;
//        ArrayList<SpikingNeuron> firedNeurons;
//        Runtime.getRuntime().gc();
//        int sampleId = 1;
//
//        network.resetNeuronsFirings(); // Removes all firings (firing times) and sets to zero the last spike time, the number of spikes received and the number of spikes emitted.        
//        network.resetConnectionsWeights(); // Clears existing training weights. T synapti weight of each neuron are set to its initial state (position 0)
//        _log.info("------- Unsupervised training using STDP learning rule -------");
//        for (int t = 0; t < this.getTrainingRounds(); t++) {    // Number of training times           
//            for (DataSample sample : trainingData) {   // For each training data sample  
////                System.out.println("Sample " + sampleId);
//                network.getNetwork().getReservoir().stream().forEach((neuron) -> {
//                    neuron.getCore().setLastSpikeTime(0);
//                    neuron.getCore().resetCore();   // Resets neurons' core. Sets action potentials to zero among other features according to the neuron model, eg. in SLIF the refractory time and the membrne potential are set to zero
//                    neuron.resetCurrent();          // Resets the accumulated input current given by the synaptic weights
//                });
//
//                for (int sampleTime = 0; sampleTime < sample.getSpikeData().getRows(); sampleTime++) {
//                    network.setInputSpikes(sample.getSpikeData().getVecRow(sampleTime), elapsedTime);   // Set the input spike data into the input neurons and add the elapsed time to the neuron firing list
//                    firedNeurons = network.stimulateNetwork(elapsedTime); // Propagates the information through the network and returns the fired neurons                    
//                    _log.debug("Elapsed time " + elapsedTime + " Time " + sampleTime + " Fired " + firedNeurons.size());
////                    if (this.isSavingWeightMode()) {
////                        this.adaptAndSaveWeights(network);  // Adapts and save the synaptic weights according to the STDP. This procedure does not requires the fired neurons
////                    } else {
//                    this.adaptWeights(network, firedNeurons);   // Adapts the synaptic weights according to the STDP. This method requires the fired neurons for faster computations
////                    }
//                    elapsedTime++;
//                }
////                System.out.println("Sample time " + duration);
//                sampleId++;
//                _log.debug("Sample " + sampleId);
//            }
//        }
//        network.recordCurrentWeights();   // Save the last connection state of the network
//
//        this.setTrainingTime(elapsedTime);
////        System.out.println("Training complete");
//        Runtime.getRuntime().gc();
//        this.setExecuted(true);
//        _log.info("------- Unsupervised training complete -------");
    }

    /**
     * Adapt the synaptic weights according to the STDP learning rule. This
     * approach utilizes the closest spike times of pre- and postsynaptic
     * neurons to strengthen or weaken the synapse. This method does not save
     * the weights and requires the fired neurons for faster computations.
     *
     * @param network The reservoir
     * @param firedNeurons The neurons that fired when they were stimulated
     */
    public void adaptWeights(NetworkController network, ArrayList<SpikingNeuron> firedNeurons) {
        double avgFiringRate;

        double tempWeight;
        for (SpikingNeuron firedNeuron : firedNeurons) {
            for (Synapse synapse : firedNeuron.getOutputSynapses()) {  // this neuron is the presynaptic. The presynaptic neuron fires after the postsynaptic neuron (The synapse is weaken)                
                if (synapse.getLastUpdate() != network.getNetwork().getReservoir().get(synapse.getTargetNeuronIdx()).getLastSpikeTime()) {
                    avgFiringRate = network.getNetwork().getReservoir().get(synapse.getTargetNeuronIdx()).getAverageFiringRate();    // average firing rate of the postsynaptic neuron                    
                    tempWeight = getWeightGain(firedNeuron.getLastSpikeTime(), network.getNetwork().getReservoir().get(synapse.getTargetNeuronIdx()).getLastSpikeTime(), synapse.getWeight(), avgFiringRate);
                    tempWeight = Math.max(lowerBound, synapse.getWeight() + tempWeight);
                    //synapse.setWeight(synapse.getWeight() + getWeightGain(firedNeuron.getLastSpikeTime(), network.getReservoir().get(synapse.getTargetNeuronIdx()).getLastSpikeTime()));                    
                    synapse.setWeight(tempWeight);
                    synapse.setLastUpdate(network.getNetwork().getReservoir().get(synapse.getTargetNeuronIdx()).getLastSpikeTime());
                }
            }
            for (Synapse synapse : firedNeuron.getInputSynapses()) { // this neuron is the post synaptic. The postsynaptic neuron fires after the presynaptic neuron (The synapse is strenghten)
                if (synapse.getLastUpdate() != network.getNetwork().getReservoir().get(synapse.getSourceNeuronIdx()).getLastSpikeTime()) {
                    avgFiringRate = firedNeuron.getAverageFiringRate(); // average firing rate of the postsynaptic neuron
                    tempWeight = getWeightGain(network.getNetwork().getReservoir().get(synapse.getSourceNeuronIdx()).getLastSpikeTime(), firedNeuron.getLastSpikeTime(), synapse.getWeight(), avgFiringRate);
                    tempWeight = Math.min(upperBound, synapse.getWeight() + tempWeight);
                    //synapse.setWeight(synapse.getWeight() + getWeightGain(network.getReservoir().get(synapse.getSourceNeuronIdx()).getLastSpikeTime(), firedNeuron.getLastSpikeTime()));
                    synapse.setWeight(tempWeight);
                    synapse.setLastUpdate(network.getNetwork().getReservoir().get(synapse.getSourceNeuronIdx()).getLastSpikeTime());
                }
            }
        }
    }

    /**
     *
     * @param tPre The last firing time of the presynaptic neuron
     * @param tPos The last firing time of the postsynaptic neuron
     * @param weight current value of the synaptic weight i->j synapse value (i:
     * postsynaptic neuron, j: presynaptic)
     * @param rho The average firing rate of the postsynaptic neuron
     * @return Postsynaptic neuron's firing rate
     */
    public double getWeightGain(int tPre, int tPos, double weight, double rho) {

        double deltaWeight = 0.0;
        double k = this.getScalabilityFactor(rho);
        double dw = this.getNearestNeighbourStdpWeigthGain(tPre, tPos);
        deltaWeight = (this.alpha * weight * (1 - rho / this.tagetFiringRate) + this.beta * (dw)) * k;
        return deltaWeight;
    }

    /**
     *
     * @param tPre
     * @param tPos
     * @return
     */
    public double getNearestNeighbourStdpWeigthGain(int tPre, int tPos) {
        double dw = 0.0;
        int delta = tPre - tPos;
        if (delta < 0) {    // potentiation
            dw = this.Apos * Math.exp(delta / this.tauPos);
        } else if (delta > 0) { // depression
            dw = -this.Aneg * Math.exp(-delta / this.tauNeg);
        }
        return dw;
    }

    public double getScalabilityFactor(double averageFiringRate) {
        double scalabilityFactor = 0.0;
        scalabilityFactor = averageFiringRate / (this.getDeltaWindow() * (1 + Math.abs(1 - averageFiringRate / this.tagetFiringRate) * this.gamma));
        return scalabilityFactor;
    }

    /**
     *
     * @param weight current value of the synaptic weight j->i synapse value (i:
     * postsynaptic neuron, j: presynaptic neuron)
     * @param rho Postsynaptic neuron's firing rate
     * @return Returns the wight change
     */
    public double deltaWeight(double weight, double rho) {
        double change = 0;
        return change;
    }

    /**
     *
     * @param weight j->i synapse value (i: postsynaptic neuron, j: presynaptic
     * neuron)
     * @return Change for synaptic potentiation
     */
    private double calculateApos(double weight) {
        double Apos = 0;
        Apos = weight * this.Apos;
        return Apos;
    }

    /**
     *
     * @param weight j->i synapse value (i: postsynaptic neuron, j: presynaptic
     * neuron)
     * @param rho Postsynaptic neuron's firing rate
     * @return Change for synaptic depression
     */
    private double calculateAneg(double weight) {
        double Aneg = 0;
        Aneg = weight * Apos;
        return Aneg;
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

    /**
     * @return the deltaWindow
     */
    public int getDeltaWindow() {
        return deltaWindow;
    }

    /**
     * @param deltaWindow the deltaWindow to set
     */
    public void setDeltaWindow(int deltaWindow) {
        this.deltaWindow = deltaWindow;
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
     * @return the tagetFiringRate
     */
    public double getTagetFiringRate() {
        return tagetFiringRate;
    }

    /**
     * @param tagetFiringRate the tagetFiringRate to set
     */
    public void setTagetFiringRate(double tagetFiringRate) {
        this.tagetFiringRate = tagetFiringRate;
    }

    /**
     * @return the gamma
     */
    public double getGamma() {
        return gamma;
    }

    /**
     * @param gamma the gamma to set
     */
    public void setGamma(double gamma) {
        this.gamma = gamma;
    }

    /**
     * @return the beta
     */
    public double getBeta() {
        return beta;
    }

    /**
     * @param beta the beta to set
     */
    public void setBeta(double beta) {
        this.beta = beta;
    }

    @Override
    public void updateSynapticWeights(ArrayList<SpikingNeuron> firedNeurons, int elapsedTime) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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

}
