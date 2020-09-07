/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.trainingAlgorithms;

import java.util.ArrayList;
import jneucube.spikingNeurons.SpikingNeuron;


/**
 *
 * @author Josafath Israel Espinosa Ramos (Centro de Investigacion en
 * Computacion, Instituto Politecnico Nacional, Mexico 2015)
 */
public class Avalanche {

    private double startTime = 0;
    private double endTime = 0;
    private int size = 0;
    //private ArrayList<Integer> firedNeurons = new ArrayList<>();
    private ArrayList<ArrayList<SpikingNeuron>> firedNeurons =new ArrayList<>();

    public double getDuration() {
        return this.endTime - this.startTime;
    }

    /**
     * @return the startTime
     */
    public double getStartTime() {
        return startTime;
    }

    /**
     * @param startTime the startTime to set
     */
    public void setStartTime(double startTime) {
        this.startTime = startTime;
    }

    /**
     * @return the endTime
     */
    public double getEndTime() {
        return endTime;
    }

    /**
     * @param endTime the endTime to set
     */
    public void setEndTime(double endTime) {
        this.endTime = endTime;
    }

    /**
     * @return the size
     */
    public int getSize() {
        return size;
    }

    /**
     * @param size the size to set
     */
    public void setSize(int size) {
        this.size = size;
    }

    private void increaseSize(int size) {
        this.size += size;
    }

    public void printInfo() {
        System.out.println("Start " + this.startTime + " end " + this.endTime + " duration " + this.getDuration() + " size " + this.size);
    }



    /**
     * Adds the number of fired neurons to a list and increases the size of the
     * avalanche.
     *
     * @param firedNeurons The list containing the fired neurons
     */
    public void addFiredNeurons(ArrayList<SpikingNeuron> firedNeurons) {
        this.firedNeurons.add(firedNeurons);        
        this.increaseSize(firedNeurons.size());
    }

}
