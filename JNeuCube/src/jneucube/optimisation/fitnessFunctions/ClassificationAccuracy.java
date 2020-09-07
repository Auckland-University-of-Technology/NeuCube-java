/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.optimisation.fitnessFunctions;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import jneucube.classifiers.KNN;
import jneucube.cube.NeuCubeController;
import jneucube.data.DataSample;
import jneucube.data.DataSplitter;
import jneucube.data.RatioDataSplitter;
import jneucube.spikingNeurons.cores.LIF;
import jneucube.trainingAlgorithms.supervised.deSNNs;
import jneucube.trainingAlgorithms.unsupervised.STDP;
import jneucube.util.ConfusionMatrix;
import jneucube.util.Matrix;
import jneucube.util.Util;

/**
 *
 * @author Josafath Israel Espinosa Ramos
 */
public class ClassificationAccuracy extends FitnessFunction {

    NeuCubeController project = new NeuCubeController();
    private DataSplitter dataSplitter = new RatioDataSplitter();
    ArrayList<DataSample> trainingSet = new ArrayList<>();
    ArrayList<DataSample> testingSet = new ArrayList<>();
    private String dataDirectory = "";
    private String propertiesFile = "";
    private int numCubes = 10;
    private String metric = "accuracy";
    private double bestAgentFitness;

    public ClassificationAccuracy() {
        this.setOptimalValue(1.0);
        this.setOptimisationType(FitnessFunction.MAXIMISATION);
        this.bestAgentFitness = Double.NEGATIVE_INFINITY * FitnessFunction.MAXIMISATION;
//        this.setDimensionality(7);
//        this.setRangeValues(new double[][]{{0.05, 0.5}, {2.0, 10.0}, {0.001, 0.01}, {0.001, 0.01}, {0.001, 0.01}, {0.001, 0.01}, {3, 10}});
    }

