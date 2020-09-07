/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.connectionAlgorithms;

import java.util.ArrayList;
import jneucube.spikingNeurons.NeuronType;
import jneucube.spikingNeurons.SpikingNeuron;
import jneucube.util.Matrix;

public class EpussSmallWorld extends ConnectionAlgorithm {

    public static final boolean HOMOGENEOUS_TYPE = true;
    public static final boolean RANDOM_TYPE = false;
    private double radius = 2.5;
    private double positiveConnectionRate = 0.8;
    private double bias = 1; //The bias depends on the threshold value of the neuron model
    private boolean weightType = RANDOM_TYPE; // This variable defines the type of weights value (eighter homogeneous or random)
    private double homoValue = 0.0; // value of the homogenous weights
    private ArrayList<SpikingNeuron> inputPositiveNeurons;
    private ArrayList<SpikingNeuron> inputNegativeNeurons;

    /**
     * Creates connection among the list of spiking neurons. No connections
     * between neurons in the reservoir to input neurons and no bidirectional
     * connections between two neurons are created
     *
     * @param neurons
     * @return
     */
    @Override
    public Matrix createConnections(ArrayList<SpikingNeuron> neurons) {
        double distanceThreshold = this.radius;
        Matrix connections = new Matrix(neurons.size(), neurons.size(), 1.0);
        Matrix randMatrix = Matrix.randBinary(neurons.size());
        Matrix neuronDistances = this.computeDistances(neurons);

        //SMALL WORLD CONNECTIVITY
        for (int i = 0; i < neurons.size(); i++) {
            for (int j = 0; j < neurons.size(); j++) {
                if (neuronDistances.get(i, j) > distanceThreshold || neurons.get(j).getNeuronType() == NeuronType.INPUT_NEURON_POSITIVE) {
                    connections.set(i, j, 0.0);  // No connections between reservoir neurons and input neurons
                } else if (connections.get(i, j) == 1.0 && connections.get(j, i) == 1.0) { //No bidirectional connections                    
                    if (randMatrix.get(i, j) == 1.0) {
                        connections.set(i, j, 0.0);
                    } else {
                        connections.set(j, i, 0.0);
                    }
                }
                // Connection from the input nodes to the reservoir
            }
        }

        if (this.inputPositiveNeurons != null && this.inputNegativeNeurons != null) {
            for (int i = 0; i < this.inputPositiveNeurons.size(); i++) {
                for (int j = 0; j < neurons.size(); j++) {
                    connections.set(this.inputPositiveNeurons.get(i).getIdx(), neurons.get(j).getIdx(), 0.0);
                    connections.set(neurons.get(j).getIdx(), this.inputPositiveNeurons.get(i).getIdx(), 0.0);
                }
                connections.set(this.inputPositiveNeurons.get(i).getIdx(), this.inputNegativeNeurons.get(i).getIdx(), 1.0);
            }
            for (int i = 0; i < this.inputNegativeNeurons.size(); i++) {
                for (int j = 0; j < this.inputNegativeNeurons.size(); j++) {
                    if (i != j) {
                        connections.set(this.inputNegativeNeurons.get(i).getIdx(), this.inputNegativeNeurons.get(j).getIdx(), 1.0);
                    }
                }
            }
        }


////// JIER CODE
//        for (int i = 0; i < neurons.size(); i++) {
//            for (int j = 0; j < neurons.size(); j++) {
//                if (neuronDistances.get(i, j) > distanceThreshold || neurons.get(j).getNeuronType() == NeuronType.INPUT_NEURON_POSITIVE || neurons.get(j).getNeuronType() == NeuronType.INPUT_NEURON_NEGATIVE) {
//                    connections.set(i, j, 0.0);  // No connections between reservoir neurons and input neurons
//                } else if (connections.get(i, j) == 1.0 && connections.get(j, i) == 1.0) { //No bidirectional connections                    
//                    if (randMatrix.get(i, j) == 1.0) {
//                        connections.set(i, j, 0.0);
//                    } else {
//                        connections.set(j, i, 0.0);
//                    }
//                }
//            }
//        }
//
//        for (int i = 0; i < neurons.size(); i++) {
//            for (int j = 0; j < neurons.size(); j++) {
//                if (neurons.get(i).getNeuronType() == NeuronType.INPUT_NEURON_POSITIVE || neurons.get(i).getNeuronType() == NeuronType.INPUT_NEURON_NEGATIVE) {
//                    if (neuronDistances.get(i, j) == 0) { // The input node has the same coordinates as the reservoir neuron
//                        connections.set(i, j, 1.0);
//                    } else {
//                        connections.set(i, j, 0.0);
//                    }
//                }
//
//            }
//        }
////// FINISH JIER CODE
        return connections;
    }

