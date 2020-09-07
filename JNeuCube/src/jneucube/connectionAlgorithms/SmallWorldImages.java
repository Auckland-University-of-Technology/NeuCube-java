/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.connectionAlgorithms;

import java.util.ArrayList;
import java.util.Random;
import jneucube.spikingNeurons.NeuronType;
import jneucube.spikingNeurons.SpikingNeuron;
import jneucube.util.Matrix;
import jneucube.util.Util;

/**
 *
 * @author Josafath Israel Espinosa Ramos
 */
public class SmallWorldImages extends ConnectionAlgorithm {

    private double radius = 2.5;
    private double positiveConnectionRate = 0.7;

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
        Random random=new Random();
        double distanceThreshold = this.radius;
        Matrix randMatrix = Matrix.randBinary(neurons.size());
        Matrix neuronDistances = this.computeDistances(neurons);

        //SMALL WORLD CONNECTIVITY
        for (int i = 0; i < neurons.size(); i++) {
            for (int j = 0; j < neurons.size(); j++) {
                if (neuronDistances.get(i, j) > distanceThreshold || neurons.get(j).getNeuronType() == NeuronType.INPUT_NEURON_POSITIVE || neurons.get(j).getNeuronType() == NeuronType.INPUT_NEURON_NEGATIVE) {
                    randMatrix.set(i, j, 0.0);  // No connections between reservoir neurons and input neurons
                } else if (randMatrix.get(i, j) != 0.0 && randMatrix.get(j, i) != 0.0) { //If bidirectional connections
                    if (random.nextDouble() < 0.5) {
                        randMatrix.set(i, j, 0.0);
                    } else {
                        randMatrix.set(j, i, 0.0);
                    }
                }
            }
        }

        // The first layer (input neurons) are connected to the corresponding neuron in the reservoir 
        SpikingNeuron pre;
        SpikingNeuron post;
        for (int i = 0; i < neurons.size(); i++) {
            pre = neurons.get(i);
            if (pre.getNeuronType() == NeuronType.INPUT_NEURON_POSITIVE) {
                for (int j = 0; j < neurons.size(); j++) {
                    post = neurons.get(j);
                    if ((post.getNeuronType() == NeuronType.RESERVOIR_NEURON) && (pre.getPosX() == post.getPosX()) && (pre.getPosY() == post.getPosY()) && (pre.getPosZ() == post.getPosZ())) {
                        randMatrix.set(i, j, 1.0);
                    } else {
                        randMatrix.set(i, j, 0.0);
                    }
                }
            }
        }

        this.replicateConnections(neurons, randMatrix);

        return randMatrix;
    }

    /**
     * This function replicates the positive input neuron's connections or
     * weights to the negative input neurons.
     *
     * @param neurons the set of neurons where the positive and negative inputs
     * are.
     * @param matrix matrix of connections or weights.
     */
    private void replicateConnections(ArrayList<SpikingNeuron> neurons, Matrix matrix) {
        // Negative input neurons have the same postsynaptic connections and weights as the positive input neurons.
        ArrayList<SpikingNeuron> inputPositive = new ArrayList<>();
        ArrayList<SpikingNeuron> inputNegative = new ArrayList<>();
        neurons.stream().filter((neuron) -> (neuron.getNeuronType() == NeuronType.INPUT_NEURON_POSITIVE)).forEachOrdered((neuron) -> {
            inputPositive.add(neuron);
        });
        neurons.stream().filter((neuron) -> (neuron.getNeuronType() == NeuronType.INPUT_NEURON_NEGATIVE)).forEachOrdered((neuron) -> {
            inputNegative.add(neuron);
        });

        int pIdx;
        int nIdx;
        for (int i = 0; i < inputPositive.size(); i++) {
            SpikingNeuron pNeuron = inputPositive.get(i);
            SpikingNeuron nNeuron = inputNegative.get(i);
            pIdx = pNeuron.getIdx();
            nIdx = nNeuron.getIdx();
            for (int j = 0; j < neurons.size(); j++) {
                matrix.set(nIdx, j, matrix.get(pIdx, j) * -1);
            }
        }
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
                initialWeights.set(i, j, connections.get(i, j) * initialWeights.get(i, j));
                if (neurons.get(i).getNeuronType() == NeuronType.INPUT_NEURON_POSITIVE || neurons.get(i).getNeuronType() == NeuronType.INPUT_NEURON_NEGATIVE) {
                    initialWeights.set(i, j, Math.abs(initialWeights.get(i, j) * 2.0));     // Input neurons are stronger
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

}
