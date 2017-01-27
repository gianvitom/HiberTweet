package com.gianvito.twitter;

import java.io.Serializable;
import javax.faces.bean.NoneScoped;
import javax.inject.Named;
import javax.persistence.Id;
import org.hibernate.annotations.Entity;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

/**
 * Entita' Hashtag
 * 
 * @author Gianvito Morena
 */
@Entity
@Named
@Indexed
@NoneScoped
public class Hashtag implements Serializable{
    
    @Id
    private Long id;
    
    // Stringa dell'hashtag
    @Field(index=Index.YES, analyze=Analyze.YES, store=Store.NO)
    private String hashtag;
    
    public Hashtag(){
        hashtag = "null";
    }
    
    public Hashtag(String hashtag){
        this.hashtag = hashtag;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    public String getHashtag() {
        return hashtag;
    }

    public void setHashtag(String hashtag) {
        this.hashtag = hashtag;
    }
    
    
}
