/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The {@code ConfusionMatrix} class that allows visualization of the
 * performance of a classifier. Each row of the matrix represents the instances
 * in an actual class while each column represents the instances in a predicted
 * class. It extends the {@link Matrix} class.
 *
 * @author Josafath Israel Espinosa Ramos
 */
public class ConfusionMatrix extends Matrix {

    Matrix confusionTable;
    private Matrix errorMatrix;

    /**
     * Creates a confusion matrix determined by the actual and predicted groups
     * in the vectors actualValues and precictedValues, respectively. Each row
     * of the matrix represents the instances in an actual class while each
     * column represents the instances in a predicted class.
     *
     * @param actualValues a vector which elements indicate the
     * @param predictedValues a vector
     */
    public ConfusionMatrix(double[] actualValues, double[] predictedValues) {
        this.errorMatrix = this.createErrorMatrix(actualValues, predictedValues);
        this.calculateConfusionMatrix(this.errorMatrix.getVecCol(0), this.errorMatrix.getVecCol(1));
    }

    /**
     * Calculates the confusion matrix given a n-by-2 classification matrix
     * where the vector columns indicate the actual and predicted values
     * respectively.
     *
     * @param classificationMatrix
     */
    public ConfusionMatrix(Matrix classificationMatrix) {
        this.errorMatrix = this.createErrorMatrix(classificationMatrix.getVecCol(0), classificationMatrix.getVecCol(1));
        this.calculateConfusionMatrix(errorMatrix.getVecCol(0), this.errorMatrix.getVecCol(1));
    }

    /**
     * Creates a confusion matrix determined by the actual and predicted groups
     * in the vectors actualValues and precictedValues, respectively. Each row
     * of the matrix represents the instances in an actual class while each
     * column represents the instances in a predicted class.
     *
     * @param sampleIds
     * @param actualValues a vector which elements indicate the
     * @param predictedValues a vector
     */
    public ConfusionMatrix(int[] sampleIds, double[] actualValues, double[] predictedValues) {
        this.errorMatrix = this.createErrorMatrix(sampleIds,actualValues, predictedValues);
        this.calculateConfusionMatrix(this.errorMatrix.getVecCol(1), this.errorMatrix.getVecCol(2));
    }

    private Matrix createErrorMatrix(double[] actualValues, double[] predictedValues) {
        Matrix matrix = new Matrix(actualValues.length, 3);
        matrix.setCol(0, actualValues);
        matrix.setCol(1, predictedValues);
        for (int i = 0; i < matrix.getRows(); i++) {
            matrix.set(i, 2, (matrix.get(i, 0) == matrix.get(i, 1)) ? 0.0 : 1.0);
        }
        return matrix;
    }

    private Matrix createErrorMatrix(int[] sampleIds,double[] actualValues, double[] predictedValues) {
        Matrix matrix = new Matrix(actualValues.length, 4);
        double[] doublesIds= Arrays.stream(sampleIds).asDoubleStream().toArray();
        matrix.setCol(0, doublesIds);
        matrix.setCol(1, actualValues);
        matrix.setCol(2, predictedValues);
        for (int i = 0; i < matrix.getRows(); i++) {
            matrix.set(i, 3, (matrix.get(i, 1) == matrix.get(i, 2)) ? 0.0 : 1.0);
        }
        return matrix;
    }

    /**
     * Calculates the confusion matrix from a vector of actual values and a
     * vector of predicted values.
     *
     * @param actualValues
     * @param predictedValues
     */
    private void calculateConfusionMatrix(double[] actualValues, double[] predictedValues) {

        HashMap<Double, Integer> classMap = this.getClassMap(Util.catArray(actualValues, predictedValues));
        int numClasses = classMap.size();

        List<Double> indexes = new ArrayList<>(classMap.keySet());
        Collections.sort(indexes);

        this.setRows(numClasses);
        this.setCols(numClasses);
        this.setData(new double[numClasses][numClasses]);

        for (int i = 0; i < actualValues.length; i++) {
            this.setIncrease(indexes.indexOf(actualValues[i]), indexes.indexOf(predictedValues[i]), 1.0);
        }
        this.calculateConfusionTable();
    }

