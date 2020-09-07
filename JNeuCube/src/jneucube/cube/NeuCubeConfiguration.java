/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.cube;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import jneucube.classifiers.Classifier;
import jneucube.connectionAlgorithms.ConnectionAlgorithm;
import jneucube.crossValidation.CrossValidation;
import jneucube.crossValidation.MonteCarlo;
import jneucube.data.OnlineReader;
import jneucube.data.OnlineSeismicFileReader;
import jneucube.data.SpatioTemporalData;
import jneucube.tasks.Tasks;
import jneucube.distances.Distance;
import jneucube.encodingAlgorithms.EncodingAlgorithm;
import static jneucube.log.Log.LOGGER;
import jneucube.network.Network;
import jneucube.network.reservoirBuilders.ReservoirBuilder;
import jneucube.spikingNeurons.NeuronType;
import jneucube.spikingNeurons.SpikingNeuron;
import jneucube.spikingNeurons.cores.Core;
import jneucube.spikingNeurons.cores.CoreConstants;
import jneucube.trainingAlgorithms.LearningAlgorithm;
import jneucube.util.Util;

/**
 *
 * @author Josafath Israel Espinosa Ramos
 */
public class NeuCubeConfiguration {

    private Properties properties = new Properties();

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
    }

    public boolean loadProperties(String propertiesFileName) {
        return loadProperties(new File(propertiesFileName));
    }

    public boolean loadProperties(File propertiesFile) {
        try (InputStream inputStream = new FileInputStream(propertiesFile)) {
            if (inputStream != null) {
                this.properties.load(inputStream);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return false;
        }
        return true;
    }

    public boolean configureNeuCube(NeuCube neucube) {
        LOGGER.info("Configuring NeuCube for " + this.properties.getProperty("problemType"));
        if (properties.isEmpty()) {
            LOGGER.error("The properties file is empty.");
            return false;
        }

        int task = this.getTask(this.properties.getProperty("problemType"));
        neucube.setProblemType(task); // Some methods or algorithms depend on the type of task to be performed

        SpatioTemporalData STD = this.configureSpatioTemporalData();
        Network network = this.configureNetwork();
        ReservoirBuilder reservoirBuilder = this.configureReservoirBuilder(this.properties.getProperty("reservoirBuilder"));
        ConnectionAlgorithm connectionAlgorithm = this.configureConnectionAlgorithm(this.properties.getProperty("connectionAlgorithmName"));
        EncodingAlgorithm encodingAlgorithm=null;
        if(!STD.isEncoded()){
             encodingAlgorithm = this.configureEncodingAlgorithm(this.properties.getProperty("encodingAlgorithmName"));
        }        
        LearningAlgorithm unsupervisedLearningAlgorithm = this.configureUnsupervisedLearningAlgorithm(this.properties.getProperty("unsupervisedLearningAlgorithmName"));
        LearningAlgorithm supervisedLearningAlgorithm = this.configureSupervisedLearningAlgorithm(this.properties.getProperty("supervisedLearningAlgorithmName"));
        CrossValidation crossValidation = this.configureCrossValidation(this.properties.getProperty("crossValidationMethodName"));
        Classifier classifier = this.configureClassifier(this.properties.getProperty("classifierName"));

        neucube.setSSTD(STD);
        neucube.setNetwork(network);
        neucube.setConnectionAlgorithm(connectionAlgorithm);
        neucube.setEncodingAlgorithm(encodingAlgorithm);
        neucube.setUnsupervisedLearningAlgorithm(unsupervisedLearningAlgorithm);
        neucube.setSupervisedLearningAlgorithm(supervisedLearningAlgorithm);
        neucube.setClassifier(classifier);
        neucube.setCrossValidation(crossValidation);
        neucube.setReservoirBuilder(reservoirBuilder);
        LOGGER.info("NeuCube project configuration complete");
        return true;
    }

    public int getTask(String problemType) {
        int task;
        switch (problemType.toUpperCase()) {
            case "CLASSIFICATION":
                task = Tasks.CLASSIFICATION;
                break;
            case "REGRESSION":
                task = Tasks.REGRESSION;
                break;
            default:
                task = Tasks.CLASSIFICATION;
                break;
        }
        return task;
    }

    public SpatioTemporalData configureSpatioTemporalData() {
        LOGGER.info("Configuring spatio-temporal dataset");
        SpatioTemporalData SSTD = new SpatioTemporalData();
        SSTD.setEncoded(Boolean.parseBoolean(this.properties.getProperty("stdIsEncodedData").toLowerCase()));
        SSTD.setNumVariables(Integer.parseInt(this.properties.getProperty("stdVariables")));
        SSTD.setRateForTraining(Double.parseDouble(this.properties.getProperty("rateForTraining")));
        SSTD.setDirectory(this.properties.getProperty("stdDirectory"));
        SSTD.setOffLine(Boolean.parseBoolean(this.properties.getProperty("stdOffLine")));
        LOGGER.info("Complete");
        return SSTD;
    }

    public Network configureNetwork() {
        Network network = new Network();
        SpikingNeuron spikingNeuron = this.configureSpikingNeuron();
        network.setSpikingNeuron(spikingNeuron);
        network.setNumVariables(Integer.parseInt(this.properties.getProperty("numVariables")));
        network.setAllowInhibitoryInputNeurons(Boolean.parseBoolean(this.properties.getProperty("allowInhibitoryInputNeurons").toLowerCase()));
        return network;
    }

    public SpikingNeuron configureSpikingNeuron() {
        LOGGER.info("Configuring the spiking neuron model");
        SpikingNeuron spikingNeuron = new SpikingNeuron();
        Core core = configureCore(this.properties.getProperty("coreName"));
        spikingNeuron.setCore(core);
        switch (this.properties.getProperty("snType").toUpperCase()) {
            case "EXCITATORY": {
                spikingNeuron.setType(NeuronType.EXCITATORY);
            }
            break;
            case "INHIBITORY": {
                spikingNeuron.setType(NeuronType.INHIBITORY);
            }
            break;
            default: {
                spikingNeuron.setType(NeuronType.EXCITATORY);
            }
        }

        spikingNeuron.setMaxDelay(Integer.parseInt(this.properties.getProperty("snMaxDelay")));
        switch (this.properties.getProperty("snTypeDelay").toUpperCase()) {
            case "FIXED": {
                spikingNeuron.setTypeDelay(SpikingNeuron.FIXED_DELAY);
            }
            break;
            case "RANDOM": {
                spikingNeuron.setTypeDelay(SpikingNeuron.RANDOM_DELAY);
            }
            break;
            default:
                spikingNeuron.setTypeDelay(SpikingNeuron.FIXED_DELAY);
        }
        LOGGER.info("Spiking neuron configuration complete");
        return spikingNeuron;
    }

    public Core configureCore(String coreName) {
        LOGGER.info("Configuring " + coreName + " spiking neuron core");
        Core core;
        switch (coreName.toUpperCase()) {
            case "SLIF": {
                CoreConstants.SIMPLIFIED_LIF.setThresholdVoltage(Double.parseDouble(this.properties.getProperty("lifThresholdVoltage")));
                CoreConstants.SIMPLIFIED_LIF.setResetVoltage(Double.parseDouble(this.properties.getProperty("lifResetVoltage")));
                CoreConstants.SIMPLIFIED_LIF.setThresholdRefractoryTime(Integer.parseInt(this.properties.getProperty("lifThresholdRefractoryTime")));
                CoreConstants.SIMPLIFIED_LIF.setLeakValue(Double.parseDouble(this.properties.getProperty("slifLeakValue")));
                core = CoreConstants.SIMPLIFIED_LIF;
            }
            break;
            case "LIF": {
                CoreConstants.LIF.setThresholdVoltage(Double.parseDouble(this.properties.getProperty("lifThresholdVoltage")));
                CoreConstants.LIF.setResetVoltage(Double.parseDouble(this.properties.getProperty("lifResetVoltage")));
                CoreConstants.LIF.setThresholdRefractoryTime(Integer.parseInt(this.properties.getProperty("lifThresholdRefractoryTime")));
                CoreConstants.LIF.setResistance(Double.parseDouble(this.properties.getProperty("lifResistance")));
                CoreConstants.LIF.setCapacitance(Double.parseDouble(this.properties.getProperty("lifCapacitance")));
                core = CoreConstants.LIF;
            }
            break;
            case "IZHIKEVICH": {
                CoreConstants.IZHIKEVICH.setBehaviour(this.properties.getProperty("izBehaviour").charAt(0));
                core = CoreConstants.IZHIKEVICH;
            }
            break;
            case "PROBABILISTIC": {
                CoreConstants.PROBABILISTIC.setThresholdVoltage(Double.parseDouble(this.properties.getProperty("probThresholdVoltage")));
                CoreConstants.PROBABILISTIC.setResetVoltage(Double.parseDouble(this.properties.getProperty("probResetVoltage")));
                CoreConstants.PROBABILISTIC.setThresholdRefractoryTime(Integer.parseInt(this.properties.getProperty("probThresholdRefractoryTime")));
                CoreConstants.PROBABILISTIC.setResistance(Double.parseDouble(this.properties.getProperty("probResistance")));
                CoreConstants.PROBABILISTIC.setCapacitance(Double.parseDouble(this.properties.getProperty("probCapacitance")));
                core = CoreConstants.PROBABILISTIC;
            }
            break;
            default:
                core = CoreConstants.LIF;
        }
        core.setRecordFirings(Boolean.parseBoolean(this.properties.getProperty("coreRecordFirings").toLowerCase()));
        LOGGER.info("Spiking neuron core configuration complete");
        return core;
    }

    public ConnectionAlgorithm configureConnectionAlgorithm(String connectionAlgorithmName) {
        LOGGER.info("Configuring " + connectionAlgorithmName + " connection algorithm");
        ConnectionAlgorithm connectionAlgorithm;
        switch (connectionAlgorithmName.toUpperCase()) {
            case "SMALL_WORLD": {
                ConnectionAlgorithm.SMALL_WORLD.setRadius(Double.parseDouble(this.properties.getProperty("smallWorldRadius")));
                ConnectionAlgorithm.SMALL_WORLD.setPositiveConnectionRate(Double.parseDouble(this.properties.getProperty("smallWorldPossitiveRate")));
                connectionAlgorithm = ConnectionAlgorithm.SMALL_WORLD;
            }
            break;
            case "SMALL_WORLD_IMAGES": {
                ConnectionAlgorithm.SMALL_WORLD_IMAGES.setRadius(Double.parseDouble(this.properties.getProperty("smallWorldRadius")));
                ConnectionAlgorithm.SMALL_WORLD_IMAGES.setPositiveConnectionRate(Double.parseDouble(this.properties.getProperty("smallWorldPossitiveRate")));
                connectionAlgorithm = ConnectionAlgorithm.SMALL_WORLD_IMAGES;
            }
            break;
            case "EPUSSS_SMALL_WORLD": {
                ConnectionAlgorithm.EPUSSS_SMALL_WORLD.setRadius(Double.parseDouble(this.properties.getProperty("epussSmallWorldRadius")));
                ConnectionAlgorithm.EPUSSS_SMALL_WORLD.setPositiveConnectionRate(Double.parseDouble(this.properties.getProperty("epussSmallWorldPossitiveRate")));
                ConnectionAlgorithm.EPUSSS_SMALL_WORLD.setBias(Double.parseDouble(this.properties.getProperty("epussSmallWorldBias")));
                connectionAlgorithm = ConnectionAlgorithm.EPUSSS_SMALL_WORLD;
            }
            break;
            case "FULL_CONNECT":{
                connectionAlgorithm = ConnectionAlgorithm.FULL_CONNECT;
            }
            break;
            default:
                connectionAlgorithm = ConnectionAlgorithm.SMALL_WORLD;
        }
        connectionAlgorithm.setMinWeightValue(Double.parseDouble(this.properties.getProperty("minWeightValue")));
        connectionAlgorithm.setMaxWeightValue(Double.parseDouble(this.properties.getProperty("maxWeightValue")));
        LOGGER.info("Connection algorithm configuration complete");
        return connectionAlgorithm;
    }

    public EncodingAlgorithm configureEncodingAlgorithm(String encodingAlgorithmName) {                
        LOGGER.info("Configuring " + encodingAlgorithmName + " encoding algorithm");
        EncodingAlgorithm algorithm;
        switch (encodingAlgorithmName.toUpperCase()) {
            case "AER": {
                EncodingAlgorithm.AER.setSpikeThresholdValue(Double.parseDouble(this.properties.getProperty("aerSpikeThresholdValue")));
                algorithm = EncodingAlgorithm.AER;
            }
            break;
            case "ONLINE_AER": {
                EncodingAlgorithm.ONLINE_AER.setNumFeatures(Integer.parseInt(this.properties.getProperty("aerOnlineNumFeatures")));
                EncodingAlgorithm.ONLINE_AER.setAlpha(Double.parseDouble(this.properties.getProperty("aerOnlineAlpha")));
                algorithm = EncodingAlgorithm.ONLINE_AER;
            }
            break;
            case "BSA": {
                String strThresholdVec = this.properties.getProperty("thresholdVec");
                String strFilterOrderVec = this.properties.getProperty("filterOrderVec");
                String strFilterCutoffVec = this.properties.getProperty("filterCutoffVec");
                EncodingAlgorithm.BSA.setNumFilters(Integer.parseInt(this.properties.getProperty("numFilters")));
                EncodingAlgorithm.BSA.setThresholdVec(Util.StringToDouble(strThresholdVec.split(",")));
                EncodingAlgorithm.BSA.setFilterOrderVec(Util.StringToInteger(strFilterOrderVec.split(",")));
                EncodingAlgorithm.BSA.setFilterCutoffVec(Util.StringToDouble(strFilterCutoffVec.split(",")));
                algorithm = EncodingAlgorithm.BSA;
            }
            break;
            case "SF": {
                EncodingAlgorithm.SF.setSearchOptimalThreshold(Boolean.parseBoolean(this.properties.getProperty("searchOptimalThreshold")));
                if(!EncodingAlgorithm.SF.isSearchOptimalThreshold()){
                    EncodingAlgorithm.SF.setSpikeThresholdValue(Double.parseDouble(this.properties.getProperty("sfSpikeThresholdValue")));                
                }
                algorithm = EncodingAlgorithm.SF;
            }
            break;
            case "TBR": {
                EncodingAlgorithm.TBR.setSearchOptimalThreshold(Boolean.parseBoolean(this.properties.getProperty("searchOptimalThreshold")));
                if(!EncodingAlgorithm.TBR.isSearchOptimalThreshold()){
                    EncodingAlgorithm.TBR.setSpikeThresholdValue(Double.parseDouble(this.properties.getProperty("tbrSpikeThresholdValue")));
                }
                algorithm = EncodingAlgorithm.SF;
            }            
            break;
            default: {
                algorithm = EncodingAlgorithm.AER;
            }
        }
        LOGGER.info("Encoding algorithm configuration complete");
        return algorithm;
    }

    public LearningAlgorithm configureUnsupervisedLearningAlgorithm(String unsupervisedLearningAlgorithmName) {
        LOGGER.info("Configuring " + unsupervisedLearningAlgorithmName + " unsupervised learning algorithm");
        LearningAlgorithm algorithm = null;
        switch (unsupervisedLearningAlgorithmName.toUpperCase()) {
            case "STDP": {
                if (validateCommonStdpVariables()) {
                    LearningAlgorithm.STDP.setApos(Double.parseDouble(this.properties.getProperty("Apos")));
                    LearningAlgorithm.STDP.setAneg(Double.parseDouble(this.properties.getProperty("Aneg")));
                    LearningAlgorithm.STDP.setTauPos(Double.parseDouble(this.properties.getProperty("tauPos")));
                    LearningAlgorithm.STDP.setTauNeg(Double.parseDouble(this.properties.getProperty("tauNeg")));
                    LearningAlgorithm.STDP.setUpperBound(Double.parseDouble(this.properties.getProperty("upperBound")));
                    LearningAlgorithm.STDP.setLowerBound(Double.parseDouble(this.properties.getProperty("lowerBound")));
                }
                algorithm = LearningAlgorithm.STDP;
            }
            break;
            case "ONLINE_STDP": {
                if (validateCommonStdpVariables()) {
                    LearningAlgorithm.ONLINE_STDP.setApos(Double.parseDouble(this.properties.getProperty("Apos")));
                    LearningAlgorithm.ONLINE_STDP.setAneg(Double.parseDouble(this.properties.getProperty("Aneg")));
                    LearningAlgorithm.ONLINE_STDP.setTauPos(Double.parseDouble(this.properties.getProperty("tauPos")));
                    LearningAlgorithm.ONLINE_STDP.setTauNeg(Double.parseDouble(this.properties.getProperty("tauNeg")));
                    LearningAlgorithm.ONLINE_STDP.setUpperBound(Double.parseDouble(this.properties.getProperty("upperBound")));
                    LearningAlgorithm.ONLINE_STDP.setLowerBound(Double.parseDouble(this.properties.getProperty("lowerBound")));
                }
                algorithm = LearningAlgorithm.ONLINE_STDP;
            }
            break;
            case "STDP_IMAGES": {
                if (validateCommonStdpVariables()) {
                    LearningAlgorithm.ONLINE_STDP.setApos(Double.parseDouble(this.properties.getProperty("Apos")));
                    LearningAlgorithm.ONLINE_STDP.setAneg(Double.parseDouble(this.properties.getProperty("Aneg")));
                    LearningAlgorithm.ONLINE_STDP.setTauPos(Double.parseDouble(this.properties.getProperty("tauPos")));
                    LearningAlgorithm.ONLINE_STDP.setTauNeg(Double.parseDouble(this.properties.getProperty("tauNeg")));
                    LearningAlgorithm.ONLINE_STDP.setUpperBound(Double.parseDouble(this.properties.getProperty("upperBound")));
                    LearningAlgorithm.ONLINE_STDP.setLowerBound(Double.parseDouble(this.properties.getProperty("lowerBound")));
                }
                algorithm = LearningAlgorithm.ONLINE_STDP;
            }
            break;
            case "STDPH": {
                if (validateCommonStdpVariables()) {
                    LearningAlgorithm.STDPH.setApos(Double.parseDouble(this.properties.getProperty("Apos")));
                    LearningAlgorithm.STDPH.setAneg(Double.parseDouble(this.properties.getProperty("Aneg")));
                    LearningAlgorithm.STDPH.setTauPos(Double.parseDouble(this.properties.getProperty("tauPos")));
                    LearningAlgorithm.STDPH.setTauNeg(Double.parseDouble(this.properties.getProperty("tauNeg")));
                    LearningAlgorithm.STDPH.setUpperBound(Double.parseDouble(this.properties.getProperty("upperBound")));
                    LearningAlgorithm.STDPH.setLowerBound(Double.parseDouble(this.properties.getProperty("lowerBound")));
                }
                algorithm = LearningAlgorithm.STDPH;
            }
            break;
            case "STDP_DELAYS": {
                if (validateCommonStdpVariables()) {
                    LearningAlgorithm.STDP_DELAYS.setApos(Double.parseDouble(this.properties.getProperty("Apos")));
                    LearningAlgorithm.STDP_DELAYS.setAneg(Double.parseDouble(this.properties.getProperty("Aneg")));
                    LearningAlgorithm.STDP_DELAYS.setTauPos(Double.parseDouble(this.properties.getProperty("tauPos")));
                    LearningAlgorithm.STDP_DELAYS.setTauNeg(Double.parseDouble(this.properties.getProperty("tauNeg")));
                    LearningAlgorithm.STDP_DELAYS.setUpperBound(Double.parseDouble(this.properties.getProperty("upperBound")));
                    LearningAlgorithm.STDP_DELAYS.setLowerBound(Double.parseDouble(this.properties.getProperty("lowerBound")));
                }
                algorithm = LearningAlgorithm.STDP_DELAYS;
            }
            break;
            case "STDP_MATLAB": {
                LearningAlgorithm.STDP_MATLAB.setRate(Double.parseDouble(this.properties.getProperty("learningRate")));
                LearningAlgorithm.STDP_MATLAB.setUpperBound(Double.parseDouble(this.properties.getProperty("upperBound")));
                LearningAlgorithm.STDP_MATLAB.setLowerBound(Double.parseDouble(this.properties.getProperty("lowerBound")));
                algorithm=LearningAlgorithm.STDP_MATLAB;
            }
            break;
            case "EPUSSS": {
                LearningAlgorithm.EPUSSS.setAlpha(Double.parseDouble(this.properties.getProperty("epusssAlpha")));
                LearningAlgorithm.EPUSSS.setDeltaAlpha(Double.parseDouble(this.properties.getProperty("epusssDeltaAlpha")));
                LearningAlgorithm.EPUSSS.setAlphaMin(Double.parseDouble(this.properties.getProperty("epusssAlphaMin")));
                LearningAlgorithm.EPUSSS.setAlphaMax(Double.parseDouble(this.properties.getProperty("epusssAlphaMax")));
                LearningAlgorithm.EPUSSS.setNumDeepLayers(Integer.parseInt(this.properties.getProperty("epusssNumDeepLayers")));
                LearningAlgorithm.EPUSSS.setStepAhead(Integer.parseInt(this.properties.getProperty("epusssStepAhead")));
                algorithm = LearningAlgorithm.EPUSSS;
            }
            break;
            case "NRDP": {
                algorithm = LearningAlgorithm.NRDP;
            }
            break;
            default: {
                algorithm = LearningAlgorithm.STDP;
            }
            break;
        }
        algorithm.setTrainingRounds(Integer.parseInt(this.properties.getProperty("trainingRounds")));
        algorithm.setSavingWeightMode(Boolean.parseBoolean(this.properties.getProperty("savingWeightMode").toLowerCase()));
        LOGGER.info("Unsupevised learning algorithm configuration complete");
        return algorithm;
    }

    public boolean validateCommonStdpVariables() {
        boolean status = false;
        if (Util.isDouble(this.properties.getProperty("Apos"), 0.0, 0.1)) {
            if (Util.isDouble(this.properties.getProperty("Aneg"), 0.0, 0.1)) {
                if (Util.isDouble(this.properties.getProperty("tauPos"))) {
                    if (Util.isDouble(this.properties.getProperty("tauNeg"))) {
                        if (Util.isDouble(this.properties.getProperty("upperBound"))) {
                            if (Util.isDouble(this.properties.getProperty("lowerBound"))) {
                                status = true;
                            } else {
                                //LearningAlgorithm.STDP.setLowerBound(Double.NEGATIVE_INFINITY);
                                LOGGER.error("Format number", "Invalid decimal number found for the lower bound synaptic weight");
                            }
                        } else {
                            //LearningAlgorithm.STDP.setUpperBound(Double.POSITIVE_INFINITY);
                            LOGGER.error("Format number", "Invalid decimal number found for the upper bound synaptic weight");
                        }
                    } else {
                        LOGGER.error("Format number", "Invalid decimal number found for intput 'τ-'");
                    }
                } else {
                    LOGGER.error("Format number", "Invalid decimal number found for intput 'τ+'");
                }
            } else {
                LOGGER.error("Format number", "Invalid decimal number found for intput 'A-'");
            }
        } else {
            LOGGER.error("Format number", "Invalid decimal number found for intput 'A+'");
        }
        return status;
    }

    public LearningAlgorithm configureSupervisedLearningAlgorithm(String supervisedLearningAlgorithmName) {
        LOGGER.info("Configuring " + supervisedLearningAlgorithmName + " supervised learning algorithm");
        LearningAlgorithm algorithm = null;
        switch (supervisedLearningAlgorithmName.toUpperCase()) {
            case "DE_SNN_S": {
                Classifier classifier = this.configureClassifier(this.properties.getProperty("classifierName"));
                LearningAlgorithm.DE_SNN_S.setPositiveDrift(Double.parseDouble(this.properties.getProperty("positiveDrift")));
                LearningAlgorithm.DE_SNN_S.setNegativeDrift(Double.parseDouble(this.properties.getProperty("negativeDrift")));
                LearningAlgorithm.DE_SNN_S.setMod(Double.parseDouble(this.properties.getProperty("mod")));
                LearningAlgorithm.DE_SNN_S.setAlpha(Double.parseDouble(this.properties.getProperty("deSnnAlpha")));
                LearningAlgorithm.DE_SNN_S.setClassifier(classifier);
                algorithm = LearningAlgorithm.DE_SNN_S;
            }
            break;
            case "DE_SNN_ADAPTIVE": {
                Classifier classifier = this.configureClassifier(this.properties.getProperty("classifierName"));
                LearningAlgorithm.DE_SNN_ADAPTIVE.setPositiveDrift(Double.parseDouble(this.properties.getProperty("positiveDrift")));
                LearningAlgorithm.DE_SNN_ADAPTIVE.setNegativeDrift(Double.parseDouble(this.properties.getProperty("negativeDrift")));
                LearningAlgorithm.DE_SNN_ADAPTIVE.setMod(Double.parseDouble(this.properties.getProperty("mode")));
                LearningAlgorithm.DE_SNN_ADAPTIVE.setAlpha(Double.parseDouble(this.properties.getProperty("deSnnAlpha")));
                LearningAlgorithm.DE_SNN_ADAPTIVE.setClassifier(classifier);
                algorithm = LearningAlgorithm.DE_SNN_ADAPTIVE;
            }
            break;
            default: {
                algorithm = LearningAlgorithm.DE_SNN_S;
            }
        }
        LOGGER.info("Supervised learning algorithm configuration complete");
        return algorithm;
    }

    public CrossValidation configureCrossValidation(String crossValidationMethodName) {
        LOGGER.info("Configuring " + crossValidationMethodName + " cross validation method");
        CrossValidation method = null;
        switch (crossValidationMethodName.toUpperCase()) {
            case "K_FOLD": {
                CrossValidation.K_FOLD.setNumFolds(Integer.parseInt(this.properties.getProperty("numFolds")));
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
                method = CrossValidation.LEAVE_ONE_OUT;
            }
        }
        LOGGER.info("Cross validation method configuration complete");
        return method;
    }

    public Classifier configureClassifier(String classifierName) {
        LOGGER.info("Configuring " + classifierName + " classification algorithm");
        Classifier method = null;
        Distance distance = this.getClassifierDistance(this.properties.getProperty("classifierDistance"));
        switch (classifierName.toUpperCase()) {
            case "KNN": {
                Classifier.KNN.setK(Integer.parseInt(this.properties.getProperty("knnK")));
                Classifier.KNN.setDistance(distance);
                Classifier.KNN.setTask(this.getTask(this.properties.getProperty("problemType")));
                method = Classifier.KNN;
            }
            break;
            case "WKNN": {
                Classifier.WKNN.setK(Integer.parseInt(this.properties.getProperty("knnK")));
                Classifier.WKNN.setDistance(distance);
                Classifier.WKNN.setTask(this.getTask(this.properties.getProperty("problemType")));
                method = Classifier.WKNN;
            }
            break;
            default: {
                Classifier.KNN.setTask(this.getTask(this.properties.getProperty("problemType")));
                method = Classifier.KNN;
            }
        }
        LOGGER.info("Classification algorithm configuration complete");
        return method;
    }

    public Distance getClassifierDistance(String distanceName) {
        switch (distanceName.toUpperCase()) {
            case "EUCLIDIAN": {
                return Distance.EUCLIDIAN_DISTANCE;
            }
            case "GAUSSIAN": {
                return Distance.GAUSSIAN_DISTANCE;
            }
            default: {
                return Distance.EUCLIDIAN_DISTANCE;
            }
        }
    }

    public OnlineReader configureOnlineReader() {
        String onlineReaderName = properties.getProperty("onlineReader");
        LOGGER.info("Configuring online reader: " + onlineReaderName);
        OnlineReader onlineReader = null;
        switch (onlineReaderName.toUpperCase()) {
            case "ONLINE_SEISMIC_FILE_READER": {
                LOGGER.info("  - Setting online seismic file reader parameters");
                OnlineSeismicFileReader reader = new OnlineSeismicFileReader();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                try {
                    Date date = sdf.parse(properties.getProperty("readerCalendar"));
                    Calendar calendar = sdf.getCalendar();
                    //Calendar currentCalendar = new GregorianCalendar(2010, Calendar.JANUARY, 1, 0, 0, 0);                    
                    reader.setHourIncreament(1);
                    reader.setCurrentCalendar(calendar);
                    reader.setFileDir(properties.getProperty("fileReaderDirectory"));
                    reader.setFileName("");
                    onlineReader = reader;
                } catch (ParseException ex) {
                    LOGGER.error(ex.getMessage());
                }
            }
            break;
            default: {
                onlineReader = new OnlineSeismicFileReader();
            }
        }
        LOGGER.info("Online reader configuration complete");
        return onlineReader;
    }

    private ReservoirBuilder configureReservoirBuilder(String reservoirBuilderName) {
        LOGGER.info("Configuring " + reservoirBuilderName + " reservoir builder algorithm");
        ReservoirBuilder reservoirBuilder = null;
        switch (reservoirBuilderName.toUpperCase()) {
            case "MIX_RESERVOIR": {
                ReservoirBuilder.MIX_RESERVOIR.setExcitatoryNeuronRate(Double.parseDouble(properties.getProperty("excitatoryNeuronRate")));
                reservoirBuilder = ReservoirBuilder.MIX_RESERVOIR;
            }
            case "NEUCUBE_RESERVOIR": {
                reservoirBuilder = ReservoirBuilder.NEUCUBE_RESERVOIR;
            }
            default: {
                reservoirBuilder = ReservoirBuilder.NEUCUBE_RESERVOIR;
            }
        }
        LOGGER.info("Reservoir builder algorithm complete");
        return reservoirBuilder;
    }

}
