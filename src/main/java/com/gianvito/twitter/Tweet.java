/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gianvito.twitter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.faces.bean.NoneScoped;
import javax.inject.Named;
import javax.persistence.Id;
import org.apache.solr.analysis.LowerCaseFilterFactory;
import org.apache.solr.analysis.PhoneticFilterFactory;
import org.apache.solr.analysis.SnowballPorterFilterFactory;
import org.apache.solr.analysis.StandardFilterFactory;
import org.apache.solr.analysis.StandardTokenizerFactory;
import org.apache.solr.analysis.SynonymFilterFactory;

import org.hibernate.annotations.Entity;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.AnalyzerDef;
import org.hibernate.search.annotations.AnalyzerDefs;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Parameter;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.annotations.TokenFilterDef;
import org.hibernate.search.annotations.TokenizerDef;


/**
 * Entita' Tweet
 * 
 * @author Gianvito Morena
 */
@Entity
@Indexed
@Named("tweet")
@NoneScoped
@Analyzer(impl = org.apache.lucene.analysis.standard.StandardAnalyzer.class)
@AnalyzerDefs({
    // Definizione dell'analyzer per i sinonimi
    @AnalyzerDef(name = "synonym_analyzer",
            tokenizer = @TokenizerDef(factory = StandardTokenizerFactory.class),
            filters = {@TokenFilterDef(factory = LowerCaseFilterFactory.class)
                    ,@TokenFilterDef(factory = SynonymFilterFactory.class,
            params = {@Parameter(name = "ignoreCase", value = "true"),
                        @Parameter(name = "expand", value = "true"),
                        @Parameter(name = "synonyms", value="sinonimi.txt")})
                        ,@TokenFilterDef(factory = SnowballPorterFilterFactory.class,
                                        params = {@Parameter(name = "language", value = "Italian"), })
                    }
            ),
    // Definizione dell'analyzer per l'approssimazione fonetica
    @AnalyzerDef(name = "phonetic_analyzer",
            tokenizer = @TokenizerDef(factory = StandardTokenizerFactory.class),
            filters = {@TokenFilterDef(factory = StandardFilterFactory.class),
                @TokenFilterDef(factory = PhoneticFilterFactory.class,
                                params = {@Parameter(name = "encoder", value = "DoubleMetaphone"),
                                            @Parameter(name = "inject", value = "false")}
                                )
                    }
                )
})
public class Tweet implements Serializable{
    
    @Id
    private Long id;
    
    // Data di pubblicazione del Tweet
    @Field(index=Index.YES, analyze=Analyze.NO, store=Store.NO)
    private Date createdAt;
    
    // Utente del Tweet
    @Field(index=Index.YES, analyze=Analyze.NO, store=Store.NO)
    private String utente;
    
    // Testo del Tweet con l'applicazione degli analyzer
    @Fields({
        @Field(name="testo", index=Index.YES, analyze=Analyze.YES, store=Store.NO),
        @Field(name="testo_synonym", index=Index.YES, analyze=Analyze.YES, store=Store.NO, analyzer = @Analyzer(definition = "synonym_analyzer")),
        @Field(name="testo_phonetic", index=Index.YES, analyze=Analyze.YES, store=Store.NO, analyzer = @Analyzer(definition = "phonetic_analyzer"))
    })
    private String testo;
    
    // Lista di hashtag associati
    @IndexedEmbedded
    private List<Hashtag> hashtags;
    
    // Numero di volte che il Tweet e' stato inserito tra i preferiti
    @Field(index=Index.YES, analyze=Analyze.NO, store=Store.NO)
    private Integer favouriteCount;
    
    // Numero di retweet
    @Field(index=Index.YES, analyze=Analyze.NO, store=Store.NO)
    private Integer retweetCount;
    
    // Lingua del Tweet
    @Field(index=Index.YES, analyze=Analyze.NO, store=Store.NO)
    private String language;
    
    // Luogo di pubblicazione del Tweet
    @IndexedEmbedded 
    private Location location;
    

    // Costruttore vuoto
    public Tweet(){
        this.createdAt = new Date();
        this.favouriteCount = 0;
        this.id = new Long(0);
        this.language = "";
        this.retweetCount = 0;
        this.testo = "";
        this.utente = "";
        this.hashtags = new ArrayList();
    }
    
    // Secondo costruttore
    public Tweet(Date createdAt, Integer favouriteCount, Long id, String language, Location location, Integer retweetCount, String testo, String utente){
        this.createdAt = createdAt;
        this.favouriteCount = favouriteCount;
        this.id = id;
        this.language = language;
        this.location = location;
        this.retweetCount = retweetCount;
        this.testo = testo;
        this.utente = utente;
    }

    public Long getId() {
        return id;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }

    public String getUtente() {
        return utente;
    }

    public String getTesto() {
        return testo;
    }

    public List<Hashtag> getHashtags() {
        return hashtags;
    }

    public Integer getFavouriteCount() {
        return favouriteCount;
    }

    public Integer getRetweetCount() {
        return retweetCount;
    }

    public String getLanguage() {
        return language;
    }

    public Location getLocation() {
        return location;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public void setUtente(String utente) {
        this.utente = utente;
    }

    public void setTesto(String testo) {
        this.testo = testo;
    }

    public void setHashtags(ArrayList<Hashtag> hashtags) {
        this.hashtags = hashtags;
    }

    public void setFavouriteCount(Integer favoriteCount) {
        this.favouriteCount = favoriteCount;
    }

    public void setRetweetCount(Integer retweetCount) {
        this.retweetCount = retweetCount;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
    
    public void addHashtag(Hashtag htag){
        hashtags.add(htag);
    }

    public void setHashtags(List<Hashtag> hashtags) {
        this.hashtags = hashtags;
    }
    
    
}
