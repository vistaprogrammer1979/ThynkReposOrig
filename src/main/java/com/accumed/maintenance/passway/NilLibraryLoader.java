/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.accumed.maintenance.passway;

// Copyright (C) 2013 - Will Glozer.  All rights reserved.


/**
 * A native library loader that refuses to load libraries.
 *
 * @author Will Glozer
 */
public class NilLibraryLoader implements LibraryLoader {
    /**
     * Don't load a shared library.
     *
     * @param name      Name of the library to load.
     * @param verify    Ignored, no verification is done.
     *
     * @return false.
     */
    public boolean load(String name, boolean verify) {
        return false;
    }
}
