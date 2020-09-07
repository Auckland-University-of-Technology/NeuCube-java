/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.classifiers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jneucube.tasks.Tasks;
import jneucube.distances.Distance;
import jneucube.util.Matrix;
import jneucube.util.Util;

/**
 * The {@code KNN} class implements the K-nearest neighbors (KNN) classification
 * algorithm.
 *
 * @author Josafath Israel Espinosa Ramos
 */
public class KNN extends Classifier {
    //Logger _log = LogManager.getLogger(KNN.class);
    
    /**
     * The number of nearest neighbors
     */
    public int k = 3;
    /**
     * The task to perform: Classification or regression
     */
    private int task = Tasks.CLASSIFICATION;
    /**
     * The object that calculates a distance function or metric: e.g. Euclidian,
     * Distance-weighted, etc. If no distance is specified, the function
     * calculates the euclidian distance between two neurons using the synaptic
     * weights.
     */
    public Distance distance = Distance.EUCLIDIAN_DISTANCE;

    public KNN() {

    }

    public KNN(int k) {
        this.k = k;
    }

    public KNN(int k, Distance distance) {
        this.k = k;
        this.distance = distance;
    }

    /**
     * This function classifies a sample of features using a training set and
     * their corresponding labels.
     *
     * @param sample A vector (1-by-n matrix) of features to be classified. It must have
     * the same number of columns as the training matrix.
     * @param training A m-by-n matrix used to group the rows in the matrix
     * Sample. This matrix must have the same number of columns as the sample.
     * Each row of the training matrix belongs to the group whose value is the
     * corresponding entry of the group matrix.
     * @param group A vector (m-by-1 matrix) of classes whose distinct values
     * define the grouping (classes) of the rows in the training matrix.
     * @return the label or the value to which the sample belongs to
     */
    @Override
    public double classify(Matrix sample, Matrix training, Matrix group) {
        double classLabel = 0;
        if (training.getRows() > this.k) {
            double[][] labelDistanceList = this.getLabeledDistances(sample, training, group);// column 1=class, column 2= distance
            Util.quickSort(labelDistanceList, 0, labelDistanceList.length - 1, 1);    // Sort by distance (second column) in an ascendant order
            if (this.task == Tasks.CLASSIFICATION) {
                classLabel = this.getLabelForClassification(labelDistanceList);
            } else {
                classLabel = this.getLabelForRegression(labelDistanceList);
            }
            return classLabel;
        } else {
            return -1;
        }
    }

    /**
     * This function calculates the probability that a sample belongs to
     * different classes.
     *
     * @param sample  A vector (1-by-n matrix) of features to be classified. It must have
     * the same number of columns as the training matrix.
     * @param training A m-by-n matrix used to group the rows in the matrix
     * Sample. This matrix must have the same number of columns as the sample.
     * Each row of the training matrix belongs to the group whose value is the
     * corresponding entry of the group matrix.
     * @param group A vector (m-by-1 matrix) of classes whose distinct values
     * define the grouping of the rows in the training matrix.
     * @return A map that contains the number of neighbors per class.
     */
    @Override
    public HashMap<Integer, Double> getProbabilities(Matrix sample, Matrix training, Matrix group) {
        double[][] labelDistanceList = this.getLabeledDistances(sample, training, group);
        Util.quickSort(labelDistanceList, 0, labelDistanceList.length - 1, 1);    // Sort by distance (second column) in an ascendant order
        HashMap<Integer, Double> map = new HashMap();
        map = this.getEvents(labelDistanceList);
        return map;
    }

    /**
     * This function calculates the distances between a sample and all the
     * samples in the training set, then assign the class to the corresponding
     * distance.
     *
     * @param sample A 1 by m matrix of features to be classified. It must have
     * the same number of columns as the training matrix.
     * @param training A n by m Matrix used to group the rows in the matrix
     * Sample. This matrix must have the same number of columns as the sample.
     * Each row of the training matrix belongs to the group whose value is the
     * corresponding entry of the group matrix.
     * @param group A 1 by m matrix (vector of classes) whose distinct values
     * define the grouping of the rows in the training matrix.
     * @return a n by 2 matrix where the first column indicates the class label
     * and the second the distance between the sample and a sample of the
     * training set.
     */
    private double[][] getLabeledDistances(Matrix sample, Matrix training, Matrix group) {
        double[][] labelDistanceList = new double[training.getRows()][2];
        double d;
        for (int i = 0; i < training.getRows(); i++) {
            d = this.distance.getDistance(training.getVecRow(i), sample.getVecRow(0));
            labelDistanceList[i][0] = group.get(i, 0);   // the class label
            labelDistanceList[i][1] = d;                // the distance
        }
        return labelDistanceList;
    }

    /**
     * This function calculates the occurrences per class of the k nearest
     * neighbors and gives the percent of the label assigned to the .At 
     *
     * @param labelDistanceList A n by 2 matrix that contains the labels (1st column) and
     * distances (2nd column)
     * @return a map that contains the k-nearest neighbors occurrences of the
     * classes
     */
    private HashMap<Integer, Double> getEvents(double[][] labelDistanceList) {
        HashMap<Integer, Double> map = new HashMap();
        double count;
        for (int i = 0; i < this.k; i++) {  // Counts the number of class labels
            count = 0.0;
            if (map.containsKey((int) labelDistanceList[i][0])) {
                count =  map.get((int) labelDistanceList[i][0]);
            }
            count++;
            map.put((int) labelDistanceList[i][0], count);
        }    
        return map;
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
        double count;
        map = this.getEvents(labelDistanceList);
        count = 0;
        for (Map.Entry<Integer, Double> entry : map.entrySet()) {  // Calculates the arg max
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
     * @param labelDistanceList
     * @return
     */
    private double getLabelForRegression(double[][] labelDistanceList) {
        double value = 0.0;
        for (int i = 0; i < this.k; i++) {
            value += labelDistanceList[i][0];
        }
        return value / this.k;
    }

    /**
     * @return the k
     */
    public int getK() {
        return k;
    }

    /**
     * @param k the k to set
     */
    public void setK(int k) {
        this.k = k;
    }

    /**
     * @return the task
     */
    public int getTask() {
        return task;
    }

    /**
     * @param task the task to set
     */
    public void setTask(int task) {
        this.task = task;
    }

    /**
     * @return the distance
     */
    public Distance getDistance() {
        return distance;
    }

    /**
     * @param distance the distance to set
     */
    public void setDistance(Distance distance) {
        this.distance = distance;
    }

    public static void main(String args[]) {
        try {
            KNN knn = new KNN(4);
            Matrix data = new Matrix("C:\\DataSets\\Iris\\data.csv", ",");
            Matrix classes = new Matrix("C:\\DataSets\\Iris\\classes.csv", ",");
            Matrix sample = new Matrix("C:\\DataSets\\Iris\\sample.csv", ",");
            double label = knn.classify(sample, data, classes);
            System.out.println(label);
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

}
