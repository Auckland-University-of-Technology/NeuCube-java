/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.distances;

import java.util.ArrayList;

/**
 *
 * @author em9403
 */
public class GammaFactor extends Distance {

    private double delta = 0.002;   // the delta window, in [sec]
    private double samplingFrequency = 1000; // the sampling frequency in Hz

    /**
     * Get the distance between a perfect match 1 and the similarity between two
     * firing trains.
     *
     * @param v1 the firing times of the target spiking neuron.
     * @param v2 the firing times of the predicted spiking neuron.
     * @return the distance between two firing trains.
     */
    @Override
    public double getDistance(double[] v1, double[] v2) {
        return 1 - this.evaluate(v1, v2, delta, samplingFrequency);
    }

    /**
     * The {@code getDistance} function calculates and returns the coincide
     * between two spike trains according to the coincidence factor gamma.
     * Kistler W, Gerstner W, van Hemmen J (1997) Reduction of HodgkinHuxley
     * equations to a single-variable threshold model. Neural Comp. 9:
     * 1015–1045.
     *
     * Jolivet R, Rauch A, Lüscher H, Gerstner W (2006) Predicting spike timing
     * of neocortical pyramidal neurons by simple threshold models. Journal of
     * Computational Neuroscience. 21:1573-6873.
     * https://doi.org/10.1007/s10827-006-7074-5, DOI 10.1007/s10827-006-7074-5
     *
     * @param targetSpikeTrain the firing times of the target spiking neuron.
     * @param predictedSpikeTrain the firing times of the predicted spiking
     * neuron.
     * @return the coincidence factor.
     */
    public double evaluate(ArrayList<Double> targetSpikeTrain, ArrayList<Double> predictedSpikeTrain) {
        double[] target = targetSpikeTrain.stream().mapToDouble(Double::doubleValue).toArray();
        double[] predicted = targetSpikeTrain.stream().mapToDouble(Double::doubleValue).toArray();
        return evaluate(target, predicted, delta, samplingFrequency);
    }

    /*
     * The {@code getDistance} function calculates and returns the coincide
     * between two spike trains according to the coincidence factor gamma.
    
     * @param targetSpkTrain the firing times of the target spiking neuron.
     * @param predSpkTrain the firing times of the predicted spiking
     * neuron.
     * @param delta the delta window, in [sec]
     * @param samplingFrequency the sampling frequency in Hz
     * @return
     */
    public double evaluate(double[] targetSpkTrain, double[] predSpkTrain, double delta, double samplingFrequency) {
        double gamma;
        double nTarget = targetSpkTrain.length;
        double nPred = predSpkTrain.length;
        if (nTarget == 0 && nPred == 0) {
            return 1;
        }
        double nCoinc = this.getCoincidences(targetSpkTrain, predSpkTrain, delta * samplingFrequency);
        double rExp = nPred / samplingFrequency;
        gamma = (2 / (1 - (2.0 * delta * rExp))) * ((nCoinc - (2 * delta * nTarget * rExp)) / (nTarget + nPred));
        return gamma;
    }

    /**
     * Detects the coincidence between two spike trains. A variation of the
     * function used in - Renaud Jolivet 2007.05
     *
     * @param referenceFirings An array with the reference model firing times
     * @param modelFirings An array list with the new model firing times
     * @param deltaBins The window time
     * @return The number of coincidences between two spike trains
     */
    public int getCoincidences(double[] referenceFirings, double[] modelFirings, double deltaBins) {
        double f1;
        double f2;
        int nCoinc = 0;
        int iCoinc;
        for (Double referenceFiring : referenceFirings) {
            iCoinc = 0;
            for (Double modelFiring : modelFirings) {
                f1 = referenceFiring;
                f2 = modelFiring;
                if (Math.abs(f1 - f2) <= deltaBins) {
                    iCoinc++;
                }
            }
            if (iCoinc > 0) {
                nCoinc++;
            }
        }
        return nCoinc;
    }

    /**
     * Gamma factor obtained from - Renaud Jolivet 2007.05 See Kistler et al,
     * Neural Comp 9:1015-1045 (1997) Jolivet et al, J Neurophysiol 92:959-976
     * (2004) http://icwww.epfl.ch/~gerstner/QuantNeuronMod2007/GamCoincFac.m
     *
     * @param targetSpkTrain Reference firing times
     * @param predSpkTrain The new model firing times
     * @param deltaWindow The delta window, in ms
     * @param samplingFrequency The sampling frequency in Hz
     * @return
     */
    public double evaluateJolivet(ArrayList<Double> targetSpkTrain, ArrayList<Double> predSpkTrain, double deltaWindow, double samplingFrequency) {
        double gamma;
        int NSpikesPred = predSpkTrain.size();
        if (NSpikesPred == 0) {
            return 0;
        }
        int nSpikesTarget = targetSpkTrain.size();
        double deltaBins = deltaWindow * samplingFrequency;   // [time bins]   % half-width of coincidence detection (2 msec)
        // Compute frequencies, normalisation and average coincidences 
        double freqPred = samplingFrequency * (NSpikesPred - 1) / Math.max(predSpkTrain.get(NSpikesPred - 1) - predSpkTrain.get(0), 1.0);
        double nCoincAvg = 2.0 * deltaWindow * nSpikesTarget * freqPred;
        double nNorm = 1.0 - 2.0 * freqPred * deltaWindow;
        double nCoinc = this.getCoincidencesJolivet(targetSpkTrain, predSpkTrain, deltaBins);
        gamma = (nCoinc - nCoincAvg) / (1.0 / 2.0 * (NSpikesPred + nSpikesTarget)) * 1.0 / nNorm;
        return gamma;
    }

    /**
     * Detects the coincidence between two spike trains - Renaud Jolivet 2007.05
     *
     * @param referenceFirings An array with the reference model firing times
     * @param modelFirings An array list with the new model firing rimes
     * @param deltaBins
     * @return The number of coincidences between two spike trains
     */
    public int getCoincidencesJolivet(ArrayList<Double> referenceFirings, ArrayList<Double> modelFirings, double deltaBins) {
        double f1;
        double f2;
        int nCoinc = 0;
        for (Double referenceFiring : referenceFirings) {
            for (Double modelFiring : modelFirings) {
                f1 = referenceFiring;
                f2 = modelFiring;
                if (Math.abs(f1 - f2) <= deltaBins) {
                    nCoinc++;
                }
            }
        }
        return nCoinc;
    }

    /**
     * @return the delta
     */
    public double getDelta() {
        return delta;
    }

    /**
     * @param delta the delta to set
     */
    public void setDelta(double delta) {
        this.delta = delta;
    }

    /**
     * @return the samplingFrequency
     */
    public double getSamplingFrequency() {
        return samplingFrequency;
    }

    /**
     * @param samplingFrequency the samplingFrequency to set
     */
    public void setSamplingFrequency(double samplingFrequency) {
        this.samplingFrequency = samplingFrequency;
    }

}
