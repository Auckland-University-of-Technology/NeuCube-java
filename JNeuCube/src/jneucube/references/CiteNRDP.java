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
public class CiteNRDP extends Citation {

    public CiteNRDP() {
        this.setInfo();
    }

    @Override
    public void setInfo() {
        this.setAuthor("J. I. Espinosa-Ramos and E. Capecci");
        this.setTitle("Neuroreceptor Dependent Plasticity");
        this.setJournal("");
        this.setYear("");
        this.setVolume("");
        this.setNumber("");
        this.setPages("");
        this.setNote("");
        this.setUrl("");
    }

}
