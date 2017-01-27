package com.gianvito.twitter.DAO;

import com.gianvito.twitter.Tweet;
import com.gianvito.util.HibernateUtil;
import java.util.ArrayList;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.search.FullTextSession;

/**
 * DAO di salvataggio. Salva i dati presenti nella List tweet nel DB
 * 
 * @author Gianvito Morena
 */
public class SaveDAO {
    // Logger
    private static final Logger log = Logger.getLogger(SaveDAO.class.getName());
    // Lista di tweet da salvare
    private List<Tweet> tweets;
    
    // Costruttore vuoto
    public SaveDAO(){
        tweets = new ArrayList<>();
    }
    
    // Costruttore con la lista da salvare in ingresso
    public SaveDAO(List<Tweet> tweets){
        this.tweets = tweets;
    }
    
    // Funzione di salvataggio dei Tweet
    public void saveTweetsOnDB(){
        Integer tweetSalvati = 0;
        
        // Creiamo la Session
        Session session = HibernateUtil.getSessionFactory().openSession();
        // Creiamo la Transaction per la nostra operazione
        
        Transaction tx = session.beginTransaction();
        log.debug("Stiamo per salvare " + tweets.size() + " tweet");
        for(Tweet tweet: tweets){
            Tweet tweetDB = (Tweet)session.get(Tweet.class, tweet.getId());
            if(tweetDB == null){
                // Funzione di salvataggio del Tweet
                session.save(tweet);
                tweetSalvati++;
            }
        }
        // Commit della Transaction
        tx.commit();
        // Forziamo il salvataggio su DB
        session.flush();
        // Chiudiamo la Session
        session.close();
        log.debug("Salvati " + tweets.size() + " tweet");
        notify("Dati salvati", "Salvataggio di: " + tweetSalvati + " tweet.");
    }
    
    
    // Indicizzazione delle righe del DB attraverso Hibernate Search
    public void dataIndex(){
    	log.debug("Facciamo l'index dei dati sul db");
        // Creiamo la Session
        Session session = HibernateUtil.getSessionFactory().openSession();
        // Creiamo la FullTextSession di Hibernate Search
    	FullTextSession fullTextSession = org.hibernate.search.Search.getFullTextSession(session);
    	try {
                // Richiamiamo la funzione per l'indicizzazione
                fullTextSession.createIndexer().startAndWait();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        
        // Chiudiamo la Session
    	session.close();
        notify("Dati indicizzati", "Indice dei token aggiornato.");
    }
    
    // Funzione di notifica all'interfaccia Web
    public void notify(String notifiedText, String details){
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, notifiedText, details));
    }
    
    public List<Tweet> getTweets() {
        return tweets;
    }
    
    public void setTweets(List<Tweet> tweets) {
        this.tweets = tweets;
    }
    
    // Aggiunge i Tweet alla lista di salvataggio
    public Boolean addTweet(Tweet tweet){
        Boolean result = tweets.add(tweet);
        return result;
    }
    
    
}
