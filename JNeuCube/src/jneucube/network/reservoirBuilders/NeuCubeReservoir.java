/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.network.reservoirBuilders;

import java.util.ArrayList;
import jneucube.network.NetworkController;
import jneucube.spikingNeurons.NeuronType;
import jneucube.spikingNeurons.SpikingNeuron;
import jneucube.spikingNeurons.cores.Core;
import jneucube.spikingNeurons.cores.Izhikevich;
import jneucube.util.Matrix;


/**
 *
 * @author Josafath Israel Espinosa Ramos
 */
public class NeuCubeReservoir implements ReservoirBuilder {

    @Override
    public ArrayList<SpikingNeuron> getReservoirNeurons(NetworkController nController, Matrix reservoirCoordinates) throws CloneNotSupportedException {        
        ArrayList<SpikingNeuron> neuronList = new ArrayList<>();
        int numNeurons = reservoirCoordinates.getRows();
        neuronList.addAll(nController.createNeuronGroup(nController.getNetwork().getSpikingNeuron(), numNeurons));        
        for (int i = 0; i < neuronList.size(); i++) {
            neuronList.get(i).setIdx(i);
            neuronList.get(i).setPosXYZ(reservoirCoordinates.getVecRow(i));
            neuronList.get(i).setNeuronType(NeuronType.RESERVOIR_NEURON);            
        }        

        return neuronList;
    }

    public SpikingNeuron getExcitatoryNeuronModel() {
        SpikingNeuron spikingNeuron = new SpikingNeuron();
        Core core = new Izhikevich('U');
        core.setRecordFirings(false);
        spikingNeuron.setType(NeuronType.EXCITATORY);
        spikingNeuron.setMaxDelay(1);
        spikingNeuron.setTypeDelay(SpikingNeuron.FIXED_DELAY);
        spikingNeuron.setNeuronType(NeuronType.RESERVOIR_NEURON);
        return spikingNeuron;
    }

}
