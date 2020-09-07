/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.network;

import jneucube.spikingNeurons.cores.Core;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import jneucube.data.DataSample;
import jneucube.spikingNeurons.NeuronType;
import jneucube.spikingNeurons.SpikingNeuron;
import jneucube.spikingNeurons.Synapse;
import jneucube.util.Matrix;
import static jneucube.log.Log.LOGGER;

/**
 *
 * @author Josafath Israel Espinosa Ramos (Centro de Investigacion en
 * Computacion, Instituto Politecnico Nacional, Mexico 2015)
 */
public class OldNetwork {

    //private int numberOfNeurons;
    private SpikingNeuron spikingNeuron = new SpikingNeuron();

    private ArrayList<SpikingNeuron> inputNeurons = new ArrayList<>();    // Some neurons of the reservoir representing the features
    private ArrayList<SpikingNeuron> outputNeurons = new ArrayList<>();
    private ArrayList<SpikingNeuron> reservoir = new ArrayList<>();
    private ArrayList<SpikingNeuron> inputNeuronsPostitive = new ArrayList<>(); // Added in June 2016
    private ArrayList<SpikingNeuron> inputNeuronsNegative = new ArrayList<>();  // Added in June 2016    

    private ArrayList<SpikingNeuron> firedNeurons = new ArrayList<>();    // Once the network is stimulated, the fired neurons are listed here
    private ArrayList<Integer> changedWeights = new ArrayList<>();

    private ArrayList<Synapse> synapses = new ArrayList<>();

    private HashMap<Integer, ArrayList<SpikingNeuron>> delayFiredNeuron = new HashMap<>();
    private boolean allowInhibitoryInputNeurons = true;

    private int numVariables = 0;
    private int numInputs = 0;
    private int numNeuronsX = 5;
    private int numNeuronsY = 5;
    private int numNeuronsZ = 5;
    private int numSynapses = 0;

    private boolean train = false;    // This flag detemines whether the SNN should be trained or just propagating spike trains. This flag should be tunred on or off before calling the "train" or "propagateSample" of the NetworkController class

    //Logger LOGGER = LoggerFactory.getLogger(NetworkController.class);

    /**
     * ****************************************
     */
    //              R E M O V E                //
    /**
     * ****************************************
     */
    /**
     * This function creates the reservoir and input neurons of the network.
     * Before creating the network, this function cleans all the list that
     * contain the neurons.
     *
     * @param reservoirCoordinates mx2 matrix containing the coordinates (x,y,z)
     * in the reservoir
     * @param inputCoordinates mx2 matrix containing the input coordinates
     * (x,y,x)
     * @param numVariables number of variables per input (multiples NeuCubes)
     */
    public void createNetwork(Matrix reservoirCoordinates, Matrix inputCoordinates, int numVariables) {
        LOGGER.info("----- Creating network coordinates -----");
        this.clear();   // Removes the synaptic weights, the connections, and all neurons in the network (reservoir, positive and negative inputs, outputs)
        this.setNumInputs(inputCoordinates.getRows());
        LOGGER.debug("   - Network for variable 1");
        this.createReservoir(reservoirCoordinates);     // Creates an arraylist of neurons
//        this.setInputNeuronsPositive(inputCoordinates); // Set input neurons for positive spikes        
        this.addInputNeuronsPositive(inputCoordinates);

        if (this.allowInhibitoryInputNeurons) {
            this.addInputNeuronsNegative(inputCoordinates); // Add input neurons for negative spikes
        }
        this.replicateReservoir(numVariables);
        LOGGER.info("----- Complete -----");
        LOGGER.info(" Total neurons: " + this.reservoir.size());
    }

    /**
     * ****************************************
     */
    //              R E M O V E                //
    /**
     * ****************************************
     */
    /**
     * Creates a network given a file containing the x,y,z coordinates.
     * Duplicated neurons are not considered in the network
     *
     * @param mCoordinates
     */
    public void createReservoir(Matrix mCoordinates) {
        this.addNeurons(mCoordinates);
    }

    /**
     * ****************************************
     */
    //              R E M O V E                //
    /**
     * ****************************************
     */
    /**
     * Creates a list of spiking neurons given the matrix with their
     * coordinates. The procedure avoids repetition of coordinates
     *
     * @param coordinates
     */
    public void addNeurons(Matrix coordinates) {
        int id = this.reservoir.size(); // This instruction is very important. It mantains a unique identifier for each neuron
        for (int i = 0; i < coordinates.getRows(); i++) {
            if (this.getNeuron(coordinates.getVecRow(i), reservoir) == null) { // Validates that neurons are not repeated
                try {
                    SpikingNeuron neuron = this.createReservoirNeuron(coordinates.getVecRow(i)[0], coordinates.getVecRow(i)[1], coordinates.getVecRow(i)[2]);
                    neuron.setIdx(id++);
                    //System.out.println("neuron " + id);
                    this.reservoir.add(neuron);
                } catch (CloneNotSupportedException ex) {
                    LOGGER.error(ex.toString());
                }
            }
        }
    }

