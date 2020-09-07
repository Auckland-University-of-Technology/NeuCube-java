/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.tasks;

import java.io.File;
import java.util.ArrayList;
import jneucube.cube.NeuCubeController;
import jneucube.data.DataSample;
import jneucube.util.ConfusionMatrix;
import jneucube.util.Matrix;
import jneucube.util.Util;

/**
 *
 * @author em9403
 */
public class ClassificationTask {
        /**
     * This function performs a straightforward and complete example of a
     * classification task. The function executes the following steps:
     *
     * 1 Creates a new project.
     *
     * 2 Configurates the SNN using a properties file.
     *
     * 3 Loads and encodes the data set.
     *
     * 4 Initialises the SNN, i.e. loads the reservoir and input neuron
     * coordinates, then creates the connections.
     *
     * 5 Saves the initial NeuCube features i.e. the connection matrix
     * (NeuCube_Connection_Matrix.csv), the initial synaptic weights matrix
     * (NeuCube_Initial_Weight_Matrix.csv), the initial synaptic weights
     * (NeuCube_Initial_Weights.csv), and the initial configuration of the
     * NeuCube (NeuCube_Initial.xml which can be loaded and visualized using the
     * NeuCubeFX).
     *
     * 6 Splits the data into training and testing sets. It is worth mentioning
     * that the training set is submitted to cross-validation for estimating the
     * prediction performance of the model, and the test dataset for evaluating
     * the generalisation of the model. For more information about training and
     * testing data sets see the recommended link
     * https://machinelearningmastery.com/difference-test-validation-datasets/
     *
     * 7 Propagates all data sample for firing activity analysis and saves the
     * firing activity (NeuCube_Initial_Firing_Activity.csv). Before data
     * propagation the training data is encoded into spike trains and the
     * variable
     * {@link jneucube.cube.NeuCubeController#setRecordFiringActivity(boolean)}
     * is set to true
     * {@link jneucube.cube.NeuCubeController#setRecordFiringActivity(boolean)}
     * for fast execution.
     *
     * 8 Performs cross-validation with the training dataset. Shows and saves
     * the results of the cross-validation using the training data i.e. the
     * confusion matrices, the overall confusion matrix, the metrics generated
     * from the overall confusion matrix (accuracy, recall, specificity,
     * precision, F1, informedness), and saves the overall confusion matrix
     * (NeuCube_Overall_Confusion_Matrix.csv). Note that this process estimates
     * the prediction performance of the model.
     *
     * 9 Evaluates the final model's performance (i.e. generalisation). Fitting
     * and evaluating the model using the training and testing datasets
     * respectively. It saves the test matrix (NeuCube_Validation_Results.csv)
     * which columns indicates the sample id, the actual class, the predicted
     * class and the error (0 well predicted, 1 error), and shows the test
     * matrix. Then, the function calculates and shows the confusion matrix
     * using the test matrix, and shows the metrics (accuracy, recall,
     * specificity, precision, F1, informedness) derived from it.
     * <br>
     * Following steps should be executed only if the model achieved a good
     * predictive performance.
     * <br>
     * 10 Saves the trained neucube (NeuCube_Final.xml which can be loaded and
     * visualized using the NeuCubeFX).
     *
     * 11 Analysis after training. Saves the matrix and list of the synaptic
     * weights for further analysis into the NeuCube_Trained_Weight_Matrix.csv
     * and NeuCube_Trained_Weights.csv files respectively. Propagates the data
     * for firing activity analysis using the training set, and saves it into
     * the NeuCube_Trained_Firing_Activity.csv file.
     *
     *
     * Summarising the training (cross validataion) and testing results
     *
     * @param dir The directory that contains the properties file and the XML
     * file.
     * @param propertiesFile the properties file that contains the parameters of
     * the NueCube
     * @param initialNeuCubeFile The XML file that contains the structure of the
     * NeuCube.
     * @param finalNeuCubeFile The XML file that will contain the structure of
     * the trained NeuCube.
     */
    public void run(String dir, String propertiesFile, String initialNeuCubeFile, String finalNeuCubeFile) {
        // 1 Creating a new project
        NeuCubeController project = new NeuCubeController();
        project.createProject(dir);

        // 2 Configuration of the SNN using a properties file.
        project.configureNeuCube(propertiesFile);

        // 3 Loading and encoding a data set.                 
        // In the case of huge data (data time points and samples), it is recommended to load the spatio-temporal data after saving the initialised NeuCube
        project.loadSpatioTemporalData();

        // 4 Initialising the SNN (Loads the reservoir and input neuron coordinates, and creates the connections).
        project.initializeNetwork();

        // 5 Saving the initial NeuCube
        project.exportConnectionMatrix("NeuCube_Connection_Matrix.csv");
        project.exportCurrentWeightMatrix("NeuCube_Initial_Weight_Matrix.csv");
        project.exportCurrentWeights("NeuCube_Initial_Weights.csv");
        project.saveNeucube(initialNeuCubeFile);
        

        // 6 Spliting the dataset for training (cross-validation) and testing (model evaluation).
        project.getDataController().splitData();
        ArrayList<DataSample> trainingSet = project.getDataController().getTrainingSamples();
        ArrayList<DataSample> testingSet = project.getDataController().getValidationSamples();

        // 7 Analysis before training 
        // 7.1 Propagating the training dataset without changing the weights. 
        // If the data has been already encoded into spike trains then remove 
        // or comment the following line project.runEncoder(trainingSet);
        //project.runEncoder(trainingSet);
        project.setRecordFiringActivity(true);
        project.propagateDataset(trainingSet);
        // 7.2 Saving the firing activity of the whole dataset before training        
        project.exportFiringActivity("NeuCube_Initial_Firing_Activity.csv");

        // 8 Executing the cross-validation with the training dataset
        project.setRecordFiringActivity(false);
        ConfusionMatrix cmCrossValidation = project.runCrossvalidation(trainingSet);
        // 8.1 Showing and saving the results after training        
        System.out.println("Cross validation confusion matrix");
        cmCrossValidation.getErrorMatrix().show();
        cmCrossValidation.getErrorMatrix().export(project.getStrPath() + File.separator + "NeuCube_Cross_Validation_Error_Matrix.csv", ",");
        cmCrossValidation.show();
        cmCrossValidation.export(project.getStrPath() + File.separator + "NeuCube_Cross_Validation_Confusion_Matrix.csv", ",");
        cmCrossValidation.showMetrics();
        cmCrossValidation.exportMetrics(project.getStrPath() + File.separator + "NeuCube_Cross_Validation_Metrics.txt");

        // 9 Evaluating the final model's performance (i.e. generalisation). Fitting and evaluating the model using the training and testing datasets respectively.
        // 9.1 Fitting (unsupervised and supervised training) the SNN model using the training set        
        project.fitModel(trainingSet);
        // 9.2 Evaluating the SNN model using the testing dataset and getting the test matrix (sample ids, known labels, predicted labels, errors)
        Matrix testMatrix = project.evaluateModel(testingSet);        
        System.out.println("Test matrix");
        testMatrix.show();
        testMatrix.export(project.getStrPath() + File.separator + "NeuCube_Validation_Results.csv", ",");
        // 9.3 Calculating the confusion matrix using the known and predicted labels (columns 2 and 3) from the test matrix.
        ConfusionMatrix cmTest = new ConfusionMatrix(testMatrix.getVecCol(1), testMatrix.getVecCol(2));
        System.out.println("Test confusion matrix");
        cmTest.getErrorMatrix().show();
        cmTest.getErrorMatrix().export(project.getStrPath() + File.separator + "NeuCube_Test_Error_Matrix.csv", ",");
        cmTest.show();
        cmTest.export(project.getStrPath() + File.separator + "NeuCube_Test_Confusion_Matrix.csv", ",");
        cmTest.showMetrics();
        cmTest.exportMetrics(project.getStrPath() + File.separator + "NeuCube_Test_Metrics.txt");

        // Only if the model achieved a good generalisation
        // Analysis after training
        // 11 Export the weights after training
        project.exportCurrentWeightMatrix("NeuCube_Trained_Weight_Matrix.csv");
        project.exportCurrentWeights("NeuCube_Trained_Weights.csv");
        // 11.1 Propagating the data for firing activity analysis using the training set
        project.setRecordFiringActivity(true);
        project.propagateDataset(trainingSet);
        project.exportFiringActivity("NeuCube_Trained_Firing_Activity.csv");
        
        // 10 Saving the neucube  
        project.removeSpatioTemporalData();          // In the case of huge data (data time points and samples), it is recommended to remove the data samples beore saving the NeuCube 
        project.getNetworkController().removeNeuronsFromList(project.getNetworkController().getNetwork().getOutputNeurons()); // In the case of several samples, e.g. more than 1000
        project.getNetworkController().resetNeuralActivity();   // Removes all the spiking activity
        project.clearCrossvalidation();
        project.saveNeucube(finalNeuCubeFile);

        // 12 Summarising the training (cross validataion) and testing results
        System.out.println("Cross-validation ");
        cmCrossValidation.showMetrics();
        System.out.println("Test");
        cmTest.showMetrics();
        // 12.1 Saving the training (cross validataion) and testing results
        StringBuffer bf = new StringBuffer();
        bf.append(System.lineSeparator());
        bf.append("Cross-validation").append(System.lineSeparator());
        bf.append(cmCrossValidation.getMetricsToString());
        bf.append("Test").append(System.lineSeparator());
        bf.append(cmTest.getMetricsToString());//        
        Util.saveStringToFile(bf, project.getStrPath() + File.separator + "NeuCube_Metrics.txt");
    }

}
