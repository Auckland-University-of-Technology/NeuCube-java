/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.spikingNeurons.cores;

import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author Josafath Israel Espinosa Ramos
 */
public class CoreConstants {

    private CoreConstants() {
    }
    public static final Izhikevich IZHIKEVICH = new Izhikevich();
    public static final SLIF SIMPLIFIED_LIF = new SLIF();
    public static final LIF LIF = new LIF();
    public static final Probabilistic PROBABILISTIC = new Probabilistic();
    public static final ArrayList<Core> CORE_LIST = new ArrayList<>(Arrays.asList(SIMPLIFIED_LIF, LIF, IZHIKEVICH, PROBABILISTIC));
}
