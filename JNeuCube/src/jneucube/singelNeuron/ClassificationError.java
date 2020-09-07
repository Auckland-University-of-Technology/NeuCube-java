/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.singelNeuron;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import jneucube.classifiers.Classifier;
import jneucube.crossValidation.CrossValidation;
import jneucube.data.DataSample;
import static jneucube.log.Log.LOGGER;
import jneucube.optimisation.fitnessFunctions.FitnessFunction;
import jneucube.spikingNeurons.SpikingNeuron;
import jneucube.spikingNeurons.Synapse;
import jneucube.spikingNeurons.cores.Core;
import jneucube.spikingNeurons.cores.CoreConstants;
import jneucube.util.ConfusionMatrix;

/**
 *
 * @author em9403
 */
public class ClassificationError extends FitnessFunction {

    private SpikingNeuron spikingNeuron;
    //private DataController dataController = new DataController();
    private ArrayList<DataSample> trainingData;
    private ArrayList<DataSample> testingData;
    private Classifier classifier = Classifier.KNN;
    private CrossValidation crossValidation;
    private ConfusionMatrix crossValidationConfusionMatrix;
    private ConfusionMatrix testConfusionMatrix;
    private HashMap<Double, Double> firingAverages;

    public ClassificationError() {
        this.setOptimalValue(0.0);
        this.setOptimisationType(FitnessFunction.MINIMISATION);
        //this.setDimensionality(14); // number of connections
    }

    /**
     * The {@code evaluateIndividual} function calculates the fitness of an
     * individual. THe individual is formed by the number of features of the
     * dataset plus the threshold value and the threshold of the refractory time
     * of the LIF model.
     *
     * @param id
     * @param individual number of features plus for the LIF threshold voltage
     * and threshold refractory time
     * @return
     */
    @Override
    public double evaluateIndividual(int id, double[] individual) {
        // an individual is a set of weights        
        double fitness;
        double trainAccuracy;
        double testAccuracy;

        this.updateWeights(individual);         // Updates the network weights
        this.updateSpikingNeuron(individual);   // Update the spiking neuron

        // Training the model (cross validation)

        
        this.runCrossvalidation();
        int[] sampleIds = new int[this.trainingData.size()];
        double[] actualValues = new double[this.trainingData.size()];
        double[] predictedValues = new double[this.trainingData.size()];
        for (int i = 0; i < this.trainingData.size(); i++) {
            sampleIds[i] = this.trainingData.get(i).getSampleId();
            actualValues[i] = this.trainingData.get(i).getClassId();
            predictedValues[i] = this.trainingData.get(i).getValidationClassId();
        }
        this.crossValidationConfusionMatrix = new ConfusionMatrix(sampleIds, actualValues, predictedValues);
        trainAccuracy = this.crossValidationConfusionMatrix.getAccuracy();

        // Fitting the model and running classification for evaluation
        this.runExperiment(this.trainingData, this.testingData);  // fitting the model

        // Evaluation of the model
        sampleIds = new int[this.testingData.size()];
        actualValues = new double[this.testingData.size()];
        predictedValues = new double[this.testingData.size()];
        for (int i = 0; i < this.testingData.size(); i++) {
            sampleIds[i] = this.testingData.get(i).getSampleId();
            actualValues[i] = this.testingData.get(i).getClassId();
            predictedValues[i] = this.testingData.get(i).getValidationClassId();
        }
        this.testConfusionMatrix = new ConfusionMatrix(sampleIds, actualValues, predictedValues);
        testAccuracy = this.testConfusionMatrix.getAccuracy();

        //fitness = (trainAccuracy + testAccuracy) / 2.0;
        fitness = this.testConfusionMatrix.getErrors() + this.crossValidationConfusionMatrix.getErrors();
        LOGGER.info(id + "=" + fitness);
        return fitness;
    }

    /**
     * Runs the cross-validation process. For each fold it calls the function
     * {@link #runExperiment(java.util.ArrayList, java.util.ArrayList)}
     *
     */
    private void runCrossvalidation() {
        for (int fold = 0; fold < this.crossValidation.getNumFolds(); fold++) {
            this.crossValidation.setCurrentFold(fold);
            this.runExperiment(this.getCrossValidation().getTrainingData(fold), this.getCrossValidation().getValidationData(fold));
        }
    }

