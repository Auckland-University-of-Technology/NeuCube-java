/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.tasks;

import jneucube.cube.NeuCubeController;

/**
 *
 * @author em9403
 */
public class RegressionTask {

    public void run(String dir, String propertiesFile, String initialNeuCubeFile, String finalNeuCubeFile) {
        NeuCubeController project = new NeuCubeController();
        project.createProject(dir);

        // 2 Configuration of the SNN using a properties file.
        project.configureNeuCube(propertiesFile);

        // 3 Loading and encoding a data set.                 
        // In the case of huge data (data time points and samples), it is recommended to load the spatio-temporal data after saving the initialised NeuCube
        project.loadSpatioTemporalData();

        // 4 Initialising the SNN (Loads the reservoir and input neuron coordinates, and creates the connections).
        project.initializeNetwork();
        project.saveNeucube(initialNeuCubeFile);

        project.setRecordFiringActivity(false);
        project.runCrossvalidation();
        
        project.saveNeucube(finalNeuCubeFile);
        // 7 Showing and saving regression results
        project.showRegressionMatrix();
        project.exportRegressionMatrix("regression_result.csv");
        System.out.println("RMSE");
        project.showRegressionRMSE();        
        project.exportRMSE("regression_rmse.txt");
        
    }
}
