/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.crossValidation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import jneucube.data.DataSample;
import static jneucube.log.Log.LOGGER;
import jneucube.util.Util;

/**
 * This object splits the data into training and validation sets using the
 * k-fold cross validation method. If kFolds is equals to number of samples then
 * the data is leave-one-out cross validation
 *
 * @author Josafath Israel Espinosa Ramos
 */
public class Kfold extends CrossValidation {

    private int numFolds = 5; // If numFolds= number of samples then the method is leave-one-out cross validation

    

    public Kfold() {

    }

    public Kfold(int numFolds) {
        this.numFolds = numFolds;
    }

    /**
     * This function creates k folds data sets from a map that contains the
     * classes and their respective data samples. Every group of samples is
     * split proportionally, i.e into k fold subsamples, which maintain the
     * proportion when groups contains different number of elements. The
     * elements of each fold are randomly shuffled.
     *
     * @param dataClasses the map with the class label and the data samples that
     * belongs to that label
     */
    @Override
    public void split(HashMap<Double, ArrayList<DataSample>> dataClasses) {
        LOGGER.info("------- Spliting data set using Kfold cross validation -------");
        //this.setNumClasses(dataClasses.size());
        this.folds.clear();
        int count;
        double rate = 1.0 / numFolds;
        int idxs[];
        int start;
        int end;
        ArrayList<DataSample> subSamples;

        for (Map.Entry<Double, ArrayList<DataSample>> entry : dataClasses.entrySet()) { // for each class
            count = (int) Math.round(entry.getValue().size() * rate);
            idxs = Util.getRandomPermutation(entry.getValue().size());
            for (int i = 0; i < numFolds; i++) {
                if (this.folds.containsKey(i)) {
                    subSamples = this.folds.get(i);
                } else {
                    subSamples = new ArrayList<>();
                    this.folds.put(i, subSamples);
                }
                start = i * count;
                if (i == (numFolds - 1)) {
                    end = idxs.length;
                } else {
                    end = start + count;
                }
                for (int j = start; j < end; j++) {
                    subSamples.add(entry.getValue().get(idxs[j]));
                }
                this.setNumValidationSamples(subSamples.size());
                this.setNumTrainingSamples(subSamples.size() * (numFolds - 1));
            }
        }
        this.folds.entrySet().forEach((fold) -> {
            Collections.shuffle(fold.getValue());
        });
        LOGGER.info("------- Spliting complete -------");
    }

    public HashMap<Double, ArrayList<DataSample>> getDataClasses(ArrayList<DataSample> dataset) {
        HashMap<Double, ArrayList<DataSample>> map = new HashMap<>();
        return map;
    }

    /**
     * Selects the training data, i.e. the folds that differs from the selected
     * numFold
     *
     * @param numFold
     * @return
     */
    @Override
    public ArrayList<DataSample> getTrainingData(int numFold) {
        ArrayList<DataSample> data = new ArrayList<>();
        if (!folds.isEmpty()) {
            for (Map.Entry<Integer, ArrayList<DataSample>> entry : this.folds.entrySet()) {
                if (entry.getKey() != numFold) {
                    data.addAll(entry.getValue());
                }
            }
        }
        return data;
    }

    @Override
    public ArrayList<DataSample> getValidationData(int numFold) {
        ArrayList<DataSample> data = new ArrayList<>();
        if (!folds.isEmpty()) {
            data = this.folds.get(numFold);
        }
        return data;
    }

    /**
     * @return the numFolds
     */
    @Override
    public int getNumFolds() {
        return numFolds;
    }

    /**
     * @param numFolds the numFolds to set
     */
    public void setNumFolds(int numFolds) {
        this.numFolds = numFolds;
    }

    @Override
    public String toString() {
        return "K-fold";
    }

    @Override
    public ArrayList<DataSample> getTrainingData() {
        return this.getTrainingData(this.getCurrentFold());
    }

    @Override
    public ArrayList<DataSample> getValidationData() {
        return this.getValidationData(this.getCurrentFold());
    }
}
