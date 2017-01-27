package com.gianvito.comment.DAO;

import com.gianvito.comment.Comment;
import com.gianvito.util.HibernateUtil;
import java.io.Serializable;
import java.util.Date;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.apache.log4j.Logger;
import org.hibernate.Session;

/**
 * DAO per Comment
 * 
 * @author Gianvito Morena
 */
@Named("commentDAO")
@ViewScoped
public class CommentDAO implements Serializable{
    // Logger
    private static final Logger log = Logger.getLogger(CommentDAO.class.getName());
    // Entità da gestire
    private Comment comment;
    // Attributi da gestire
    private String topic, author, text;
    
    // Costruttore vuoto
    public void CommentDAO(){
        comment = new Comment();
        this.author = new String();
        this.topic = new String();
        this.text = new String();
    }
    
    // Costruttore con topic, autore e testo in ingresso
    public void CommentDAO(String topic, String author, String text){
        comment = new Comment(0, topic, author, text, new Date());
    }
        
    // Inserimento di un commento nel DB
    public void insertComment(){
        log.debug("Inserimento di un commento");
        comment = new Comment(0, topic, author, text, new Date());
        
        // Apriamo la Session
        Session session = HibernateUtil.getSessionFactory().openSession();
        // Creiamo una Transaction
        session.beginTransaction();
        // Salviamo l'entita' Comment
        session.save(comment);
        // Commit della Transaction
        session.getTransaction().commit();
        // Chiusura della Session
        session.close();
        // Notifica l'avvenuta operazione
        notify("Commento inserito", "Il commento e' stato inserito correttamente.");
    }

    public Comment getComment() {
        return comment;
    }
    public void setComment(Comment comment) {
        this.comment = comment;
    }
    public String getTopic() {
        return topic;
    }
    public String getAuthor() {
        return author;
    }
    public String getText() {
        return text;
    }
    public void setTopic(String topic) {
        this.topic = topic;
    }
    public void setAuthor(String author) {
        this.author = author;
    }
    public void setText(String text) {
        this.text = text;
    }
    
    // Notifica all'interfaccia dell'applicazione un messaggio
    public void notify(String notifiedText, String details){
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, notifiedText, details));
    }
    
    
}
