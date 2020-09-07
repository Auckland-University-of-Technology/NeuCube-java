/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.inputMappingAlgorithms;

import java.util.ArrayList;
import java.util.Collections;
import jneucube.util.Matrix;
import static jneucube.log.Log.LOGGER;

/**
 *
 * @author Josafath Israel Espinosa Ramos
 */
public class RandomMapping extends InputMapping {

    private Matrix neuronsCoordinates;
    private int numFeatures;

    @Override
    public Matrix createCoordinates() {
        LOGGER.info("- Creating input coordinates using " + this.toString());
        long processTime = System.nanoTime();
        if (this.neuronsCoordinates == null) {
            throw new NullPointerException("Reservoir coordinates can not be founds.");
        }
        Matrix randInputs = this.getRandomInputs(this.numFeatures, neuronsCoordinates);
        LOGGER.info("- Complete (time " + ((System.nanoTime() - processTime) / 1000000) + ")");
        return randInputs;

    }

    /**
     * Gets random locations from the surfaces of the network.
     *
     * @param numInputs The number of input neurons.
     * @param neuronsLocation The coordinates of the neurons in the network.
     * @return a numInputs-by-3 Matrix containing the Z, Y and Z coordinates of
     * the inputs.
     */
    public Matrix getRandomInputs(int numInputs, Matrix neuronsLocation) {
        Matrix inputLocations = new Matrix(numInputs, neuronsLocation.getCols());
        Matrix min = neuronsLocation.min(2);
        Matrix max = neuronsLocation.max(2);
        int minX = (int) min.get(0, 0);
        int minY = (int) min.get(0, 1);
        int minZ = (int) min.get(0, 2);

        int maxX = (int) max.get(0, 0);
        int maxY = (int) max.get(0, 1);
        int maxZ = (int) max.get(0, 2);

        ArrayList<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < neuronsLocation.getRows(); i++) {
            if (neuronsLocation.get(i, 0) == minX || neuronsLocation.get(i, 0) == maxX || neuronsLocation.get(i, 1) == minY || neuronsLocation.get(i, 1) == maxY || neuronsLocation.get(i, 2) == minZ || neuronsLocation.get(i, 2) == maxZ) {
                indexes.add(i);
            }
        }
        Collections.shuffle(indexes);
        for (int i = 0; i < numInputs; i++) {
            inputLocations.setRow(i, neuronsLocation.getVecRow(indexes.get(i)));
        }
        return inputLocations;
    }

    /**
     * @return the neuronsCoordinates
     */
    public Matrix getNeuronsCoordinates() {
        return neuronsCoordinates;
    }

    /**
     * @param neuronsCoordinates the neuronsCoordinates to set
     */
    public void setNeuronsCoordinates(Matrix neuronsCoordinates) {
        this.neuronsCoordinates = neuronsCoordinates;
    }

    /**
     * @return the numFeatures
     */
    public int getNumFeatures() {
        return numFeatures;
    }

    /**
     * @param numFeatures the numFeatures to set
     */
    public void setNumFeatures(int numFeatures) {
        this.numFeatures = numFeatures;
    }

    @Override
    public String toString() {
        return "Random matching";
    }

}
