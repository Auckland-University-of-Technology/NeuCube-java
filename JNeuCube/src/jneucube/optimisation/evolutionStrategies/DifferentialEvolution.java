    /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.optimisation.evolutionStrategies;

import java.io.IOException;
import java.util.Random;
import jneucube.optimisation.fitnessFunctions.FitnessFunction;
import jneucube.optimisation.fitnessFunctions.SumFunction;
import jneucube.util.Matrix;
import jneucube.util.Util;

/**
 *
 * @author Josafath Israel Espinosa Ramos
 */
public class DifferentialEvolution {

    /**
     * Configuration variables
     */
    private int maxGenerations = 100;
    private FitnessFunction fitnessFunction;
    private double crossoverProbability = 0.7;
    private double weightingFactor = 0.1;
    private int populationSize = 0;

    /**
     * Functional variables
     */
    private double[][] population;
    private double[] populationFitness;
    double[][] offsprings;
    double[] offspringsFitness;

    private double[] bestIndividual;
    private int bestIndividualIndex;
    private double bestIndividualFitness;
    private int bestIndividualGeneration;
    private String filePopulationMatrix = "";
    Matrix populationMatrix;

    /**
     * The {@code run()} function creates an initial population and executes the
     * iterative process of evolution {@link #runGenerations(int)}.
     */
    public void run() {
        double iterationStartTime = System.nanoTime();
        double iterationProcessTime;
        int iteration = 0;
        
        System.out.println("<optimisation type=\"differential evolution\">");
        System.out.println("<iteration id=\"0\">");
        if(this.populationSize==0){
            this.populationSize=this.fitnessFunction.getDimensionality()*10;
        }
        this.createInitialPopulation(this.fitnessFunction.getRangeValues());
        populationMatrix = new Matrix(population);
        this.populationFitness = new double[this.population.length];
        this.offsprings = new double[this.population.length][this.fitnessFunction.getDimensionality()];
        this.offspringsFitness = new double[this.population.length];
        this.bestIndividual = new double[this.fitnessFunction.getDimensionality()];
        this.bestIndividualFitness = Double.NEGATIVE_INFINITY * this.fitnessFunction.getOptimisationType();
        this.evaluateInitialPopulation();
        iterationProcessTime = (System.nanoTime() - iterationStartTime) / 1000000;   // milliseconds  
        System.out.println("<best_agent id=\"" + this.bestIndividualIndex + "\" generation_id=\"" + iteration + "\">");
        System.out.println("<best_agent_solution>" + Util.getHorzArray(bestIndividual) + "</best_agent_solution>");
        System.out.println("<best_agent_fitness>" + this.bestIndividualFitness + "</best_agent_fitness>");
        System.out.println("</best_agent>");
        System.out.println("<population min=\"" + Util.min(populationFitness) + "\" max=\"" + Util.max(populationFitness) + "\"  mean=\"" + Util.mean(populationFitness) + "\" std=\"" + Util.std(populationFitness) + " \"></population>");
        System.out.println("<iteration_time>" + iterationProcessTime + "</iteration_time>");
        System.out.println("</iteration>");

        this.runGenerations(iteration);
        System.out.println("</optimisation>");
    }

    /**
     * The {@code run(String strPopulationFileName, int iteration)} function
     * sets the initial population by reading a comma delimited file that
     * contains the population of a generation and executes the iterative
     * process of evolution {@link #runGenerations(int)}. The file is an m-by-n
     * matrix where m is the number of individuals in the population, n is the
     * number of dimensions of the candidate solutions plus their fitness.
     *
     * @param strPopulationFileName a comma delimited CSV file that contains the
     * population of a generation
     * @param iteration the generation from which the evolutionary algorithm
     * will continue
     * @throws java.io.IOException
     */
    public void run(String strPopulationFileName, int iteration) throws IOException {
        System.out.println("<optimisation type=\"differential evolution\">");
        this.readPopulation(strPopulationFileName);
        this.runGenerations(iteration);
        System.out.println("</optimisation>");
    }

