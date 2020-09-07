/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.cube;

import com.thoughtworks.xstream.XStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import jneucube.crossValidation.CrossValidation;
import jneucube.data.DataController;
import jneucube.data.DataSample;
import jneucube.data.OnlineDataReaderJob;
import jneucube.data.OnlineReader;
import jneucube.tasks.Tasks;
import jneucube.encodingAlgorithms.EncodingAlgorithm;
import jneucube.inputMappingAlgorithms.InputMapping;
import static jneucube.log.Log.LOGGER;
import jneucube.network.NetworkController;
import jneucube.spikingNeurons.SpikingNeuron;
import jneucube.trainingAlgorithms.LearningAlgorithm;
import jneucube.trainingAlgorithms.OnlineLearningJob;
import jneucube.util.ConfusionMatrix;
import jneucube.util.Matrix;
import jneucube.util.Messages;
import jneucube.util.NeuCubeRuntimeException;
import jneucube.util.Util;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import org.quartz.SimpleTrigger;
import static org.quartz.TriggerBuilder.newTrigger;
import org.quartz.impl.StdSchedulerFactory;

import static org.quartz.JobBuilder.newJob;

/**
 *
 * @author Josafath Israel Espinosa Ramos
 */
public class NeuCubeController {

    private NeuCube neucube;
    private String strPath;
    private NeuCubeConfiguration configuration = new NeuCubeConfiguration();
    NetworkController networkController = new NetworkController();
    DataController dataController = new DataController();

    //Logger LOGGER = LoggerFactory.getLogger(NeuCubeController.class);
    //Logger LOGGER = LogManager.getLogger(NeuCubeController.class);
    // private static final Logger LOGGER = LogManager.getLogger();
    public NeuCubeController() {

    }

    public NeuCubeController(NeuCube neucube) {
        this.neucube = neucube;
    }

    public NetworkController getNetworkController() {
        return this.networkController;
    }

    public DataController getDataController() {
        return this.dataController;
    }

    /**
     * Creates a new instance of the NeuCube and sets the working directory.
     *
     * @param path
     * @return
     */
    public boolean createProject(String path) {
        File file = new File(path);
        if (file.isDirectory()) {
            this.setStrPath(path);
            this.neucube = new NeuCube();
        } else {
            LOGGER.error("The directory does not exist");
            return false;
        }

        return true;
    }

    /**
     * If the configuration succeed, then it sets the NeuCube properties to
     * their correspondent controller.
     *
     * @param propFileName
     * @return
     */
    public boolean configureNeuCube(String propFileName) {
        String propertiesFileName = this.getStrPath() + File.separator + propFileName;
        return this.configureNeuCube(new File(propertiesFileName));
    }

    /**
     * If the configuration succeed, then it sets the NeuCube properties to
     * their correspondent controller.
     *
     * @param propertiesFile
     * @return
     */
    public boolean configureNeuCube(File propertiesFile) {
        if (this.configuration.loadProperties(propertiesFile)) {
            return this.configureNeuCube();
        }
        return false;
    }

    public boolean configureNeuCube(Properties properties) {
        this.configuration.setProperties(properties);
        return this.configureNeuCube();
    }

    private boolean configureNeuCube() {
        if (this.configuration.configureNeuCube(this.neucube)) {
            this.initializeControllers();
            return true;
        }
        return false;
    }

    /**
     * Initializes the controllers of all NeuCube modules.
     */
    public void initializeControllers() {
        this.networkController.setNetwork(neucube.getNetwork());
        this.networkController.setReservoirBuilder(neucube.getReservoirBuilder());
        this.dataController.setData(neucube.getSSTD());
    }

    /**
     * Loads the spatio temporal data from the directory specified when the
     * project was created
     *
     * @return
     */
    public boolean loadSpatioTemporalData() {
        if (Boolean.parseBoolean(this.configuration.getProperties().getProperty("stdOffLine"))) {
            if (this.configuration.getProperties().getProperty("stdDirectory") != null) {
                return loadSpatioTemporalData(this.configuration.getProperties().getProperty("stdDirectory"));
            } else {
                return loadSpatioTemporalData(this.getStrPath());
                //LOGGER.error("The property stdDirectory can not be found in the properties file.");
            }
        }
        return false;
    }

    public boolean removeSpatioTemporalData() {
        return this.dataController.removeData();
    }

    /**
     * This function loads all the data samples from the specified directory
     * path.
     *
     * @param strPath
     * @return
     */
    public boolean loadSpatioTemporalData(String strPath) {
        long startTime = System.nanoTime();
        LOGGER.info("Loading spatio-temporal dataset");
        File path = new File(strPath);
        Messages message = Messages.DATA_LOAD_SUCCESS;
        String strClassFileName = this.configuration.getProperties().getProperty("stdTargetClassLabelFile");
        File classFile = null;
        if (strClassFileName == null) {
            strClassFileName = "";
        }
        if (strClassFileName.equals("")) {
            File[] sampleClassesFile = path.listFiles((File dir, String name) -> name.matches("tar_class.*\\.csv"));
            if (sampleClassesFile.length > 0) {
                classFile = sampleClassesFile[0];
            } else {
                throw new NeuCubeRuntimeException("The tar_class*.csv was not found.");
            }
        } else {
            classFile = new File(strPath + File.separator + strClassFileName);
        }

        if (!classFile.exists()) {
            throw new NeuCubeRuntimeException("The class label file was not found.");
        }
        if (this.dataController.loadData(path, classFile, this.neucube.getProblemType(), null)) {
            LOGGER.info(message.getMessage());
        } else {
            LOGGER.error(message.getMessage());
            return false;
        }
        this.dataController.showDataInfo();
        LOGGER.info("Complete (time " + ((System.nanoTime() - startTime) / 1000000) + " milliseconds seconds)");
        return true;
    }

    /**
     * This function reads a NeuCube project xml file.
     *
     * @param strFileName
     * @return
     */
    public boolean readNeuCube(String strFileName) {
        File file = new File(this.strPath + File.separator + strFileName);
        if (file.exists()) {
            Long startTime = System.nanoTime();
            LOGGER.info("------- Loading project from file " + file.toString() + " -------");
            XStream xstream = new XStream();
            this.neucube = (NeuCube) xstream.fromXML(file);
            this.initializeControllers();
            LOGGER.info("------- Complete (time " + ((System.nanoTime() - startTime) / 1000000) + " milliseconds seconds) -------");
            return true;
        } else {
            LOGGER.error("NeuCube file not found error");
            return false;
        }
    }

    /**
     * Function that creates the reservoir and input neurons of the network and
     * executes the connection algorithm for creating the synaptic connections.
     *
     * @return
     */
    public boolean initializeNetwork() {
        Matrix mappingCoordinates = this.createReservoirCoordinates();
        Matrix inputCoordinates = this.createInputCoordinates(mappingCoordinates);
        this.networkController.createNetwork(mappingCoordinates, inputCoordinates);
        this.runConnectionAlgorithm();
        return true;
    }

    /**
     * Creates a matrix that contains the coordinates of the neurons in the
     * reservoir. If the property mappingCoordinatesFileName is null the
     * function will create network with x by y by z number of neurons,
     * otherwise, it will load the coordinates from a csv file.
     *
     * @return an n by 3 matrix that contains the x,y,z coordinates of n neurons
     */
    public Matrix createReservoirCoordinates() {
        long processTime = System.nanoTime();
        LOGGER.info("- Creating reservoir coordinates");
        Matrix mappingCoordinates = null;
        if (this.configuration.getProperties().getProperty("mappingMode").toUpperCase().equals("AUTO")) {
            //if ( this.configuration.getProperties().getProperty("mappingCoordinatesFileName").isEmpty()) {            
            int x = Integer.parseInt(this.configuration.getProperties().getProperty("numNeuronsX"));
            int y = Integer.parseInt(this.configuration.getProperties().getProperty("numNeuronsY"));
            int z = Integer.parseInt(this.configuration.getProperties().getProperty("numNeuronsZ"));
            mappingCoordinates = this.createMappingCoordinates(x, y, z);
        } else {
            try {
                mappingCoordinates = new Matrix(this.getStrPath() + File.separator + this.configuration.getProperties().getProperty("mappingCoordinatesFileName"), ",");
            } catch (IOException ex) {
                LOGGER.error(ex.getMessage());
            }
        }
        LOGGER.info("- Complete (time " + ((System.nanoTime() - processTime) / 1000000) + ")");
        return mappingCoordinates;
    }

