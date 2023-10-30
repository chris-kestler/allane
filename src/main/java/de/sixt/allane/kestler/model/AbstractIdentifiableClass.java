package de.sixt.allane.kestler.model;

import jakarta.persistence.*;

import java.io.Serializable;

/** All tables currently have an id field. This class takes advantage of this for Generics */
@MappedSuperclass
public abstract class AbstractIdentifiableClass<T extends Serializable> implements Serializable, Identifiable<T> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private T id;

    public T getId(){
        return this.id;
    }

    public void setId(T t ){
        this.id = t;
    }
}