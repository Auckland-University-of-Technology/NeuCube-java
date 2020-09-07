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
public class CiteDeSNNs extends Citation {

    public CiteDeSNNs() {
        this.setInfo();
    }

    @Override
    public void setInfo() {
        this.setAuthor("Nikola Kasabov, Kshitij Dhoblea, Nuttapod Nuntalid and\n\t\tGiacomo Indiveri");
        this.setTitle("Dynamic evolving spiking neural networks for on-line spatio-\n\t\tand spectro-temporal pattern recognition");
        this.setJournal("Neural Networks");
        this.setYear("2013");
        this.setVolume("41");
        this.setNumber("");
        this.setPages("188--201");
        this.setNote("Special Issue on Autonomous Learning");
        this.setUrl("http://dx.doi.org/10.1016/j.neunet.2012.11.014");
    }

}