    /**
     * ****************************************
     */
    //              R E M O V E                //
    /**
     * ****************************************
     */
    public SpikingNeuron createNeuron(double x, double y, double z) throws CloneNotSupportedException {
        SpikingNeuron neuron = this.spikingNeuron.clone();
        neuron.setPosX(x);
        neuron.setPosY(y);
        neuron.setPosZ(z);
//        neuron.setCore(this.core.clone());
        neuron.setRegion(0);
        return neuron;
    }

    /**
     * ****************************************
     */
    //              R E M O V E                //
    /**
     * ****************************************
     */
    public SpikingNeuron createReservoirNeuron(double x, double y, double z) throws CloneNotSupportedException {
        SpikingNeuron neuron = this.createNeuron(x, y, z);
        neuron.setNeuronType(NeuronType.RESERVOIR_NEURON);
        neuron.setRegion(0);
        return neuron;
    }

    /**
     * ****************************************
     */
    //              R E M O V E                //
    /**
     * ****************************************
     */
    public void setInputNeuronsPositive(Matrix mCoordinates) {
        boolean found = false;
        for (SpikingNeuron neuron : this.reservoir) {
            for (int r = 0; r < mCoordinates.getRows(); r++) {
                if (neuron.getPosX() == mCoordinates.getVecRow(r)[0] && neuron.getPosY() == mCoordinates.getVecRow(r)[1] && neuron.getPosZ() == mCoordinates.getVecRow(r)[2]) {
                    //System.out.println(neuron.getIdx() + " " + neuron.getStrType() + ": " + neuron.getSringCoordinates() + " -> " + neuron.getStringPreSynapses());
                    neuron.setNeuronType(NeuronType.INPUT_NEURON_POSITIVE);
                    this.inputNeurons.add(neuron);
                    this.inputNeuronsPostitive.add(neuron);
                    found = true;
                }
            }
        }
        if (!found) {
            this.addInputNeuronsPositive(mCoordinates);
        }
    }

    /**
     * ****************************************
     */
    //              R E M O V E                //
    /**
     * ****************************************
     */
    public void addInputNeuronsNegative(Matrix mCoordinates) {
        SpikingNeuron neuron;
        for (int i = 0; i < mCoordinates.getRows(); i++) {
            try {
                neuron = this.createReservoirNeuron(mCoordinates.getVecRow(i)[0], mCoordinates.getVecRow(i)[1], mCoordinates.getVecRow(i)[2]);
                neuron.setIdx(this.reservoir.size());
                neuron.setNeuronType(NeuronType.INPUT_NEURON_NEGATIVE);
                this.reservoir.add(neuron);
                this.inputNeurons.add(neuron);
                this.inputNeuronsNegative.add(neuron);
            } catch (CloneNotSupportedException ex) {
                LOGGER.error(ex.toString());
            }
        }
    }

    /**
     * ****************************************
     */
    //              R E M O V E                //
    /**
     * ****************************************
     */
    public void addInputNeuronsPositive(Matrix mCoordinates) {
        SpikingNeuron neuron;
        for (int i = 0; i < mCoordinates.getRows(); i++) {
            try {
                neuron = this.createReservoirNeuron(mCoordinates.getVecRow(i)[0], mCoordinates.getVecRow(i)[1], mCoordinates.getVecRow(i)[2]);
                neuron.setIdx(this.reservoir.size());
                neuron.setNeuronType(NeuronType.INPUT_NEURON_POSITIVE);
                this.reservoir.add(neuron);
                this.inputNeurons.add(neuron);
                this.inputNeuronsPostitive.add(neuron);
            } catch (CloneNotSupportedException ex) {
                LOGGER.error(ex.toString());
            }
        }
    }

    /**
     * ****************************************
     */
    //              R E M O V E                //
    /**
     * ****************************************
     */
    public boolean replaceReservoirCores(Core neuronCore) {
        boolean status = false;
        for (SpikingNeuron neuron : this.reservoir) {
            try {
                neuron.setCore(neuronCore.clone());
                status = true;
            } catch (CloneNotSupportedException ex) {
                LOGGER.error(ex.toString());
                break;
            }
        }
        return status;
    }

    /**
     * ****************************************
     */
    //              R E M O V E                //
    /**
     * ****************************************
     */
    /**
     * Replicates n times the reservoir for a multivariate NeuCube (June 2016)
     *
     * @param times
     */
    public void replicateReservoir(int times) {
        ArrayList<SpikingNeuron> temp;
        this.numVariables = times;
        int end = this.getNumberOfNeurons();
        for (int i = 1; i < times; i++) {
            LOGGER.debug("   - Replicating network for variable " + (i + 1));
            try {
                temp = this.replicateNeuronsReservoir(0, end);
                this.reservoir.addAll(temp);
            } catch (CloneNotSupportedException ex) {
                LOGGER.error(ex.toString());
            }
            LOGGER.debug("   - Replicating complete ");
        }
    }

