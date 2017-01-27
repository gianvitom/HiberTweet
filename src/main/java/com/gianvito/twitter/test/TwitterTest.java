/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gianvito.twitter.test;

import com.gianvito.twitter.Search;
import com.gianvito.util.Twitter4JSupport;
import java.io.IOException;
import twitter4j.TwitterException;

/**
 *
 * @author gianvito
 */
public class TwitterTest {
    //private static HosebirdSupport ts;
    private static Twitter4JSupport ts4j;
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TwitterTest.class.getName());
    
    
    public static void main(String[] args) throws TwitterException, IOException {
        log.debug("Prova del supporto a Twitter per Hibernate Search");
        search();
        
    }
    
    public static void test2(){
        ts4j = new Twitter4JSupport();
        ts4j.authentication(Boolean.TRUE);
        ts4j.searchString("cod", "popular", 10, "all", Long.MAX_VALUE, null, null);
        ts4j.stampaTweets();
    }
    
    public static void search(){
        Search search = new Search("cod");
        search.search();
    }
    
    /**
    public static void utilizzaHoseBird(){
        //ts = new HosebirdSupport();
        //HosebirdSupport ts = new HosebirdSupport();
        //ts.creaInfoDiConnessione();
        //ts.creaClient();
        
        //BlockingQueue<String> msgQueue = ts.getMsgQueue();
        
        for (int i = 0; i < 2; i++) {
            try {
                String tweet = msgQueue.take();
                log.debug(tweet);
            } catch (InterruptedException ex) {
                Logger.getLogger(TwitterTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        //ts.chiudiConnessione();
    }


    public static void esempio() throws TwitterException, IOException{
        ts4j = new Twitter4JSupport();
        Twitter twitter = ts4j.autenticazione(Boolean.TRUE);
        try {
            Query query = new Query("cod patch ps4");
            query.setResultType(Query.ResultType.popular);
            query.setCount(10);
            QueryResult result;
            do {
                result = twitter.search(query);
                List<Status> tweets = result.getTweets().subList(0, 1);
                for (Status tweet : tweets) {
                    log.debug("@" + tweet.getUser().getScreenName() + " - " + tweet.getText());
                }
            } while ((query = result.nextQuery()) != null);
            System.exit(0);
        } catch (TwitterException te) {
            te.printStackTrace();
            log.debug("Failed to search tweets: " + te.getMessage());
            System.exit(-1);
        }
    }
    */
}
