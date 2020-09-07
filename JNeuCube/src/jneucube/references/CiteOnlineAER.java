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
public class CiteOnlineAER extends Citation {

    public CiteOnlineAER() {
        this.setInfo();
    }

    @Override
    public void setInfo() {
        this.setAuthor("Josafath I. Espinosa-Ramos");
        this.setTitle("Online thresholding base algorithm");
        this.setJournal("");
        this.setYear("");
        this.setVolume("");
        this.setNumber("");
        this.setPages("");
        this.setNote("Based on the thresholding algorithm described in \n\t\t"
                + "'Fast sensory motor control based on event-based\n\t\t"
                + "hybrid neuromorphic-procedural system' (T. Delbruck\n\t\t"
                + "and P. Lichtsteiner). This online version utilises\n\t\t"
                + "the recursive first (mean) and the second (variance)\n\t\t"
                + "moments to produce a spike.");
        this.setUrl("https://www.researchgate.net/publication/224714447_Fast_sensory_motor_control_based_on_event-based_hybrid_neuromorphic-procedural_system");

    }

}
