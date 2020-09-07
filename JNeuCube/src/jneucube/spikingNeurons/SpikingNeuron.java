/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.spikingNeurons;

import java.util.ArrayList;
import java.util.Arrays;

import jneucube.spikingNeurons.cores.Core;
import jneucube.spikingNeurons.cores.LIF;
import static jneucube.log.Log.LOGGER;

/**
 * This class define the structure
 *
 * @author Josafath Israel Espinosa Ramos (Centro de Investigacion en
 * Computacion, Instituto Politecnico Nacional, Mexico 2015)
 */
public class SpikingNeuron implements Cloneable {

    public static int FIXED_DELAY = 0;  // All the output synapses have the same delay value
    public static int RANDOM_DELAY = 1; // Every output synapse has different delay value

    private Core core = new LIF(); // The default core is the leaky integrate and fire neuron
    private double posX = 0;        // Position in the Z coordinate in a 3D space (used in the connection algorithmn and visualization)
    private double posY = 0;        // Position in the Y coordinate in a 3D space (used in the connection algorithm and visualization)
    private double posZ = 0;        // Position in the Z coordinate in a 3D space (used in the connection algorithm and visualization)
    private int idx;                // Unique identifier of the neuron. It is consecutive
    private int region = 0;         // The region of the brain to which the neuron belongs according to its position
    private String label = "";      // The label of the neuron, usually the name of the input
    private int type = NeuronType.EXCITATORY; // Type of neuron excitatory or inhibitory
    private int neuronType = NeuronType.RESERVOIR_NEURON;   // Type of neuron in the network    
    private int numSpikesReceived = 0;          // number of spikes received
    private int numSpikesEmitted = 0;           // number of spikes emmited
    private double firingProbability = 1.0;     // for future applications using probabiilistic spiking neurons
    private ArrayList<Synapse> outputSynapses = new ArrayList<>();  // Connection to other neurons 
    private ArrayList<Synapse> inputSynapses = new ArrayList<>(); // Connections from other neurons
    private double current = 0.0;               // The amount of current acumulated due to incoming spikes
    private boolean stimulated = false;         // Indicates whether the neuron is stimilated due to an incoming spike

    private int maxDelay = 1;                   // The maximum time delay to propagate a spike. The delay value is a property of the synapse. Added on 2017/11/05
    private int typeDelay = 0;                  // Fixed 0, random 1   Added on 2017/11/05

    private double classId;                        // The class to which the neuron belongs to. It is only for output neurons.

    /**
     * Class constructor
     */
    public SpikingNeuron() {

    }

    /**
     * Class constructor specifying the type of core (behavior)
     *
     * @param core
     */
    public SpikingNeuron(Core core) {
        this.core = core;
    }

    /**
     * Class constructor specifying the type of core, the identifier of the
     * neuron and the type of neuron (reservoir, input or output)
     *
     * @param core
     * @param idx
     * @param neuronType
     */
    public SpikingNeuron(Core core, int idx, int neuronType) {
        this.core = core;
        this.idx = idx;
        this.neuronType = neuronType;
    }

    /**
     * This function calculates the membrane potential of a neuron after
     * calculating the total incoming current from presynaptic neurons. If the
     * neuron received spikes, then the membrane potential will increase in
     * function of the synaptic weights otherwise the neuron decreases its
     * membrane potential. Once the membrane potential was calculated, the
     * current is set to zero for the next incoming spikes.
     *
     * @param time
     */
    public void computeMembranePotential(int time) {
        if (this.stimulated) {   // indicates whether the neuron received a spike
            this.core.computeMembranePotential(time, this.current);
        } else {
            this.core.computeMembranePotential(time, 0);
        }
        this.resetCurrent();
        this.stimulated = false;
    }

    /**
     * This method resets the neuron to its initial state before training. It
     * sets to zero the number of spikes received and emitted
     * {@link resetNumberOfSpikes()}, resets the output connection weights
     * {@link resetConnectionWeights()}, resets the accumulated current and sets
     * to false the stimulated status {@link resetCurrent()}, and clears the
     * core.
     */
    public void reset() {
        this.resetNumberOfSpikes();
        this.resetConnectionWeights(this.outputSynapses);
        this.resetCurrent();
        this.core.clear();   // resets and clear the core
    }