    /**
     * The function creates a matrix with the X,Y,Z position of the neurons
     * given X,Y,Z number of neurons
     *
     * @param numNeuronsX
     * @param numNeuronsY
     * @param numNeuronsZ
     * @return
     */
    public Matrix createMappingCoordinates(int numNeuronsX, int numNeuronsY, int numNeuronsZ) {
        int numNeurons = numNeuronsX * numNeuronsY * numNeuronsZ;
        int rowId;
        this.neucube.getNetwork().setNumNeuronsX(numNeuronsX);
        this.neucube.getNetwork().setNumNeuronsY(numNeuronsY);
        this.neucube.getNetwork().setNumNeuronsZ(numNeuronsZ);
        Matrix coordinateMatrix = new Matrix(numNeurons, 3, 0.0);

        for (int z = 0; z < numNeuronsZ; z++) {
            for (int y = 0; y < numNeuronsY; y++) {
                for (int x = 0; x < numNeuronsX; x++) {
                    rowId = (z * numNeuronsY * numNeuronsX) + (y * numNeuronsX) + x;
                    coordinateMatrix.setRow(rowId, new double[]{x - (numNeuronsX / 2), y - (numNeuronsY / 2), z - (numNeuronsZ / 2)});
                }
            }
        }
        return coordinateMatrix;
    }

    /**
     * Creates a matrix that contains the coordinates of the input neurons.
     * Depending on the property inputMapping, the function creates the
     * coordinates from files, from an algorithm, or for image processing. For
     * the algorithm case, the input mapping algorithms may utilize the
     * coordinates of the reservoir coordinates and extra information from the
     * data, e.g. the graph matching algorithm requires the spike trains.
     *
     * @param reservoirCoordinates an m -by-3 matrix that contains the x,y and z
     * coordinates of the reservoir neurons
     * @return an m by 3 matrix that contains the x,y and z coordinates of n
     * inputs
     */
    public Matrix createInputCoordinates(Matrix reservoirCoordinates) {
        Matrix inputCoordinates = null;
        switch (this.configuration.getProperties().getProperty("inputMapping").toUpperCase()) { // File, Random, Image
            case ("FILE"): {
                try {
                    inputCoordinates = new Matrix(this.getStrPath() + File.separator + this.configuration.getProperties().getProperty("inputCoordinatesFileName"), ",");
                } catch (IOException ex) {
                    LOGGER.error(ex.getMessage());
                }
            }
            break;
            case ("ALGORITHM"): {
                switch (this.configuration.getProperties().getProperty("inputMappingAlgorithm").toUpperCase()) {
                    case ("RANDOM_MAPPING"): {
                        int numInputCoordinates = this.getDataController().getData().getNumFeatures();
                        InputMapping.RANDOM_MAPPING.setNumFeatures(numInputCoordinates);
                        InputMapping.RANDOM_MAPPING.setNeuronsCoordinates(reservoirCoordinates);
                        inputCoordinates = InputMapping.RANDOM_MAPPING.createCoordinates();
                    }
                    break;
                    case ("GRAPH_MATCHING"): {
                        if (this.getDataController().getData().getNumSamples() <= 0) {
                            LOGGER.error("No data was found.");
                            throw new NeuCubeRuntimeException("No data was found.");
                        }
                        if (!this.getDataController().getData().isEncoded()) {
                            LOGGER.info("Encoding data for mapping input variables. The GRAPH MATCHING algorithm requires spike trains for mapping input variables.");
                            this.runEncoder();
                        }
                        Matrix spikeTrains = this.getDataController().mergeSpikeData();
                        InputMapping.GRAPH_MATCHING.setNeuronsCoordinates(reservoirCoordinates);
                        InputMapping.GRAPH_MATCHING.setSpikeTrain(spikeTrains);
                        inputCoordinates = InputMapping.GRAPH_MATCHING.createCoordinates();
                    }
                    break;
                    default: {
                        LOGGER.info("No algorithm specified for input mapping");
                        throw new NeuCubeRuntimeException("No algorithm specified for input mapping");
                    }
                }
            }
            break;
            case ("IMAGE"): {
                int xCoordinates = Integer.parseInt(this.configuration.getProperties().getProperty("numInputCoordinatesX"));    // Columns
                int yCoordinates = Integer.parseInt(this.configuration.getProperties().getProperty("numInputCoordinatesY"));    // Rows
                inputCoordinates = new Matrix(yCoordinates * xCoordinates, 3);
                for (int y = 0; y < yCoordinates; y++) {        // For each row
                    for (int x = 0; x < xCoordinates; x++) {    // For each column
                        inputCoordinates.set((y * xCoordinates) + x, 0, x);
                        inputCoordinates.set((y * xCoordinates) + x, 1, y);
                        //inputCoordinates.set( (y*xCoordinates)+x, 2, -1);
                    }
                }
            }
            break;
            default: {
                LOGGER.info("No input mapping method was specified.");
                throw new NeuCubeRuntimeException("No input mapping method was specified.");
            }
        }
        return inputCoordinates;
    }

    /**
     * This functions executes the connection algorithm and creates the synaptic
     * connections among neurons.
     */
    public void runConnectionAlgorithm() {
        networkController.createConnections(this.neucube.getConnectionAlgorithm());
    }

    /**
     * Executes the encoding method to transform all raw data samples into spike
     * trains.
     *
     * @return
     */
    public boolean runEncoder() {
        return this.runEncoder(this.dataController.getDataSamples());
    }

    /**
     * Executes the encoding method to transform raw data into spike trains.
     *
     * @param data
     * @return
     */
    public boolean runEncoder(ArrayList<DataSample> data) {
        return this.dataController.runEncoder(this.neucube.getEncodingAlgorithm(), data);
    }

    /**
     * This function executes a cross-validation method using the whole dataset
     * previously loaded. It gets the map of the class labels and the data
     * samples for each o them from the whole dataset previously loaded. Then it
     * calls the {@link #runCrossvalidation(java.util.HashMap) function to perform the exepriments.
     *
     * @return
     */
    public ConfusionMatrix runCrossvalidation() {
        return this.runCrossvalidation(neucube.getSSTD().getDataSamples());
    }

    /**
     * This function executes a cross-validation method using the selected
     * training data. First, it creates a map of the class labels and the data
     * samples for each o them and calls the {@link #runCrossvalidation(java.util.HashMap)
     * function to perform the exepriments.
     *
     * @param trainingData
     * @return a confusion matrix of actual vs predicted values
     */
    public ConfusionMatrix runCrossvalidation(ArrayList<DataSample> trainingData) {
        HashMap<Double, ArrayList<DataSample>> map;
        
        if (this.neucube.getProblemType() == Tasks.CLASSIFICATION) {
            map= this.dataController.detectClasses(trainingData);
        }else{
            map =this.dataController.getDataClasses();
        }

        ConfusionMatrix cf = null;
        if (this.runCrossvalidation(map)) {
            double[] actualValues = new double[trainingData.size()];
            double[] predictedValues = new double[trainingData.size()];
            for (int i = 0; i < trainingData.size(); i++) {
                actualValues[i] = trainingData.get(i).getClassId();
                predictedValues[i] = trainingData.get(i).getValidationClassId();
            }
            cf = new ConfusionMatrix(actualValues, predictedValues);
        }
        return cf;
    }

    /**
     * This function executes the cross validation method and shows the overall
     * classification measures. The data set is repeatedly split into a training
     * dataset and a validation dataset. To validate the model performance, an
     * additional test dataset held out from cross-validation should be used.
     * The following steps are executed: 1 splits the data set into training and
     * validation data sets, see the {@link #splitData(java.util.HashMap)}
     * function. The number of experiments depends on the chosen method, e.g. if
     * the method is the K-fold cross validation, this function will execute k
     * experiments using different set for training and testing; on the other
     * hand, if it is the Monte Carlo method, the number of experiments must be
     * indicated previously. 2 For each the function will select the set for
     * training and the set for validation from the cross-validation method and
     * executes an experiment
     * {@link #runExperiment(java.util.ArrayList, java.util.ArrayList)}. If the
     * task is classification it will will run the statistics (e.g. confusion
     * matrix) of the cross-validation method, otherwise it will calculate the
     * regression metric.
     *
     * @param map a map that contains the class label and the data samples for
     * each one.
     * @return
     */
    private boolean runCrossvalidation(HashMap<Double, ArrayList<DataSample>> map) {
        LOGGER.info("Executing cross validation.");
        long processTime = System.nanoTime();
        if (this.splitData(map)) {
            for (int fold = 0; fold < this.neucube.getCrossValidation().getNumFolds(); fold++) {
                LOGGER.info("- Fold " + fold);
                this.neucube.getCrossValidation().setCurrentFold(fold);
                this.runExperiment(this.neucube.getCrossValidation().getTrainingData(fold), this.neucube.getCrossValidation().getValidationData(fold));
                //if (this.neucube.getProblemType() == Tasks.CLASSIFICATION) {
                //this.neucube.getCrossValidation().runStatistics(fold);
                if (this.neucube.getProblemType() == Tasks.REGRESSION) {
                    this.neucube.getCrossValidation().calculateRegression(fold);
                }
                LOGGER.info("- Fold " + fold + " complete");
            }
        }
        LOGGER.info("Cross validation complete (time " + ((System.nanoTime() - processTime) / 1000000) + ")");
        return true;
    }