    /**
     * ****************************************
     */
    //              R E M O V E                //
    /**
     * ****************************************
     */
    /**
     * Replicates the reservoir given the indexes of the neurons (June 2016)
     *
     * @param startIndex
     * @param endIndex
     * @return
     * @throws CloneNotSupportedException
     */
    private ArrayList<SpikingNeuron> replicateNeuronsReservoir(int startIndex, int endIndex) throws CloneNotSupportedException {
        int neuronIdx = this.reservoir.size();
        ArrayList<SpikingNeuron> tempList = new ArrayList<>();
        for (int i = startIndex; i < endIndex; i++) {
            SpikingNeuron neuron = (SpikingNeuron) this.reservoir.get(i).clone();
            neuron.setIdx(neuronIdx);
            tempList.add(neuron);
            neuronIdx++;
            if (neuron.getNeuronType() == NeuronType.INPUT_NEURON_POSITIVE) {
                this.inputNeuronsPostitive.add(neuron);
                this.inputNeurons.add(neuron);
            } else if (neuron.getNeuronType() == NeuronType.INPUT_NEURON_NEGATIVE) {
                this.inputNeuronsNegative.add(neuron);
                this.inputNeurons.add(neuron);
            }
        }
        return tempList;
    }

    /**
     * ************************************
     * R E M O V E
     **************************************
     */
    /**
     * Clears the network. The neuron properties, and the lists of output, input
     * and the reservoir neurons
     */
    public void clear() {
        this.outputNeurons.clear();
        this.inputNeurons.clear();
        this.inputNeuronsNegative.clear();
        this.inputNeuronsPostitive.clear();
        this.reservoir.clear();
    }

    /**
     * ****************************************
     */
    //              R E M O V E                //
    /**
     * ****************************************
     */
    /**
     * Removes all synapses (input and output) from all neurons (reservoir and
     * output)
     */
    public void deleteConnections() {
        for (SpikingNeuron neuron : this.reservoir) {
            neuron.removeAllConnections();
        }
        for (SpikingNeuron neuron : this.outputNeurons) {
            neuron.removeAllConnections();
        }
    }

    public int getNumberOfNeurons() {
        return this.reservoir.size();
    }

    /**
     * ****************************************
     */
    //              R E M O V E                //
    /**
     * ****************************************
     */
    public void createOutputNeurons(int numSamples) throws CloneNotSupportedException {
        for (int i = 0; i < numSamples; i++) {
            SpikingNeuron neuron = this.spikingNeuron.clone();
            neuron.setPosX(200);
            neuron.setPosY(0);
            neuron.setPosZ((i - (numSamples / 2)) * 10);
//            neuron.setCore(this.core.clone());
            neuron.setIdx(i);
            neuron.setNeuronType(NeuronType.OUTPUT_NEURON);
            this.reservoir.stream().forEach((sourceNeuron) -> {
                neuron.addOutputSynapse(new Synapse(sourceNeuron, neuron, 0.0));
            });
            this.outputNeurons.add(neuron);
        }
    }

    /**
     * ****************************************
     */
    //              R E M O V E                //
    /**
     * ****************************************
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
     * ****************************************
     */
    //              R E M O V E                //
    /**
     * ****************************************
     */
    public void setAllReservoir() {
        this.reservoir.stream().forEach((neuron) -> {
            neuron.setLabel("");
            neuron.setNeuronType(NeuronType.RESERVOIR_NEURON);
        });
    }

    /**
     * @return the numNeuronsX
     */
    public int getNumNeuronsX() {
        return numNeuronsX;
    }

    /**
     * @return the numNeuronsY
     */
    public int getNumNeuronsY() {
        return numNeuronsY;
    }

    /**
     * @return the numNeuronsZ
     */
    public int getNumNeuronsZ() {
        return numNeuronsZ;
    }

    /**
     * @return the reservoir
     */
    public ArrayList<SpikingNeuron> getReservoir() {
        return reservoir;
    }

    /**
     * @param reservoir the reservoir to set
     */
    public void setReservoir(ArrayList<SpikingNeuron> reservoir) {
        this.reservoir = reservoir;
    }

    /**
     * @return the inputNeurons
     */
    public ArrayList<SpikingNeuron> getInputNeurons() {
        return inputNeurons;
    }

    /**
     * @param inputNeurons the inputNeurons to set
     */
    public void setInputNeurons(ArrayList<SpikingNeuron> inputNeurons) {
        this.inputNeurons = inputNeurons;
    }

    /**
     * @return the outputNeurons
     */
    public ArrayList<SpikingNeuron> getOutputNeurons() {
        return outputNeurons;
    }

