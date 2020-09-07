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
import jneucube.util.Util;
import static jneucube.log.Log.LOGGER;

/**
 * This object randomly splits the dataset into training and validation data
 * sets using the Monte Carlo cross-validation
 *
 * @author Josafath Israel Espinosa Ramos
 */
public class MonteCarlo extends CrossValidation {

    public static final int RANDOM = 1;
    public static final int SEQUENTIAL = 2;

    private double trainingRate = 0.0;
    private int numExperiments = 1;
    private int type = RANDOM;
    //Logger LOGGER = LoggerFactory.getLogger(MonteCarlo.class);

    @Override
    public void split(HashMap<Double, ArrayList<DataSample>> dataClasses) {
        LOGGER.info("------- Spliting data set using Monte Carlo cross validation -------");
//        this.setNumClasses(dataClasses.size());
        this.folds.clear();
        int count;
        int idxs[];
        ArrayList<DataSample> tSamples;
        ArrayList<DataSample> vSamples;
        ArrayList<DataSample> subSamples;
        if (this.type == RANDOM) {
            for (int i = 0; i < this.numExperiments; i++) {
                subSamples = new ArrayList<>();
                tSamples = new ArrayList<>();
                vSamples = new ArrayList<>();
                for (Map.Entry<Double, ArrayList<DataSample>> entry : dataClasses.entrySet()) {
                    count = (int) (entry.getValue().size() * this.trainingRate);
                    idxs = Util.getRandomPermutation(entry.getValue().size());
                    for (int j = 0; j < entry.getValue().size(); j++) {
                        if (j < count) {
                            tSamples.add(entry.getValue().get(idxs[j]));
                        } else {
                            vSamples.add(entry.getValue().get(idxs[j]));
                        }
                    }
                }
                subSamples.addAll(tSamples);
                subSamples.addAll(vSamples);
                this.folds.put(i, subSamples);
                this.setNumTrainingSamples(tSamples.size());
                this.setNumValidationSamples(vSamples.size());
            }
        } else {
            count = (int) (dataClasses.get(1.0).size() * this.trainingRate);
            this.folds.put(0, dataClasses.get(1.0));    // the key value was hard coded in SpatioTemporalData.java line 101 this.dataClasses.put(1.0, dataSamples);
            this.setNumTrainingSamples(count);
            this.setNumValidationSamples(dataClasses.get(1.0).size() - count);
        }

        LOGGER.info("------- Spliting complete -------");
    }

    public void runSequential() {

    }

    /**
     * Selects the training data, i.e. the folds that differs from the selected
     * numFold
     *
     * @param numFold
     * @return
     */
    public ArrayList<DataSample> getTrainingData(int numFold) {
        ArrayList<DataSample> data = new ArrayList<>();
        int end = (int) Math.floor(this.folds.get(numFold).size() * trainingRate);
        for (int i = 0; i < end; i++) {
            data.add(this.folds.get(numFold).get(i));
        }
        return data;
    }

    public ArrayList<DataSample> getValidationData(int numFold) {
        ArrayList<DataSample> data = new ArrayList<>();
        int start = (int) Math.floor(this.folds.get(numFold).size() * trainingRate);
        for (int i = start; i < this.folds.get(numFold).size(); i++) {
            data.add(this.folds.get(numFold).get(i));
        }
        return data;
    }

    /**
     * @return the trainingRate
     */
    public double getTrainingRate() {
        return trainingRate;
    }

    /**
     * @param trainingRate the trainingRate to set
     */
    public void setTrainingRate(double trainingRate) {
        this.trainingRate = trainingRate;
    }

    /**
     * @return the numExperiments
     */
    public int getNumExperiments() {
        return numExperiments;
    }

    /**
     * @param numExperiments the numExperiments to set
     */
    public void setNumExperiments(int numExperiments) {
        this.numExperiments = numExperiments;
    }

    @Override
    public String toString() {
        return "Monte Carlo";
    }

    /**
     * @return the type
     */
    public int getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(int type) {
        this.type = type;
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
