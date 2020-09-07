/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.optimisation.fitnessFunctions;

/**
 *
 * @author Josafath Israel Espinosa Ramos
 */
public abstract class FitnessFunction {

    public static int MINIMISATION = -1;
    public static int MAXIMISATION = 1;

    private int dimensionality = 0;
    private double[][] rangeValues = null;
    protected double optimalValue = 0;
    protected int optimisationType = -1; // minimisation
    /**
     * A predefined candidate solution which could have a high accuracy. Then
     * the algorithm can use it as an attractor. This candidate solution will be
     * part of the initial population.
     */
    private double[] initialIndividual = null;

    public abstract double evaluateIndividual(int id, double[] individual);

    /**
     * @return the optimisationType
     */
    public int getOptimisationType() {
        return optimisationType;
    }

    /**
     * @param optimisationType the optimisationType to set
     */
    public void setOptimisationType(int optimisationType) {
        this.optimisationType = optimisationType;
    }

    /**
     * @return the optimalValue
     */
    public double getOptimalValue() {
        return optimalValue;
    }

    /**
     * @param optimalValue the optimalValue to set
     */
    public void setOptimalValue(double optimalValue) {
        this.optimalValue = optimalValue;
    }

    /**
     * @return the dimensionality
     */
    public int getDimensionality() {
        return dimensionality;
    }

    /**
     * @param dimensionality the dimensionality to set
     */
    public void setDimensionality(int dimensionality) {
        this.dimensionality = dimensionality;
    }

    /**
     * @return the rangeValues
     */
    public double[][] getRangeValues() {
        return rangeValues;
    }

    /**
     * @param rangeValues the rangeValues to set
     */
    public void setRangeValues(double[][] rangeValues) {
        this.rangeValues = rangeValues;
    }

    /**
     * @return the initialIndividual
     */
    public double[] getInitialIndividual() {
        return initialIndividual;
    }

    /**
     * @param initialIndividual the initialIndividual to set
     */
    public void setInitialIndividual(double[] initialIndividual) {
        this.initialIndividual = initialIndividual;
    }

}
