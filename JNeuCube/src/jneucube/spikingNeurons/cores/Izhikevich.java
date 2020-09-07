/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.spikingNeurons.cores;

import java.util.ArrayList;
import java.util.Arrays;
import jneucube.util.Util;

/**
 * This object calculates the dynamics of the Izhikevich model.
 *
 * @author Josafath Israel Espinosa Ramos (Centro de Investigacion en
 * Computacion, Instituto Politecnico Nacional, Mexico 2015)
 *
 * @see "Simple model of spiking neurons" and "Which model to use for cortical
 * spiking neurons" articles
 *
 */
public final class Izhikevich extends Core {

    double a;   // Describes the time scale of the recovery variable u
    double b;   // Describes the sensitivity of the recovery variable u to the subthreshold fluctuations of the membrane potential v
    double c;   // Describes the after-spike reset value of the membrane potential v caused by the fast high-threshold K+ conductances
    double d;   // Describes after-spike reset of the recovery variable u caused by slowhigh-threshold Na+ andK+ conductances
    double I;   // The input current. It can be generated by the activity of presynaptic neurons
    double v = -65.0;   // The membrane voltage. This initial value is the resting state.
    double u = -20.0;   // The recovery variable for negative feedback.
    double tau = 1.0;   // It works for 1 ms precision
    private final double thresholdVoltage = 30.0;   // This value in this model is not a threshold, but the peak of the spike. The threshold value of the model neuron is between –70 and -50, and it is dynamic, as in biological neurons
    public static final ArrayList<String> IZHIKEVICH_BEHAVIOURS = new ArrayList<>(Arrays.asList("A tonic spiking", "B phasic spiking", "C tonic bursting", "D phasic bursting",
            "E mixed mode", "F spike frequency adaptation", "G Class 1", "H Class 2",
            "I spike latency", "J subthreshold oscillations", "K resonator",
            "L integrator", "M rebound spike", "N rebound burst", "O threshold variability",
            "P bistability", "Q DAP", "R accomodation", "S inhibition-induced spiking",
            "T inhibition-induced bursting",    // A-T values take from the tutorial program https://www.izhikevich.org/publications/whichmod.htm#izhikevich
            "U Regular spiking neurons", "V Intrinsically bursting", "W Chattering", // excitatory neurons (from the paper "Simple models of spiking neurons")
            "X Fast spiking", "Y Low-threshold spiking", // inhibitory neurons (from the paper "Simple models of spiking neurons")
            "0 Inhibitory neuron", "1 Excitatory neuron")); // from the program https://www.izhikevich.org/publications/spikes.htm 

    private char behaviour = 'A';
    private String behaviors = "ABCDEFGHIJKLMNOPQRSTUVWXY01"; // U-W excitatory, X-Y inhibitory (from the paper "Simple models of spiking neurons"), 0 inhibitory, 1 excitatory

    /**
     *
     * @param behaviour from A to T
     */
    public Izhikevich(char behaviour) {
        this.setBehaviour(behaviour);
    }

    /**
     *
     * @param type inhibitory 0, excitatory 1
     */
    public Izhikevich(int type) {

    }

    public Izhikevich() {
    }

    /**
     * Calculates the membrane voltage and the recovery variable given an input
     * current. If the membrane voltage surpasses a threshold (-70 to -50 mV),
     * then the membrane emits a spike which voltage value is set to 30 mV.
     *
     * @param time The stimulation time.
     * @param current The electrical current produced by an intracellular
     * injection or by the activity of presynaptic neurons.
     */
    @Override
    public void computeMembranePotential(int time, double current) {
        this.setFired(false);
        double tempV = v;
        v = v + tau * ((0.04 * v * v) + (5.0 * v) + 140.0 - u + current);
        u = u + tau * a * (b * tempV - u);
        this.setMembranePotential(v);
        if (this.isRecordMembranePotential()) {
            this.membranePotentials.add(this.membranePotential);
        }
        if (this.getMembranePotential() > thresholdVoltage) {
            v = c;
            u = u + d;
            this.setMembranePotential(thresholdVoltage);
            this.setFired(true);
            this.setLastSpikeTime(time);
            this.addFirings(time);
        }
        this.calculateFiringRate(time);
        //this.membranePotential = v;
    }

    public double[][] run(double current, int simulationTime) {
        this.resetMembranePotential();
        this.getFirings().clear();
        double[][] data = new double[2][simulationTime];
        for (int i = 0; i < simulationTime; i++) {
            this.computeMembranePotential(i, current);
            data[0][i] = i * 1.0;
            data[1][i] = this.getMembranePotential();
        }
        return data;
    }

    @Override
    public void reset() {
        resetMembranePotential();
    }

    /**
     * Set the membrane potential and the recovery variable to the resting
     * values
     */
    @Override
    public void resetMembranePotential() {
        this.v = this.getParameterC();
        this.membranePotential = this.v;
        this.resetRecoveryVariable();
    }

    public void resetRecoveryVariable() {
        this.u = -20.0;
    }

