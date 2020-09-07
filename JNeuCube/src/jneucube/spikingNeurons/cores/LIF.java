/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.spikingNeurons.cores;

/**
 *
 * @author Josafath Israel Espinosa Ramos
 */
public class LIF extends Core {

    private double thresholdVoltage = 0.1;
    private double resetVoltage = 0.0;
    private int refractoryTime = 0;
    private int thresholdRefractoryTime = 4;

    private double resistance = 1;       // Ohms
    private double capacitance = 10;    // Farads
    double tau = 1.0;   // time step of the Euler method

    public LIF(){
        
    }
    
    public LIF(double thresholdVoltage, int thresholdRefractoryTime){
        this.thresholdVoltage=thresholdVoltage;
        this.thresholdRefractoryTime=thresholdRefractoryTime;
    }
    //private double leakValue = 0.002;
    public void copy(LIF source) {
        this.setResetVoltage(source.getResetVoltage());
        this.setThresholdVoltage(source.getThresholdVoltage());
        this.setThresholdRefractoryTime(source.getThresholdRefractoryTime());
        this.setResistance(source.getResistance());
        this.setCapacitance(source.getCapacitance());
    }

    /**
     * The leaky integrate-and-fire model is defined by Tm (dv/dt)=-v(t)+RI(t).
     * Where tm is the membrane time constant, R the resistance and I the
     * current. The membrane time constant tm is calculated as tm=RC, being C
     * the capacitance. Therefore, in any case that R=1, the equation can be
     * simplified as dv/dt= (-v+I)/tm. As a consequence we deduce that tm=C.
     *
     * @param time
     * @param current
     */
    @Override
    public void computeMembranePotential(int time, double current) {
        this.setFired(false);

        if (time > refractoryTime) {
            this.membranePotential += this.tau * (-(this.membranePotential - current * this.resistance) / (this.resistance * this.capacitance)); // the real LIF one
            if (this.isRecordMembranePotential()) {
                this.membranePotentials.add(this.membranePotential);
            }
            if (this.membranePotential >= this.thresholdVoltage) {
                this.membranePotential = this.resetVoltage;
                this.refractoryTime = time + this.thresholdRefractoryTime;
                this.setFired(true);
                this.setLastSpikeTime(time);
                if (this.isRecordFirings()) {
                    this.addFirings(time);
                }
            }
        } else {
            if (this.isRecordMembranePotential()) {
                this.membranePotentials.add(this.membranePotential);
            }
        }

        //this.calculateFiringRate(time);
        this.calculateFiringRate(current);
    }

    /**
     * Calculates the firing rate given a current f = ∆abs + τm (ln (RI/ RI −
     * vth)) ∆abs: absolute refractory period τm: RC vth: membrane potential
     * threshold
     *
     * @param current
     */
    @Override
    public void calculateFiringRate(double current) {
        double f = 0.0;
        double tm = this.resistance * this.capacitance;
        f = 1 / (this.thresholdRefractoryTime + (tm * Math.log((this.resistance * current) / ((this.resistance * current) - this.thresholdVoltage))));
        this.setFiringRate(f);
    }

    @Override
    public void reset() {
        this.resetMembranePotential();
        this.resetRefractoryTime();
        this.fired = false;
    }

    @Override
    public void resetMembranePotential() {
        this.membranePotential = this.resetVoltage;
    }

    public void resetRefractoryTime() {
        this.refractoryTime = 0;
    }

    public double[][] run(double current, int simulationTime) {
        this.membranePotential = this.resetVoltage;
        double[][] data = new double[2][simulationTime];
        for (int i = 0; i < simulationTime; i++) {
            computeMembranePotential(i, current);
            data[0][i] = i * 1.0;
            data[1][i] = this.membranePotential;
        }
        return data;
    }

    @Override
    public String toString() {
        return "Leaky I&F";
    }

    /**
     * @return the thresholdVoltage
     */
    public double getThresholdVoltage() {
        return thresholdVoltage;
    }

    /**
     * @param thresholdVoltage the thresholdVoltage to set
     */
    public void setThresholdVoltage(double thresholdVoltage) {
        this.thresholdVoltage = thresholdVoltage;
    }

    /**
     * @return the resetVoltage
     */
    public double getResetVoltage() {
        return resetVoltage;
    }

    /**
     * @param resetVoltage the resetVoltage to set
     */
    public void setResetVoltage(double resetVoltage) {
        this.resetVoltage = resetVoltage;
    }

    /**
     * @return the refractoryTime
     */
    public int getRefractoryTime() {
        return refractoryTime;
    }

    /**
     * @param refractoryTime the refractoryTime to set
     */
    public void setRefractoryTime(int refractoryTime) {
        this.refractoryTime = refractoryTime;
    }

    /**
     * @return the thresholdRefractoryTime
     */
    public int getThresholdRefractoryTime() {
        return thresholdRefractoryTime;
    }

    /**
     * @param thresholdRefractoryTime the thresholdRefractoryTime to set
     */
    public void setThresholdRefractoryTime(int thresholdRefractoryTime) {
        this.thresholdRefractoryTime = thresholdRefractoryTime;
    }

    public void duplicate(LIF target) {
        this.setMembranePotential(target.getMembranePotential());
        this.setCapacitance(target.getCapacitance());
        this.setResistance(target.getResistance());
        this.setResetVoltage(target.getResetVoltage());
        this.setThresholdRefractoryTime(target.getThresholdRefractoryTime());
        this.setThresholdVoltage(target.getThresholdVoltage());
    }

    @Override
    public void showProperties() {
        System.out.println("Core " + this.toString());
        System.out.println("Resitance " + this.getResistance());
        System.out.println("Capacitance " + this.getCapacitance());
        System.out.println("Threshold voltage " + this.getThresholdVoltage());
        System.out.println("Reset voltage " + this.getResetVoltage());
        System.out.println("Threshold refractory time " + this.getThresholdRefractoryTime());
        System.out.println("Firings " + this.getFirings().size());
    }

    /**
     * @return the resistance
     */
    public double getResistance() {
        return resistance;
    }

    /**
     * @param resistance the resistance to set
     */
    public void setResistance(double resistance) {
        this.resistance = resistance;
    }

    /**
     * @return the capacitance
     */
    public double getCapacitance() {
        return capacitance;
    }

    /**
     * @param capacitance the capacitance to set
     */
    public void setCapacitance(double capacitance) {
        this.capacitance = capacitance;
    }

    public static void main(String args[]) {
        LIF lif = new LIF();
        lif.setRecordMembranePotential(true);
        int duration = 500;
        double current = 0.5;
        for (int i = 0; i < duration; i++) {
            lif.computeMembranePotential(i, current);
        }
        for (Double voltage : lif.getMembranePotentials()) {
            System.out.println(voltage);
        }
//        for (Double firing : lif.getFirings()) {
//            System.out.println(firing);
//        }
    }

}
