/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.network;

import jneucube.network.reservoirBuilders.ReservoirBuilder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;
import jneucube.connectionAlgorithms.ConnectionAlgorithm;
import jneucube.data.DataSample;
import jneucube.exceptions.EmptyListOfNeuronsException;
import jneucube.spikingNeurons.NeuronType;
import jneucube.spikingNeurons.SpikingNeuron;
import jneucube.spikingNeurons.Synapse;
import jneucube.spikingNeurons.cores.Core;
import jneucube.trainingAlgorithms.LearningAlgorithm;
import jneucube.util.Matrix;
import jneucube.util.Messages;
import jneucube.util.NeuCubeRuntimeException;
import static jneucube.log.Log.LOGGER;
import jneucube.util.Util;

/**
 * The {@code NetworkController} class contains all methods and functions that
 * gives functionality to the network.
 *
 * @author Josafath Israel Espinosa Ramos
 */
public class NetworkController {

    private Network network;
    private ReservoirBuilder reservoirBuilder;
    //   public ArrayList<HashMap<SpikingNeuron, Integer>> uniqueList = new ArrayList<>();

    public HashMap<SpikingNeuron, Integer> uniqueActiveNeurons = new HashMap<>();

    public NetworkController() {

    }

    public NetworkController(Network network) {
        this.network = network;
    }
    //private SpikingNeuron neuronModel;

