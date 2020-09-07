/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.trainingAlgorithms;

import java.util.ArrayList;
import jneucube.network.NetworkController;
import jneucube.spikingNeurons.SpikingNeuron;
import jneucube.spikingNeurons.Synapse;
import jneucube.util.Matrix;


/**
 *
 * @author Josafath Israel Espinosa Ramos (Centro de Investigacion en
 * Computacion, Instituto Politecnico Nacional, Mexico 2015)
 */
public class LearningAlgorithmStatistics {

    Matrix synapticStatistics;
    Matrix neuronStatistics;

//    static {
//        System.setProperty("log4j.configurationFile", "jneucube/log4j-configFile.xml");
//    }
//    Logger _log = LoggerFactory.getLogger(LearningAlgorithmStatistics.class);
    public Matrix getSynapticStatistics() {
        return synapticStatistics;
    }

    public Matrix getNeuronStatistics() {
        return neuronStatistics;
    }

    public void run(NetworkController network, int trainingTime) {
        this.synapticStatistics = new Matrix(8, trainingTime + 1, 0.0);    // minWeight, maxWeight, avgMinMaxWeight, positive, negative, avgPosNeg , conductance, avgConductance 
        this.neuronStatistics = new Matrix(5, trainingTime + 1, 0.0); // ancestors (firing neurons), descendants, firing rate, branching parameter
        for (int i = 0; i <= trainingTime; i++) {
            computeTimeStatistics(network, i);
            if (i % 500 == 0) {
                System.out.println(".");
            } else {
                System.out.print(".");
            }

        }
    }

    /**
     * This function detects the avalanches produced by a spiking neural network
     * after being training
     *
     * @param network The trained spiking neural network
     * @param trainingTime The time of the training process
     * @param minDelta Minimum spike inactivity period (silent period). It
     * should be a multiple of the minimal resolution of the data. Here, the
     * number of time steps.
     * @return
     */
    public ArrayList<Avalanche> getAvalanches(NetworkController network, int trainingTime, int minDelta) {
        ArrayList<Avalanche> avalanches = new ArrayList<>();
        ArrayList<SpikingNeuron> firedNeurons;
        int delta = 0;    // Inactive activity window
        boolean inAvalanche = false;
        Avalanche avalanche = new Avalanche();
        for (int elapsedTime = 0; elapsedTime < trainingTime; elapsedTime++) {
            firedNeurons = network.getFiredNeurons(elapsedTime);//  network.countFiredNeurons(elapsedTime);
            if (!firedNeurons.isEmpty()) {
                if (!inAvalanche) {   // Creates a new avalanche
                    avalanche = new Avalanche();
                    avalanche.setStartTime(elapsedTime);
                    avalanche.addFiredNeurons(firedNeurons);
                    delta = minDelta;   // resets the activity window
                    inAvalanche = true;
                } else {  // Increases the size of the avalanche
                    delta = minDelta;
                    avalanche.addFiredNeurons(firedNeurons);
                }
            } else {
                delta = Math.max(0, delta - 1); // Decrease the inactive activity window
                if (delta == 0 && inAvalanche) {
                    avalanche.setEndTime(elapsedTime);
                    avalanches.add(avalanche);
                    inAvalanche = false;
                }
            }
        }

        return avalanches;
    }

    public void computeTimeStatistics(NetworkController network, int elapsedTime) {
        double maxWeight = Double.NEGATIVE_INFINITY;
        double minWeight = Double.POSITIVE_INFINITY;
        double avgMinMaxWeight;
        int posSynapses = 0;
        int negSynapses = 0;
        double avgPosNeg;
        double conductance = 0;
        double avgConductance;
        int numConnections = 0;

        int firingNeuronsRate;
        int ancestors = 0;  // Firing neurons
        int descendants = 0;
        double branchingParameter = 0;//Neuronal Avalanche Dr. John M. Beggs, Indiana Univeristy Department of Physics

        double tempWeight;

        for (SpikingNeuron neuron : network.getNetwork().getReservoir()) {
            for (Synapse postSynapse : neuron.getInputSynapses()) {
                numConnections++;
                tempWeight = postSynapse.getWeights().get(elapsedTime);
                conductance += tempWeight;
                if (maxWeight < tempWeight) {
                    maxWeight = tempWeight;
                }
                if (minWeight > tempWeight) {
                    minWeight = tempWeight;
                }
                if (tempWeight >= 0.0) {
                    posSynapses++;
                } else {
                    negSynapses++;
                }
            }

            for (Double firing : neuron.getCore().getFirings()) {
                if (elapsedTime - 1 == firing) {
                    ancestors++;
                    for (Synapse synapse : neuron.getOutputSynapses()) {
                        //SpikingNeuron descendant = network.getReservoir().get(synapse.getTargetNeuron().getIdx());
                        SpikingNeuron descendant = network.getNetwork().getReservoir().get(synapse.getTargetNeuronIdx());
                        for (Double desFiring : descendant.getCore().getFirings()) {
                            if (desFiring <= elapsedTime) {
                                if (elapsedTime == desFiring) {
                                    descendants++;
                                    break;
                                }
                            } else {
                                break;
                            }
                        }
                    }
                }
            }
        }

        avgMinMaxWeight = (maxWeight + minWeight) / 2;
        avgPosNeg = (posSynapses + negSynapses) / 2;
        avgConductance = conductance / numConnections;
        firingNeuronsRate = ancestors / network.getNetwork().getReservoir().size();
        if (ancestors != 0) {
            branchingParameter = (descendants * 1.0) / (ancestors * 1.0);
        }

        this.synapticStatistics.set(0, elapsedTime, minWeight);
        this.synapticStatistics.set(1, elapsedTime, maxWeight);
        this.synapticStatistics.set(2, elapsedTime, avgMinMaxWeight);
        this.synapticStatistics.set(3, elapsedTime, posSynapses);
        this.synapticStatistics.set(4, elapsedTime, negSynapses);
        this.synapticStatistics.set(5, elapsedTime, avgPosNeg);
        this.synapticStatistics.set(6, elapsedTime, conductance);
        this.synapticStatistics.set(7, elapsedTime, avgConductance);

        this.neuronStatistics.set(0, elapsedTime, ancestors);
        this.neuronStatistics.set(1, elapsedTime, descendants);
        this.neuronStatistics.set(2, elapsedTime, firingNeuronsRate);
        this.neuronStatistics.set(3, elapsedTime, branchingParameter);
    }

    public static void main(String args[]) {

    }

}
