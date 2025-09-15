/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.accumed.jpa.Eclipselink.sequence;

import org.eclipse.persistence.queries.ValueReadQuery;
import org.eclipse.persistence.sequencing.NativeSequence;

/**
 *
 * @author smutlak
 */
public class IdentCurrentSequence extends NativeSequence {

    public IdentCurrentSequence() {
        super();
    }

   
    public IdentCurrentSequence(boolean shouldUseIdentityIfPlatformSupports) {
        super(shouldUseIdentityIfPlatformSupports);
    }

    public IdentCurrentSequence(String name) {
        super(name);
    }

    public IdentCurrentSequence(String name, boolean shouldUseIdentityIfPlatformSupports) {
        super(name, shouldUseIdentityIfPlatformSupports);
    }

    public IdentCurrentSequence(String name, int size) {
        super(name, size);
    }

    public IdentCurrentSequence(String name, int size, int initialValue) {
        super(name, size, initialValue);
    }

    public IdentCurrentSequence(String name, int size, int initialValue, boolean shouldUseIdentityIfPlatformSupports) {
        super(name, size, initialValue, shouldUseIdentityIfPlatformSupports);
    }

    @Override
    public ValueReadQuery getSelectQuery() {
        String query = "SELECT IDENT_CURRENT('#tablename#')";
        query = query.replaceFirst("(#tablename#)", this.getName().substring(9));
        return new ValueReadQuery(query);
    }
}
