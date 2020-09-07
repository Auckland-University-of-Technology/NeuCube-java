/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.network.reservoirBuilders;

import java.util.ArrayList;
import java.util.Collections;
import jneucube.network.NetworkController;
import jneucube.spikingNeurons.NeuronType;
import jneucube.spikingNeurons.SpikingNeuron;
import jneucube.spikingNeurons.cores.Core;
import jneucube.spikingNeurons.cores.Izhikevich;
import jneucube.util.Matrix;

/**
 * The {@code MixReservoir} class creates a reservoir with mixed excitatory and
 * inhibitory neurons. It implements the
 * {@link jneucube.network.ReservoirBuilder} interface.
 *
 * @author Josafath Israel Espinosa Ramos
 */
public class MixReservoir implements ReservoirBuilder {

    private double excitatoryNeuronRate = 0.7;

    @Override
    public ArrayList<SpikingNeuron> getReservoirNeurons(NetworkController nController, Matrix reservoirCoordinates) throws CloneNotSupportedException {
        int numNeurons = reservoirCoordinates.getRows();
        int numExcitatoryNeurons = (int) (numNeurons * 0.7);
        int numInhibitoryNeurons = numNeurons - numExcitatoryNeurons;
        SpikingNeuron excitatoryModel = this.getExcitatoryNeuronModel();
        SpikingNeuron inhibitoryModel = this.getInhibitoryNeuronModel();
        ArrayList<SpikingNeuron> neuronList = new ArrayList<>();
        neuronList.addAll(nController.createNeuronGroup(excitatoryModel, numExcitatoryNeurons));
        neuronList.addAll(nController.createNeuronGroup(inhibitoryModel, numInhibitoryNeurons));
        Collections.shuffle(neuronList);
        for (int i = 0; i < neuronList.size(); i++) {
            neuronList.get(i).setIdx(i);
            neuronList.get(i).setPosXYZ(reservoirCoordinates.getVecRow(i));
            neuronList.get(i).setNeuronType(NeuronType.RESERVOIR_NEURON);
            neuronList.get(i).setMaxDelay(1);
            neuronList.get(i).setTypeDelay(SpikingNeuron.FIXED_DELAY);
            neuronList.get(i).getCore().setRecordFirings(false);
        }
        // map the coordinates

        return neuronList;
    }

    public SpikingNeuron getExcitatoryNeuronModel() {
        SpikingNeuron spikingNeuron = new SpikingNeuron();
        Core core = new Izhikevich('U');
        spikingNeuron.setCore(core);
        spikingNeuron.setType(NeuronType.EXCITATORY);
        return spikingNeuron;
    }

    public SpikingNeuron getInhibitoryNeuronModel() {
        SpikingNeuron spikingNeuron = new SpikingNeuron();
        Core core = new Izhikevich('X');
        spikingNeuron.setCore(core);
        spikingNeuron.setType(NeuronType.INHIBITORY);
        return spikingNeuron;
    }

    /**
     * @return the excitatoryNeuronRate
     */
    public double getExcitatoryNeuronRate() {
        return excitatoryNeuronRate;
    }

    /**
     * @param excitatoryNeuronRate the excitatoryNeuronRate to set
     */
    public void setExcitatoryNeuronRate(double excitatoryNeuronRate) {
        this.excitatoryNeuronRate = excitatoryNeuronRate;
    }

}
