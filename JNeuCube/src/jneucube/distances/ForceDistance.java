/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.distances;

import jneucube.util.Util;

/**
 * 
 * @author em9403
 */
public class ForceDistance extends Distance {
    private double k = 8.9875517873681764;  // Coulomb's constant for an electric field
    @Override
    public double getDistance(double[] v1, double[] v2) {        
        double q1=Util.sum(v1);
        double q2=Util.sum(v2);
        double distance=Util.getEuclidianDistance(v1, v2);
        double force=this.k*((q1*q2)/distance);
        return force;
    }

    /**
     * @return the k
     */
    public double getK() {
        return k;
    }

    /**
     * @param k the k to set
     */
    public void setK(double k) {
        this.k = k;
    }
    
}
