/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.utils;

import java.lang.annotation.Repeatable;

/**
 * Annotation to map requirements with code
 * @author Jean-Christophe Malapert <jean-christophe.malapert@cnes.fr>
 */
@Repeatable(Requirements.class)
public @interface Requirement {
    String documentName() default "SGDS-ST-8000-1538-CNES_0100";
    String reqId();
    String reqName();
    String comment() default "[none]";    
}
