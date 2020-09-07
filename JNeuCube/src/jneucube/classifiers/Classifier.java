/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.classifiers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import jneucube.util.Matrix;

/**
 *
 * @author Josafath Israel Espinosa Ramos
 */
public abstract class Classifier {

    public static final KNN KNN = new KNN();
    public static final wKNN WKNN = new wKNN();

    public static final ArrayList<Classifier> CLASSIFIER_LIST = new ArrayList<>(Arrays.asList(KNN, WKNN));

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
    public abstract double classify(Matrix sample, Matrix training, Matrix group);

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
    public abstract HashMap<Integer, Double> getProbabilities(Matrix sample, Matrix training, Matrix group);

}
