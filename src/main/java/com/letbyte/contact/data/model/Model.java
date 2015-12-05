package com.letbyte.contact.data.model;

/**
 * Created by nuc on 11/1/2015.
 */

public class Model<T> {
    public enum Type {
        CONTACT
    }

    public Model(T t, Type type) {
        super();
        this.t = t;
        this.type = type;
    }

    public T t;
    public Type type;
}
