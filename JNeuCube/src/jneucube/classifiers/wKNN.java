/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.classifiers;

import java.util.HashMap;
import java.util.Map;
import jneucube.distances.Distance;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 * The {@code wKNN} class implements the weighted K-nearest neighbors (wKNN)
 * classification algorithm. It extends the {@link KNN} and weigh the
 * contribution of each of the k neighbors according to their distance to the
 * query point xq, giving greater weight wi to closer neighbors, where the
 * weight is wi=1/d(xq,xi)^2.
 *
 * @author Josafath Israel Espinosa Ramos
 */
public class wKNN extends KNN {


    public wKNN() {

    }

    public wKNN(int k) {
        this.k = k;
    }

    public wKNN(int k, Distance distance) {
        this.k = k;
        this.distance = distance;
    }

    /**
     * This function weights the contribution of each of the k neighbors according
     * to their distance to the query point xq, giving greater weight wi to
     * closer neighbors, where the weight is wi=1/d(xq,xi)^2. This function is
     * executed when the task is a classification problem.
     *
     * @param labelDistanceList An n by 2 matrix that temporally holds the class
     * label of a neuron and the distance from the neuron to be classified.
     * First column the label, second column the distances
     * @return the class to which a neuron belongs to
     */
    @Override
    public double getLabelForClassification(double[][] labelDistanceList) {
        Map<Integer, Double> mapSumDistances = new HashMap();
        double classLabel = 0;
        double sum = 0.0;
        for (int i = 0; i < this.k; i++) {  // Counts the number of class labels        
            sum = 1 / labelDistanceList[i][1];
            if (mapSumDistances.containsKey((int) labelDistanceList[i][0])) {
                sum += mapSumDistances.get((int) labelDistanceList[i][0]);
            }
            mapSumDistances.put((int) labelDistanceList[i][0], sum);
        }

        sum = 0.0;
        for (Map.Entry<Integer, Double> entry : mapSumDistances.entrySet()) {  // Get the class with the maximum distance        
            if (entry.getValue() > sum) {
                sum = entry.getValue();
                classLabel = entry.getKey();
            }
        }
        return classLabel;
    }

    @Override
    public String toString() {
        return "wKNN";
    }

//    @Override
//    public HashMap<Integer, Integer> getProbabilities(Matrix sample, Matrix training, Matrix group) {
//        double[][] labelDistanceList = this.getLabeledDistances(sample, training, group);
//        Util.quickSort(labelDistanceList, 0, labelDistanceList.length - 1, 1);    // Sort by distance (second column) in an ascendant order
//        HashMap<Integer, Integer> map = new HashMap();
//        map = getProbabilities(labelDistanceList);
//        return map;
//    }
//    /**
//     * This function classifies a sample of features using a training set and
//     * their corresponding labels.
//     *
//     * @param sample A 1 by m matrix of features to be classified. It must have
//     * the same number of columns as the training matrix.
//     * @param training A n by m Matrix used to group the rows in the matrix
//     * Sample. This matrix must have the same number of columns as the sample.
//     * Each row of the training matrix belongs to the group whose value is the
//     * corresponding entry of the group matrix.
//     * @param group A 1 by m matrix (vector of classes) whose distinct values
//     * define the grouping of the rows in the training matrix.
//     * @return the label or the value to which the sample belongs to
//     */
//    @Override
//    public double classify(Matrix sample, Matrix training, Matrix group) {
//        double classLabel = 0;
//        double[][] labelDistanceList = this.getLabeledDistances(sample, training, group);
//        Util.quickSort(labelDistanceList, 0, labelDistanceList.length - 1, 1);    // Sort by distance (second column) in an ascendant order
//        if (this.task == Tasks.CLASSIFICATION) {
//            classLabel = this.getLabelForClassification(labelDistanceList); // weighted             
//        } else {
//            classLabel = this.getLabelForRegression(labelDistanceList);
//        }
//        return classLabel;
//    }
//    /**
//     * This function calculates the distances between a sample and all the
//     * samples in the training set, then assign the class to the corresponding
//     * distance.
//     *
//     * @param sample A 1 by m matrix of features to be classified. It must have
//     * the same number of columns as the training matrix.
//     * @param training A n by m Matrix used to group the rows in the matrix
//     * Sample. This matrix must have the same number of columns as the sample.
//     * Each row of the training matrix belongs to the group whose value is the
//     * corresponding entry of the group matrix.
//     * @param group A 1 by m matrix (vector of classes) whose distinct values
//     * define the grouping of the rows in the training matrix.
//     * @return a n by 2 matrix where the first column indicates the class label
//     * and the second the distance between the sample and a sample of the
//     * training set.
//     */
//    private double[][] getLabeledDistances(Matrix sample, Matrix training, Matrix group) {
//        double[][] labelDistanceList = new double[training.getRows()][2];
//        double d;
//        for (int i = 0; i < training.getRows(); i++) {
//            d = this.distance.getDistance(sample.getRow(0), training.getRow(i));
//            labelDistanceList[i][0] = group.get(i, 0);   // the class label
//            labelDistanceList[i][1] = d;                // the distance
//        }
//        return labelDistanceList;
//    }
//    /**
//     * This function calculates the occurrences per class of the k nearest
//     * neighbors.
//     *
//     * @param labelDistanceList A n by 2 matrix that contains the labels and
//     * distances
//     * @return a map that contains the k-nearest neighbors occurrences of the
//     * classes
//     */
//    private HashMap<Integer, Integer> getProbabilities(double[][] labelDistanceList) {
//        HashMap<Integer, Integer> map = new HashMap();
//        int count;
//        for (int i = 0; i < this.k; i++) {  // Counts the number of class labels
//            count = 0;
//            if (map.containsKey((int) labelDistanceList[i][0])) {
//                count = map.get((int) labelDistanceList[i][0]);
//            }
//            count++;
//            map.put((int) labelDistanceList[i][0], count);
//        }
//        return map;
//    }
//    /**
//     * This function calculates and returns the average distance among the k
//     * nearest neighbors. This function is executed when the task is a
//     * regression problem.
//     *
//     * @param labelDistanceList
//     * @return
//     */
//    public double getLabelForRegression(double[][] labelDistanceList) {
//        double value = 0.0;
//        for (int i = 0; i < this.k; i++) {
//            value += labelDistanceList[i][0];
//        }
//        return value / this.k;
//    }
    
    
//    public static void main(String args[]) {
//        try {
//            String dir = "C:\\DataSets\\benchmark test\\";
//            int sample = 1;
//            Matrix trainSamples = new Matrix(dir + "train_samples" + sample + ".csv", ",");
//            Matrix trainLabels = new Matrix(dir + "train_labels" + sample + ".csv", ",");
//            Matrix testSamples = new Matrix(dir + "test_samples" + sample + ".csv", ",");
//            Matrix desiredLabels = new Matrix(dir + "test_labels" + sample + ".csv", ",");
//            wKNN wknn = new wKNN(3);
//            Matrix predictedLabels = wknn.classify(trainSamples, trainLabels, testSamples);
//            for (int r = 0; r < desiredLabels.getRows(); r++) {
//                System.out.println(desiredLabels.get(r, 0) + "," + predictedLabels.get(r, 0));
//            }
//        } catch (IOException ex) {
//            java.util.logging.Logger.getLogger(wKNN.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }

}