    /**
     * Gets an m-by-2 matrix that contains the result of a classification task.
     * There, m is the number of samples of the validation data, the actual and
     * predicted values are recorded in the first and second column of the
     * matrix respectively. The number of fold is according the number of
     * experiments performed, which it is usually one, the the index is zero.
     *
     * @return the regression matrix
     */
    public Matrix getRegressionMatrix() {
        return this.neucube.getCrossValidation().getRegressionMatrix(0);
    }

    /**
     * Shows an m-by-2 matrix that contains the result of a classification task.
     * There, m is the number of samples of the validation data, the actual and
     * predicted values are recorded in the first and second column of the
     * matrix respectively. The number of fold is according the number of
     * experiments performed, which it is usually one, the the index is zero.
     *
     */
    public void showRegressionMatrix() {
        this.neucube.getCrossValidation().getRegressionMatrix(0).show();
    }

    /**
     * Export the result of a regression task (see
     * {@link #getRegressionMatrix()}) into a comma delimited file.
     *
     * @param fileName the file name
     */
    public void exportRegressionMatrix(String fileName) {
        this.neucube.getCrossValidation().getRegressionMatrix(0).export(this.getStrPath() + File.separator + fileName, ",");
    }

    /**
     * Shows the root mean square error between the actual and the predicted
     * time series of a regression task.
     */
    public void showRegressionRMSE() {
        System.out.println(this.neucube.getCrossValidation().getOverallRMSE());
    }
    
    public void exportRMSE(String fileName){
        StringBuffer sb=new StringBuffer();
        sb.append(this.neucube.getCrossValidation().getOverallRMSE());
        Util.saveStringToFile(sb, this.getStrPath() + File.separator +fileName);
    }
    public double getRMSE(){
        return this.neucube.getCrossValidation().getOverallRMSE();
    }

    /**
     * Get the confusion matrix of an experiment of a classification task after
     * performing the cross validation into a comma delimited file.
     *
     * @param fold
     * @return
     */
    public ConfusionMatrix getConfusionMatrix(int fold) {
        return this.neucube.getCrossValidation().getConfusionMatrix(fold);
    }

    /**
     * Shows the confusion matrix of an experiment of a classification task
     * after performing the cross validation.
     *
     * @param fold
     */
    public void showConfusionMatrix(int fold) {
        this.neucube.getCrossValidation().getConfusionMatrix(fold).show();
    }

    /**
     * Shows the main metrics derived from the confusion matrix.
     *
     * @param fold the cross validation fold
     */
    public void showMeasures(int fold) {
        this.neucube.getCrossValidation().getConfusionMatrix(fold).showMetrics();
    }

    public void showConfusionMatrices() {
        int numFolds = this.neucube.getCrossValidation().getNumFolds();
        for (int i = 0; i < numFolds; i++) {
            System.out.println("Confusion matrix fold " + (i + 1));
            ConfusionMatrix matrix = this.getConfusionMatrix(i);
            matrix.show();
            matrix.showMetrics();
        }
    }

    /**
     * Export the confusion matrix of an experiment of a classification task
     * after performing the cross validation into a comma delimited file.
     *
     * @param fileName the file name
     * @param fold the fold of a cross validation
     */
    public void exportConfusionMatrix(String fileName, int fold) {
        this.getConfusionMatrix(fold).export(this.getStrPath() + File.separator + fileName, ",");
    }

    /**
     * Gets the overall confusion matrix of all experiment of a classification
     * task after performing the cross validation.
     *
     * @return
     */
    public Matrix getOverallConfusionMatrix() {
        return this.neucube.getCrossValidation().getOverallConfusionMatrix();
    }

    /**
     * Shows the overall confusion matrix of all experiment of a classification
     * task after performing the cross validation.
     */
    public void showOverallConfusionMatrix() {
        this.getOverallConfusionMatrix().show();
    }

    /**
     * Export the overall confusion matrix (mean of all confusion matrix) of the
     * cross validation into a comma delimited file.
     *
     * @param fileName the file name
     */
    public void exportOverallConfusionMatrix(String fileName) {
        this.getOverallConfusionMatrix().export(this.getStrPath() + File.separator + fileName, ",");
    }

    /**
     * Shows the overall of all metrics of the cross validation.
     */
    public void showOverallMetrics() {
        this.neucube.getCrossValidation().showOverallMetrics();
    }

    public void exportOveralMetrics(String fileName) {
        this.neucube.getCrossValidation().exportOverallMetrics(this.getStrPath() + File.separator + fileName);
    }

    public StringBuffer getOverallMetricsToString() {
        return this.neucube.getCrossValidation().getOverallMetricsToString();
    }

    /**
     * This function splits the data set into training and validation data sets
     * according to the cross validation algorithm previously configured and set
     * into the NeuCube. These sets are stored in the trainingData and
     * validationData lists of the {@link jneucube.data.SpatioTemporalData}
     * class.
     *
     * @param mapDataClasses a map that contains the class label and the data
     * samples that corresponds to each one.
     * @return true if the data set is not empty and was split correctly
     */
    public boolean splitData(HashMap<Double, ArrayList<DataSample>> mapDataClasses) {

        if (!neucube.getSSTD().getDataClasses().isEmpty()) {
            this.neucube.getCrossValidation().clear();
            this.neucube.getCrossValidation().split(mapDataClasses);
            return true;
        } else {
            LOGGER.error(Messages.DATA_SET_EMPTY.toString());
        }
        return false;
    }

    /**
     * This function splits the data set into training and validation data sets
     * according to the cross validation algorithm previously configured and set
     * into the NeuCube. These sets are stored in the trainingData and
     * validationData lists of the {@link jneucube.data.SpatioTemporalData}
     * class.
     *
     * @param crossValidationMethod
     * @param mapDataClasses a map that contains the class label and the data
     * samples that corresponds to each one.
     * @return true if the data set is not empty and was split correctly
     */
    public boolean splitData(CrossValidation crossValidationMethod, HashMap<Double, ArrayList<DataSample>> mapDataClasses) {
        if (!neucube.getSSTD().getDataClasses().isEmpty()) {
            crossValidationMethod.clear();
            crossValidationMethod.split(mapDataClasses);
            return true;
        } else {
            LOGGER.error(Messages.DATA_SET_EMPTY.toString());
        }
        return false;
    }

    /**
     * This function performs a complete experiment by executing: 1 the encoding
     * process using the whole data set if it has not been encoded, 2 the
     * unsupervised training using the set of data for training, 3 the
     * supervised training using that set of data for training, 4 the validation
     * process using the data set for validation. The function returns a n-by-4
     * validation matrix (see {@link #runValidation(java.util.ArrayList)}) where
     * every n row is associated to a data sample and the columns indicate the
     * id, the actual class, the predicted class, and classification status
     * (status (0) error, (1) correct.
     *
     * @param trainingData the set of data for training
     * @param validationData the set of data for validation
     * @return a validation matrix
     */
    public Matrix runExperiment(ArrayList<DataSample> trainingData, ArrayList<DataSample> validationData) {
        LOGGER.info("- Running experiment ");
        long processTime = System.nanoTime();
        this.fitModel(trainingData);
        Matrix validationMatrix = this.evaluateModel(validationData);
        LOGGER.info("- Experiment complete (time " + ((System.nanoTime() - processTime) / 1000000) + ")");
        return validationMatrix;
    }

    /**
     * This function fits the SNN model to the trining data. 1 executes
     * unsupervised learning. 2 Propagates the training data set for detecting
     * the activated neurons. The output neurons so that and then supervised
     * training) the SNN on the training dataset.
     *
     * @param trainingData
     */
    public void fitModel(ArrayList<DataSample> trainingData) {
        LOGGER.info("- Model fitting");
        long processTime = System.nanoTime();
        if (!this.neucube.getSSTD().isEncoded()) {
            this.neucube.getEncodingAlgorithm().encode(trainingData, this.neucube.getSSTD().getDataSamples(), 0, this.neucube.getSSTD().getNumRecords());   // Encodes both training and validation data
            this.neucube.getSSTD().setEncoded(true);
        }
        // Executing the unsupervised training
        this.runUnsupervised(trainingData);
        // Propagates the training data set for detecting the activated neurons        
        //this.propagateDataset(trainingData);
        //Executing the supervised training
        this.runSupervised(trainingData);

        LOGGER.info("- Model fitting complete (time " + ((System.nanoTime() - processTime) / 1000000) + ")");
    }

