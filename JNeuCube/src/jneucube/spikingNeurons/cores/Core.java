/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.spikingNeurons.cores;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Core is the abstract base class for all type of cores which define the
 * behavior of a spiking neuron, for example, the leaky integrate and fire
 * models, or Izhikevich model. The core encapsulates the differential equations
 * that defines the membrane potential of a neuron in time
 *
 *
 * @author Josafath Israel Espinosa Ramos (Centro de Investigacion en
 * Computacion, Instituto Politecnico Nacional, Mexico 2015)
 */
public abstract class Core implements Cloneable {

    //Functional properties
    //-Dynamics of the model    
    double membranePotential;   // The action potential (voltage)
    //-Status of the core
    boolean fired;              // True if the core is fired. The next step time this value is false    
    int lastSpikeTime;          // The last time the core emitted a spike
    /**
     * The time that the core emitted a spike for the first time. The value is
     * updated by calling the function {@link #setLastSpikeTime(int)} if its
     * value is equals to zero.
     */
    private int firstSpikeTime = 0;         // 
    //-Informaiton properties
    ArrayList<Double> firings = new ArrayList<>();    //A list that contians the times that the core emits a spike
    ArrayList<Double> membranePotentials = new ArrayList<>();    //A list that contians the times that the core emits a spike
    private byte[] spikeTrain;  // The sequence of spikes produced after the core is stimulated with a current    
    //-Properties for calculating the firing rate for irregular input currents
    LinkedList<Byte> firingWindowList = new LinkedList<>(); // shifting window
    LinkedList<Integer> firingsForRate = new LinkedList<>(); // shifting window
    private int deltaWindow = 10;           // Time interval for counting the number of response spikes (The time steps of the shifting window)
    private double firingRate = 0;          // Calcualted by the object
    private int deltaFirings = 0;           // Add or removes the number of firings in a window
    private boolean continuosFiringRate = false; // True in continuous time, false for time window
    private double averageFiringRate = 0.0;

    //Configuration properties
    private boolean recordMembranePotential = false; // True if recording the membrane potential during simulation is needed. For large data it is recormended to be false.    
    private boolean recordFirings = true; // True if recording the firings is needed. For large data it is recormended to be false.    
    private HashMap<String, Object> userData = new HashMap<>();    // A map containing a set of properties defined by the user for this core

    /**
     * Calculates the action potential given a specific current
     *
     * @param time
     * @param current The current (external or synaptic weight) to stimulate the
     * model.
     */
    public abstract void computeMembranePotential(int time, double current);

    /**
     * Reset the core according to the model
     */
    public abstract void reset();

    /**
     * Resets the core's membrane potential to a specific value according to the
     * model
     */
    public abstract void resetMembranePotential();

    /**
     * Shows the core's properties
     */
    public abstract void showProperties();

    public abstract void calculateFiringRate(double current);

    /**
     *
     * @param time The time at which the core emitted a spike
     */
    public void addFirings(double time) {
        //this.spikeTrain[(int) time]=1;  // add
        this.getFirings().add(time);
    }

    public void clear() {
        this.lastSpikeTime = 0;
        this.firings.clear();
        this.spikeTrain = null;
        this.firingWindowList.clear();
        this.firingsForRate.clear();
        this.membranePotentials.clear();
        this.deltaWindow = 10;
        this.firingRate = 0.0;
        this.deltaFirings = 0;
        this.averageFiringRate = 0.0;
        this.reset();
    }

    @Override
    public Core clone() throws CloneNotSupportedException {
        Core core = (Core) super.clone();
        core.firings = new ArrayList<>();
        core.spikeTrain = null;
        return core;
    }

    public void resetFiringRateProperties() {

    }

    /**
     * @return the firing time list
     */
    public ArrayList<Double> getFirings() {
        return firings;
    }

    /**
     * @return the fired
     */
    public boolean isFired() {
        return fired;
    }

    /**
     * @param fired the fired to set. True if the core's action potential
     * surpassed the spiking threshold value
     */
    public void setFired(boolean fired) {
        this.fired = fired;
        if (fired) {
            this.deltaFirings++; // number of firings in an interval (utilised for the calculation of the firing rate)
        }

    }

    /**
     * @return the membranePotential
     */
    public double getMembranePotential() {
        return membranePotential;
    }

    /**
     * @param membranePotential the membranePotential to set
     */
    public void setMembranePotential(double membranePotential) {
        this.membranePotential = membranePotential;
    }

    /**
     * @return the lastSpikeTime
     */
    public int getLastSpikeTime() {
        return lastSpikeTime;
    }

    /**
     * This function records the time of a spike and keeps that value until a
     * new spike is emitted. If the core hasn't emitted a spike, then the
     * function records the time of the first spike by setting the variable {@link #firstSpikeTime) equals  to zero.
     *
     * @param timePoint the lastSpikeTime to set
     */
    public void setLastSpikeTime(int timePoint) {
        this.lastSpikeTime = timePoint;
        if (this.firstSpikeTime == 0) {
            this.firstSpikeTime = timePoint;
        }
    }

    /**
     * @return the spikeTrain
     */
    public byte[] getSpikeTrain() {
        return spikeTrain;
    }

