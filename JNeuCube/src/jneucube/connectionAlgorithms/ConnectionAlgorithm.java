/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.connectionAlgorithms;

import java.util.ArrayList;
import java.util.Arrays;
import jneucube.spikingNeurons.SpikingNeuron;
import jneucube.util.Matrix;

/**
 *
 * @author Josafath Israel Espinosa Ramos (Centro de Investigacion en Computacion, Instituto Politecnico Nacional, Mexico 2015)
 */
public abstract class ConnectionAlgorithm {

    public static final SmallWorld SMALL_WORLD = new SmallWorld();
    public static final SmallWorldImages SMALL_WORLD_IMAGES = new SmallWorldImages();
    public static final EpussSmallWorld EPUSSS_SMALL_WORLD = new EpussSmallWorld();
    public static final FullConnect FULL_CONNECT = new FullConnect();
    public static final ArrayList<ConnectionAlgorithm> CONNECTION_ALGORITHM_LIST = new ArrayList<>(Arrays.asList(SMALL_WORLD,SMALL_WORLD_IMAGES,EPUSSS_SMALL_WORLD));

     //Matrix neuronConnections;
    //Matrix weights;
    //private double positiveConnectionRate=0.8;
    double minWeightValue=-0.01;
    double maxWeightValue=0.01;
    int numPositiveConnections=0;
    int numNegativeConnections=0;
    

    public abstract Matrix createConnections(ArrayList<SpikingNeuron> neurons);
    public abstract Matrix createWeights(ArrayList<SpikingNeuron> neurons, Matrix connections);

    /**
     * @return the minWeightValue
     */
    public double getMinWeightValue() {
        return minWeightValue;
    }

    /**
     * @param minWeightValue the minWeightValue to set
     */
    public void setMinWeightValue(double minWeightValue) {
        this.minWeightValue = minWeightValue;
    }

    /**
     * @return the maxWeightValue
     */
    public double getMaxWeightValue() {
        return maxWeightValue;
    }

    /**
     * @param maxWeightValue the maxWeightValue to set
     */
    public void setMaxWeightValue(double maxWeightValue) {
        this.maxWeightValue = maxWeightValue;
    }

    /**
     * @return the numPositiveConnections
     */
    public int getNumPositiveConnections() {
        return numPositiveConnections;
    }

    /**
     * @param numPositiveConnections the numPositiveConnections to set
     */
    public void setNumPositiveConnections(int numPositiveConnections) {
        this.numPositiveConnections = numPositiveConnections;
    }

    /**
     * @return the numNegativeConnections
     */
    public int getNumNegativeConnections() {
        return numNegativeConnections;
    }

    /**
     * @param numNegativeConnections the numNegativeConnections to set
     */
    public void setNumNegativeConnections(int numNegativeConnections) {
        this.numNegativeConnections = numNegativeConnections;
    }

//    /**
//     * @return the positiveConnectionRate
//     */
//    public double getPositiveConnectionRate() {
//        return positiveConnectionRate;
//    }
//
//    /**
//     * @param positiveConnectionRate the positiveConnectionRate to set
//     */
//    public void setPositiveConnectionRate(double positiveConnectionRate) {
//        this.positiveConnectionRate = positiveConnectionRate;
//    }

}
