/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube;

import jneucube.cube.NeuCubeController;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author Josafath Israel Espinosa Ramos
 */
public class JNeuCube {    
    static {
        System.setProperty("log4j.configurationFile", "jneucube/log4j-configFile.xml");
    }    
    
    public static void main(String args[]) {
        int argCount = args.length;

        if (argCount == 3) {
            String dir = args[0];   // directory where the data set, mapping coordinates and input coordintes are located
            String propFileName = args[1];  // properties file that contains the configuration of the NeuCube
            int step = Integer.parseInt(args[2]);   // The step to be executed
            NeuCubeController project = new NeuCubeController();
            try {
                File file =new File(dir);
                if(file.isDirectory()){
                    project.createProject(dir);                    
                    if(project.configureNeuCube(propFileName)){
                        project.run(step);
                    }
                }
            } catch (IOException ex) {
                System.out.println("error");
            }
        } else {
            System.out.println("The number of arguments must be 3");
        }
    }

}
