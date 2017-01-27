package com.gianvito.twitter;

import com.gianvito.util.Twitter4JSupport;
import com.gianvito.twitter.DAO.SaveDAO;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import static org.apache.log4j.Level.DEBUG;
import org.apache.log4j.Logger;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.event.map.GeocodeEvent;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.GeocodeResult;
import org.primefaces.model.map.Marker;


/**
 * Funzione di ricerca di stringhe su Twitter in base a diversi parametri inseriti in ingresso.
 * 
 * @author Gianvito Morena
 */
@Named("search")
@SessionScoped
public class Search implements Serializable{
    // Logger di Log4J
    private static final Logger log = Logger.getLogger(Search.class.getName());
    // Testo da ricercare
    private String text;
    // Lista di tweet trovati
    private ArrayList<Tweet> tweets;
    // Tipo di ricerca da effettuare (mista, recenti, popolari)
    private String searchTypeNP;
    // Numero di risultati paginati
    private Integer countNum;
    // Lingua di ricerca
    private String language;
    // Indica se siamo di fronte alla prima ricerca o quella di pagine successive
    private Boolean firstSearch;
    // Valore che permette di salvare l'ID più basso per la paginazione
    private Long lowestId;
    // Entità di supporto a Twitter4J
    private Twitter4JSupport t4js;
    // Coordinate di ricerca
    private LatLng searchCoordinates;
    // Raggio di ricerca
    private Double searchRadius;
    // Centro della mappa visibile nell'interfaccia Web
    private String centerGeoMap;
    // Zoom dell'interfaccia
    private Integer zoomOnGeoMap;
    // Modello utilizzato da PrimeFaces per il rendering della mappa
    private MapModel geoModel;
    
    // Cpstruttore vuoto
    public Search(){
        this.text = "";
        this.searchTypeNP = "mixed";
        this.countNum = 15;
        this.lowestId = Long.MAX_VALUE;
        this.firstSearch = true;
        this.language = "it";
        this.geoModel = new DefaultMapModel();
        this.centerGeoMap = "0, 0";
        this.zoomOnGeoMap = 2;
    }
    
    // Costruttore con in ingresso solo il testo da ricercare
    public Search(String text){
        //this.tweets = new ArrayList<>();
        this.text = text;
        //this.t4js = new Twitter4JSupport();
        this.searchTypeNP = "mixed";
        this.countNum = 10;
    }
    
    // Funzione di ricerca
    public void search(){
        // Creiamo la funzione di supporto a Twitter4J
        t4js = new Twitter4JSupport();
        
        // Effettuiamo l'autenticazione
        log.debug("Autenticazione software");
        t4js.authentication(Boolean.TRUE);
        
        // Riportiamo al valore massimo lowestId
        lowestId = Long.MAX_VALUE;
        
        // Impostiamo che e' la prima search per questa keyword
        firstSearch = true;
        
        // Richiamiamo il metodo per la ricerca e assegiamo i tweet risultanti
        lowestId = t4js.searchString(text, searchTypeNP, countNum, language, lowestId, searchCoordinates, searchRadius);
        this.tweets = t4js.getTweets();
        
        // Impostiamo che non è più la prima ricerca
        firstSearch = false;
        
        // Notifichiamo
        notify("Ricerca effettuata", "Risultati attuali: " + tweets.size());
        
    }
    
    // Salva quasiasi entità in tweets all'interno del DB
    public void saveEverythingOnDB(){
        SaveDAO saveDAO = new SaveDAO(tweets);
        saveDAO.saveTweetsOnDB();
    }
    
    // Caricamento dei tweet successivi
    public void loadOtherTweets(){
        if(!firstSearch){
            log.debug("Cerchiamo altri tweet con il lowestId uguale a: " + lowestId);
            lowestId = t4js.searchString(text, searchTypeNP, countNum, language, lowestId, searchCoordinates, searchRadius);
            tweets.addAll(t4js.getTweets());
            //stampaTweetInSearch();
            log.debug("Ora il lowestId e' uguale a:" + lowestId);
            notify("Sono stati caricati altri tweet", "Risultati attuali: " + tweets.size());
        }else{
            // Non fa niente dato che l'utente deve effettuare prima una ricerca normale
        }
    }
    
    // Funzione di risposta all'evento proveniente dall'interfaccia oer la ricerca della posizione in LAT e LONG
    public void onGeocode(GeocodeEvent event){
        List<GeocodeResult> results = event.getResults();
        geoModel = new DefaultMapModel();
        
        if (results != null && !results.isEmpty()) {
            LatLng center = results.get(0).getLatLng();
            log.debug("Impostazione della posizione a " + center.getLat() + " " + center.getLng());
            centerGeoMap = center.getLat() + "," + center.getLng();
            GeocodeResult result = results.get(0);
            geoModel.addOverlay(new Marker(result.getLatLng(), result.getAddress()));
            zoomOnGeoMap = 9;
            searchCoordinates = result.getLatLng();
             
            // Possiamo prendere anche gli altri risultati
            /*
            for (int i = 0; i < results.size(); i++) {
                GeocodeResult result = results.get(i);
                geoModel.addOverlay(new Marker(result.getLatLng(), result.getAddress()));
            }
            */
        }
    }
    
    // Funzione di rimozione della posizione trovata
    public void removePosition(){
        geoModel = new DefaultMapModel();
        this.centerGeoMap = "0, 0";
        this.searchRadius = 100.0;
        this.searchCoordinates = null;
        this.zoomOnGeoMap = 2;
        log.debug("Posizione rimossa");
    }
    
    public String getText() {
        return text;
    }
    public List<Tweet> getTweets() {
        return (List<Tweet>) tweets;
    }
    public String getSearchTypeNP() {
        return searchTypeNP;
    }
    public Integer getCountNum() {
        return countNum;
    }
    public String getLanguage() {
        return language;
    }
    public Long getLowestId() {
        return lowestId;
    }
    public void setText(String text) {
        this.text = text;
        log.log(DEBUG, "Il testo della Search e' " + text);
    }
    public void setTweets(ArrayList<Tweet> tweets) {
        this.tweets = (ArrayList<Tweet>) tweets;
    }
    public void setSearchTypeNP(String searchTypeNP) {
        this.searchTypeNP = searchTypeNP;
    }
    public void setCountNum(Integer countNum) {
        this.countNum = countNum;
    }
    public void setLanguage(String language) {
        this.language = language;
    }
    public void setLowestId(Long lowestId) {
        this.lowestId = lowestId;
    }
    public void setSearchCoordinates(LatLng searchCoordinates) {
        this.searchCoordinates = searchCoordinates;
    }
    public LatLng getSearchCoordinates() {
        return searchCoordinates;
    }
    public MapModel getGeoModel() {
        return geoModel;
    }
    public String getCenterGeoMap() {
        return centerGeoMap;
    }
    public Double getSearchRadius() {
        return searchRadius;
    }
    public void setSearchRadius(Double searchRadius) {
        this.searchRadius = searchRadius;
        log.debug("Raggio di ricerca impostato a " + this.searchRadius);
    }
    public Integer getZoomOnGeoMap() {
        return zoomOnGeoMap;
    }
    public void setZoomOnGeoMap(Integer zoomOnGeoMap) {
        this.zoomOnGeoMap = zoomOnGeoMap;
    }
    
    public void notify(String notifiedText, String details){
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, notifiedText, details));
    }
    
    public void printTweetsInSearch(){
        for (Tweet tweet : tweets) {
            log.debug(tweet.getUtente() + " -----> " + tweet.getTesto());
        }
    }
}
