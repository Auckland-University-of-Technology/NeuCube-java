/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jneucube.inputMappingAlgorithms;

import java.util.ArrayList;
import java.util.Arrays;
import jneucube.util.Matrix;

/**
 *
 * @author Josafath Israel Espinosa Ramos
 */
public abstract class InputMapping   {

    public static final GraphMatching GRAPH_MATCHING = new GraphMatching();    
    public static final RandomMapping RANDOM_MAPPING = new RandomMapping();    
    public static final ArrayList<InputMapping> INPUT_MAPPING_ALGORITHM_LIST = new ArrayList<>(Arrays.asList(GRAPH_MATCHING, RANDOM_MAPPING));
    
    public abstract Matrix createCoordinates();
}
