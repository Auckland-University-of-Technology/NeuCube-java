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
 *
 * @author Josafath Israel Espinosa Ramos
 */
public class STDP_DELAYS extends LearningAlgorithm {

    private double tauPos = 10; // tau: time windows determining the range of spike interval over which the STDP occurs for POSITIVE modifications 
    private double tauNeg = 10; // tau: time windows determining the range of spike interval over which the STDP occurs for NEGATIVE modifications   
    private double Apos = 0.001; // POSITIVE synaptic modifications  
    private double Aneg = 0.001; // NEGATIVE synaptic modifications 
    private boolean saveWeights = false;
    private double upperBound = Double.POSITIVE_INFINITY; // Maximum synaptic weight value
    private double lowerBound = Double.NEGATIVE_INFINITY; // Minimum synaptic weight value
    private double meanFiringRate;  // To implement homeostasis. Calculated as a runnung average over a time sxale of seconds.

    //Logger _log = LoggerFactory.getLogger(STDP_DELAYS.class);

//    @Override
//    public Citation getReference() {
//        return Citation.CITE_STDP;
//    }
    @Override
    public void train(Network network, ArrayList<DataSample> trainingData) {

//        ArrayList<SpikingNeuron> firedNeurons = new ArrayList<>();
//        int elapsedTime = 0;
//        Runtime.getRuntime().gc();
//        double startTime;
//        double duration;
//        int sampleId = 1;
//        HashMap<Integer, ArrayList<Synapse>> stimulatedSynapsesTimes = new HashMap<>();
//        ArrayList<Synapse> stimulatedSynapses;
//        //LearningAlgorithm learningAlgorithm = new STDP();
//
//        network.resetNeuronsFirings(); // Removes all firings (firing times) and sets to zero the last spike time, the number of spikes received and the number of spikes emitted.        
//        network.resetConnectionsWeights(); // Clears existing training weights and sets the neurons' synaptic weights to the initial state (position 0)
//        _log.info("------- Unsupervised training using STDP learning rule -------");
//        for (int t = 0; t < this.getTrainingRounds(); t++) {    // Number of training times           
//            for (DataSample sample : trainingData) {   // For each training data sample  
//                startTime = System.nanoTime();
////                System.out.println("Sample " + sampleId);
//                network.getNetwork().getReservoir().stream().forEach((neuron) -> {
//                    neuron.getCore().setLastSpikeTime(0);
//                    neuron.getCore().reset();   // Resets neurons' core. Sets action potentials to zero among other features according to the neuron model, eg. in SLIF the refractory time and the membrne potential are set to zero
//                    neuron.resetCurrent();          // Resets the accumulated input current given by the synaptic weights
//                });
//                for (int sampleTime = 0; sampleTime < sample.getSpikeData().getRows(); sampleTime++) {
//                    network.setInputSpikes(sample.getSpikeData().getVecRow(sampleTime), elapsedTime);   // Set the input spike data into the input neurons and add the elapsed time to the neuron firing list
//
//                    // Searches the synapses that should release the spike at the current time and release the spike
//                    stimulatedSynapses = stimulatedSynapsesTimes.get(elapsedTime);
//                    if (stimulatedSynapses != null) {
//                        for (Synapse synapse : stimulatedSynapses) {
//                            synapse.releaseSpike(network.getNetwork().getReservoir());    // The target neuron (postsynaptic neuron) receives the spike, updates the amount of current released, increases the number of spikes received and set its status as stimulated=true
//                        }
//                        stimulatedSynapsesTimes.get(elapsedTime).clear();
//                        stimulatedSynapsesTimes.remove(elapsedTime);
//                    }
//
//                    // similar to firedNeurons = network.stimulateNetwork(elapsedTime); (NetworkController class)
//                    firedNeurons.clear();
//                    for (SpikingNeuron neuron : network.getNetwork().getReservoir()) {
//                        if (neuron.getNeuronType() == NeuronType.RESERVOIR_NEURON) {
//                            neuron.computeMembranePotential(elapsedTime);   //
//                        }
//                        if (neuron.isFired()) {
//                            firedNeurons.add(neuron);   // this list is used for the next data point along with the input neurons
//                            /// propagate spike
////                            this.increaseSpikesEmitted();
////                            for (Synapse synapse : this.outputSynapses) {  // 
////                                synapse.addStimuli();
////                                synapse.releaseSpike(reservoir);
////                            }
//                            // propagates the spike
//                            neuron.increaseSpikesEmitted();
//                            for (Synapse synapse : neuron.getOutputSynapses()) {
//                                synapse.addStimuli();
//                                // synapse.releaseSpike(reservoir);
//                                //reservoir.get(this.getTargetNeuronIdx()).receiveSpike(this.weight * this.type); // After 
//                                if (stimulatedSynapsesTimes.containsKey(elapsedTime)) {
//                                    stimulatedSynapsesTimes.get(elapsedTime + synapse.getDelay()).add(synapse);
//                                } else {
//                                    ArrayList<Synapse> synapseList = new ArrayList<>();
//                                    synapseList.add(synapse);
//                                    stimulatedSynapsesTimes.put(elapsedTime + synapse.getDelay(), synapseList);
//                                }
//
//                            }
//                        }
//
//                        //
//                        if (this.isSavingWeightMode()) {
//                            this.adaptAndSaveWeights(network);  // Adapts and save the synaptic weights according to the STDP. This procedure does not requires the fired neurons
//                        } else {
//                            this.adaptWeights(network);   // Adapts the synaptic weights according to the STDP. This method requires the fired neurons for faster computations
//                        }
//                    }
//                    _log.debug("Elapsed time " + elapsedTime + " Time " + sampleTime + " Fired " + firedNeurons.size());
//                    // UPDATE THE WEIGHTS ACCORDING TO THE LEARNING RULE
//                    elapsedTime++;
//                }
//                duration = (System.nanoTime() - startTime) / 1000000;   // milliseconds
////                System.out.println("Sample time " + duration);
//                sampleId++;
//                _log.debug("Sample " + sampleId);
//            }
//        }
//        network.recordCurrentWeights();   // Save the last connection state of the network
//        Runtime.getRuntime().gc();
//        _log.info("------- Unsupervised training complete -------");
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
                    //synapse.setWeight(synapse.getWeight() + getWeightGain(firedNeuron.getLastSpikeTime(), network.getReservoir().get(synapse.getTargetNeuronIdx()).getLastSpikeTime()));                    
                    synapse.setWeight(tempWeight);
                    synapse.setLastUpdate(network.getReservoir().get(synapse.getTargetNeuronIdx()).getLastSpikeTime());
                    this.increaseWeightChanges();
                }
            }
            for (Synapse synapse : firedNeuron.getInputSynapses()) { // this neuron is the post synaptic. The postsynaptic neuron fires after the presynaptic neuron (The synapse is strenghten)
                if (synapse.getLastUpdate() != network.getReservoir().get(synapse.getSourceNeuronIdx()).getLastSpikeTime()) {
                    tempWeight = getWeightGain(network.getReservoir().get(synapse.getSourceNeuronIdx()).getLastSpikeTime(), firedNeuron.getLastSpikeTime());
                    tempWeight = Math.min(upperBound, synapse.getWeight() + tempWeight);
                    //synapse.setWeight(synapse.getWeight() + getWeightGain(network.getReservoir().get(synapse.getSourceNeuronIdx()).getLastSpikeTime(), firedNeuron.getLastSpikeTime()));
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
                        //synapse.setWeight(synapse.getWeight() + getWeightGain(neuron.getLastSpikeTime(), network.getReservoir().get(synapse.getTargetNeuronIdx()).getLastSpikeTime()));
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
                        //synapse.setWeight(synapse.getWeight() + getWeightGain(network.getReservoir().get(synapse.getSourceNeuronIdx()).getLastSpikeTime(), neuron.getLastSpikeTime()));
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

    /**
     *
     * @param tPre The last firing time of the presynaptic neuron
     * @param tPos The last firing time of the postsynaptic neuron
     * @return
     */
    private double getWeightGain(int tPre, int tPos) {
        double f = 0;
        int delta = tPre - tPos;
        if (delta < 0) {
            f = this.getApos() * Math.exp(delta / this.tauPos);     // potentiation
        } else if (delta > 0) {
            f = -this.getAneg() * Math.exp(-delta / this.tauNeg);   // depression
        }
//        int delta = tPos - tPre;
//        if (delta > 0) {
//            f = this.getApos() * Math.exp(-delta / this.tauPos);     // potentiation
//        } else if (delta < 0) {
//            f = -this.getAneg() * Math.exp(delta / this.tauNeg);     // depression
//        }

        return f;
    }

    public void test(NetworkController network, Matrix spikeTrains) {
        int elapsedTime = 0;
        double gain;
        double tempWeight;
        ArrayList<SpikingNeuron> firedNeurons = new ArrayList<>();
        int tPre;
        int tPos;
        int inputId;
        int cubeId;
        int neuronIdx;
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

            for (SpikingNeuron firedNeuron : firedNeurons) {   // Propagate all spikes in the network
                //firedNeuron.propagateSpike(network.getReservoir());
                for (Synapse synapse : firedNeuron.getOutputSynapses()) {  // this neuron is the presynaptic                    
                    if (synapse.getLastUpdate() != network.getNetwork().getReservoir().get(synapse.getTargetNeuronIdx()).getLastSpikeTime()) {
                        gain = this.getWeightGain(firedNeuron.getLastSpikeTime(), network.getNetwork().getReservoir().get(synapse.getTargetNeuronIdx()).getLastSpikeTime());
                        tempWeight = Math.max(lowerBound, synapse.getWeight() + gain);
                        //synapse.setWeight(synapse.getWeight() + getWeightGain(firedNeuron.getLastSpikeTime(), network.getReservoir().get(synapse.getTargetNeuronIdx()).getLastSpikeTime()));                    
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
        //Matrix spikeTrains = new Matrix("D:\\spikes.csv", ",");
        Matrix spikeTrains = null;
        try {
            spikeTrains = new Matrix("H:\\KEDRI Projects\\Matlab programs\\spikes.csv", ",");
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(STDP_DELAYS.class.getName()).log(Level.SEVERE, null, ex);
        }
        int sampleId = 0;
        SpikingNeuron n1 = new SpikingNeuron(new SLIF(), 0, NeuronType.INPUT_NEURON_POSITIVE);
        SpikingNeuron n2 = new SpikingNeuron(new SLIF(), 1, NeuronType.INPUT_NEURON_POSITIVE);
        SpikingNeuron n3 = new SpikingNeuron(new SLIF(), 2, NeuronType.INPUT_NEURON_POSITIVE);

        Synapse synapse1 = new Synapse(n1, n3, 0.3);
        Synapse synapse2 = new Synapse(n2, n3, -0.3);

        n1.addOutputSynapse(synapse1);
        n2.addOutputSynapse(synapse2);

        n3.addInputSynapse(synapse1);
        n3.addInputSynapse(synapse2);

        NetworkController network = new NetworkController();
        network.getNetwork().setNumVariables(1);
        network.getNetwork().getReservoir().add(n1);
        network.getNetwork().getReservoir().add(n2);
        network.getNetwork().getReservoir().add(n3);

        //n1.setSpikeTrain(sampleId, spikeTrains.getVecCol(0));
        //n2.setSpikeTrain(sampleId, spikeTrains.getVecCol(1));
        //n3.setSpikeTrain(sampleId, spikeTrains.getVecCol(2));
        network.getNetwork().getInputNeurons().add(n1);
        network.getNetwork().getInputNeurons().add(n2);
        network.getNetwork().getInputNeurons().add(n3);

        STDP stdp = new STDP();
        stdp.setApos(0.15);
        stdp.setAneg(0.15);
        stdp.setTrainingRounds(1);
        stdp.setUpperBound(0.4);
        stdp.setLowerBound(-0.4);
        stdp.test(network, spikeTrains);

        //DataSample
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
