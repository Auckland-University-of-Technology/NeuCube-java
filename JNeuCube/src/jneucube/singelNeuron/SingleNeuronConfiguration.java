/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.singelNeuron;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;
import jneucube.classifiers.Classifier;
import static jneucube.classifiers.Classifier.KNN;
import jneucube.classifiers.KNN;
import jneucube.crossValidation.CrossValidation;
import jneucube.crossValidation.MonteCarlo;
import jneucube.data.DataController;
import jneucube.data.SpatioTemporalData;
import jneucube.distances.Distance;
import jneucube.tasks.Tasks;
import static jneucube.log.Log.LOGGER;
import jneucube.optimisation.evolutionStrategies.DifferentialEvolution;
import jneucube.spikingNeurons.SpikingNeuron;
import jneucube.spikingNeurons.cores.Core;
import jneucube.spikingNeurons.cores.CoreConstants;
import jneucube.util.Messages;
import jneucube.util.Util;

/**
 *
 * @author em9403
 */
public class SingleNeuronConfiguration {

    private Properties properties = new Properties();

    public void configure() {
        SpatioTemporalData data = this.configureSpatioTemporalData();
        DataController dataController = this.configureDataController(data);
        SpikingNeuron spikingNeuron = this.configureSpikingNeuron();
        ClassificationError ff = configureFitnessFunction(spikingNeuron, data.getNumFeatures());
        CrossValidation crossValidation = this.configureCrossValidation();
        DifferentialEvolution de = this.configureDE();

        ff.setTrainingData(data.getTrainingData());
        ff.setTestingData(data.getValidationData());
        ff.setCrossValidation(crossValidation);
        de.setFitnessFunction(ff);

    }

    public void setProperties(String propertiesFileName) {
        if (!loadProperties(new File(propertiesFileName))) {
            System.exit(0);
        }
        if (!this.validateProperties()) {
            System.exit(0);
        }
    }

