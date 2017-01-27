package com.gianvito.util;

import com.gianvito.twitter.Hashtag;
import com.gianvito.twitter.Location;
import com.gianvito.twitter.Tweet;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import static org.apache.log4j.Level.DEBUG;
import static org.apache.log4j.Level.ERROR;
import twitter4j.GeoLocation;
import twitter4j.HashtagEntity;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import org.apache.log4j.Logger;
import org.primefaces.model.map.LatLng;

/**
 * Supporto a Twitter4J per la corretta gestione dei Tweet
 * 
 * @author Gianvito Morena
 */
@Named
@ViewScoped
public class Twitter4JSupport implements Serializable{
    // Risultato della query
    private QueryResult qr;
    // File dove sono memorizzati i dati di autenticazione per Twitter
    private final String propertiesFile = "/WEB-INF/OAuth.properties";
    // Dati di autenticazione per Twitter
    private String consumerKey, consumerSecret, token, tokenSecret;
    // Lista dei tweet risultanti dalla ricerca
    private ArrayList<Tweet> tweets;
    // ID più basso attualmente trovato (utilizzato per la paginazione)
    private Long lowestId = Long.MAX_VALUE;
    // Logger
    private static final Logger log = Logger.getLogger(Twitter4JSupport.class.getName());
    
    // Costruttore vuoto
    public Twitter4JSupport(){      
        
    }
    
    // Autenticazione OAuth
    public Twitter authentication(Boolean developer){
        // Istanza della Factory riutilizzabile in quanto thread-safe
        // Caricamento dei parametri
        loadParameters();
        Twitter twitter = TwitterFactory.getSingleton();
        AccessToken accessToken = new AccessToken(token, tokenSecret);
        try{
            // Impostazione dei dati di sicurezza per Twitter
            twitter.setOAuthConsumer(consumerKey, consumerSecret);
            twitter.setOAuthAccessToken(accessToken);
        }catch(Exception ex){
            log.error("Problema sull'autentication su Twitter: " + ex.toString());
        }
        
        return twitter;
    }    
    
    // Caricamento dei parametri
    public void loadParameters(){
       Properties props = new Properties();
       InputStream is;
       
       // Prova a caricare il file in locale
       try {
           File f = new File(propertiesFile);
           is = new FileInputStream(f);
       } catch (Exception e) {
           is = null;
           log.debug("Non carico il file properties come InputStream");
       }
       
       // Carica il file per l'applicazione Web
       try {
           log.debug("Caricamento del file " + propertiesFile);
           ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
           props.load(ec.getResourceAsStream(propertiesFile));
       } catch (Exception e) {
           log.error("Errore nel caricamento del file properties: " + e.toString());
       }

       consumerKey = props.getProperty("twitter.consumer.key");
       consumerSecret = props.getProperty("twitter.consumer.key.secret");
       token = props.getProperty("twitter.consumer.token");
       tokenSecret = props.getProperty("twitter.consumer.token.secret");
    }
    
    // Crea la lista di entita' Tweet dalla QueryResult di Twitter4J
    public List<Tweet> createTweets(List<Status> statuses){
        tweets = new ArrayList<>();
        
        for(Status status: statuses){
            Tweet tweet = new Tweet();
            
            // Impostazione data di pubblicazione
            tweet.setCreatedAt(status.getCreatedAt());
            
            // Impostazione del numero di volte che e' stato preferito
            tweet.setFavouriteCount(status.getFavoriteCount());
            
            // Creazione della lista di hashtag
            HashtagEntity[] hentities = status.getHashtagEntities();
            if (hentities != null){
                for(HashtagEntity hentity: hentities){
                    Hashtag htag = new Hashtag(hentity.getText());
                    tweet.addHashtag(htag);
                }
            }
            
            // Impostazione dell'ID
            tweet.setId(status.getId());
            
            // Impostazione della lingua
            tweet.setLanguage(status.getLang());
            
            // Impostazione della posizione
            GeoLocation geo = status.getGeoLocation();
            if(geo != null && geo.getLatitude() != 0 && geo.getLongitude() != 0){
                log.debug("Dati geografici per "+ tweet.getId() + " " + geo.getLatitude() + " " + geo.getLongitude());
                Location location = new Location();
                location.setLatitude(geo.getLatitude());
                location.setLongitude(geo.getLongitude());
                tweet.setLocation(location);
            }
            
            // Impostazione del numero di retweet
            tweet.setRetweetCount(status.getRetweetCount());
            
            // Impostazione del testo
            tweet.setTesto(status.getText());
            
            // Impostazione dello username del tweeter
            tweet.setUtente(status.getUser().getScreenName());
            
            // Aggiornamento dell'ID minimo per la ricerca paginata
            if(tweet.getId() < lowestId){
                lowestId = tweet.getId();
            }
            
            // Aggiunta del tweet alla lista privata
            tweets.add(tweet);
        }
        
        return tweets;
    }
    
    // Ricerca della stringa su Twitter
    public Long searchString(String text, String searchType, Integer numCount, String language, Long lowestId, LatLng coordinates, Double radius){
        try {
            Twitter twitter = TwitterFactory.getSingleton();
            
            // Impostazione del lowestID per la paginazione
            this.lowestId = lowestId;
            
            Query query = new Query(text);
            
            switch(searchType){
                case "popular":
                    log.log(DEBUG, "Ricerca per popolarita'");
                    query.setResultType(Query.ResultType.popular);
                    break;
                case "recent":
                    log.log(DEBUG, "Ricerca per tweet piu' recenti");
                    query.setResultType(Query.ResultType.recent);
                    break;
                case "mixed":
                    log.log(DEBUG, "Ricerca mista");
                    query.setResultType(Query.ResultType.mixed);
                    break;
                default:
                    log.log(DEBUG, "Non e' stata definita una ricerca");
                    break;
            }
            // Impostazione del numero di risultati
            query.setCount(numCount);
            // Impostazione della lingua per la ricerca
            if(!language.equals("all")){
                query.setLang(language);
            }
            // Impostazione della paginazione
            query.setMaxId(lowestId);
            // Impostazione della posizione
            if(coordinates != null){
                GeoLocation geo = new GeoLocation(coordinates.getLat(), coordinates.getLng());
                query.setGeoCode(geo, radius, Query.Unit.km);
            }
            
            QueryResult result;
            
            // Esecuzione della query
            result = twitter.search(query);
            ArrayList<Status> statuses = (ArrayList<Status>) result.getTweets();
            
            // Creazione della lista di entita Tweet
            tweets = (ArrayList<Tweet>) createTweets(statuses);
            
        } catch (TwitterException ex) {
            log.log(ERROR, null, ex);
        }
        
        return this.lowestId;
    }
    
    // Utility per la stampa degli Status di QueryResult
    public void stampaStatus(List<Status> statuses){
        for (Status tweet : statuses) {
            log.debug("@" + tweet.getUser().getScreenName() + " - " + tweet.getText());
        }
    }
    
    // Utility per la stampa dei Tweet
    public void stampaTweets(){
        for (Tweet tweet : tweets) {
            log.debug("@" + tweet.getUtente() + " -----> " + tweet.getTesto());
        }
    }

    public ArrayList<Tweet> getTweets() {
        return tweets;
    }
    public void setTweets(ArrayList<Tweet> tweets) {
        this.tweets = tweets;
    }
      
}
