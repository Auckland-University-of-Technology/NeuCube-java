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
public class GaussianDistance extends Distance {
    
    private Distance distance = new EuclidianDistance();
    private double sigma = 1.0;

    /**
     * Calculates the Gaussian distance wd=exp(- d(x21,x2)^2 / σ^2) where d is
     * by default the euclidian distance.
     *
     * @param v1
     * @param v2
     * @return
     */
    @Override
    public double getDistance(double[] v1, double[] v2) {
        double d = this.distance.getDistance(v1, v2);
        d = Math.exp(d / (2 * (this.sigma * this.sigma)));    // NeuCube Matlab version (wknn.m line 20). Here the sign was removed to preserve the relationship that the closest the distance the nearest neighbor.
        return d;
    }

    /**
     * @return the distance
     */
    public Distance getDistance() {
        return distance;
    }

    /**
     * @param distance the distance to set
     */
    public void setDistance(Distance distance) {
        this.distance = distance;
    }

    @Override
    public String toString() {
        return "Gaussian Weighted Distance";
    }

    /**
     * @return the sigma
     */
    public double getSigma() {
        return sigma;
    }

    /**
     * @param sigma the sigma to set
     */
    public void setSigma(double sigma) {
        this.sigma = sigma;
    }

}
