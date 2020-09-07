/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.trainingAlgorithms.unsupervised;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import jneucube.data.DataSample;
import jneucube.data.SpatioTemporalData;
import jneucube.network.Network;
import jneucube.network.NetworkController;
import jneucube.spikingNeurons.NeuronType;
import jneucube.spikingNeurons.SpikingNeuron;
import jneucube.spikingNeurons.Synapse;
import jneucube.spikingNeurons.cores.SLIF;
import jneucube.trainingAlgorithms.LearningAlgorithm;
import jneucube.util.Matrix;


/**
 * This object implements the Spike Timing Dependent Plasticity. A temporally
 * asymmetric form of Hebbian learning induced by temporal correlations between
 * the spikes of pre- and postsynaptic neurons. This approach utilizes the
 * closest spike times of pre- and postsynaptic neurons to strengthen or weaken
 * the synapse.
 *
 * @author Josafath Israel Espinosa Ramos
 *
 * @see Spike-timing dependent plasticity.
 * http://www.scholarpedia.org/article/Spike-timing_dependent_plasticity
 */
public class STDP extends LearningAlgorithm {

    private double rate = 0.15; // This propertie will be deprecated

    private double tauPos = 10; // tau: time window that describes the range of spike interval over which the STDP occurs for POSITIVE modifications 
    private double tauNeg = 10; // tau: time window that describes the range of spike interval over which the STDP occurs for NEGATIVE modifications   
    private double Apos = 0.001; // POSITIVE synaptic modifications  
    private double Aneg = 0.001; // NEGATIVE synaptic modifications 
    // private boolean saveWeights = false;
    private double upperBound = Double.POSITIVE_INFINITY; // Maximum synaptic weight value
    private double lowerBound = Double.NEGATIVE_INFINITY; // Minimum synaptic weight value
    private double meanFiringRate;  // To implement homeostasis. Calculated as a runnung average over a time sxale of seconds.


    /**
     * Executes the STDP learning rule. In STDP no data is needed for synaptic
     * adaptation.
     *
     * @param network
     * @param trainingData
     */
    @Override
    public void train(Network network, ArrayList<DataSample> trainingData) {
        trainOriginal(network, trainingData);
    }

    /**
     *
     * @param network
     * @param sample
     */
    @Override
    public void train(Network network, DataSample sample) {        
        this.setWeightChanges(0); // set to zero the number of weight changes 
        if (this.isSavingWeightMode()) {
            this.adaptAndSaveWeights(network);  // Adapts and save the synaptic weights according to the STDP. This procedure does not requires the fired neurons
        } else {
            this.adaptWeights(network);   // Adapts the synaptic weights according to the STDP. This method requires the fired neurons for faster computations
        }
        if (this.isSaveWeightChanges()) {
            this.getWeightChangeList().add(this.getWeightChanges()); // Add the number of weight changes in a time step (for learning proof) 
        }
        this.increaseSampleWeightChanges(this.getWeightChanges());
        this.increaseTrainingWeightChanges(this.getWeightChanges());
    }

