/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.references;

/**
 *
 * @author Josafath Israel Espinosa Ramos
 */
public class CiteSmallWorld extends Citation {

    public CiteSmallWorld() {
        this.setInfo();
    }

    @Override
    public void setInfo() {
        this.setAuthor("J. Hu and Z. G. Hou and Y. X. Chen and N. Kasabov\n\t\tand N. Scott");
        this.setTitle("EEG-based classification of upper-limb ADL using\n\t\tSNN for active robotic rehabilitation");
        this.setJournal("5th IEEE RAS/EMBS International Conference on\n\t\tBiomedical Robotics and Biomechatronics");
        this.setYear("2014");
        this.setVolume("");
        this.setNumber("");
        this.setPages("409-414");
        this.setNote("Conference paper");
        this.setUrl("\thttp://ieeexplore.ieee.org/abstract/document/6913811/");
    }

}