    /**
     * Creates the weight values for all connections. Connections from input
     * neurons that treats positive spikes are increased twice. Connections from
     * input neurons that treats negative spikes are set to zero
     *
     * @param neurons
     * @param connections
     * @return
     */
    @Override
    public Matrix createWeights(ArrayList<SpikingNeuron> neurons, Matrix connections) {
        Matrix neuronDistances = this.computeDistances(neurons);
        Matrix invDistances = neuronDistances.operation('\\', 1.0); // Closer distances will produce higher weight values. The minimum distance between 2 neurons must be 1
        Matrix initialWeights = Matrix.rand(neurons.size());        // Random matrix        
        initialWeights = initialWeights.operation('-', 1.0 - this.positiveConnectionRate);    // Since the vector has a uniform distribution between 0.0 and 1.0 we can substract 
        initialWeights.sign();                                        // The weigths are converted into 1, 0 or -1
        initialWeights = initialWeights.operation('*', Matrix.rand(neurons.size()));    //New random matrix is generated because at this point there are not higher values that percentage of positive connections due the previous substraction
        initialWeights = initialWeights.operation('*', invDistances);   // Higher weight values for closer distances
        initialWeights = initialWeights.operation('*', connections);

        initialWeights.norm(this.minWeightValue, this.maxWeightValue);  // weights normalisation between the specified range of min and max values
        //initialWeights = initialWeights.operation('*', this.maxWeightValue); // Since all weights= [0,1], a multiplication reduces or increases maximum weight value

        for (int i = 0; i < neurons.size(); i++) {
            for (int j = 0; j < neurons.size(); j++) {

                if (this.weightType == HOMOGENEOUS_TYPE) {
                    initialWeights.set(i, j, ((initialWeights.get(i, j) > 0) ? 1 : -1) * this.homoValue);
                } else {
                    initialWeights.set(i, j, connections.get(i, j) * initialWeights.get(i, j));
                }

            }
        }
        for (int i = 0; i < neurons.size(); i++) {
            for (int j = 0; j < neurons.size(); j++) {
                // do we need negative weights in initialization
//                if (neurons.get(i).getNeuronType() == NeuronType.INPUT_NEURON_POSITIVE || neurons.get(i).getNeuronType() == NeuronType.INPUT_NEURON_NEGATIVE) {
                if (neurons.get(i).getNeuronType() == NeuronType.INPUT_NEURON_POSITIVE && neurons.get(j).getNeuronType() == NeuronType.INPUT_NEURON_NEGATIVE) {

                    //The bias depends on the threshold value of the neuron model
                    initialWeights.set(neurons.get(i).getIdx(), neurons.get(j).getIdx(), bias);     // Input neurons are stronger
                }
            }
        }

        //this.weights=initialWeights;    
        return initialWeights;
    }

    public Matrix computeDistances(Matrix coordinates) {
        Matrix matrixDistance = Matrix.getEuclidianDistance(coordinates, coordinates);
        return matrixDistance;
    }

    public Matrix computeDistances(ArrayList<SpikingNeuron> neurons) {
        Matrix matrix;
        Matrix A = new Matrix(neurons.size(), 3, 0.0);
        for (int i = 0; i < neurons.size(); i++) {
            A.setCols(i, neurons.get(i).getPosXYZ());
        }
        matrix = Matrix.getEuclidianDistance(A, A);
        return matrix;
    }

    /**
     * @return the radius
     */
    public double getRadius() {
        return radius;
    }

    /**
     * @param radius the radius to set
     */
    public void setRadius(double radius) {
        this.radius = radius;
    }

    @Override
    public String toString() {
        return "Small World";
    }

    /**
     * @return the positiveConnectionRate
     */
    public double getPositiveConnectionRate() {
        return positiveConnectionRate;
    }

    /**
     * @param positiveConnectionRate the positiveConnectionRate to set
     */
    public void setPositiveConnectionRate(double positiveConnectionRate) {
        this.positiveConnectionRate = positiveConnectionRate;
    }

    /**
     * @return the bias
     */
    public double getBias() {
        return bias;
    }

    /**
     * @param bias the bias to set
     */
    public void setBias(double bias) {
        this.bias = bias;
    }

    /**
     * @return the weightType
     */
    public boolean isWeightType() {
        return weightType;
    }

    /**
     * @param weightType the weightType to set
     */
    public void setWeightType(boolean weightType) {
        this.weightType = weightType;
    }

    /**
     * @return the homoValue
     */
    public double getHomoValue() {
        return homoValue;
    }

    /**
     * @param homoValue the homoValue to set
     */
    public void setHomoValue(double homoValue) {
        this.homoValue = homoValue;
    }

    public void setInputNeurons(ArrayList<SpikingNeuron> inputPositiveNeurons, ArrayList<SpikingNeuron> inputNeuronsNegative) {
        this.inputPositiveNeurons = inputPositiveNeurons;
        this.inputNegativeNeurons = inputNeuronsNegative;
    }

}