    /**
     * This function creates the reservoir and input neurons of the network.
     * Before creating the network, this function cleans all the list that
     * contain the neurons. It is an atomic operation, if a neuron is duplicated
     * or an error occurs while creating the neuron, the function clears the
     * complete network.
     *
     * @param reservoirCoordinates mx2 matrix containing the coordinates (x,y,z)
     * in the reservoir
     * @param inputCoordinates mx2 matrix containing the input coordinates
     * (x,y,x)
     */
    public void createNetwork(Matrix reservoirCoordinates, Matrix inputCoordinates) {
        LOGGER.info("Creating network coordinates");
        long processTime = System.nanoTime();
        try {
            this.deleteNetwork();// Removes the synaptic weights, the connections, and all neurons in the network (reservoir, positive and negative inputs, outputs)
            this.network.setNumInputs(inputCoordinates.getRows());
            LOGGER.debug("   - Network for variable 1");
            this.createReservoirNeurons(reservoirCoordinates);
            this.createInputNeuronsPositive(inputCoordinates);
            if (this.network.isAllowInhibitoryInputNeurons()) {
                this.createInputNeuronsNegative(inputCoordinates); // Add input neurons for negative spikes
            }
            this.replicateReservoir(this.network.getNumVariables());
            LOGGER.info("Complete (time " + ((System.nanoTime() - processTime) / 1000000) + ")");
            LOGGER.info("Total neurons: " + this.network.getReservoir().size());
        } catch (CloneNotSupportedException e) {
            this.deleteNetwork();
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * Creates a list of neurons by replicating the specified neuron model.
     *
     * @param neuronModel the type of spiking neuron to replicate
     * @param numNeurons the number of neurons to be created
     * @return a list of neurons
     * @throws java.lang.CloneNotSupportedException
     */
    public ArrayList<SpikingNeuron> createNeuronGroup(SpikingNeuron neuronModel, int numNeurons) throws CloneNotSupportedException {
        ArrayList<SpikingNeuron> group = new ArrayList<>();
        for (int i = 0; i < numNeurons; i++) {
            SpikingNeuron newNeuron = neuronModel.clone();
            group.add(newNeuron);
        }
        return group;
    }

    /**
     * Set the position of a list of neuron in a 3D space giving a matrix of 3D
     * coordinates. The number of rows in the matrix must be the same as the
     * number of neurons in the list, and the number of columns must be 3,
     * representing the X,Y and Z coordinates.
     *
     * @param neuronList
     * @param coordinates
     */
    public void mapNeurons(ArrayList<SpikingNeuron> neuronList, Matrix coordinates) {
        if (coordinates.getRows() == neuronList.size() && coordinates.getCols() == 3) {
            for (int i = 0; i < neuronList.size(); i++) {
                neuronList.get(i).setPosXYZ(coordinates.getVecRow(i));
            }
        }
    }

    /**
     * Creates new neurons and put them in the reservoir. The function avoids
     * repetition of neurons.
     *
     * @param coordinates
     * @throws CloneNotSupportedException
     */
    public void createReservoirNeurons(Matrix coordinates) throws CloneNotSupportedException {
        this.network.getReservoir().addAll(this.reservoirBuilder.getReservoirNeurons(this, coordinates));
//        for (int i = 0; i < coordinates.getRows(); i++) {
//            SpikingNeuron neuron = this.createNeuron(coordinates.getVecRow(i), this.network.getReservoir());
//            if (neuron != null) {
//                neuron.setNeuronType(NeuronType.RESERVOIR_NEURON);
//                this.network.getReservoir().add(neuron);
//            }
//        }
    }

    /**
     * Creates the set of positive input neurons which propagate the positive
     * spikes (set of ones) form a spike train.
     *
     * @param coordinates the n by 3 matrix that contains the X, Y and Z
     * coordinates of the input neurons.
     * @throws CloneNotSupportedException
     */
    public void createInputNeuronsPositive(Matrix coordinates) throws CloneNotSupportedException {
        for (int i = 0; i < coordinates.getRows(); i++) {
            SpikingNeuron neuron = this.createNeuron(coordinates.getVecRow(i), this.network.getInputNeuronsPositive());
            if (neuron != null) {
                neuron.setNeuronType(NeuronType.INPUT_NEURON_POSITIVE);
                this.network.getReservoir().add(neuron);
                this.network.getInputNeurons().add(neuron);
                this.network.getInputNeuronsPositive().add(neuron);
            }
        }
    }

    /**
     * Creates the set of negative input neurons which propagate the negative
     * spikes (set of negative ones) form a spike train.
     *
     * @param coordinates the n by 3 matrix that contains the X, Y and Z
     * coordinates of the input neurons.
     * @throws CloneNotSupportedException
     */
    public void createInputNeuronsNegative(Matrix coordinates) throws CloneNotSupportedException {
        for (int i = 0; i < coordinates.getRows(); i++) {
            SpikingNeuron neuron = this.createNeuron(coordinates.getVecRow(i), this.network.getInputNeuronsNegative());
            if (neuron != null) {
                neuron.setNeuronType(NeuronType.INPUT_NEURON_NEGATIVE);
                this.network.getReservoir().add(neuron);
                this.network.getInputNeurons().add(neuron);
                this.network.getInputNeuronsNegative().add(neuron);
            }
        }
    }

    /**
     * Creates the output layer of neurons. Every spiking neuron that is created
     * is also associated to a data sample. All the neurons in the reservoir are
     * connected to every neuron in the output layer. Every spiking neuron is
     * added to the list of output neurons of the network.
     *
     * @param samples The list of data samples that will be associated to the
     * output layer
     * @throws CloneNotSupportedException
     */
    public void createOutputNeurons(ArrayList<DataSample> samples) throws CloneNotSupportedException {
        for (int i = 0; i < samples.size(); i++) {
            this.createOutputNeuron(samples.get(i));
        }
    }

    /**
     * Creates the output layer of neurons. Every spiking neuron that is created
     * is also associated to a data sample. All the neurons in the reservoir are
     * connected to every neuron in the output layer. Every spiking neuron is
     * added to the list of output neurons of the network.
     *
     * @param samples The list of data samples that will be associated to the
     * output layer
     * @param coordinates spatial coordinates of the neuron
     * @throws CloneNotSupportedException
     */
    public void createOutputNeurons(ArrayList<DataSample> samples, Matrix coordinates) throws CloneNotSupportedException {
        if (samples.size() == coordinates.getRows()) {
            for (int i = 0; i < samples.size(); i++) {
                SpikingNeuron neuron = this.createOutputNeuron(samples.get(i), coordinates.getVecRow(i));
                this.network.getOutputNeurons().add(neuron);
            }
        }
    }

    /**
     * Creates an output spiking neuron
     * {@link #createOutputNeuron(jneucube.data.DataSample, double[])} and adds
     * it to the list of output neurons.
     *
     * @param sample The data sample that is associated to the output neuron.
     * @throws CloneNotSupportedException
     */
    public void createOutputNeuron(DataSample sample) throws CloneNotSupportedException {
        int posX = 0;
        if (!this.getNetwork().getOutputNeurons().isEmpty()) {
            posX = (int) this.getNetwork().getOutputNeurons().get(this.getNetwork().getOutputNeurons().size() - 1).getPosX() + 1;
        }
        SpikingNeuron outputNeuron = this.createOutputNeuron(sample, new double[]{posX, 0, 0});
        this.addNeuron(outputNeuron, this.getNetwork().getOutputNeurons()); // Add the new neuron to the list of output neurons
        //return outputNeuron;
    }

    /**
     * Creates a spiking neuron that is associated to a data sample and connects
     * it to all the neurons in the reservoir. The user should specify the
     * coordinates of the neuron. The output neurons should have different
     * coordinates otherwise no neuron will be created.
     *
     * @param sample The data sample that is associated to the output neuron.
     * @param coordinates The coordinates of the neuron.
     *
     * @return The output spiking neuron.
     * @throws CloneNotSupportedException
     */
    public SpikingNeuron createOutputNeuron(DataSample sample, double[] coordinates) throws CloneNotSupportedException {
        SpikingNeuron neuron = this.createNeuron(coordinates, this.network.getOutputNeurons());
        if (neuron != null) {
            neuron.setIdx(sample.getSampleId());
            neuron.setNeuronType(NeuronType.OUTPUT_NEURON);
            neuron.setClassId(sample.getClassId());

            // Creates the connections only with active neurons
            for (SpikingNeuron sourceNeuron : this.uniqueActiveNeurons.keySet()) {
                neuron.addInputSynapse(new Synapse(sourceNeuron, neuron, 0.0));
            }
//            // Creates the connection with all neurons in the reservoir
//            this.network.getReservoir().stream().forEach((sourceNeuron) -> {
//                neuron.addInputSynapse(new Synapse(sourceNeuron, neuron, 0.0));
//            });
        }
        return neuron;
    }

    /**
     * The {@code createOutputNeuronPrediction} function creates a new output
     * neuron that is connected to the neurons in the reservoir. Since this
     * function is utilised for output prediction by the
     * {@link  jneucube.cube.NeuCubeController#getPrediction(jneucube.data.DataSample, jneucube.util.Matrix, jneucube.util.Matrix)}
     * function after the supervised learning then a previous list of neurons
     * must be previously created. If no output neurons were found then the
     * function throws an exception that means that no supervised learning has
     * been performed or that the list of output neurons was cleaned.
     *
     * @param sampleId the sample identifier
     * @param classLabel the actual class label
     * @throws CloneNotSupportedException
     */
    public void createOutputNeuronPrediction(int sampleId, double classLabel) throws CloneNotSupportedException {
        int posX = 0;
        if (this.getNetwork().getOutputNeurons().isEmpty()) {
            LOGGER.error("The list of input neurons is empty.");
            throw new EmptyListOfNeuronsException("The list of output neurons is empty.");
        } else {
            posX = (int) this.getNetwork().getOutputNeurons().get(this.getNetwork().getOutputNeurons().size() - 1).getPosX() + 1;
        }
        SpikingNeuron temp = this.getNetwork().getOutputNeurons().get(0);
        SpikingNeuron outputNeuron = this.createNeuron(new double[]{posX, 0, 0}, this.network.getOutputNeurons());
        if (outputNeuron != null) {
            outputNeuron.setIdx(sampleId);
            outputNeuron.setNeuronType(NeuronType.OUTPUT_NEURON);
            outputNeuron.setClassId(classLabel);
            for (Synapse tempSynapse : temp.getInputSynapses()) {
                SpikingNeuron sourceNeuron = this.network.getReservoir().get(tempSynapse.getSourceNeuronIdx());
                outputNeuron.addInputSynapse(new Synapse(sourceNeuron, outputNeuron, 0.0));
            }
        }
        this.addNeuron(outputNeuron, this.getNetwork().getOutputNeurons()); // Add the new neuron to the list of output neurons
        //return outputNeuron;
    }

    /**
     * This function searches in the neuron list for a neuron located in the
     * specified coordinates. The procedure avoids repetition of coordinates so
     * if no neuron found, the function creates a new neuron and adds it to the
     * reservoir.
     *
     * @param coordinates the spatial coordinates where the neuron is located
     * @param neuronList the list of neurons for validating no repetitions
     * @return a unique neuron
     * @throws CloneNotSupportedException
     */
    public SpikingNeuron createNeuron(double[] coordinates, ArrayList<SpikingNeuron> neuronList) throws CloneNotSupportedException {
        if (this.getNeuron(coordinates, neuronList) == null) { // Searches and validates that the new neuron is not repeated
            try {
                return this.createNeuron(coordinates);
            } catch (CloneNotSupportedException e) {
                LOGGER.error(e.getMessage());
            }
        }
        return null;

    }

    /**
     * This function creates a new neuron using the neuron model previously
     * configured with a spatial location determined by the X, Y and Z
     * coordinates.
     *
     * @param coordinates the X,Y and Z coordinates
     * @return a new neuron
     * @throws CloneNotSupportedException
     */
    public SpikingNeuron createNeuron(double[] coordinates) throws CloneNotSupportedException {
        SpikingNeuron neuron = this.network.getSpikingNeuron().clone();
        neuron.setIdx(this.network.getReservoir().size()); // This instruction is very important. It mantains a unique identifier for each neuron
        neuron.setPosX(coordinates[0]);
        neuron.setPosY(coordinates[1]);
        neuron.setPosZ(coordinates[2]);
        neuron.setRegion(0);
        return neuron;
    }

    /**
     * Search for an existing neuron located in the same coordinates. The
     * function returns the found neuron otherwise null.
     *
     * @param coordinates An R3 array indicating the X,Y,Z coordinates
     * @param network A list containing the reservoir neurons
     * @return
     */
    public SpikingNeuron getNeuron(double[] coordinates, ArrayList<SpikingNeuron> network) {
        for (SpikingNeuron neuron : network) {
            if (neuron.getPosX() == coordinates[0] && neuron.getPosY() == coordinates[1] && neuron.getPosZ() == coordinates[2]) {
                return neuron;
            }
        }
        return null;
    }

    /**
     * Replicates n times the reservoir for a multivariate NeuCube (June 2016)
     *
     * @param times
     */
    public void replicateReservoir(int times) {
        int end = this.network.getReservoir().size();
        try {
            for (int t = 1; t < times; t++) {
                LOGGER.debug("   - Replicating network for variable " + (t + 1));
                for (int i = 0; i < end; i++) {
                    SpikingNeuron neuron = (SpikingNeuron) this.network.getReservoir().get(i).clone();
                    neuron.setIdx(this.network.getReservoir().size());
                    this.network.getReservoir().add(neuron);
                    if (neuron.getNeuronType() == NeuronType.INPUT_NEURON_POSITIVE) {
                        this.network.getInputNeurons().add(neuron);
                        this.network.getInputNeuronsPositive().add(neuron);
                    } else if (neuron.getNeuronType() == NeuronType.INPUT_NEURON_NEGATIVE) {
                        this.network.getInputNeurons().add(neuron);
                        this.network.getInputNeuronsNegative().add(neuron);
                    }
                }
                LOGGER.debug("   - Replicating complete ");
            }
        } catch (CloneNotSupportedException ex) {
            LOGGER.error(ex.toString());
        }
    }

    /**
     * This function replaces the core of each neuron in the reservoir by a
     * clone of the core indicated in the parameter
     *
     * @param neuronCore
     * @return
     */
    public boolean replaceReservoirCores(Core neuronCore) {
        try {
            replaceCores(neuronCore, this.network.getReservoir());
        } catch (CloneNotSupportedException ex) {
            LOGGER.error(ex.toString());
            return false;
        }
        return true;
    }

    /**
     * This function replaces the core of each neuron in the list of neurons by
     * a clone of the core indicated in the parameter
     *
     * @param coreModel
     * @param neuronList
     * @throws CloneNotSupportedException
     */
    public void replaceCores(Core coreModel, ArrayList<SpikingNeuron> neuronList) throws CloneNotSupportedException {
        for (SpikingNeuron neuron : neuronList) {
            neuron.setCore(coreModel.clone());
        }
    }

    /*
     * This function returns a list of fired neurons in the cuurent time.    
     * @param currentTime
     * @return 
     */
    public ArrayList<SpikingNeuron> getFiredNeurons(int currentTime) {
        ArrayList<SpikingNeuron> list = new ArrayList<>();
        for (SpikingNeuron neuron : this.network.getReservoir()) {
            if (neuron.isFiredFiringList(currentTime)) {
                list.add(neuron);
            }
        }
        return list;
    }

    /**
     * For visualization multiple variable cube
     *
     * @param beginIdx
     * @param endIdx
     * @param currentTime
     * @return
     */
    public ArrayList<SpikingNeuron> getFiredNeurons(int beginIdx, int endIdx, int currentTime) {
        ArrayList<SpikingNeuron> list = new ArrayList<>();
        for (SpikingNeuron neuron : this.network.getFiredNeurons()) {
            if (neuron.getIdx() >= beginIdx && neuron.getIdx() <= endIdx) {
                list.add(neuron);
            }
        }
        return list;
    }

    /**
     * This function creates the connectivity of the network according to the
     * specified connection algorithm. The algorithm creates a matrix of
     * connection and a matrix of weights. Then
     *
     * @param connectionAlgorithm
     */
    public void createConnections(ConnectionAlgorithm connectionAlgorithm) {
        LOGGER.info("Creating connection matrix");
        long processTime = System.nanoTime();
        int numNeurons = this.network.getReservoir().size() / this.network.getNumVariables();
        if (numNeurons > 0) {
            ArrayList<SpikingNeuron> originalCube = new ArrayList<>(this.network.getReservoir().subList(0, numNeurons));
            Matrix tempConnections = connectionAlgorithm.createConnections(originalCube);
            Matrix tempWeights = connectionAlgorithm.createWeights(originalCube, tempConnections);
            LOGGER.info("Complete (time " + ((System.nanoTime() - processTime) / 1000000) + ")");
            this.createConnections(tempConnections, tempWeights);
        } else {
            LOGGER.error(Messages.EMPTY_RESERVOIR.toString());
        }
    }

    /**
     * Creates the connectivity of the network by giving a file containing the
     * connections of the neurons and a file the contains the weights of those
     * connections. Both file should contain an n by n matrix, where n is the
     * number of neurons of the reservoir including the positive and negative
     * input neurons.
     *
     * @param connectionsFileName a csv file with a matrix that contains the
     * connections between neurons, 1 for excitatory connection, -1 inhibitory
     * connection, 0 no connection.
     * @param weightsFileName a csv file with a matrix that contains the weight
     * values of the connections
     * @throws java.io.IOException
     */
    public void createConnections(String connectionsFileName, String weightsFileName) throws IOException {
        Matrix tempConnections = new Matrix(connectionsFileName, ",");
        Matrix tempWeights = new Matrix(weightsFileName, ",");
        this.createConnections(tempConnections, tempWeights);
    }

    /**
     * Creates the connectivity of the networkController given a matrix of
     * connections and a matrix of weights. If the project uses a multi neucube,
     * this function replicates the connectivity to all cubes.
     *
     *
     * @param tempConnections A matrix containing the connections between
     * neurons 1 excitatory connection, -1 inhibitory connection, 0 no
     * connection
     * @param tempWeights A matrix containing the weight values of the
     * connections
     */
    public void createConnections(Matrix tempConnections, Matrix tempWeights) {
        LOGGER.info("Creating neuron connections ");
        long processTime = System.nanoTime();
        this.clearNetwork();
        Matrix connections = new Matrix(tempConnections.getData()); // 
        Matrix weights = new Matrix(tempWeights.getData());
        for (int i = 1; i < this.network.getNumVariables(); i++) { // Replicates the cube many times as the number of varibales in the data
            connections = connections.vertInsert(tempConnections);
            weights = weights.vertInsert(tempWeights);
        }
        this.createNetworkConnections(connections, weights);
        LOGGER.info("Complete (time " + ((System.nanoTime() - processTime) / 1000000) + ")");
    }

    /**
     * Creates the connections (synapses) among the neurons of the network given
     * a connection matrix and a connection of weights. If it is a multi
     * variable cube model, the function replicates the connections of the first
     * cube to the other. All the numbers different from zero (positive or
     * negative) in the connection matrix are considered as a connection.
     *
     * @param connections the connection matrix
     * @param weights the matrix of weights
     */
    private void createNetworkConnections(Matrix connections, Matrix weights) {
        LOGGER.info("Creating synapses");
        long processTime = System.nanoTime();
        SpikingNeuron preSynapticNeuron;    // source
        SpikingNeuron postSynapticNeuron;   // target
        int realIdx;
        int synapseIdx = 0;
        this.network.getSynapses().clear();
        int displaySynapseId = 0;
        int numNeurons = this.network.getReservoir().size() / this.network.getNumVariables();
        Random randomDelay = new Random();
        if (connections.getRows() == weights.getRows() && connections.getCols() == weights.getCols()) {
            for (int i = 0; i < connections.getRows(); i++) {
                preSynapticNeuron = this.network.getReservoir().get(i);

                if (i % numNeurons == 0) {
                    displaySynapseId = 0; // It resets the id that is used in the display (DisplayController) to match with the Xform (connectionXform) containing the synapses
                }
                for (int j = 0; j < connections.getCols(); j++) {
                    if (connections.get(i, j) != 0) {
                        realIdx = j + (numNeurons * (i / numNeurons));  // The real neuron id to be connected since the number of columns in the connection matrix is th enumber of neurons without replication
                        postSynapticNeuron = this.network.getReservoir().get(realIdx);

                        Synapse synapse = new Synapse(preSynapticNeuron, postSynapticNeuron, weights.get(i, j));
                        synapse.setDisplayIdx(displaySynapseId); // To control the 3D synapses position in the group of synapses 
                        synapse.setIdx(synapseIdx);
                        synapse.setType((int) connections.get(i, j));   // set excitatory or inhibitory connections 20170411
                        if (preSynapticNeuron.getTypeDelay() == SpikingNeuron.FIXED_DELAY) {
                            synapse.setDelay(preSynapticNeuron.getMaxDelay());    // All presynaptic connections have the same delay
                        } else {
                            synapse.setDelay(randomDelay.nextInt(preSynapticNeuron.getMaxDelay() - 1) + 1); // 
                        }

                        preSynapticNeuron.addOutputSynapse(synapse);
                        postSynapticNeuron.addInputSynapse(synapse);
                        this.network.getSynapses().add(synapse);
                        synapseIdx++;
                        displaySynapseId++;
                    }
                }
            }
            LOGGER.info("Complete total synapses: " + this.network.getNumSynapses() + " (time " + ((System.nanoTime() - processTime) / 1000000) + ")");
        } else {
            LOGGER.error("The matrices must contain the same number of rows and columns");
        }
    }

    /**
     * Returns a nxn matrix (n=number of neurons in the reservoir) that
     * indicates the type of connection (1 excitatory, -1 inhibitory, 0 no
     * connection) between two neurons in the reservoir.
     *
     * @return
     */
    public Matrix getConnectionMatrix() {
        int numNeurons = this.network.getReservoir().size();
        Matrix matrix = new Matrix(numNeurons, numNeurons, 0.0);
        this.network.getReservoir().stream().forEach((neuron) -> {
            neuron.getOutputSynapses().stream().forEach((synapse) -> {
                matrix.set(neuron.getIdx(), synapse.getTargetNeuronIdx(), 1.0);
            });
        });
        return matrix;
    }

    /**
     * Returns a nxn matrix, where m is the number of neurons in the reservoir,
     * that indicates the weight value of the connection between two neurons in
     * the reservoir.
     *
     * @return
     */
    public Matrix getWeightMatrix() {
        int numNeurons = this.network.getReservoir().size();
        Matrix matrix = new Matrix(numNeurons, numNeurons, 0.0);
        this.network.getReservoir().stream().forEach((neuron) -> {
            neuron.getOutputSynapses().stream().forEach((synapse) -> {
                matrix.set(neuron.getIdx(), synapse.getTargetNeuronIdx(), synapse.getWeight());
            });
        });
        return matrix;
    }

    /**
     * Returns a m-by-m matrix, where m is the number of neurons in the
     * reservoir, that indicates the weight value of the connection between two
     * neurons in the reservoir in a simulation a time point. Note that the
     * field
     * {@link jneucube.trainingAlgorithms.LearningAlgorithm#savingWeightMode} is
     * set to true to access the weights at a specific simulation time point. If
     * the field is set to false, the network will record the initial and final
     * weights after the training process. The set of weights can be accessed by
     * setting the parameter time to 0 or 1 for initial or final weights
     * respectively. and 1 respectively.
     *
     * @param time the stimulation time point
     * @return a matrix of weights
     */
    public Matrix getWeightMatrix(int time) {
        int numNeurons = this.network.getReservoir().size();
        Matrix matrix = new Matrix(numNeurons, numNeurons, 0.0);
        this.network.getReservoir().stream().forEach((neuron) -> {
            neuron.getOutputSynapses().stream().forEach((synapse) -> {
                matrix.set(neuron.getIdx(), synapse.getTargetNeuronIdx(), synapse.getWeights().get(time));
            });
        });
        return matrix;
    }

    /**
     * Returns an m-by-1 matrix, where m is the number of connections, that
     * contains the values of all synaptic weights.
     *
     * @return the m-by-1 matrix
     */
    public Matrix getCurrenttWeigths() {
        Matrix m = new Matrix(this.network.getNumSynapses(), 1, 0.0);
        for (int i = 0; i < network.getSynapses().size(); i++) {
            m.set(i, 0, network.getSynapses().get(i).getWeight());
        }
        return m;
    }

    /**
     * Returns an m-by-1 matrix, where m is the number of connections, that
     * contains the values of all synaptic weights in a simulation time point.
     * Note that the field
     * {@link jneucube.trainingAlgorithms.LearningAlgorithm#savingWeightMode} is
     * set to true to access the weights at a specific simulation time point. If
     * the field is set to false, the network will record the initial and final
     * weights after the training process. The set of weights can be accessed by
     * setting the parameter time to 0 or 1 for initial or final weights
     * respectively. and 1 respectively.
     *
     * @param time
     * @return the m-by-1 matrix
     */
    public Matrix getWeigths(int time) {
        Matrix m = new Matrix(this.network.getNumSynapses(), 1);
        for (int i = 0; i < network.getSynapses().size(); i++) {
            m.set(i, 0, network.getSynapses().get(i).getWeights().get(time));
        }
        return m;
    }

    /**
     * Gets the weight of all input synapsed connected to the presynaptic
     * neuron.
     *
     * @param neuron the postsynaptic neuron
     * @return a 1 by n matrix that contains the weight values
     */
    public Matrix getNeuronInputWeights(SpikingNeuron neuron) {
        Matrix matrix = new Matrix(1, neuron.getInputSynapses().size(), 0.0);
        for (int i = 0; i < neuron.getInputSynapses().size(); i++) {
            matrix.set(0, i, neuron.getInputSynapses().get(i).getWeight());
        }
        return matrix;
    }

    /**
     * Gets the weight of all output synapses connected to postsynaptic neurons.
     *
     * @param neuron the presynaptic neuron
     * @return a 1 by n matrix that contains the weight values
     */
    public Matrix getNeuronOutputWeights(SpikingNeuron neuron) {
        Matrix matrix = new Matrix(1, neuron.getOutputSynapses().size(), 0.0);
        for (int i = 0; i < neuron.getOutputSynapses().size(); i++) {
            matrix.set(0, i, neuron.getOutputSynapses().get(i).getWeight());
        }
        return matrix;
    }

    /**
     * This function clears the neuron and removes it from every list of neurons
     * of the network
     *
     * @param neuron
     */
    public void removeNeuron(SpikingNeuron neuron) {
        this.removeConnections(neuron);
        neuron.clear();
        this.network.getReservoir().remove(neuron);
        this.network.getInputNeuronsPositive().remove(neuron);
        this.network.getInputNeuronsNegative().remove(neuron);
        this.network.getInputNeurons().remove(neuron);
        this.network.getFiredNeurons().remove(neuron);
        this.network.getOutputNeurons().remove(neuron);
    }

    /**
     * Removes the connections from the presynaptic neurons and the connections
     * to postsynaptic neurons of the specified neuron.
     *
     * @param spikingNeuron the neuron connected from presynaptic and
     * postsynaptic neurons
     */
    public void removeConnections(SpikingNeuron spikingNeuron) {
        SpikingNeuron tempNeuron;
        for (Synapse synapse : spikingNeuron.getInputSynapses()) {
            tempNeuron = this.network.getReservoir().get(synapse.getSourceNeuronIdx());
            tempNeuron.getOutputSynapses().remove(synapse);
            synapse.reset();
            this.network.getSynapses().remove(synapse);
        }
        for (Synapse synapse : spikingNeuron.getOutputSynapses()) {
            tempNeuron = this.network.getReservoir().get(synapse.getTargetNeuronIdx());
            tempNeuron.getInputSynapses().remove(synapse);
            synapse.reset();
            this.network.getSynapses().remove(synapse);
        }
    }

    /**
     * This function removes every neuron and all it synapses from a list of
     * neurons, then it clears the list.
     *
     * @param neuronList the list of neurons
     */
    public void removeNeuronsFromList(ArrayList<SpikingNeuron> neuronList) {
        LOGGER.info("Removing list of neurons");
        this.clearNeuronsFromList(neuronList);
        neuronList.clear();
        LOGGER.info("Complete");        
    }

    /**
     * Removes all the elements of the network. This function should be called
     * before creating the neurons and their weights. It clears (resets the
     * neuron properties, removes connections, and clears the core) all the
     * neurons and removes them from thir corresponding list of neurons (output
     * neurons list, inputs neurons list and reservoir).
     */
    public void deleteNetwork() {
        this.removeNeuronsFromList(this.network.getOutputNeurons());
        this.removeNeuronsFromList(this.network.getInputNeuronsNegative());
        this.removeNeuronsFromList(this.network.getInputNeuronsPositive());
        this.removeNeuronsFromList(this.network.getInputNeurons());
        this.removeNeuronsFromList(this.network.getReservoir());
        this.network.getFiredNeurons().clear();
        this.network.getChangedWeights().clear();
        this.network.getSynapses().clear();
        this.network.setTrain(false);
    }

    /**
     * This function clears the neuron from a list of neurons.
     *
     * @see jneucube.spikingNeurons.SpikingNeuron#clear()} every neuron and all
     * it synapses from a list of neurons.
     *
     * @param neuronList the list of neurons
     */
    public void clearNeuronsFromList(ArrayList<SpikingNeuron> neuronList) {
        neuronList.forEach((neuron) -> {
            this.removeConnections(neuron);
            neuron.clear();
        });
    }

    /**
     * This function sets the neurons to their initial state and removes all the
     * synaptic connections. This function should be called before creating new
     * connections.
     */
    public void clearNetwork() {
        this.clearNeuronsFromList(this.network.getReservoir());
        this.removeNeuronsFromList(this.network.getOutputNeurons());

        this.network.getFiredNeurons().clear();
        this.network.getChangedWeights().clear();
        this.network.getSynapses().clear();
        this.network.setTrain(false);
    }

    /**
     * This function sets the proper conditions for a new training process. It
     * resets every neuron of the network (reservoir and output neurons)
     */
    public void resetNetworkForTraining() {
        LOGGER.info("Resetting network for training.");
        network.getReservoir().parallelStream().forEach((neuron) -> {
            neuron.reset();
        });
        network.getOutputNeurons().parallelStream().forEach((neuron) -> {
            neuron.reset();
        });
        this.network.getFiredNeurons().clear();
        this.network.getChangedWeights().clear();
        this.network.setTrain(false);
        LOGGER.info("Complete.");
    }

    /**
     * Resets the activity (number of spikes received and transmitted, and
     * clears the core) of all neurons in the reservoir {
     *
     * @see jneucube.spikingNeurons.SpikingNeuron#resetActivity()}. This
     * function should be utilized before stimulating the network without
     * changing the synaptic weights, e.g. after the unsupervised training and
     * before supervised training, or before calculating the branching parameter
     */
    public void resetNeuralActivity() {
        network.getReservoir().parallelStream().forEach((neuron) -> {
            neuron.resetActivity();
        });
    }

    /**
     * This function propagates spike trains and performs training using a
     * specified dataset. After training, the function adds the final weights
     * even if the field
     * {@link jneucube.trainingAlgorithms.LearningAlgorithm#savingWeightMode} of
     * the {@link jneucube.trainingAlgorithms.LearningAlgorithm} is set to
     * false.
     *
     * @param network
     * @param trainingData
     * @param learningAlgorithm
     * @param recordActiveNeurons records the neurons that emit a spike during
     * the simulation
     */
    public void train(Network network, ArrayList<DataSample> trainingData, LearningAlgorithm learningAlgorithm, boolean recordActiveNeurons) {
        int elapsedTime = 0;
        //Runtime.getRuntime().gc();
        double startTime = System.nanoTime();
        double processingTime;

        LOGGER.info("Training " + trainingData.size() + " samples using " + learningAlgorithm + " learning rule.");
        learningAlgorithm.resetFieldsForTraining();
        this.resetNeuralActivity();
        this.uniqueActiveNeurons.clear();
        for (int t = 0; t < learningAlgorithm.getTrainingRounds(); t++) {    // Number of training times           
            for (DataSample sample : trainingData) {   // For each training data sample  
                elapsedTime = this.trainSample(learningAlgorithm, sample, elapsedTime, recordActiveNeurons); // Propagates the data sample through the network                  
                //elapsedTime = this.trainSampleUnlimited(learningAlgorithm, sample, elapsedTime, trainingMode); // Propagates the data sample through the network                  
                //this.recordCurrentWeights();   // Save the connection state of the network after each sample
            }
            this.recordCurrentWeights();   // Save the last connection state of the network
        }

        learningAlgorithm.setTrainingTime(elapsedTime);
//        Runtime.getRuntime().gc();
        processingTime = (System.nanoTime() - startTime) / 1000000;   // milliseconds
//        this.setExecuted(true);
        LOGGER.info("Training complete (" + trainingData.size() + " samples in " + processingTime + " ms), active neurons=" + this.uniqueActiveNeurons.size());
    }

    /**
     * Adds the current weights into the list of weights.
     */
    public void recordCurrentWeights() {
        LOGGER.info("Adding current weights.");
        for (SpikingNeuron neuron : this.network.getReservoir()) {
            for (Synapse synapse : neuron.getOutputSynapses()) {
                synapse.addWeight(synapse.getWeight());
            }
        }
        LOGGER.info("Complete.");
    }

    /**
     * Update the status to record the firing activity of all neurons in the
     * reservoir.
     *
     * @param recordFiringActivity set to true for recoding the firing times of
     * all neurons in the reservoir
     */
    public void setRecordFiringActivity(boolean recordFiringActivity) {
        this.network.getReservoir().forEach((neuron) -> {
            neuron.getCore().setRecordFirings(recordFiringActivity);
        });
    }
    
    public void setRecordMembranePotential(boolean recordMembrnePotential) {
        this.network.getReservoir().forEach((neuron) -> {
            neuron.getCore().setRecordMembranePotential(recordMembrnePotential);
        });
    }

    /**
     * This function trains the spike trains of a sample through the SNN.
     * Depending on flag ,the algorithm network is trained or just stimulated.
     * Before propagation of the sample, this function resets the neuron: 1
     * resets the core (membrane potential, refractory time, set the fired
     * status fired=false, set time of the last spike lastSpike=0), 2 resets the
     * accumulated current=0, 3 set the neuron status stimulated=false, 4 set
     * the number of spikes received and emitted numSpikesReceived=0,
     * numSpikesEmitted=0.
     *
     *
     * @param learningAlgorithm the learning algorithm to use for training
     * @param sample the sample to train
     * @param elapsedTime The time elapsed since the beginning of the
     * stimulation process (from the first sample)
     * @param recordActiveNeurons records the neurons that emit a spike during
     * the simulation
     * @return the elapsed time after propagation of the sample
     */
    public int trainSample(LearningAlgorithm learningAlgorithm, DataSample sample, int elapsedTime, boolean recordActiveNeurons) {
        LOGGER.info("Training spike trains of sample " + sample.getSampleId() + " using " + learningAlgorithm.toString());
        double startTime = System.nanoTime();
        double processingTime;
        ArrayList<SpikingNeuron> firedNeurons;
        learningAlgorithm.resetFieldsForSample();   // Initializes the learning algorithm for a sample (e.g. the deSNNs)
        // Resets the accumulated current in the and sets the stimulated status to false.
        network.getReservoir().parallelStream().forEach((neuron) -> {
            neuron.resetCurrent();
        });

        for (int sampleTime = 0; sampleTime < sample.getSpikeData().getRows(); sampleTime++) {
            this.setInputSpikes(sample.getSpikeData().getVecRow(sampleTime), elapsedTime); // Set the input spike data into the input neurons and add the elapsed time to the neuron firing list                    
            //firedNeurons = spikePropagationDelays(delayPool, elapsedTime); // Propagates the information through the network and returns the fired neurons                                        
            firedNeurons = spikePropagation(elapsedTime, recordActiveNeurons); // Propagates the information through the network and returns the fired neurons
            learningAlgorithm.train(network, sample);
            LOGGER.debug("Elapsed time: " + elapsedTime + "; Sample time: " + sampleTime + "; Fired neurons: " + firedNeurons.size() + "; Weight changes " + learningAlgorithm.getWeightChanges());
            elapsedTime++;
        }

        processingTime = (System.nanoTime() - startTime) / 1000000;   // milliseconds        
        LOGGER.debug("Sample: " + sample.getSampleId() + " ; processing time: " + processingTime + " ; Sample weight changes:" + learningAlgorithm.getSampleWeightChanges());

        return elapsedTime;
    }

    /**
     * This function trains the spike trains of a sample through the SNN. If the
     * input spike train is finished and there are neurons or synapses pending
     * to propagate or release a spike, the algorithm continues the propagation
     * process by sending an empty vector. Depending on the flag "train" of the
     * network, the algorithm is trained or just stimulated.
     *
     * Before propagation, the function resets all the neurons in the reservoir.
     *
     * @param learningAlgorithm the learning algorithm to use for training
     * @param sample the sample to train
     * @param elapsedTime The time elapsed since the beginning of the
     * stimulation process (from the first sample)
     * @return the elapsed time after propagation of the sample
     */
    public int trainSampleUnlimited(LearningAlgorithm learningAlgorithm, DataSample sample, int elapsedTime) {
        double startTime = System.nanoTime();
        double processingTime;          // The time that consumes this process
        int sampleTime = 0;             // The elapsed time that the network is being stimulated with the data of the sample
        int stimulationTime = 0;          // The elapsed time that the network is being stimulated since this process started
        int numAncestors;               // The number of spiking neurons active at time t-1.
        double accumulatedRatio = 0.0;    // The branching parameter at time t. σ=descendants/ancestors  σ=firingNeuorns/numAncestors.

        ArrayList<SpikingNeuron> firedNeurons = new ArrayList<>();
        learningAlgorithm.resetFieldsForSample();   // Initializes the learning algorithm for a sample (e.g. the deSNNs)
        HashMap<Integer, ArrayList<Synapse>> delayPool = new HashMap<>(); // Maps the synapses that should release a spike in a specific time

        // Reseting neurons' core and the accumulated input current given by the synaptic weights. Sets action potentials to zero among other features according to the neuron model, eg. in SLIF the refractory time and the membrne potential are set to zero
        network.getReservoir().parallelStream().forEach((neuron) -> {
            neuron.resetCurrent();
        });

        double[] emptySpikeVector = new double[sample.getSpikeData().getCols()];
        for (int i = 0; i < emptySpikeVector.length; i++) {
            emptySpikeVector[i] = 0.0;
        }
        // Continues the propagation of the spikes if there are firing neurons that should propagate a spike, or there are synapes that should release a spike
        while (sampleTime < sample.getSpikeData().getRows() || !firedNeurons.isEmpty() || !delayPool.isEmpty()) {
            numAncestors = firedNeurons.size(); // Active spiking neurons at time t-1
            if (sampleTime < sample.getSpikeData().getRows()) {
                this.setInputSpikes(sample.getSpikeData().getVecRow(sampleTime), elapsedTime); // Set the input spike data into the input neurons and add the elapsed time to the neuron firing list                    
                sampleTime++;
            } else {
                this.setInputSpikes(emptySpikeVector, elapsedTime);   // Set the input spike data into the input neurons and add the elapsed time to the neuron firing list
            }
            firedNeurons = spikePropagationDelays(delayPool, elapsedTime, false); // Propagates the information through the network and returns the fired neurons                                        

            // Calculation of the branching parameter ratio
            if (numAncestors != 0) {
                accumulatedRatio += (firedNeurons.size() * 1.0) / numAncestors; // descendants/ancestors
            }

            learningAlgorithm.train(network, sample);

            stimulationTime++;
            elapsedTime++;
            LOGGER.debug("Elapsed time " + elapsedTime + " Time " + stimulationTime + " Fired " + firedNeurons.size() + " Weight changes " + learningAlgorithm.getWeightChanges() + " delay pool " + delayPool.size());
        }
        //branchingParameter = accumulatedRatio / numBranchingValues;
        processingTime = (System.nanoTime() - startTime) / 1000000;   // milliseconds
        LOGGER.debug("Sample " + sample.getSampleId() + " " + processingTime);
        return elapsedTime;
    }

    /**
     * This function propagates the spike trains of a sample through the SNN.
     * For details see the {@link #propagateSample(jneucube.data.DataSample, int, boolean) function.
     * The parameter delays is set to false.
     *
     * @param sample the sample to train
     * @param elapsedTime The time elapsed since the beginning of the
     * stimulation process (from the first sample)
     * @return the elapsed time plus the time stpes for propagating the sample
     */
    public int propagateSample(DataSample sample, int elapsedTime) {
        return this.propagateSample(sample, elapsedTime, false);
    }

    /**
     * This function propagates the spike trains of a sample through the SNN
     * using synaptic delays. For details see the {@link #propagateSample(jneucube.data.DataSample, int, boolean) function.
     * The parameter delays is set to true.
     *
     * @param sample the sample to train
     * @param elapsedTime The time elapsed since the beginning of the
     * stimulation process (from the first sample)
     * @return the elapsed time plus the time steps for propagating the sample
     */
    public int propagateSampleWithDelays(DataSample sample, int elapsedTime) {
        return this.propagateSample(sample, elapsedTime, true);
    }

    /**
     * This function propagates the spike trains of a sample through the SNN. It
     * executes the following steps: 1 resets the current of all neurons in the
     * reservoir. 2 For each sample a) set the input spikes into the network, b)
     * detects the number of ancestors (i.e. neurons that fired at time t-1), c)
     * if the parameter delays is set to true then it executes the propagation
     * using synaptic delays (see {@link #spikePropagationDelays(java.util.HashMap, int)),
     * otherwise it propagates the spikes by calling the {@link #spikePropagation(int) function.
     * d) calculates the branching parameter, i.e the ratio of the number of
     * fired neurons at time t divide to the number of fired neurons at
     * time t-1 (ancestors).
     *
     * @param sample the sample to train
     * @param elapsedTime The time elapsed since the beginning of the
     * stimulation process (from the first sample)
     * @param delays true if the propagation will use synaptic delays, otherwise
     * false
     * @return the elapsed time plus the time steps for propagating the sample
     */
    private int propagateSample(DataSample sample, int elapsedTime, boolean delays) {
        LOGGER.info("Propagating spike trains of sample " + sample.getSampleId());
        double startTime = System.nanoTime();
        double processingTime;
        int numAncestors;                   // The number of spiking neurons active at time t-1.
        double accumulatedRatio = 0.0;      // The branching parameter at time t. σ=descendants/ancestors  σ=firingNeuorns/numAncestors.
        ArrayList<SpikingNeuron> firedNeurons = new ArrayList<>();

        HashMap<Integer, ArrayList<Synapse>> delayPool = new HashMap<>(); // Maps the synapses that should release a spike in a specific time
        // Resets the accumulated current in the and sets the stimulated status to false.
        network.getReservoir().parallelStream().forEach((neuron) -> {
            neuron.resetCurrent();
        });

//        this.uniqueActiveNeurons.clear();  // Clear the list of the neurons that fire during the unsupervised learning                
        // Propagating the spike trains and adapting synapses
        for (int sampleTime = 0; sampleTime < sample.getSpikeData().getRows(); sampleTime++) {
            this.setInputSpikes(sample.getSpikeData().getVecRow(sampleTime), elapsedTime); // Set the input spike data into the input neurons and add the elapsed time to the neuron firing list                    
            numAncestors = firedNeurons.size(); // Active spiking neurons at time t-1
            if (delays) {
                firedNeurons = spikePropagationDelays(delayPool, elapsedTime, true); // Propagates the information through the network and returns the fired neurons                                        
            } else {
                firedNeurons = spikePropagation(elapsedTime, true); // Propagates the information through the network and returns the fired neurons
            }

            if (numAncestors != 0) {
                accumulatedRatio += (firedNeurons.size() * 1.0) / numAncestors; // descendants/ancestors
            }
            LOGGER.debug("Elapsed time " + elapsedTime + " Time " + sampleTime + " Fired " + firedNeurons.size());
            elapsedTime++;
        }
//        System.out.println(this.uniqueActiveNeurons.size());
//        for (SpikingNeuron neuron : this.uniqueActiveNeurons.keySet()) {
//            System.out.print(neuron.getIdx()+" " );
//        }
//        System.out.println("");

        processingTime = (System.nanoTime() - startTime) / 1000000;   // milliseconds
        LOGGER.debug("Sample " + sample.getSampleId() + " processing time " + processingTime);
        return elapsedTime;
    }

    /**
     * This function trains the spike trains of a sample through the SNN. If the
     * input spike train is finished and there are neurons or synapses pending
     * to propagate or release a spike, the algorithm continues the propagation
     * process by sending an empty vector. For details see the {@link #propagateSampleUnlimited(jneucube.data.DataSample, int, boolean) function.
     *
     * @param sample the sample to train
     * @param elapsedTime The time elapsed since the beginning of the
     * stimulation process (from the first sample)
     * @return the elapsed time plus the time steps for propagating the sample
     */
    public int propagateSampleUnlimited(DataSample sample, int elapsedTime) {
        return this.propagateSampleUnlimited(sample, elapsedTime, false);
    }

    /**
     * This function trains the spike trains of a sample through the SNN using
     * synaptic delays. If the input spike train is finished and there are
     * neurons or synapses pending to propagate or release a spike, the
     * algorithm continues the propagation process by sending an empty vector.
     * For details see the {@link #propagateSampleUnlimited(jneucube.data.DataSample, int, boolean) function.
     *
     * @param sample the sample to train
     * @param elapsedTime The time elapsed since the beginning of the
     * stimulation process (from the first sample)
     * @return the elapsed time plus the time steps for propagating the sample
     */
    public int propagateSampleUnlimitedWithDelays(DataSample sample, int elapsedTime) {
        return this.propagateSample(sample, elapsedTime, false);
    }

    /**
     * This function trains the spike trains of a sample through the SNN. If the
     * input spike train is finished and there are neurons or synapses pending
     * to propagate or release a spike, the algorithm continues the propagation
     * process by sending an empty vector. The function executes the following
     * steps: 1 resets the current of all neurons in the reservoir. 2 creates an
     * empty vector which size is equals to the number of temporal features.
     * This vector is utilised to send zeros to the SNN after finishing to
     * propagate the data of the sample and there are neurons or synapses
     * pending to propagate or release a spike. 3 While there is information in
     * the SNN for propagating the function executes a) calculates the number of
     * fired neurons at time t-1 (ancestors); b) set the input spike train from
     * the data sample if the function has not propagated all the data otherwise
     * it will set the empty vector as input of the SNN. c) if the parameter
     * delays is set to true then it executes the propagation using synaptic
     * delays (see {@link #spikePropagationDelays(java.util.HashMap, int)),
     * otherwise it propagates the spikes by calling the {@link #spikePropagation(int) function.
     * d) calculates the branching parameter, i.e the ratio of the number of
     * fired neurons at time t divide to the number of fired neurons at time t-1 (ancestors).
     *
     * @param sample the sample to train
     * @param elapsedTime The time elapsed since the beginning of the
     * stimulation process (from the first sample)
     * @param delays true if the propagation will use synaptic delays, otherwise
     * false
     * @return the elapsed time plus the time steps for propagating the sample
     */
    private int propagateSampleUnlimited(DataSample sample, int elapsedTime, boolean delays) {
        LOGGER.info("Propagating spike trains of sample " + sample.getSampleId() + " using synaptic delays.");
        double startTime = System.nanoTime();
        double processingTime;          // The time that consumes this process
        int sampleTime = 0;             // The elapsed time that the network is being stimulated with the data of the sample
        int stimulationTime = 0;          // The elapsed time that the network is being stimulated since this process started
        int numAncestors;               // The number of spiking neurons active at time t-1.
        double accumulatedRatio = 0.0;    // The branching parameter at time t. σ=descendants/ancestors  σ=firingNeuorns/numAncestors.

        ArrayList<SpikingNeuron> firedNeurons = new ArrayList<>();
        HashMap<Integer, ArrayList<Synapse>> delayPool = new HashMap<>(); // Maps the synapses that should release a spike in a specific time

        // Reseting neurons' core and the accumulated input current given by the synaptic weights. Sets action potentials to zero among other features according to the neuron model, eg. in SLIF the refractory time and the membrne potential are set to zero
        network.getReservoir().parallelStream().forEach((neuron) -> {
            neuron.resetCurrent();
        });

        double[] emptySpikeVector = new double[sample.getSpikeData().getCols()];
        for (int i = 0; i < emptySpikeVector.length; i++) {
            emptySpikeVector[i] = 0.0;
        } // Continues the propagation of the spikes if there are firing neurons that should propagate a spike, or there are synapes that should release a spike
        while (sampleTime < sample.getSpikeData().getRows() || !firedNeurons.isEmpty() || !delayPool.isEmpty()) {
            numAncestors = firedNeurons.size(); // Active spiking neurons at time t-1
            if (sampleTime < sample.getSpikeData().getRows()) {
                this.setInputSpikes(sample.getSpikeData().getVecRow(sampleTime), elapsedTime); // Set the input spike data into the input neurons and add the elapsed time to the neuron firing list                    
                sampleTime++;
            } else {
                this.setInputSpikes(emptySpikeVector, elapsedTime);   // Set the input spike data into the input neurons and add the elapsed time to the neuron firing list
            }
            if (delays) {
                firedNeurons = spikePropagationDelays(delayPool, elapsedTime, true); // Propagates the information through the network and returns the fired neurons                                        
            } else {
                firedNeurons = spikePropagation(elapsedTime, true); // Propagates the information through the network and returns the fired neurons
            }
            // Calculation of the branching parameter ratio
            if (numAncestors != 0) {
                accumulatedRatio += (firedNeurons.size() * 1.0) / numAncestors; // descendants/ancestors
            }
            stimulationTime++;
            elapsedTime++;
            LOGGER.debug("Elapsed time " + elapsedTime + " Time " + stimulationTime + " Fired " + firedNeurons.size() + " delay pool " + delayPool.size());
        }
        //branchingParameter = accumulatedRatio / numBranchingValues;
        processingTime = (System.nanoTime() - startTime) / 1000000;   // milliseconds
        LOGGER.debug("Sample " + sample.getSampleId() + " " + processingTime);
        return elapsedTime;
    }

    /**
     * Set the input spike data into the input neurons. This step is crucial
     * during the propagation of spike trains.
     *
     * @param spikes The input data to be propagated. Every element of the array
     * correspond one data point of a feature.
     * @param elapsedTime The training time
     */
    public void setInputSpikes(double[] spikes, int elapsedTime) {
        int neuronIdx;
        int cubeId;
        int inputId;
        for (int col = 0; col < this.network.getInputNeuronsPositive().size(); col++) { // Inserts the spike trains into the input neurons
            inputId = col / this.network.getNumVariables();
            cubeId = col % this.network.getNumVariables();
            neuronIdx = this.network.getNumInputs() * cubeId + inputId; // Same index for the positive and negative input neurons in the list
            this.network.getInputNeuronsPositive().get(neuronIdx).getCore().setFired(false);
            if (spikes[col] == 1) {
                this.network.getInputNeuronsPositive().get(neuronIdx).getCore().setFired(true);
                this.network.getInputNeuronsPositive().get(neuronIdx).getCore().getFirings().add((double) elapsedTime);
                this.network.getInputNeuronsPositive().get(neuronIdx).getCore().setLastSpikeTime(elapsedTime);
            }
            if (!this.network.getInputNeuronsNegative().isEmpty()) { // if the network works with negative (inhibitory) input neurons
                this.network.getInputNeuronsNegative().get(neuronIdx).getCore().setFired(false);
                if (spikes[col] == -1) {
                    this.network.getInputNeuronsNegative().get(neuronIdx).getCore().setFired(true);
                    this.network.getInputNeuronsNegative().get(neuronIdx).getCore().getFirings().add((double) elapsedTime);
                    this.network.getInputNeuronsNegative().get(neuronIdx).getCore().setLastSpikeTime(elapsedTime);
                }
            }
        }
    }

    /**
     * Propagates the information through the reservoir. The returned fired
     * neuron list is used in the synapse adaptation process (STDP without
     * saving the weights) for faster computations.
     *
     * @param elapsedTime The training time
     * @param recordActiveNeurons record the active neurons during the
     * propagation of data
     * @return The fired neurons
     */
    public ArrayList<SpikingNeuron> spikePropagation(int elapsedTime, boolean recordActiveNeurons) {
        synchronized (this.network.getFiredNeurons()) {
            this.network.getFiredNeurons().clear();
            for (SpikingNeuron neuron : this.network.getReservoir()) {
                if (neuron.getNeuronType() == NeuronType.RESERVOIR_NEURON || neuron.getNeuronType() == NeuronType.OUTPUT_NEURON) {
                    neuron.computeMembranePotential(elapsedTime);
                }
                if (neuron.isFired()) {
                    this.network.getFiredNeurons().add(neuron);   // this list is used for the next data point along with the input neurons                                    
                }
                //System.out.println(neuron.getIdx()+"  "+neuron.isFired()+"  "+neuron.getMembranePotential());
            }
            for (SpikingNeuron firedNeuron : this.network.getFiredNeurons()) {   // Propagate all spikes in the network
                firedNeuron.propagateSpike(this.network.getReservoir());
                if (recordActiveNeurons) {
                    uniqueActiveNeurons.put(firedNeuron, firedNeuron.getNumSpikesEmitted()); //adds the fired neuron to a unique list of active neurons
                }

            }
        }
        return this.network.getFiredNeurons();
    }

    /**
     * Propagates the information through the reservoir using synaptic delays.
     *
     * @param delayPool
     * @param elapsedTime
     * @return
     */
    public ArrayList<SpikingNeuron> spikePropagationDelays(HashMap<Integer, ArrayList<Synapse>> delayPool, int elapsedTime, boolean recordActiveNeurons) {
        synchronized (this.network.getFiredNeurons()) {
            ArrayList<Synapse> stimulatedSynapses;

            this.network.getFiredNeurons().clear();
            for (SpikingNeuron neuron : network.getReservoir()) {
                if (neuron.getNeuronType() == NeuronType.RESERVOIR_NEURON) {
                    neuron.computeMembranePotential(elapsedTime);   //
                }
                if (neuron.isFired()) {
                    this.network.getFiredNeurons().add(neuron);   // this list is used for the next data point along with the input neurons                            
                    neuron.increaseSpikesEmitted();
                    if (recordActiveNeurons) {
                        uniqueActiveNeurons.put(neuron, neuron.getNumSpikesEmitted()); //adds the fired neuron to a unique list of active neurons
                    }

                    // Stores the time that the synapses will release a spike depending on their delays
                    for (Synapse synapse : neuron.getOutputSynapses()) {
                        synapse.addStimuli();
                        if (delayPool.containsKey(elapsedTime + synapse.getDelay())) {
                            delayPool.get(elapsedTime + synapse.getDelay()).add(synapse);
                        } else {
                            ArrayList<Synapse> synapseList = new ArrayList<>();
                            synapseList.add(synapse);
                            delayPool.put(elapsedTime + synapse.getDelay(), synapseList);
                        }
                    }
                }
            }

            // Releasing the spike from the synapses so that the postsynaptic neurons can calculate their membrane potential in the next time step
            stimulatedSynapses = delayPool.get(elapsedTime + 1);
            if (stimulatedSynapses != null) {
                for (Synapse synapse : stimulatedSynapses) {
                    synapse.releaseSpike(network.getReservoir());    // The target neuron (postsynaptic neuron) receives the spike, updates the amount of current released, increases the number of spikes received and set its status as stimulated=true
                }
                delayPool.get(elapsedTime + 1).clear();
                delayPool.remove(elapsedTime + 1);
            }
        }
        return this.network.getFiredNeurons();
    }

    /**
     * This function obtains the firing times of all neurons in the reservoir,
     * which also includes input neurons.
     *
     * @return an m-by-n matrix
     */
    public Matrix getFiringActivity() {
        Matrix m = new Matrix();
        double[][] firingActivity = new double[this.network.getReservoir().size()][];
        double[] firings;
        SpikingNeuron neuron;
        for (int i = 0; i < this.network.getReservoir().size(); i++) {
            neuron = this.network.getReservoir().get(i);
            Double[] boxed = neuron.getFirings().stream().toArray(Double[]::new);
            if (boxed.length != 0) {
                firings = Stream.of(boxed).mapToDouble(Double::doubleValue).toArray();
            } else {
                firings = new double[1];
            }
            firingActivity[i] = firings;
        }
        m.setData(firingActivity);

        return m;
    }

    /**
     * ***********************************
     * // R E M O V E ***********************************
     */
    /**
     * Propagates the information through the reservoir. The returned fired
     * neuron list is used in the synapse adaptation process (STDP without
     * saving the weights) for faster computations.
     *
     * @param elapsedTime The training time
     * @return The fired neurons
     */
    public ArrayList<SpikingNeuron> stimulateNetwork(int elapsedTime) {
        synchronized (this.network.getFiredNeurons()) {
            this.network.getFiredNeurons().clear();
            for (SpikingNeuron neuron : this.network.getReservoir()) {
                if (neuron.getNeuronType() == NeuronType.RESERVOIR_NEURON) {
                    neuron.computeMembranePotential(elapsedTime);
                }
                if (neuron.isFired()) {
                    this.network.getFiredNeurons().add(neuron);   // this list is used for the next data point along with the input neurons
                }
                //System.out.println(neuron.getIdx()+"  "+neuron.isFired()+"  "+neuron.getMembranePotential());
            }
            for (SpikingNeuron firedNeuron : this.network.getFiredNeurons()) {   // Propagate all spikes in the network
                firedNeuron.propagateSpike(this.network.getReservoir());
            }
        }
        return this.network.getFiredNeurons();
    }

    /**
     * This function only propagates the samples into the NeuCube as a feed
     * forward SNN and calculates the branching parameter (measure of
     * stability). Before the process starts, the firing activity of all neurons
     * is removed.
     *
     * @param data the list of samples that contain the input spike trains
     * @param numTrainingRounds number of times that the networks is stimulated
     * with the data
     * @return the branching parameter value
     */
    public double getBranchingParameter(ArrayList<DataSample> data, int numTrainingRounds) {
        int elapsedTime = 0;
        ArrayList<SpikingNeuron> firedNeurons;
        Runtime.getRuntime().gc();
        double startTime;
        int sampleId = 1;
        double duration = 0;
        double branchingParameter = 0;
        double accumulatedRatio = 0.0;
        int numBranchingValues = 0;
        int prevFiringNeurons = 0;

        // Removes all firings (firing times) and sets to zero the last spike time, the number of spikes received and the number of spikes emitted.                
        this.resetNeuralActivity();

        LOGGER.info("------- Propagating data and computing branching parameter -------");
        for (int t = 0; t < numTrainingRounds; t++) {    // Number of training times           
            for (DataSample sample : data) {   // For each training data sample  
                startTime = System.nanoTime();
//                System.out.println("Sample " + sampleId);
                network.getReservoir().parallelStream().forEach((neuron) -> {
                    neuron.resetCurrent();
                });

                for (int sampleTime = 0; sampleTime < sample.getSpikeData().getRows(); sampleTime++) {
                    this.setInputSpikes(sample.getSpikeData().getVecRow(sampleTime), elapsedTime);   // Set the input spike data into the input neurons and add the elapsed time to the neuron firing list
                    firedNeurons = this.stimulateNetwork(elapsedTime); // Propagates the information through the network and returns the fired neurons                    
                    if (firedNeurons.size() > 0) {
                        accumulatedRatio += ((prevFiringNeurons * 1.0) / firedNeurons.size()); // descendants/ancestors
                        numBranchingValues++;
                    }
                    prevFiringNeurons = firedNeurons.size();
                    LOGGER.debug("Elapsed time " + elapsedTime + " Time " + sampleTime + " Fired " + firedNeurons.size());
                    elapsedTime++;
                }
                duration = (System.nanoTime() - startTime) / 1000000;   // milliseconds
                sampleId++;
                LOGGER.debug("Sample " + sampleId);
            }
        }
        branchingParameter = accumulatedRatio / numBranchingValues;
        LOGGER.info("------- Branching parameter complete " + duration + " ms -------");
        return branchingParameter;
    }

    /**
     * Returns a spiking neuron object given a 33D coordinate. This function is
     * utilized for images processing
     *
     * @param posX
     * @param posY
     * @param posZ
     * @return
     */
    public SpikingNeuron getSpikingNeuron(int posX, int posY, int posZ) {
        int idx = (this.network.getNumNeuronsX() * this.network.getNumNeuronsY() * posZ) + ((this.network.getNumNeuronsY() * (posX)) + posY);
        return this.network.getReservoir().get(idx);
    }

    /**
     * @return the network
     */
    public Network getNetwork() {
        return network;
    }

    /**
     * @param network the network to set
     */
    public void setNetwork(Network network) {
        this.network = network;
    }

    /**
     * Adds a neuron to the specified list of neurons.
     *
     * @param neuron the neuron to add
     * @param neuronList the list of neurons
     */
    public void addNeuron(SpikingNeuron neuron, ArrayList<SpikingNeuron> neuronList) {
        neuronList.add(neuron);
    }

    /**
     * Removes a neuron from the specified list of neurons.
     *
     * @param neuron the neuron to be removed
     * @param neuronList the list of neurons
     */
    public void removeNeuron(SpikingNeuron neuron, ArrayList<SpikingNeuron> neuronList) {
        neuronList.remove(neuron);
    }

    /**
     * Removes a neuron from the specified list of neurons.
     *
     * @param index the index of the element to be removed
     * @param neuronList the list of neurons
     */
    public void removeNeuron(int index, ArrayList<SpikingNeuron> neuronList) {
        neuronList.remove(index);
    }

    /**
     * This function creates an m by n matrix that contains the m presynaptic
     * weights of the n output neurons, which are related to the training
     * samples. The matrix is utilized to classify a data sample.
     *
     * @return the matrix
     */
    public Matrix getTrainingMatrix() {
        int numOutputNeurons = this.getNetwork().getOutputNeurons().size();
        int numSynapses = this.getNetwork().getOutputNeurons().get(0).getInputSynapses().size();
        Matrix training = new Matrix(numOutputNeurons, numSynapses);
        for (int i = 0; i < this.getNetwork().getOutputNeurons().size(); i++) {
            SpikingNeuron temp = this.getNetwork().getOutputNeurons().get(i);
            // System.out.println(temp.getInputSynapses().size());
            training.setRow(i, this.getNeuronInputWeights(temp).getVecRow(0));
        }
        return training;
    }

    /**
     * This function creates an n by 1 matrix that contains the class of the
     * output neurons, which are related to the training samples. The matrix is
     * utilized to classify a data sample whose distinct values define the
     * classes of the Training Matrix.
     *
     * @return the matrix with the classes
     */
    public Matrix getClassMatrix() {
        int numOutputNeurons = this.getNetwork().getOutputNeurons().size();
        Matrix classes = new Matrix(numOutputNeurons, 1);
        // The training set
        for (int i = 0; i < this.getNetwork().getOutputNeurons().size(); i++) {
            SpikingNeuron temp = this.getNetwork().getOutputNeurons().get(i);
            classes.set(i, 0, temp.getClassId());
        }
        return classes;
    }

    /**
     * This function propagates the samples of a dataset through the SNN. If the
     * dataset is empty, it throws IllegalArgumentException. Before propagation
     * it resets the previous neural activity (see
     * {@link #resetNeuralActivity()}), then, for each data sample it calls the {@link #propagateSample(jneucube.data.DataSample, boolean, boolean) function.
     *
     * @param dataset The set of samples to propagate
     * @param unlimited true if it is required to propagate not only the
     * information in the data sample but all the information in the SNN.
     * @param delays true if it is required to use synaptic delays
     * @return the time steps for propagating the dataset.
     */
    public int propagateDataset(ArrayList<DataSample> dataset, boolean unlimited, boolean delays) {
        LOGGER.info("Propagating dataset ");
        long processTime = System.nanoTime();
        int timeSteps = 0;
        if (dataset.isEmpty()) {
            throw new IllegalArgumentException("The dataset is empty.");
        }

        if (!dataset.isEmpty()) {
            this.resetNeuralActivity(); // this.resetNumberOfSpikes();     this.resetCurrent();    this.core.clear();              
            this.uniqueActiveNeurons.clear();  // Clear the list of the neurons that fired in a previous data propagation
            for (DataSample dataSample : dataset) {
                timeSteps = this.propagateSample(dataSample, timeSteps, unlimited, delays);
            }
            LOGGER.info("Complete  (" + dataset.size() + " samples in " + ((System.nanoTime() - processTime) / 1000000) + "ms) active neurons=" + this.uniqueActiveNeurons.size() + " ");
            return timeSteps;
        } else {
            LOGGER.error(Messages.DATA_SET_EMPTY.toString());
        }
        return 0;
    }

    /**
     * This function propagates the data sample into the SNN. Depending on the
     * parameters unlimited and delays the function will propagate information
     * in different modalities. If it is required to propagate just the data in
     * the sample set the unlimited=false, otherwise, if it is required to
     * continue the process after all data in the sample has been propagated but
     * there are pending neurons or synapses to release spikes set
     * unlimited=true. If it is required to use synaptic delays set delays=true,
     * otherwise set to delays=false. For more detail see
     * {@link jneucube.network.NetworkController#propagateSample(jneucube.data.DataSample, int)}.
     * null null null null null null null null null null null null null     {@link jneucube.network.NetworkController#propagateSampleUnlimited(jneucube.data.DataSample, int)},     
     * {@link jneucube.network.NetworkController#propagateSampleWithDelays(jneucube.data.DataSample, int)}
     * {@link jneucube.network.NetworkController#propagateSampleUnlimitedWithDelays(jneucube.data.DataSample, int)}
     *
     * @param dataSample the data sample to propagate through the SNN
     * @param unlimited true if it is required to propagate not only the
     * information in the data sample but all the information in the SNN.
     * @param delays true if it is required to use synaptic delays
     * @return the time steps for propagating the sample
     */
    public int propagateSample(DataSample dataSample, int timeSteps, boolean unlimited, boolean delays) {
        if (dataSample != null) {
            if (unlimited) {
                if (delays) {
                    return this.propagateSampleUnlimitedWithDelays(dataSample, timeSteps);
                } else {
                    return this.propagateSampleUnlimited(dataSample, timeSteps);
                }
            } else {
                if (delays) {
                    return this.propagateSampleWithDelays(dataSample, timeSteps);
                } else {
                    return this.propagateSample(dataSample, timeSteps);
                }
            }
        } else {
            LOGGER.error(Messages.DATA_SET_EMPTY.toString());
        }
        return 0;
    }

    /**
     * Sets the weights of the network to the last state of the network, e.g.,
     * after training.
     */
    public void setNetworkLastState() {
        Synapse synapse;
        // 0 Seting the last weights
        for (int i = 0; i < this.network.getSynapses().size(); i++) {
            synapse = this.network.getSynapses().get(i);
            synapse.setWeight(synapse.getWeights().get(synapse.getWeights().size() - 1));
        }
    }

    /**
     * This function executes pruning of the SNN giving a class label. It will
     * remove all the unnecessary neurons and connections that are not involved
     * in a classification task. It is recommended to perform pruning from a
     * pre-loaded NeuCube model. This function executes the following steps: 1
     * Propagate the spike trains. 2 Put all fired neurons into one unique list.
     * 3 Remove all the connections from the useless neurons. 4 Remove the
     * useless neurons (retain useless ones) from the SNN. 5 Re-index the
     * synapses. 6 Create new indexes for the unique neurons. 7 Set the new pre
     * and post synaptic neuron indexes. 8 Re-index the neurons.
     *
     * @param dataset The set of samples to propagate
     * @param unlimited if the simulation is unlimited (i.e. if continues until
     * no activity remain in the SNN)
     * @param delays if the simulation includes synaptic delays
     */
    public void pruneInactiveNeurons(ArrayList<DataSample> dataset, boolean unlimited, boolean delays) {
        LOGGER.info("Prunning innactive neurons.");
        long processTime = System.nanoTime();
        int initialNeurons = this.network.getReservoir().size();

        Synapse synapse;
        // 0 Seting the last weights
        this.setNetworkLastState();

        // 1 Propagate the spike trains
        this.propagateDataset(dataset, unlimited, delays);

        // 2 Put all fired neurons into one unique list
        SpikingNeuron neuron;

        // 3 Remove all the connections from the useless neurons
        for (SpikingNeuron rNeuron : this.network.getReservoir()) {
            if (!this.uniqueActiveNeurons.containsKey(rNeuron)) {
                this.removeConnections(rNeuron);
            }
        }
        // 4 Remove the useless neurons (retain useless ones) from the SNN.
        this.network.getReservoir().retainAll(this.uniqueActiveNeurons.keySet());

        // 5 Re-indexing the synapses
        for (int i = 0; i < this.network.getSynapses().size(); i++) {
            synapse = this.network.getSynapses().get(i);
            synapse.setIdx(i);
            synapse.setDisplayIdx(i);
        }

        // 6 Create new indexes for the unique neurons
        HashMap<Integer, Integer> neuronMap = new HashMap<>();
        for (int i = 0; i < this.network.getReservoir().size(); i++) {
            neuron = this.network.getReservoir().get(i);
            neuronMap.put(neuron.getIdx(), i);
        }
        // 7 Set the new pre and post synaptic neuron indexes.
        int neuronIdx;
        int sourceId;
        int targetId;
        for (int i = 0; i < this.network.getReservoir().size(); i++) {
            neuron = this.network.getReservoir().get(i);
            for (Synapse outputSynapse : neuron.getOutputSynapses()) {
                sourceId = outputSynapse.getSourceNeuronIdx();
                neuronIdx = neuronMap.get(sourceId);
                outputSynapse.setSourceNeuronIdx(neuronIdx);
                targetId = outputSynapse.getTargetNeuronIdx();
                neuronIdx = neuronMap.get(targetId);
                outputSynapse.setTargetNeuronIdx(neuronIdx);
            }
        }
        // 8 Re-index the neurons
        for (int i = 0; i < this.network.getReservoir().size(); i++) {
            neuron = this.network.getReservoir().get(i);
            neuron.setIdx(i);
        }
        neuronMap.clear();
        LOGGER.info("Complete  ( Initial neurons: " + initialNeurons + ", active neurons: " + this.uniqueActiveNeurons.size() + ", neurons after prunning: " + (this.network.getReservoir().size()) + " time in " + ((System.nanoTime() - processTime) / 1000000) + "ms)");
    }

    /**
     * This function removes all the synapses which did not change during the
     * training. If the SNN has not been trained then it throws a
     * NeuCubeRuntimeException. To find the difference between the initial and
     * after training state of the SNN this function executes the following
     * steps: 1 Put the useless synapses in a list. For each synapse it compares
     * the first and the last weight, if they have the same value then no
     * changes occurred during the training and then the synapse is useless. 2
     * Remove the synapse from the pre and post synaptic neurons. 3 Remove the
     * useless synapses from the list of synapses in the network. 4 Re-index the
     * synapses.
     */
    public void pruneInactiveSynapses() {
        LOGGER.info("Prunning innactive synapses");
        long processTime = System.nanoTime();
        ArrayList<Synapse> synapses = this.network.getSynapses();
        ArrayList<Synapse> inactiveSynapses = new ArrayList<>();
        int initialSynapses = synapses.size();
        SpikingNeuron neuron;
        if (synapses.get(0).getWeights().size() == 1) {
            throw new NeuCubeRuntimeException("The SNN has not been trained. Perform the unsupervised training before using this method.");
        }
        // 1 Detect useless synapses
        for (Synapse synapse : synapses) {
            if (synapse.getWeights().get(0).equals(synapse.getWeights().get(synapse.getWeights().size() - 1))) {
                inactiveSynapses.add(synapse);
            }
        }

        // 2 Remove the useless synapses from the pre (source) and post (target) synaptic neurons
        for (Synapse synapse : inactiveSynapses) {
            neuron = this.network.getReservoir().get(synapse.getSourceNeuronIdx());
            neuron.getOutputSynapses().remove(synapse);
            neuron = this.network.getReservoir().get(synapse.getTargetNeuronIdx());
            neuron.getOutputSynapses().remove(synapse);
        }
        // 3 Remove the list of useless synapses from the list of synapses
        synapses.removeAll(inactiveSynapses);
        // 4 Re-index the synapses
        for (int i = 0; i < synapses.size(); i++) {
            synapses.get(i).setIdx(i);
            synapses.get(i).setDisplayIdx(i);
        }
        LOGGER.info("Complete  ( Initial synapses: " + initialSynapses + ", inactive synapses: " + inactiveSynapses.size() + ", synapses after prunning: " + (synapses.size()) + " time in " + ((System.nanoTime() - processTime) / 1000000) + "ms)");
    }

    /**
     * This function calculates the number of neurons and the number of firings
     * emitted after being stimulated with a dataset.
     *
     * @param dataset The samples
     * @param unlimited
     * @param delays
     * @return
     */
    public HashMap<SpikingNeuron, Integer> getFunctionalNeurons(ArrayList<DataSample> dataset, boolean unlimited, boolean delays) {
        LOGGER.info("Calculating functional neurons");
        long processTime = System.nanoTime();
        int initialNeurons = this.network.getReservoir().size();
        // 1 Propagate the spike trains
        this.propagateDataset(dataset, unlimited, delays);
        LOGGER.info("Calculation of functional neurons complete " + this.uniqueActiveNeurons.size() + "/" + initialNeurons);
        return this.uniqueActiveNeurons;
    }

    public ArrayList<Double> getFunctionalWeights(ArrayList<DataSample> dataset, boolean unlimited, boolean delays) {
        LOGGER.info("Calculating functional connections");
        HashMap<SpikingNeuron, Integer> functionalNeurons = this.getFunctionalNeurons(dataset, true, true);
        ArrayList<Double> functionalWeights = new ArrayList<>();
        SpikingNeuron postSynapticNeuron;
        int numWeights;
        for (SpikingNeuron neuron : functionalNeurons.keySet()) {
            for (Synapse synapse : neuron.getOutputSynapses()) {
                postSynapticNeuron = this.network.getReservoir().get(synapse.getTargetNeuronIdx());
                if (!postSynapticNeuron.getFirings().isEmpty()) {
                    numWeights = synapse.getWeights().size();
                    functionalWeights.add(synapse.getWeights().get(numWeights - 1));
                }
            }
        }
        LOGGER.info("Calculation of functional connections complete " + functionalWeights.size() + "/" + this.network.getSynapses().size());
        return functionalWeights;
    }

    /**
     * @return the reservoirBuilder
     */
    public ReservoirBuilder getReservoirBuilder() {
        return reservoirBuilder;
    }

    /**
     * @param reservoirBuilder the reservoirBuilder to set
     */
    public void setReservoirBuilder(ReservoirBuilder reservoirBuilder) {
        this.reservoirBuilder = reservoirBuilder;
    }

//    /**
//     * @return the neuronModel
//     */
//    public SpikingNeuron getNeuronModel() {
//        return neuronModel;
//    }
//
//    /**
//     * @param neuronModel the neuronModel to set
//     */
//    public void setNeuronModel(SpikingNeuron neuronModel) {
//        this.neuronModel = neuronModel;
//    }
}