    public void runExperiment(ArrayList<DataSample> trainingData, ArrayList<DataSample> testingData) {
        double[][] frTraining = this.propagateData(trainingData);

        this.firingAverages = this.getFiringAverages(frTraining);

        this.evaluateData(testingData, this.firingAverages);

    }

    public void evaluateData(ArrayList<DataSample> dataSamples) {
       double[][] frTraining = this.propagateData(dataSamples);
       this.firingAverages = this.getFiringAverages(frTraining);
       this.evaluateData(dataSamples, this.firingAverages);
       System.out.println(firingAverages);
    }

    public void evaluateData(ArrayList<DataSample> dataSamples, HashMap<Double, Double> firingAverages) {
        double[][] frTesting = this.propagateData(dataSamples);
        double distance;
        double distanceTemp;
        for (int i = 0; i < frTesting.length; i++) {
//            System.out.println(frTesting[i][0] + "," + frTesting[i][1] + "," + frTesting[i][2]);
            distanceTemp = Double.POSITIVE_INFINITY;
            for (Map.Entry<Double, Double> entry : firingAverages.entrySet()) {
                Double classId = entry.getKey();
                Double firingRate = entry.getValue();
                distance = Math.abs(frTesting[i][2] - firingRate);
                if (distance < distanceTemp) {
                    distanceTemp = distance;
                    dataSamples.get(i).setValidationClassId(classId);
                }
            }
        }
    }

    /**
     *
     * @param frTraining an m-by-3 containing the sample id, the class id, and
     * the number of spikes of the spiking neuron.
     * @return
     */
    public HashMap<Double, Double> getFiringAverages(double[][] frTraining) {
        HashMap<Double, double[]> map = new HashMap<>();  // map for the firing rate
        HashMap<Double, Double> mapAverage = new HashMap<>(); // map for calcuolating the average
        double[] firingVec;
        for (int i = 0; i < frTraining.length; i++) {
            if (map.containsKey(frTraining[i][1])) {
                firingVec = map.get(frTraining[i][1]);
                firingVec[0] += frTraining[i][2];
                firingVec[1]++;
            } else {
                firingVec = new double[2];
                firingVec[0] = frTraining[i][2];
                firingVec[1] = 1;
                map.put(frTraining[i][1], firingVec);
            }
        }
        for (Map.Entry<Double, double[]> entry : map.entrySet()) {
            Double key = entry.getKey();
            double[] value = entry.getValue();
            Double avg = value[0] / (value[1] * 1.0);
            mapAverage.put(key, avg);
        }
        return mapAverage;
    }

    public void updateWeights(double[] individual) {
        for (int i = 0; i < this.spikingNeuron.getInputSynapses().size(); i++) {
            this.spikingNeuron.getInputSynapses().get(i).setWeight(individual[i]);
        }
    }

    public void updateSpikingNeuron(double[] individual) {
        int startIndex = this.spikingNeuron.getInputSynapses().size();
        Core core = this.spikingNeuron.getCore();
        if (core.getClass() == CoreConstants.LIF.getClass()) {
            double thresholdVoltage = individual[startIndex];
            int thresholdRefractoryTime = (int) individual[startIndex + 1];
            CoreConstants.LIF.setThresholdVoltage(thresholdVoltage);
            CoreConstants.LIF.setThresholdRefractoryTime(thresholdRefractoryTime);
        } else if (core.getClass() == CoreConstants.IZHIKEVICH.getClass()) {
            int idx = (int) individual[startIndex];
            idx = Math.min(Math.max(idx, 0), CoreConstants.IZHIKEVICH.getBehaviors().length() - 1);
            char behaviour = CoreConstants.IZHIKEVICH.getBehaviors().charAt(idx);
            CoreConstants.IZHIKEVICH.setBehaviour(behaviour);
        }
    }

