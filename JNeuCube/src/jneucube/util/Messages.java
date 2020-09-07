/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.util;

/**
 *
 * @author em9403
 */
public enum Messages {

    NO_MESSAGE(0,0,"",""),
    DATA_LOAD_SUCCESS(0,11000,"Spatio-temporal data.","Spatio-temporal data was successfully loaded"),
    DATA_SAMPLE(1,11001, "Incorrect folder structure.","Please select a valid folder."),
    DATA_FOLDER_STRUCTURE_SAMPLE(1,11002, "The folder does not contain valid files.","Please include valid files that maches with sam[0-9]*.*"),
    DATA_FOLDER_STRUCTURE_CLASS(1,11003, "The folder does not contain a valid file to define classes.","Please include a valid file that maches wih tar*.*, containing the classes for each data sample."), 
    DATA_CLASS_FILE(1,11004, "The number of reccords in the 'tar' file do not correspond to the number of samples.",""),
    DATA_SAMPLE_FEATURES(1,11005,"Some samples contain different number of features.","Please"),
    DATA_SAMPLE_RECORDS(2,11006,"Some samples contain different number of records.","Please"),       
    DATA_SET_EMPTY(1,11007,"Empty data set.","Please load a valid dataset."),
    
    DATA_ENCODED_SUCCESS(0,12000,"Spatio-temporal data was successfully encoded.",""),
    DATA_ENCODED_FAIL(1,12001,"An error occured while encoding spatio-temporal data.","Please contact software develepment team."),
    DATA_ENCODED_FAlSE(1,12002,"No data is encoded into spike trains.","Please encode the data before stimulating the network."),
    
    CHART_SAVE_SUCCESS(0,1,"The chart was successfully saved.",""),
    CHART_SAVE_FAIL(1,1,"An error occured while saving the char.","Please contact software develepment team."),
    
    SPIKING_NEURON_UPDATED(0,13001,"Neuron model properties were successfully updated",""),
    
    DISPLAY_OPEN_ERROR(1,14001,"There were some problems oppening 3D displays.","Please contact software develepment team."),
            
    NETWORK_CREATION_SUCCESS(0,15001,"The network was successfully created.",""),
    INPUT_NEURONS_MISMATCH(1,15002,"The number of input neurons do not correspond to the number of data features.","Please modify the input neuron file."),
    EMPTY_RESERVOIR(2,15003,"The reservoir is empty.","Please create the reservoir and input neurons."),
    
    SAVE_PROPERTIES_SUCCED(0,16001,"The properties where successfully saved.",""),
    SAVE_PROPERTIES_ERROR(1,16002,"An error occured while sabe the properties.","Please contact software develepment team.");
    
    //... add more cases here ...

    private final int type; // 0 info, 1 error, 2 warning
    private final int id;
    private final String message;
    private final String masthead;

    Messages(int type, int id, String masthead, String message) {
        this.type=type;
        this.id = id;
        this.masthead=masthead;
        this.message = message;
    }
    public int getType(){
        return type;
    }

    public int getId() {
        return id;
    }

    public String getMasthead(){
        return masthead;
    }
    public String getMessage() {
        return message;
    }

    @Override
    public String toString(){
        String error="";
        switch (getType()){
            case 0:{error="Information ";}break;
            case 1:{error="Error ";}break;
            case 2:{error="Warning ";}break;
        }                
        return error+getId()+": "+getMasthead()+getMessage();
    }

}
