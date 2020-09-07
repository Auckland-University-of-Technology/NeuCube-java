/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.spikingNeurons.cores;



/**
 * A simplification of the leaky integrate and fired model
 *
 * @author Josafath Israel Espinosa Ramos (Centro de Investigacion en
 * Computacion, Instituto Politecnico Nacional, Mexico 2015)
 */
public class SLIF extends Core {

    private double thresholdVoltage = 0.5;
    private double resetVoltage = 0.0;
    private int refractoryTime = -1;
    private int thresholdRefractoryTime = 6;
    private double leakValue = 0.002;

    public void copy(SLIF source) {
        this.setResetVoltage(source.getResetVoltage());
        this.setThresholdVoltage(source.getThresholdVoltage());
        this.setThresholdRefractoryTime(source.getThresholdRefractoryTime());
        this.setLeakValue(source.getLeakValue());
    }

    @Override
    public void computeMembranePotential(int time, double current) {
        this.setFired(false);
        if (time > refractoryTime) {
            if (current != 0.0) {
                this.membranePotential += current;
            } else {
                this.membranePotential = Math.max(this.resetVoltage, this.membranePotential - this.leakValue);
            }
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

        this.calculateFiringRate(time);
        //System.out.println("core "+System.identityHashCode(this));
        //System.out.println("core "+Integer.toHexString(System.identityHashCode(this)));

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
        this.refractoryTime = -1;
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
        return "Simplified LIF";
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
     * @return the leakValue
     */
    public double getLeakValue() {
        return leakValue;
    }

    /**
     * @param leakValue the leakValue to set
     */
    public void setLeakValue(double leakValue) {
        this.leakValue = leakValue;
    }

    public void duplicate(SLIF target) {
        this.setMembranePotential(target.getMembranePotential());
        this.setLeakValue(target.getLeakValue());
        this.setResetVoltage(target.getResetVoltage());
        this.setThresholdRefractoryTime(target.getThresholdRefractoryTime());
        this.setThresholdVoltage(target.getThresholdVoltage());
    }

    @Override
    public void showProperties() {
        System.out.println("Core " + this.toString());
        System.out.println("Threshold voltage " + this.getThresholdVoltage());
        System.out.println("Reset voltage " + this.getResetVoltage());
        System.out.println("Threshold refractory time " + this.getThresholdRefractoryTime());
        System.out.println("Leak value " + this.getLeakValue());
        System.out.println("Firings " + this.getFirings().size());
    }

    @Override
    public void calculateFiringRate(double current) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
