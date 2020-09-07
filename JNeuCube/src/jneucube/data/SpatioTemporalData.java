/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.data;

import jneucube.tasks.Tasks;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import jneucube.util.Matrix;
import jneucube.util.Messages;
import jneucube.util.StringMatrix;
import jneucube.util.Util;
import static jneucube.log.Log.LOGGER;

/**
 *
 * @author Josafath Israel Espinosa Ramos (Centro de Investigacion en
 * Computacion, Instituto Politecnico Nacional, Mexico 2015)
 */
public class SpatioTemporalData {

    public static final int SINGLE_VARIABLE = 1;    // Added June 2016
    public static final int MULTIPLE_VARIABLES = 2; // Added June 2016    
    public static final int MEMORY_SOURCE=1;        // The data is read and stored in memory
    public static final int DISK_SOURCE=2;          // Thr data will be read from disk
    
    private ArrayList<DataSample> dataSamples = new ArrayList<>();      // *All samples of the data set
    private ArrayList<DataSample> trainingData = new ArrayList<>();     // *List of samples for training
    private ArrayList<DataSample> validationData = new ArrayList<>();   // *List of samples for validation    
  
    private double rateForTraining = 0.7;
    private int numSamples = 0;             // Number of samples of the data set
    private int numTrainingSamples = 0;     // Number of samples for training
    private int numValidationSamples = 0;   // Numer of samples for validation

    private int numVariables = 1;   // *Number of variables to be analysed (temperature, presure, humidity) (Added June 2016)
    private ArrayList<String> variableLabels = new ArrayList<>(Arrays.asList("Variable 1"));
    private int numFeatures = 0;    // *Number of features (number of EEG channels, number of sensors)
    private int numRecords = 0;     // *Number of time records per sample (sampling rate, e.g. 128 Hz=128 records)
    private int numClasses = 0;     // *Number of classes 

    private String[] sampleLabels;  // The labels of the samples
    private String[] featureLabels; // The labels of the features
    private String[] classLabels;   // The labels of the classes
    private double[] classIds;      // *Class identifiers

    private String dataName = "";   // The alias of the data
    private String directory = "";  // The directory that contains the files (samples, class labels, feature labels) of the data set
    private int dataSource=MEMORY_SOURCE;
    private HashMap<Double, ArrayList<DataSample>> dataClasses = new HashMap(); // *Hashmap that contains the samples per class

    private int startTrainingTime;  // Starting time (first record) for training
    private int endTrainingTime;    // End time (last record) for training
    private int startValidationTime;// Starting time (first record) for validation
    private int endValidationTime;  // End time (last record) for validation

    private boolean encoded = false;    // True if the data has been encoded
    private boolean offLine=true;
    private double spikeRate = 0;   // The spike rate after encoding
    private Matrix selectedFeatures;    // A binary matrix that sets the features to be analysed
    //ogger LOGGER = LoggerFactory.getLogger(SpatioTemporalData.class);

    public SpatioTemporalData() {

    }

    /**
     * Loads the data from a directory. The name of the files must meet NeuCube
     * criteria. For samples [sam[0-9].*\\.csv], for file that contains the
     * classes of each sample [tar_class.*\\.csv], and the labels of input
     * neurons ["lbl*.*"]. By default the function loads raw data (no encoded),
     * if the data has been previously encoded into spike trains, the property
     * isEncoded must be set to true before calling this function.
     *
     * @param path
     * @param task Classification or Regression [Tasks.CLASSIFICATIONS,
     * Tasks.REGRESSION]
     * @param message Any
     * @return
     */
    public boolean loadData(File path, int task, Messages message) {
        //File dir = new File(strDir);        
        File classFile = null;
        File labelFile = null;
        if (path.isDirectory()) {
            File[] sampleFiles = path.listFiles((File dir, String name) -> name.matches("sam[0-9].*\\.csv"));
            File[] sampleClassesFile = path.listFiles((File dir, String name) -> name.matches("tar_class.*\\.csv"));
            File[] sampleLabelsFile = path.listFiles((File dir, String name) -> name.matches("lbl*.*"));

            if (sampleClassesFile.length > 0) {
                classFile = sampleClassesFile[0];
            }
            if (sampleLabelsFile.length > 0) {
                labelFile = sampleLabelsFile[0];
            }
            return this.loadData(sampleFiles, classFile, labelFile, task, message);
        } else {
            message = Messages.DATA_SAMPLE;
            return false;
        }
    }

