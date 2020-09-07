/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.analyticTools;

import java.util.ArrayList;
import java.util.Collections;
import jneucube.cube.NeuCubeController;
import jneucube.data.DataSample;
import static jneucube.log.Log.LOGGER;
import jneucube.util.Matrix;
import jneucube.util.NeuCubeRuntimeException;
import jneucube.util.Util;

/**
 * The class {@code Pruner} is a tool for removing the inacive neurons and
 * weights when specific data is feeded to the spiking neurl network.
 *
 * @author Dr. Josafath Israel Espinosa Ramos
 */
public class Pruner {

    public Pruner() {

    }

    /**
     * The {@code pruneInactiveNeurons} removes the inactive neurons and
     * connections of a trained NeuCube model. This process should be executed
     * if the model achieved good generalisation and if it is intended to put it
     * in a production environment. The function executes the following steps: 1
     * Creates a new project. 2 Loads the NeuCube model that will be pruned. 3
     * Prune the spiking neurla network (reservoir). Set the variable
     * {@link jneucube.cube.NeuCubeController#setRecordFiringActivity(boolean)}
     * to true. Calls the
     * {@link jneucube.cube.NeuCubeController#pruneInactiveNeurons()} function.
     * 4 Saves relevant information for further analysis i.e. the firing
     * activity (NeuCube_Prunned_Firing_Activity.csv), saves the matrix of
     * synaptic weights (NeuCube_Prunned_Weight_Matrix.csv), saves the list of
     * synaptic weights (NeuCube_Prunned_Weights.csv), and saves the pruned
     * NeuCube model .
     *
     * @param dir The directory that contains the properties file and the XML
     * file.
     * @param neuCubeFile The XML file that contains the structure of the
     * NeuCube.
     * @param prunnedNeuCubeFile The XML file that will contain the structure of
     * the trained NeuCube.
     */
    public void pruneInactiveNeurons(String dir, String neuCubeFile, String prunnedNeuCubeFile) {
        // 1 Create the project
        NeuCubeController project = new NeuCubeController();
        project.createProject(dir);
        // 2 Loading a trained NeuCube model
        project.loadNeuCube(neuCubeFile);
        // 3 Pruning innactive neurons
        project.setNetworkLastState();
        project.setRecordFiringActivity(true);
        project.pruneInactiveNeurons();
        // 4 Saving information for analysis
        project.exportFiringActivity("NeuCube_Pruned_Firing_Activity.csv");
        project.exportCurrentWeights("NeuCube_Pruned_Weights.csv");
        project.exportCurrentWeightMatrix("NeuCube_Pruned_Weight_Matrix.csv");
        project.saveNeucube(prunnedNeuCubeFile);
    }

