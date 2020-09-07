/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.singelNeuron;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.stream.Stream;
import jneucube.crossValidation.CrossValidation;
import jneucube.data.DataController;
import jneucube.data.DataSample;
import jneucube.data.SpatioTemporalData;
import jneucube.optimisation.evolutionStrategies.DifferentialEvolution;
import jneucube.spikingNeurons.SpikingNeuron;
import jneucube.spikingNeurons.Synapse;
import jneucube.util.Matrix;
import jneucube.util.Util;

/**
 *
 * @author em9403
 */
public class SingleNeuronClassificationTask {

    public void run(Properties properties) throws IOException {
        SingleNeuronConfiguration config = new SingleNeuronConfiguration();
        config.setProperties(properties);
        SpatioTemporalData data = config.configureSpatioTemporalData();
        DataController dataController = config.configureDataController(data);
        SpikingNeuron spikingNeuron = config.configureSpikingNeuron();
        ClassificationError ff = config.configureFitnessFunction(spikingNeuron, data.getNumFeatures());
        CrossValidation crossValidation = config.configureCrossValidation();
        DifferentialEvolution de = config.configureDE();
        String strResultsPath = config.getProperties().getProperty("strOutputDir");
        
        HashMap<Double, ArrayList<DataSample>> mapDataClasses = dataController.detectClasses(dataController.getTrainingSamples());
        crossValidation.clear();
        crossValidation.split(mapDataClasses);
        
        ff.setTrainingData(data.getTrainingData());
        ff.setTestingData(data.getValidationData());
        ff.setCrossValidation(crossValidation);
        de.setFitnessFunction(ff);
        this.createConnections(spikingNeuron, data.getNumFeatures());

        int iteration = Integer.parseInt(properties.getProperty("iteration"));
        if (iteration == 0) {
            de.run();
        } else {
            String strPopulationFileName = properties.getProperty("populationFileName");
            de.run(strResultsPath + File.separator+strPopulationFileName, iteration);
        }

        System.out.println("Best individual");
        Util.printHorzArray(de.getBestIndividual());
        System.out.println(de.getBestIndividualFitness());

        // Evaluation of the best individual
        
        double fitness = ff.evaluateIndividual(de.getBestIndividualIndex(), de.getBestIndividual());
        // Cross-Validation results
        ff.getCrossValidationConfusionMatrix().export(strResultsPath + File.separator + "CrossValidationConfusionMatrix.csv", ",");
        ff.getCrossValidationConfusionMatrix().getErrorMatrix().export(strResultsPath + File.separator + "CrossValidationErrorMatrix.csv", ",");
        Util.saveStringToFile(ff.getCrossValidationConfusionMatrix().getMetricsToString(), strResultsPath + File.separator + "CrossValidationMetrics.txt");
        // Testing results
        ff.getTestConfusionMatrix().export(strResultsPath + File.separator + "TestConfusionMatrix.csv", ",");
        ff.getTestConfusionMatrix().getErrorMatrix().export(strResultsPath + File.separator + "TestErrorMatrix.csv", ",");
        Util.saveStringToFile(ff.getTestConfusionMatrix().getMetricsToString(), strResultsPath + File.separator + "TestMetrics.txt");

        Matrix errors = Matrix.vertcat(ff.getCrossValidationConfusionMatrix().getErrorMatrix(), ff.getTestConfusionMatrix().getErrorMatrix());
        errors.export(strResultsPath + File.separator + "ErrorMatrix.csv", ",");

        StringBuffer sb = new StringBuffer();
        sb.append(ff.getFiringAverages().toString());
        Util.saveStringToFile(sb, strResultsPath + File.separator + "FiringRates.txt");

        // For plotting the firing activity
        ff.updateWeights(de.getBestIndividual());
        ArrayList<DataSample> samples = dataController.getDataSamples();
        Matrix firingActivityMatrix = new Matrix();
        double[][] firingActivity = new double[samples.size()][];
        double[] firings;
        for (int i = 0; i < samples.size(); i++) {
            ff.propagateSample(samples.get(i));
            Double[] boxed = ff.getSpikingNeuron().getFirings().stream().toArray(Double[]::new);
            if (boxed.length != 0) {
                firings = Stream.of(boxed).mapToDouble(Double::doubleValue).toArray();
            } else {
                firings = new double[1];
            }
            firingActivity[i] = firings;
        }
        firingActivityMatrix.setData(firingActivity);
        firingActivityMatrix.export(strResultsPath + File.separator +"firingActivity.csv", ",");
    }

    public void run(String propertiesFileName) throws IOException {
        SingleNeuronConfiguration config = new SingleNeuronConfiguration();
        if (!config.loadProperties(new File(propertiesFileName))) {
            System.exit(0);
        }
        this.run(config.getProperties());
    }

    public void createConnections(SpikingNeuron spikingNeuron, int numFeatures) {
        for (int i = 0; i < numFeatures; i++) {
            spikingNeuron.getInputSynapses().add(new Synapse());
        }
    }
    
    public void testFitnessFunction(Properties properties){
        SingleNeuronConfiguration config = new SingleNeuronConfiguration();
        config.setProperties(properties);
        SpatioTemporalData data = config.configureSpatioTemporalData();
        DataController dataController = config.configureDataController(data);
        SpikingNeuron spikingNeuron = config.configureSpikingNeuron();
        //ClassificationError ff = config.configureFitnessFunction(spikingNeuron, data.getNumFeatures());
        SingleNeuronClassificationError ff = new SingleNeuronClassificationError();
                
        CrossValidation crossValidation = config.configureCrossValidation();

        String strResultsPath = config.getProperties().getProperty("strOutputDir");
        
        HashMap<Double, ArrayList<DataSample>> mapDataClasses = dataController.detectClasses(dataController.getTrainingSamples());
        crossValidation.clear();
        crossValidation.split(mapDataClasses);
        
        this.createConnections(spikingNeuron, data.getNumFeatures());
        
        ff.setTrainingData(data.getTrainingData());
        ff.setTestingData(data.getValidationData());
        ff.setCrossValidation(crossValidation);
        ff.setSpikingNeuron(spikingNeuron);
        
        //double[] individual=new double[]{-0.00193802, 0.004614143, -0.007015714, -2.63E-04, 7.81E-04, 0.01118112, 0.01238623, 0.011415881, 0.004479885, -0.014240987, -0.004260465, 0.003023273, -0.008946983, -0.007929055, 1.252365896, 2.633692058};
        double[] individual=new double[]{-0.011539654, 0.006363787, -0.011497017, -0.004518722, -0.001071649, 0.014183166, 0.024148439, 0.01230449, -0.001372377, -0.00594475, -0.008669288, 0.001056347, -0.01273277, 4.03E-04, 5.0, 1.930461588};
        double fitness=ff.evaluateIndividual(0, individual);
        System.out.println("Fitness "+fitness);
        
    }

   
}
