/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.singelNeuron;

import java.util.ArrayList;
import jneucube.classifiers.Classifier;
import jneucube.classifiers.KNN;
import jneucube.crossValidation.CrossValidation;
import jneucube.data.DataSample;
import jneucube.distances.Distance;
import static jneucube.log.Log.LOGGER;
import jneucube.optimisation.fitnessFunctions.FitnessFunction;
import jneucube.spikingNeurons.SpikingNeuron;
import jneucube.spikingNeurons.Synapse;
import jneucube.spikingNeurons.cores.Core;
import jneucube.spikingNeurons.cores.CoreConstants;
import jneucube.util.ConfusionMatrix;
import jneucube.util.Matrix;

/**
 *
 * @author em9403
 */
public class SingleNeuronClassificationError extends FitnessFunction {

    private SpikingNeuron spikingNeuron;
    private ArrayList<DataSample> trainingData;
    private ArrayList<DataSample> testingData;
    private Classifier classifier;
    private CrossValidation crossValidation;
    private ConfusionMatrix crossValidationConfusionMatrix;
    private ConfusionMatrix testConfusionMatrix;

    public SingleNeuronClassificationError() {
        this.optimalValue = 0.0;
        this.optimisationType = FitnessFunction.MINIMISATION;

        KNN knn = Classifier.KNN;
        knn.setDistance(Distance.GAMMA_DISTANCE);
        knn.setK(5);
        this.classifier=knn;
    }

    /**
     * The {@code evaluateIndividual} function evaluates the fitness of an
     * individual or candidate solution. In this single neuron classification
     * approach, the individual is a formed by the weights of the connections
     * between the input nodes and the single output neuron and the features of
     * the single neuron to be optimised.
     *
     * @param id the identifier of the individual.
     * @param individual the individual.
     * @return the fitness of the individual.
     */
    @Override
    public double evaluateIndividual(int id, double[] individual) {
        double fitness;
        double trainAccuracy;
        double testAccuracy;
        
        this.updateWeights(individual);         // Updates the network weights
        this.updateSpikingNeuron(individual);   // Update the spiking neuron
        
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
        LOGGER.info("Fitness "+id + "=" + fitness);
        
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
        Matrix trainingMatrix = new Matrix(this.propagateData(trainingData));   // getting the firing times of the training dataset
        Matrix testingMatrix = new Matrix(this.propagateData(testingData));     // getting the firing times of the test dataset
        
        // Getting the labels of the training dataset
        Matrix group = new Matrix(trainingData.size(), 1);                      
        for (int i = 0; i < trainingData.size(); i++) {            
            group.set(i, 0, trainingData.get(i).getClassId());
        }
        

        // Classification of the test dataset
        for (int i = 0; i < testingMatrix.getRows(); i++) {
            double label = classifier.classify(testingMatrix.getRow(i), trainingMatrix, group);                        
            testingData.get(i).setValidationClassId(label);            
        }

    }

    /**
     * Stimulates the spiking neuron with all the samples. For each sample it
     * calls the {@link #propagateSample(jneucube.data.DataSample)} function.
     *
     * @param samples the list of samples
     * @return an array of length n which elements are arrays of different
     * lengths.
     */
    public double[][] propagateData(ArrayList<DataSample> samples) {
        LOGGER.debug("Propagating  " + samples.size() + " samples...");
        double[][] data = new double[samples.size()][];
        long start = System.currentTimeMillis();        
        
        for (int i = 0; i < samples.size(); i++) {                   
            double[] firingTimes = this.propagateSample(samples.get(i));
            data[i] = firingTimes;
        }
        LOGGER.debug("Complete " + (System.currentTimeMillis() - start));
        return data;
    }

    /**
     * Stimulates the spiking neuron with the data of the sample and returns the
     * firing times. It resets the activity, the current and the core of spiking
     * neuron before stimulating the neuron.
     *
     * @param sample the data sample.
     * @return the firing times.
     */
    public double[] propagateSample(DataSample sample) {
        int sampleTime = sample.getNumRecords();
        spikingNeuron.resetActivity();  //it resets the curren and clears the core (it alse resets the core        
        double current;
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
        return spikingNeuron.getFirings().stream().mapToDouble(Double::doubleValue).toArray();
    }

    /**
     *
     * @param individual
     */
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
