//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2021.05.11 at 02:45:17 PM CEST 
//
package org.datacite.schema.kernel_4;

import fr.cnes.doi.utils.spec.Requirement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for funderIdentifierType.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * <p>
 * &nbsp;
 * <pre>
 * &lt;simpleType name="funderIdentifierType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="ISNI"/&gt;
 *     &lt;enumeration value="GRID"/&gt;
 *     &lt;enumeration value="ROR"/&gt;
 *     &lt;enumeration value="Crossref Funder ID"/&gt;
 *     &lt;enumeration value="Other"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 *
 */
@Requirement(reqId = Requirement.DOI_SRV_010, reqName = Requirement.DOI_SRV_010_NAME)
@Requirement(reqId = Requirement.DOI_SRV_040, reqName = Requirement.DOI_SRV_040_NAME)
@Requirement(reqId = Requirement.DOI_INTER_060, reqName = Requirement.DOI_INTER_060_NAME)
@XmlType(name = "funderIdentifierType")
@XmlEnum
public enum FunderIdentifierType {

    /**
     *
     */
    ISNI("ISNI"),
    /**
     *
     */
    GRID("GRID"),
    /**
     *
     */
    ROR("ROR"),
    @XmlEnumValue("Crossref Funder ID")
    CROSSREF_FUNDER_ID("Crossref Funder ID"),
    /**
     *
     */
    @XmlEnumValue("Other")
    OTHER("Other");
    private final String value;

    FunderIdentifierType(String v) {
        value = v;
    }

    /**
     * Returns the value.
     *
     * @return the value
     */
    public String value() {
        return value;
    }

    /**
     * Returns the FunderIdentifierType from the value
     *
     * @param v value
     * @return the FunderIdentifierType
     */
    public static FunderIdentifierType fromValue(String v) {
        for (FunderIdentifierType c : FunderIdentifierType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
