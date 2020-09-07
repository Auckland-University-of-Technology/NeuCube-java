/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.data;

import java.util.Calendar;
import java.util.GregorianCalendar;
import jneucube.util.Matrix;
import static jneucube.log.Log.LOGGER;
/**
 *
 * @author Josafath Israel Espinosa Ramos
 */
public class OnlineSeismicFileReader extends OnlineReader {
    
    private String fileDir;
    private String fileName;
    private int hourIncreament = 1;
    private Calendar currentCalendar = Calendar.getInstance(); 
    
    //Logger LOGGER = LoggerFactory.getLogger(OnlineSeismicFileReader.class);

    @Override
    public DataSample getData() {
        DataSample sample = new DataSample();
        Matrix dayRecord;
        
        int year=currentCalendar.get(Calendar.YEAR);
        int dayOfYear = currentCalendar.get(Calendar.DAY_OF_YEAR)-1;
        int hourOfDay=currentCalendar.get(Calendar.HOUR_OF_DAY);
        
        long numRecord = (dayOfYear * 24) + hourOfDay;
        LOGGER.debug("      - Reading file " + getFileDir() + getFileName() + this.hourIncreament + ".csv");
        LOGGER.debug("      - Num day " + dayOfYear + " num hour " + hourOfDay + " num record " + numRecord);

        dayRecord = new Matrix(fileDir + fileName+year+".csv", ",", numRecord);
        
        sample.setNumFeatures(dayRecord.getCols());
        sample.setNumRecords(dayRecord.getRows());
        sample.setData(dayRecord);

        this.currentCalendar.add(Calendar.HOUR_OF_DAY, hourIncreament);

        return sample;
    }

    public void increaseHour() {

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
     * @return the hourIncreament
     */
    public int getHourIncreament() {
        return hourIncreament;
    }

    /**
     * @param hourIncreament the hourIncreament to set
     */
    public void setHourIncreament(int hourIncreament) {
        this.hourIncreament = hourIncreament;
    }

    /**
     * @return the currentCalendar
     */
    public Calendar getCurrentCalendar() {
        return currentCalendar;
    }

    /**
     * @param currentCalendar the currentCalendar to set
     */
    public void setCurrentCalendar(Calendar currentCalendar) {
        this.currentCalendar = currentCalendar;
    }



    public static void main(String args[]) {
        OnlineSeismicFileReader reader = new OnlineSeismicFileReader();
        DataSample sample;
        Calendar currentDate = new GregorianCalendar(2010,Calendar.JANUARY,1,0,0,0);   
        
        reader.setFileDir("C:\\DataSets\\seismic\\seismic_52ch_2010-2013\\");
        reader.setFileName("");
        reader.setCurrentCalendar(currentDate);
        for (int i = 0; i < 48; i++) {
            sample = reader.getData();
            sample.getData().show();
        }
    }    

    @Override
    public void initialize() {
        this.hourIncreament=1;
    }
}
