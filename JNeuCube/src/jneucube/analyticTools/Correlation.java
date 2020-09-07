/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.analyticTools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import jneucube.network.Network;
import jneucube.spikingNeurons.SpikingNeuron;
import jneucube.spikingNeurons.Synapse;

/**
 * The {@code Correlation} class is a analytic tool for measuring the
 * interchange of information between the temporal variables. 
 *
 * 
 * 
 * @author em9403
 */
public class Correlation {

     
    /**
     * 
     * @param network
     * @param aSet
     * @param bSet 
     */
    public void getCorelation(Network network, HashMap<SpikingNeuron, Integer> aSet,  HashMap<SpikingNeuron, Integer> bSet){
        
        SpikingNeuron targetNeuron;
        SpikingNeuron sourceNeuron;
        int totalSpikesA=0; // total number of spikes 
        int numNeuronsA2B=0;
        int totalSpikesA2B=0;        
        int numCommonNeurons=0;
        int totalSpikesCommonNeurons=0;
        double ratioA2B=0;          // ratio of the number of spikes that neuron b received
      //  ratio of spikes sent from neuron a to neuron b in relation to the number of spikes that b received
        double spikeRatioA2B=0;     // influence of neruon a on neuron b. Number of spikes that neuron a emited form making neuron b fire
        double probA2B=0;
        for(Map.Entry<SpikingNeuron, Integer> entry:aSet.entrySet()){
            sourceNeuron=entry.getKey();
            totalSpikesA+=entry.getValue();            
            for(Synapse synapse:sourceNeuron.getOutputSynapses()){
                targetNeuron= network.getReservoir().get(synapse.getTargetNeuronIdx());                
                if(bSet.containsKey(targetNeuron)){ // if a neuron a is connected to a neuron b 
                    totalSpikesA2B+=entry.getValue(); // sums the number of spikes emmited from neuron a to a connected neuron b 
                    numNeuronsA2B++;                    
                    ratioA2B=entry.getValue()/ targetNeuron.getNumSpikesReceived(); // influence of neuron a on neuron b
                    spikeRatioA2B= entry.getValue()/ bSet.get(targetNeuron); // influence of neuron a on neuron b
                }
            }
            if(bSet.containsKey(sourceNeuron)){
                numCommonNeurons++;
                totalSpikesCommonNeurons+=totalSpikesA+bSet.get(sourceNeuron);
            }
        }
    }
}
