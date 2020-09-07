/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.optimisation.evolutionStrategies;

import java.io.IOException;
import jneucube.optimisation.fitnessFunctions.FitnessFunction;
import jneucube.util.Matrix;
import jneucube.util.Util;

/**
 *
 * @author Josafath Israel Espinosa Ramos
 */
public class GridSearch {

    private FitnessFunction fitnessFunction;
    private String outputFile = "";

    private double[] lifThresholds = new double[]{0.1, 0.2};       // Leaky Integrate and Fire neuron
    private int[] lifRefractoryTimes = new int[]{2, 6, 8};         // Leaky Integrate and Fire neuron
    private double[] stdpApos = new double[]{0.001, 0.005, 0.01};           // STDP
    private double[] stdpAneg = new double[]{0.001, 0.005, 0.01};           // STDP
    private double[] deSnnDriftPos = new double[]{0.001, 0.005, 0.01};      // deSNN
    private double[] deSnnDriftNeg = new double[]{0.001, 0.005, 0.01};      // deSNN
    private int[] kNeighbours = new int[]{3, 5, 10};                        // KNN

    public void run(int start) throws IOException {
        Matrix populationMatrix;
        if(start==0){
            populationMatrix= this.createInitialPopulation();         
        }else{
            start=start-1;
            populationMatrix = new Matrix(this.outputFile,",");
        }                
        double[] bestIndiviual = this.evaluatePopulation(populationMatrix,start);
        Util.printHorzArray(bestIndiviual);
    }

    public Matrix createInitialPopulation() {
        int populationSize = lifThresholds.length * lifRefractoryTimes.length * stdpApos.length * stdpAneg.length * deSnnDriftPos.length * deSnnDriftNeg.length * kNeighbours.length;
        double[][] population = new double[populationSize][];
        int idx = 0;
        for (int a = 0; a < lifThresholds.length; a++) {
            for (int b = 0; b < lifRefractoryTimes.length; b++) {
                for (int c = 0; c < stdpApos.length; c++) {
                    for (int d = 0; d < stdpAneg.length; d++) {
                        for (int e = 0; e < deSnnDriftPos.length; e++) {
                            for (int f = 0; f < deSnnDriftNeg.length; f++) {
                                for (int g = 0; g < kNeighbours.length; g++) {
                                    population[idx] = new double[]{this.lifThresholds[a], lifRefractoryTimes[b], stdpApos[c], stdpAneg[d], deSnnDriftPos[e], deSnnDriftNeg[f], kNeighbours[g]};
                                    idx++;
                                }
                            }
                        }
                    }
                }
            }
        }
        Matrix populationMatrix = new Matrix(population);
        populationMatrix.insertCol();//One more column for the fitness
        populationMatrix.export(this.outputFile, ",");
        return populationMatrix;
    }

    public double[] evaluatePopulation(Matrix populationMatrix, int start) {
        double fitness;
        double bestIndividualFitness = Double.NEGATIVE_INFINITY * this.getFitnessFunction().getOptimisationType();
        int bestIndividualIndex = 0;        
        // Evaluation of the intial population
        for (int i = start; i < populationMatrix.getRows(); i++) {
            double []individual=new double[populationMatrix.getVecRow(i).length-1];
            System.arraycopy(populationMatrix.getVecRow(i), 0, individual, 0, populationMatrix.getVecRow(i).length-1);
            fitness = this.getFitnessFunction().evaluateIndividual(i,individual);
            populationMatrix.set(i, populationMatrix.getCols() - 1, fitness);
            populationMatrix.export(this.outputFile, ",");
            if (fitness * this.getFitnessFunction().getOptimisationType() > bestIndividualFitness * this.getFitnessFunction().getOptimisationType()) {
                bestIndividualIndex = i;
                bestIndividualFitness = fitness;
            }
        }
        return populationMatrix.getVecRow(bestIndividualIndex);
    }

    /**
     * @return the lifThresholds
     */
    public double[] getLifThresholds() {
        return lifThresholds;
    }

    /**
     * @param lifThresholds the lifThresholds to set
     */
    public void setLifThresholds(double[] lifThresholds) {
        this.lifThresholds = lifThresholds;
    }

    /**
     * @return the lifRefractoryTimes
     */
    public int[] getLifRefractoryTimes() {
        return lifRefractoryTimes;
    }

    /**
     * @param lifRefractoryTimes the lifRefractoryTimes to set
     */
    public void setLifRefractoryTimes(int[] lifRefractoryTimes) {
        this.lifRefractoryTimes = lifRefractoryTimes;
    }

