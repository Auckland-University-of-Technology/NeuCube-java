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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import jneucube.encodingAlgorithms.EncodingAlgorithm;
import jneucube.util.Matrix;
import jneucube.util.Messages;
import jneucube.util.NeuCubeRuntimeException;
import jneucube.util.StringMatrix;
import jneucube.util.Util;
import static jneucube.log.Log.LOGGER;

/**
 *
 * @author Josafath Israel Espinosa Ramos
 */
public class DataController {

    private SpatioTemporalData data;

    /**
     * Loads the data from a directory. The name of the files must meet NeuCube
     * criteria. For samples [sam[0-9].*\\.csv], for file that contains the
     * classes of each sample [tar_class.*\\.csv], and the labels of input
     * neurons ["lbl*.*"]. By default the function loads raw data (no encoded),
     * if the data has been previously encoded into spike trains, the property
     * isEncoded must be set to true before calling this function.
     *
     * @param path
     * @param classFile
     * @param task Classification or Regression [Tasks.CLASSIFICATIONS,
     * Tasks.REGRESSION]
     * @param message Any
     * @return
     */
    public boolean loadData(File path, File classFile, int task, Messages message) {
        //File dir = new File(strDir);        
        //File classFile = null;
        File labelFile = null;
        if (path.isDirectory()) {
            this.data.setDirectory(path.getAbsolutePath());
            //File[] sampleFiles = path.listFiles((File dir, String name) -> name.matches("sam[a-zA-Z]*[0-9].*\\.csv"));
                                
            File[] sampleFiles = path.listFiles((File dir, String name) -> name.matches("sam[0-9].*(_[0-9].*)?\\.csv"));
            //File[] sampleClassesFile = path.listFiles((File dir, String name) -> name.matches("tar_class.*\\.csv"));
            File[] sampleLabelsFile = path.listFiles((File dir, String name) -> name.matches("lbl*.*"));

//            if (sampleClassesFile.length > 0) {
//                classFile = sampleClassesFile[0];
//            }
            if (sampleLabelsFile.length > 0) {
                labelFile = sampleLabelsFile[0];
            }
            return this.loadData(sampleFiles, classFile, labelFile, task, message);
        } else {
            message = Messages.DATA_SAMPLE;
            return false;
        }
    }

