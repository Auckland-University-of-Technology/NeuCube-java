/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.classifiers;

import java.util.HashMap;
import java.util.Map;
import jneucube.tasks.Tasks;
import jneucube.util.Matrix;

import jneucube.util.Util;


/**
 *
 * @author Josafath Israel Espinosa Ramos
 */
public class wKNNgeneral {

    private int k = 4;
    /**
     * The task to perform: Classification or regression
     */
    private int task = Tasks.CLASSIFICATION;

    

    /**
     * The object that calculates a distance function or metric: e.g. Euclidian,
     * Distance-weighted, etc. If no distance is specified, the function
     * calculates the weighted euclidian distance between two neurons using the
     * synaptic weights.
     */
    /**
     *
     * @param trainingSet N x D | N: number of samples, D : dimension of the
     * sample
     * @param trainingSetclasses vector (N x 1 matrix)
     * @param validationSample vector (1 x D matrix)
     * @return
     */
    public double classifySample(double[][] trainingSet, double[] trainingSetclasses, double [] validationSample) {
        double classLabel = 0.0;
        Matrix distances = new Matrix(trainingSet.length, 2, 0.0);
        for (int r = 0; r < trainingSet.length; r++) {
            distances.set(r, 0, trainingSetclasses[r]);  // label
            distances.set(r, 1, getDistance(validationSample, trainingSet[r]) );    // value
        }
        Util.quickSort(distances.getData(), 0, distances.getRows() - 1, 1);    // Sort by distance (second column) in an ascendant order
        distances = distances.getRows(0, k);
        if (this.task == Tasks.CLASSIFICATION) {
            classLabel = this.getLabelForClassification(distances.getData());
        } else {
            classLabel = this.getLabelForRegression(distances.getData());
        }
        return classLabel;
    }

    /**
     * This function counts the number of events for each class and returns the
     * class with the highest number of events. This function is executed when
     * the task is a classification problem.
     *
     * @param labelDistanceList An mx2 matrix that temporally holds the class
     * label of a neuron and the distance from the neuron to be classified.
     * First column the label, second column the distances
     * @return the class to which a neuron belongs to
     */
    public double getLabelForClassification(double[][] labelDistanceList) {
        Map<Integer, Double> map = new HashMap();
        double classLabel = 0;
        double sum = 0.0;
        for (double[] labelDistanceList1 : labelDistanceList) {            
            sum = 1.0 / labelDistanceList1[1];
            if (map.containsKey((int) labelDistanceList1[0])) {
                sum += map.get((int) labelDistanceList1[0]);
            }
            map.put((int) labelDistanceList1[0], sum);
        }
        sum = 0.0;
        for (Map.Entry<Integer, Double> entry : map.entrySet()) {  // Calculates the arg max
            if (entry.getValue() > sum) {
                sum = entry.getValue();
                classLabel = entry.getKey();
            }
        }
        return classLabel;
    }

    /**
     * This function counts the number of events for each class and returns the
     * class with the highest number of events. This function is executed when
     * the task is a classification problem.
     *
     * @param labelDistanceList An mx2 matrix that temporally holds the class
     * label of a neuron and the distance from the neuron to be classified.
     * First column the label, second column the distances
     * @return the class to which a neuron belongs to
     */
    public double getLabelForClassification2(double[][] labelDistanceList) {
        Map<Integer, Integer> map = new HashMap();
        double classLabel = 0;
        int count;
        for (double[] labelDistanceList1 : labelDistanceList) {            
            count = 0;
            if (map.containsKey((int) labelDistanceList1[0])) {
                count = map.get((int) labelDistanceList1[0]);
            }
            count++;
            map.put((int) labelDistanceList1[0], count);
        }
        count = 0;
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {  // Calculates the arg max
            if (entry.getValue() > count) {
                count = entry.getValue();
                classLabel = entry.getKey();
            }
        }
        return classLabel;
    }

    /**
     * This function calculates and returns the average distance among the k
     * nearest neighbors. This function is executed when the task is a
     * regression problem.
     *
     * @param labelDistanceList Column 0 class, column 1 distance
     * @return
     */
    public double getLabelForRegression(double[][] labelDistanceList) {
        double value = 0.0;
        for (double[] labelDistanceList1 : labelDistanceList) {
            value += labelDistanceList1[1];
        }
        return value / labelDistanceList.length;
    }

    /**
     *
     * @param point1 
     * @param point2
     * @return
     */
    public double getDistance(double []point1, double[] point2) {
        double distance = 0.0;
        for (int i = 0; i < point1.length; i++) {
            distance += Math.pow(point1[i] - point2[i], 2.0);            
        }
        distance = Math.sqrt(distance);
        return distance;
    }

    public static void main(String args[]) {
        double[][] trainingSet = {{5.0, 6.0}, {9.0, 5.0}, {10.0, 9.0}, {4.0, 10.0}, {8.0, 10.0}};
        double[][] trainingSetCasses = {{1.0, 1.0, 1.0, 2.0, 2.0}};
        double[][] validationSet = {{6.0, 8.0}, {9.0, 7.0}};
        wKNNgeneral classifier = new wKNNgeneral();

        double classLabel=classifier.classifySample(trainingSet, trainingSetCasses[0], validationSet[0]);
                
//        double labelDistance[][] = {{1.0, 0.2}, {2.0, 0.4}, {1.0, 0.2}, {1.0, 0.1}, {2.0, 0.4}};
//        Matrix distances = new Matrix(labelDistance);
//        Util.quickSort(distances.getData(), 0, distances.getRows() - 1, 1);
//        distances = distances.getRows(0, 4);
//        double classLabel = classifier.getLabelForClassification2(distances.getData());

        System.out.println(classLabel);
    }
}
