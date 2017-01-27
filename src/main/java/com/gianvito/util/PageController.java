package com.gianvito.util;

import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import org.apache.log4j.Logger;

/**
 * Utility per il cambio di pagina nell'applicazione Web
 * 
 * @author Gianvito Morena
 */
@Named
@SessionScoped
public class PageController implements Serializable{
    // Logger
    private static final Logger log = Logger.getLogger(PageController.class.getName());
    // Pagina attuale
    private String page;
    
    // Costruttore vuoto
    public PageController(){
        this.page = "welcome";
    }
    
    // Costruttore con pagina in ingresso
    public PageController(String page){
        this.page = page;
    }
    
    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
        log.debug("Cambio pagina in " + this.page);
    }
    
    // Imposta la pagina per mostrare i dati dal DB
    public void setPageShowResults(){
        this.setPage("showResults");
    }
    
    // Imposta la pagina per l'inserimento dei dati nel DB
    public void setPageInsertData(){
        this.setPage("insertData");
    }
    
    // Imposta la pagina di benvenuto
    public void setPageWelcome(){
        this.setPage("welcome");
    }
    
    // Imposta la pagina per la ricerca su Twitter
    public void setPagePrintTwitter(){
        this.setPage("printTwitter");
    }
    
    // Imposta la pagina per la ricerca nel DB con Hibernate Search
    public void setPageTweetsInDB(){
        this.setPage("tweetsInDB");
    }

}
