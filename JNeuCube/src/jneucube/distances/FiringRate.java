/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.distances;

/**
 *
 * @author em9403
 */
public class FiringRate extends Distance {

    private int time;

    /**
     * Calculates the firing rate of both v1 and v2 and returns the Euclidian
     * distance.
     *
     * @param v1 firing times of target neuron.
     * @param v2 firing times of predicted neuron.
     * @return the Euclidian distance between the firing rates of v1 and v2
     */
    @Override
    public double getDistance(double[] v1, double[] v2) {
        double firingRateV1 = v1.length / this.time;
        double firingRateV2 = v2.length / this.time;
        return Math.sqrt((firingRateV2 - firingRateV1) * (firingRateV2 - firingRateV1));
    }

    /**
     * @return the time
     */
    public int getTime() {
        return time;
    }

    /**
     * @param time the time to set
     */
    public void setTime(int time) {
        this.time = time;
    }

}
