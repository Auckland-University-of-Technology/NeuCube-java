/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.spikingNeurons;

import java.util.Random;


/**
 * 
 * @author Josafath Israel Espinosa Ramos (Centro de Investigacion en Computacion, Instituto Politecnico Nacional, Mexico 2015)
 */
public class NeuroReceptor {

    private double min;
    private double max;
    private double gain;
    private double level;
    private int alias;  // 1 AMPAR, 2 NMDAR, 3 GABAAR, 4 GABABR

    public NeuroReceptor(double min, double max, double gain) {
        Random r = new Random();
        this.min=min;
        this.max=max;
        this.gain=gain;
        level = min + (max - min) * r.nextDouble();
    }

    /**
     * @return the min
     */
    public double getMin() {
        return min;
    }

    /**
     * @param min the min to set
     */
    public void setMin(double min) {
        this.min = min;
    }

    /**
     * @return the max
     */
    public double getMax() {
        return max;
    }

    /**
     * @param max the max to set
     */
    public void setMax(double max) {
        this.max = max;
    }

    /**
     * @return the gain
     */
    public double getGain() {
        return gain;
    }

    /**
     * @param gain the gain to set
     */
    public void setGain(double gain) {
        this.gain = gain;
    }

    /**
     * @return the level
     */
    public double getLevel() {
        return level;
    }

    /**
     * @param level the level to set
     */
    public void setLevel(double level) {
        this.level = level;
    }

    /**
     * @return the alias
     */
    public int getAlias() {
        return alias;
    }

    /**
     * @param alias the alias to set
     */
    public void setAlias(int alias) {
        this.alias = alias;
    }

}
