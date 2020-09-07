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
public class CiteIzhikevichModel extends Citation {
    
    public CiteIzhikevichModel(){
        this.setInfo();
    }

    @Override
    public void setInfo() {
        this.setAuthor("Eugene M. Izhikevich");
        this.setTitle("Simple model of spiking neurons");
        this.setJournal("IEEE Transactions on Neural Networks");
        this.setYear("2003");
        this.setVolume("14");
        this.setNumber("6");
        this.setPages("1569-1572");
        this.setNote("See also 'Which model to use for cortical spiking\n\t\tneurons?'");
        this.setUrl("\thttp://www.izhikevich.org/publications/spikes.pdf");
    }

}
