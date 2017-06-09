/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.logging.shell;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

/**
 * Creates an handler with the Shell formatter.
 * @author Jean-Christophe Malapert
 */
public class ShellHandler extends ConsoleHandler {
    
    public ShellHandler() {
        super();
        this.setFormatter(new ShellFormatter());
        this.setLevel(Level.INFO);
    }
    
    
}
