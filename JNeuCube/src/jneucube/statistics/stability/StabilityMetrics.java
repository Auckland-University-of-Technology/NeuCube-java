/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.statistics.stability;

import java.util.ArrayList;
import jneucube.data.DataSample;
import jneucube.network.NetworkController;
import jneucube.spikingNeurons.SpikingNeuron;
import jneucube.spikingNeurons.Synapse;
import jneucube.util.Util;
import static jneucube.log.Log.LOGGER;

/**
 *
 * @author Josafath Israel Espinosa Ramos
 */
public class StabilityMetrics {


    /**
     * Computes the Kolmogorov-Smirnov test to compare the distribution of the
     * data with the normal distribution.
     *
     * @param network
     * @param alpha
     * @param mean
     * @param std
     * @return
     */
    public double getKolmogorovSmirnovTestError(NetworkController network, double alpha, double mean, double std) {
        //double[][] zValues = network.getNetworkStandardNormalisedConductanceValues(mean, std); // first column original weights, second column the standard normalised weights
        double[][] zValues = network.getWeightMatrix().getZScore(mean,std); // first column original weights, second column the standard normalised weights
        
        LOGGER.info("Performing Kolmogorov-Smirnov Test");
        double error;
        int idx = 0;
        int n = zValues.length;
        double criticalValue = (n <= 50) ? alpha : alpha / Math.sqrt(n);
        Util.quickSort(zValues, 0, zValues.length - 1, 1);
        double max = Double.NEGATIVE_INFINITY;
        double temp;
        double Fz;
        double empirical;
        for (int i = 0; i < n; i++) {
            //Fz = this.cdfMarsaglia(zValues[i][1]);
            //Fz = this.cdfMarsaglia2(zValues[i][1]);
            Fz = this.cdfHorner(zValues[i][1]);    // Using Horner's method (algorithm for calculating polynomials)
            empirical = (i * 1.0) / (n * 1.0);// this.empirical(i, n);
            //temp = Math.abs((Fz - (i - 1) / n) - ((i / n) - Fz));
            temp = Math.abs(empirical - Fz);
            if (temp > max) {
                max = temp;
                idx = i;
            }
//            if (i < 100) {
//                System.out.println(Fz);
//            }
        }
        //error = Math.max(0, max - criticalValue);
        error = (max - criticalValue);
        LOGGER.info("Kolmogorov-Smirnov test complete " + error);
        return error;
    }

    /**
     * Calculate the cumulative density function CDF using Horner's method (an
     * algorithm for calculating polynomials) for estimating the error function.
     * https://stackoverflow.com/questions/9242907/how-do-i-generate-normal-cumulative-distribution-in-java-its-inverse-cdf-how
     * [Date consulted: 11 June 2017]
     *
     * @param x
     * @return
     */
    public double cdfHorner(double x) {
        double sign = 1;
        if (x < 0) {
            sign = -1;
        }
        double result = 0.5 * (1.0 + sign * erf(Math.abs(x) / Math.sqrt(2)));
        return result;
    }

    /**
     * Calculates the error function erf for the cumulative density function CDF
     * using the Horner's method (an algorithm for calculating polynomials).
     * https://stackoverflow.com/questions/9242907/how-do-i-generate-normal-cumulative-distribution-in-java-its-inverse-cdf-how
     * [Date consulted: 11 June 2017]
     *
     * @param x
     * @return
     */
    private double erf(double x) {
        double a1 = 0.254829592;
        double a2 = -0.284496736;
        double a3 = 1.421413741;
        double a4 = -1.453152027;
        double a5 = 1.061405429;
        double p = 0.3275911;
        x = Math.abs(x);
        double t = 1 / (1 + p * x);
        //Calculation of nth order polynomial takes O(n^2) operations
        //return 1 - (a1 * t + a2 * t * t + a3 * t * t * t + a4 * t * t * t * t + a5 * t * t * t * t * t) * Math.Exp(-1 * x * x);

        //Horner's method, takes O(n) operations for nth order polynomial
        return 1 - ((((((a5 * t + a4) * t) + a3) * t + a2) * t) + a1) * t * Math.exp(-1 * x * x);
    }

    public double getBranchingParameterError(NetworkController network, ArrayList<DataSample> data) {
        double targetValue = 1.0;
        double branchingParameter = network.getBranchingParameter(data, 1);
        double error = Math.abs(targetValue - branchingParameter);
        return error;
    }

    /**
     * Calculates the coverage of the data. About 68.27% of values drawn from a
     * normal distribution are within one standard deviation σ away from the
     * mean; about 95.45% of the values lie within two standard deviations; and
     * about 99.7% are within three standard deviations. This fact is known as
     * the 68-95-99.7 (empirical) rule, or the 3-sigma rule.
     *
     * @param network
     * @param mean
     * @param std
     * @return
     */
    public double getGaussianCoverageError(NetworkController network, double mean, double std) {
        int countStd1 = 0;
        int countStd2 = 0;
        int countStd3 = 0;
        int countWeights = 0;
        double percentageStd1 = 0.0;
        double percentageStd2 = 0.0;
        double percentageStd3 = 0.0;
        double std1 = 0.6827;
        double std2 = 0.9545;
        double std3 = 0.997;
        double error;
        LOGGER.info("Evaluating Gaussian coverage");
        for (SpikingNeuron neuron : network.getNetwork().getReservoir()) {
            for (Synapse synapse : neuron.getOutputSynapses()) {
                countWeights++;
                if (synapse.getWeight() > (mean - std) && synapse.getWeight() < (mean + std)) {
                    countStd1++;
                }
                if (synapse.getWeight() > (mean - (std * 2)) && synapse.getWeight() < (mean + (std * 2.0))) {
                    countStd2++;
                }
                if (synapse.getWeight() > (mean - (std * 3)) && synapse.getWeight() < (mean + (std * 3.0))) {
                    countStd3++;
                }
            }
        }
        percentageStd1 = (countStd1 * 1.0) / countWeights;
        percentageStd2 = (countStd2 * 1.0) / countWeights;
        percentageStd3 = (countStd3 * 1.0) / countWeights;

        error = Math.abs(percentageStd1 - std1) + Math.abs(percentageStd2 - std2) + Math.abs(percentageStd3 - std3);
        LOGGER.info("Evaluation of Gaussian coverage complete: " + error);
        return error;
    }
}