    /**
     * Creates a map of classes and the number of observations determined by the
     * known groups in the actualValues vector.
     *
     * @param actualValues the vector of actual
     * @return the map of classes
     */
    public HashMap<Double, Integer> getClassMap(double[] actualValues) {
        HashMap<Double, Integer> classMap = new HashMap<>();
        double classId;
        for (int i = 0; i < actualValues.length; i++) {
            classId = actualValues[i];
            if (classMap.containsKey(classId)) {
                classMap.put(classId, classMap.get(classId) + 1);
            } else {
                classMap.put(classId, 1);
            }
        }
        return classMap;
    }

    /**
     * Calculates the table of confusionTable (2x2 confusionTable matrix) of a
     * confusionTable matrix (m x m)
     *
     */
    public void calculateConfusionTable() {
        this.confusionTable = new Matrix(2, 2, 0.0);
        Matrix temp;
        for (int i = 0; i < this.getRows(); i++) {
            temp = new Matrix(2, 2, 0.0);
            //temp.setIncrease(i, i, i);

            temp.set(0, 0, this.get(i, i));
            temp.set(0, 1, this.getRow(i).sum() - this.get(i, i));
            temp.set(1, 0, this.getCol(i).sum() - this.get(i, i));
            temp.set(1, 1, this.diagonal().sum() - this.get(i, i));
            confusionTable = confusionTable.operation('+', temp);
        }
        confusionTable.operation('/', this.getRows());
    }

    /**
     * Calculates the accuracy from the confusion matrix
     *
     * @return the accuracy
     */
    public double getAccuracy() {
        if (this.getRows() == 2) {
            return this.getAccuracy(this);
        } else if (this.getRows() > 2) {
            return this.getAccuracy(this.confusionTable);
        }
        return 0.0;
    }

    /**
     * Calculates the accuracy of the classification.
     * (TP+TN) / (TP+TN+FP+FN)
     *
     * @param m
     * @return the accuracy
     */
    public double getAccuracy(Matrix m) {
        return m.diagonal().sum() / m.sum(1).sum();
    }

    /**
     * The recall (also called sensitivity, true positive rate, or probability
     * of detection) method metrics the proportion of actual positives that are
     * correctly identified as such.
     *
     * @return the recall value
     */
    public double getRecall() {
        if (this.getRows() == 2) {
            return this.getRecall(this);
        } else if (this.getRows() > 2) {
            return this.getRecall(this.confusionTable);
        }
        return 0.0;
    }

    /**
     * Calculates the recall metric from the table of confusion.
     * TP/ TP+FN
     * @param m a 2 by 2 matrix that describes the table of confusion.
     * @return the recall value
     */
    private double getRecall(Matrix m) {
        return m.get(0, 0) / (m.get(0, 0) + m.get(1, 0));
    }

    /**
     * The specificity (also called the true negative rate) metrics the
     * proportion of actual negatives that are correctly identified as such.
     *
     * @return the specificity value
     */
    public double getSpecificity() {
        if (this.getRows() == 2) {
            return this.getSpecificity(this);
        } else if (this.getRows() > 2) {
            return this.getSpecificity(this.confusionTable);
        }
        return 0.0;
    }

    /**
     * Calculates the specificity metric from the table of confusion.
     *
     * @param m a 2 by 2 matrix that describes the table of confusion.
     * @return the recall value
     */
    private double getSpecificity(Matrix m) {
        return m.get(1, 1) / (m.get(1, 1) + m.get(0, 1));
    }

    /**
     * Calculates the precision metric of the table of confusion.
     *
     * @return the precision value
     */
    public double getPrecision() {
        if (this.getRows() == 2) {
            return this.getPrecision(this);
        } else if (this.getRows() > 2) {
            return this.getPrecision(this.confusionTable);
        }
        return 0.0;
    }

