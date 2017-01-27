package com.gianvito.twitter.views;

import com.gianvito.twitter.Tweet;
import com.gianvito.util.HibernateUtil;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.hibernate.Session;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.Marker;

/**
 * Estensione della funzione di View per la sezione di Tweet nel p:dataTable presente nell'interfaccia
 * in caso di ricerca su DB
 * 
 * @author Gianvito Morena
 */
@Named("tweetSelViewDB")
@ViewScoped
public class TweetSelectionViewDB extends TweetSelectionView{
    
    @Override
    public void addCoordinates(){
        Session session = HibernateUtil.getSessionFactory().openSession();
        for (Tweet tweet : super.tweets) {
            Tweet tweetDB = (Tweet)session.get(Tweet.class, tweet.getId());
            if(!(tweetDB.getLocation() == null)){
                if(centerLat == 0.0){
                    centerLat = tweetDB.getLocation().getLatitude();
                    centerLon = tweetDB.getLocation().getLongitude();
                }
                LatLng tweetCoordinates = new LatLng(tweetDB.getLocation().getLatitude(), tweetDB.getLocation().getLongitude());
                log.debug("Impostazione delle coordinate: " + tweetDB.getLocation().getLatitude() + " " + tweetDB.getLocation().getLongitude());
                advancedModel.addOverlay(new Marker(tweetCoordinates, tweetDB.getUtente()));
            }
        }
        session.close();
    }
    
}
