package com.gianvito.twitter.DAO;

import com.gianvito.twitter.Search;
import com.gianvito.twitter.Tweet;
import com.gianvito.util.HibernateUtil;
import java.io.Serializable;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import org.apache.log4j.Logger;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.util.Version;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.hibernate.search.query.dsl.Unit;
import org.primefaces.event.map.GeocodeEvent;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.GeocodeResult;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;

/**
 * Permette la ricerca di Tweet all'interno del DB con l'utilizzo di Hibernate Search.
 * 
 * @author Gianvito Morena
 */
@Named("searchDB")
@SessionScoped
public class SearchDAO implements Serializable{
    // Logger
    private static final Logger log = Logger.getLogger(Search.class.getName());
    // Testo per la ricerca
    private String text;
    // Tipo di ricerca
    private String searchType;
    // Numero di risultati massimo per la ricerca (paginazione)
    private Integer resultNum;
    // Indice dell'ultima riga caricata
    private Integer lastRow;
    // Indica se è la prima ricerca che si effettua per quella Session
    private Boolean firstSearch;
    // Lista di Tweet trovati nel DB
    private List<Tweet> tweets;
    // Coordinate di ricerca
    private LatLng searchCoordinates;
    // Raggio di ricerca
    private Double searchRadius;
    // Centro della mappa p:gmap nell'interfaccia
    private String centerGeoMap;
    // Zoom principale della mappa nell'interfaccia
    private Integer zoomOnGeoMap;
    // Modello per il rendering della p:gmap
    private MapModel geoModel;
    // Indica se si tratta di una ricerca multipla
    private Boolean multipleSearch;
    // Lista dei vari tipi di ricerca scelti
    private List<String> multipleChoiceList;
    // Indica se si tratta di AND o di OR in una ricerca multipla
    private String booleanChoice;
    
    // Costruttore vuoto
    public SearchDAO(){
        this.searchType = "normale";
        this.resultNum = 10;
        this.lastRow = 0;
        this.firstSearch = true;
        this.centerGeoMap = "0, 0";
        this.geoModel = new DefaultMapModel();
        this.zoomOnGeoMap = 2;
        this.multipleSearch = false;
        this.multipleChoiceList = new ArrayList<>();
    }
    
    // Effettuiamo la ricerca per la prima volta (prima pagina di risultati)
    public void search(){
        this.lastRow = 0;
        this.firstSearch = true;
        this.searchOnDB();
        this.firstSearch = false;
    }
    
    // Caricamente degli altri Tweet presenti per una query
    public void loadOtherTweets(){
        this.searchOnDB();
    }
    
