/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.data;

import java.util.ArrayList;
import java.util.Date;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import static jneucube.log.Log.LOGGER;

/**
 *
 * @author em9403
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class OnlineDataReaderJob implements Job {
    
    public static String KEY_ELAPSED_TIME = "KEY_ELAPSED_TIME";
    public static final String KEY_ONLINE_READER = "KEY_ONLINE_READER";
    
    public static final String KEY_SPATIO_TEMPORAL_DATA = "KEY_SPATIO_TEMPORAL_DATA";
    public static final String KEY_MAX_BUFFER_SIZE = "KEY_MAX_BUFFER_SIZE";
    public static final String KEY_DATA_TYPE = "KEY_DATA_TYPE";
    

    //Logger LOGGER = LoggerFactory.getLogger(OnlineDataReaderJob.class);

    public OnlineDataReaderJob() {

    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap map = context.getJobDetail().getJobDataMap();
        OnlineReader onlineReader = (OnlineReader) map.get(KEY_ONLINE_READER);
        SpatioTemporalData std = (SpatioTemporalData) map.get(KEY_SPATIO_TEMPORAL_DATA);
        
        ArrayList<DataSample> data;
        int dataType=map.getInt(KEY_DATA_TYPE);
        int maxBufferSize = map.getInt(KEY_MAX_BUFFER_SIZE);
        int elapsedTime = 0;
        
        if (map.containsKey(KEY_ELAPSED_TIME)) {
            elapsedTime = map.getInt(KEY_ELAPSED_TIME);
        }
        elapsedTime++;        
        map.put(KEY_ELAPSED_TIME, elapsedTime);
        if(dataType==DataSample.TRAINING){
            data=std.getTrainingData();
        }else{
            data=std.getValidationData();
        }
        
        LOGGER.error("***" + context.getJobDetail().getKey() + " executing.[" + new Date() + "] BUFFER SIZE " + data.size());
        if (data.size() < maxBufferSize) {            
            DataSample sample = onlineReader.getData();
            data.add(sample);
        }
        LOGGER.error("  *" + context.getJobDetail().getKey() + " complete. BUFFER SIZE " + data.size());
    }
}
