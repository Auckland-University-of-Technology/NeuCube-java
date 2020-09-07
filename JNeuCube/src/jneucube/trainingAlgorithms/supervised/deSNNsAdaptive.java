/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.trainingAlgorithms.supervised;

import java.util.ArrayList;
import jneucube.classifiers.Classifier;
import jneucube.data.DataSample;
import jneucube.data.SpatioTemporalData;
import jneucube.network.Network;
import jneucube.spikingNeurons.NeuronType;
import jneucube.spikingNeurons.SpikingNeuron;
import jneucube.spikingNeurons.Synapse;
import jneucube.trainingAlgorithms.LearningAlgorithm;
import static jneucube.log.Log.LOGGER;

/**
 *
 * @author Josafath Israel Espinosa Ramos
 */
public class deSNNsAdaptive extends LearningAlgorithm {

    private double positiveDrift = 0.005;
    private double negativeDrift = 0.005;
    private double mod = 0.8;       // modulation factor, that defines how important the order of the first spike is mod=[0,1]
    private double alpha = 1.0;     // is a learning parameter (in a partial case it is equal to 1);
    private Classifier classifier = Classifier.WKNN;

    //Logger LOGGER = LoggerFactory.getLogger(deSNNsAdaptive.class);

    
    public deSNNsAdaptive() {

    }

    public deSNNsAdaptive(double drift, double mod, double alpha) {
        this.positiveDrift = drift;
        this.negativeDrift = drift;
        this.mod = mod;
        this.alpha = alpha;
    }
    
    @Override
    public void train(Network network, ArrayList<DataSample> trainingData) {
        LOGGER.info("------- Supervised training using deSNNs algorithm -------");
        ArrayList<SpikingNeuron> trainingOutputNeurons = this.run(network, trainingData);
        network.setOutputNeurons(trainingOutputNeurons);
        LOGGER.info("------- Supervised training complete -------");
    }

    @Override
    public void validate(Network network, SpatioTemporalData std) {
        double classLabel;
        LOGGER.info("------- Validating data using deSNNs algorithm -------");
        ArrayList<SpikingNeuron> validationOutputNeurons = this.run(network, std.getValidationData());
        for (int m = 0; m < validationOutputNeurons.size(); m++) { // Distance calculation and class assigment
//            classLabel = this.classifier.classify(validationOutputNeurons.get(m), network.getOutputNeurons(), std.getTrainingData());
//            std.getValidationData().get(m).setValidationClassId(classLabel);    // Sets the class 
            /**
             * ******* ADAPTIVE ********
             */
            if(std.getValidationData().get(m).getClassId()!=std.getValidationData().get(m).getValidationClassId() ){
                network.getOutputNeurons().add(validationOutputNeurons.get(m));
                std.getTrainingData().add(std.getValidationData().get(m));
            }
            //LOGGER.debug("Id " + std.getValidationData().get(m).getSampleId() + " Real " + std.getValidationData().get(m).getClassId() + " Predicted " + std.getValidationData().get(m).getValidationClassId());
            /**
             * ***********************
             */
        }
        LOGGER.info("------- Validataion complete -------");
    }