    /**
     * This method resets the neuron to its initial state and removes all the
     * presynaptic and postsynaptic connections for creating new connections. It
     * sets to zero the number of spikes received and emitted
     * {@link resetNumberOfSpikes()}, removes the connections
     * {@link removeConnections()}, resets the accumulated current and sets to
     * false the stimulated status {@link resetCurrent()}, and clears the core.
     */
    public void clear() {
        this.resetNumberOfSpikes();
        this.resetCurrent();
        this.removeAllConnections();
        this.core.clear();  // resets and clear the core
    }

    /**
     * Sets to zero the number of spikes received from the presynaptic neuron
     * and the number of spikes emitted to the postsynaptic neuron.
     */
    public void resetNumberOfSpikes() {
        this.numSpikesEmitted = 0;
        this.numSpikesReceived = 0;
    }

    /**
     * Sets the accumulated current to zero and sets the stimulated status to
     * false.
     */
    public void resetCurrent() {
        this.current = 0.0;
        this.setStimulated(false);
    }

//    /**
//     * This function preserves the initial weight values of all the output
//     * synapse and removes all the weights accumulated during the learning
//     * process.
//     */
//    public void resetConnectionWeights() {
//        for (Synapse synapse : this.getOutputSynapses()) {
//            synapse.reset();
//        }
//    }
    /**
     * This function preserves the initial weight values of all the output
     * synapse and removes all the weights accumulated during the learning
     * process.
     *
     * @param connections the list of connections from or to the neuron
     */
    public void resetConnectionWeights(ArrayList<Synapse> connections) {
        for (Synapse synapse : connections) {
            synapse.reset();
        }
    }

    /**
     * This function resets the output synapses to their initial state and
     * clears the list.
     *
     * @param connections the list of connections from or to the neuron
     */
    public void removeConnections(ArrayList<Synapse> connections) {
        resetConnectionWeights(connections);
        connections.clear();
    }

    /**
     * This function resets the input and output synapses to their initial state
     * and clears both the input and output lists of connections.
     */
    public void removeAllConnections() {
        this.removeInputConnections();
        this.removeOutputConnections();
    }

    /**
     * This function resets the output synapses to their initial state and
     * clears the list of input synapses.
     */
    public void removeInputConnections() {
        this.removeConnections(this.inputSynapses);
    }

    /**
     * This function resets the output synapses to their initial state and
     * clears the list of output synapses.
     */
    public void removeOutputConnections() {
        this.removeConnections(this.outputSynapses);
    }

    /**
     * Sets the membrane potential to a resting state. The value depends on the
     * core model.
     */
    public void resetMembranePotential() {
        this.getCore().resetMembranePotential();
    }

    /**
     * Gets the current membrane potential of the spiking neuron.
     *
     * @return the membrane potential of the spiking neuron.
     */
    public double getMembranePotential() {
        return this.getCore().getMembranePotential();
    }

    /**
     * Reset the number of spikes {@link resetNumberOfSpikes()} and clears the
     * core {
     *
     * @see jneucube.spikingNeurons.cores.Core#clear()}
     */
    public void resetActivity() {
        this.resetNumberOfSpikes();
        this.resetCurrent();
        this.core.clear();
    }

    /**
     * This method is executed by a presynaptic neuron, so that all neurons
     * connected to this neuron will receive a spike. The amount of current
     * "released" in those neurons will increase (more positive or more
     * negative) according to the type of synapse (excitatory or inhibitory).
     *
     * @param reservoir
     */
    public void propagateSpike(ArrayList<SpikingNeuron> reservoir) {
        this.increaseSpikesEmitted();
        for (Synapse synapse : this.outputSynapses) {  // 
            synapse.addStimuli();
            synapse.releaseSpike(reservoir);
        }
    }

    /**
     * This function updates the amount of current "released" from the synapse
     * due to a incoming spike from the presynaptic neuron.
     *
     * @param current The amount of current released due to an incoming spike.
     * It is the synaptic weight.
     */
    public void receiveSpike(double current) {
        this.increaseSpikesReceived();
        this.current += current;        // Acumulates the current that will be used to compute the membrane potential
        this.stimulated = true;         // Indicates whether the neuron has received a spike
    }

    /**
     *
     * @return the last spike of the neuron
     */
    public int getLastSpikeTime() {
        return this.core.getLastSpikeTime();
    }

    /**
     * This function returns all the neurons to which this neuron is connected
     * to. All the presynaptic (output) connections.
     *
     * @param reservoir
     * @return
     */
    public ArrayList<SpikingNeuron> getPostsynapticNeurons(ArrayList<SpikingNeuron> reservoir) {
        ArrayList<SpikingNeuron> neurons = new ArrayList<>();
        for (Synapse synapse : this.getOutputSynapses()) {
            neurons.add(reservoir.get(synapse.getTargetNeuronIdx()));
        }
        return neurons;
    }

