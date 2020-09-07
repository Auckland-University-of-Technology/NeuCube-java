/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.trainingAlgorithms.unsupervised;

import java.util.ArrayList;
import jneucube.data.DataSample;
import jneucube.data.SpatioTemporalData;
import jneucube.network.Network;
import jneucube.spikingNeurons.NeuronType;
import jneucube.spikingNeurons.SpikingNeuron;
import jneucube.spikingNeurons.cores.Izhikevich;
import jneucube.spikingNeurons.cores.SLIF;
import jneucube.trainingAlgorithms.LearningAlgorithm;
import jneucube.util.Matrix;


/**
 * Neuro receptor dependent plasticity rule
 *
 * @author Josafath Israel Espinosa Ramos (Centro de Investigacion en
 * Computacion, Instituto Politecnico Nacional, Mexico 2015)
 */
public class NRDP extends LearningAlgorithm {

    private double minA = 0.0;       // The minimum treshold value for AMPAR
    private double maxA = 0.901679611594126;       // The maximum threshold value for AMPAR
    private double gainA = 0.5428717518563672;    // The value that AMPAR increases after an spike
    private double minN = 0.0;       // The minimum threshold value for NDMAR
    private double maxN = 0.23001290732040292;       // Maximum threshold value for NDMAR
    private double gainN = 0.011660312977761912;    // The value that NDMAR increases after an spike
    private double minGA = 0.0;      // The minimum tHreshold value for GABAGaR
    private double maxGA = 0.7554145024515596;     // The maximum threshold value for GABAGaR
    private double gainGA = 0.3859076787035615; // The value that GA increases when there is no spike
    private double minGB = 0.0;      // The minimum threshold value for GABAGbR
    private double maxGB = 0.7954714253083993;     // The maximum threshold value for GABAGbR
    private double gainGB = 0.11032115434326673; // The value that GB increases in absence of spikes
    private double gabaImpact = 0.01;        // the percentage that GA and GB impacts on A
    private double gabaRate = 0.7;        // The probability of GA occurrence. If GA is not expressed then GB is expressed
    private int timeWindow = 10;
    //private boolean flagGA = false;   // At the begining of the algorithm GA is not expressed but GB (slow inhibitory)

    @Override
    public void train(Network network, ArrayList<DataSample> trainingData) {

    }

    @Override
    public void updateSynapticWeights(ArrayList<SpikingNeuron> firedNeurons, int elapsedTime) {

    }

    @Override
    public void resetFieldsForTraining() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void resetFieldsForSample() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String toString() {
        return "NRDP";
    }

    /**
     * @return the gabaRate
     */
    public double getGabaRate() {
        return gabaRate;
    }

    /**
     * @param gabaRate the gabaRate to set
     */
    public void setGabaRate(double gabaRate) {
        this.gabaRate = gabaRate;
    }

    /**
     * @return the timeWindow
     */
    public int getTimeWindow() {
        return timeWindow;
    }

    /**
     * @param timeWindow the timeWindow to set
     */
    public void setTimeWindow(int timeWindow) {
        this.timeWindow = timeWindow;
    }

