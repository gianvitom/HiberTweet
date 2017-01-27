package com.gianvito.util;
 
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Utility per la gestione del ciclo di vita di Hibernate
 * 
 * @author Gianvito Morena
 */
public class HibernateUtil {
        
        // Session Factory
	private static SessionFactory sessionFactory = buildSessionFactory();
 
        // Costruzione della Session Factory
	private static SessionFactory buildSessionFactory() {
		try {
			// Crea la SessionFactory da hibernate.cfg.xml
			return new Configuration().configure().buildSessionFactory();
		} catch (Throwable ex) {
			System.err.println("Initial SessionFactory creation failed." + ex);
			throw new ExceptionInInitializerError(ex);
		}
	}
        
        // Restituisce la Session Factory
	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}
        
        // Effettua la chiusura della SessionFactory
	public static void shutdown() {
		// Close caches and connection pools
		getSessionFactory().close();
	}
        
        // Riavvia la SessionFactory
        public static SessionFactory restart(){
            shutdown();
            sessionFactory = buildSessionFactory();
            return sessionFactory;
        }
 
}