    /**
     * @param outputNeurons the outputNeurons to set
     */
    public void setOutputNeurons(ArrayList<SpikingNeuron> outputNeurons) {
        this.outputNeurons = outputNeurons;
    }

    /**
     * ****************************************
     */
    //              R E M O V E                //
    /**
     * ****************************************
     */
    public void changeCore(Core core) {
//        LOGGER.info("----- Changing neurons' core from " + this.core.toString() + " to " + core.toString() + " -----");
        LOGGER.info("----- Changing neurons' core from " + this.spikingNeuron.getCore().toString() + " to " + core.toString() + " -----");
//        this.setCore(core);
        LOGGER.info("   - Changing neurons in the reservoir ");
        for (SpikingNeuron neuron : this.getReservoir()) {
            try {
                neuron.setCore(core.clone());
            } catch (CloneNotSupportedException ex) {
                LOGGER.error(ex.toString());
            }
        }
        LOGGER.info("   - Changing output neurons");
        for (SpikingNeuron neuron : this.getOutputNeurons()) {
            try {
                neuron.setCore(core.clone());
            } catch (CloneNotSupportedException ex) {
                LOGGER.error(ex.toString());
            }
        }
        LOGGER.info("----- Complete -----");
    }

    /**
     * ****************************************
     */
    //              R E M O V E                //
    /**
     * ****************************************
     */
    @Deprecated
    public void resetActionPotentials() {
        this.getReservoir().stream().forEach((neuron) -> {
            neuron.resetMembranePotential();
        });
    }

    /**
     * ****************************************
     */
    //              R E M O V E                //
    /**
     * ****************************************
     */
    /**
     * Resets the firings of all neurons in the reservoir
     */
    public void resetNeuronsFirings() {
        this.getReservoir().parallelStream().forEach((neuron) -> {
            neuron.resetActivity();
        });
    }

    /**
     * ****************************************
     */
    //              R E M O V E                //
    /**
     * ****************************************
     */
    /**
     * This function resets the firings and set the variable for recording
     * firings of all the neurons in the reservoir
     *
     * @param recordFirings
     */
    public void resetNeuronsFirings(boolean recordFirings) {
        this.getReservoir().parallelStream().forEach((neuron) -> {
            neuron.resetActivity();
            neuron.getCore().setRecordFirings(recordFirings);
        });
    }

    /**
     * ****************************************
     */
    //              R E M O V E                //
    /**
     * ****************************************
     */
    /**
     *
     * @param currentTime
     * @return
     */
    public ArrayList<SpikingNeuron> getFiredNeurons(int currentTime) {
        ArrayList<SpikingNeuron> list = new ArrayList<>();
        for (SpikingNeuron neuron : this.reservoir) {
            if (neuron.isFiredFiringList(currentTime)) {
                list.add(neuron);
            }
        }
        return list;
    }