    private void trainOriginal(Network network, ArrayList<DataSample> trainingData) {
//        int elapsedTime = 0;
//        ArrayList<SpikingNeuron> firedNeurons;
//        Runtime.getRuntime().gc();
//        double startTime;
//        double duration;
//        int sampleId = 1;
//
//        // Removes all firings (firing times) and sets to zero the last spike time, the number of spikes received and the number of spikes emitted.
//        // Clears existing training weights. The synaptic weights of each neuron are set to their initial state (position 0)
//        nc.getReservoir().parallelStream().forEach((neuron) -> {
//            neuron.getCore().setLastSpikeTime(0);
//            neuron.reset();
//        });
//
//        this.getWeightChangeList().clear(); // Clear the list of weight changes
//        _log.info("------- Unsupervised training using STDP learning rule -------");
//
//        for (int t = 0; t < this.getTrainingRounds(); t++) {    // Number of training times           
//            for (DataSample sample : trainingData) {   // For each training data sample  
//                startTime = System.nanoTime();
////                System.out.println("Sample " + sampleId);
//                nc.getReservoir().parallelStream().forEach((neuron) -> {
//                    neuron.getCore().setLastSpikeTime(0);
//                    neuron.reset(); // Resets neurons' core and the accumulated input current given by the synaptic weights. Sets action potentials to zero among other features according to the neuron model, eg. in SLIF the refractory time and the membrne potential are set to zero
//                });
//
//                for (int sampleTime = 0; sampleTime < sample.getSpikeData().getRows(); sampleTime++) {
//                    this.setWeightChanges(0); // set to zero the number of weight changes 
//                    currentTime = sampleTime;
//                    nc.setInputSpikes(sample.getSpikeData().getVecRow(sampleTime), elapsedTime);   // Set the input spike data into the input neurons and add the elapsed time to the neuron firing list
//                    firedNeurons = nc.stimulateNetwork(elapsedTime); // Propagates the information through the nc and returns the fired neurons                                        
//                    if (this.isSavingWeightMode()) {
//                        this.adaptAndSaveWeights(nc);  // Adapts and save the synaptic weights according to the STDP. This procedure does not requires the fired neurons
//                    } else {
//                        this.adaptWeights(nc);   // Adapts the synaptic weights according to the STDP. This method requires the fired neurons for faster computations
//                    }
//                    if (this.isSaveWeightChanges()) {
//                        this.getWeightChangeList().add(this.getWeightChanges()); // Add the number of weight changes in a time step (for learning proof) 
//                    }
//                    _log.debug("Elapsed time " + elapsedTime + " Time " + sampleTime + " Fired " + firedNeurons.size() + " Weight changes " + this.getWeightChanges());
//                    elapsedTime++;
//                }
//                duration = (System.nanoTime() - startTime) / 1000000;   // milliseconds
////                System.out.println("Sample time " + duration);
//                sampleId++;
//                _log.debug("Sample " + sampleId);
//            }
//        }
//        nc.recordCurrentWeights();   // Save the last connection state of the nc
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
     */
    public void adaptWeights(Network network) {
        double tempWeight;
        for (SpikingNeuron firedNeuron : network.getFiredNeurons()) {
            for (Synapse synapse : firedNeuron.getOutputSynapses()) {  // this neuron is the presynaptic. The presynaptic neuron fires after the postsynaptic neuron (The synapse is weaken)                
                if (synapse.getLastUpdate() != network.getReservoir().get(synapse.getTargetNeuronIdx()).getLastSpikeTime()) {
                    tempWeight = getWeightGain(firedNeuron.getLastSpikeTime(), network.getReservoir().get(synapse.getTargetNeuronIdx()).getLastSpikeTime());
                    tempWeight = Math.max(lowerBound, synapse.getWeight() + tempWeight);
                    //synapse.setWeight(synapse.getWeight() + getWeightGain(firedNeuron.getLastSpikeTime(), nc.getReservoir().get(synapse.getTargetNeuronIdx()).getLastSpikeTime()));                    
                    synapse.setWeight(tempWeight);
                    synapse.setLastUpdate(network.getReservoir().get(synapse.getTargetNeuronIdx()).getLastSpikeTime());
                    this.increaseWeightChanges();
                }
            }
            for (Synapse synapse : firedNeuron.getInputSynapses()) { // this neuron is the post synaptic. The postsynaptic neuron fires after the presynaptic neuron (The synapse is strenghten)
                if (synapse.getLastUpdate() != network.getReservoir().get(synapse.getSourceNeuronIdx()).getLastSpikeTime()) {
                    tempWeight = getWeightGain(network.getReservoir().get(synapse.getSourceNeuronIdx()).getLastSpikeTime(), firedNeuron.getLastSpikeTime());
                    tempWeight = Math.min(upperBound, synapse.getWeight() + tempWeight);
                    //synapse.setWeight(synapse.getWeight() + getWeightGain(nc.getReservoir().get(synapse.getSourceNeuronIdx()).getLastSpikeTime(), firedNeuron.getLastSpikeTime()));
                    synapse.setWeight(tempWeight);
                    synapse.setLastUpdate(network.getReservoir().get(synapse.getSourceNeuronIdx()).getLastSpikeTime());
                    this.increaseWeightChanges();
                }
            }
        }

    }

    /**
     * Adapt the synaptic weights according to the STDP learning rule. This
     * approach utilizes the closest spike times of pre- and postsynaptic
     * neurons to strengthen or weaken the synapse. This method saves the
     * synaptic weights. If the synapse weight doesn't change, the weight is
     * repeated even if they don't change. This procedure is slower than the
     * method without saving the weights.
     *
     * @param network The reservoir
     */
    public void adaptAndSaveWeights(Network network) {
        double tempWeight;
        for (SpikingNeuron neuron : network.getReservoir()) {
            for (Synapse synapse : neuron.getOutputSynapses()) {  // this neuron is the presynaptic                    
                if (neuron.isFired()) {
                    if (synapse.getLastUpdate() != network.getReservoir().get(synapse.getTargetNeuronIdx()).getLastSpikeTime()) {
                        tempWeight = getWeightGain(neuron.getLastSpikeTime(), network.getReservoir().get(synapse.getTargetNeuronIdx()).getLastSpikeTime());
                        tempWeight = Math.max(lowerBound, synapse.getWeight() + tempWeight);
                        //synapse.setWeight(synapse.getWeight() + getWeightGain(neuron.getLastSpikeTime(), nc.getReservoir().get(synapse.getTargetNeuronIdx()).getLastSpikeTime()));
                        synapse.setWeight(tempWeight);
                        synapse.setLastUpdate(network.getReservoir().get(synapse.getTargetNeuronIdx()).getLastSpikeTime());
                        this.increaseWeightChanges();
                    }
                }
                synapse.addWeight();
            }
            for (Synapse synapse : neuron.getInputSynapses()) { // this neuron is the post synaptic                
                if (neuron.isFired()) {
                    if (synapse.getLastUpdate() != network.getReservoir().get(synapse.getSourceNeuronIdx()).getLastSpikeTime()) {
                        tempWeight = getWeightGain(network.getReservoir().get(synapse.getSourceNeuronIdx()).getLastSpikeTime(), neuron.getLastSpikeTime());
                        tempWeight = Math.min(upperBound, synapse.getWeight() + tempWeight);
                        //synapse.setWeight(synapse.getWeight() + getWeightGain(nc.getReservoir().get(synapse.getSourceNeuronIdx()).getLastSpikeTime(), neuron.getLastSpikeTime()));
                        synapse.setWeight(tempWeight);
                        synapse.setLastUpdate(network.getReservoir().get(synapse.getSourceNeuronIdx()).getLastSpikeTime());
                        this.increaseWeightChanges();
                    }
                }
            }
        }
    }

    @Override
    public void updateSynapticWeights(ArrayList<SpikingNeuron> firedNeurons, int elapsedTime) {

    }

    @Override
    public void resetFieldsForTraining() {
        this.setTrainingWeightChanges(0);
    }

    @Override
    public void resetFieldsForSample() {
        this.setSampleWeightChanges(0);
    }

    /**
     *
     * @param tPre The last firing time of the presynaptic neuron
     * @param tPos The last firing time of the postsynaptic neuron
     * @return
     */
    private double getWeightGain(int tPre, int tPos) {
        double f = 0;
        //Implementation accroding to Jesper Sjöström and Wulfram Gerstner (2010) Spike-timing dependent plasticity. Scholarpedia, 5(2):1362., revision #151671
        int delta = tPos - tPre;
        if (delta > 0) {
            f = this.getApos() * Math.exp(-delta / this.tauPos);     // potentiation
        } else if (delta < 0) {
            f = -this.getAneg() * Math.exp(delta / this.tauNeg);     // depression
        }
        return f;
    }

    public void test(NetworkController network, Matrix spikeTrains) {
        int elapsedTime = 0;
        double gain;
        double tempWeight;
        ArrayList<SpikingNeuron> firedNeurons = new ArrayList<>();

        for (int sampleTime = 0; sampleTime < 128; sampleTime++) {
            firedNeurons.clear();

            for (int col = 0; col < spikeTrains.getCols(); col++) { // Inserts the spike trains into the input neurons
                if (spikeTrains.get(sampleTime, col) != 0) {
                    network.getNetwork().getInputNeurons().get(col).getCore().setFired(true);
                    network.getNetwork().getInputNeurons().get(col).getCore().getFirings().add((double) elapsedTime);
                    network.getNetwork().getInputNeurons().get(col).getCore().setLastSpikeTime(elapsedTime);
                    firedNeurons.add(network.getNetwork().getInputNeurons().get(col));
                }
            }

            for (SpikingNeuron firedNeuron : firedNeurons) {   // Propagate all spikes in the nc
                //firedNeuron.propagateSpike(nc.getReservoir());
                for (Synapse synapse : firedNeuron.getOutputSynapses()) {  // this neuron is the presynaptic                    
                    if (synapse.getLastUpdate() != network.getNetwork().getReservoir().get(synapse.getTargetNeuronIdx()).getLastSpikeTime()) {
                        gain = this.getWeightGain(firedNeuron.getLastSpikeTime(), network.getNetwork().getReservoir().get(synapse.getTargetNeuronIdx()).getLastSpikeTime());
                        tempWeight = Math.max(lowerBound, synapse.getWeight() + gain);
                        //synapse.setWeight(synapse.getWeight() + getWeightGain(firedNeuron.getLastSpikeTime(), nc.getReservoir().get(synapse.getTargetNeuronIdx()).getLastSpikeTime()));                    
                        System.out.print("Post then pre: " + synapse.getWeight() + " -> ");
                        synapse.setWeight(tempWeight);
                        //synapse.setWeight(synapse.getWeight() + gain);
                        System.out.println(synapse.getWeight() + " gain=" + gain);
                        synapse.addWeight();
                        synapse.setLastUpdate(network.getNetwork().getReservoir().get(synapse.getTargetNeuronIdx()).getLastSpikeTime());
                        System.out.println("Postsynaptic (" + (synapse.getSourceNeuronIdx() + 1) + " " + (synapse.getTargetNeuronIdx() + 1) + ") Point " + sampleTime + " dw " + gain);
                    }
                }
                for (Synapse synapse : firedNeuron.getInputSynapses()) { // this neuron is the post synaptic                
                    if (synapse.getLastUpdate() != network.getNetwork().getReservoir().get(synapse.getSourceNeuronIdx()).getLastSpikeTime()) {
                        gain = this.getWeightGain(network.getNetwork().getReservoir().get(synapse.getSourceNeuronIdx()).getLastSpikeTime(), firedNeuron.getLastSpikeTime());
                        tempWeight = Math.min(upperBound, synapse.getWeight() + gain);
                        System.out.print("Pre then post: " + synapse.getWeight() + " -> ");
                        synapse.setWeight(tempWeight);
                        //synapse.setWeight(synapse.getWeight() + gain);
                        System.out.println(synapse.getWeight() + " gain=" + gain);
                        synapse.addWeight();
                        synapse.setLastUpdate(network.getNetwork().getReservoir().get(synapse.getSourceNeuronIdx()).getLastSpikeTime());
                        System.out.println("Presynaptic (" + (synapse.getSourceNeuronIdx() + 1) + " " + (synapse.getTargetNeuronIdx() + 1) + ") Point " + sampleTime + " dw " + gain);
                    }
                }
            }
            //this.updateSynapticWeights(firedNeurons, elapsedTime);   // Update the synaptic weights
            elapsedTime++;
            System.out.println("\n" + elapsedTime);
        }
    }

    @Override
    public void validate(Network network, SpatioTemporalData std) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * @return the rate
     */
    public double getRate() {
        return rate;
    }

    /**
     * @param rate the rate to set
     */
    public void setRate(double rate) {
        this.rate = rate;
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
     * @return the aPos
     */
    public double getApos() {
        return Apos;
    }

    /**
     * @param Apos
     */
    public void setApos(double Apos) {
        this.Apos = Apos;
    }

    /**
     * @return the aNeg
     */
    public double getAneg() {
        return Aneg;
    }

    /**
     * @param Aneg
     */
    public void setAneg(double Aneg) {
        this.Aneg = Aneg;
    }

    @Override
    public String toString() {
        return "STDP";
    }

    public static void main(String args[]) {
        try {
            //Matrix spikeTrains = new Matrix("D:\\spikes.csv", ",");
            Matrix spikeTrains = new Matrix("H:\\KEDRI Projects\\Matlab programs\\STP_spikes.csv", ",");
            int sampleId = 0;
            SpikingNeuron n1 = new SpikingNeuron(new SLIF(), 0, NeuronType.INPUT_NEURON_POSITIVE);
            SpikingNeuron n2 = new SpikingNeuron(new SLIF(), 1, NeuronType.INPUT_NEURON_POSITIVE);
            SpikingNeuron n3 = new SpikingNeuron(new SLIF(), 2, NeuronType.INPUT_NEURON_POSITIVE);

//            SpikingNeuron n1 = new SpikingNeuron(new LIF(), 0, NeuronType.INPUT_NEURON_POSITIVE);
//            SpikingNeuron n2 = new SpikingNeuron(new LIF(), 1, NeuronType.INPUT_NEURON_POSITIVE);
//            SpikingNeuron n3 = new SpikingNeuron(new LIF(), 2, NeuronType.INPUT_NEURON_POSITIVE);
            Synapse synapse1 = new Synapse(n1, n3, 0.3);
            Synapse synapse2 = new Synapse(n2, n3, -0.3);

            n1.addOutputSynapse(synapse1);
            n2.addOutputSynapse(synapse2);

            n3.addInputSynapse(synapse1);
            n3.addInputSynapse(synapse2);

            NetworkController nc = new NetworkController();  
            Network network=new Network();
            nc.setNetwork(network);
            nc.getNetwork().setNumVariables(1);
            nc.getNetwork().getReservoir().add(n1);
            nc.getNetwork().getReservoir().add(n2);
            nc.getNetwork().getReservoir().add(n3);

            //n1.setSpikeTrain(sampleId, spikeTrains.getVecCol(0));
            //n2.setSpikeTrain(sampleId, spikeTrains.getVecCol(1));
            //n3.setSpikeTrain(sampleId, spikeTrains.getVecCol(2));
            nc.getNetwork().getInputNeurons().add(n1);
            nc.getNetwork().getInputNeurons().add(n2);
            nc.getNetwork().getInputNeurons().add(n3);

            STDP stdp = new STDP();
            stdp.setApos(0.15);
            stdp.setAneg(0.15);
            stdp.setTrainingRounds(1);
            stdp.setUpperBound(0.4);
            stdp.setLowerBound(-0.4);
            stdp.test(nc, spikeTrains);
            //DataSample
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(STDP.class.getName()).log(Level.SEVERE, null, ex);
        }
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
     * @return the meanFiringRate
     */
    public double getMeanFiringRate() {
        return meanFiringRate;
    }

    /**
     * @param meanFiringRate the meanFiringRate to set
     */
    public void setMeanFiringRate(double meanFiringRate) {
        this.meanFiringRate = meanFiringRate;
    }

}