    /**
     * The {@code pruneInactiveNeuronsByClass} function removes the inactive
     * neurons and connections of a NeuCube model when it is stimulated with
     * specific channels of the samples that belongs to a set of classes. The
     * XML file that contains the NeuCube model structure must also contain the
     * encoded data. It is recommended to execute this function only if the
     * model achieved good generalisation. The function executes the following
     * steps: 1 Creates a new project. 2 Loads the NeuCube model that will be
     * pruned. 3 Selects the samples that belongs to the classes indicated. 4
     * Encodes the data if needed. 5 Set to zero all the data points of the
     * features that do not belong to the specified features. 6 If the parameter
     * train is set to true then the function fits the model with the selected
     * samples. 7 Prunes the SNN by propagating the spike trains of the selected
     * samples. Sets the variable
     * {@link jneucube.cube.NeuCubeController#setRecordFiringActivity(boolean)}
     * to true. Then, it calls the
     * {@link jneucube.cube.NeuCubeController#pruneInactiveNeurons(double)}
     * function. 8 Saves relevant information for further analysis i.e. the
     * firing activity
     * (NeuCube_Pruned_Firing_Activity_Classes_[strClasses]_Features_[strFeatrues].csv),
     * the matrix of synaptic weights
     * (NeuCube_Pruned_Weights_Classes_[strClasses]_Features_[strFeatrues].csv),
     * the list of synaptic weights
     * (NeuCube_Pruned_Weight_Matrix_Classes_[strClasses]_Features_[strFeatrues].csv),
     * and finally, the structure of the pruned NeuCube model
     * (prunnedNeuCubeFile).
     *
     * @param dir The directory that contains the properties file and the XML
     * file.
     * @param neuCubeFile The XML file that contains the structure of the
     * NeuCube (trained or not).
     * @param prunnedNeuCubeFile The XML file that will contain the structure of
     * the pruned NeuCube (trained or not).
     * @param strClasses The string that contains the classes in a cron-like
     * expression, like <code>1,3-6,8</code>. The values should be in the range
     * of the number of classes. The range of values are inclusive, e.g. in the
     * range 6-9, both the 6th and 9th classes are included in the selection.
     *
     * @param strFeatures The string that contains the features in a cron-like
     * expression, like <code>1,3-6,8</code>. The values should be in the range
     * of the number of classes. The range of values are inclusive, e.g. in the
     * range 6-9, both the 6th and 9th classes are included in the selection.
     * are inclusive.
     * @param train Indicates whether the model should be trained (true) or not
     * (false).
     */
    public void pruneInactiveNeuronsByClass(String dir, String neuCubeFile, String prunnedNeuCubeFile, String strClasses, String strFeatures, boolean train) {
        LOGGER.info("Pruning the SNN using samples of classes " + strClasses + " and features " + strFeatures);
        ArrayList<Integer> classList = Util.numberExpression(strClasses);
        ArrayList<Integer> featureList = Util.numberExpression(strFeatures);
        Collections.sort(classList);
        Collections.sort(featureList);

        // 1 Create the project
        NeuCubeController project = new NeuCubeController();
        project.createProject(dir);

        // 2 Loading a trained NeuCube model
        project.loadNeuCube(neuCubeFile);

        if (project.getDataController().getData().getDataSamples().isEmpty()) {
            project.loadSpatioTemporalData(project.getDataController().getData().getDirectory());
        }

        // 3 Select the samples that correspond to the specified classes
        ArrayList<DataSample> dataset = new ArrayList<>();
        for (int i = 0; i < classList.size(); i++) {
            dataset.addAll(project.getDataController().getDataClasses().get(new Double(classList.get(i))));
        }

        // 4 Encode the data
        if (!project.getDataController().getData().isEncoded()) {
            project.runEncoder(dataset);
        }

        // 5 Modify the channels
        for (Integer feature : featureList) {
            if (feature < 1 || feature > project.getDataController().getData().getNumFeatures()) {
                throw new NeuCubeRuntimeException("Faature " + feature + " is not a valid one.");
            }
        }
//        // For visualisation of the selected features in the NeuCubeFX (causes erors)
//        Matrix selectedFeatures = new Matrix(project.getDataController().getData().getNumFeatures(), 1, 0.0);
//        for (int i = 0; i < featureList.size(); i++) {
//            selectedFeatures.set(featureList.get(i) - 1, 0, 1.0);
//        }
//        project.getDataController().getData().setSelectedFeatures(selectedFeatures);

        for (DataSample sample : dataset) {
            Matrix spikeData = sample.getSpikeData();
            for (int col = 0; col < spikeData.getCols(); col++) {
                if (!featureList.contains(col + 1)) {
                    spikeData.setCol(col, 0.0);
                }
            }
        }

        // 6 Fitting the model using the selected data
        project.setNetworkLastState();
        if (train) {
            project.fitModel(dataset);
        }

        // 7 Prunning the model using the selected data
        project.setRecordFiringActivity(true);
        project.pruneInactiveNeurons(dataset, false, false);

        // 8 Saving relevant information
        project.exportFiringActivity("NeuCube_Pruned_Firing_Activity_Classes_" + strClasses + "_Features_" + strFeatures + ".csv");
        project.exportCurrentWeights("NeuCube_Pruned_Weights_Classes_" + strClasses + "_Features_" + strFeatures + ".csv");
        project.exportCurrentWeightMatrix("NeuCube_Pruned_Weight_Matrix_Classes_" + strClasses + "_Features_" + strFeatures + ".csv");
        project.saveNeucube(prunnedNeuCubeFile);
        LOGGER.info("Pruning complete");
    }

