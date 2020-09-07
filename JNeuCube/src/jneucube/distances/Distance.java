/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.distances;

import java.util.ArrayList;
import java.util.Arrays;
import jneucube.util.Matrix;
import static jneucube.log.Log.LOGGER;

/**
 *
 * @author em9403
 */
public abstract class Distance {

    public static final EuclidianDistance EUCLIDIAN_DISTANCE = new EuclidianDistance();
    public static final GaussianDistance GAUSSIAN_DISTANCE = new GaussianDistance();
    public static final CorrelationDistance CORRELATION_DISTANCE= new CorrelationDistance();
    public static final GammaFactor GAMMA_DISTANCE= new GammaFactor();
    
    public static final ArrayList<Distance> DISTANCE_LIST = new ArrayList<>(Arrays.asList(EUCLIDIAN_DISTANCE));
    public static final ArrayList<Distance> ALL_DISTANCE_LIST = new ArrayList<>(Arrays.asList(EUCLIDIAN_DISTANCE, GAUSSIAN_DISTANCE));

    //public abstract double getDistance(SpikingNeuron validationNeuron, SpikingNeuron trainedNeuron);
    /**
     *
     * @param v1 Matrix 1 x n (vector)
     * @param v2 Matrix 1 x n (vector)
     * @return the distance between the two vectors
     */
    public abstract double getDistance(double [] v1, double []v2);

    public Matrix getDistance(Matrix m1, Matrix m2) {        
        Matrix m=new Matrix (m1.getRows(), m2.getRows());
        if (m1.getCols() == m2.getCols()) {
            for(int r=0;r<m1.getRows();r++){
                for(int c=0;c<m2.getRows();c++){
                    m.set(r, c, this.getDistance(m1.getVecRow(r), m2.getVecRow(c) ) );
                }
            }
        } else {
            LOGGER.error("Matrix dimensions must agree.");
        }
        return m;
    }
}