    public void runGenerations(int iteration) {
        Random rand = new Random();
        double iterationStartTime;
        double iterationProcessTime;
        double randTemp, jrand;
        int[] matingPoolIdx;
        int i, j;
        Matrix iterationMatrix = new Matrix(population.length, this.fitnessFunction.getRangeValues().length + 1, 0.0);
        iterationMatrix.fill(0, populationMatrix);// .setData(populationMatrix.getData());
        iterationMatrix.setCol(this.fitnessFunction.getRangeValues().length, this.populationFitness);
        iterationMatrix.export(this.filePopulationMatrix + "_" + iteration + ".csv", ",");

        while ((iteration < this.maxGenerations) && (this.bestIndividualFitness != this.fitnessFunction.getOptimalValue())) {
            iteration++;
            System.out.println("<iteration id=\"" + iteration + "\">");
            iterationStartTime = System.nanoTime();
            for (i = 0; i < this.population.length; i++) {
                // select the indexes of 3 different individuals
                matingPoolIdx = this.getMatingPoolIndexes(i, this.population.length);
                // ensures that the new vector gets at least one parameter from the mutant vector
                jrand=rand.nextInt(this.fitnessFunction.getDimensionality());
                //Creates a new individual with the weighted difference between two population vectors (randomly chosen) to a third vector.
                for (j = 0; j < this.fitnessFunction.getDimensionality(); j++) {
                    randTemp = rand.nextDouble();
                    // Crossover and mutation operations
                    //if (randTemp <= this.crossoverProbability || j == rand.nextInt(this.fitnessFunction.getDimensionality())) {
                    if (randTemp <= this.crossoverProbability || j == jrand) {
                        this.offsprings[i][j] = this.population[matingPoolIdx[0]][j] + this.weightingFactor * (population[matingPoolIdx[1]][j] - population[matingPoolIdx[2]][j]);
                    } else {
                        this.offsprings[i][j] = this.population[i][j];
                    }
                }
                // evaluation of new individuals (children)
                offspringsFitness[i] = this.fitnessFunction.evaluateIndividual(i, this.offsprings[i]);
            }
            // Selection of the next generation
            for (i = 0; i < this.populationFitness.length; i++) {
                if (offspringsFitness[i] * this.fitnessFunction.getOptimisationType() >= populationFitness[i] * this.fitnessFunction.getOptimisationType()) {
                    System.arraycopy(offsprings[i], 0, population[i], 0, offsprings[i].length);
                    populationFitness[i] = offspringsFitness[i];
                }
                if (populationFitness[i] * this.fitnessFunction.getOptimisationType() > this.bestIndividualFitness * this.fitnessFunction.getOptimisationType()) {
                    this.bestIndividualFitness = populationFitness[i];
                    System.arraycopy(population[i], 0, bestIndividual, 0, population[i].length);
                    this.bestIndividualIndex = i;
                    this.bestIndividualGeneration=iteration;
                }
            }
            iterationProcessTime = (System.nanoTime() - iterationStartTime) / 1000000;   // milliseconds  
            System.out.println("<best_agent id=\"" + this.bestIndividualIndex + "\" generation_id=\"" + bestIndividualGeneration + "\">");
            System.out.println("<best_agent_solution>" + Util.getHorzArray(bestIndividual) + "</best_agent_solution>");
            System.out.println("<best_agent_fitness>" + this.bestIndividualFitness + "</best_agent_fitness>");
            System.out.println("</best_agent>");
            System.out.println("<population min=\"" + Util.min(populationFitness) + "\" max=\"" + Util.max(populationFitness) + "\"  mean=\"" + Util.mean(populationFitness) + "\" std=\"" + Util.std(populationFitness) + " \"></population>");
            System.out.println("<iteration_time>" + iterationProcessTime + "</iteration_time>");
            System.out.println("</iteration>");

            iterationMatrix.fill(0, populationMatrix);// .setData(populationMatrix.getData());
            iterationMatrix.setCol(this.fitnessFunction.getRangeValues().length, this.populationFitness);
            //iterationMatrix.show();
            iterationMatrix.export(this.filePopulationMatrix + "_" + iteration + ".csv", ",");
        }
    }

