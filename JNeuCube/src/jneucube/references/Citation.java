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
public abstract class Citation {

    public static final CiteSTDP CITE_STDP=new CiteSTDP();
    public static final CiteNRDP CITE_NRDP=new CiteNRDP();
    public static final CiteDeSNNs CITE_DE_SNN_S=new CiteDeSNNs();
    public static final CiteAER CITE_AER=new CiteAER();
    public static final CiteOnlineAER CITE_ONLINE_AER=new CiteOnlineAER();
    public static final CiteSmallWorld CITE_SMALL_WORLD=new CiteSmallWorld();
    public static final CiteIzhikevichModel CITE_IZHIKEVICH_MODEL=new CiteIzhikevichModel();
    public static final CiteLIFModel CITE_LIF_MODEL=new CiteLIFModel();
    
    String author = "";
    String title = "";
    String year = "";
    String journal = "";
    String volume = "";
    String number = "";
    String pages = "";
    String note = "";
    String url = "";
    
    public abstract void setInfo();

    /**
     * @return the author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * @param author the author to set
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the year
     */
    public String getYear() {
        return year;
    }

    /**
     * @param year the year to set
     */
    public void setYear(String year) {
        this.year = year;
    }

    /**
     * @return the journal
     */
    public String getJournal() {
        return journal;
    }

    /**
     * @param journal the journal to set
     */
    public void setJournal(String journal) {
        this.journal = journal;
    }

    /**
     * @return the volume
     */
    public String getVolume() {
        return volume;
    }

    /**
     * @param volume the volume to set
     */
    public void setVolume(String volume) {
        this.volume = volume;
    }

    /**
     * @return the number
     */
    public String getNumber() {
        return number;
    }

    /**
     * @param number the number to set
     */
    public void setNumber(String number) {
        this.number = number;
    }

    /**
     * @return the pages
     */
    public String getPages() {
        return pages;
    }

    /**
     * @param pages the pages to set
     */
    public void setPages(String pages) {
        this.pages = pages;
    }

    /**
     * @return the note
     */
    public String getNote() {
        return note;
    }

    /**
     * @param note the note to set
     */
    public void setNote(String note) {
        this.note = note;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        StringBuilder bd = new StringBuilder();
        bd.append("Author:\t").append(this.author).append("\n");
        bd.append("Title:\t\t").append(this.title).append("\n");
        bd.append("Journal:\t").append(this.journal).append("\n");
        bd.append("Year:\t\t").append(this.year).append("\n");
        bd.append("Volume:\t").append(this.volume).append("\n");
        bd.append("Number:\t").append(this.number).append("\n");
        bd.append("Pages:\t").append(this.pages).append("\n");        
        bd.append("Note:\t").append(this.note).append("\n");
        bd.append("URL:\t").append(this.url).append("\n");
        return bd.toString();
    }

}
