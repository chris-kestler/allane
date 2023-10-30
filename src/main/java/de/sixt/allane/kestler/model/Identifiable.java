package de.sixt.allane.kestler.model;

import java.io.Serializable;

/** All tables currently have an id field. This interface, and Abstract Identifiable class, takes advantage of this for Generics */
public interface Identifiable<T extends Serializable> {

    T getId();
    void setId(T t );
}