    /**
     * The {@code readPopulation(String strPopulationFileName)} function reads a comma delimited CSV file that has an m-by-n
     * matrix where m is the number of individuals in the population, n is the
     * number of dimensions of the candidate solutions plus their fitness.
     * @param strPopulationFileName a comma delimited CSV file
     * @throws IOException 
     */
    public void readPopulation(String strPopulationFileName) throws IOException {
        Matrix iPopulation = new Matrix(strPopulationFileName, ",");
        this.population = iPopulation.getData(0, iPopulation.getRows(), 0, iPopulation.getCols() - 1);
        this.populationMatrix = new Matrix(population);
        this.populationFitness = new double[this.population.length];
        this.offsprings = new double[this.population.length][this.fitnessFunction.getDimensionality()];
        this.offspringsFitness = new double[this.population.length];
        this.bestIndividual = new double[this.fitnessFunction.getDimensionality()];
        this.bestIndividualFitness = Double.NEGATIVE_INFINITY * this.fitnessFunction.getOptimisationType();
        this.evaluateInitialPopulation();
    }

    /**
     *    
     * @param rangeValues N by 2 matrix where every row contains the min a max
     * value of the variable
     */
    public void createInitialPopulation(double[][] rangeValues) {
        //int size = 10 * rangeValues.length;        
        //int size = 10;
        int dimension = rangeValues.length;
        this.population = new double[this.populationSize][];
        int startPopulation = 0;
        double[] initialIndividual = this.fitnessFunction.getInitialIndividual();
        if (initialIndividual != null) {
            this.population[0] = new double[dimension];
            System.arraycopy(initialIndividual, 0, this.population[0], 0, dimension);
            startPopulation = 1;
        }
        for (int i = startPopulation; i < this.populationSize; i++) {
            this.population[i] = createNewIndividual(rangeValues);
        }
    }

//    public void setInitialPopulation(){
//        private double[][] population;    
//    private double[] populationFitness;
//    double[][] offsprings;
//    double[] offspringsFitness;
//    
//    private double[] bestIndividual;
//    private int bestIndividualIndex;
//    private double bestIndividualFitness;
//    }
    /**
     *
     */
    public void evaluateInitialPopulation() {
        // Evaluation of the intial population
        for (int i = 0; i < this.population.length; i++) {
            this.populationFitness[i] = this.fitnessFunction.evaluateIndividual(i, this.population[i]);
            //if (this.fitnessFunction.getOptimisationType() == FitnessFunction.MINIMISATION) {
            if (this.populationFitness[i] * this.fitnessFunction.getOptimisationType() > this.bestIndividualFitness * this.fitnessFunction.getOptimisationType()) {
                this.bestIndividualIndex = i;
                this.bestIndividualFitness = this.populationFitness[i];
                //this.bestIndividual = this.population[i];
                System.arraycopy(population[i], 0, bestIndividual, 0, population[i].length);
            }
        }
    }

    /**
     *
     * @param rangeValues N by 2 matrix where every row contains the min a max
     * value of the variable
     * @return an array of double values
     */
    public double[] createNewIndividual(double[][] rangeValues) {
        int dimension = rangeValues.length;
        double[] individual = new double[dimension];
        for (int i = 0; i < dimension; i++) {
            individual[i] = Util.getRandom(rangeValues[i][0], rangeValues[i][1]);
        }
        return individual;
    }

