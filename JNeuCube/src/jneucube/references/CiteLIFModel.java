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
public class CiteLIFModel extends Citation{
    
    public CiteLIFModel(){
        this.setInfo();
    }

    @Override
    public void setInfo() {
        this.setAuthor("Wulfram Gerstner and Werner M. Kistler");
        this.setTitle("Spiking Neuron Models: Single Neurons, Populations, Plasticity");
        this.setJournal("Cambridge University Press");
        this.setYear("2002");
        this.setVolume("");
        this.setNumber("");
        this.setPages("102");
        this.setNote("");
        this.setUrl("");
    }

}
