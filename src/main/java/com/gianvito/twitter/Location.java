/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gianvito.twitter;

import java.io.Serializable;
import javax.faces.bean.NoneScoped;
import javax.inject.Named;
import javax.persistence.Id;
import org.hibernate.annotations.Entity;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Latitude;
import org.hibernate.search.annotations.Longitude;
import org.hibernate.search.annotations.Spatial;
import org.hibernate.search.annotations.SpatialMode;

/**
 * Entita' Location
 * 
 * @author Gianvito Morena
 */
@Named
@Entity
@NoneScoped
@Indexed
@Spatial(spatialMode = SpatialMode.GRID)
public class Location implements Serializable{
    
    @Id
    private Long id;
    
    // Coordinata latitudine
    @Latitude
    private Double latitude;
    
    // Coordinata longitudine
    @Longitude
    private Double longitude;
    
    // Costruttore vuoto
    public Location(){

    }
    
    public Long getId() {
        return id;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    
    
}
