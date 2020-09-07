/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.crossValidation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import jneucube.data.DataSample;
import static jneucube.log.Log.LOGGER;

/**
 *
 * @author Josafath Israel Espinosa Ramos
 */
public class LeaveOneOut extends CrossValidation {


    @Override
    public void split(HashMap<Double, ArrayList<DataSample>> dataClasses) {
        LOGGER.info("------- Spliting data set using Kfold cross validation -------");
//        this.setNumClasses(dataClasses.size());
        this.folds.clear();
        ArrayList<DataSample> dataSamples = new ArrayList<>();
        dataClasses.entrySet().stream().forEach((entry) -> {
            dataSamples.addAll(entry.getValue());
        });
        run(dataSamples);
        LOGGER.info("------- Spliting complete -------");
    }

    private void run(ArrayList<DataSample> dataSamples) {
        ArrayList<DataSample> temp;
        for (int i = 0; i < dataSamples.size(); i++) {
            temp = new ArrayList<>();
            temp.add(dataSamples.get(i));
            folds.put(i, temp);
        }
    }

    @Override
    public ArrayList<DataSample> getTrainingData(int numFold) {
        ArrayList<DataSample> data = new ArrayList<>();
        for (Map.Entry<Integer, ArrayList<DataSample>> entry : this.folds.entrySet()) {
            if (entry.getKey() != numFold) {
                data.addAll(entry.getValue());
            }
        }
        return data;
    }

    @Override
    public ArrayList<DataSample> getValidationData(int numFold) {
        return this.folds.get(numFold);
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
