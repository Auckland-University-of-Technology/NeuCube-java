/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.data;

import java.util.ArrayList;
import java.util.Arrays;


/**
 *
 * @author em9403
 */
public abstract class OnlineReader {

    public static final OnlineSingleFileReader ONLINE_SINGLE_FILE_READER =new OnlineSingleFileReader();
    public static final OnlineMultipleFileReader ONLINE_MULTIPLE_FILE_READER =new OnlineMultipleFileReader();    
    public static final ArrayList<OnlineReader> ONLINE_FILE_READER_LIST = new ArrayList<>(Arrays.asList(ONLINE_SINGLE_FILE_READER,ONLINE_MULTIPLE_FILE_READER));
    
    private boolean encoded=false;
    
    public abstract DataSample getData();
    public abstract void initialize();

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
}
