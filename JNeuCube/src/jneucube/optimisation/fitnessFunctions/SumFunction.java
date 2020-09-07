/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jneucube.optimisation.fitnessFunctions;

import jneucube.util.Util;

/**
 *
 * @author Josafath Israel Espinosa Ramos
 */
public class SumFunction extends FitnessFunction{

    public SumFunction(){        
        this.setOptimalValue(30.0);
        this.setRangeValues(new double [][]{{1.0,5.0},{2.0,3.0},{10.0,20.0},{-2.0,0.0}});
        this.setOptimisationType(FitnessFunction.MAXIMISATION);
        this.setDimensionality(4);
    }
     
    @Override
    public double evaluateIndividual(int id, double[] individual) {
        double sum=0;
        for(int i=0;i<individual.length;i++){
            //if(individual[i] )
            if(Util.isInRange(individual[i], this.getRangeValues()[i][0], this.getRangeValues()[i][1])){
                sum+=individual[i];
            }else{
                sum=Double.NEGATIVE_INFINITY*this.getOptimisationType();
            }
        }
        return sum;
    }

}
