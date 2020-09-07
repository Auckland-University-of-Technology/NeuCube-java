/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.data;

import java.util.Collections;

/**
 * The {@code RatioDataSplitter} class splits the dataset into train and test
 * datasets given a split ratio.
 *
 * @author em9403
 */
public class RatioDataSplitter extends DataSplitter {

    /**
     * This function access the dataController and splits the data set into
     * training (for cross-validation) and testing data set (for evaluation of
     * the model) given a rate for training. 1) it clears the training and
     * validations sets. 2) selects balanced random samples for the testing set.
     * 3) Set the training set with the remaining samples. 4) shuffle the
     * training data.
     *
     * @param dataController
     */
    @Override
    public void split(DataController dataController) {        
        double rateForTraining=dataController.getData().getRateForTraining();
        dataController.getData().getTrainingData().clear();
        dataController.getData().getValidationData().clear();
        dataController.getData().getValidationData().addAll(dataController.getRandomDataSamples(1 - rateForTraining));
        dataController.getData().getTrainingData().addAll(dataController.getDataSamples());
        dataController.getData().getTrainingData().removeAll(dataController.getData().getValidationData());
        Collections.shuffle(dataController.getData().getTrainingData());
    }

}