    /**
     * Calculates the precision metric from the table of confusion.
     *
     * @param m a 2 by 2 matrix that describes the table of confusion.
     * @return the recall value
     */
    private double getPrecision(Matrix m) {
        return m.get(0, 0) / (m.get(0, 0) + m.get(0, 1));
    }

    /**
     * Calculates the F1 score metric of a test's accuracy.
     *
     * @return the F1 metric
     */
    public double getF1score() {
        if (this.getRows() == 2) {
            return this.getF1score(this);
        } else if (this.getRows() > 2) {
            return this.getF1score(this.confusionTable);
        }
        return 0.0;
    }

    /**
     * Calculates the F1 score metric from the table of confusion.
     *
     * @param m a 2 by 2 matrix that describes the table of confusion.
     * @return the recall value
     */
    private double getF1score(Matrix m) {
        return (2 * m.get(0, 0)) / ((2 * m.get(0, 0)) + m.get(0, 1) + m.get(1, 0));
    }

    /**
     * Calculates the informedness metric from the table of confusion.
     *
     * @return
     */
    public double getInformedness() {
        return this.getRecall() + this.getSpecificity() - 1;
    }

    public double getErrors() {
        Matrix matrix = this.errorMatrix.sum(1);
        return matrix.get(0, matrix.getCols()-1); //
    }

    /**
     * @return the confusionTable
     */
    public Matrix getConfusionTable() {
        return confusionTable;
    }

    /**
     * @param confusionTable the confusionTable to set
     */
    public void setConfusionTable(Matrix confusionTable) {
        this.confusionTable = confusionTable;
    }

    public void showMetrics() {
        System.out.println("Accuracy: " + this.getAccuracy());
        System.out.println("Recall: " + this.getRecall());
        System.out.println("Specificity: " + this.getSpecificity());
        System.out.println("Precision: " + this.getPrecision());
        System.out.println("F1: " + this.getF1score());
        System.out.println("Informedness: " + this.getInformedness());
    }

    public StringBuffer getMetricsToString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Accuracy: ").append(this.getAccuracy()).append(System.lineSeparator());
        sb.append("Recall: ").append(this.getRecall()).append(System.lineSeparator());
        sb.append("Specificity: ").append(this.getSpecificity()).append(System.lineSeparator());
        sb.append("Precision: ").append(this.getPrecision()).append(System.lineSeparator());
        sb.append("F1: ").append(this.getF1score()).append(System.lineSeparator());
        sb.append("Informedness: ").append(this.getInformedness()).append(System.lineSeparator());
        return sb;
    }

    public void exportMetrics(String fileName) {
        StringBuffer sb = this.getMetricsToString();
        this.export(fileName, sb);
    }

    /**
     * @return the errorMatrix
     */
    public Matrix getErrorMatrix() {
        return errorMatrix;
    }

    /**
     * @param errorMatrix the errorMatrix to set
     */
    public void setErrorMatrix(Matrix errorMatrix) {
        this.errorMatrix = errorMatrix;
    }

    public static void main(String args[]) {
        try {
            //Matrix m = new Matrix("C:\\DataSets\\confusionTest.csv", ",");
            //ConfusionMatrix cm = new ConfusionMatrix(m.getVecCol(0), m.getVecCol(1));
            
            //Matrix m = new Matrix("C:\\DataSets\\PigeonConfusionTest.csv", ",");
            
            Matrix m = new Matrix("H:\\DataSets\\pigeons\\Experiments\\Experiments 20200206\\SingleNeuron\\LIF\\OverTotal\\Pigeon1\\run1\\CrossValidationErrorMatrix.csv",",");
            
            ConfusionMatrix cm = new ConfusionMatrix(m.getVecCol(1), m.getVecCol(2));
            
            cm.show();
            System.out.println("");
            cm.getConfusionTable().show();
            System.out.println("");
            cm.showMetrics();

        } catch (IOException ex) {
            System.out.println(ex);
        }

    }

}
