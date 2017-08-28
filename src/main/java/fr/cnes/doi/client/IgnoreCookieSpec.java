/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.client;

import java.util.Collections;
import java.util.List;
import org.apache.http.Header;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.impl.cookie.CookieSpecBase;

/**
 *
 * @author Jean-Christophe Malapert <jean-christophe.malapert@cnes.fr>
 */
public class IgnoreCookieSpec extends CookieSpecBase{

    /**
     * Returns an empty list.
     * 
     * @param cookies
     * @return An empty list.
     */
    @Override
    public List<Header> formatCookies(List<Cookie> cookies) {
        return Collections.emptyList();
    }

    /**
     * Returns '0' as version.
     * 
     * @return '0' as version.
     */
    @Override
    public int getVersion() {
        return 0;
    }

    /**
     * Returns a null version header.
     * 
     * @return A null version header.
     */
    @Override
    public Header getVersionHeader() {
        return null;
    }

    /**
     * Returns an empty list.
     * 
     * @param header
     * @param origin
     * @return An empty list.
     * @throws org.apache.http.cookie.MalformedCookieException
     */
    @Override
    public List<Cookie> parse(Header header, CookieOrigin origin)
            throws MalformedCookieException {
        return Collections.emptyList();
    }
    
}