    /**
     * The {@code propagateData} function iterates a list of samples and
     * calculates the number of spikes that a spiking neuron produces after
     * being stimulated with each sample {@link #propagateSample(jneucube.data.DataSample).
     *
     * @param samples the array list of temporal samples
     * @return an m-by-3 matrix containing the sample id, the class id, and the
     * number of spikes that a neuron produces after being stimulated with the
     * temporal sample.
     */
    public double[][] propagateData(ArrayList<DataSample> samples) {
        double[][] firingRates = new double[samples.size()][3];
        long start=System.currentTimeMillis();
        LOGGER.debug("Propagating  "+samples.size() +" samples...");
        for (int i = 0; i < samples.size(); i++) {
            long startSample=System.currentTimeMillis();
            LOGGER.debug("Propagating sample "+i +" "+samples.get(i).getSampleFileName());
            firingRates[i][0] = samples.get(i).getSampleId();
            firingRates[i][1] = (int) samples.get(i).getClassId();
            firingRates[i][2] = propagateSample(samples.get(i));    // firing rate
            LOGGER.debug("Complete sample  (time="+(System.currentTimeMillis()-startSample)+")");
        }
        LOGGER.debug("Complete "+(System.currentTimeMillis()-start));
        return firingRates;
    }

    /**
     * The {@code propagateSample} utilises the temporal data of a sample to
     * stimulate a spiking neuron for producing a spike train.
     *
     * @param sample the sample with temporal data.
     * @return the number of spikes that a spiking neuron produces
     */
    public double propagateSample(DataSample sample) {        
        int sampleTime = sample.getNumRecords();
        spikingNeuron.resetActivity();
        spikingNeuron.resetCurrent();
        spikingNeuron.getCore().reset();

        double current = 0.0;
        double[] features;
        Synapse synapse;
        for (int t = 0; t < sampleTime; t++) {
            features = sample.getData().getVecRow(t);
            current = 0.0;
            for (int i = 0; i < features.length; i++) {
                synapse = spikingNeuron.getInputSynapses().get(i);
                current += (features[i] * synapse.getWeight());
            }
            spikingNeuron.getCore().computeMembranePotential(t, current);
        }
        int numFirings = spikingNeuron.getFirings().size();
        int numRows = sample.getData().getRows();
        double firingRate = numFirings / (numRows * 1.0);
        //return spikingNeuron.getFirings().size();
        return firingRate;
    }

    /**
     * @return the spikingNeuron
     */
    public SpikingNeuron getSpikingNeuron() {
        return spikingNeuron;
    }

    /**
     * @param spikingNeuron the spikingNeuron to set
     */
    public void setSpikingNeuron(SpikingNeuron spikingNeuron) {
        this.spikingNeuron = spikingNeuron;
    }

    /**
     * @return the classifier
     */
    public Classifier getClassifier() {
        return classifier;
    }

    /**
     * @param classifier the classifier to set
     */
    public void setClassifier(Classifier classifier) {
        this.classifier = classifier;
    }

    /**
     * @return the crossValidation
     */
    public CrossValidation getCrossValidation() {
        return crossValidation;
    }

    /**
     * @param crossValidation the crossValidation to set
     */
    public void setCrossValidation(CrossValidation crossValidation) {
        this.crossValidation = crossValidation;
    }

    /**
     * @return the crossValidationConfusionMatrix
     */
    public ConfusionMatrix getCrossValidationConfusionMatrix() {
        return crossValidationConfusionMatrix;
    }

    /**
     * @param crossValidationConfusionMatrix the crossValidationConfusionMatrix
     * to set
     */
    public void setCrossValidationConfusionMatrix(ConfusionMatrix crossValidationConfusionMatrix) {
        this.crossValidationConfusionMatrix = crossValidationConfusionMatrix;
    }

    /**
     * @return the testConfusionMatrix
     */
    public ConfusionMatrix getTestConfusionMatrix() {
        return testConfusionMatrix;
    }

    /**
     * @param testConfusionMatrix the testConfusionMatrix to set
     */
    public void setTestConfusionMatrix(ConfusionMatrix testConfusionMatrix) {
        this.testConfusionMatrix = testConfusionMatrix;
    }

    /**
     * @return the firingAverages
     */
    public HashMap<Double, Double> getFiringAverages() {
        return firingAverages;
    }

    /**
     * @param firingAverages the firingAverages to set
     */
    public void setFiringAverages(HashMap<Double, Double> firingAverages) {
        this.firingAverages = firingAverages;
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
    }

    /**
     * @return the testingData
     */
    public ArrayList<DataSample> getTestingData() {
        return testingData;
    }

    /**
     * @param testingData the testingData to set
     */
    public void setTestingData(ArrayList<DataSample> testingData) {
        this.testingData = testingData;
    }

}
