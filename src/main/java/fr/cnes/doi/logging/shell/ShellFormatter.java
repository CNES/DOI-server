/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.logging.shell;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Formats the string without the date.
 * @author Jean-Christophe Malapert <jean-christophe.malapert@cnes.fr>
 */
public final class ShellFormatter extends Formatter {

    @Override
    public String format(LogRecord record) {
        return record.getMessage();
    }
}