    public void setRecordFiringActivity(boolean recordFiringActivity) {
        this.networkController.setRecordFiringActivity(recordFiringActivity);
    }
    
    public void setRecordMembranePotential(boolean recordMembranePotential) {
        this.networkController.setRecordMembranePotential(recordMembranePotential);
    }

    /**
     * This function propagates the whole dataset previously loaded through the
     * SNN. For each data sample it calls the {@link #propagateSample(jneucube.data.DataSample, boolean, boolean) function.
     *
     * @param unlimited true if it is required to propagate not only the
     * information in the data sample but all the information in the SNN.
     * @param delays true if it is required to use synaptic delays
     * @return the time steps for propagating the dataset.
     */
    public int propagateDataset(boolean unlimited, boolean delays) {
        ArrayList<DataSample> dataset = this.neucube.getSSTD().getDataSamples();
        return this.propagateDataset(dataset, unlimited, delays);
    }

    /**
     * This function propagates the whole dataset previously loaded through the
     * SNN. For each data sample it calls the {@link #propagateSample(jneucube.data.DataSample, boolean, boolean) function.
     * By default, it only propagates the spike trains generated after the encoding process without synaptic delays.
     *
     * @return the time steps for propagating the dataset.
     */
    public int propagateDataset() {
        ArrayList<DataSample> dataset = this.neucube.getSSTD().getDataSamples();
        return this.propagateDataset(dataset, false, false);
    }

    /**
     * This function propagates the whole dataset previously loaded through the
     * SNN. For each data sample it calls the {@link #propagateSample(jneucube.data.DataSample, boolean, boolean) function.
     * By default, it only propagates the spike trains generated after the encoding process without synaptic delays.
     *
     * @param dataset The set of samples to propagate
     * @return the time steps for propagating the dataset.
     */
    public int propagateDataset(ArrayList<DataSample> dataset) {
        return this.propagateDataset(dataset, false, false);
    }

    /**
     * This function propagates the samples of a dataset through the SNN. For
     * each data sample it calls the {@link #propagateSample(jneucube.data.DataSample, boolean, boolean) function.
     *
     * @param dataset The set of samples to propagate
     * @param unlimited true if it is required to propagate not only the
     * information in the data sample but all the information in the SNN.
     * @param delays true if it is required to use synaptic delays
     * @return the time steps for propagating the dataset.
     */
    public int propagateDataset(ArrayList<DataSample> dataset, boolean unlimited, boolean delays) {
        if (!this.neucube.getSSTD().isEncoded()) {
            this.runEncoder(dataset);
        }
        return this.networkController.propagateDataset(dataset, unlimited, delays);
    }

//    /**
//     * This function loads a comma delimited CSV file and propagates the data
//     * into the SNN. It calls the {@link #propagateSample(jneucube.data.DataSample, boolean, boolean) function.
//     *
//     * @param sampleFileName the data sample to propagate through the SNN
//     * @param isEncoded set true if the data are spike trains
//     * @param unlimited true if it is required to propagate not only the
//     * information in the data sample but all the information in the SNN.
//     * @param delays true if it is required to use synaptic delays
//     * @return the time steps for propagating the sample
//     */
//    public int propagateSample(String sampleFileName, boolean isEncoded, boolean unlimited, boolean delays) {
//        try {
//            DataSample sample = this.dataController.loadSample(new File(this.strPath + File.separator + sampleFileName), isEncoded);
//            sample.setEncoded(isEncoded);
//            return this.propagateSample(sample, unlimited, delays);
//        } catch (IOException ex) {
//            LOGGER.error(ex.getMessage());
//        }
//        return 0;
//    }
//    /**
//     * This function propagates the data sample into the SNN. Depending on the
//     * parameters unlimited and delays the function will propagate information
//     * in different modalities. If it is required to propagate just the data in
//     * the sample set the unlimited=false, otherwise, if it is required to
//     * continue the process after all data in the sample has been propagated but
//     * there are pending neurons or synapses to release spikes set
//     * unlimited=true. If it is required to use synaptic delays set delays=true,
//     * otherwise set to delays=false. For more detail see
//     * {@link jneucube.network.NetworkController#propagateSample(jneucube.data.DataSample, int)}.
//     * null null null null null null null null null null null null null null
//     * null null null null     {@link jneucube.network.NetworkController#propagateSampleUnlimited(jneucube.data.DataSample, int)},     
//     * {@link jneucube.network.NetworkController#propagateSampleWithDelays(jneucube.data.DataSample, int)}
//     * {@link jneucube.network.NetworkController#propagateSampleUnlimitedWithDelays(jneucube.data.DataSample, int)}
//     *
//     * @param dataSample the data sample to propagate through the SNN
//     * @param unlimited true if it is required to propagate not only the
//     * information in the data sample but all the information in the SNN.
//     * @param delays true if it is required to use synaptic delays
//     * @return the time steps for propagating the sample
//     */
//    public int propagateSample(DataSample dataSample, boolean unlimited, boolean delays) {
//        if (dataSample != null) {
//            if (!dataSample.isEncoded()) {
//                this.neucube.getEncodingAlgorithm().encode(dataSample);
//            }
//            return this.networkController.propagateSample(dataSample, unlimited, delays);
//        } else {
//            LOGGER.error(Messages.DATA_SET_EMPTY.toString());
//        }
//        return 0;
//    }
    /**
     * Runs the unsupervised learning algorithm using all the dataset samples.
     * This function calls the {@link #runUnsupervised(java.util.ArrayList)}
     * function, which resets the network to its initial state. Note that the
     * spatio-temporal data must be previously encoded into spike trains and the
     * status set to true {@link jneucube.data.SpatioTemporalData#encoded}
     *
     * @return
     */
    public boolean runUnsupervised() {
        ArrayList<DataSample> trainingData = this.neucube.getSSTD().getDataSamples();
        return this.runUnsupervised(trainingData);
    }

    /**
     * Runs the unsupervised learning algorithm using the index sample of the
     * list data samples. This function calls the
     * {@link #runUnsupervised(jneucube.data.DataSample)} function.
     *
     * @param index the index of the sample from the list of samples
     * @param isEncoded if the data contains spike trains
     * @return
     */
    public boolean runUnsupervised(int index, boolean isEncoded) {
        DataSample sample = this.neucube.getSSTD().getDataSamples().get(index);
        sample.setEncoded(isEncoded);
        return this.runUnsupervised(sample);
    }

    /**
     * Runs the unsupervised learning algorithm with the data read from a Comma
     * Delimited (CSV) file. This function calls the
     * {@link #runUnsupervised(jneucube.data.DataSample)} function.
     *
     * @param sampleFileName the file that contains the data
     * @param isEncoded if the data contains spike trains
     * @return
     */
    public boolean runUnsupervised(String sampleFileName, boolean isEncoded) {
        try {
            DataSample sample = this.dataController.loadSample(new File(this.strPath + File.separator + sampleFileName), isEncoded);
            sample.setEncoded(isEncoded);
            return this.runUnsupervised(sample);
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
            return false;
        }
    }

    /**
     * Runs the unsupervised learning algorithm using one data sample. During
     * the unsupervised learning, the SNN will change their connections
     * (synaptic weights). An analysis of the evolution of the SNN after
     * training can be done by adding the weights to the SNN
     * {@link jneucube.network.NetworkController#recordCurrentWeights()}, then
     * getting the weight matrix
     * {@link jneucube.network.NetworkController#getWeightMatrix(int)} and
     * exporting the data into a comma delimited file
     * {@link jneucube.util.Matrix#export(java.lang.String, java.lang.String)}.
     * Note that saving the weights after every sample will increase the use of
     * memory of the system.
     *
     * recordCurrentWeights();
     *
     * Analysis of the changes can be performed bFor analysis of
     *
     * @param sample the encoded data sample
     * @return
     */
    private boolean runUnsupervised(DataSample sample) {
        if (sample != null) {
            if (!sample.isEncoded()) {
                this.neucube.getEncodingAlgorithm().encode(sample);
            }
            LearningAlgorithm algorithm = this.neucube.getUnsupervisedLearningAlgorithm();
            boolean recordActiveNeurons = false; // records the neurons that emit a spike during the simulation
            this.networkController.trainSample(algorithm, sample, 0, recordActiveNeurons);
        } else {
            LOGGER.error(Messages.DATA_SET_EMPTY.toString());
        }
        return false;
    }