    // Funzione di ricerca vera e propria che sfrutta Hibernate Search
    public void searchOnDB(){    	
    	// Creiamo la Session e la FullTextSession
        log.debug("Ricerca di " + text);
    	Session session = createSession();
    	FullTextSession fts = createFullTextSession(session);
    	
    	// Comincia la transazione
    	Transaction tx = fts.beginTransaction();
    	
    	// Costruiamo la query
    	QueryBuilder qBuilder = fts.getSearchFactory().buildQueryBuilder().forEntity(Tweet.class).get();
    	org.apache.lucene.search.Query query = null;
        
        // Scegliamo la query
        if(!multipleSearch){
            switch(searchType){
                case "normale":
                    // Query normale
                    log.debug("Query normale");
                    query = createNormalQuery(qBuilder);
                    break;
                case "fuzzy":
                    // Query Fuzzy
                    log.debug("Query fuzzy");
                    query = createFuzzyQuery(qBuilder);
                    break;
                case "wildcard":
                    // Query wildcard
                    log.debug("Query wildcard");
                    query = createWildcardQuery(qBuilder);
                    break;
                case "frase":
                    // Phrase query
                    log.debug("Query fonetica");
                    query =  createPhraseQuery(qBuilder);
                    break;
                case "sinonimi":
                    log.debug("Sinonimi");
                    // Query sui sinonimi
                    query = createSynonymQuery(qBuilder, fts);
                    break;
                case "spaziale":
                    log.debug("Ricerca spaziale");
                    // Query di tipo spaziale
                    query = createSpatialQuery(qBuilder);
                    break;
                case "fonetica":
                    // Query con approssimazione fonetica
                    log.debug("Query fonetica");
                    query =  createPhoneticQuery(qBuilder, fts);
                    break;
                default:
                    // Query normale in default
                    log.debug("Query normale in default");
                    query = createNormalQuery(qBuilder);
                    break;
            }
        }else{
            // Nel caso di ricerca multipla andiamo a costruire la Query bool()
            log.debug("Ricerca multipla");
            query = buildQueries(qBuilder, fts);
        }
        

    	// Creiamo la FullTextQuery e la eseguiamo in base al tasto premuto
        try{
            FullTextQuery hQuery = fts.createFullTextQuery(query, Tweet.class);
            // Impostiamo l'indice del primo elemento
            hQuery.setFirstResult(lastRow);
            log.debug("Impostazione ultima riga " + lastRow);
            
            // Impostazione del numero massimo di risultati
            hQuery.setMaxResults(resultNum);
            log.debug("Numero di risultati impostato: " + resultNum);
            
            // Associazione della lista di risultati dopo aver eseguito la query
            List<Tweet> result =  (List<Tweet>)hQuery.list();
            
            // Aggiornamento dell’ultima riga per la paginazione
            lastRow = lastRow + result.size();
            log.debug("Sono presenti altri " + result.size() + " elementi");
            
            // Comportamento diverso in base a se e' la prima ricerca effettuata
            if(firstSearch){
                tweets = result;
            }else{
                for(Tweet otherTweet: result)
                    tweets.add(otherTweet);
            }
            log.debug("Ci sono: " + tweets.size() + " elementi in Tweets");

            //stampaTweetTrovati(tweets);
            notify("Query effettuata", "Risultati attuali: " + tweets.size() + " elementi.");
        }catch(NullPointerException ex){
            log.debug(ex);
        }
        
        // Commit della Transaction della Session
    	tx.commit();
        // Chiusura della Session
    	session.close();
        
    }
    
    // Funzione di indicizzazione di Hibernate Search
    public void dataIndex(){
    	log.debug("Facciamo l'index dei dati sul db");
        // Creiamo la Session
        Session session = HibernateUtil.restart().openSession();
        // Creiamo la FullTextSession
    	FullTextSession fullTextSession = org.hibernate.search.Search.getFullTextSession(session);
        // Creiamo la Transaction
        Transaction transaction = fullTextSession.beginTransaction();

    	try {
                // Richiamiamo la funzione di indicizzazione di Hibernate Search
                fullTextSession.createIndexer().startAndWait();
		} catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    Connection close = fullTextSession.close();
                    session.close();
		}
        
        // Commit della Transaction
        transaction.commit();
        // Forziamo la scrittura dei dati
        fullTextSession.flush();
        // Forziamo la scrittura degli indici
        fullTextSession.flushToIndexes();
        // Forziamo la scrittura della sessione
        session.flush();        
        // Chiudiamo la Session
    	session.close();
        
