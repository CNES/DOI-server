/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.logging.shell;

import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;

/**
 * Creates an handler with the Shell formatter.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public final class ShellHandler extends ConsoleHandler {

    /**
     * Handler that represents a output shell.
     */
    public ShellHandler() {
        super();
        this.setFormatter(new ShellFormatter());
        this.setLevel(Level.INFO);
    }

    /**
     * Set a Formatter. This Formatter will be used to format LogRecords for this Handler.
     * Some Handlers may not use Formatters, in which case the Formatter will be remembered, but not used.
     * @param newFormatter the Formatter to use (may not be null)
     * @throws SecurityException - if a security manager exists and if the caller does not have LoggingPermission("control").
     */
    @Override
    public synchronized void setFormatter(final Formatter newFormatter) throws SecurityException {
        super.setFormatter(newFormatter);
    }

    /**
     * Set the log level specifying which message levels will be logged by this Handler. 
     * Message levels lower than this value will be discarded. The intention is to 
     * allow developers to turn on voluminous logging, but to limit the messages 
     * that are sent to certain Handlers.
     * @param newLevel - the new value for the log level
     * @throws SecurityException - if a security manager exists and if the caller does not have LoggingPermission
     */
    @Override
    public synchronized void setLevel(final Level newLevel) throws SecurityException {
        super.setLevel(newLevel);
    }

}