    /**
     * Runs the unsupervised learning algorithm given a training dataset. The
     * function resets the network to its initial state before propagating the
     * spike trains. Note that the spatio-temporal data must be previously
     * encoded into spike trains and the status set to true
     * {@link jneucube.data.SpatioTemporalData#encoded}. The data set for
     * training could be obtained from a fold of the cross validation method.
     * After the training process is finished, the final synaptic weights are
     * saved and can be exported into a comma separated file for its analysis,
     * independently of the saving weight mode of the learning algorithm (see
     * {@link jneucube.trainingAlgorithms.LearningAlgorithm#savingWeightMode}).
     * If it is set to false, the initial and final weight matrices can be
     * obtained by accessing the {@link #networkController} and calling the
     * function {@link jneucube.network.NetworkController#getWeightMatrix(int)},
     * (0=initial, 1=final), or the current weight matrix (final weights)
     * {@link jneucube.network.NetworkController#getWeightMatrix()}. If it is
     * set to true, the weight matrices can be obtained at any simulation time
     * by calling the function
     * {@link jneucube.network.NetworkController#getWeightMatrix(int)} which
     * parameter indicates the simulation time point. Exporting the data into a
     * comma delimited file just requires calling the
     * {@link jneucube.util.Matrix#export(java.lang.String, java.lang.String)}
     * function.
     *
     * @param trainingData
     * @return
     */
    public boolean runUnsupervised(ArrayList<DataSample> trainingData) {
        LOGGER.info("Unsupervised training ");
        long processTime = System.nanoTime();
        if (!trainingData.isEmpty()) {
            if (neucube.getSSTD().isEncoded()) {
                jneucube.trainingAlgorithms.LearningAlgorithm algorithm = this.neucube.getUnsupervisedLearningAlgorithm();
                this.networkController.resetNetworkForTraining();
                boolean recordActiveNeurons = true; // records the neurons that emit a spike during the simulation
                this.networkController.train(this.neucube.getNetwork(), trainingData, algorithm, recordActiveNeurons);
                LOGGER.info("Unsupervised training complete in " + ((System.nanoTime() - processTime) / 1000000) + "ms; training weight changes:" + algorithm.getTrainingWeightChanges());
                return true;
            } else {
                LOGGER.error(Messages.DATA_ENCODED_FAlSE.toString());
            }
        } else {
            LOGGER.error(Messages.DATA_SET_EMPTY.toString());
        }
        return false;

        //return this.runUnsupervised(this.neucube.getCrossValidation().getTrainingData());
    }

    /**
     * Executes the supervised learning over all the data set.
     *
     * @return
     */
    public boolean runSupervised() {
        ArrayList<DataSample> trainingData = this.neucube.getSSTD().getDataSamples();
        return this.runSupervised(trainingData);
    }

