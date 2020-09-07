/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.classifiers;

import java.util.Date;
import static jneucube.data.OnlineDataReaderJob.KEY_SPATIO_TEMPORAL_DATA;
import jneucube.data.OnlineWriter;
import jneucube.data.SpatioTemporalData;
import jneucube.encodingAlgorithms.EncodingAlgorithm;
import static jneucube.log.Log.LOGGER;
import jneucube.network.Network;
import jneucube.trainingAlgorithms.LearningAlgorithm;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 *
 * @author Josafath Israel Espinosa Ramos
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class OnlineClassifierJob implements Job {
    
    public static final String KEY_LEARNING_ALGORITHM = "KEY_LEARNING_ALGORITHM";
    public static final String KEY_ENCODING_ALGORITHM = "KEY_ENCODING_ALGORITHM";
    public static final String KEY_NETWORK = "KEY_NETWORK";
    public static final String KEY_FLAG = "KEY_FLAG";
    public static final String KEY_ELAPSED_TIME = "KEY_ELAPSED_TIME";
    public static final String KEY_ONLINE_WRITER = "KEY_ONLINE_WRITER";
    

    public OnlineClassifierJob() {

    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap map = context.getJobDetail().getJobDataMap();
        Network network = (Network) map.get(KEY_NETWORK);
        SpatioTemporalData std = (SpatioTemporalData) map.get(KEY_SPATIO_TEMPORAL_DATA);
        LearningAlgorithm learningAlgorithm = (LearningAlgorithm) map.get(KEY_LEARNING_ALGORITHM);
        EncodingAlgorithm encodingAlgorithm = (EncodingAlgorithm) map.get(KEY_ENCODING_ALGORITHM);
        OnlineWriter onlineWriter=(OnlineWriter) map.get(KEY_ONLINE_WRITER);
        
        boolean flag = map.getBoolean(KEY_FLAG);
        int elapsedTime = 0;
//        LOGGER.info("---" + context.getJobDetail().getKey() + " Executing.[" + new Date() + "] BUFFER SIZE " + std.getValidationData().size() + " FLAG " + flag);
        if (std.getValidationData().size() > 0 && flag == true) {
            LOGGER.info("---" + context.getJobDetail().getKey() + " Classifyng data.[" + new Date() + "] BUFFER SIZE " + std.getValidationData().size() + " FLAG " + flag);
            //LOGGER.info("---" + context.getJobDetail().getKey() + "  - Classifying data");
            flag = false;
            map.put(KEY_FLAG, flag);
            if (map.containsKey(KEY_ELAPSED_TIME)) {
                elapsedTime = map.getInt(KEY_ELAPSED_TIME);
            }
            elapsedTime++;
            LOGGER.debug("  -Encoding data ");            
            encodingAlgorithm.setEncodingSatus(true);
            encodingAlgorithm.encode(std.getTrainingData(), std.getValidationData(), 0, std.getNumRecords());   // Encodes validation data
            learningAlgorithm.validate(network, std);   // Uses the validation data to stimulate the NeuCube and classifies it            
            onlineWriter.setData(std.getValidationData().get(0).getValidationClassId());    // Send the class value to any ooutput (standard output, logger, any port, etc)
            std.getValidationData().remove(0);          // Removes the data used for classification
            
            flag = true;
            map.put(KEY_FLAG, flag);
            map.put(KEY_ELAPSED_TIME, elapsedTime);
            LOGGER.info("  -" + context.getJobDetail().getKey() + " Complete (" + elapsedTime + "). BUFFER SIZE " + std.getValidationData().size());
        }
    }

}