    /**
     * Runs a classification task and evaluates the fitness of the candidate
     * solution, i.e the mean accuracy of training stage multiply by the mean
     * accuracy of the evaluation stage of all NeuCubes. This function executes
     * the following process:
     *
     * 1) sets the parameters of the NeuCube. For each neuCube: 2) Splits the
     * data into training and testing sets. 3) Initialises a new NeuCube model.
     * 4) Runs the cross-validation with the training dataset. 5) Fits the model
     * with the training dataset. 6) Evaluates the model with the testing
     * dataset.
     *
     *
     * LIF threshold, LIF Refractory time, STDP Apos STDP Aneg, deSNN
     * positiveDrift, deSNN negativeDrift, KNN neighbours
     *
     * @param id
     * @param individual
     * @return
     */
    @Override
    public double evaluateIndividual(int id, double[] individual) {
        double fitness = 0;
        double meanTrainMetric = 0;
        double tempTrainingMetric;
        double meanTestMetric = 0;
        double tempTestingMetric;
        double tempFitness;
        Matrix testMatrix;
        ConfusionMatrix cmTest;
        double processingTime = System.nanoTime();
        double cubeStartTime;
        double cubeProcessingTime;

        int bestCube = 0;
        double bestFitness = 0;
        double bestTrain = 0;
        double bestTest = 0;

//        individual=new double[]{0.1,6,0.001,0.001,0.005,0.005,5}; // Default configuration
//        individual=new double[]{0.7355220555952771, 5.017376203950342, 0.005035709211451064, 0.004838185245009199, 0.008510130920159724, 0.005156040239001932, 4.562062588129773};
//        individual=new double[]{0.1, 5.017376203950342, 0.005035709211451064, 0.004838185245009199, 0.008510130920159724, 0.005156040239001932, 4.562062588129773};
        // 1 Setting the parameters of the NeuCube
        this.configureNeuCube(individual);
        System.out.println("<agent id=\"" + id + "\">");
        System.out.println("<agent_solution>" + Util.getHorzArray(individual) + "</agent_solution>");
        for (int i = 0; i < this.numCubes; i++) {
            cubeStartTime = System.nanoTime();
            // 2 Splitting the data into training and testing sets
            //project.getDataController().splitData(dataSplitter);                        
            this.splitData();
            // 3 initializing a new NeuCube model
                                
            this.project.initializeNetwork();
            // 4 Running the cross-validation with the training dataset            
            ConfusionMatrix cmCrossValidation = this.project.runCrossvalidation(this.trainingSet);
            // 5 Fitting the model with the training dataset
            this.project.fitModel(this.trainingSet);
            // 6 Evaluating the model with the testing dataset
            testMatrix = this.project.evaluateModel(this.testingSet);

            cmTest = new ConfusionMatrix(testMatrix.getVecCol(1), testMatrix.getVecCol(2));

            switch (metric.toLowerCase()) {
                case "accuracy": {
                    tempTrainingMetric = cmCrossValidation.getAccuracy();
                    tempTestingMetric = cmTest.getAccuracy();
                }
                break;
                case "recall": {
                    tempTrainingMetric = cmCrossValidation.getRecall();
                    tempTestingMetric = cmTest.getRecall();
                }
                break;
                case "precision": {
                    tempTrainingMetric = cmCrossValidation.getPrecision();
                    tempTestingMetric = cmTest.getPrecision();
                }
                break;
                case "specificity": {
                    tempTrainingMetric = cmCrossValidation.getSpecificity();
                    tempTestingMetric = cmTest.getSpecificity();
                }
                break;
                default: {
                    tempTrainingMetric = cmCrossValidation.getAccuracy();
                    tempTestingMetric = cmTest.getAccuracy();
                }
            }

            meanTrainMetric += tempTrainingMetric;
            meanTestMetric += tempTestingMetric;

            tempFitness = (tempTrainingMetric + tempTestingMetric) / 2;
            cubeProcessingTime = (System.nanoTime() - cubeStartTime) / 1000000;   // milliseconds  

            if (tempFitness > bestFitness) {
                bestCube = i;
                bestFitness = tempFitness;
                bestTrain = tempTrainingMetric;
                bestTest = tempTestingMetric;
            }

            project.getNetworkController().removeNeuronsFromList(project.getNetworkController().getNetwork().getOutputNeurons());
            project.getNetworkController().resetNeuralActivity();   // Removes all the spiking activity        
            project.clearCrossvalidation();

            System.out.println("<agent_cube id=\"" + i + "\" fitness=\"" + tempFitness + "\" train=\"" + tempTrainingMetric + "\" test=\"" + tempTestingMetric + "\" time=\"" + cubeProcessingTime + "\"></agent_cube>");
            //System.out.println("initialisation:"+endTime1+", cross-validation:"+endTime2+", fit model:"+endTime3+", evaluate model:"+endTime4);
        }

        fitness = ((meanTrainMetric / this.numCubes) + (meanTestMetric / this.numCubes)) / 2;
        processingTime = (System.nanoTime() - processingTime) / 1000000;   // milliseconds  
        System.out.println("<agent_best_cube id=\"" + bestCube + " \" fitness=\"" + bestFitness + "\" train=\"" + bestTrain + "\" test=\"" + bestTest + "\" overall=\"" + ((bestTrain + bestTest) / 2) + "\" ></agent_best_cube>");
        System.out.println("<agent_fitness>" + fitness + "</agent_fitness>");
        System.out.println("<agent_process_time>" + processingTime + "</agent_process_time>");
        System.out.println("</agent>");
        if (fitness > this.bestAgentFitness) {
            this.bestAgentFitness = fitness;
            this.saveBestAgent(individual);
        }
        return fitness;
    }

    public void initialiseProject(String dataDirectory, String propertiesFile) {
        this.dataDirectory = dataDirectory;
        this.propertiesFile = propertiesFile;
        this.project = new NeuCubeController();
        this.project.createProject(dataDirectory);
        // 2 Configuration of the SNN using a properties file.
        this.project.configureNeuCube(propertiesFile);        
        this.project.loadSpatioTemporalData();
    }

