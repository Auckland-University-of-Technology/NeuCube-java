/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.data;

import java.io.IOException;
import jneucube.util.Matrix;
import static jneucube.log.Log.LOGGER;
/**
 *
 * @author em9403
 */

public class OnlineMultipleFileReader extends OnlineReader {

    private String fileDir;
    private String fileName;
    private int counter=0;    
    
    public OnlineMultipleFileReader(){
        this.setEncoded(true);
    }

    @Override
    public DataSample getData() {
        DataSample sample=null;
        try {
            sample=new DataSample();
            LOGGER.info("Reading file "+getFileDir()+"\\"+getFileName()+counter+".csv");
            Matrix data=new Matrix(getFileDir()+"\\"+getFileName()+counter+".csv",",");
            sample.setNumFeatures(data.getCols());
            sample.setNumRecords(data.getRows());
            sample.setData(data);
            sample.setEncoded(this.isEncoded());
            counter++;
            
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
        return sample;
    }    

    /**
     * @return the fileDir
     */
    public String getFileDir() {
        return fileDir;
    }

    /**
     * @param fileDir the fileDir to set
     */
    public void setFileDir(String fileDir) {
        this.fileDir = fileDir;
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * @return the counter
     */
    public int getCounter() {
        return counter;
    }

    /**
     * @param counter the counter to set
     */
    public void setCounter(int counter) {
        this.counter = counter;
    }

    @Override
    public String toString(){
        return "Multiple file reader";
    }

    @Override
    public void initialize() {
        this.counter=0;
    }

}