    public int[] getMatingPoolIndexes(int individualIndex, int populationSize) {
        int[] idx = new int[]{individualIndex, individualIndex, individualIndex};
        while (idx[0] == individualIndex) {
            idx[0] = Util.getRandomInt(0, populationSize);
        }

        while (idx[1] == individualIndex || idx[1] == idx[0]) {
            idx[1] = Util.getRandomInt(0, populationSize);
        }

        while (idx[2] == individualIndex || idx[2] == idx[0] || idx[2] == idx[1]) {
            idx[2] = Util.getRandomInt(0, populationSize);
        }
        return idx;
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
     * @return the maxGenerations
     */
    public int getMaxGenerations() {
        return maxGenerations;
    }

    /**
     * @param maxGenerations the maxGenerations to set
     */
    public void setMaxGenerations(int maxGenerations) {
        this.maxGenerations = maxGenerations;
    }

    /**
     * @return the weightingFactor
     */
    public double getWeightingFactor() {
        return weightingFactor;
    }

    /**
     * @param weightingFactor the weightingFactor to set
     */
    public void setWeightingFactor(double weightingFactor) {
        this.weightingFactor = weightingFactor;
    }

    /**
     * @return the crossoverProbability
     */
    public double getCrossoverProbability() {
        return crossoverProbability;
    }

    /**
     * @param crossoverProbability the crossoverProbability to set
     */
    public void setCrossoverProbability(double crossoverProbability) {
        this.crossoverProbability = crossoverProbability;
    }

    /**
     * @return the populationSize
     */
    public int getPopulationSize() {
        return populationSize;
    }

    /**
     * @param populationSize the populationSize to set
     */
    public void setPopulationSize(int populationSize) {
        this.populationSize = populationSize;
    }

    /**
     * @return the filePopulationMatrix
     */
    public String getFilePopulationMatrix() {
        return filePopulationMatrix;
    }

    /**
     * @param filePopulationMatrix the filePopulationMatrix to set
     */
    public void setFilePopulationMatrix(String filePopulationMatrix) {
        this.filePopulationMatrix = filePopulationMatrix;
    }

    /**
     * @return the populationFitness
     */
    public double[] getPopulationFitness() {
        return populationFitness;
    }

    /**
     * @param populationFitness the populationFitness to set
     */
    public void setPopulationFitness(double[] populationFitness) {
        this.populationFitness = populationFitness;
    }

    /**
     * @return the bestIndividual
     */
    public double[] getBestIndividual() {
        return bestIndividual;
    }

    /**
     * @param bestIndividual the bestIndividual to set
     */
    public void setBestIndividual(double[] bestIndividual) {
        this.bestIndividual = bestIndividual;
    }

    /**
     * @return the bestIndividualIndex
     */
    public int getBestIndividualIndex() {
        return bestIndividualIndex;
    }

    /**
     * @param bestIndividualIndex the bestIndividualIndex to set
     */
    public void setBestIndividualIndex(int bestIndividualIndex) {
        this.bestIndividualIndex = bestIndividualIndex;
    }

    /**
     * @return the bestIndividualFitness
     */
    public double getBestIndividualFitness() {
        return bestIndividualFitness;
    }

    /**
     * @param bestIndividualFitness the bestIndividualFitness to set
     */
    public void setBestIndividualFitness(double bestIndividualFitness) {
        this.bestIndividualFitness = bestIndividualFitness;
    }

    /**
     * @return the populationMatrix
     */
    public Matrix getPopulationMatrix() {
        return populationMatrix;
    }

    /**
     * @param populationMatrix the populationMatrix to set
     */
    public void setPopulationMatrix(Matrix populationMatrix) {
        this.populationMatrix = populationMatrix;
    }

    public static void main(String args[]) {
        DifferentialEvolution de = new DifferentialEvolution();
        FitnessFunction ff = new SumFunction();
        de.setFitnessFunction(ff);
        de.setMaxGenerations(100);
        de.setCrossoverProbability(0.7);
        de.setWeightingFactor(0.1);
        de.setPopulationSize(ff.getDimensionality() * 10);
        de.run();
    }

}