    public double[] runToSpikes(double current, int simulationTime) {
        double[] data = new double[simulationTime];
        for (int i = 0; i < simulationTime; i++) {
            computeMembranePotential(i, current);
            if (this.isFired()) {
                data[i] = 1.0;
            } else {
                data[i] = 0.0;
            }
            this.setFired(false);
        }
        return data;
    }

    @Override
    public String toString() {
        return "Izhikevich";
    }

    /**
     * @return the behaviour
     */
    public char getBehaviour() {
        return behaviour;
    }

    /**
     * Set the behaviour of a spiking neuron according to: A-T from the paper
     * "Which model to use for cortical spiking neurons"; U-W excitatory, X-Y
     * inhibitory from the paper "Simple models of spiking neurons"; 0
     * inhibitory, 1 excitatory from the program
     * https://www.izhikevich.org/publications/spikes.htm
     *
     * @param behaviour the behaviour to set
     */
    public void setBehaviour(char behaviour) {
        this.behaviour = behaviour;
        double[][] params = {{0.02, 0.2, -65, 6, 14}, // tonic spiking
        {0.02, 0.25, -65, 6, 0.5}, // phasic spiking
        {0.02, 0.2, -50, 2, 15}, // tonic bursting
        {0.02, 0.25, -55, 0.05, 0.6}, // phasic bursting
        {0.02, 0.2, -55, 4, 10}, // mixed mode
        {0.01, 0.2, -65, 8, 30}, // spike frequency adaptation
        {0.02, -0.1, -55, 6, 0}, // Class 1
        {0.2, 0.26, -65, 0, 1.0}, // Class 2
        {0.02, 0.2, -65, 6, 7}, // spike latency
        {0.05, 0.26, -60, 0, 0}, // subthreshold oscillations
        {0.1, 0.26, -60, -1, 0}, // resonator
        {0.02, -0.1, -55, 6, 0}, // integrator
        {0.03, 0.25, -60, 4, 0}, // rebound spike
        {0.03, 0.25, -52, 0, 0}, // rebound burst
        {0.03, 0.25, -60, 4, 0}, // threshold variability
        {1, 1.5, -60, 0, -65}, // bistability
        {1, 0.2, -60, -21, 0}, // DAP
        {0.02, 1.0, -55, 4, 0}, // accomodation
        {-0.02, -1.0, -60, 8, 80}, // inhibition-induced spiking
        {-0.026, -1.0, -45, 0, 80}, // inhibition-induced bursting

        {0.02, 0.2, -65, 8, 10}, // Regular spiking neurons (excitatory) Simple model of spiking neurons
        {0.02, 0.2, -55, 4, 10}, // Intrinsically bursting (excitatory) Simple model of spiking neurons
        {0.02, 0.2, -50, 2, 10}, // Chattering (excitatory) Simple model of spiking neurons
        {0.1, 0.2, -65, 2, 10}, // Fast spiking (inhibitory) Simple model of spiking neurons
        {0.02, 0.25, -65, 2, 10}, // Low-threshold spiking (inhibitory) Simple model of spiking neurons            
        {Util.getRandom(0.02, 0.1), Util.getRandom(0.2, 0.25), -65, 2.0, Util.getRandom(-6.0, 6.0)}, // inhibitory neurons (from the program https://www.izhikevich.org/publications/spikes.htm)
        {0.02, 0.2, Util.getRandomInt(-65, -49), Util.getRandom(2, 9), Util.getRandom(-15.0, 15.0)}}; // Excitatory neurons (from the program https://www.izhikevich.org/publications/spikes.htm )

        int idx = this.behaviors.indexOf(behaviour);
        this.a = params[idx][0];
        this.b = params[idx][1];
        this.c = params[idx][2];
        this.d = params[idx][3];
        this.I = params[idx][4];
    }

    public double getParameterA() {
        return this.a;
    }

    public double getParameterB() {
        return this.b;
    }

    public double getParameterC() {
        return this.c;
    }

    public double getParameterD() {
        return this.d;
    }

    public double getParameterI() {
        return this.I;
    }

    /**
     * @return the thresholdVoltage
     */
    public double getThresholdVoltage() {
        return thresholdVoltage;
    }

    /**
     * @return the behaviors
     */
    public String getBehaviors() {
        return behaviors;
    }

    @Override
    public void showProperties() {
        System.out.println("Core " + this.toString());
        System.out.println("Behaviour " + IZHIKEVICH_BEHAVIOURS.get(behaviors.indexOf(this.getBehaviour())));
        System.out.println("Firings " + this.getFirings().size());
    }

    public static void main(String args[]) {
        Izhikevich iz = new Izhikevich('H');
        double current = 1.2;
        int duration = 1000;
        iz.setRecordMembranePotential(true);
        for (int i = 0; i < duration; i++) {
            iz.computeMembranePotential(i, current);
        }
        iz.getMembranePotentials().forEach((voltage) -> {
            System.out.println(voltage);
        });
    }

    @Override
    public void calculateFiringRate(double current) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}