    public void setSpikeTrain(byte[] spikeTrain) {
        this.spikeTrain = spikeTrain;
    }

    /**
     * @return the recordFirings
     */
    public boolean isRecordFirings() {
        return recordFirings;
    }

    /**
     * @param recordFirings the recordFirings to set
     */
    public void setRecordFirings(boolean recordFirings) {
        this.recordFirings = recordFirings;
    }

    public void calculateAverageFiringRate(double currentFiringRate, double previousFiringRate, int time) {
        if (time > 0) {
            double dif = Math.abs(currentFiringRate - previousFiringRate);
            //avg = (1.0 / time) * (dif + (time - 1) * this.averageFiringRate);
            this.averageFiringRate = (dif + (time - 1) * this.averageFiringRate) / time;
        }
    }

    public void calculateFiringRate(int time) {
        byte newData;
        double previousFiringRate = this.firingRate;
        if (this.continuosFiringRate) {
            this.firingRate = (this.getFirings().size() * 1.0) / time;
        } else {
            newData = (this.isFired()) ? (byte) 0x01 : (byte) 0x0;
            if (this.firingWindowList.size() == this.deltaWindow) {
                if (this.firingWindowList.get(0) == 0x01) {
                    this.deltaFirings--;
                }
                this.firingWindowList.set(0, newData);
                this.firingWindowList.addLast(this.firingWindowList.removeFirst());
            } else {
                this.firingWindowList.add(newData);
            }
            this.firingRate = (this.deltaFirings * 1.0) / this.deltaWindow;
        }
        this.calculateAverageFiringRate(this.firingRate, previousFiringRate, time);
    }

    public void calculateFiringRate2(int time) {
        double previousFiringRate = this.firingRate;
        if (this.isFired()) {
            if (this.firingsForRate.size() < this.deltaWindow) {
                this.firingsForRate.add(time);
            } else if (this.firingsForRate.getFirst() < (time - this.deltaWindow)) {
                this.firingsForRate.set(0, time);
                this.firingsForRate.addLast(this.firingsForRate.removeFirst());
            }
        }
        this.firingRate = (this.firingsForRate.size() * 0.1) / this.deltaWindow;
        this.calculateAverageFiringRate(this.firingRate, previousFiringRate, time);
    }

    /**
     * @return the deltaWindow
     */
    public int getDeltaWindow() {
        return deltaWindow;
    }

    /**
     * @param deltaWindow the deltaWindow to set
     */
    public void setDeltaWindow(int deltaWindow) {
        this.deltaWindow = deltaWindow;
    }

    /**
     * @return the firingRate
     */
    public double getFiringRate() {
        return firingRate;
    }

    /**
     * @param firingRate the firingRate to set
     */
    public void setFiringRate(double firingRate) {
        this.firingRate = firingRate;
    }

    /**
     * @return the continuosFiringRate
     */
    public boolean isContinuosFiringRate() {
        return continuosFiringRate;
    }

    /**
     * @param continuosFiringRate the continuosFiringRate to set
     */
    public void setContinuosFiringRate(boolean continuosFiringRate) {
        this.continuosFiringRate = continuosFiringRate;
    }

    /**
     * @return the averageFiringRate
     */
    public double getAverageFiringRate() {
        return averageFiringRate;
    }

    /**
     * @param averageFiringRate the averageFiringRate to set
     */
    public void setAverageFiringRate(double averageFiringRate) {
        this.averageFiringRate = averageFiringRate;
    }

    /**
     * Sets a new property or updates a property previously defined by the user
     *
     * @param property
     * @param value
     */
    public void setUserData(String property, Object value) {
        this.getUserData().put(property, value);
    }

    /**
     * Gets the value of a property previously defined by the user
     *
     * @param property the name of a property
     * @return
     */
    public Object getUserData(String property) {
        return this.getUserData().get(property);
    }

    /**
     * @return the userData
     */
    public HashMap<String, Object> getUserData() {
        return userData;
    }

    /**
     * @param userData the userData to set
     */
    public void setUserData(HashMap<String, Object> userData) {
        this.userData = userData;
    }

    /**
     * @return true if the model will record the membrane potential
     */
    public boolean isRecordMembranePotential() {
        return recordMembranePotential;
    }

    /**
     * Set true if the model will record the membrane potential during
     * simulation.
     *
     * @param recordMembranePotential the recordMembranePotential to set
     */
    public void setRecordMembranePotential(boolean recordMembranePotential) {
        this.recordMembranePotential = recordMembranePotential;
    }

    /**
     * @return the firstSpikeTime
     */
    public int getFirstSpikeTime() {
        return firstSpikeTime;
    }

    /**
     * @param firstSpikeTime the firstSpikeTime to set
     */
    public void setFirstSpikeTime(int firstSpikeTime) {
        this.firstSpikeTime = firstSpikeTime;
    }

    /**
     * @return the membranePotentials
     */
    public ArrayList<Double> getMembranePotentials() {
        return membranePotentials;
    }

    /**
     * @param membranePotentials the membranePotentials to set
     */
    public void setMembranePotentials(ArrayList<Double> membranePotentials) {
        this.membranePotentials = membranePotentials;
    }

}
