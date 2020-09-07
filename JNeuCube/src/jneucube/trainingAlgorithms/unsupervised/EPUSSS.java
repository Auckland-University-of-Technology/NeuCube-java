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
import jneucube.spikingNeurons.NeuronType;
import jneucube.spikingNeurons.SpikingNeuron;
import jneucube.spikingNeurons.Synapse;
import jneucube.trainingAlgorithms.LearningAlgorithm;
import jneucube.util.Matrix;


/**
 *
 * @author Josafath Israel Espinosa Ramos
 */
public class EPUSSS extends LearningAlgorithm {

    private double alpha = 0.1; //learning rate
    private double deltaAlpha = 0.01; // Increase or decrease learning rate by deltaAlpha
    private double alphaMax = 1; // Maximum value for alpha
    private double alphaMin = 0.0002;  // Minimum value for alpha  
    private double[][] errors;
    private double[][] actualOutput;
    private double[][] desiredOutput;
    private int numDeepLayers = 1;
    private int stepAhead = 0;
    private DataSample sample1 = new DataSample();
    private Matrix epussWeights = new Matrix();
    private int[] outputNeuronIndex;
    public String dir = "C:\\Users\\hbahrami\\Desktop\\EPUSSS-Java\\data\\";
    //Logger _log = LoggerFactory.getLogger(EPUSSS.class);

    @Override
    public void train(Network network, ArrayList<DataSample> trainingData) {
        this.learning(network, trainingData, true);
    }

    @Override
    public void validate(Network network, SpatioTemporalData std) {
        // If condition value sets to false means that the algorithm does not have testing phase
        this.learning(network, std.getValidationData(), false);
    }

    private void updateWeightsE1(ArrayList<SpikingNeuron> reservoir, SpikingNeuron neuron, int deepLayer) {
        SpikingNeuron preNeuron;
        if (deepLayer > 0) {
            for (Synapse synapse : neuron.getInputSynapses()) {

                preNeuron = reservoir.get(synapse.getSourceNeuronIdx());
                if (preNeuron.getNeuronType() == NeuronType.RESERVOIR_NEURON) {
                    // Go back in layers and find the neurons' status and based on that update the weights
                    if ((synapse.getType() * synapse.getWeight()) > 0 && preNeuron.isFired()) {

                        strengthSynapse(synapse);
                    } else if ((synapse.getType() * synapse.getWeight()) > 0 && !preNeuron.isFired()) {
                        this.strengthSynapse(synapse);
                        this.updateWeightsE1(reservoir, preNeuron, deepLayer - 1);
                    } else if ((synapse.getType() * synapse.getWeight()) < 0) {
                        this.weakenSynapse(synapse);
                    }

//                    else if (synapse.getType() == Synapse.INHIBITORY && preNeuron.isFired()) {
//                        this.weakenSynapse(synapse);
//                    } else if (synapse.getType() == Synapse.INHIBITORY && !preNeuron.isFired()) {
//                        this.weakenSynapse(synapse);
//                    }
                }
            }
        }
    }

    private void updateWeightsE2(ArrayList<SpikingNeuron> reservoir, SpikingNeuron neuron, int deepLayer) {
        SpikingNeuron preNeuron;
        if (deepLayer > 0) {
            for (Synapse synapse : neuron.getInputSynapses()) {

                preNeuron = reservoir.get(synapse.getSourceNeuronIdx());
                if (preNeuron.getNeuronType() == NeuronType.RESERVOIR_NEURON) {
                    if ((synapse.getType() * synapse.getWeight()) > 0) {
                        this.weakenSynapse(synapse);
                    } //                if (synapse.getType() == Synapse.EXCITATORY && preNeuron.isFired()) {
                    //                    this.weakenSynapse(synapse);
                    //                } else if (synapse.getType() == Synapse.EXCITATORY && !preNeuron.isFired()) {
                    //                    synapse.decreaseWeight(this.getAlpha());
                    //                    this.weakenSynapse(synapse);
                    //                } 
                    else if ((synapse.getType() * synapse.getWeight()) < 0 && preNeuron.isFired()) {
                        this.strengthSynapse(synapse);
                    } else if ((synapse.getType() * synapse.getWeight()) > 0 && !preNeuron.isFired()) {
                        this.strengthSynapse(synapse);
                        this.updateWeightsE1(reservoir, preNeuron, deepLayer - 1);
                    }
                }
            }
        }
    }