    /**
     * Executes the supervised learning algorithm using the index of the sample
     * form the list of data samples.
     *
     * @param index the index of the sample from the list of samples
     * @param isEncoded if the data contains spike trains
     * @return
     */
    public boolean runSupervised(int index, boolean isEncoded) {
        DataSample dataSample = this.neucube.getSSTD().getDataSamples().get(index);
        dataSample.setEncoded(isEncoded);
        // create output neuron here
        try {
            this.networkController.createOutputNeuron(dataSample);
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(NeuCubeController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return this.runSupervised(dataSample);
    }

    /**
     * Executes the supervised learning algorithm using the file name containing
     * the data for training
     *
     * @param sampleFileName the file that contains the data
     * @param isEncoded if the data contains spike trains
     * @return
     */
    public boolean runSupervised(String sampleFileName, boolean isEncoded) {
        try {
            DataSample sample = this.dataController.loadSample(new File(this.strPath + File.separator + sampleFileName), isEncoded);
            sample.setEncoded(isEncoded);
            return this.runUnsupervised(sample);
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
            return false;
        }
    }

    /**
     * Executes the supervised learning algorithm using a single data sample.
     * The followings step are executed. 1 The data will be encoded if the
     * encoded status of the sample is set to false. 2 A new output neuron is
     * created. 3 The neural activity is reset before propagating the spike
     * trains. 4 Propagation of the sample setting the training mode to true.
     * Note that the elapsed training time is set to zero for each data sample
     * since accumulative time is not relevant for the supervised training.
     *
     * @param sample the encoded data sample
     * @return true if no error was found
     */
    private boolean runSupervised(DataSample sample) {
        if (sample != null) {
//            try {
            if (!sample.isEncoded()) {
                this.neucube.getEncodingAlgorithm().encode(sample);
            }
            //this.networkController.createOutputNeuron(sample);
            this.networkController.resetNeuralActivity();
            LearningAlgorithm algorithm = this.neucube.getSupervisedLearningAlgorithm();
            this.networkController.trainSample(algorithm, sample, 0, false); // Note that the elapsed training time is irrelevant in this process 
            return true;
//            } catch (CloneNotSupportedException ex) {
//                java.util.logging.Logger.getLogger(NeuCubeController.class.getName()).log(Level.SEVERE, null, ex);
//            }
        } else {
            LOGGER.error(Messages.DATA_SET_EMPTY.toString());
        }
        return false;
    }

    /**
     * This function executes a supervised learning. Unlike the unsupervised
     * training where the complete reservoir is reset, here only the neural
     * activity is because the supervised learning will take the information
     * produced by a pre trained SNN for training the output neurons. Note that
     * every time this function is called, the output neurons are removed.
     *
     * @param trainingData
     * @return
     */
    public boolean runSupervised(ArrayList<DataSample> trainingData) {
        LOGGER.info("- Supervised training using " + trainingData.size() + " samples ");
        long processTime = System.nanoTime();
        if (!trainingData.isEmpty()) {
            this.networkController.removeNeuronsFromList(networkController.getNetwork().getOutputNeurons());
            for (DataSample dataSample : trainingData) {
                // create output neuron here
                try {
                    this.networkController.createOutputNeuron(dataSample);
                } catch (CloneNotSupportedException ex) {
                    Logger.getLogger(NeuCubeController.class.getName()).log(Level.SEVERE, null, ex);
                }
                this.runSupervised(dataSample);
            }
            LOGGER.info("- Supervised training complete  (" + trainingData.size() + " samples in " + ((System.nanoTime() - processTime) / 1000000) + "ms)");
            return true;
        } else {
            LOGGER.error(Messages.DATA_SET_EMPTY.toString());
        }
        return false;
    }

    /**
     * This function classifies all the samples of the specified data set. The
     * samples for validation must be previously labeled. The classification
     * value is recorded in the field
     * {@link jneucube.data.DataSample#validationClassId} of every
     * {@link jneucube.data.DataSample}. For optimal performance, this function
     * extracts the training and group sets from the network before performing
     * the classification of every data sample. The function returns a n-by-4
     * validation matrix where every n row is associated to a data sample and
     * the columns indicate the id, the actual class, the predicted class, and
     * classification status (0) no error, (1) error.
     *
     * @param dataSamples the data set for validation
     * @return the validation matrix which columns indicate the id, the actual
     * class, the predicted class, and classification status (0) no error, (1)
     * error
     */
    public Matrix evaluateModel(ArrayList<DataSample> dataSamples) {
        Matrix training = this.networkController.getTrainingMatrix();   // Set for training (matrix of features)
        Matrix group = this.networkController.getClassMatrix();         // Set for group
        return this.evaluateModel(dataSamples, training, group);
    }

    public Matrix evaluateModel(ArrayList<DataSample> dataSamples, Matrix training, Matrix group) {
        LOGGER.info("Testing the SNN model with " + dataSamples.size() + " samples ");
        long processTime = System.nanoTime();
        Matrix validation = new Matrix(dataSamples.size(), 4);
        //Matrix training = this.networkController.getTrainingMatrix();   // Set for training (matrix of features)
        //Matrix group = this.networkController.getClassMatrix();         // Set for group
        double classLabel;
        int index = 0;
        for (DataSample dataSample : dataSamples) {
            classLabel = this.getPrediction(dataSample, training, group);
            dataSample.setValidationClassId(classLabel);
            validation.set(index, 0, dataSample.getSampleId()); //[index][0] = dataSample.getClassId();
            validation.set(index, 1, dataSample.getClassId()); //[index][0] = dataSample.getClassId();
            validation.set(index, 2, dataSample.getValidationClassId()); //[index][0] = dataSample.getClassId();
            validation.set(index, 3, (dataSample.getClassId() == dataSample.getValidationClassId()) ? 0.0 : 1.0); //[index][0] = dataSample.getClassId();
            index++;
        }
        LOGGER.info("Testing complete ( " + dataSamples.size() + " samples in time " + ((System.nanoTime() - processTime) / 1000000) + ")");
        return validation;
    }

    /**
     * This function classifies a data sample from the spatio-temporal data set.
     * The classification value is recorded in the field
     * {@link jneucube.data.DataSample#validationClassId} of every
     * {@link jneucube.data.DataSample}. The data is classified by calling the
     * function {@link #runValidation(jneucube.data.DataSample)}. Do not iterate
     * over a list of sample and use this function, since the training and group
     * set will be created in every iteration. Use
     * {@link #runValidation(java.util.ArrayList)} instead. The function returns
     * a validation vector where the elements indicate the sample id, the actual
     * class, the predicted class, and classification status (0) error, (1)
     * correct.
     *
     * @param sampleIndex the index of the data sample
     * @param isEncoded true if the data contains spike trains, false if it is
     * raw data
     * @return validation vector
     */
    public double[] evaluateSample(int sampleIndex, boolean isEncoded) {
        DataSample sample = this.dataController.getDataSample(sampleIndex);
        sample.setEncoded(isEncoded);
        return this.evaluateSample(sample);
    }

    /**
     * This function classifies a data sample read from a (CSV) comma delimited
     * file. The classification value is recorded in the field
     * {@link jneucube.data.DataSample#validationClassId} of every
     * {@link jneucube.data.DataSample}. The data is classified by calling the
     * function {@link #runValidation(jneucube.data.DataSample)}. Do not iterate
     * over a list of sample and use this function, since the training and group
     * set will be created in every iteration. Use
     * {@link #runValidation(java.util.ArrayList)} instead. The function returns
     * a validation vector where the elements indicate the sample id, the actual
     * class, the predicted class, and classification status (0) error, (1)
     * correct.
     *
     * @param sampleFileName the file that contains the data
     * @param classLabel the class label to which the sample belongs to
     * @param isEncoded true if the data contains spike trains, false if it is
     * raw data
     * @return validation vector
     */
    public double[] evaluateSample(String sampleFileName, double classLabel, boolean isEncoded) {
        try {
            DataSample sample = this.dataController.loadSample(new File(this.strPath + File.separator + sampleFileName), isEncoded);
            sample.setEncoded(isEncoded);
            sample.setClassId(classLabel);
            return this.evaluateSample(sample);
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
            return null;
        }
    }

    /**
     * This function classifies a data sample. The classification value is
     * recorded in the field {@link jneucube.data.DataSample#validationClassId}
     * of every {@link jneucube.data.DataSample}. Since the training and group
     * sets for classification are created every time this function is called,
     * it is recommended to not call this function in an iterative process. Use
     * {@link #runValidation(java.util.ArrayList)} instead. The function returns
     * a validation vector where the elements indicate the sample id, the actual
     * class, the predicted class, and classification status (0) error, (1)
     * correct.
     *
     * @param sample the data sample to classify and validate
     * @return validation vector [sample id, class, validation, status]
     */
    public double[] evaluateSample(DataSample sample) {
        if (sample != null) {
            double[] validationVector = new double[4];
            if (!sample.isEncoded()) {
                this.neucube.getEncodingAlgorithm().encode(sample);
            }
            Matrix training = this.networkController.getTrainingMatrix();
            Matrix group = this.networkController.getClassMatrix();
            double classLabel;
            classLabel = this.getPrediction(sample, training, group);
            sample.setValidationClassId(classLabel);
            validationVector[0] = sample.getSampleId();
            validationVector[1] = sample.getClassId();
            validationVector[2] = sample.getValidationClassId();
            validationVector[3] = ((sample.getClassId() == sample.getValidationClassId()) ? 0.0 : 1.0);
            return validationVector;
        } else {
            LOGGER.error(Messages.DATA_SET_EMPTY.toString());
        }
        return null;
    }

    /**
     * This function classifies all the samples of the specified data set. The
     * classification value is recorded in the field
     * {@link jneucube.data.DataSample#validationClassId} of every
     * {@link jneucube.data.DataSample}. For optimal performance, this function
     * extracts the training and group sets from the network before performing
     * the classification of every data sample. The function returns a m-by-1
     * matrix where every n row is associated to a data sample and the column
     * indicates the predicted class.
     *
     * @param dataSamples the set of data to classify
     * @return an m-by-1 matrix with the labels of each sample
     */
    public Matrix recallDataSet(ArrayList<DataSample> dataSamples) {
        LOGGER.info("- Recall the SNN model");
        long processTime = System.nanoTime();
        Matrix labels = new Matrix(dataSamples.size(), 1);
        Matrix training = this.networkController.getTrainingMatrix();   // Set for training (matrix of features)
        Matrix group = this.networkController.getClassMatrix();         // Set for group
        double classLabel;
        int index = 0;
        for (DataSample dataSample : dataSamples) {
            classLabel = this.getPrediction(dataSample, training, group);
            dataSample.setValidationClassId(classLabel);
            labels.set(index, 0, dataSample.getValidationClassId()); //[index][0] = dataSample.getClassId();
            index++;
        }
        LOGGER.info("- Complete (time " + ((System.nanoTime() - processTime) / 1000000) + ")");
        return labels;
    }

    /**
     * This function classifies a new data sample. If an error occurs, the
     * function will return -1. It executes the following steps. 1 If the
     * encoding status of the sample is set to false, then it encodes the sample
     * using the encoding algorithm utilised during the training stage of the
     * network. 2 Gets the training set of samples which corresponds to the
     * weights of output neurons created during the supervised stage (training
     * matrix). 3 Gets the group of the set of samples (group matrix). 4 It
     * calls the
     * {@link  #getPrediction(jneucube.data.DataSample, jneucube.util.Matrix, jneucube.util.Matrix)}
     * function for getting the class label for the data sample.
     *
     * @param dataSample the data sample to classify
     * @return the class label of the data sample
     */
    public double recallSample(DataSample dataSample) {
        if (dataSample != null) {
            if (!dataSample.isEncoded()) {
                this.neucube.getEncodingAlgorithm().encode(dataSample);
            }
            Matrix training = this.networkController.getTrainingMatrix();
            Matrix group = this.networkController.getClassMatrix();
            return this.getPrediction(dataSample, training, group);
        } else {
            LOGGER.error(Messages.DATA_SET_EMPTY.toString());
        }
        return -1;
    }

    /**
     * This function classifies a new data sample. If an error occurs, the
     * function will return -1. It calls the
     * {@link #recallSample(jneucube.data.DataSample)} function to classify the
     * sample.
     *
     * @param dataSample the data sample to classify
     * @param isEncoded true if the data contains spike trains, false if it is
     * raw data
     * @return the class label of the data sample
     */
    public double recallSample(DataSample dataSample, boolean isEncoded) {
        dataSample.setEncoded(isEncoded);
        return this.recallSample(dataSample);
    }

    /**
     * This function classifies a data sample read from a (CSV) comma delimited
     * file. If an error occurs, the function will return -1. It calls the
     * {@link #recallSample(jneucube.data.DataSample)} function to classify the
     * sample.
     *
     * @param sampleFileName the file that contains the data
     * @param isEncoded true if the data contains spike trains, false if it is
     * raw data
     * @return he class label of the data sample
     */
    public double recallSample(String sampleFileName, boolean isEncoded) {
        try {
            DataSample dataSample = this.dataController.loadSample(new File(this.strPath + File.separator + sampleFileName), isEncoded);
            dataSample.setEncoded(isEncoded);
            return this.recallSample(dataSample);
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
            return -1;
        }
    }

    /**
     * Gets the proportion that the specified sample belongs to different
     * classes. The function executes the following steps. 1 Gets the training
     * set of samples which corresponds to the weights of output neurons created
     * during the supervised stage (training matrix). 2 Gets the group of the
     * set of samples (group matrix). 3 Executes the supervised training (only
     * for the specified sample) in which a new output neuron is created. 4 The
     * input weights of the new output neuron are recorded into a matrix (sample
     * matrix). 5 The new output neuron is removed from the network (see
     * {@link jneucube.network.NetworkController#removeNeuron(jneucube.spikingNeurons.SpikingNeuron)})
     * (if the neuron is not removed, it will take part of the network as a new
     * training sample that will impact when recalling new samples). 6 Calls the
     * {@link jneucube.classifiers.Classifier#getProbabilities(jneucube.util.Matrix, jneucube.util.Matrix, jneucube.util.Matrix)}
     * function, which parameters are the sample, the training, and the group
     * matrices, for getting a hashmap that indicates the proportion of the
     * classes that the sample belongs to.
     *
     * @param dataSample the data sample
     * @return a hashmap that contains the class and the proportion
     */
    public HashMap<Integer, Double> recallSampleProbabilities(DataSample dataSample) {
        if (dataSample != null) {
            if (!dataSample.isEncoded()) {
                this.neucube.getEncodingAlgorithm().encode(dataSample);
            }
            Matrix training = this.networkController.getTrainingMatrix();
            Matrix group = this.networkController.getClassMatrix();
            int idxOutputNeuron = this.neucube.getNetwork().getOutputNeurons().size();
            // create output neuron here
            try {
                this.networkController.createOutputNeuron(dataSample);
            } catch (CloneNotSupportedException ex) {
                Logger.getLogger(NeuCubeController.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.runSupervised(dataSample);
            SpikingNeuron outputNeuron = this.neucube.getNetwork().getOutputNeurons().get(idxOutputNeuron);
            //this.neucube.getNetwork().getOutputNeurons().remove(idxOutputNeuron); // removes the last output neuron of the list, which is associated to the sample
            Matrix sample = this.networkController.getNeuronInputWeights(outputNeuron);
            this.networkController.removeNeuron(outputNeuron);
            return this.neucube.getClassifier().getProbabilities(sample, training, group);
        } else {
            LOGGER.error(Messages.DATA_SET_EMPTY.toString());
        }
        return null;
    }

    /**
     * Gets the proportion that the specified sample belongs to different
     * classes. The function calls
     * {@link #recallSampleProbabilities(jneucube.data.DataSample)} function for
     * getting the classes and proportions.
     *
     * @param sampleFileName the file that contains the data
     * @param isEncoded true if the data contains spike trains, false if it is
     * raw data
     * @return a hashmap that contains the class and the proportion
     */
    public HashMap<Integer, Double> recallSampleProbabilities(String sampleFileName, boolean isEncoded) {
        try {
            DataSample dataSample = this.dataController.loadSample(new File(this.strPath + File.separator + sampleFileName), isEncoded);
            dataSample.setEncoded(isEncoded);
            return this.recallSampleProbabilities(dataSample);
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
        return null;
    }

    /**
     * Gets the proportion that the specified sample belongs to different
     * classes. The function calls
     * {@link #recallSampleProbabilities(jneucube.data.DataSample)} function for
     * getting the classes and proportions.
     *
     * @param dataSample the data sample
     * @param isEncoded true if the data contains spike trains, false if it is
     * raw data
     * @return a hashmap that contains the class and the proportion
     */
    public HashMap<Integer, Double> recallSampleProbabilities(DataSample dataSample, boolean isEncoded) {
        dataSample.setEncoded(isEncoded);
        return this.recallSampleProbabilities(dataSample);
    }

    /**
     * This function gets the predicted label of a data sample. It executes the
     * following steps. 1 Executes the supervised training in which a new output
     * neuron is created, then, adapts the output neuron pre synapses that are
     * connected to the reservoir. 2 The weights of the pre synapses are
     * recorded into a matrix (sample matrix). 3 The new output neuron is
     * removed from the network (see
     * {@link jneucube.network.NetworkController#removeNeuron(jneucube.spikingNeurons.SpikingNeuron)})
     * (if the neuron is not removed, it will take part of the network as a new
     * training sample that will impact when recalling new samples) 4 Calls the
     * {@link jneucube.classifiers.Classifier#classify(jneucube.util.Matrix, jneucube.util.Matrix, jneucube.util.Matrix)}
     * function to obtain the class label of the sample. The classification
     * value is recorded in the field
     * {@link jneucube.data.DataSample#validationClassId} of every
     * {@link jneucube.data.DataSample}.
     *
     * @param dataSample A 1 by m matrix of features to be classified. It must
     * have the same number of columns as the training matrix.
     * @param training A n by m Matrix used to group the rows in the matrix
     * Sample. This matrix must have the same number of columns as the sample.
     * Each row of the training matrix belongs to the group whose value is the
     * corresponding entry of the group matrix.
     * @param group A 1 by m matrix (vector of classes) whose distinct values
     * define the grouping of the rows in the training matrix.
     * @return the class of the sample
     */
    public double getPrediction(DataSample dataSample, Matrix training, Matrix group) {
        int idxOutputNeuron = this.neucube.getNetwork().getOutputNeurons().size();        
        try {
            this.networkController.createOutputNeuronPrediction(dataSample.getSampleId(),dataSample.getClassId());
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(NeuCubeController.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.runSupervised(dataSample);
        SpikingNeuron outputNeuron = this.neucube.getNetwork().getOutputNeurons().get(idxOutputNeuron);
        Matrix sample = this.networkController.getNeuronInputWeights(outputNeuron);
        //this.neucube.getNetwork().getOutputNeurons().remove(idxOutputNeuron); // removes the last output neuron of the list, which is associated to the sample
        this.networkController.removeNeuron(outputNeuron); // removes the neuron from the network, including all the connections associated to it.
        return this.neucube.getClassifier().classify(sample, training, group);
    }

    public void runOnlineExperiments() throws SchedulerException {
        OnlineReader onlineReader = configuration.configureOnlineReader();
        int maxBufferSize = Integer.parseInt(configuration.getProperties().getProperty("maxBufferSize"));

        LOGGER.info("------- Initializing jobs -------------------");
        SchedulerFactory sf = new StdSchedulerFactory();
        Scheduler sched = sf.getScheduler();

        JobDetail jobOnlineLearning = newJob(OnlineLearningJob.class).withIdentity("jobOnlineLearning", "onlineLearning").build();
        jobOnlineLearning.getJobDataMap().put(OnlineLearningJob.KEY_LEARNING_ALGORITHM, neucube.getUnsupervisedLearningAlgorithm());
        jobOnlineLearning.getJobDataMap().put(OnlineLearningJob.KEY_ENCODING_ALGORITHM, neucube.getEncodingAlgorithm());
        jobOnlineLearning.getJobDataMap().put(OnlineLearningJob.KEY_NETWORK, neucube.getNetwork());
        jobOnlineLearning.getJobDataMap().put(OnlineDataReaderJob.KEY_SPATIO_TEMPORAL_DATA, neucube.getSSTD());
        jobOnlineLearning.getJobDataMap().put(OnlineLearningJob.KEY_FLAG, true);

        SimpleTrigger triggerOnlineSTDP = newTrigger().withIdentity("triggerOnlineSTDP", "onlineLearning").startNow()
                .withSchedule(simpleSchedule().withIntervalInMilliseconds(100).repeatForever()).build();

//        SimpleTrigger triggerOnlineSTDP = newTrigger().withIdentity("triggerOnlineSTDP", "onlineLearning").startNow()
//                .withSchedule(simpleSchedule().withRepeatCount(10).withIntervalInMilliseconds(500)).build();
        Date ft = sched.scheduleJob(jobOnlineLearning, triggerOnlineSTDP);

        LOGGER.info(jobOnlineLearning.getKey() + " will run at: " + ft + " and repeat: " + triggerOnlineSTDP.getRepeatCount() + " times, every "
                + triggerOnlineSTDP.getRepeatInterval() / 1000 + " seconds");

        JobDetail jobOnlineDataReader = newJob(OnlineDataReaderJob.class).withIdentity("OnlineDataReaderJob", "onlineLearning").build();
        jobOnlineDataReader.getJobDataMap().put(OnlineDataReaderJob.KEY_ONLINE_READER, onlineReader);
        jobOnlineDataReader.getJobDataMap().put(OnlineDataReaderJob.KEY_SPATIO_TEMPORAL_DATA, neucube.getSSTD());
        jobOnlineDataReader.getJobDataMap().put(OnlineDataReaderJob.KEY_MAX_BUFFER_SIZE, maxBufferSize);
        jobOnlineDataReader.getJobDataMap().put(OnlineDataReaderJob.KEY_DATA_TYPE, DataSample.TRAINING);

        SimpleTrigger triggerOnlineData = newTrigger().withIdentity("triggerOnlineData", "onlineLearning").startNow()
                .withSchedule(simpleSchedule().repeatForever().withIntervalInMilliseconds(200)).build();//  .withIntervalInSeconds(2).withRepeatCount(4)).build();        

//        SimpleTrigger triggerOnlineData = newTrigger().withIdentity("triggerOnlineData", "onlineLearning").startNow()
//                .withSchedule(simpleSchedule().withRepeatCount(5).withIntervalInMilliseconds(1000)).build();//  .withIntervalInSeconds(2).withRepeatCount(4)).build();                
        ft = sched.scheduleJob(jobOnlineDataReader, triggerOnlineData);
        LOGGER.info(jobOnlineDataReader.getKey() + " will run at: " + ft + " and repeat: " + triggerOnlineData.getRepeatCount() + " times, every "
                + triggerOnlineData.getRepeatInterval() / 1000 + " seconds");

        sched.start();
        LOGGER.info("------- Started Scheduler -----------------");
        try {
            // sleep for one minute for triggers to file....
            //Thread.sleep(10L * 1000L);
            Thread.sleep(2 * 60 * 1000);
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage());
        }

        LOGGER.info("------- Shutting Down ---------------------");
        sched.shutdown(true);
        LOGGER.info("------- Shutdown Complete -----------------");
        neucube.getUnsupervisedLearningAlgorithm().getTrainingTime();
        this.networkController.recordCurrentWeights();
    }

    /**
     * Saves the synaptic weight matrix into a comma delimited file. Note that
     * the field
     * {@link jneucube.trainingAlgorithms.LearningAlgorithm#savingWeightMode} is
     * set to true to access the weights at a specific simulation time point. If
     * the field is set to false, the network will record the initial and final
     * weights after the training process. The set of weights can be accessed by
     * setting the parameter time to 0 or 1 for initial or final weights
     * respectively. and 1 respectively.
     *
     * @param fileName the file name
     * @param time the simulation time point
     */
    public void exportWeightMatrix(String fileName, int time) {
        this.networkController.getWeightMatrix(time).export(this.strPath + File.separator + fileName, ",");
    }

    /**
     * Saves the current synaptic weight matrix into a comma delimited file.
     *
     * @param fileName the file name
     */
    public void exportCurrentWeightMatrix(String fileName) {
        this.networkController.getWeightMatrix().export(this.strPath + File.separator + fileName, ",");
    }

    /**
     * Saves the current synaptic weights into a comma delimited file.
     *
     * @param fileName the file name
     */
    public void exportCurrentWeights(String fileName) {
        this.networkController.getCurrenttWeigths().export(this.strPath + File.separator + fileName, ",");
    }

    public void exportConnectionMatrix(String fileName) {
        this.networkController.getConnectionMatrix().export(this.strPath + File.separator + fileName, ",");
    }

    /**
     * Saves the synaptic weights at a specific stimulation time into a comma
     * delimited file. Note that the field
     * {@link jneucube.trainingAlgorithms.LearningAlgorithm#savingWeightMode} is
     * set to true to access the weights at a specific simulation time point. If
     * the field is set to false, the network will record the initial and final
     * weights after the training process. The set of weights can be accessed by
     * setting the parameter time to 0 or 1 for initial or final weights
     * respectively. and 1 respectively.
     *
     * @param fileName the file name
     * @param time the simulation time point
     */
    public void exportWeights(String fileName, int time) {
        this.networkController.getWeigths(time).export(this.strPath + File.separator + fileName, ",");
    }

    /**
     * Saves the spike trains of an encoded sample into a comma delimited file.
     *
     * @param fileName the file name
     * @param sampleId the index of the sample
     */
    public void saveSampleSpikeTrains(String fileName, int sampleId) {
        this.getDataController().saveSampleSpikeTrains(this.getStrPath() + File.separator + fileName, sampleId);
    }

    /**
     * Exports the firing times of all the reservoir neurons, which includes the
     * input neurons into a comma delimited file.
     *
     * @param fileName the file name
     */
    public void exportFiringActivity(String fileName) {
        this.networkController.getFiringActivity().export(this.getStrPath() + File.separator + fileName, ",");
    }

    /**
     * This function sets the proper conditions for a new training process. It
     * resets every neuron of the network (reservoir and output neurons)
     */
    public void resetNetworkForTraining() {
        this.networkController.resetNetworkForTraining();
    }

    /**
     * Executes all the steps of the NeuCube till the one specified. Step 0:
     * Creates the NeuCube and saves it
     *
     * @param step
     * @throws java.io.IOException
     */
    public void run(int step) throws IOException {
        int currentStep = 0;
        while (currentStep <= step) {
            switch (currentStep) {
                case 0: {
                    if (this.initializeNetwork()) {
                        neucube.save(this.strPath + File.separator + "NeuCube_initial.xml");
                        //double[][] distribution = neucube.getNetwork().getNetworkNormalDistributionConductanceValues();
                        //Matrix matrixDistribution = new Matrix(distribution);
                        //matrixDistribution.export(this.strPath + File.separator +"NeuCube_initial_distribution.csv", ",");
                        currentStep++;
                    }
                }
                break;
                case 1: {
                    if (this.loadSpatioTemporalData(this.strPath)) {
                        if (Boolean.parseBoolean(this.configuration.getProperties().getProperty("stdOffLine"))) {
                            // this.runValidationExperiments();
                        } else {
                            try {
                                //String strPath = prop.getProperty("stdDirectory");
                                this.runOnlineExperiments();
                                //neucube.save(strPath + File.separator + "NeuCube_trained.xml");
                                //double[][] distribution = neucube.getNetwork().getNetworkNormalDistributionConductanceValues();
                                //Matrix matrixDistribution = new Matrix(distribution);
                                //matrixDistribution.export(strPath + File.separator + "NeuCube_final_distribution.csv", ",");
                            } catch (SchedulerException ex) {
                                java.util.logging.Logger.getLogger(NeuCubeController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        currentStep++;
                    } else {
                        break;
                    }
                }
                break;
            }
        }
    }

    /**
     * @return the neucube
     */
    public NeuCube getNeucube() {
        return neucube;
    }

    /**
     * @param neucube the neucube to set
     */
    public void setNeucube(NeuCube neucube) {
        this.neucube = neucube;
    }

    /**
     * @return the strPath
     */
    public String getStrPath() {
        return strPath;
    }

    /**
     * @param strPath the strPath to set
     */
    public void setStrPath(String strPath) {
        this.strPath = strPath;
    }

    public boolean saveNeucube(String file) {
        //return neucube.save(this.getStrPath() + File.separator + file);
        return neucube.save2(this.getStrPath() + File.separator + file);
    }

    /**
     * Loads a NeuCube model from a XML file.
     *
     * @param fileName the XML file that contains the NeuCube model.
     */
    public void loadNeuCube(String fileName) {
        File file = new File(this.getStrPath() + File.separator + fileName);
        Long startTime = System.nanoTime();
        LOGGER.info("------- Loading project from file " + file.toString() + " -------");
        XStream xstream = new XStream();
        this.neucube = (NeuCube) xstream.fromXML(file);
        this.initializeControllers();
        LOGGER.info("------- Complete (time " + ((System.nanoTime() - startTime) / 1000000) + " milliseconds seconds) -------");
    }

    /**
     * Prune the inactive neurons after propagating the whole dataset. For more
     * details see
     * {@link jneucube.network.NetworkController#pruneInactiveNeurons(java.util.ArrayList, boolean, boolean)}.
     *
     */
    public void pruneInactiveNeurons() {
        ArrayList<DataSample> dataset = this.getDataController().getDataSamples();
        boolean unlimited = false;
        boolean delay = false;
        this.getNetworkController().pruneInactiveNeurons(dataset, unlimited, delay);
    }

    /**
     * Prune the inactive neurons after propagating the dataset for the
     * specified class label. For more details see
     * {@link jneucube.network.NetworkController#pruneInactiveNeurons(java.util.ArrayList, boolean, boolean)}.
     *
     * @param classLabel the class label of the data
     */
    public void pruneInactiveNeurons(double classLabel) {
        ArrayList<DataSample> dataset = this.getDataController().getDataClasses().get(classLabel);
        boolean unlimited = false;
        boolean delay = false;
        this.getNetworkController().pruneInactiveNeurons(dataset, unlimited, delay);
    }

    /**
     * Set the weights to the last weight values recorded, e.g., after training.
     */
    public void setNetworkLastState() {
        this.getNetworkController().setNetworkLastState();
    }

    /**
     * Prune the inactive neurons after propagating the specified dataset. For
     * more details see
     * {@link jneucube.network.NetworkController#pruneInactiveNeurons(java.util.ArrayList, boolean, boolean)}.
     *
     * @param dataset the data set to simulate
     * @param unlimited
     * @param delay
     */
    public void pruneInactiveNeurons(ArrayList<DataSample> dataset, boolean unlimited, boolean delay) {
        this.getNetworkController().pruneInactiveNeurons(dataset, unlimited, delay);
    }

    public HashMap<SpikingNeuron, Integer> getInactiveNeurons(ArrayList<DataSample> dataset, boolean unlimited, boolean delay) {
        return this.getNetworkController().getFunctionalNeurons(dataset, unlimited, delay);
    }

    public ArrayList<Double> getFunctionalWeights(ArrayList<DataSample> dataset, boolean unlimited, boolean delays) {
        return this.getNetworkController().getFunctionalWeights(dataset, unlimited, delays);
    }

    /**
     * Prune the inactive synapses after the unsupervised training. For more
     * details see
     * {@link jneucube.network.NetworkController#pruneInactiveSynapses()}.
     */
    public void pruneInactiveSynapses() {
        this.getNetworkController().pruneInactiveSynapses();
    }

    public boolean clearDataset() {
        this.neucube.getSSTD().clearData();
        return true;
    }

    public boolean clearCrossvalidation() {
        this.neucube.getCrossValidation().clear();
        return true;
    }

    public boolean clearSpikeTrainSamples() {
        this.neucube.getSSTD().clearSpikeTrains();
        return true;
    }

    public boolean clearEncoder() {
        this.neucube.getEncodingAlgorithm().clear();
        return true;
    }

    /**
     * @return the configuration
     */
    public NeuCubeConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * @param configuration the configuration to set
     */
    public void setConfiguration(NeuCubeConfiguration configuration) {
        this.configuration = configuration;
    }

}