    public static void main(String args[]) {
        Network network = new Network();
        SpatioTemporalData std = new SpatioTemporalData();
        Izhikevich iz1 = new Izhikevich('C');
        Izhikevich iz2 = new Izhikevich('C');
        DataSample sample = new DataSample();
        NRDP nrdp = new NRDP();
        nrdp.setTrainingRounds(1);

        Matrix matrix = new Matrix(100, 2, 0.0);
        double[] spikeState1 = iz1.runToSpikes(15, 100);
        double[] spikeState2 = iz2.runToSpikes(15, 100);
        double[][] c = {{0, 0, 1, 1, 0},
        {0, 0, 1, 0, 0},
        {0, 0, 0, 1, 0},
        {0, 0, 0, 0, 1},
        {0, 0, 1, 0, 0}};
        Matrix connections = new Matrix(c);
        Matrix weights = connections.operation('*', 0.2);

        matrix.setRows(0, spikeState1);
        matrix.setRows(1, spikeState2);

        sample.setSpikeData(matrix);
        std.getDataSamples().add(sample);
        std.getTrainingData().add(sample);

        SpikingNeuron n1 = new SpikingNeuron(iz1);
        n1.setNeuronType(NeuronType.INPUT_NEURON_POSITIVE);
        n1.setIdx(1);
        SpikingNeuron n2 = new SpikingNeuron(iz2);
        n2.setNeuronType(NeuronType.INPUT_NEURON_NEGATIVE);
        n2.setIdx(2);
        SpikingNeuron n3 = new SpikingNeuron(new SLIF());
        n3.setIdx(3);
        SpikingNeuron n4 = new SpikingNeuron(new SLIF());
        n4.setIdx(4);
        SpikingNeuron n5 = new SpikingNeuron(new SLIF());
        n5.setIdx(5);

        network.getReservoir().add(n1);
        network.getReservoir().add(n2);
        network.getReservoir().add(n3);
        network.getReservoir().add(n4);
        network.getReservoir().add(n5);
        network.getInputNeurons().add(n1);
        network.getInputNeurons().add(n2);

//        network.createConnections(connections, weights,1);
        nrdp.setGabaRate(0.7);
        nrdp.setTimeWindow(10);
        nrdp.train(network, std.getTrainingData());

    }

    /**
     * @return the minA
     */
    public double getMinA() {
        return minA;
    }

    /**
     * @param minA the minA to set
     */
    public void setMinA(double minA) {
        this.minA = minA;
    }

    /**
     * @return the maxA
     */
    public double getMaxA() {
        return maxA;
    }

    /**
     * @param maxA the maxA to set
     */
    public void setMaxA(double maxA) {
        this.maxA = maxA;
    }

    /**
     * @return the gainA
     */
    public double getGainA() {
        return gainA;
    }

    /**
     * @param gainA the gainA to set
     */
    public void setGainA(double gainA) {
        this.gainA = gainA;
    }

    /**
     * @return the minN
     */
    public double getMinN() {
        return minN;
    }

    /**
     * @param minN the minN to set
     */
    public void setMinN(double minN) {
        this.minN = minN;
    }

    /**
     * @return the maxN
     */
    public double getMaxN() {
        return maxN;
    }

    /**
     * @param maxN the maxN to set
     */
    public void setMaxN(double maxN) {
        this.maxN = maxN;
    }

    /**
     * @return the gainN
     */
    public double getGainN() {
        return gainN;
    }

    /**
     * @param gainN the gainN to set
     */
    public void setGainN(double gainN) {
        this.gainN = gainN;
    }

    /**
     * @return the minGA
     */
    public double getMinGA() {
        return minGA;
    }

    /**
     * @param minGA the minGA to set
     */
    public void setMinGA(double minGA) {
        this.minGA = minGA;
    }

    /**
     * @return the maxGA
     */
    public double getMaxGA() {
        return maxGA;
    }

    /**
     * @param maxGA the maxGA to set
     */
    public void setMaxGA(double maxGA) {
        this.maxGA = maxGA;
    }

    /**
     * @return the gainGA
     */
    public double getGainGA() {
        return gainGA;
    }

    /**
     * @param gainGA the gainGA to set
     */
    public void setGainGA(double gainGA) {
        this.gainGA = gainGA;
    }

    /**
     * @return the minGB
     */
    public double getMinGB() {
        return minGB;
    }

    /**
     * @param minGB the minGB to set
     */
    public void setMinGB(double minGB) {
        this.minGB = minGB;
    }

    /**
     * @return the maxGB
     */
    public double getMaxGB() {
        return maxGB;
    }

    /**
     * @param maxGB the maxGB to set
     */
    public void setMaxGB(double maxGB) {
        this.maxGB = maxGB;
    }

    /**
     * @return the gainGB
     */
    public double getGainGB() {
        return gainGB;
    }

    /**
     * @param gainGB the gainGB to set
     */
    public void setGainGB(double gainGB) {
        this.gainGB = gainGB;
    }

    /**
     * @return the gabaImpact
     */
    public double getGabaImpact() {
        return gabaImpact;
    }

    /**
     * @param gabaImpact the gabaImpact to set
     */
    public void setGabaImpact(double gabaImpact) {
        this.gabaImpact = gabaImpact;
    }

    @Override
    public void validate(Network network, SpatioTemporalData std) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