    public boolean loadFile(File sampleFile) {
        this.dataSamples.clear();
        LOGGER.info("Loading " + sampleFile.toString() + " file ");
        try {
            DataSample sample = loadSample(sampleFile);
            sample.setSampleId(1);
            sample.setSampleLabel("Sample");
            this.dataSamples.add(sample);
            this.numSamples = 1;
            this.numClasses = 1;
            this.numFeatures = sample.getNumFeatures();  // The first sample determines the number of features of the spatio temporal data        
            this.numRecords = sample.getNumRecords();    // The first sample determines the number of data points of the spatio temporal data  
            LOGGER.info("Complete");
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     *
     * @param sampleFiles An array containing the name of the sample files in
     * CSV format (separated by comas)
     * @param sampleClassesFile The file that contains all sample classes
     * @param featureLabelFile
     * @param task
     * @param message
     * @return
     */
    private boolean loadData(File[] sampleFiles, File sampleClassesFile, File featureLabelFile, int task, Messages message) {
        this.numSamples = sampleFiles.length;
        this.dataSamples.clear();
        try {
            Matrix classes = new Matrix(sampleClassesFile, ",");    //Loads the classes file 
            if (classes.getRows() >= this.numSamples) {
                Util.sortFiles(sampleFiles);
                this.loadSamples(sampleFiles);
                this.setClasses(this.dataSamples, sampleClassesFile, task);
                this.setFeaturesLabels(featureLabelFile);
                //status = validateSamples(message);
                if (validateSamples(message)) {
                    message = Messages.DATA_LOAD_SUCCESS;
                } else {
                    this.clearData();
                }
            } else {
                message = Messages.DATA_CLASS_FILE;
                return false;
            }
        } catch (IOException e) {
            this.clearData();
            return false;
        }
        return true;
    }

    /**
     * This function loads every sample file and puts it into the an array list
     * of DataSamples. By default the function loads raw data (no encoded), if
     * the data has been previously encoded into spike trains, the property
     * isEncoded must be set to true before calling this function.
     *
     * @param sampleFiles
     * @return
     */
    public boolean loadSamples(File[] sampleFiles) {
        this.numSamples = sampleFiles.length;
        this.numClasses = 1;
        this.dataSamples.clear();
        LOGGER.info("Loading " + sampleFiles.length + " files ");
        try {
            for (int i = 0; i < sampleFiles.length; i++) {  // Reads all sample files and stores the data in a DataSample object
                LOGGER.debug("   - Loading sample id " + i + " " + sampleFiles[i].getName());
                DataSample sample = loadSample(sampleFiles[i]);
                sample.setSampleId(i);
                sample.setSampleLabel("Sample " + (i + 1));
                this.dataSamples.add(sample);
            }
            this.numFeatures = this.dataSamples.get(0).getNumFeatures();  // The first sample determines the number of features of the spatio temporal data        
            this.numRecords = this.dataSamples.get(0).getNumRecords();    // The first sample determines the number of data points of the spatio temporal data                    
            LOGGER.info("Complete");
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * This functions loads a file containing one spatio-temporal sample. By
     * default the function loads raw data (no encoded), if the data has been
     * previously encoded into spike trains, the property isEncoded must be set
     * to true before calling this function.
     *
     * @param dataFile The file containing the data
     * @return The data sample
     */
    private DataSample loadSample(File dataFile) throws IOException {
        DataSample sample = new DataSample();
        Matrix data = new Matrix(dataFile, ",");
        sample.setNumFeatures(data.getCols());
        sample.setNumRecords(data.getRows());
        if (this.isEncoded() == true) {
            sample.setSpikeData(data);
            sample.setEncoded(true);
        } else {
            sample.setData(data);
        }
        return sample;
    }

    /**
     * Set the class to which each sample belongs to
     *
     * @param samples
     * @param sampleClassesFile A comma separated file containing the class for
     * each sample. The file is an n x 1 matrix, where n is the number of
     * samples
     * @param task The task to perform. The task are defined in the Tasks class;
     */
    private void setClasses(ArrayList<DataSample> samples, File sampleClassesFile, int task) throws IOException {
        this.dataClasses.clear();
        LOGGER.info("Assigning classes to the samples");
        Matrix classes = new Matrix(sampleClassesFile, ",");    //Loads the classes file an stores data in a matrix        
        this.classIds = classes.getVecCol(0);
        if (classIds.length == this.numSamples) {
            for (int i = 0; i < this.numSamples; i++) {
                samples.get(i).setClassId(classIds[i]);
            }
            if (task == Tasks.CLASSIFICATION) {
                this.dataClasses = this.detectClasses(samples);
                this.setNumClasses(dataClasses.size());
            } else {
                this.dataClasses.put(1.0, samples);
            }
        }
        LOGGER.info("Complete");
    }

    /**
     * This function maps the number of classes and the number of samples per
     * class
     *
     * @param samples
     * @return
     */
    public HashMap<Double, ArrayList<DataSample>> detectClasses(ArrayList<DataSample> samples) {
        HashMap<Double, ArrayList<DataSample>> classes = new HashMap<>();
        double classId;
        for (DataSample dataSample : samples) {    // Group the samples by class
            classId = dataSample.getClassId();
            if (classes.containsKey(classId)) {
                classes.get(classId).add(dataSample);
            } else {
                ArrayList<DataSample> tempSample = new ArrayList<>();
                tempSample.add(dataSample);
                classes.put(classId, tempSample);
            }
        }
        this.setNumClasses(classes.size());
        return classes;
    }

    /**
     * This function set the feature labels. If not feature label file is
     * provided, the program assign the labels
     *
     * @param featureLabelFile
     */
    public void setFeaturesLabels(File featureLabelFile) {
        LOGGER.info("Setting feature labels");
        if (featureLabelFile != null) {                 // Set the labels
            this.loadFeatureLabels(featureLabelFile);
        } else {  // create the names of the labels
            LOGGER.info("Feature labels file not found. Creating feature labels");
            this.createFeatureLables(this.numFeatures);
        }
        this.setSelectedFeatures(new Matrix(this.numFeatures, 1, 1.0));
        LOGGER.info("Complete");
    }

    public void loadFeatureLabels(File featureLabelFile) {
        StringMatrix labels = new StringMatrix(featureLabelFile, ",");
        for (int j = 0; j < this.numFeatures; j++) {
            if (j < labels.getRows()) {
                this.featureLabels[j] = labels.get(j, 0);
            }
        }
    }

    public void createFeatureLables(int numFeatures) {
        this.featureLabels = new String[numFeatures];
        for (int i = 0; i < numFeatures; i++) {
            this.featureLabels[i] = "Feature " + (i + 1);
        }
    }

    public boolean validateSamples(Messages message) {
        boolean isValid = true;
        if (this.classIds.length != this.numSamples) {
            message = Messages.DATA_CLASS_FILE;
            return false;
        }
        for (DataSample dataSample : this.dataSamples) {
            if (dataSample.getNumFeatures() != this.numFeatures) {
                message = Messages.DATA_SAMPLE_FEATURES;
                isValid = false;
            }
            if (dataSample.getNumRecords() != this.numRecords) {
                message = Messages.DATA_SAMPLE_RECORDS;
                isValid = false;
            }
        }
        return isValid;
    }

    public ArrayList<DataSample> randomiseData(ArrayList<DataSample> samples) {
        ArrayList<DataSample> randomSamples = new ArrayList<>();
        int[] idxs = Util.getRandomPermutation(samples.size());
        for (int i = 0; i < samples.size(); i++) {
            randomSamples.add(samples.get(idxs[i]));
        }
        return randomSamples;
    }

    public void showInfo() {
        System.out.println("Name " + this.getDataName());
        System.out.println("Samples " + this.getNumSamples());
        System.out.println("Recordings " + this.getNumRecords());
        System.out.println("Features " + this.getNumFeatures());
        System.out.println("Ecoded data " + this.isEncoded());
        System.out.println("Records per class");
        this.dataClasses.entrySet().stream().forEach((entry) -> {
            System.out.println(entry.getKey() + " " + entry.getValue().size());
        });
    }

    /**
     * @return the dataName
     */
    public String getDataName() {
        return dataName;
    }

    /**
     * @param dataName the dataName to set
     */
    public void setDataName(String dataName) {
        this.dataName = dataName;
    }

    /**
     * @return the numClasses
     */
    public int getNumClasses() {
        return numClasses;
    }

    /**
     * @param numClasses the numClasses to set
     */
    public void setNumClasses(int numClasses) {
        this.numClasses = numClasses;
    }

    /**
     * @return the numSamples
     */
    public int getNumSamples() {
        return numSamples;
    }

    public void setNumSamples(int numSamples) {
        this.numSamples = numSamples;
    }

    /**
     * @return the numRecords
     */
    public int getNumRecords() {
        return numRecords;
    }

    public void setNumRecords(int numRecords) {
        this.numRecords = numRecords;
    }

    /**
     * @return the numFeatures
     */
    public int getNumFeatures() {
        return numFeatures;
    }

    public void setNumFeatures(int numFeatures) {
        this.numFeatures = numFeatures;
    }

    /**
     * @return the featureLabels
     */
    public String[] getFeatureLabels() {
        return featureLabels;
    }

    public void setFeatureLabels(String[] featureLabels) {
        this.featureLabels = featureLabels;
    }

    /**
     * @return the classIds
     */
    public double[] getClassIds() {
        return classIds;
    }

    /**
     * @return the directory
     */
    public String getDirectory() {
        return directory;
    }

    /**
     * @return the classLabels
     */
    public String[] getClassLabels() {
        return classLabels;
    }

    /**
     * @param classLabels the classLabels to set
     */
    public void setClassLabels(String[] classLabels) {
        this.classLabels = classLabels;
    }

    /**
     * @return the sampleLabels
     */
    public String[] getSampleLabels() {
        return sampleLabels;
    }

    /**
     * @param sampleLabels the sampleLabels to set
     */
    public void setSampleLabels(String[] sampleLabels) {
        this.sampleLabels = sampleLabels;
    }

    /**
     * @return the startTrainingTime
     */
    public int getStartTrainingTime() {
        return startTrainingTime;
    }

    /**
     * @param startTrainingTime the startTrainingTime to set
     */
    public void setStartTrainingTime(int startTrainingTime) {
        this.startTrainingTime = startTrainingTime;
    }

    /**
     * @return the endTrainingTime
     */
    public int getEndTrainingTime() {
        return endTrainingTime;
    }

    /**
     * @param endTrainingTime the endTrainingTime to set
     */
    public void setEndTrainingTime(int endTrainingTime) {
        this.endTrainingTime = endTrainingTime;
    }

    /**
     * @return the startValidationTime
     */
    public int getStartValidationTime() {
        return startValidationTime;
    }

    /**
     * @param startValidationTime the startValidationTime to set
     */
    public void setStartValidationTime(int startValidationTime) {
        this.startValidationTime = startValidationTime;
    }

    /**
     * @return the endValidationTime
     */
    public int getEndValidationTime() {
        return endValidationTime;
    }

    /**
     * @param endValidationTime the endValidationTime to set
     */
    public void setEndValidationTime(int endValidationTime) {
        this.endValidationTime = endValidationTime;
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
     * @return the dataSamples
     */
    public ArrayList<DataSample> getDataSamples() {
        return dataSamples;
    }

    /**
     * @param dataSamples the dataSamples to set
     */
    public void setDataSamples(ArrayList<DataSample> dataSamples) {
        this.dataSamples = dataSamples;
    }

    /**
     * @return the trainingData
     */
    public ArrayList<DataSample> getTrainingData() {
        return trainingData;
    }

    /**
     * @param trainingData the trainingData to set
     */
    public void setTrainingData(ArrayList<DataSample> trainingData) {
        this.trainingData = trainingData;
        this.numTrainingSamples = trainingData.size();
    }

    /**
     * @return the validationData
     */
    public ArrayList<DataSample> getValidationData() {
        return validationData;
    }

    /**
     * @param validationData the validationData to set
     */
    public void setValidationData(ArrayList<DataSample> validationData) {
        this.validationData = validationData;
        this.numValidationSamples = validationData.size();
    }

    public String getClassesSummary() {
        String string = "{";
        if (!this.dataClasses.isEmpty()) {
            for (Map.Entry<Double, ArrayList<DataSample>> entry : this.dataClasses.entrySet()) {
                string += entry.getKey() + "->" + entry.getValue().size() + ", ";
            }
            string = string.substring(0, string.length() - 2);
        }
        string += "}";
        return string;
    }

    /**
     * @return the rateForTraining
     */
    public double getRateForTraining() {
        return rateForTraining;
    }

    /**
     * @param rateForTraining the rateForTraining to set
     */
    public void setRateForTraining(double rateForTraining) {
        this.rateForTraining = rateForTraining;
    }

    /**
     * @return the spikeRate
     */
    public double getSpikeRate() {
        return spikeRate;
    }

    /**
     * @param spikeRate the spikeRate to set
     */
    public void setSpikeRate(double spikeRate) {
        this.spikeRate = spikeRate;
    }

    /**
     * @return the encoded
     */
    public boolean isEncoded() {
        return encoded;
    }

    /**
     * @param encoded the encoded to set
     */
    public void setEncoded(boolean encoded) {
        this.encoded = encoded;
    }

    /**
     * @param directory the directory to set
     */
    public void setDirectory(String directory) {
        this.directory = directory;
    }

    /**
     * @return the selectedFeatures
     */
    public Matrix getSelectedFeatures() {
        return selectedFeatures;
    }

    /**
     * @param selectedFeatures the selectedFeatures to set
     */
    public void setSelectedFeatures(Matrix selectedFeatures) {
        this.selectedFeatures = selectedFeatures;
    }

    /**
     * @return the numVariables
     */
    public int getNumVariables() {
        return numVariables;
    }

    /**
     * @param numVariables the numVariables to set
     */
    public void setNumVariables(int numVariables) {
        this.numVariables = numVariables;
    }

    /**
     * @return the dataClasses
     */
    public HashMap<Double, ArrayList<DataSample>> getDataClasses() {
        return dataClasses;
    }

    public void setDataClasses(HashMap<Double, ArrayList<DataSample>> dataClasses) {
        this.dataClasses = dataClasses;
    }

    /**
     * @return the variableLabels
     */
    public ArrayList<String> getVariableLabels() {
        return variableLabels;
    }

    /**
     * @param variableLabels the variableLabels to set
     */
    public void setVariableLabels(ArrayList<String> variableLabels) {
        this.variableLabels = variableLabels;
    }

    public void addVariable(String label) {
        this.variableLabels.add(label);
        this.numVariables++;
    }

    public void editVariable(int idx, String newLabel) {
        this.variableLabels.set(idx, newLabel);
    }

    public void removeVariable(int idx) {
        if (this.numVariables > 1) {
            this.variableLabels.remove(idx);
            this.numVariables--;
        }
    }

    public void clearData() {
        this.dataSamples.clear();
        this.trainingData.clear();
        this.validationData.clear();

        this.numSamples = 0;             // Number of samples of the data set
        this.numTrainingSamples = 0;     // Number of samples for training
        this.numValidationSamples = 0;   // Numer of samples for validation

        this.numVariables = 1; // *Number of variables to be analysed (temperature, presure, humidity) (Added June 2016)
        this.variableLabels.clear();
        this.numFeatures = 0;    // *Number of features (number of EEG channels, number of sensors)
        this.numRecords = 0;     // *Number of time records per sample (sampling rate, e.g. 128 Hz=128 records)
        this.numClasses = 0;     // *Number of classes 

        this.sampleLabels = null;
        this.featureLabels = null;
        this.classLabels = null;
        this.classIds = null;

        //this.dataName = "";   // The alias of the data
        //this.directory = "";  // The directory that contains the files (samples, class labels, feature labels) of the data set
        this.dataClasses.clear();

        //this.encoded = false;    // True if the data has been encoded
        this.spikeRate = 0;   // The spike rate after encoding
        this.selectedFeatures = null;
    }

    public void clearSpikeTrains() {
        this.dataSamples.forEach((sample) -> {
            sample.clearSpikeData();
        });
        this.encoded = false;    // True if the data has been encoded
        this.spikeRate = 0;   // The spike rate after encoding
    }

    /**
     * @return the dataSource
     */
    public int getDataSource() {
        return dataSource;
    }

    /**
     * @param dataSource the dataSource to set
     */
    public void setDataSource(int dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * @return the offLine
     */
    public boolean isOffLine() {
        return offLine;
    }

    /**
     * @param offLine the offLine to set
     */
    public void setOffLine(boolean offLine) {
        this.offLine = offLine;
    }
}
