/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.logging.api;

import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import org.restlet.engine.util.DateUtils;

/**
 * Log access formatter
 * @author malapert
 */
public class LogAccessFormatter extends Formatter {
  /**
   * Define the log format
   * 
   * @param record Log record
   * @return Returns a format such as Date - record
   */
  @Override
  public String format(LogRecord record) {
    return (DateUtils.format(new Date(), DateUtils.FORMAT_RFC_3339.get(0)) + " - " + record.getMessage() + "\n");
  }

}
