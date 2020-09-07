/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.crossValidation;

import java.util.ArrayList;
import java.util.HashMap;
import jneucube.data.DataSample;
import jneucube.util.ConfusionMatrix;
import jneucube.util.Matrix;
import jneucube.util.Util;

/**
 *
 * @author Josafath Israel Espinosa Ramos Knowledge Engineering and Discovery
 * Research Institute (September 2016)
 */
public abstract class CrossValidation {

    public static final Kfold K_FOLD = new Kfold();
    public static final MonteCarlo MONTE_CARLO = new MonteCarlo();
    public static final LeaveOneOut LEAVE_ONE_OUT = new LeaveOneOut();

    private int numTrainingSamples = 0;
    private int numValidationSamples = 0;
    private int currentFold = 0;
    HashMap<Integer, ArrayList<DataSample>> folds = new HashMap();  // The hash map containing the validation folds
    private ArrayList<Matrix> confusionMatrices = new ArrayList<>();// Confusion matrices for each class
    private ArrayList<Matrix> confusionTables = new ArrayList<>();  // Confusion tables for each fold
    private ArrayList<Double> rmseVector = new ArrayList<>();               // Root mean square error for each fold
    private ArrayList<Matrix> regressionMatrices = new ArrayList<>();       // Root mean square error for each fold

    private double overallAccuracy = 0.0;
    private double overallRecall = 0.0;
    private double overallSpecificity = 0.0;
    private double overallPrecision = 0.0;
    private double overallF1 = 0.0;
    private double overallInformedness = 0.0;
    private double overallRMSE = 0.0;

    public CrossValidation() {

    }

    public abstract void split(HashMap<Double, ArrayList<DataSample>> dataClasses);

    public abstract ArrayList<DataSample> getTrainingData(int numFold);

    public abstract ArrayList<DataSample> getValidationData(int numFold);

    public abstract ArrayList<DataSample> getTrainingData();

    public abstract ArrayList<DataSample> getValidationData();

    public void runStatistics(ArrayList<DataSample> trainingData) {

    }

    public HashMap<Integer, ArrayList<DataSample>> getFolds() {
        return this.folds;
    }

    public int getNumFolds() {
        return this.folds.size();
    }

    public void clearStatistics() {
        this.confusionMatrices = new ArrayList<>();// Confusion matrices for each class
        this.confusionTables = new ArrayList<>();  // Confusion tables for each fold
        this.overallAccuracy = 0.0;
        this.overallRecall = 0.0;
        this.overallSpecificity = 0.0;
        this.overallPrecision = 0.0;
        this.overallF1 = 0.0;
        this.overallInformedness = 0.0;
    }

    /**
     * Calculates the statistics of validation data (confusion matrices,
     * confusion tables, and their derivations) for classification task.
     *
     * @param numFold
     */
    public void runStatistics(int numFold) {
        ConfusionMatrix foldConfusionMatrix = this.getConfusionMatrix(numFold);
        this.confusionMatrices.add(foldConfusionMatrix);

        double tempAccuracy = foldConfusionMatrix.getAccuracy();
        double tempRecall = foldConfusionMatrix.getRecall();
        double tempSpecificity = foldConfusionMatrix.getSpecificity();
        double tempPrecision = foldConfusionMatrix.getPrecision();
        double tempF1score = foldConfusionMatrix.getF1score();
        double tempInformedness = foldConfusionMatrix.getInformedness();

        this.overallAccuracy += (tempAccuracy / this.folds.size());
        this.overallRecall += (tempRecall / this.folds.size());
        this.overallSpecificity += (tempSpecificity / this.folds.size());
        this.overallPrecision += (tempPrecision / this.folds.size());
        this.overallF1 += (tempF1score / this.folds.size());
        this.overallInformedness += (tempInformedness / this.folds.size());
    }

    /**
     * Creates a confusion matrix with the validation data of the corresponding
     * fold.
     *
     * @param numFold The number of fold that contains the validation data
     * @return a n by n matrix where n is the number of classes in the data set
     */
    public ConfusionMatrix getConfusionMatrix(int numFold) {
        double[] actualValues = new double[this.getValidationData(numFold).size()];
        double[] predictedValues = new double[this.getValidationData(numFold).size()];

        ArrayList<DataSample> validationData = this.getValidationData(numFold);
        for (int i = 0; i < validationData.size(); i++) {
            actualValues[i] = validationData.get(i).getClassId();
            predictedValues[i] = validationData.get(i).getValidationClassId();
        }
        ConfusionMatrix m = new ConfusionMatrix(actualValues, predictedValues);
        return m;
    }

    /**
     * Creates a confusion matrix using a validation matrix previously created
     * after running the validation process while running an experiment
     * {@link jneucube.cube.NeuCubeController#runExperiment(java.util.ArrayList, java.util.ArrayList)}.
     *
     * @param validationMatrix the validation matrix which columns indicate the
     * id, the actual class, the predicted class, and classification status (0)
     * no error, (1) error
     * @return
     */
    public ConfusionMatrix getConfusionMatrix(Matrix validationMatrix) {
        ConfusionMatrix m = new ConfusionMatrix(validationMatrix.getVecCol(1), validationMatrix.getVecCol(2));
        return m;
    }