    public boolean removeData() {
        LOGGER.info("Removing Spatio-temporal data");
        this.data.clearData();        
        LOGGER.info("Complete");
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
        this.data.setNumSamples(sampleFiles.length);
        this.data.getDataSamples().clear();
        try {
            Matrix classes = new Matrix(sampleClassesFile, ",");    //Loads the classes file 
            if (classes.getRows() >= this.data.getNumSamples()) {
                Util.sortFiles(sampleFiles);
                this.loadSamples(sampleFiles);
                this.setClasses(this.data.getDataSamples(), sampleClassesFile, task);
                this.setFeaturesLabels(featureLabelFile);
                //status = validateSamples(message);
                if (validateSamples(message)) {
                    message = Messages.DATA_LOAD_SUCCESS;
                } else {
                    this.data.clearData();
                }
            } else {
                message = Messages.DATA_CLASS_FILE;
                return false;
            }
        } catch (IOException e) {
            this.data.clearData();
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
        LOGGER.info("Loading " + sampleFiles.length + " files ");
        try {
            for (int i = 0; i < sampleFiles.length; i++) {  // Reads all sample files and stores the data in a DataSample object
                LOGGER.info("- Loading sample id " + i + " " + sampleFiles[i].getName());
                DataSample sample = loadSample(sampleFiles[i], this.data.isEncoded());
                sample.setSampleId(i);
                sample.setSampleLabel("Sample " + (i + 1));
                sample.setSampleFileName(sampleFiles[i].getAbsolutePath());
                this.data.getDataSamples().add(sample);
            }
            this.data.setNumFeatures(this.data.getDataSamples().get(0).getNumFeatures());   // The first sample determines the number of features of the spatio temporal data        
            this.data.setNumRecords(this.data.getDataSamples().get(0).getNumRecords());     // The first sample determines the number of data points of the spatio temporal data                    
            LOGGER.info("Complete "+sampleFiles.length + " files ");
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
     * @param isEncoded indicates whether the data is encoded or not
     * @return The data sample
     * @throws java.io.IOException
     */
    public DataSample loadSample(File dataFile, boolean isEncoded) throws IOException {
        DataSample sample = new DataSample();
        Matrix temp = new Matrix(dataFile, ",");
        sample.setNumFeatures(temp.getCols());
        sample.setNumRecords(temp.getRows());
        if (this.data.getDataSource() == SpatioTemporalData.MEMORY_SOURCE) {
            if (isEncoded) {
                sample.setSpikeData(temp);
                sample.setEncoded(true);
            } else {
                sample.setData(temp);
            }
        } else {
            if (isEncoded) {
                sample.setEncoded(true);
            }
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
        this.data.getDataClasses().clear();
        LOGGER.info("Assigning classes to the samples");
        Matrix classes = new Matrix(sampleClassesFile, ",");    //Loads the classes file an stores data in a matrix
        double[] classIds = classes.getVecCol(0);
        if (classIds.length == this.data.getNumSamples()) {
            for (int i = 0; i < this.data.getNumSamples(); i++) {
                samples.get(i).setClassId(classIds[i]);
            }
            if (task == Tasks.CLASSIFICATION) {
                this.data.setDataClasses(this.detectClasses(samples));
                this.data.setNumClasses(this.data.getDataClasses().size());
            } else {
                this.data.getDataClasses().put(1.0, samples);
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
        this.data.setNumClasses(classes.size());
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
            this.createFeatureLables(this.data.getNumFeatures());
        }
        this.data.setSelectedFeatures(new Matrix(this.data.getNumFeatures(), 1, 1.0));
        LOGGER.info("Complete");
    }

    public void loadFeatureLabels(File featureLabelFile) {
        StringMatrix labels = new StringMatrix(featureLabelFile, ",");
        this.data.setFeatureLabels(new String[labels.getRows()]);
        for (int j = 0; j < this.data.getNumFeatures(); j++) {
            if (j < labels.getRows()) {
                this.data.getFeatureLabels()[j] = labels.get(j, 0);
            }
        }
    }

    public void createFeatureLables(int numFeatures) {
        String[] featureLabels = new String[numFeatures];
        for (int i = 0; i < numFeatures; i++) {
            featureLabels[i] = "Feature " + (i + 1);
        }
        this.data.setFeatureLabels(featureLabels);
    }

    public boolean validateSamples(Messages message) {
        boolean isValid = true;
        for (DataSample dataSample : this.data.getDataSamples()) {
            if (dataSample.getNumFeatures() != this.data.getNumFeatures()) {
                message = Messages.DATA_SAMPLE_FEATURES;
                isValid = false;
            }
        }
        return isValid;
    }

    /**
     * Randomly permutes the spatio-temporal data set using the
     * {@link Collections#shuffle(java.util.List)} function.
     *
     */
    public void permuteData() {
        Collections.shuffle(this.data.getDataSamples());
    }

    /**
     * Randomly permutes the spatio-temporal data set using the
     * {@link Collections#shuffle(java.util.List)} function.
     *
     * @param dataSet the dataset
     */
    public void permuteData(ArrayList<DataSample> dataSet) {
        Collections.shuffle(dataSet);
    }

    public void showDataInfo() {
        System.out.println("Name " + this.data.getDataName());
        System.out.println("Samples " + this.data.getNumSamples());
        System.out.println("Recordings " + this.data.getNumRecords());
        System.out.println("Features " + this.data.getNumFeatures());
        System.out.println("Ecoded data " + this.data.isEncoded());
        System.out.println("Records per class");
        // Function to sort map by Key 

        ArrayList<Double> sortedKeys = new ArrayList<>(this.data.getDataClasses().keySet());
        Collections.sort(sortedKeys);

        sortedKeys.forEach((key) -> {
            System.out.println(key + " " + this.data.getDataClasses().get(key).size());
        });
//        this.data.getDataClasses().entrySet().stream().forEach((entry) -> {
//            System.out.println(entry.getKey() + " " + entry.getValue().size());
//        });
    }

    /**
     * @return the data spatio-temporal data set
     */
    public SpatioTemporalData getData() {
        return data;
    }

    /**
     * @param data the spatio-temporal data to set
     */
    public void setData(SpatioTemporalData data) {
        this.data = data;
    }

    /**
     * Gets all the elements of the data set.
     *
     * @return a list with all the data samples
     */
    public ArrayList<DataSample> getDataSamples() {
        return this.data.getDataSamples();
    }

    /**
     * Gets a specific data sample from the spatio-temporal data set.
     *
     * @param index
     * @return a data sample
     */
    public DataSample getDataSample(int index) {
        return this.data.getDataSamples().get(index);
    }

    /**
     * Gets a sublist of samples in the data set.
     *
     * @param fromIndex low endpoint (inclusive) of the subList
     * @param toIndex high endpoint (exclusive) of the subList
     * @return a view of the specified range within the list of data samples
     */
    public ArrayList<DataSample> getDataSamples(int fromIndex, int toIndex) {
        return new ArrayList<>(this.data.getDataSamples().subList(fromIndex, toIndex));
    }

    public ArrayList<DataSample> getRandomDataSamples(int numSamples, double classLabel) {
        ArrayList<DataSample> samples = new ArrayList<>();
        ArrayList<DataSample> temp = new ArrayList<>();
        temp.addAll(this.getDataClasses().get(classLabel));
        int size = this.getDataClasses().get(classLabel).size();
        int idx;
        for (int i = 0; i < numSamples; i++) {
            size = temp.size();
            idx = Util.getRandomInt(0, size);
            samples.add(temp.get(idx));
            temp.remove(idx);
        }
        return samples;
    }

    /**
     * Gets the whole data set and selects a rate of random samples of the
     * specified class label. The selected samples are added to a new ArrayList
     * of data samples.
     *
     * @param rate the rate of the data samples
     * @param classLabel the class label
     * @return
     */
    public ArrayList<DataSample> getRandomDataSamples(double rate, double classLabel) {
        ArrayList<DataSample> samples = new ArrayList<>();
        samples.addAll(this.getDataClasses().get(classLabel));
        Collections.shuffle(samples);
        int splitIndex = (int) (samples.size() * rate);
        samples.subList(splitIndex, samples.size()).clear();
        return samples;
    }

    /**
     * Gets the whole data set and selects a sublist of random samples of each
     * class label. The selected samples are added to a new ArrayList of data
     * samples.
     *
     * @param rate the rate of the data samples
     * @return an array list of samples
     */
    public ArrayList<DataSample> getRandomDataSamples(double rate) {
        ArrayList<DataSample> samples = new ArrayList<>();
        for (Double key : this.getDataClasses().keySet()) {
            samples.addAll(this.getRandomDataSamples(rate, key));
        }
        return samples;
    }

    /**
     * Gets a sublist of samples in the data set.
     *
     * @param rate a number between 0 (inclusive) and 1 (inclusive)
     * @return a view of the specified rate of data samples
     */
    public ArrayList<DataSample> getDataSamples(double rate) {
        if (!Util.isInRange(rate, 0.0, 1.0)) {
            throw new IllegalArgumentException(rate + " not in range " + 0.0 + ".." + 1.0);
        }
        int toIndex = (int) (this.data.getDataSamples().size() * rate);
        return new ArrayList<>(this.data.getDataSamples().subList(0, toIndex));
    }

    public int getNumDataSamples() {
        return this.getDataSamples().size();
    }

    /**
     * This function return the list of training samples. It should be called
     * after splitting the data using any cross validation method, see
     * {@link jneucube.crossValidation.CrossValidation}, otherwise will return
     * and empty list.
     *
     * @return a list of samples for training
     */
    public ArrayList<DataSample> getTrainingSamples() {
        return this.data.getTrainingData();
    }

    /**
     * This function return the list of validation samples. It should be called
     * after splitting the data using any cross validation method, see
     * {@link jneucube.crossValidation.CrossValidation}, otherwise will return
     * and empty list.
     *
     * @return a list of samples for validation
     */
    public ArrayList<DataSample> getValidationSamples() {
        return this.data.getValidationData();
    }

    /**
     * Gets a map containing the samples for each class of the data set.
     *
     * @return a HashMap with the samples for each class of the data set.
     */
    public HashMap<Double, ArrayList<DataSample>> getDataClasses() {
        return this.data.getDataClasses();
    }

    /**
     * Merges all the data samples into one matrix
     *
     * @return a matrix
     */
    public Matrix mergeData() {
        if (!this.data.getDataSamples().isEmpty()) {
            Matrix[] m = new Matrix[this.data.getDataSamples().size()];
            for (int i = 0; i < this.data.getDataSamples().size(); i++) {
                m[i] = data.getDataSamples().get(i).getData();
            }
            return Matrix.merge(m);
        } else {
            LOGGER.error("No data set can be found.");
        }
        return null;
    }

    /**
     * Merges all the data samples encoded into spike trains into one spike
     * train matrix
     *
     * @return a matrix
     */
    public Matrix mergeSpikeData() {
        if (!this.data.getDataSamples().isEmpty()) {
            Matrix[] m = new Matrix[this.data.getDataSamples().size()];
            for (int i = 0; i < this.data.getDataSamples().size(); i++) {
                m[i] = data.getDataSamples().get(i).getSpikeData();
            }
            return Matrix.merge(m);
        } else {
            LOGGER.error("No data set was found.");
        }
        return null;
    }

    /**
     * This function saves the spike train of a data sample previously encoded
     * in a coma separated file.
     *
     * @param fileName the file to
     * @param sampleId the index of the sample
     */
    public void saveSampleSpikeTrains(String fileName, int sampleId) {
        DataSample sample = this.getDataSample(sampleId);
        if (sample.isEncoded()) {
            this.getDataSample(sampleId).getSpikeData().export(fileName, ",");
        } else {
            throw new NeuCubeRuntimeException("The sample should be encoded into spike trains.");
        }
    }

    /**
     * Splits the data into training and testing sets.
     */
    public void splitData() {
        this.splitData(new RatioDataSplitter());
        //this.splitData(this.data.getRateForTraining());
    }

//    /**
//     * This function splits the data set into training (for cross-validation)
//     * and testing data set (for evaluation of the model) given a rate for
//     * training. 1) it clears the training and validations sets. 2) selects
//     * balanced random samples for the testing set. 3) Set the training set with
//     * the remaining samples. 4) shuffle the training data.
//     *
//     * @param rateForTraining the rate of samples for the training set
//     */
//    public void splitData(double rateForTraining) {
//        splitData(new RatioDataSplitter());
//        
////        this.data.getTrainingData().clear();
////        this.data.getValidationData().clear();
////        this.data.getValidationData().addAll(this.getRandomDataSamples(1 - rateForTraining));
////        this.data.getTrainingData().addAll(this.getDataSamples());
////        this.data.getTrainingData().removeAll(this.data.getValidationData());
////        Collections.shuffle(this.data.getTrainingData());
//    }
    
    public void splitData(DataSplitter dataSplitter){
        dataSplitter.split(this);
    }

    /**
     * Executes the encoding method to transform raw data into spike trains.
     *
     * @param algorithm the encoding algorithm
     * @param data the data to encode
     * @return
     */
    public boolean runEncoder(EncodingAlgorithm algorithm, ArrayList<DataSample> data) {
        if (!this.getDataClasses().isEmpty()) {
            algorithm.setEncodingSatus(false);
            algorithm.encode(data, data, 0, this.data.getNumRecords());
            this.data.setEncoded(true);
            return true;
        } else {
            LOGGER.error(Messages.DATA_SET_EMPTY.toString());
        }
        return false;
    }
}
