/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jneucube.distances;

import jneucube.util.Util;
import static jneucube.log.Log.LOGGER;

/**
 *
 * @author Josafath Israel Espinosa Ramos
 */
public class CorrelationDistance extends Distance{

    @Override
    public double getDistance(double[] v1, double[] v2) {
        if(v1.length==v2.length){
            return 1-Util.correlation(v1, v2);
            // calculate the pairwise distance matrix D m by n (m rows in v1, D re
        }else {
            LOGGER.error("Matrix dimensions must agree.");
        }
        return 0.0;
    }

}
