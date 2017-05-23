/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.resource;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

/**
 *
 * @author malapert
 */
public class DoiResource extends ServerResource {
    private String userName;

    private Object user;

    @Override
    public void doInit() {
        this.userName = getAttribute("user");
        this.user = null; // Could be a lookup to a domain object.
    }

    @Get("txt")
    public String toString() {
        return "Account of user \"" + this.userName + "\"";
    }    
}
