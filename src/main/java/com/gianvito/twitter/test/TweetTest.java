/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gianvito.twitter.test;

import com.gianvito.twitter.DAO.SaveDAO;
import com.gianvito.twitter.Tweet;

/**
 *
 * @author gianvito
 */
public class TweetTest {
    public static void main( String[] args ){
    	//indexDati();
    	inserisciTweet();
    	//querySulDB();
    }
    
    public static void inserisciTweet(){
        SaveDAO save = new SaveDAO();
        Tweet tweet = new Tweet();
        
        save.addTweet(tweet);
        
        save.saveTweetsOnDB();
    }
}