    public void saveBestAgent(double[] individual) {
        this.splitData();
        // 3 initializing a new NeuCube model
        this.project.initializeNetwork();
        project.exportConnectionMatrix("NeuCube_Connection_Matrix.csv");
        project.exportCurrentWeightMatrix("NeuCube_Initial_Weight_Matrix.csv");
        project.exportCurrentWeights("NeuCube_Initial_Weights.csv");
        project.saveNeucube("NeuCube_Initial.xml" );
        
        project.setRecordFiringActivity(true);
        project.propagateDataset(this.trainingSet);
        // 7.2 Saving the firing activity of the whole dataset before training        
        project.exportFiringActivity("NeuCube_Initial_Firing_Activity.csv");

        // 4 Running the cross-validation with the training dataset            
        ConfusionMatrix cmCrossValidation = this.project.runCrossvalidation(this.trainingSet);
        // 5 Fitting the model with the training dataset
        this.project.fitModel(this.trainingSet);
        // 6 Evaluating the model with the testing dataset
        Matrix testMatrix = this.project.evaluateModel(this.testingSet);
        ConfusionMatrix cmTest = new ConfusionMatrix(testMatrix.getVecCol(1), testMatrix.getVecCol(2));

        cmCrossValidation.getErrorMatrix().export(project.getStrPath() + File.separator + "NeuCube_Cross_Validation_Error_Matrix.csv", ",");
        cmCrossValidation.export(project.getStrPath() + File.separator + "NeuCube_Cross_Validation_Confusion_Matrix.csv", ",");
        cmCrossValidation.exportMetrics(project.getStrPath() + File.separator + "NeuCube_Cross_Validation_Metrics.txt");
        testMatrix.export(project.getStrPath() + File.separator + "NeuCube_Validation_Results.csv", ",");
        cmTest.getErrorMatrix().export(project.getStrPath() + File.separator + "NeuCube_Test_Error_Matrix.csv", ",");
        cmTest.export(project.getStrPath() + File.separator + "NeuCube_Test_Confusion_Matrix.csv", ",");
        cmTest.exportMetrics(project.getStrPath() + File.separator + "NeuCube_Test_Metrics.txt");

        project.exportCurrentWeightMatrix("NeuCube_Trained_Weight_Matrix.csv");
        project.exportCurrentWeights("NeuCube_Trained_Weights.csv");
        // 11.1 Propagating the data for firing activity analysis using the training set
        project.setRecordFiringActivity(true);
        project.propagateDataset(trainingSet);
        project.exportFiringActivity("NeuCube_Trained_Firing_Activity.csv");

        // 12.1 Saving the training (cross validataion) and testing results
        StringBuffer bf = new StringBuffer();
        bf.append("Parameters");
        bf.append(System.lineSeparator());
        bf.append(Util.vec2str(individual, ","));
        bf.append(System.lineSeparator());
        bf.append("Cross-validation").append(System.lineSeparator());
        bf.append(cmCrossValidation.getMetricsToString());
        bf.append("Test").append(System.lineSeparator());
        bf.append(cmTest.getMetricsToString());//  
        
        Util.saveStringToFile(bf, project.getStrPath() + File.separator + "NeuCube_Metrics.txt");
        
        //project.removeSpatioTemporalData();          // In the case of huge data (data time points and samples), it is recommended to remove the data samples beore saving the NeuCube 
        //project.getNetworkController().removeNeuronsFromList(project.getNetworkController().getNetwork().getOutputNeurons()); // In the case of several samples, e.g. more than 1000
        project.getNetworkController().resetNeuralActivity();   // Removes all the spiking activity
        project.clearCrossvalidation();
        project.saveNeucube("NeuCube_Tranined.xml" );

    }

    public void splitData() {
        this.dataSplitter.split(project.getDataController());
//        this.project.getDataController().splitData();
        this.trainingSet = project.getDataController().getTrainingSamples();
        this.testingSet = project.getDataController().getValidationSamples();
//
//        ArrayList<DataSample> tempTestingSet = new ArrayList<>();
//        tempTestingSet.addAll(project.getDataController().getDataSamples().subList(175, 210));    // the indexes for user 1. Change to different users
//        ArrayList<DataSample> tempTrainingSet = new ArrayList<>();
//        tempTrainingSet.addAll(project.getDataController().getDataSamples());
//        tempTrainingSet.removeAll(tempTestingSet);
//        Collections.shuffle(tempTrainingSet);
//        project.getDataController().getData().setTrainingData(tempTrainingSet);
//        project.getDataController().getData().setValidationData(tempTestingSet);
//        this.trainingSet = project.getDataController().getTrainingSamples();
//        this.testingSet = project.getDataController().getValidationSamples();
    }

