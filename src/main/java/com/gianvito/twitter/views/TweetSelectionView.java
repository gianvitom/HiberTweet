package com.gianvito.twitter.views;

import com.gianvito.twitter.DAO.SaveDAO;
import com.gianvito.twitter.Tweet;
import java.io.Serializable;
import java.util.List;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.apache.log4j.Logger;
import org.primefaces.event.map.OverlaySelectEvent;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;

/**
 * Funzione di View per la sezione di Tweet nel p:dataTable presente nell'interfaccia
 * 
 * @author Gianvito Morena
 */
@Named("tweetSelView")
@ViewScoped
public class TweetSelectionView implements Serializable{
    // Logger
    static final Logger log = Logger.getLogger(TweetSelectionView.class.getName());
    // Lista di Tweet selezionati
    List<Tweet> tweets;
    // MapModel utilizzato dal componente di PrimeFaces
    MapModel advancedModel;
    // Marker all'interno della mappa
    private Marker marker;
    // Coordinata latitudine per il centro della mappa
    Double centerLat;
    // Coordinata longitudine per il centro della mappa
    Double centerLon;
    
    // Costruttore vuoto
    public TweetSelectionView(){
        advancedModel = new DefaultMapModel();
        centerLat = new Double(0.0);
        centerLon = new Double(0.0);
    }
   
    // Stampa dei tweet trovati
    public void printTweetsInExportView(){
        for (Tweet tweet : tweets) {
            log.debug("@" + tweet.getUtente() + " -----> " + tweet.getTesto());
        }
    }
    
    // Salvataggio della selezione sul DB
    public void saveSelectionOnDB(){
        SaveDAO saveDAO = new SaveDAO(tweets);
        saveDAO.saveTweetsOnDB();
    }
    
    
    // Aggiunta delle coordinate sulla mappa p:gmap
    public void addCoordinates(){
        for (Tweet tweet : tweets) {
            if(!(tweet.getLocation() == null)){
                if(centerLat == 0.0){
                    // Carichiamo le info sulla posizione direttamente dal Tweet
                    centerLat = tweet.getLocation().getLatitude();
                    centerLon = tweet.getLocation().getLongitude();
                }
                LatLng tweetCoordinates = new LatLng(tweet.getLocation().getLatitude(), tweet.getLocation().getLongitude());
                log.debug("Impostazione delle coordinate: " + tweet.getLocation().getLatitude() + " " + tweet.getLocation().getLongitude());
                // Aggiungiamo il marker al model
                advancedModel.addOverlay(new Marker(tweetCoordinates, tweet.getUtente()));
            }
        }
    }
    
    // Rimuove le coordinate dalla mappa p:gmap
    public void removeCoordinates(){
        advancedModel.getMarkers().clear();
        resetCenter();
    }
    
    // Effettua il reset delle coordinate del centro della mappa
    public void resetCenter(){
        centerLat = 0.0;
        centerLon = 0.0;
    }
    
    // Restituisce l'overlay inserito in precedenza in relazione alla pressione
    //  sul marker sulla p:gmap
    public void onMarkerSelect(OverlaySelectEvent event) {
        marker = (Marker) event.getOverlay();
    }
    
    
    public MapModel getAdvancedModel() {
        return advancedModel;
    }  
    public Marker getMarker() {
        return marker;
    }
    public List<Tweet> getTweets(){
        return tweets;
    }
    public Double getCenterLat() {
        return centerLat;
    }
    public Double getCenterLon() {
        return centerLon;
    }
    public void setTweets(List<Tweet> tweets) {
        this.tweets = tweets;
        printTweetsInExportView();
        removeCoordinates();
        addCoordinates();
    }
    public void setMarker(Marker marker) {
        this.marker = marker;
    }
    public void setCenterLat(Double centerLat) {
        this.centerLat = centerLat;
    }
    public void setCenterLon(Double centerLon) {
        this.centerLon = centerLon;
    }
    
}
