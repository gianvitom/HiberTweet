package com.gianvito.comment;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.faces.bean.NoneScoped;
import javax.inject.Named;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.Entity;
import org.hibernate.search.annotations.*;


/**
 * Entita' Commento
 * 
 * @author Gianvito Morena
 */
@Entity
@Indexed
@Named("comment")
@NoneScoped
public class Comment implements java.io.Serializable{
        
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
        
        // Argomento del commento
	@Field(index=Index.YES, analyze=Analyze.YES, store=Store.NO)
	private String topic;

        // Autore del commento
	@Field(index=Index.YES, analyze=Analyze.YES, store=Store.NO)
	private String autore;
	
        // Testo del commento
	@Field(index=Index.YES, analyze=Analyze.YES, store=Store.NO)
	@Analyzer(definition = "synonym_analyzer")
	private String testo;

        // Data di pubblicazione del commento
	@Field(index=Index.YES, analyze=Analyze.NO, store=Store.NO)
	@DateBridge(resolution=Resolution.DAY)
	private Date publicationDate;
        
        // Altre variabili di supporto
        private String sd;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	
        public Comment(){
            this.autore = new String();
            this.testo = new String();
            this.topic = new String();
            this.publicationDate = new Date();
        }
        
        public Comment(Integer id, String topic, String autore, String testo, Date data){
            this.id = id;
            this.topic = topic;
            this.autore = autore;
            this.testo = testo;
            this.publicationDate = new Date();
        }
        
        public Integer getId() {
            return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getAutore() {
		return autore;
	}

	public void setAutore(String autore) {
		this.autore = autore;
	}

	public String getTesto() {
		return testo;
	}

	public void setTesto(String testo) {
		this.testo = testo;
	}

	public Date getPublicationDate() {
		return publicationDate;
	}

	public void setPublicationDate(Date publicationDate) {
            this.publicationDate = new Date();
	}
	
}