    public void configureNeuCube(double[] individual) {
        double lifThresholdVoltage = individual[0];
        int lifThresholdRefractoryTime = (int) individual[1];
        double stdpApos = individual[2];
        double stdpAneg = individual[3];
        double deSNNposDrift = individual[4];
        double deSNNnegDrift = individual[5];
        int knn = (int) individual[6];
        //int numNeurons=(int) individual[7]; includes the number of neurons
        
        this.configureSpikingNeuron(lifThresholdVoltage, lifThresholdRefractoryTime);
        this.configureSTDP(stdpApos, stdpAneg);
        this.configureDeSNN(deSNNposDrift, deSNNnegDrift);
        this.configureKNN(knn);  
        //this.configureNumNeurons(numNeurons);
        
    }
    
    public void configureNumNeurons(int numNeurons){
        this.project.getConfiguration().getProperties().setProperty("numNeuronsX", String.valueOf(numNeurons) ); // new line
    }

    public void configureSpikingNeuron(double thresholdVoltage, int thresholdRefractoryTime) {
        LIF lif = (LIF) this.project.getNeucube().getNetwork().getSpikingNeuron().getCore();
        lif.setThresholdVoltage(thresholdVoltage);
        lif.setThresholdRefractoryTime(thresholdRefractoryTime);
    }

    public void configureSTDP(double Apos, double Aneg) {
        STDP stdp = (STDP) this.project.getNeucube().getUnsupervisedLearningAlgorithm();
        stdp.setApos(Apos);
        stdp.setAneg(Aneg);
    }

    public void configureDeSNN(double posDrift, double negDrift) {
        deSNNs de = (deSNNs) this.project.getNeucube().getSupervisedLearningAlgorithm();
        de.setPositiveDrift(posDrift);
        de.setNegativeDrift(negDrift);
    }

    public void configureKNN(int neighbours) {
        KNN knn = (KNN) this.project.getNeucube().getClassifier();
        knn.setK(neighbours);
    }

    /**
     * @return the dataDirectory
     */
    public String getDataDirectory() {
        return dataDirectory;
    }

    /**
     * @param dataDirectory the dataDirectory to set
     */
    public void setDataDirectory(String dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    /**
     * @return the propertiesFile
     */
    public String getPropertiesFile() {
        return propertiesFile;
    }

    /**
     * @param propertiesFile the propertiesFile to set
     */
    public void setPropertiesFile(String propertiesFile) {
        this.propertiesFile = propertiesFile;
    }

    /**
     * @return the numCubes
     */
    public int getNumCubes() {
        return numCubes;
    }

    /**
     * @param numCubes the numCubes to set
     */
    public void setNumCubes(int numCubes) {
        this.numCubes = numCubes;
    }

    /**
     * @return the dataSplitter
     */
    public DataSplitter getDataSplitter() {
        return dataSplitter;
    }

    /**
     * @param dataSplitter the dataSplitter to set
     */
    public void setDataSplitter(DataSplitter dataSplitter) {
        this.dataSplitter = dataSplitter;
    }

    /**
     * @return the metric
     */
    public String getMetric() {
        return metric;
    }

    /**
     * @param metric the metric to set
     */
    public void setMetric(String metric) {
        this.metric = metric;
    }

    /**
     * @return the bestAgentFitness
     */
    public double getBestAgentFitness() {
        return bestAgentFitness;
    }

    /**
     * @param bestAgentFitness the bestAgentFitness to set
     */
    public void setBestAgentFitness(double bestAgentFitness) {
        this.bestAgentFitness = bestAgentFitness;
    }

    public static void main(String[] args) {
        ClassificationAccuracy ff = new ClassificationAccuracy();
        String dataDirectory = "C:\\DataSets\\MetOcean\\Data\\Shi Fei\\1.24(8X8)\\3input,3feature,17X17";
        String propertiesFile = "3input3feature17x17.properties";
        //LIF threshold, LIF Refractory time, STDP Apos, STDP Aneg, deSNN positiveDrift, deSNN negativeDrift, KNN neighbours
        double[] individual = new double[]{0.8, 4.0, 0.002, 0.003, 0.004, 0.003, 4.0};
        ff.initialiseProject(dataDirectory, propertiesFile);

        for (int i = 0; i < 1; i++) {
            double individualFitness = ff.evaluateIndividual(i, individual);
            //System.out.println("Individual Fitness="+individualFitness);
        }

    }

}