    /**
     * The {@code pruneInactiveNeuronsBySamples} function removes the inactive
     * neurons and connections of a NeuCube model when it is stimulated with
     * specific channels of the samples that belongs to a set of classes. The
     * XML file that contains the NeuCube model structure must also contain the
     * encoded data. It is recommended to execute this function only if the
     * model achieved good generalisation. The function executes the following
     * steps: 1 Creates a new project. 2 Loads the NeuCube model that will be
     * pruned. 3 Selects the specified samples. 4 Encodes the data if needed. 5
     * Set to zero all the data points of the features that do not belong to the
     * specified features. 6 If the parameter train is set to true then the
     * function fits the model with the selected samples. 7 Prunes the SNN by
     * propagating the spike trains of the selected samples. Sets the variable
     * {@link jneucube.cube.NeuCubeController#setRecordFiringActivity(boolean)}
     * to true. Then, it calls the
     * {@link jneucube.cube.NeuCubeController#pruneInactiveNeurons(double)}
     * function. 8 Saves relevant information for further analysis i.e. the
     * firing activity
     * (NeuCube_Pruned_Firing_Activity_Samples_[strSamples]_Features_[strFeatrues].csv),
     * the matrix of synaptic weights
     * (NeuCube_Pruned_Weights_Samples_[strSamples]_Features_[strFeatrues].csv),
     * the list of synaptic weights
     * (NeuCube_Pruned_Weight_Matrix_Samples_[strSamples]_Features_[strFeatrues].csv),
     * and finally, the structure of the pruned NeuCube model
     * (prunnedNeuCubeFile).
     *
     * @param dir The directory that contains the properties file and the XML
     * file.
     * @param neuCubeFile The XML file that contains the structure of the
     * NeuCube (trained or not).
     * @param prunnedNeuCubeFile The XML file that will contain the structure of
     * the pruned NeuCube (trained or not).
     * @param strSamples The string that contains the samples in a cron-like
     * expression, like <code>1,3-6,8</code>. The values should be in the range
     * of the dataset size. The range of values are inclusive, e.g. in the range
     * 6-9, both the 6th and 9th samples are included in the selection.
     *
     * @param strFeatures The string that contains the features in a cron-like
     * expression, like <code>1,3-6,8</code>. The values should be in the range
     * of the number of classes. The range of values are inclusive, e.g. in the
     * range 6-9, both the 6th and 9th classes are included in the selection.
     * are inclusive.
     * @param train Indicates whether the model should be trained (true) or not
     * (false).
     */
    public void pruneInactiveNeuronsBySamples(String dir, String neuCubeFile, String prunnedNeuCubeFile, String strSamples, String strFeatures, boolean train) {
        LOGGER.info("Pruning the SNN using samples " + strSamples + " and features " + strFeatures);
        ArrayList<Integer> sampleList = Util.numberExpression(strSamples);
        ArrayList<Integer> featureList = Util.numberExpression(strFeatures);
        Collections.sort(sampleList);
        Collections.sort(featureList);

        // 1 Create the project
        NeuCubeController project = new NeuCubeController();
        project.createProject(dir);
        
        // 2 Loading a trained NeuCube model
        project.loadNeuCube(neuCubeFile);
        

        if (project.getDataController().getData().getDataSamples().isEmpty()) {
            project.loadSpatioTemporalData(project.getDataController().getData().getDirectory());
        }

        // 3 Select the samples that correspond to the specified samples
        ArrayList<DataSample> dataset = new ArrayList<>();
        for (int i = 0; i < sampleList.size(); i++) {
            dataset.add(project.getDataController().getDataSample(sampleList.get(i) - 1));
        }

        // 4 Encode the data
        if (!project.getDataController().getData().isEncoded()) {
            project.runEncoder(dataset);
        }
        // 5 Modify the channels        
        for (Integer feature : featureList) {
            if (feature < 1 || feature > project.getDataController().getData().getNumFeatures()) {
                throw new NeuCubeRuntimeException("Faature " + feature + " is not a valid one.");
            }
        }

//        // For visualisation of the selected features in the NeuCubeFX (causes erors)
//        Matrix selectedFeatures = new Matrix(project.getDataController().getData().getNumFeatures(), 1, 0.0);
//        for (int i = 0; i < featureList.size(); i++) {
//            selectedFeatures.set(featureList.get(i) - 1, 0, 1.0);
//        }
//        project.getDataController().getData().setSelectedFeatures(selectedFeatures);
        for (DataSample sample : dataset) {
            Matrix spikeData = sample.getSpikeData();
            for (int col = 0; col < spikeData.getCols(); col++) {
                if (!featureList.contains(col + 1)) {
                    spikeData.setCol(col, 0.0);
                }
            }
        }

        // 6 Fitting the model using the selected data
        project.setNetworkLastState(); 
        if (train) {
            project.fitModel(dataset);
        }

        // 7 Prunning the model using the selected data        
        project.setRecordFiringActivity(true);
        project.pruneInactiveNeurons(dataset, false, false);

        // 8 Saving relevant information
        project.exportFiringActivity("NeuCube_Pruned_Firing_Activity_Samples_" + strSamples + "_Features_" + strFeatures + ".csv");
        project.exportCurrentWeights("NeuCube_Pruned_Weights_Samples_" + strSamples + "_Features_" + strFeatures + ".csv");
        project.exportCurrentWeightMatrix("NeuCube_Pruned_Weight_Matrix_Samples_" + strSamples + "_Features_" + strFeatures + ".csv");
        project.saveNeucube(prunnedNeuCubeFile);
        LOGGER.info("Pruning complete");
    }