        notify("Indici ricostruiti", "I valori presenti sono stati aggiornati");
    }
    
    // Funzione di risposta all'evento proveniente dall'interfaccia oer la ricerca della posizione in LAT e LONG
    public void onGeocode(GeocodeEvent event) {
        List<GeocodeResult> results = event.getResults();
        geoModel = new DefaultMapModel();
        
        if (results != null && !results.isEmpty()) {
            LatLng center = results.get(0).getLatLng();
            log.debug("Impostazione della posizione a " + center.getLat() + " " + center.getLng());
            centerGeoMap = center.getLat() + "," + center.getLng();
            zoomOnGeoMap = 9;
            GeocodeResult result = results.get(0);            
            geoModel.addOverlay(new Marker(result.getLatLng(), result.getAddress()));
            searchCoordinates = result.getLatLng();
        }
    }
    
    // Funzione di rimozione della posizione
    public void removePosition(){
        geoModel = new DefaultMapModel();
        this.centerGeoMap = "0, 0";
        this.searchRadius = 100.0;
        this.searchCoordinates = null;
        this.zoomOnGeoMap = 2;
        log.debug("Posizione rimossa");
    }
    
    // Rilascio del lock di Hibernate Search
    public void closeHS(){
        HibernateUtil.shutdown();
        log.debug("Lock di Hibernate Search rilasciato");
        notify("Hibernate Search rilasciato", "Puoi chiudere il browser");
    }
    
    // Riavvio della Session Factory
    public void rebootHS(){
        HibernateUtil.restart();
        log.debug("Hibernate Search riavviato");
        notify("Hibernate Search riavviato", "Pronto ad eseguire");
    }
    
    
    // Notifica delle informazioni
    public void notify(String notifiedText, String details){
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, notifiedText, details));
    }
    
    // Notifica degli errori
    public void notifyError(String notifiedText, String details){
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, notifiedText, details));
    }
    
    // Creazione di una Session
    public Session createSession(){
    	return HibernateUtil.getSessionFactory().openSession();
    }
    
    // Creazione di una FullTextSession
    public FullTextSession createFullTextSession(Session session){
    	return  org.hibernate.search.Search.getFullTextSession(session);
    }
    
    // Stampa dei Tweet trovati con log4j
    public void printFoundTweets(List<Tweet> results){
        for(Tweet tweet: results){
            log.debug("Tweet: @" + tweet.getUtente() + " --> " + tweet.getTesto());
        }
    }
    
    // Creazione di una query normale
    public org.apache.lucene.search.Query createNormalQuery(QueryBuilder qBuilder){
    	return qBuilder.keyword().onFields("testo") // Sul campo testo
                                .matching(text) // Con il testo corrispondente a text
                                .createQuery();
    }
    
    // Creazione di una query fuzzy
    public org.apache.lucene.search.Query createFuzzyQuery(QueryBuilder qBuilder){
    	return qBuilder.keyword().fuzzy()
                                .withThreshold(.5f) // Valore di soglia per il matching
                                .withPrefixLength(1) // Lunghezza del prefisso
                                .onField("testo") // Campo di ricerca
                                .matching(text) // Testo di ricerca
                                .createQuery();
    }
    
    // Creazione di una query wildcard
    public org.apache.lucene.search.Query createWildcardQuery(QueryBuilder qBuilder){
    	return qBuilder.keyword().wildcard()
                                .onField("testo") // Campo di ricerca
                                .matching(text + "*") // Testo con token *
                                .createQuery();
    }
    
    // Ricerca di una frase
    public org.apache.lucene.search.Query createPhraseQuery(QueryBuilder qBuilder){
    	return qBuilder.phrase().withSlop(4) // Numero di altre parole massimo tra quelle cercate
                                .onField("testo")   // Campo di ricerca
                                .sentence(text) // Testo di ricerca
                                .createQuery();
    }
    
    // Ricerca multipla
    public org.apache.lucene.search.Query createMultipleQuery(QueryBuilder qBuilder, FullTextSession fts){
        // Costruiamo la query multipla
    	return buildQueries(qBuilder, fts);
    }
    
    // Ricerca per sinonimi
    public org.apache.lucene.search.Query createSynonymQuery(QueryBuilder qBuilder, FullTextSession fts){
        
        // Utilizziamo il QueryParse per utilizzare l'indice specifico per i sinonimi sul campo testo
        QueryParser qparser = new QueryParser(Version.LUCENE_36, "testo", fts.getSearchFactory().getAnalyzer("synonym_analyzer"));
        
        org.apache.lucene.search.Query lQuery = null;
        try {
             // Impostiamo l'indice appropriato
             lQuery = qparser.parse("testo_synonym:"+text);
        } catch (ParseException ex) {
            java.util.logging.Logger.getLogger(SearchDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return lQuery;
    }
    
    // Ricerca di tipo spaziale
    public org.apache.lucene.search.Query createSpatialQuery(QueryBuilder qBuilder){
        if(searchCoordinates != null & searchRadius != null){
            return qBuilder.spatial().onDefaultCoordinates().within(searchRadius, Unit.KM) // Raggio di copertura
                                                        .ofLatitude(searchCoordinates.getLat()) // Latitutine
                                                        .andLongitude(searchCoordinates.getLng()).createQuery(); // Longitudine
        }else{
            notifyError("Devi specificare una posizione per la ricerca spaziale", "");
        }
        
        return null;
    	
    }
    
    // Ricerca per approssimazione fonetica
    public org.apache.lucene.search.Query createPhoneticQuery(QueryBuilder qBuilder, FullTextSession fts){
        
        // Utilizziamo il QueryParse per utilizzare l'indice specifico per i sinonimi sul campo testo
        QueryParser qparser = new QueryParser(Version.LUCENE_36, "testo", fts.getSearchFactory().getAnalyzer("phonetic_analyzer"));
        
        org.apache.lucene.search.Query lQuery = null;
        try {
            // Impostiamo l'indice appropriato
            lQuery = qparser.parse("testo_phonetic:"+text);
        } catch (ParseException ex) {
            java.util.logging.Logger.getLogger(SearchDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return lQuery;
    }
    
    
    public void setNormalSearch(){
        this.searchType = "Normale";
        log.debug("Impostazione --> Ricerca normale");
    }
    public void setWildcardSearch(){
        this.searchType = "Wildcard";
        log.debug("Impostazione --> Ricerca wildcard");
    }
    public void setFuzzySearch(){
        this.searchType = "Fuzzy";
        log.debug("Impostazione --> Ricerca fuzzy");
    }
    public void setPhoneticSearch(){
        this.searchType = "Phonetic";
        log.debug("Impostazione --> Ricerca Phonetic");
    }
    public void setSynonymsSearch(){
        this.searchType = "Synonyms";
        log.debug("Impostazione --> Ricerca synonyms");
    }
    public String getText() {
        return text;
    }
    public String getSearchType() {
        return searchType;
    }
    public List<Tweet> getTweets() {
        return tweets;
    }
    public void setText(String text) {
        this.text = text;
    }
    public void setSearchType(String searchType) {
        log.debug("Impostiamo la ricerca a " + searchType);
        this.searchType = searchType;
    }
    public void setTweets(ArrayList<Tweet> tweets) {
        this.tweets = tweets;
    }
    public Integer getResultNum() {
        return resultNum;
    }
    public void setResultNum(Integer resultNum) {
        this.resultNum = resultNum;
    }
    public void setTweets(List<Tweet> tweets) {
        this.tweets = tweets;
    }
    public LatLng getSearchCoordinates() {
        return searchCoordinates;
    }
    public Double getSearchRadius() {
        return searchRadius;
    }
    public String getCenterGeoMap() {
        return centerGeoMap;
    }
    public MapModel getGeoModel() {
        return geoModel;
    }
    public void setSearchCoordinates(LatLng searchCoordinates) {
        this.searchCoordinates = searchCoordinates;
    }
    public void setSearchRadius(Double searchRadius) {
        this.searchRadius = searchRadius;
    }
    public void setCenterGeoMap(String centerGeoMap) {
        this.centerGeoMap = centerGeoMap;
    }
    public void setGeoModel(MapModel geoModel) {
        this.geoModel = geoModel;
    }
    public Integer getZoomOnGeoMap() {
        return zoomOnGeoMap;
    }
    public void setZoomOnGeoMap(Integer zoomOnGeoMap) {
        this.zoomOnGeoMap = zoomOnGeoMap;
    }
    public String getBooleanChoice() {
        return booleanChoice;
    }
    public void setBooleanChoice(String booleanChoice) {
        this.booleanChoice = booleanChoice;
    }
    public Boolean getMultipleSearch() {
        return multipleSearch;
    }
    public Boolean isMultipleSearch() {
        return multipleSearch;
    }
    public List<String> getMultipleChoiceList() {
        return multipleChoiceList;
    }
    // Impostazione di ricerca multipla
    public void setMultipleSearch(Boolean multipleSearch) {
        log.debug("Ricerca multipla? --> " + this.multipleSearch);
        if(this.multipleSearch == true){
            this.multipleSearch = false;
            log.debug("Ricerca multipla disabilitata");
        }else if(this.multipleSearch == false){
            this.multipleSearch = true;
            log.debug("Ricerca multipla abilitata");
        }
    }
    public void setMultipleChoiceList(List<String> multipleChoiceList) {
        this.multipleChoiceList = multipleChoiceList;
    }
    
    // Funzione di stampa dei Tweet
    public void printTweets(List<Tweet> tweets){
        for(Tweet tweet: tweets){
            log.debug("Tweet:" + tweet.getUtente() + "    " + tweet.getTesto());
        }
    }
    
    // Permette di costruire una query multipla a partire dalle impostazioni dall'interfaccia Web
    public org.apache.lucene.search.Query buildQueries(QueryBuilder qBuilder, FullTextSession fts){
        List<org.apache.lucene.search.Query> queries = new ArrayList<>();
        org.apache.lucene.search.Query result = null;
        // Dimensione della query
        Integer listDim = multipleChoiceList.size();
        
        for(int i = 0; i < listDim; i++){
            // Parsing dell'i-esimo elemento
            queries.add(parseListElement(qBuilder, fts, multipleChoiceList.get(i)));
        }
        
        // Richiamiamo la funzione specifica per il numero di elementi must o should
        switch(listDim){
            case 2:
                result = create2Query(qBuilder, queries);
                break;
            case 3:
                result = create3Query(qBuilder, queries);
                break;
            case 4:
                result = create4Query(qBuilder, queries);
                break;
            case 5:
                result = create5Query(qBuilder, queries);
                break;
            case 6:
                result = create6Query(qBuilder, queries);
                break;
            case 7:
                result = create7Query(qBuilder, queries);
                break;
        }
        return result;
    }
    
    // Parsing dell'elemento element per capire il tipo di quert
    public org.apache.lucene.search.Query parseListElement(QueryBuilder qBuilder, FullTextSession fts, String element){
        org.apache.lucene.search.Query query = null;
        switch(element){
            case "normale":
                // Query normale
                log.debug("Query normale");
                query = createNormalQuery(qBuilder);
                break;
            case "fuzzy":
                // Fuzzy query
                log.debug("Query fuzzy");
                query = createFuzzyQuery(qBuilder);
                break;
            case "wildcard":
                // Query wildcard
                log.debug("Query wildcard");
                query = createWildcardQuery(qBuilder);
                break;
            case "frase":
                // Phrase query
                log.debug("Ricerca per frase (con slop)");
                query =  createPhraseQuery(qBuilder);
                break;
            case "sinonimi":
                log.debug("Sinonimi");
                // Query sui sinonimi uguale a quella normale con la differenza dell'analyzer
                query = createSynonymQuery(qBuilder, fts);
                break;
            case "spaziale":
                log.debug("Ricerca spaziale");
                // Query sui sinonimi uguale a quella normale con la differenza dell'analyzer
                query = createSpatialQuery(qBuilder);
                break;
            case "fonetica":
                // Phrase query
                log.debug("Query fonetica");
                query =  createPhoneticQuery(qBuilder, fts);
                break;
        }
        
        return query;
    }
    
    // Query con 2 opzioni
    public org.apache.lucene.search.Query create2Query(QueryBuilder qBuilder, List<org.apache.lucene.search.Query> queries){
        org.apache.lucene.search.Query query = null;
        
        switch(booleanChoice){
            case "AND":
                query = qBuilder.bool().must(queries.get(0)).must(queries.get(1)).createQuery();
                break;
            case "OR":
                query = qBuilder.bool().should(queries.get(0)).should(queries.get(1)).createQuery();
                break;
        }
        
        return query;
    }
    
    // Query con 3 opzioni
    public org.apache.lucene.search.Query create3Query(QueryBuilder qBuilder, List<org.apache.lucene.search.Query> queries){
        org.apache.lucene.search.Query query = null;
        
        switch(booleanChoice){
            case "AND":
                query = qBuilder.bool().must(queries.get(0)).must(queries.get(1)).must(queries.get(2)).createQuery();
                break;
            case "OR":
                query = qBuilder.bool().should(queries.get(0)).should(queries.get(1)).should(queries.get(2)).createQuery();
                break;
        }
        
        return query;
    }
    
    // Query con 4 opzioni
    public org.apache.lucene.search.Query create4Query(QueryBuilder qBuilder, List<org.apache.lucene.search.Query> queries){
        org.apache.lucene.search.Query query = null;
        
        switch(booleanChoice){
            case "AND":
                query = qBuilder.bool().must(queries.get(0)).must(queries.get(1)).must(queries.get(2)).must(queries.get(3)).createQuery();
                break;
            case "OR":
                query = qBuilder.bool().should(queries.get(0)).should(queries.get(1)).should(queries.get(2)).should(queries.get(3)).createQuery();
                break;
        }
        
        return query;
    }
    
    // Query con 5 opzioni
    public org.apache.lucene.search.Query create5Query(QueryBuilder qBuilder, List<org.apache.lucene.search.Query> queries){
        org.apache.lucene.search.Query query = null;
        
        switch(booleanChoice){
            case "AND":
                query = qBuilder.bool().must(queries.get(0)).must(queries.get(1)).must(queries.get(2))
                                        .must(queries.get(3)).must(queries.get(4)).createQuery();
                break;
            case "OR":
                query = qBuilder.bool().should(queries.get(0)).should(queries.get(1)).should(queries.get(2))
                                        .should(queries.get(3)).should(queries.get(4)).createQuery();
                break;
        }
        
        return query;
    }
    
    // Query con 6 opzioni
    public org.apache.lucene.search.Query create6Query(QueryBuilder qBuilder, List<org.apache.lucene.search.Query> queries){
        org.apache.lucene.search.Query query = null;
        
        switch(booleanChoice){
            case "AND":
                query = qBuilder.bool().must(queries.get(0)).must(queries.get(1)).must(queries.get(2))
                                        .must(queries.get(3)).must(queries.get(4)).must(queries.get(5)).createQuery();
                break;
            case "OR":
                query = qBuilder.bool().should(queries.get(0)).should(queries.get(1)).should(queries.get(2))
                                        .should(queries.get(3)).should(queries.get(4)).should(queries.get(5)).createQuery();
                break;
        }
        
        return query;
    }
    
    // Query con 7 opzioni
    public org.apache.lucene.search.Query create7Query(QueryBuilder qBuilder, List<org.apache.lucene.search.Query> queries){
        org.apache.lucene.search.Query query = null;
        
        switch(booleanChoice){
            case "AND":
                query = qBuilder.bool().must(queries.get(0)).must(queries.get(1)).must(queries.get(2))
                                        .must(queries.get(3)).must(queries.get(4)).must(queries.get(5))
                                        .must(queries.get(5)).createQuery();
                break;
            case "OR":
                query = qBuilder.bool().should(queries.get(0)).should(queries.get(1)).should(queries.get(2))
                                        .should(queries.get(3)).should(queries.get(4)).should(queries.get(5))
                                        .should(queries.get(6)).createQuery();
                break;
        }
        
        return query;
    }
    
    // Abilitazione-disabilitazione della ricerca multipla
    public void enableDisableMultipleSearch(){
        log.debug("Ricerca multipla? --> " + this.multipleSearch);
        if(this.multipleSearch == true){
            this.multipleSearch = false;
            log.debug("Ricerca multipla disabilitata");
        }else if(this.multipleSearch == false){
            this.multipleSearch = true;
            log.debug("Ricerca multipla abilitata");
        }
    }
    
}