    /**
     * Propagates the information through the reservoir and adapts the
     * pre-synapses of the output neurons based on the spike order ranking. The
     * propagation of information differs from the common method used in the
     * STDP (network.stimulateNetwork(elapsedTime) ). In deSNNs, the synapse
     * adaptation is calculated at the time a neuron fires for faster
     * computation.
     *
     * @param network The network
     * @param samples The data to stimulate the network
     * @return
     */
    public ArrayList<SpikingNeuron> run(Network network, ArrayList<DataSample> samples) {
        ArrayList<SpikingNeuron> outputNeurons = createOutputNeurons(network, samples.size());
//        ArrayList<SpikingNeuron> firedNeurons = new ArrayList<>();
//        SpikingNeuron outputNeuron;
//        Synapse outputNeuronSynapse;
//        boolean orderFlag;
//        double weight;
//        double startTime;
//        double endTime;
//        double duration;
//        int elapsedTime = 0; // Unlike the STDP, here we do not use the training elapsed time but the time of the sample, since the samples (events) should be independent
//        int sampleId = 0;
//        int spikeOrder;
//
//        network.resetNeuronsFirings(); // Removes all firings (firing times) and sets to zero the last spike time, the number of spikes received and the number of spikes emitted.        
//
//        for (DataSample sample : samples) {   // For each training data sample
////            sample.getSpikeData().print();
//            startTime = System.nanoTime();
////            System.out.println("Running sample " + sampleId);
//            outputNeuron = outputNeurons.get(sampleId);
//            spikeOrder = -1;
//            network.getReservoir().stream().forEach((neuron) -> {
//                neuron.getCore().setLastSpikeTime(0);
//                neuron.getCore().resetCore();   // Resets neurons' core. Sets action potentials to zero among other features according to the neuron model, eg. in SLIF the refractory time and the membrne potential are set to zero
//                neuron.resetCurrent();          // Resets the accumulated input current given by the synaptic weights
//            });
//
//            for (int sampleTime = 0; sampleTime < sample.getSpikeData().getRows(); sampleTime++) { // For each time unit in a sample
//                firedNeurons.clear();
//                orderFlag = true;
//                network.setInputSpikes(sample.getSpikeData().getVecRow(sampleTime), elapsedTime); // Set the input spike data into the input neurons
//                //network.setInputSpikes(sample.getSpikeData().getVecRow(sampleTime), sampleTime); // Set the input spike data into the input neurons.
//                // Propagates the information through the reservoir and adapts the pre-synapses of the output neurons based on the spike order ranking
//
//                for (SpikingNeuron neuron : network.getReservoir()) {
//                    if (neuron.getNeuronType() == NeuronType.RESERVOIR_NEURON) {
//                        neuron.computeMembranePotential(elapsedTime);
//                        //neuron.computeMembranePotential(sampleTime);    // Unlike the STDP, here we do not use the training elapsed time but the time of the sample, since the samples (events) are independent
//                    }
//                    outputNeuronSynapse = outputNeuron.getInputSynapses().get(neuron.getIdx());
//                    weight = outputNeuronSynapse.getWeight();
//                    if (neuron.isFired()) {
//                        firedNeurons.add(neuron);   // this list is used for the next data point along with the input neurons
//                        if (outputNeuronSynapse.getStimuli() == 0) { // first spike is comming                            
//                            if (orderFlag) {
//                                spikeOrder++;
//                                orderFlag = false;
//                            }
////                            System.out.println(neuron.getIdx()+" "+spikeOrder+ " "+sampleTime);
//                            outputNeuronSynapse.addStimuli();
//                            weight = alpha * Math.pow(mod, spikeOrder); // Rank order learning rule 
//                        } else {
//                            weight += positiveDrift;  // spike driven synaptic plasticity
//                        }
//                    } else {  // decrease the weight after the first spike if the neuron (from the reservoir) didn't fire
//                        weight = (outputNeuronSynapse.getStimuli() == 0) ? 0 : weight - negativeDrift; // spike driven synaptic plasticity
//                    }
//                    outputNeuronSynapse.setWeight(weight);
//                    outputNeuron.receiveSpike(weight);
//                }
//                outputNeuron.computeMembranePotential(sampleTime);
//                for (SpikingNeuron firedNeuron : firedNeurons) {   // Propagate all spikes in the network
//                    firedNeuron.propagateSpike(network.getReservoir());
//                }
//                elapsedTime++;
//            }
//
//            sampleId++;
//
//            endTime = System.nanoTime();
//            duration = (endTime - startTime) / 1000000;   // milliseconds
////            System.out.println("Sample time " + duration);
////            for(int i=0;i<outputNeuron.getInputSynapses().size();i++){
////                System.out.println(i+" "+outputNeuron.getInputSynapses().get(i).getWeight());
////            }
//        }

        return outputNeurons;
    }

    public ArrayList<SpikingNeuron> createOutputNeurons(Network network, int numOutputNeurons) {
        ArrayList<SpikingNeuron> neurons = new ArrayList<>();
        for (int i = 0; i < numOutputNeurons; i++) {
            try {
                SpikingNeuron neuron =network.getSpikingNeuron().clone();//  new SpikingNeuron();
                //neuron.setCore(network.getCore().clone());
                neuron.setIdx(i);
                neuron.setNeuronType(NeuronType.OUTPUT_NEURON);
                network.getReservoir().stream().forEach((sourceNeuron) -> {
                    neuron.addInputSynapse(new Synapse(sourceNeuron, neuron, 0.0));
                });
                neurons.add(neuron);
            } catch (CloneNotSupportedException ex) {
                LOGGER.error(ex.toString());
            }
        }
        return neurons;
    }

    @Override
    public void updateSynapticWeights(ArrayList<SpikingNeuron> firedNeurons, int elapsedTime) {
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

    @Override
    public String toString() {
        return "deSNNs Adaptive";
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

    public static void main(String args[]) {

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

}