    public Matrix getFunctionalWeightsMean(String dir, String neuCubeFile) {
        LOGGER.info("Calculating functional weights");
        NeuCubeController project = new NeuCubeController();
        project.createProject(dir);

        // 2 Loading a trained NeuCube model
        project.loadNeuCube(neuCubeFile);

        if (project.getDataController().getData().getDataSamples().isEmpty()) {
            project.loadSpatioTemporalData(project.getDataController().getData().getDirectory());
        }
        
        ArrayList<DataSample> projectDataset=project.getDataController().getData().getDataSamples();                
        if (!project.getDataController().getData().isEncoded()) {
            project.runEncoder(projectDataset);
        }
        
        int numSamples=project.getDataController().getData().getNumSamples();
        int numFeatures=project.getDataController().getData().getNumFeatures();

        ArrayList<DataSample> dataset=new ArrayList<>();
        DataSample sample;
        ArrayList<Double> functionalWeights;
        Matrix corr=new Matrix(numSamples, numFeatures,0.0);
        double mean;        
        for(int i=0;i<numSamples;i++){
            sample=projectDataset.get(i);            
            Matrix spikeDataBackup=sample.getSpikeData().get(0,sample.getNumRecords(), 0, sample.getNumFeatures());
            for(int j=0;j<numFeatures;j++){
                dataset.clear();                
                Matrix spikeTrains=new Matrix(spikeDataBackup.getRows(),spikeDataBackup.getCols(),0.0);
                spikeTrains.setCol(j, spikeDataBackup.getVecCol(j));
                sample.setSpikeData(spikeTrains);
                dataset.add(sample);   
                functionalWeights=project.getFunctionalWeights(dataset, false, false);
                double[] arr=functionalWeights.stream().mapToDouble(Double::doubleValue).toArray();
                if(arr.length==0){
                    mean=0.0;
                }else{
                    mean=Util.mean(arr);
                }                
                corr.set(i, j, mean);
            }
        }
        LOGGER.info("Process completed");        
        return corr;
    }
}
