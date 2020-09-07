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
public class EuclidianDistance extends Distance {

    
    @Override
    public String toString() {
        return "Euclidian Distance (weights)";
    }

    /**
     * Calculates the Euclidian distance between two points.
     * 
     * @param v1 vector 
     * @param v2 vector
     * @return the distance between the two vectors
     */
    @Override
    public double getDistance(double [] v1, double[] v2) {        
        return Util.getEuclidianDistance(v1, v2);
    }
    

}
