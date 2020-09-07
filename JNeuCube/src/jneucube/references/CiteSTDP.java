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
public final class CiteSTDP extends Citation {

    public CiteSTDP() {
        this.setInfo();
    }

    @Override
    public void setInfo() {
        this.setAuthor("Jesper Sjöström and Wulfram Gerstner");
        this.setTitle("Spike-timing dependent plasticity");
        this.setJournal("Scholarpedia");
        this.setYear("2010");
        this.setVolume("5");
        this.setNumber("2");
        this.setPages("1362");
        this.setNote("revision #151671");
        this.setUrl("http://www.scholarpedia.org/article/Spike-timing_dependent_plasticity");
    }
}