    /**
     * This function returns all the neurons that are connected to this neuron.
     * All the postsynaptic (input connections)
     *
     * @param reservoir
     * @return
     */
    public ArrayList<SpikingNeuron> getPresynapticNeurons(ArrayList<SpikingNeuron> reservoir) {
        ArrayList<SpikingNeuron> neurons = new ArrayList<>();
        for (Synapse synapse : this.getInputSynapses()) {
            //neurons.add(synapse.getSourceNeuron());
            neurons.add(reservoir.get(synapse.getSourceNeuronIdx()));
        }
        return neurons;
    }

    /**
     * Duplicates the spiking neuron properties with the exception of the
     * synapses
     *
     * @return
     * @throws CloneNotSupportedException
     */
    @Override
    public SpikingNeuron clone() throws CloneNotSupportedException {
        SpikingNeuron neuron = null;
        try {
            neuron = (SpikingNeuron) super.clone();
            neuron.core = core.clone();
            neuron.inputSynapses = new ArrayList<>();
            neuron.outputSynapses = new ArrayList<>();
        } catch (CloneNotSupportedException ex) {
            LOGGER.error(ex);
        }
        return neuron;
    }

    /**
     * Indicates whether the core has emitted a spike or not
     *
     * @return
     */
    public boolean isFired() {
        return this.core.isFired();
    }

    /**
     * Indicates whether the neuron emitted a spike in a specific time step. The
     * result is obtained from a spike train produced by the neuron
     *
     * @param time
     * @return
     */
    public boolean isFiredSpikeTrain(double time) {
        return this.core.getSpikeTrain()[(int) time] != 0;  // positive or negative spikes
    }

    /**
     * Indicates whether the neuron emitted a spike in a specific time step. The
     * result is obtained from the list of firing times.
     *
     * @param time
     * @return
     */
    public boolean isFiredFiringList(int time) {
        return this.core.getFirings().parallelStream().anyMatch((firing) -> (firing == time));
    }

    /**
     * Increments by one the number of spikes received from a presynaptic
     * neuron. Function utilized for calculating some statistics.
     */
    public void increaseSpikesReceived() {
        this.numSpikesReceived++;
    }

    /**
     * Increments by one the number of spikes emitted to the postsynaptic
     * neuron. Function utilized for calculating some statistics.
     */
    public void increaseSpikesEmitted() {
        this.numSpikesEmitted++;
    }

    /**
     * @return the core
     */
    public Core getCore() {
        return core;
    }

    /**
     * @param core the core to set
     */
    public void setCore(Core core) {
        this.core = core;
    }

    /**
     * @return the posX
     */
    public double getPosX() {
        return posX;
    }

    /**
     * @param posX the posX to set
     */
    public void setPosX(double posX) {
        this.posX = posX;
    }

    /**
     * @return the posY
     */
    public double getPosY() {
        return posY;
    }

    /**
     * @param posY the posY to set
     */
    public void setPosY(double posY) {
        this.posY = posY;
    }

    /**
     * @return the posZ
     */
    public double getPosZ() {
        return posZ;
    }

    /**
     * @param posZ the posZ to set
     */
    public void setPosZ(double posZ) {
        this.posZ = posZ;
    }

    /**
     * @return the idx
     */
    public int getIdx() {
        return idx;
    }

    /**
     * @param idx the idx to set
     */
    public void setIdx(int idx) {
        this.idx = idx;
    }

    /**
     * Set the possition of the neuron in a 3D space.
     *
     * @param posX coordinate in the x axe
     * @param posY coordinate in the y axe
     * @param posZ coordinate in the z axe
     */
    public void setPosXYZ(double posX, double posY, double posZ) {
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
    }

    /**
     * Set the possition of the neuron in a 3D space giving a set of
     * coordinates.
     *
     * @param coordinates the X,Y and Z coordinates
     */
    public void setPosXYZ(double[] coordinates) {
        this.posX = coordinates[0];
        this.posY = coordinates[1];
        this.posZ = coordinates[2];
    }

    public double[] getPosXYZ() {
        return new double[]{this.posX, this.posY, this.posZ};
    }

    /**
     * @return the region
     */
    public int getRegion() {
        return region;
    }

    /**
     * @param region the region to set
     */
    public void setRegion(int region) {
        this.region = region;
    }

    public String getStrType() {
        return (this.getNeuronType() == NeuronType.RESERVOIR_NEURON) ? "Reservoir" : "Input";
    }

