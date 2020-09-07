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
public class CiteAER extends Citation{

    public CiteAER() {
        this.setInfo();
    }

    @Override
    public void setInfo() {
        this.setAuthor("T. Delbruck and P. Lichtsteiner");
        this.setTitle("Fast sensory motor control based on event-based\n\t\thybrid neuromorphic-procedural system");
        this.setJournal("International Symposium on Circuits and Systems\n\t\t(ISCAS) 2007");
        this.setYear("2007");
        this.setVolume("");
        this.setNumber("");
        this.setPages("845--848");
        this.setNote("");
        this.setUrl("https://www.researchgate.net/publication/224714447_Fast_sensory_motor_control_based_on_event-based_hybrid_neuromorphic-procedural_system");
    }
}
