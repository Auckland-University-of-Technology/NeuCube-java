/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jneucube.connectionAlgorithms;

import java.util.ArrayList;
import jneucube.spikingNeurons.SpikingNeuron;
import jneucube.util.Matrix;

/**
 *
 * @author Josafath Israel Espinosa Ramos
 */
public class WattsStrogatz extends ConnectionAlgorithm {
    private int k=4; // neighbours (distance)
    private double beta=0.1;    // probability of rewiring

    @Override
    public Matrix createConnections(ArrayList<SpikingNeuron> neurons) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Matrix createWeights(ArrayList<SpikingNeuron> neurons, Matrix connections) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