    public void showProperties() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("Id: ").append(this.idx).append("\n");
        buffer.append("Label: ").append(this.label).append("\n");
        buffer.append("XYZ: ").append(this.getPosX()).append(" ").append(this.getPosY()).append(" ").append(this.getPosZ()).append("\n");
        buffer.append("Brain position:").append(this.region);
        System.out.println(buffer.toString());
        this.core.showProperties();
        System.out.println("Output synapses");
        this.printOutputSynapseWeights();
        System.out.println("Input synapses");
        this.printInputSynapseWeights();

    }

    public String getSringCoordinates() {
        return this.getPosX() + " " + this.getPosY() + " " + this.getPosZ();
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @return the neuronType
     */
    public int getNeuronType() {
        return neuronType;
    }

    /**
     * @param neuronType the neuronType to set
     */
    public void setNeuronType(int neuronType) {
        this.neuronType = neuronType;
    }

    public void addOutputSynapse(Synapse synapse) {
        this.outputSynapses.add(synapse);
    }

    public void addInputSynapse(Synapse synapse) {
        this.inputSynapses.add(synapse);
    }

    /**
     * @return the outputSynapses
     */
    public ArrayList<Synapse> getOutputSynapses() {
        return outputSynapses;
    }

    /**
     * @param outputSynapses the outputSynapses to set
     */
    public void setOutputSynapses(ArrayList<Synapse> outputSynapses) {
        this.outputSynapses = outputSynapses;
    }

    /**
     * Searches for the synapse between the presynaptic neuron and this neuron,
     * and returns the weight.
     *
     * @param neuronIdx The presynaptic neuron identifier
     * @return The connection value between the presynaptic neuron and the current
     * neuron
     */
    public Synapse getOutputSynapse(int neuronIdx) {
        Synapse synapse = null;
        for (Synapse outputSynapse : this.outputSynapses) {
            if (outputSynapse.getTargetNeuronIdx() == neuronIdx) {
                return outputSynapse;
            }
        }
        return synapse;
    }

    /**
     * Searches for the synapse between the presynaptic neuron and this neuron
     *
     * @param neuronIdx The presynaptic neuron identifier
     * @return The connection between the presynaptic neuron and the current
     * neuron
     */
    public double getOutputSynapseWeight(int neuronIdx) {
        for (Synapse outputSynapse : this.outputSynapses) {
            if (outputSynapse.getTargetNeuronIdx() == neuronIdx) {
                return outputSynapse.getWeight();
            }
        }
        return 0.0;
    }

    /**
     * Searches for the synapse between the presynaptic neuron and this neuron
     *
     * @param neuronIdx The presynaptic neuron identifier
     * @return The connection between the presynaptic neuron and the current
     * neuron
     */
    public Synapse getInputSynapse(int neuronIdx) {
        Synapse synapse = null;
        for (Synapse inputSynapse : this.inputSynapses) {
            if (inputSynapse.getSourceNeuronIdx() == neuronIdx) {
                return inputSynapse;
            }
        }
        return synapse;
    }

    /**
     * Searches for the synapse between the presynaptic neuron and this neuron,
     * and returns the weight.
     *
     * @param neuronIdx The presynaptic neuron identifier
     * @return The connection value between the presynaptic neuron and the
     * current neuron
     */
    public double getInputSynapseWeight(int neuronIdx) {
        for (Synapse inputSynapse : this.inputSynapses) {
            if (inputSynapse.getSourceNeuronIdx() == neuronIdx) {
                return inputSynapse.getWeight();
            }
        }
        return 0.0;
    }

    /**
     * @return the inputSynapses
     */
    public ArrayList<Synapse> getInputSynapses() {
        return inputSynapses;
    }

    /**
     * @param inputSynapses the inputSynapses to set
     */
    public void setInputSynapses(ArrayList<Synapse> inputSynapses) {
        this.inputSynapses = inputSynapses;
    }

    /**
     * @return the numSpikesReceived
     */
    public int getNumSpikesReceived() {
        return numSpikesReceived;
    }

    /**
     * @param numSpikesReceived the numSpikesReceived to set
     */
    public void setNumSpikesReceived(int numSpikesReceived) {
        this.numSpikesReceived = numSpikesReceived;
    }

    /**
     * @return the numSpikesEmitted
     */
    public int getNumSpikesEmitted() {
        return numSpikesEmitted;
    }

    /**
     * @param numSpikesEmitted the numSpikesEmitted to set
     */
    public void setNumSpikesEmitted(int numSpikesEmitted) {
        this.numSpikesEmitted = numSpikesEmitted;
    }

    /**
     *
     * @return the spike train produced by the core after stimulation of the
     * neuron
     */
    public byte[] getSpikeTrain() {
        return this.core.getSpikeTrain();
    }

    public void printSpikeTrain() {
        for (int i = 0; i < this.getSpikeTrain().length; i++) {
            System.out.print(this.getSpikeTrain()[i]);
        }
    }

    public void printlnSpikeTrain() {
        this.printSpikeTrain();
        System.out.println();
    }

    public void printFirings() {
        this.getCore().getFirings().stream().forEach((fireTime) -> {
            System.out.print(fireTime + " ");
        });
        System.out.println("");
    }

    /**
     * Prints the pre synapses with their current weights
     */
    public void printOutputSynapseWeights() {
        for (Synapse synapse : this.outputSynapses) {
            System.out.println(synapse.getSourceNeuronIdx() + "->" + synapse.getTargetNeuronIdx() + " (" + synapse.getIdx() + ") : w=" + synapse.getWeight());
        }
    }

    /**
     * Prints the post synapses with their current weights
     */
    public void printInputSynapseWeights() {
        for (Synapse synapse : this.inputSynapses) {
            System.out.println(synapse.getTargetNeuronIdx() + "->" + synapse.getSourceNeuronIdx() + " (" + synapse.getIdx() + ") : w=" + synapse.getWeight());
        }
    }

    public void printPostSynapseMolecules() {
        for (Synapse synapse : this.inputSynapses) {
            //System.out.print(synapse.getTargetNeuron().getIdx() + "->" + synapse.getSourceNeuron().getIdx() + " : w=" + synapse.getWeight());
            System.out.print(synapse.getTargetNeuronIdx() + "->" + synapse.getSourceNeuronIdx() + " : w=" + synapse.getWeight());
            System.out.print(" A=" + synapse.getAMPAR().getLevel());
            System.out.print(" N=" + synapse.getNMDAR().getLevel());
            System.out.print(" GA=" + synapse.getGABAAR().getLevel());
            System.out.print(" GB=" + synapse.getGABABR().getLevel());
            System.out.println("");
        }
    }

    /**
     * @return the firingProbability
     */
    public double getFiringProbability() {
        return firingProbability;
    }

    /**
     * @param firingProbability the firingProbability to set
     */
    public void setFiringProbability(double firingProbability) {
        this.firingProbability = firingProbability;
    }

    /**
     * @return the current
     */
    public double getCurrent() {
        return current;
    }

    /**
     * @param current the current to set
     */
    public void setCurrent(double current) {
        this.current = current;
    }

    /**
     * @return the stimulated
     */
    public boolean isStimulated() {
        return stimulated;
    }

    /**
     * @param stimulated the stimulated to set
     */
    public void setStimulated(boolean stimulated) {
        this.stimulated = stimulated;
    }

    @Override
    public String toString() {
        return "Neuron " + String.valueOf(this.idx);
    }

    /**
     * @return the maxDelay
     */
    public int getMaxDelay() {
        return maxDelay;
    }

    /**
     * @param maxDelay the maxDelay to set
     */
    public void setMaxDelay(int maxDelay) {
        this.maxDelay = maxDelay;
    }

    /**
     * @return the typeDelay
     */
    public int getTypeDelay() {
        return typeDelay;
    }

    /**
     * @param typeDelay the typeDelay to set
     */
    public void setTypeDelay(int typeDelay) {
        this.typeDelay = typeDelay;
    }

    /**
     * Transforms the list of firings into a spike train
     *
     * @param length
     * @return the spike train
     */
    public byte[] createSpikeTrain(int length) {
        byte[] spikeTrain = new byte[length];
        Arrays.fill(spikeTrain, (byte) 0);
        for (double firings : this.getCore().getFirings()) {
            spikeTrain[(int) firings] = 1;
        }
        return spikeTrain;
    }

    /**
     * @return the type
     */
    public int getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     *
     * @return the firing rate calculated by the specific core
     */
    public double getFiringRate() {
        return this.core.getFiringRate();
    }

    public ArrayList<Double> getFirings() {
        return this.core.getFirings();
    }

    /**
     *
     * @return the average firing rate calculated by the specific core
     */
    public double getAverageFiringRate() {
        return this.core.getAverageFiringRate();
    }

    /**
     * @return the classId
     */
    public double getClassId() {
        return classId;
    }

    /**
     * @param classId the classId to set
     */
    public void setClassId(double classId) {
        this.classId = classId;
    }

}
