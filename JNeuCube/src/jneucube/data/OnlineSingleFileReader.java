/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jneucube.data;


import jneucube.util.Matrix;


/**
 *
 * @author Josafath Israel Espinosa Ramos
 */
public class OnlineSingleFileReader extends OnlineReader{

    private String fileDir;
    private String fileName;
    
    private long currentRecord=0;

    //Logger _log = LoggerFactory.getLogger(OnlineSingleFileReader.class);
    
    @Override
    public DataSample getData() {
        DataSample sample = new DataSample();
        Matrix data= new Matrix(getFileDir() +"\\"+ getFileName(), ",", this.getCurrentRecord());
        sample.setNumFeatures(data.getCols());
        sample.setNumRecords(data.getRows());
        if(this.isEncoded()){
            sample.setSpikeData(data);
            sample.setEncoded(true);
        }else{
            sample.setData(data);
        }
        this.currentRecord++;
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
     * @return the currentRecord
     */
    public long getCurrentRecord() {
        return currentRecord;
    }

    /**
     * @param currentRecord the currentRecord to set
     */
    public void setCurrentRecord(long currentRecord) {
        this.currentRecord = currentRecord;
    }
    
    @Override
    public String toString(){
        return "Single file reader";
    }

    @Override
    public void initialize() {
        this.currentRecord=0;        
    }
        
}
