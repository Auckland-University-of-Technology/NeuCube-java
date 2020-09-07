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
import jneucube.util.Util;

/**
 *
 * @author em9403
 */
public class FullConnect extends ConnectionAlgorithm {

    private double radius = 2.5;
    private double positiveConnectionRate = 0.7;

    @Override
    public Matrix createConnections(ArrayList<SpikingNeuron> neurons) {
        Matrix randMatrix = new Matrix(neurons.size());
        // Matrix neuronDistances = this.computeDistances(neurons);

        for (int i = 0; i < neurons.size(); i++) {
            if (neurons.get(i).getNeuronType() == NeuronType.INPUT_NEURON_POSITIVE || neurons.get(i).getNeuronType() == NeuronType.INPUT_NEURON_NEGATIVE) {
                for (int j = 0; j < neurons.size(); j++) {
                    if (!(neurons.get(j).getNeuronType() == NeuronType.INPUT_NEURON_POSITIVE || neurons.get(j).getNeuronType() == NeuronType.INPUT_NEURON_NEGATIVE)) {
                        randMatrix.set(i, j, 1);

                    }

                }
            }
        }
        this.replicateConnections(neurons, randMatrix);
        return randMatrix;
    }

    @Override
    public Matrix createWeights(ArrayList<SpikingNeuron> neurons, Matrix connections) {
        Matrix weights = new Matrix(neurons.size(), neurons.size());        // Random matrix        

        for (int i = 0; i < neurons.size(); i++) {
            for (int j = 0; j < neurons.size(); j++) {

                if (connections.get(i, j) == 1) {
                   Util.getRandom(this.minWeightValue, this.maxWeightValue);
                   weights.set(i, j,Util.getRandom(this.minWeightValue, this.maxWeightValue));     // Input neurons are stronger (only positive values)                    
                }

            }
        }
        this.replicateConnections(neurons, weights);
        return weights;
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
