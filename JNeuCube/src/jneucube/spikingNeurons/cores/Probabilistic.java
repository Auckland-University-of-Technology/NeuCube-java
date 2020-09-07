/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.spikingNeurons.cores;

import java.util.Random;

/**
 *
 * @author Josafath Israel Espinosa Ramos
 */
public class Probabilistic extends Core {

    private double thresholdVoltage = 1.0;
    private double resetVoltage = 0.0;
    private int refractoryTime = -1; //  The elapsed time since the last spike
    private int thresholdRefractoryTime = 4;    //

    private double resistance = 1;       // Ohms
    private double capacitance = 10;    // Farads
    private double tau = 1.0;   // time step of the Euler method

    private double thresholdProbability = 1.0;  // The probability of emitting a spike 
    private double thresholdRate = 0.0;    // the percentage between the reset and the threshold values

    // this properties are utilised for calculating a firing rate scalability factor. 
    double scalabilityFactor = 0.0;
    private double predifinedFiringRate = 0.4;  // Design choice (may depend on the type of the neuron, e.g. 40Hz = 40/1000 ms, 4/100 ms, 0.4/10 ms) 
    private double gamma = 0.2;                 // Constant factor    
    Random rand = new Random();

    public static void main(String args[]) {
        Probabilistic core = new Probabilistic();
        int sumulationTime = 1000;
        double constantCurrent = 1.1;
        core.setThresholdVoltage(1.0);
        core.setThresholdRefractoryTime(4); // 4 time steps after firing
        core.setContinuosFiringRate(false); // false= Shifting window
        core.setThresholdRate(0.2);         // 20% of the threshold voltage for firing
        core.setThresholdProbability(0.5);  // Firing probaility if the voltage is in the range for producing a spike ()
        core.setDeltaWindow(10);            // 10 time steps. In this example 10 ms
        core.setPredifinedFiringRate(0.4);  // Firing rate expressed in Hz (in this exaple we stimulate the neuron during 1000 ms. However the fring rate is calculated in a 10 ms window)
        core.setGamma(0.2);
        double[][] data = core.run(constantCurrent, sumulationTime);

        System.out.println("time, voltage, fired, firing rate, average firing rate, scalability factor, threshold probability");
        for (int i = 0; i < sumulationTime; i++) {
            System.out.println(data[i][0] + ", " + data[i][1] + ", " + data[i][2] + ", " + data[i][3] + ", " + data[i][4] + ", " + data[i][5] + ", " + data[i][6]);
//            for(int j=0;j<data.length;j++){
//                System.out.print(data[i][j] +",");
//            }            
        }
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
            this.membranePotential = this.membranePotential + this.tau * (-(this.membranePotential - current * this.resistance) / (this.resistance * this.capacitance)); // the real LIF one
            if ((this.membranePotential >= this.thresholdVoltage) || ((rand.nextDouble() < this.thresholdProbability) && (this.membranePotential > (this.thresholdVoltage - ((this.thresholdVoltage - this.resetVoltage) * this.thresholdRate))))) {
                this.membranePotential = this.resetVoltage;
                this.refractoryTime = time + this.thresholdRefractoryTime;
                this.setFired(true);
                this.setLastSpikeTime(time);
                if (this.isRecordFirings()) {
                    this.addFirings(time);
                }
            }
        }
        //this.calculateFiringRate2(time);
        this.calculateFiringRate(time);

        this.scalabilityFactor = this.getAverageFiringRate() / (this.getDeltaWindow() * (1 + Math.abs(1 - this.getAverageFiringRate() / this.predifinedFiringRate) * this.getGamma()));

        if (this.getFiringRate() > this.predifinedFiringRate) {  // Saturation
            this.thresholdProbability = this.thresholdProbability - (this.thresholdProbability * this.scalabilityFactor);
        } else {
            this.thresholdProbability = this.thresholdProbability + (this.thresholdProbability * this.scalabilityFactor);
        }
    }


    
    

    public double[][] run(double current, int simulationTime) {
        this.membranePotential = this.resetVoltage;
        double[][] data = new double[simulationTime][7];
        for (int i = 0; i < simulationTime; i++) {
            computeMembranePotential(i, current);
            data[i][0] = i * 1.0;
            data[i][1] = this.membranePotential;
            data[i][2] = (this.isFired()) ? 1 : 0;
            data[i][3] = this.getFiringRate();
            data[i][4] = this.getAverageFiringRate();
            data[i][5] = this.scalabilityFactor;
            data[i][6] = this.thresholdProbability;
        }
        return data;
    }

    @Override
    public void reset() {
        this.resetMembranePotential();
        this.resetRefractoryTime();
    }

    @Override
    public void resetMembranePotential() {
        this.membranePotential = this.resetVoltage;
    }

    public void resetRefractoryTime() {
        this.refractoryTime = 0;
    }

    @Override
    public void showProperties() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String toString() {
        return "Probabilistic";
    }

    public void copy(Probabilistic source) {
        this.setResetVoltage(source.getResetVoltage());
        this.setThresholdVoltage(source.getThresholdVoltage());
        this.setThresholdRefractoryTime(source.getThresholdRefractoryTime());
        this.setResistance(source.getResistance());
        this.setCapacitance(source.getCapacitance());
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

    /**
     * @return the tau
     */
    public double getTau() {
        return tau;
    }

    /**
     * @param tau the tau to set
     */
    public void setTau(double tau) {
        this.tau = tau;
    }

    /**
     * @param thresholdProbability the thresholdProbability to set
     */
    public void setThresholdProbability(double thresholdProbability) {
        this.thresholdProbability = thresholdProbability;
    }

    /**
     * @return the thresholdRate
     */
    public double getThresholdRate() {
        return thresholdRate;
    }

    /**
     * @param thresholdRate the thresholdRate to set
     */
    public void setThresholdRate(double thresholdRate) {
        this.thresholdRate = thresholdRate;
    }

    /**
     * @return the predifinedFiringRate
     */
    public double getPredifinedFiringRate() {
        return predifinedFiringRate;
    }

    /**
     * @param predifinedFiringRate the predifinedFiringRate to set
     */
    public void setPredifinedFiringRate(double predifinedFiringRate) {
        this.predifinedFiringRate = predifinedFiringRate;
    }

    /**
     * @return the scalabilityFactor
     */
    public double getScalabilityFactor() {
        return scalabilityFactor;
    }

    /**
     * @param scalabilityFactor the scalabilityFactor to set
     */
    public void setScalabilityFactor(double scalabilityFactor) {
        this.scalabilityFactor = scalabilityFactor;
    }

    /**
     * @return the gamma
     */
    public double getGamma() {
        return gamma;
    }

    /**
     * @param gamma the gamma to set
     */
    public void setGamma(double gamma) {
        this.gamma = gamma;
    }

    @Override
    public void calculateFiringRate(double current) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