    public boolean loadProperties(File propertiesFile) {
        try (InputStream inputStream = new FileInputStream(propertiesFile)) {
            if (inputStream != null) {
                this.properties.load(inputStream);
                return true;
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return false;
        }
        return false;
    }

    public SpatioTemporalData configureSpatioTemporalData() {
        LOGGER.info("Configuring spatio-temporal dataset");
        String strDataDir = this.properties.getProperty("strDataDir");
        int numDataFeatures = Integer.parseInt(this.properties.getProperty("numDataFeatures"));
        double rateForTraining = Double.parseDouble(this.properties.getProperty("rateForTraining"));

        SpatioTemporalData SSTD = new SpatioTemporalData();
        SSTD.setNumVariables(numDataFeatures);
        SSTD.setRateForTraining(rateForTraining);
        SSTD.setDirectory(strDataDir);
        SSTD.setEncoded(false);
        SSTD.setOffLine(true);
        LOGGER.info("Complete");
        return SSTD;
    }

    public DataController configureDataController(SpatioTemporalData data) {
        DataController dataController = new DataController();
        dataController.setData(data);
        String strClassFileName = this.properties.getProperty("stdTargetClassLabelFile");
        File path = new File(data.getDirectory());
        File classFile = new File(path + File.separator + strClassFileName);
        if (!dataController.loadData(path, classFile, Tasks.CLASSIFICATION, Messages.NO_MESSAGE)) {
            System.out.println("Error loading the data set");
        }
        
        dataController.splitData();

        return dataController;
    }

    public DifferentialEvolution configureDE() {
        int maxGenerations = Integer.parseInt(this.properties.getProperty("maxGenerations"));
        double crossoverProbability = Double.parseDouble(this.properties.getProperty("crossoverProbability"));
        double weightingFactor = Double.parseDouble(this.properties.getProperty("weightingFactor"));
        int populationSize = Integer.parseInt(this.properties.getProperty("populationSize"));
        String strOutputDir = this.properties.getProperty("strOutputDir");
        String iterationFilePrefix = this.properties.getProperty("iterationFilePrefix");

        DifferentialEvolution de = new DifferentialEvolution();
        de.setMaxGenerations(maxGenerations);
        de.setCrossoverProbability(crossoverProbability);
        de.setWeightingFactor(weightingFactor);
        de.setPopulationSize(populationSize);
        de.setFilePopulationMatrix(strOutputDir + File.separator + iterationFilePrefix);
        return de;
    }

    public ClassificationError configureFitnessFunction(SpikingNeuron neuron, int numFeatures) {
        ClassificationError ce = new ClassificationError();
        double[][] neuronRanges = (double[][]) neuron.getCore().getUserData("OPTIMISATION_PARAMETRES");
        int numNeuronParametres = neuronRanges.length; // 2 that corresponds to LIF threshold value and LIF refractory time or 1 that corresponds to Izhikevich num behaviours 1-27
        ce.setDimensionality(numFeatures + numNeuronParametres);  

        ce.setSpikingNeuron(neuron);
        double[][] rangeValues = new double[numFeatures + numNeuronParametres][2]; // 2 columns min and max values 
        for (int i = 0; i < numFeatures; i++) {
            rangeValues[i][0] = -0.01;
            rangeValues[i][1] = 0.01;
        }
        for (int i = 0; i < neuronRanges.length; i++) {
            rangeValues[numFeatures + i][0] = neuronRanges[i][0];
            rangeValues[numFeatures + i][1] = neuronRanges[i][1];
        }
        ce.setRangeValues(rangeValues);

        String strInitialIndividual = properties.getProperty("initialIndividual");
        if (strInitialIndividual == null) {
            strInitialIndividual = "";
        }
        if (!strInitialIndividual.isEmpty()) {
            String[] temp = strInitialIndividual.split(",");
            double[] initialIndividual = Arrays.stream(temp)
                    .mapToDouble(Double::parseDouble)
                    .toArray();
            ce.setInitialIndividual(initialIndividual);
        }        
        return ce;
    }

    public SpikingNeuron configureSpikingNeuron() {
        Core core = this.configureCore();
        SpikingNeuron spikingNeuron = new SpikingNeuron(core);
        return spikingNeuron;
    }

    /**
     * Set the core of the spiking neuron and defines the parameters to optimise.
     * 
     * @return the core.
     */
    public Core configureCore() {
        String coreName = this.properties.getProperty("coreName");
        LOGGER.info("Configuring " + coreName + " spiking neuron core");
        Core core;
        double[][] parametres;
        switch (coreName.toUpperCase()) {
            case "LIF": {
                core = CoreConstants.LIF;
                LOGGER.info("LIF th=[0.0,0.5]; rt=[1.0, 10.0]");
                parametres = new double[][]{{0.01, 1.0}, {1.0, 10.0}};   // LIF threshold, LIF refractory time
            }
            break;
            case "IZHIKEVICH": {
                core = CoreConstants.IZHIKEVICH;                
                LOGGER.info("IZHIKEVICH behaviours=[1,"+CoreConstants.IZHIKEVICH.getBehaviors().length()+"]");
                parametres = new double[][]{{1.0, CoreConstants.IZHIKEVICH.getBehaviors().length()}};   // Izhikevich behaviour
            }
            break;
            default: {
                LOGGER.info("The core "+coreName+" is not available for this task. The LIF neuron will be used.");
                core = CoreConstants.LIF;
                LOGGER.info("LIF th=[0.0,0.5]; rt=[1.0, 10.0]");
                //LOGGER.info("LIF th=[0.01,1.0]; rt=[1.0, 10.0]");
                parametres = new double[][]{{0.01, 1.0}, {1.0, 10.0}};   // LIF threshold, LIF refractory time
            }
        }
        core.setUserData("OPTIMISATION_PARAMETRES", parametres);
        return core;
    }

    public CrossValidation configureCrossValidation() {
        String crossValidationMethodName = this.properties.getProperty("crossValidationMethodName");
        LOGGER.info("Configuring " + crossValidationMethodName + " cross validation method");
        CrossValidation method;
        switch (crossValidationMethodName.toUpperCase()) {
            case "K_FOLD": {
                CrossValidation.K_FOLD.setNumFolds(Integer.parseInt(this.properties.getProperty("numKfolds")));
                method = CrossValidation.K_FOLD;
            }
            break;
            case "MONTE_CARLO": {
                CrossValidation.MONTE_CARLO.setTrainingRate(Double.parseDouble(this.properties.getProperty("trainingRate")));
                CrossValidation.MONTE_CARLO.setNumExperiments(Integer.parseInt(this.properties.getProperty("numExperiments")));
                if (this.properties.getProperty("monteCarloType").equals("RANDOM")) {
                    CrossValidation.MONTE_CARLO.setType(MonteCarlo.RANDOM);
                } else {
                    CrossValidation.MONTE_CARLO.setType(MonteCarlo.SEQUENTIAL);
                }
                method = CrossValidation.MONTE_CARLO;
            }
            break;
            default: {
                CrossValidation.K_FOLD.setNumFolds(5);
                method = CrossValidation.K_FOLD;
            }
        }
        LOGGER.info("Cross validation method configuration complete");
        return method;
    }

    /**
     * @return the properties
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * @param properties the properties to set
     */
    public void setProperties(Properties properties) {
        this.properties = properties;
//        if (!this.validateProperties()) {
//            System.exit(0);
//        }
    }

    public boolean validateProperties() {
        String strDataDir = this.properties.getProperty("strDataDir");
        File dataDir = new File(strDataDir);
        if (!dataDir.isDirectory()) {
            LOGGER.error("No data directory was found: "+strDataDir);
            return false;
        }

        String stdTargetClassLabelFile = strDataDir + File.separator + this.properties.getProperty("stdTargetClassLabelFile");
        File classFile = new File(stdTargetClassLabelFile);
        if (!classFile.isFile()) {
            LOGGER.error("No target class label file was found: "+stdTargetClassLabelFile);
            return false;
        }

        String strOutputDir = this.properties.getProperty("strOutputDir");
        File outputDir = new File(strOutputDir);
        try {
            outputDir.mkdir();
        } catch (SecurityException se) {
            LOGGER.error("No output directory was found: "+strOutputDir);
            return false;
        }

        if (!Util.isInteger(this.properties.getProperty("numDataFeatures"), 1, Integer.MAX_VALUE)) {
            LOGGER.error("Invalid value for the numDataFeatures parametre");
            return false;
        }

        if (!Util.isDouble(this.properties.getProperty("rateForTraining"), 0.0, 1.0)) {
            LOGGER.error("Invalid value for the rateForTraining parametres (0,1.0]");
            return false;
        }
        String coreName = this.properties.getProperty("coreName");
        if (!coreName.toUpperCase().equals("LIF") && !coreName.toUpperCase().equals("IZHIKEVICH")) {
            LOGGER.error("Invalid value for the coreName parametre. Valid values={LIF,IZHIKEVICH}");
            return false;
        }
        if (!this.properties.getProperty("fitnessFunction").toUpperCase().equals("CLASSIFICATION_ERROR")) {
            LOGGER.error("Invalid value for the fitnessFunction parametres. Valid values={CLASSIFICATION_ERROR}");
            return false;
        }
        String strInitialIndividual = this.properties.getProperty("initialIndividual");
        if (strInitialIndividual == null) {
            this.properties.setProperty("initialIndividual", "");
        }

        String crossValidationMethodName = this.properties.getProperty("crossValidationMethodName");
        if (!crossValidationMethodName.toUpperCase().equals("K_FOLD")) {
            LOGGER.error("Invalid value for the crossValidationMethodName parametres. Valid values={K_FOLD}");
            return false;
        }
        if (!Util.isInteger(this.properties.getProperty("numKfolds"), 5, 10)) {
            LOGGER.error("Invalid value for the numKfolds parametre. Valid values 5<=k<=10.");
            return false;
        }
        if (!Util.isInteger(this.properties.getProperty("maxGenerations"), 1, Integer.MAX_VALUE)) {
            LOGGER.error("Invalid value for the maxGenerations parametre.");
            return false;
        }
        if (!Util.isDouble(this.properties.getProperty("crossoverProbability"), 0.0, 1.0)) {
            LOGGER.error("Invalid value for the crossoverProbability parametre. Valid values x=(0,1.0).");
            return false;
        }
        if (!Util.isDouble(this.properties.getProperty("weightingFactor"), 0.0, 1.0)) {
            LOGGER.error("Invalid value for the weightingFactor parametre. Valid vlues x=(0,1.0).");
            return false;
        }

        if (!Util.isInteger(this.properties.getProperty("populationSize"))) {
            LOGGER.error("Invalid number for the populationSize parametre.");
            return false;
        }

        if (!Util.isInteger(properties.getProperty("iteration"), 0, Integer.MAX_VALUE)) {
            LOGGER.error("Invalid number for the iteration parametre.");
            return false;
        }else{
            int iteration = Integer.parseInt(properties.getProperty("iteration"));
            if (iteration > 0) {
                String populationFileName = strOutputDir+File.separator+properties.getProperty("populationFileName");
                File populationFile = new File(populationFileName);
                if (!populationFile.isFile()) {
                    LOGGER.error("No population file was found: "+populationFileName);
                    return false;
                }
            }
        }
        return true;
    }

}
