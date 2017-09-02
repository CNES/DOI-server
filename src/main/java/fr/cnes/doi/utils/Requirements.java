/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.utils;

/**
 * Annotation to allow the use @requirement more than one
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public @interface Requirements {
    Requirement[] value();
}