    /**
     * ****************************************
     */
    //              R E M O V E                //
    /**
     * ****************************************
     */
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
        for (SpikingNeuron neuron : this.firedNeurons) {
            if (neuron.getIdx() >= beginIdx && neuron.getIdx() <= endIdx) {
                list.add(neuron);
            }
        }
        return list;
    }

    /**
     * ****************************************
     */
    //              R E M O V E                //
    /**
     * ****************************************
     */
    /**
     * Set the weights into their initial values before training. Utilized in
     * the STDP
     */
    public void resetConnectionsWeights() {
        for (SpikingNeuron neuron : this.reservoir) {
            neuron.resetConnectionWeights(neuron.getOutputSynapses());
        }
    }


    /**
     * ****************************************
     */
    //              R E M O V E                //
    /**
     * ****************************************
     */
    /**
     *
     * @param connections
     * @param weights
     * @param numCubes
     */
    public void createConnections(Matrix connections, Matrix weights, int numCubes) {
        SpikingNeuron preSynapticNeuron;    // source
        SpikingNeuron postSynapticNeuron;   // target
        int realIdx;
        int synapseIdx = 0;
        synapses.clear();
        int displaySynapseId = 0;
        int numNeurons = this.reservoir.size() / numCubes;
        Random randomDelay = new Random();

        if (connections.getRows() == weights.getRows() && connections.getCols() == weights.getCols()) {
            for (int i = 0; i < connections.getRows(); i++) {
                preSynapticNeuron = this.reservoir.get(i);

                if (i % numNeurons == 0) {
                    displaySynapseId = 0; // It resets the id that is used in the display (DisplayController) to match with the Xform (connectionXform) containing the synapses
                }
                for (int j = 0; j < connections.getCols(); j++) {
                    if (connections.get(i, j) != 0) {
                        realIdx = j + (numNeurons * (i / numNeurons));  // The real neuron id to be connected since the number of columns in the connection matrix is th enumber of neurons without replication
                        postSynapticNeuron = this.reservoir.get(realIdx);

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
                        synapses.add(synapse);
                        synapseIdx++;
                        displaySynapseId++;
                    }
                }
//                System.out.println(preSynapticNeuron.getIdx() + " " + preSynapticNeuron.getStrType() + ": " + preSynapticNeuron.getSringCoordinates() + " -> " + preSynapticNeuron.getStringPreSynapses());
            }
            this.numSynapses = synapseIdx;
        } else {
            LOGGER.error("The matrices must contain the same number of rows and columns");
        }
    }

    public ArrayList<Synapse> getSynapses() {
        return synapses;
    }

    /**
     * *************************************
     * // R E M O V E **************************************
     */
    public Matrix getConnectionMatrix() {
        int numNeurons = this.getReservoir().size();
        Matrix matrix = new Matrix(numNeurons, numNeurons, 0.0);
        this.getReservoir().stream().forEach((neuron) -> {
            neuron.getOutputSynapses().stream().forEach((synapse) -> {
                matrix.set(neuron.getIdx(), synapse.getTargetNeuronIdx(), 1.0);
            });
        });
        return matrix;
    }

    /**
     * *************************************
     * // R E M O V E **************************************
     */
    public Matrix getWeightMatrix() {
        int numNeurons = this.getReservoir().size();
        Matrix matrix = new Matrix(numNeurons, numNeurons, 0.0);
        this.getReservoir().stream().forEach((neuron) -> {
            neuron.getOutputSynapses().stream().forEach((synapse) -> {
                matrix.set(neuron.getIdx(), synapse.getTargetNeuronIdx(), synapse.getWeight());
            });
        });
        return matrix;
    }

    /**
     * *************************************
     * // R E M O V E **************************************
     */
    public void deleteAllConnections() {
        for (SpikingNeuron neuron : this.reservoir) {
            for (Synapse synapse : neuron.getInputSynapses()) {
                synapse.getWeights().clear();
            }
            for (Synapse synapse : neuron.getOutputSynapses()) {
                synapse.getWeights().clear();
            }
            neuron.getInputSynapses().clear();
            neuron.getOutputSynapses().clear();
        }
    }

    /**
     * @return the inputNeuronsPostitive
     */
    public ArrayList<SpikingNeuron> getInputNeuronsPostitive() {
        return inputNeuronsPostitive;
    }

    /**
     * @param inputNeuronsPostitive the inputNeuronsPostitive to set
     */
    public void setInputNeuronsPostitive(ArrayList<SpikingNeuron> inputNeuronsPostitive) {
        this.inputNeuronsPostitive = inputNeuronsPostitive;
    }

    /**
     * @return the inputNeuronsNegative
     */
    public ArrayList<SpikingNeuron> getInputNeuronsNegative() {
        return inputNeuronsNegative;
    }

    /**
     * @param inputNeuronsNegative the inputNeuronsNegative to set
     */
    public void setInputNeuronsNegative(ArrayList<SpikingNeuron> inputNeuronsNegative) {
        this.inputNeuronsNegative = inputNeuronsNegative;
    }

    /**
     * @param numNeuronsX the numNeuronsX to set
     */
    public void setNumNeuronsX(int numNeuronsX) {
        this.numNeuronsX = numNeuronsX;
    }

    /**
     * @param numNeuronsY the numNeuronsY to set
     */
    public void setNumNeuronsY(int numNeuronsY) {
        this.numNeuronsY = numNeuronsY;
    }

    /**
     * @param numNeuronsZ the numNeuronsZ to set
     */
    public void setNumNeuronsZ(int numNeuronsZ) {
        this.numNeuronsZ = numNeuronsZ;
    }


    /**
     * @return the numInputs
     */
    public int getNumInputs() {
        return numInputs;
    }

    /**
     * @param numInputs the numInputs to set
     */
    public void setNumInputs(int numInputs) {
        this.numInputs = numInputs;
    }

    /**
     * @return the numVariables
     */
    public int getNumVariables() {
        return numVariables;
    }

    /**
     * @param numVariables the numVariables to set
     */
    public void setNumVariables(int numVariables) {
        this.numVariables = numVariables;
    }

    /**
     * *************************************
     * // R E M O V E **************************************
     */
    /**
     * Set the input spike data into the input neurons
     *
     * @param spikes The input data to be propagated
     * @param elapsedTime The training time
     */
    public void setInputSpikes(double[] spikes, int elapsedTime) {
        int neuronIdx;
        int cubeId;
        int inputId;
        //for (int col = 0; col < spikes.length; col++) { // Inserts the spike trains into the input neurons
        for (int col = 0; col < this.getInputNeuronsPostitive().size(); col++) { // Inserts the spike trains into the input neurons
            inputId = col / this.getNumVariables();
            cubeId = col % this.getNumVariables();
            neuronIdx = this.getNumInputs() * cubeId + inputId; // Same index for the positive and negative input neurons in the list

            this.getInputNeuronsPostitive().get(neuronIdx).getCore().setFired(false);
            if (spikes[col] == 1) {
                this.getInputNeuronsPostitive().get(neuronIdx).getCore().setFired(true);
                this.getInputNeuronsPostitive().get(neuronIdx).getCore().getFirings().add((double) elapsedTime);
                this.getInputNeuronsPostitive().get(neuronIdx).getCore().setLastSpikeTime(elapsedTime);
            }
            if (!this.getInputNeuronsNegative().isEmpty()) { // if the network works with negative (inhibitory) input neurons
                this.getInputNeuronsNegative().get(neuronIdx).getCore().setFired(false);
                if (spikes[col] == -1) {
                    this.getInputNeuronsNegative().get(neuronIdx).getCore().setFired(true);
                    this.getInputNeuronsNegative().get(neuronIdx).getCore().getFirings().add((double) elapsedTime);
                    this.getInputNeuronsNegative().get(neuronIdx).getCore().setLastSpikeTime(elapsedTime);
                }
            }
        }
    }

    /**
     * ***********************************
     * // R E M O V E              
    ************************************
     */
    /**
     * This function deletes the connections of the spiking neuron before it is
     * removed from the reservoir and the input list of neurons.
     *
     * @param neuron
     */
    public void deleteNeuron(SpikingNeuron neuron) {
        neuron.removeAllConnections();
        this.reservoir.remove(neuron);
        this.inputNeuronsNegative.remove(neuron);
        this.inputNeuronsPostitive.remove(neuron);
        this.inputNeurons.remove(neuron);
    }

    /**
     * ***********************************
     * // R E M O V E              
    ************************************
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
        synchronized (firedNeurons) {
            firedNeurons.clear();
            for (SpikingNeuron neuron : this.getReservoir()) {
                if (neuron.getNeuronType() == NeuronType.RESERVOIR_NEURON) {
                    neuron.computeMembranePotential(elapsedTime);
                }
                if (neuron.isFired()) {
                    firedNeurons.add(neuron);   // this list is used for the next data point along with the input neurons
                }
                //System.out.println(neuron.getIdx()+"  "+neuron.isFired()+"  "+neuron.getMembranePotential());
            }
            for (SpikingNeuron firedNeuron : firedNeurons) {   // Propagate all spikes in the network
                firedNeuron.propagateSpike(this.getReservoir());
            }
        }
        return firedNeurons;
    }

    ////////////////////////////////////
    //           R E M O V E          //
    ////////////////////////////////////
    /**
     * This function only propagates the samples into the NeuCube as a
     * feedforward SNN. Before the process starts, the firing activity of all
     * neurons is removed.
     *
     * @param sample
     * @param elapsedTime
     * @return
     */
    public int propagateSample(DataSample sample, int elapsedTime) {
        ArrayList<SpikingNeuron> firedNeurons = new ArrayList<>();
        double startTime;
        double[] emptySpikeVector = new double[sample.getSpikeData().getCols()];
        for (int i = 0; i < emptySpikeVector.length; i++) {
            emptySpikeVector[i] = 0.0;
        }
        this.resetNeuronsFirings(true); // Removes all firings (firing times) and sets to zero the last spike time, the number of spikes received and the number of spikes emitted.                
        LOGGER.info("------- Unsupervised training using STDP learning rule -------");

        startTime = System.nanoTime();
//                System.out.println("Sample " + sampleId);
        this.getReservoir().stream().forEach((neuron) -> {
            neuron.getCore().setLastSpikeTime(0);
            neuron.getCore().reset();   // Resets neurons' core. Sets action potentials to zero among other features according to the neuron model, eg. in SLIF the refractory time and the membrne potential are set to zero
            neuron.resetCurrent();          // Resets the accumulated input current given by the synaptic weights
        });

        int sampleTime = 0;

        while (sampleTime < sample.getSpikeData().getRows() || !firedNeurons.isEmpty()) {
            if (sampleTime < sample.getSpikeData().getRows()) {
                this.setInputSpikes(sample.getSpikeData().getVecRow(sampleTime), elapsedTime);   // Set the input spike data into the input neurons and add the elapsed time to the neuron firing list
                sampleTime++;
            } else {
                this.setInputSpikes(emptySpikeVector, elapsedTime);   // Set the input spike data into the input neurons and add the elapsed time to the neuron firing list
            }
            firedNeurons = this.stimulateNetwork(elapsedTime); // Propagates the information through the network and returns the fired neurons                    
            LOGGER.debug("Elapsed time " + elapsedTime + " Time " + sampleTime + " Fired " + firedNeurons.size());
            System.out.println("Elapsed time " + elapsedTime + " Time " + sampleTime + " Fired " + firedNeurons.size());
            elapsedTime++;
        }
        LOGGER.debug("Sample: " + sample.getSampleId() + ", propagation time: " + (System.nanoTime() - startTime) / 1000000 + " ms");
        Runtime.getRuntime().gc();
        return elapsedTime;
    }

    /**
     * This function only propagates the samples into the NeuCube as a
     * feedforward SNN and calculates the branching parameter (measure of
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
        //ArrayList<SpikingNeuron> firedNeurons;
        Runtime.getRuntime().gc();
        double startTime;
        int sampleId = 1;
        double duration = 0;
        double branchingParameter = 0;
        double accumulatedRatio = 0.0;
        int numBranchingValues = 0;
        int prevFiringNeurons = 0;

        this.resetNeuronsFirings(); // Removes all firings (firing times) and sets to zero the last spike time, the number of spikes received and the number of spikes emitted.                
        LOGGER.info("------- Propagating data and computing branching parameter -------");
        for (int t = 0; t < numTrainingRounds; t++) {    // Number of training times           
            for (DataSample sample : data) {   // For each training data sample  
                startTime = System.nanoTime();
//                System.out.println("Sample " + sampleId);
                this.getReservoir().parallelStream().forEach((neuron) -> {
                    neuron.getCore().setLastSpikeTime(0);
                    neuron.reset(); // Resets neurons' core and the accumulated input current given by the synaptic weights. Sets action potentials to zero among other features according to the neuron model, eg. in SLIF the refractory time and the membrne potential are set to zero
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

    public void saveCurrentWeights() {
        for (SpikingNeuron neuron : this.reservoir) {
            for (Synapse synapse : neuron.getOutputSynapses()) {
                synapse.addWeight(synapse.getWeight());
            }
        }
    }

    /**
     * @return the firedNeurons
     */
    public ArrayList<SpikingNeuron> getFiredNeurons() {
        return firedNeurons;
    }

    ////////////////////////////////////
    //           R E M O V E          //
    ////////////////////////////////////
    public double getNetworkCurrent() {
        double conductance = 0.0;
        for (SpikingNeuron neuron : this.reservoir) {
            for (Synapse synapse : neuron.getOutputSynapses()) {
                conductance += synapse.getWeight();
            }
        }
        return conductance;
    }

    
    ////////////////////////////////////
    //           R E M O V E          //
    ////////////////////////////////////    
    /**
     * Calculates the mean value of the synaptic weights
     *
     * @return
     */
    public double getNetworkMeanCurrent() {
        double conductance = 0.0;
        int i = 0;
        for (SpikingNeuron neuron : this.reservoir) {
            for (Synapse synapse : neuron.getOutputSynapses()) {
                conductance += synapse.getWeight();
                i++;
            }
        }
        return conductance / i;
    }

    ////////////////////////////////////
    //           R E M O V E          //
    ////////////////////////////////////     
    /**
     * Calculates the standard deviation of the synaptic weights. It also
     * calculates the mean value of synaptic weights utilised for calculating
     * the standard deviation
     *
     * @return
     */
    public double getNetworkStdConductance() {
        double std = 0.0;
        double mean = this.getNetworkMeanCurrent();
        int i = 0;
        for (SpikingNeuron neuron : this.reservoir) {
            for (Synapse synapse : neuron.getOutputSynapses()) {
                std += Math.pow(synapse.getWeight() - mean, 2.0);
                i++;
            }
        }
        std = Math.sqrt(std / i);
        return std;
    }

        ////////////////////////////////////
    //           R E M O V E          //
    ////////////////////////////////////  
    /**
     * Calculates the standard deviation of the synaptic weights given their
     * mean value.
     *
     * @param mean The mean value of the synaptic weights
     * @return
     */
    public double getNetworkStdConductance(double mean) {
        double std = 0.0;
        int i = 0;
        for (SpikingNeuron neuron : this.reservoir) {
            for (Synapse synapse : neuron.getOutputSynapses()) {
                std += Math.pow(synapse.getWeight() - mean, 2.0);
                i++;
            }
        }
        std = Math.sqrt(std / i);
        return std;
    }

    ////////////////////////////////////
    //           R E M O V E          //
    ////////////////////////////////////      
    /**
     * Calculates the skewness of the synaptic weight values. This function
     * calculates the mean and the standard deviation.
     *
     * @return
     */
    public double getNetworkSkewnessConductance() {
        double mean = this.getNetworkMeanCurrent();
        double std = this.getNetworkStdConductance(mean);
        double skewness = Math.pow(mean, 3.0) / Math.pow(std, 3.0);
        return skewness;
    }

    
    ////////////////////////////////////
    //           R E M O V E          //
    ////////////////////////////////////      
    public double[][] getNetworkStandardNormalisedConductanceValues() {
        int numConnections = this.getNumSynapses();
        double mean = this.getNetworkMeanCurrent();
        double std = this.getNetworkStdConductance(mean);
        double[][] values = new double[numConnections][2];
        double value;

        int i = 0;
        for (SpikingNeuron neuron : this.reservoir) {
            for (Synapse synapse : neuron.getOutputSynapses()) {
                //value = (1 /Math.sqrt(2*Math.PI )) * Math.exp(- Math.pow(synapse.getWeight(), 2.0)  /2 );
                value = (synapse.getWeight() - mean) / std;
                values[i][0] = synapse.getWeight();
                values[i][1] = value;
                i++;
            }
        }
        return values;
    }

    
    ////////////////////////////////////
    //           R E M O V E          //
    ////////////////////////////////////      
    public double[][] getNetworkStandardNormalisedConductanceValues(double mean, double std) {
        int numConnections = this.getNumSynapses();
        double[][] values = new double[numConnections][2];
        double value;

        int i = 0;
        for (SpikingNeuron neuron : this.reservoir) {
            for (Synapse synapse : neuron.getOutputSynapses()) {
                //value = (1 /Math.sqrt(2*Math.PI )) * Math.exp(- Math.pow(synapse.getWeight(), 2.0)  /2 );
                value = (synapse.getWeight() - mean) / std;
                values[i][0] = synapse.getWeight();
                values[i][1] = value;
                i++;
            }
        }
        return values;
    }

    
    ////////////////////////////////////
    //           R E M O V E          //
    ////////////////////////////////////      
    public double[][] getNetworkNormalDistributionConductanceValues() {
        int numConnections = this.getNumSynapses();
        double mean = this.getNetworkMeanCurrent();
        double sigma = this.getNetworkStdConductance();
        double[][] values = new double[numConnections][2];
        double value;
        int i = 0;
        for (SpikingNeuron neuron : this.reservoir) {
            for (Synapse synapse : neuron.getOutputSynapses()) {
                value = (1 / Math.sqrt(2 * Math.PI * Math.pow(sigma, 2.0))) * Math.exp(-(Math.pow(synapse.getWeight() - mean, 2.0)) / (2 * Math.pow(sigma, 2.0)));
                values[i][0] = synapse.getWeight();
                values[i][1] = value;
                i++;
            }
        }
        return values;
    }
    
    
    

    ////////////////////////////////////
    //           R E M O V E          //
    ////////////////////////////////////      