    /**
     * Calculates the root mean square error (RMSE) between the actual and the
     * predicted output in a regression task. The result of the regression is
     * stored in a n-by-2 matrix, where m is the number of samples of the
     * validation data, the actual and predicted values are recorded in the
     * first and second column of the matrix respectively.
     *
     * @param numFold the number of the validation set
     */
    public void calculateRegression(int numFold) {
        ArrayList<DataSample> validationData = this.getValidationData(numFold);
        Matrix matrix = new Matrix(validationData.size(), 2);
        double sum = 0.0;
        double rmse = 0.0;
        for (int i = 0; i < validationData.size(); i++) {
            matrix.set(i, 0, validationData.get(i).getClassId());
            matrix.set(i, 1, validationData.get(i).getValidationClassId());
            sum += (validationData.get(i).getValidationClassId() - validationData.get(i).getClassId()) * (validationData.get(i).getValidationClassId() - validationData.get(i).getClassId());
        }
        rmse = Math.sqrt(sum / validationData.size());
        this.regressionMatrices.add(matrix);
        this.rmseVector.add(rmse);
        this.overallRMSE += rmse / this.folds.size();
    }

    /**
     * Shows the regression result of the validation set.
     *
     * @param numFold the fold
     */
    public void showRegression(int numFold) {
        this.getRegressionMatrices().get(numFold).show();
        System.out.println("RMSE " + this.rmseVector.get(numFold));
    }

    /**
     * Shows the confusion matrices
     */
    public void showConfusionMatrices() {
        for (int i = 0; i < this.confusionMatrices.size(); i++) {
            showConfusionMatrix(i);
        }
    }

    /**
     * Shoes the confusion tables
     */
    public void showConfusionTables() {
        for (int i = 0; i < this.confusionTables.size(); i++) {
            showConfusionTable(i);
        }
    }

    /**
     * Show the confusion matrix
     *
     * @param fold
     */
    public void showConfusionMatrix(int fold) {
        this.confusionMatrices.get(fold).show();
    }

    public void showConfusionTable(int fold) {
        this.confusionTables.get(fold).show();
    }

    /**
     * Calculates the overall confusion matrix of all the experiments performed
     * in the cross validation process. It calculates the mean of the sum of all
     * confusion matrices generated during the experiments.
     *
     * @return a confusion matrix
     */
    public Matrix getOverallConfusionMatrix() {

        Matrix temp = new Matrix(this.confusionMatrices.get(0).getRows(), this.confusionMatrices.get(0).getCols(), 0.0);
        for (Matrix confusionMatrix : this.confusionMatrices) {
            temp = temp.operation('+', confusionMatrix);
        }
        temp = temp.operation('/', this.confusionMatrices.size());
        return temp;
    }

    /**
     * Calculates the overall table of confusion of all the experiments
     * performed in the cross validation process. It calculates the mean of the
     * sum of all table of confusion generated during the experiments.
     *
     * @return a confusion table
     */
    public Matrix getOverallConfusionTable() {
        Matrix temp = new Matrix(2, 2, 0.0);
        for (Matrix confusionTable : this.confusionTables) {
            temp = temp.operation('+', confusionTable);
        }
        temp = temp.operation('/', this.confusionTables.size());
        return temp;
    }

    /**
     * Shows the some important measures of the cross validation method.
     */
    public void showOverallMetrics() {
        System.out.println("Overall accuracy: " + this.getOverallAccuracy());
        System.out.println("Overall recall: " + this.getOverallRecall());
        System.out.println("Overall specificity: " + this.getOverallSpecificity());
        System.out.println("Overall precision: " + this.getOverallPrecision());
        System.out.println("Overall F1: " + this.getOverallF1());
        System.out.println("Overall informedness: " + this.getOverallInformedness());
    }