    private void strengthSynapse(Synapse synapse) {
        synapse.increaseWeight(this.getAlpha() * synapse.getType());
    }

    private void weakenSynapse(Synapse synapse) {
        synapse.decreaseWeight(this.getAlpha() * synapse.getType());
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

    public void setNumDeepLayers(int numDeepLayers) {
        this.numDeepLayers = numDeepLayers;
    }

    public int getNumDeepLayers() {
        return this.numDeepLayers;
    }

    /**
     * @return the deltaAlpha
     */
    public double getDeltaAlpha() {
        return deltaAlpha;
    }

    /**
     * @param deltaAlpha the deltaAlpha to set
     */
    public void setDeltaAlpha(double deltaAlpha) {
        this.deltaAlpha = deltaAlpha;
    }

    /**
     * @return the alphaMax
     */
    public double getAlphaMax() {
        return alphaMax;
    }

    /**
     * @param alphaMax the alphaMax to set
     */
    public void setAlphaMax(double alphaMax) {
        this.alphaMax = alphaMax;
    }

    /**
     * @return the alphaMin
     */
    public double getAlphaMin() {
        return alphaMin;
    }

    /**
     * @param alphaMin the alphaMin to set
     */
    public void setAlphaMin(double alphaMin) {
        this.alphaMin = alphaMin;
    }

    /**
     * @return the errors
     */
    public double[][] getErrors() {
        return errors;
    }

    /**
     * @return the stepAhead
     */
    public int getStepAhead() {
        return stepAhead;
    }

    /**
     * @param stepAhead the stepAhead to set
     */
    public void setStepAhead(int stepAhead) {
        this.stepAhead = stepAhead;
    }

    // parameter for csv putput
    public void setActualOutput(double[][] actualOutput) {
        this.actualOutput = actualOutput;
    }

    public double[][] getActualOutput() {
        return this.actualOutput;
    }

    public void setEpussWeights(Matrix epussWeights) {
        this.epussWeights = epussWeights;
    }

    public Matrix getEpussWeights() {
        return this.epussWeights;
    }

    // New Method
    private void learning(Network network, ArrayList<DataSample> sampleList, boolean condition) {
//        ArrayList<SpikingNeuron> firedNeurons;
//
//        int elapsedTime = 0;
//        int neuronStatus;
//
//        double[] outputError = new double[network.getOutputNeurons().size()];
//        int errorCounter = 0;
//        double globalError = 0.0; // avarage error of all I/O neurons
//        double tempGlobalError = Double.POSITIVE_INFINITY;
//
//        errors = new double[outputError.length + 1][this.getTrainingRounds() * sampleList.size() * sampleList.get(0).getNumRecords()];
//        actualOutput = new double[network.getOutputNeurons().size()][sampleList.get(0).getNumRecords() * sampleList.size() * this.getTrainingRounds()];
//        setDesiredOutput(new double[network.getOutputNeurons().size()][sampleList.get(0).getNumRecords() * sampleList.size() * this.getTrainingRounds()]);
//        for (int t = 0; t < this.getTrainingRounds(); t++) {    // Number of training times           
//            for (DataSample sample : sampleList) {   // Stream data (only one sample)
//                network.getReservoir().stream().forEach((neuron) -> {
//                    neuron.getCore().setLastSpikeTime(0);
//                    neuron.getCore().resetCore();   // Resets neurons' core. Sets action potentials to zero among other features according to the neuron model, eg. in SLIF the refractory time and the membrne potential are set to zero
//                    neuron.resetCurrent();          // Resets the accumulated input current given by the synaptic weights
//                });
//                for (int sampleTime = 0; sampleTime < sample.getSpikeData().getRows() - stepAhead; sampleTime++) {
//                    network.setInputSpikes(sample.getSpikeData().getVecRow(sampleTime), elapsedTime);   // Set the input spike data into the input neurons and add the elapsed time to the neuron firing list
//
//                    firedNeurons = network.stimulateNetwork(elapsedTime); // Propagates the information through the network and returns the fired neurons                    
//                    globalError = 0;
//                    for (int i = 0; i < network.getOutputNeurons().size(); i++) {
//
//                        // neuronStatus = (network.getOutputNeurons().get(i).isFired()) ? 1 : 0;
//                        neuronStatus = (network.getOutputNeurons().get(i).isFired()) ? 1 : 0;
//                        //network.getOutputNeurons().get(i).getCore().getSpikeTrain()[elapsedTime] = (byte) neuronStatus;
////                        actualOutput[i][elapsedTime] = neuronStatus;// network.getOutputNeurons().get(i).getCore().getSpikeTrain()[elapsedTime];
////                        desiredOutput[i][elapsedTime] = (int) sample.getSpikeData().getVecRow(sampleTime + stepAhead)[i];
////                        outputError[i] = (int) sample.getSpikeData().getVecRow(sampleTime + stepAhead)[i] - neuronStatus;
//                        actualOutput[i][elapsedTime] = neuronStatus;// network.getOutputNeurons().get(i).getCore().getSpikeTrain()[elapsedTime];
//                        // I changed according to share price 
//                        desiredOutput[i][elapsedTime] = (int) sample.getSpikeData().getCol(outputNeuronIndex[i]).getVecRow(sampleTime + stepAhead)[i];
//                        outputError[i] = (int) sample.getSpikeData().getVecRow(sampleTime + stepAhead)[outputNeuronIndex[i]] - neuronStatus;
//
//                        if (outputError[i] == 1) {
//                            errorCounter++; // error occurrence
//                            if (condition) {
//                                updateWeightsE1(network.getReservoir(), network.getOutputNeurons().get(i), this.numDeepLayers);
//                            }
//                        } else if (outputError[i] == -1) {
//                            errorCounter++; // error occurrence
//                            if (condition) {
//                                updateWeightsE2(network.getReservoir(), network.getOutputNeurons().get(i), this.numDeepLayers);
//                            }
//                        }
//                        globalError += Math.abs(outputError[i]);
//                        errors[i][elapsedTime] = outputError[i];
//
//                    }
//                    // In errors we put both local errors for inputs/outputs neurons and the global error which is the average of local errors`
//                    globalError /= outputError.length;
//                    errors[errors.length - 1][elapsedTime] = globalError;
//
//                    if ((alpha < alphaMax) && (alpha > alphaMin) && (globalError != 0)) {
//                        if (globalError < tempGlobalError) {
//                            alpha += deltaAlpha;
//                        } else if (globalError > tempGlobalError) {
//                            alpha -= deltaAlpha;
//                        }
//                    }
//                    tempGlobalError = globalError;
//
//                    //  System.out.println(elapsedTime + ", " + sample.getSpikeData().getVecRow(sampleTime)[0] + ", " + NS[0] + ", " + errors[0][elapsedTime] + ", " + sample.getSpikeData().getVecRow(sampleTime)[0] + ", " + NS[1] + ", " + errors[1][elapsedTime] + ", " + errors[2][elapsedTime]);
//                    elapsedTime++;
//
//                }
//
//            }
//        }
//        network.saveCurrentWeights();
//        epussWeights = network.getWeightMatrix();

    }

    /**
     * @return the desiredOutput
     */
    public double[][] getDesiredOutput() {
        return desiredOutput;
    }

    /**
     * @param desiredOutput the desiredOutput to set
     */
    public void setDesiredOutput(double[][] desiredOutput) {
        this.desiredOutput = desiredOutput;
    }

    /**
     * @return the outputNeuronIndex
     */
    public int[] getOutputNeuronIndex() {
        return outputNeuronIndex;
    }

    /**
     * @param outputNeuronIndex the outputNeuronIndex to set
     */
    public void setOutputNeuronIndex(int[] outputNeuronIndex) {
        this.outputNeuronIndex = outputNeuronIndex;
    }

}