//    public double[][] getNetworkNormalDistributionConductanceValues(int time) {
//        int numConnections = this.getNumSynapses();
//        double mean = this.getNetworkMeanCurrent();
//        double sigma = this.getNetworkStdConductance();
//        double[][] values = new double[numConnections][2];
//        double value;
//        int i = 0;
//        for (SpikingNeuron neuron : this.reservoir) {
//            for (Synapse synapse : neuron.getOutputSynapses()) {
//                value = (1 / Math.sqrt(2 * Math.PI * Math.pow(sigma, 2.0))) * Math.exp(-(Math.pow(synapse.getWeights().get(time) - mean, 2.0)) / (2 * Math.pow(sigma, 2.0)));
//                values[i][0] = synapse.getWeights().get(time);
//                values[i][1] = value;
//                i++;
//            }
//        }
//        return values;
//    }

    public static void main(String args[]) {

    }

    public void setNumSynapses(int numSynapes) {
        this.numSynapses = numSynapes;
    }

    /**
     * @return the numSynapses
     */
    public int getNumSynapses() {
        return numSynapses;
    }

    /**
     * @return the spikingNeuron
     */
    public SpikingNeuron getSpikingNeuron() {
        return spikingNeuron;
    }
    
    ////////////////////////////////////
    //           R E M O V E          //
    ////////////////////////////////////  
    public SpikingNeuron getSpikingNeuron(int posX, int posY, int posZ) {
        int idx = (this.numNeuronsX * this.numNeuronsY * posZ) + ((this.numNeuronsY * (posX)) + posY);
        return this.reservoir.get(idx);
    }

    /**
     * @param spikingNeuron the spikingNeuron to set
     */
    public void setSpikingNeuron(SpikingNeuron spikingNeuron) {
        this.spikingNeuron = spikingNeuron;
    }

    /**
     * @return the changedWeights
     */
    public ArrayList<Integer> getChangedWeights() {
        return changedWeights;
    }

    /**
     * @param changedWeights the changedWeights to set
     */
    public void setChangedWeights(ArrayList<Integer> changedWeights) {
        this.changedWeights = changedWeights;
    }

    /**
     * @return the allowInhibitoryInputNeurons
     */
    public boolean isAllowInhibitoryInputNeurons() {
        return allowInhibitoryInputNeurons;
    }

    /**
     * @param allowInhibitoryInputNeurons the allowInhibitoryInputNeurons to set
     */
    public void setAllowInhibitoryInputNeurons(boolean allowInhibitoryInputNeurons) {
        this.allowInhibitoryInputNeurons = allowInhibitoryInputNeurons;
    }
    
    /**
     * @return the train
     */
    public boolean isTrain() {
        return train;
    }

    /**
     * @param train the train to set
     */
    public void setTrain(boolean train) {
        this.train = train;
    }
    

}
