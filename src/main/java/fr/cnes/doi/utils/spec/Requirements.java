/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.utils.spec;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation to allow the use @requirement more than one
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
public @interface Requirements {
    Requirement[] value();
}