    /**
     * @return the stdpApos
     */
    public double[] getStdpApos() {
        return stdpApos;
    }

    /**
     * @param stdpApos the stdpApos to set
     */
    public void setStdpApos(double[] stdpApos) {
        this.stdpApos = stdpApos;
    }

    /**
     * @return the stdpAneg
     */
    public double[] getStdpAneg() {
        return stdpAneg;
    }

    /**
     * @param stdpAneg the stdpAneg to set
     */
    public void setStdpAneg(double[] stdpAneg) {
        this.stdpAneg = stdpAneg;
    }

    /**
     * @return the deSnnDriftPos
     */
    public double[] getDeSnnDriftPos() {
        return deSnnDriftPos;
    }

    /**
     * @param deSnnDriftPos the deSnnDriftPos to set
     */
    public void setDeSnnDriftPos(double[] deSnnDriftPos) {
        this.deSnnDriftPos = deSnnDriftPos;
    }

    /**
     * @return the deSnnDriftNeg
     */
    public double[] getDeSnnDriftNeg() {
        return deSnnDriftNeg;
    }

    /**
     * @param deSnnDriftNeg the deSnnDriftNeg to set
     */
    public void setDeSnnDriftNeg(double[] deSnnDriftNeg) {
        this.deSnnDriftNeg = deSnnDriftNeg;
    }

    /**
     * @return the kNeighbours
     */
    public int[] getkNeighbours() {
        return kNeighbours;
    }

    /**
     * @param kNeighbours the kNeighbours to set
     */
    public void setkNeighbours(int[] kNeighbours) {
        this.kNeighbours = kNeighbours;
    }

    /**
     * @return the outputFile
     */
    public String getOutputFile() {
        return outputFile;
    }

    /**
     * @param outputFile the outputFile to set
     */
    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    /**
     * @return the fitnessFunction
     */
    public FitnessFunction getFitnessFunction() {
        return fitnessFunction;
    }

    /**
     * @param fitnessFunction the fitnessFunction to set
     */
    public void setFitnessFunction(FitnessFunction fitnessFunction) {
        this.fitnessFunction = fitnessFunction;
    }

    /**
     * @param thresholds the thresholds to set
     */
    public void setLifThresholds(String thresholds) {
        String[] vals = thresholds.split(",");
        this.lifThresholds = new double[vals.length];
        for (int i = 0; i < vals.length; i++) {
            this.lifThresholds[i] = Double.parseDouble(vals[i]);
        }
    }

    /**
     * @param values
     */
    public void setLifRefractoryTimes(String values) {
        String[] tempVals = values.split(",");
        this.lifRefractoryTimes = new int[tempVals.length];
        Double d;
        for (int i = 0; i < tempVals.length; i++) {
            d = Double.parseDouble(tempVals[i]);
            this.lifRefractoryTimes[i] = d.intValue();
        }
    }

    /**
     * @param values the thresholds to set
     */
    public void setStdpApos(String values) {
        String[] vals = values.split(",");
        this.stdpApos = new double[vals.length];
        for (int i = 0; i < vals.length; i++) {
            this.stdpApos[i] = Double.parseDouble(vals[i]);
        }
    }

    /**
     * @param values the thresholds to set
     */
    public void setStdpAneg(String values) {
        String[] vals = values.split(",");
        this.stdpAneg = new double[vals.length];
        for (int i = 0; i < vals.length; i++) {
            this.stdpAneg[i] = Double.parseDouble(vals[i]);
        }
    }

    /**
     * @param values the thresholds to set
     */
    public void setDeSnnDriftPos(String values) {
        String[] vals = values.split(",");
        this.deSnnDriftPos = new double[vals.length];
        for (int i = 0; i < vals.length; i++) {
            this.deSnnDriftPos[i] = Double.parseDouble(vals[i]);
        }
    }

    /**
     * @param values the thresholds to set
     */
    public void setDeSnnDriftNeg(String values) {
        String[] vals = values.split(",");
        this.deSnnDriftNeg = new double[vals.length];
        for (int i = 0; i < vals.length; i++) {
            this.deSnnDriftNeg[i] = Double.parseDouble(vals[i]);
        }
    }

    /**
     * @param values
     */
    public void setkNeighbours(String values) {
        String[] tempVals = values.split(",");
        this.kNeighbours = new int[tempVals.length];
        Double d;
        for (int i = 0; i < tempVals.length; i++) {
            d = Double.parseDouble(tempVals[i]);
            this.kNeighbours[i] = d.intValue();
        }
    }
}