    public StringBuffer getOverallMetricsToString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Overall Accuracy: ").append(this.getOverallAccuracy()).append(System.lineSeparator());
        sb.append("Overall Recall: ").append(this.getOverallRecall()).append(System.lineSeparator());
        sb.append("Overall Specificity: ").append(this.getOverallSpecificity()).append(System.lineSeparator());
        sb.append("Overall Precision: ").append(this.getOverallPrecision()).append(System.lineSeparator());
        sb.append("Overall F1: ").append(this.getOverallF1()).append(System.lineSeparator());
        sb.append("Overall Informedness: ").append(this.getOverallInformedness()).append(System.lineSeparator());
        return sb;
    }

    public void exportOverallMetrics(String fileName) {
        Util.saveStringToFile(this.getOverallMetricsToString(), fileName);
    }

    public void clear() {
        numTrainingSamples = 0;
        numValidationSamples = 0;
        currentFold = 0;
        this.folds.clear();

        this.confusionMatrices.clear();
        this.confusionTables.clear();

        this.overallAccuracy = 0;
        this.overallRecall = 0;
        this.overallSpecificity = 0;
        this.overallPrecision = 0;
        this.overallF1 = 0;
    }

    /**
     * @return the rmseVector
     */
    public ArrayList<Double> getRmseVector() {
        return rmseVector;
    }

    /**
     * @param rmseVector the rmseVector to set
     */
    public void setRmseVector(ArrayList<Double> rmseVector) {
        this.rmseVector = rmseVector;
    }

    /**
     * @return the overallAccuracy
     */
    public double getOverallAccuracy() {
        return overallAccuracy;
    }

    /**
     * @param overallAccuracy the overallAccuracy to set
     */
    public void setOverallAccuracy(double overallAccuracy) {
        this.overallAccuracy = overallAccuracy;
    }

    /**
     * @return the overallRecall
     */
    public double getOverallRecall() {
        return overallRecall;
    }

    /**
     * @param overallRecall the overallRecall to set
     */
    public void setOverallRecall(double overallRecall) {
        this.overallRecall = overallRecall;
    }

    /**
     * @return the overallSpecificity
     */
    public double getOverallSpecificity() {
        return overallSpecificity;
    }

    /**
     * @param overallSpecificity the overallSpecificity to set
     */
    public void setOverallSpecificity(double overallSpecificity) {
        this.overallSpecificity = overallSpecificity;
    }

    /**
     * @return the overallPrecision
     */
    public double getOverallPrecision() {
        return overallPrecision;
    }

    /**
     * @param overallPrecision the overallPrecision to set
     */
    public void setOverallPrecision(double overallPrecision) {
        this.overallPrecision = overallPrecision;
    }

    /**
     * @return the overallF1
     */
    public double getOverallF1() {
        return overallF1;
    }

    /**
     * @param overallF1 the overallF1 to set
     */
    public void setOverallF1(double overallF1) {
        this.overallF1 = overallF1;
    }

    /**
     * @return the confusionTables
     */
    public ArrayList<Matrix> getConfusionTables() {
        return confusionTables;
    }

    /**
     * @param confusionTables the confusionTables to set
     */
    public void setConfusionTables(ArrayList<Matrix> confusionTables) {
        this.confusionTables = confusionTables;
    }

    /**
     * @return the confusionMatrices
     */
    public ArrayList<Matrix> getConfusionMatrices() {
        return confusionMatrices;
    }

    /**
     * @param confusionMatrices the confusionMatrices to set
     */
    public void setConfusionMatrices(ArrayList<Matrix> confusionMatrices) {
        this.confusionMatrices = confusionMatrices;
    }

    /**
     * @return the numTrainingSamples
     */
    public int getNumTrainingSamples() {
        return numTrainingSamples;
    }

    /**
     * @param numTrainingSamples the numTrainingSamples to set
     */
    public void setNumTrainingSamples(int numTrainingSamples) {
        this.numTrainingSamples = numTrainingSamples;
    }

    /**
     * @return the numValidationSamples
     */
    public int getNumValidationSamples() {
        return numValidationSamples;
    }

    /**
     * @param numValidationSamples the numValidationSamples to set
     */
    public void setNumValidationSamples(int numValidationSamples) {
        this.numValidationSamples = numValidationSamples;
    }

    /**
     * @return the currentFold
     */
    public int getCurrentFold() {
        return currentFold;
    }

    /**
     * @param currentFold the currentFold to set
     */
    public void setCurrentFold(int currentFold) {
        this.currentFold = currentFold;
    }

    /**
     * @return the overallInformedness
     */
    public double getOverallInformedness() {
        return overallInformedness;
    }

    /**
     * @param overallInformedness the overallInformedness to set
     */
    public void setOverallInformedness(double overallInformedness) {
        this.overallInformedness = overallInformedness;
    }

    /**
     * @return the overallRMSE
     */
    public double getOverallRMSE() {
        return overallRMSE;
    }

    /**
     * @param overallRMSE the overallRMSE to set
     */
    public void setOverallRMSE(double overallRMSE) {
        this.overallRMSE = overallRMSE;
    }

    /**
     * @return the regressionMatrices
     */
    public ArrayList<Matrix> getRegressionMatrices() {
        return regressionMatrices;
    }

    /**
     * @param regressionMatrices the regressionMatrices to set
     */
    public void setRegressionMatrices(ArrayList<Matrix> regressionMatrices) {
        this.regressionMatrices = regressionMatrices;
    }

    /**
     * Gets an m-by-2 matrix that contains the regression result, where m is the
     * number of samples of the validation data, the actual and predicted values
     * are recorded in the first and second column of the matrix respectively.
     * The number of fold is according the number of experiments performed,
     * which it is usually one, the the index is zero.
     *
     * @param fold the number of the fold of a set of experiments, usually 1
     * experiment
     * @return a n-by-2 matrix
     */
    public Matrix getRegressionMatrix(int fold) {
        return this.regressionMatrices.get(fold);
    }

}
