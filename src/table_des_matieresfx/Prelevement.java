/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package table_des_matieresfx;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


/**
 *
 * @author Stefano Crapanzano
 */
public class Prelevement {

    private final IntegerProperty id;
    private final StringProperty nom;
    private final StringProperty type;
    private final StringProperty casier;
    private final StringProperty date;
    private final StringProperty chemin;

    public Prelevement() {
        this(null, null, null, null, null, null);
    }

    public Prelevement(Integer id, String Date, String Nom, String Type, String Casier, String Chemin) {
        this.id = new SimpleIntegerProperty(id);
        this.date = new SimpleStringProperty(Date);
        this.nom = new SimpleStringProperty(Nom);
        this.type = new SimpleStringProperty(Type);
        this.casier = new SimpleStringProperty(Casier);
        this.chemin = new SimpleStringProperty(Chemin);

        this.id.set(id);
        this.nom.set(Nom);
        this.type.set(Type);
        this.casier.set(Casier);
        this.date.set(Date);
        this.chemin.set(Chemin);

    }

    public Integer getId() {
        return id.get();
    }

    public void setId(Integer Id) {
        this.id.set(Id);
    }

    public IntegerProperty idProperty() {
        return id;
    }

    /** *
     * @return the type
     */
    public String getType() {
        return type.get();
    }

    /**
     * @param Type the type to set
     */
    public void setType(String Type) {
        this.type.set(Type);
    }

    public StringProperty typeProperty() {
        return type;
    }

    /**
     * @return the casier
     */
    public String getCasier() {
        return casier.get();
    }

    /**
     * @param Casier the casier to set
     */
    public void setCasier(String Casier) {
        this.casier.set(Casier);
    }

    public StringProperty casierProperty() {
        return casier;
    }

    /**
     * @return the date
     */
    public String getDate() {
        return date.get();
    }

    /**
     * @param Date the date to set
     */
    public void setDate(String Date) {
        this.date.set(Date);
    }

    public StringProperty dateProperty() {
        return date;
    }

    public String getNom() {
        return nom.get();
    }

    public void setNom(String Nom) {
        this.nom.set(Nom);
    }

    public StringProperty nomProperty() {
        return nom;
    }

    public String getChemin() {
        return chemin.get();
    }

    public void setChemin(String Chemin) {
        this.chemin.set(Chemin);
    }

    public StringProperty cheminProperty() {
        return chemin;
    }
}
