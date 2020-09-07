/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.data;

import jneucube.util.Matrix;


/**
 *
 * @author Josafath Israel Espinosa Ramos (Centro de Investigacion en Computacion, Instituto Politecnico Nacional, Mexico 2015)
 */
public class DataSample {
    public static final int TRAINING=1;
    public static final int VALIDATION=2;
    
    private int sampleId;
    private double classId;            // The class identifier
    private double validationClassId;  // The class after training
    private String classLabel;      // The class' name
    private String sampleLabel;     // The sample's name
    private String sampleFileName;     // The sample's name
    private Matrix data;            // The data m x n; m:data points, n:features or channels 
    private int startSpikeTime;
    private int endSpikeTime;
    private int numFeatures;
    private int numRecords;

    private int splitType=TRAINING;   // 1 testing, 2 validation 
    private Matrix spikeData;
    private boolean encoded=false;
    
    public DataSample(){
        
    }
    
    public DataSample(Matrix data){
        this.data=data;
        this.numFeatures=data.getCols();
        this.numRecords=data.getRows();
    }
    
    public String getStringSplitType(){
        if(splitType==TRAINING)
            return "Training";
        else if (splitType==VALIDATION)
            return "Validation";
        else
            return "No split type";
    }
    
    public void clearSpikeData(){
        if(this.spikeData!=null){
            this.spikeData.clear();
        }
        this.spikeData=null;
    }
    
    /**
     * @return the classId
     */
    public double getClassId() {
        return classId;
    }

    /**
     * @param classId the classId to set
     */
    public void setClassId(double classId) {
        this.classId = classId;
    }

    /**
     * @return the classLabel
     */
    public String getClassLabel() {
        return classLabel;
    }

    /**
     * @param classLabel the classLabel to set
     */
    public void setClassLabel(String classLabel) {
        this.classLabel = classLabel;
    }

    /**
     * @return the sampleLabel
     */
    public String getSampleLabel() {
        return sampleLabel;
    }

    /**
     * @param sampleLabel the sampleLabel to set
     */
    public void setSampleLabel(String sampleLabel) {
        this.sampleLabel = sampleLabel;
    }

    /**
     * @return the data
     */
    public Matrix getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(Matrix data) {
        this.data = data;
    }

    /**
     * @return the startSpikeTime
     */
    public int getStartSpikeTime() {
        return startSpikeTime;
    }

    /**
     * @param startSpikeTime the startSpikeTime to set
     */
    public void setStartSpikeTime(int startSpikeTime) {
        this.startSpikeTime = startSpikeTime;
    }

    /**
     * @return the endSpikeTime
     */
    public int getEndSpikeTime() {
        return endSpikeTime;
    }

    /**
     * @param endSpikeTime the endSpikeTime to set
     */
    public void setEndSpikeTime(int endSpikeTime) {
        this.endSpikeTime = endSpikeTime;
    }
    
    public Matrix getData(int startTime, int endTime){
        return this.data.get(startTime, endTime, 0, data.getCols());
    }

    /**
     * @return the splitType
     */
    public int getSplitType() {
        return splitType;
    }

    /**
     * @param splitType the splitType to set
     */
    public void setSplitType(int splitType) {
        this.splitType = splitType;
    }

    /**
     * @return the spikeData
     */
    public Matrix getSpikeData() {
        return spikeData;
    }

    /**
     * @param spikeData the spikeData to set
     */
    public void setSpikeData(Matrix spikeData) {
        this.spikeData = spikeData;
    }

    /**
     * @return the numFeatures
     */
    public int getNumFeatures() {
        return numFeatures;
    }

    /**
     * @param numFeatures the numFeatures to set
     */
    public void setNumFeatures(int numFeatures) {
        this.numFeatures = numFeatures;
    }

    /**
     * @return the numRecords
     */
    public int getNumRecords() {
        return numRecords;
    }

    /**
     * @param numRecords the numRecords to set
     */
    public void setNumRecords(int numRecords) {
        this.numRecords = numRecords;
    }

    /**
     * @return the sampleId
     */
    public int getSampleId() {
        return sampleId;
    }

    /**
     * @param sampleId the sampleId to set
     */
    public void setSampleId(int sampleId) {
        this.sampleId = sampleId;
    }

    /**
     * @return the validationClassId
     */
    public double getValidationClassId() {
        return validationClassId;
    }

    /**
     * @param validationClassId the validationClassId to set
     */
    public void setValidationClassId(double validationClassId) {
        this.validationClassId = validationClassId;
    }

    /**
     * @return the encoded
     */
    public boolean isEncoded() {
        return encoded;
    }

    /**
     * @param encoded the encoded to set
     */
    public void setEncoded(boolean encoded) {
        this.encoded = encoded;
    }

    /**
     * @return the sampleFileName
     */
    public String getSampleFileName() {
        return sampleFileName;
    }

    /**
     * @param sampleFileName the sampleFileName to set
     */
    public void setSampleFileName(String sampleFileName) {
        this.sampleFileName = sampleFileName;
    }

}
