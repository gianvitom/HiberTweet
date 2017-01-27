package com.gianvito.twitter.DAO;

import com.gianvito.comment.Comment;
import com.gianvito.twitter.Tweet;
import com.gianvito.util.HibernateUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import org.apache.log4j.Logger;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * DAO di caricamento. Permette il caricamento dei dati dal DB.
 * 
 * @author Gianvito Morena
 */
@Named("loadDB")
@SessionScoped
public class LoadDAO implements Serializable{
    // Logger
    private static final Logger log = Logger.getLogger(LoadDAO.class.getName());
    // Lista di tweet
    private List tweets;
    // Lista di commenti
    private List comments;
    // Entità di cui fare il caricamento
    private String entity;
    // Numero massimo di risultati per pagina per i Tweet
    private Integer tweetsResultsNum;
    // Numero massimo di risultati per pagina per i Comment
    private Integer commentsResultsNum;
    // Ultima riga caricata per i Tweet
    private Integer tweetsCurrentRow;
    // Ultima riga caricata per i Comment
    private Integer commentsCurrentRow;
    
    // Costruttore vuoto
    public LoadDAO(){
        this.entity = "tweet";
        this.tweetsResultsNum = 100;
        this.commentsResultsNum = 100;
        this.tweetsCurrentRow = 0;
        this.commentsCurrentRow = 0;
    }
    
    // Carica tutti i dati presenti nel DB per una determinata entità
    public void loadAll(){    	
        tweets = new ArrayList<Tweet>();
        comments = new ArrayList<Comment>();
        tweetsCurrentRow = 0;
        commentsCurrentRow = 0;
        
    	// Costruiamo la query per caricare tutti i dati dal DB
        switch(entity){
            case "tweet":
                log.debug("Carica i primi " + tweetsResultsNum + " elementi");
                loadTweets();
                notify("Tweet caricati", "Risultati attuali: " + tweets.size());
                break;
            case "comment":
                log.debug("Carica i primi " + commentsResultsNum + " elementi");
                loadComments();
                notify("Commenti caricati", "Risultati attuali: " + comments.size());
                break;
        }
    }
    
    // Carica tutti i tweet presenti nel DB
    public void loadTweets(){
        // Creiamo la session
        Session session = createSession();
        ScrollableResults sr = session.createQuery("SELECT tweet from Tweet tweet").scroll(ScrollMode.FORWARD_ONLY);
        
        if(sr.setRowNumber(tweetsCurrentRow)){
            int i = 0;
            do{
                log.debug("Riga corrente: " + tweetsCurrentRow + sr.get()[0].toString());
                Boolean ok = tweets.add((Tweet)sr.get()[0]);
                tweetsCurrentRow++;
                i++;
            }while(sr.next() && i < tweetsResultsNum);
        }
        
        sr.close();
    	session.close();
    }
    
    // Carica tutti i commenti presenti nel DB
    public void loadComments(){
        Session session = createSession();
        ScrollableResults sr = session.createQuery("SELECT comment from Comment comment").scroll(ScrollMode.FORWARD_ONLY);
        if(sr.setRowNumber(commentsCurrentRow)){
            int i = 0;
            do{
                log.debug("Riga corrente: " + commentsCurrentRow + sr.get()[0].toString());
                Boolean ok = comments.add((Comment)sr.get()[0]);
                commentsCurrentRow++;
                i++;
            }while(sr.next() && i < commentsResultsNum);
        }
        
        sr.close();
        session.close();
    }
    
    // Carica un'altra pagina relativa ai Comment
    public void continueLoadingComments(){
        loadComments();
    }
    
    // Carica un'altra pagina relativa ai Tweet
    public void continueLoadingTweets(){
        loadTweets();
    }
    
    // Cancella tutti i dati presenti nel DB relativamente all'entità selezionata
    public void deleteAll(){
        switch(entity){
            case "tweet":
                deleteEveryTweet();
                notify("Tweet cancellati", "Sono stati cancellati tutti i tweet.");
                break;
            case "comment":
                deleteEveryComment();
                notify("Commenti cancellati", "Sono stati cancellati tutti i commenti.");
                break;
        }
    }
    
    // Cancella ogni Tweet presente nel DB
    public void deleteEveryTweet(){
        // Creiamo la Session
        Session session = createSession();
        
        // Creiamo la transazione relativa a questa operazione
        Transaction tx = session.beginTransaction();
        
        List<Tweet> listaDaCancellare = session.createCriteria(Tweet.class).list();
        for(Tweet tweet: listaDaCancellare){
            session.delete(tweet);
        }
        tweets = new ArrayList<>();
        
        // Effettuiamo il commit
        tx.commit();
        // Scriviamo i dati sul DB
        session.flush();
        // Chiudiamo la Session
        session.close();
    }
    
    // Cancella ogni Comment presente nel DB
    public void deleteEveryComment(){
        Session session = createSession();
        Transaction tx = session.beginTransaction();
        
        List<Comment> listaDaCancellare = session.createCriteria(Comment.class).list();
        for(Comment comment: listaDaCancellare){
            session.delete(comment);
        }
        comments = new ArrayList<>();
        
        tx.commit();
        session.flush();
        session.close();
    }

    public String getEntity() {
        return entity;
    }
    public void setEntity(String entity) {
        this.entity = entity;
    }
    public List getTweets() {
        return tweets;
    }
    public List getComments() {
        return comments;
    }
    public Integer getTweetsResultsNum() {
        return tweetsResultsNum;
    }
    public Integer getCommentsResultsNum() {
        return commentsResultsNum;
    }
    public Integer getTweetsCurrentRow() {
        return tweetsCurrentRow;
    }
    public Integer getCommentsCurrentRow() {
        return commentsCurrentRow;
    }
    public void setTweets(List tweets) {
        this.tweets = tweets;
    }
    public void setComments(List comments) {
        this.comments = comments;
    }
    public void setTweetsResultsNum(Integer tweetsResultsNum) {
        this.tweetsResultsNum = tweetsResultsNum;
    }
    public void setCommentsResultsNum(Integer commentsResultsNum) {
        this.commentsResultsNum = commentsResultsNum;
    }
    public void setTweetsCurrentRow(Integer tweetsCurrentRow) {
        this.tweetsCurrentRow = tweetsCurrentRow;
    }
    public void setCommentsCurrentRow(Integer commentsCurrentRow) {
        this.commentsCurrentRow = commentsCurrentRow;
    }
    
    
    public Session createSession(){
    	return HibernateUtil.getSessionFactory().openSession();
    }
    
    public void notify(String notifiedText, String details){
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, notifiedText, details));
    }
    
    public void printTweets(List<Tweet> tweets){
        for(Tweet tweet: tweets){
            log.debug("Tweet:" + tweet.getUtente() + "    " + tweet.getTesto());
        }
    }
    public void printComments(List<Comment> comments){
        for(Comment comment: comments){
            log.debug("Commento:" + comment.getAutore() + "    " + comment.getTesto());
        }
    }
}
