/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.tasks;

import java.io.File;
import java.io.IOException;
import jneucube.cube.NeuCubeController;
import jneucube.util.ConfusionMatrix;
import jneucube.util.Matrix;

/**
 *
 * @author em9403
 */
public class RecallTask {

    private boolean encodedData = false;

    public void run(String dir, String dataDirectory, String modelName) throws IOException {
        // 1 Creating a new project
        NeuCubeController project = new NeuCubeController();
        project.createProject(dir);

        // 2 Loading a trained NeuCube model
        project.loadNeuCube(modelName);

        project.setNetworkLastState();

        project.getDataController().removeData();
        project.getDataController().getData().setEncoded(this.isEncodedData());        
        project.loadSpatioTemporalData(dataDirectory);
        if(!this.isEncodedData()){
            project.runEncoder();
        }
        

        Matrix predictedLabels = project.recallDataSet(project.getDataController().getDataSamples());
        predictedLabels.show();
        Matrix currentLabels = new Matrix(dataDirectory + File.separator + "tar_class.csv", ",");

        ConfusionMatrix cmTest = new ConfusionMatrix(currentLabels.getVecCol(0), predictedLabels.getVecCol(0));
        cmTest.getErrorMatrix().export(dataDirectory + File.separator + "NeuCube_Recall_Error_Matrix.csv", ",");
        cmTest.export(dataDirectory + File.separator + "NeuCube_Recall_ConfusionMatrix.csv", ",");
        cmTest.showMetrics();
        cmTest.exportMetrics(dataDirectory + File.separator + "NeuCube_Recall_Metrics.txt");

//        // 3 Data classification
//        // 3.1 Single sample
//        System.out.println("Sample 53");
//        // 3.1.1 From the data set. Note that in the reallity the sample is not part of the data set, so it needs to be encoded.
//        System.out.println(project.recallSample(project.getDataController().getDataSample(52), false));
//        System.out.println(project.recallSampleProbabilities(project.getDataController().getDataSample(52), false));
//        // 3.1.2 From a file.  
//        System.out.println(project.recallSample("sam53_eeg.csv", false));
//        System.out.println(project.recallSampleProbabilities("sam49_eeg.csv", false));
//        // 3.2 Set of samples
//        Matrix labels = project.recallDataSet(project.getDataController().getDataSamples(0, 20));
//        labels.show();
    }

    /**
     * @return the encodedData
     */
    public boolean isEncodedData() {
        return encodedData;
    }

    /**
     * @param encodedData the encodedData to set
     */
    public void setEncodedData(boolean encodedData) {
        this.encodedData = encodedData;
    }

}
