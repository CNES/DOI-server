/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.datacite.schema.kernel_4.DateType;
import org.datacite.schema.kernel_4.DescriptionType;
import org.datacite.schema.kernel_4.ObjectFactory;
import org.datacite.schema.kernel_4.Point;
import org.datacite.schema.kernel_4.RelatedIdentifierType;
import org.datacite.schema.kernel_4.RelationType;
import org.datacite.schema.kernel_4.Resource;
import org.datacite.schema.kernel_4.Resource.Creators;
import org.datacite.schema.kernel_4.Resource.Creators.Creator;
import org.datacite.schema.kernel_4.Resource.Dates;
import org.datacite.schema.kernel_4.Resource.Dates.Date;
import org.datacite.schema.kernel_4.Resource.Descriptions;
import org.datacite.schema.kernel_4.Resource.Formats;
import org.datacite.schema.kernel_4.Resource.GeoLocations.GeoLocation.GeoLocationPolygon;
import org.datacite.schema.kernel_4.Resource.Identifier;
import org.datacite.schema.kernel_4.Resource.RelatedIdentifiers;
import org.datacite.schema.kernel_4.Resource.Titles;
import org.datacite.schema.kernel_4.Resource.Titles.Title;
import org.datacite.schema.kernel_4.ResourceType;
import org.datacite.schema.kernel_4.TitleType;
import org.xml.sax.SAXException;

/**
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class TestMetadata {

    public static void main(final String[] argv) throws JAXBException, MalformedURLException, URISyntaxException, SAXException {

        ObjectFactory objFactory = new ObjectFactory();
        Resource res = (Resource) objFactory.createResource();

        Resource.AlternateIdentifiers.AlternateIdentifier altId = new Resource.AlternateIdentifiers.AlternateIdentifier();
        altId.setAlternateIdentifierType("IVOA");
        altId.setValue("ivo://xxxxxx");
        Resource.AlternateIdentifiers altIds = new Resource.AlternateIdentifiers();
        altIds.getAlternateIdentifier().add(altId);
        res.setAlternateIdentifiers(altIds);

        Identifier identifier = new Identifier();
        identifier.setValue("10.001/3434.122");
        res.setIdentifier(identifier);

        res.setPublisher("Jean-Christophe Malapert");

        Creator creator = new Creator();
        creator.setCreatorName("Jean-Christophe Malapert");
        Creators creators = new Resource.Creators();
        creators.getCreator().add(creator);
        res.setCreators(creators);

        Resource.GeoLocations.GeoLocation geoLoc = new Resource.GeoLocations.GeoLocation();
        GeoLocationPolygon locationPolygon = new Resource.GeoLocations.GeoLocation.GeoLocationPolygon();
        Point p1 = new Point();
        p1.setPointLatitude(40.0f);
        p1.setPointLongitude(20f);
        Point p2 = new Point();
        p2.setPointLatitude(40.0f);
        p2.setPointLongitude(25f);
        Point p3 = new Point();
        p3.setPointLatitude(45.0f);
        p3.setPointLongitude(25f);
        Point p4 = new Point();
        p4.setPointLatitude(45.0f);
        p4.setPointLongitude(20f);
        locationPolygon.getPolygonPoint().add(p1);
        locationPolygon.getPolygonPoint().add(p2);
        locationPolygon.getPolygonPoint().add(p3);
        locationPolygon.getPolygonPoint().add(p4);
        locationPolygon.getPolygonPoint().add(p1);
        geoLoc.setGeoLocationPolygon(locationPolygon);
        Resource.GeoLocations geos = new Resource.GeoLocations();
        geos.getGeoLocation().add(geoLoc);
        res.setGeoLocations(geos);

        Resource.Descriptions.Description desc = new Resource.Descriptions.Description();
        desc.setDescriptionType(DescriptionType.ABSTRACT);
        desc.getContent().add("Voici mon abstract");
        Descriptions descrs = new Resource.Descriptions();
        descrs.getDescription().add(desc);
        res.setDescriptions(descrs);

        Date date1 = new Date();
        date1.setDateType(DateType.AVAILABLE);
        date1.setValue(new Date().getValue());
        Date date2 = new Date();
        date2.setDateType(DateType.CREATED);
        date2.setValue("2013");
        Dates dates = new Dates();
        dates.getDate().add(date1);
        dates.getDate().add(date2);
        res.setDates(dates);

        Formats formats = new Formats();
        formats.getFormat().add("image/fits");
        res.setFormats(formats);

        res.setLanguage("English");

        res.setPublicationYear("2017");
        
        //Resource.AlternateIdentifiers.AlternateIdentifier altId = new Resource.AlternateIdentifiers.AlternateIdentifier();
        //altId.

        Resource.RelatedIdentifiers.RelatedIdentifier rId = new Resource.RelatedIdentifiers.RelatedIdentifier();
        rId.setRelatedIdentifierType(RelatedIdentifierType.URL);
        rId.setRelationType(RelationType.IS_VARIANT_FORM_OF);
        rId.setValue("http://example.com/sdsdsd");
        RelatedIdentifiers relatedIds = new Resource.RelatedIdentifiers();
        relatedIds.getRelatedIdentifier().add(rId);
        res.setRelatedIdentifiers(relatedIds);

        Resource.ResourceType resourceType = new Resource.ResourceType();
        resourceType.setResourceTypeGeneral(ResourceType.IMAGE);
        resourceType.setValue("My image has been acquires ....");
        res.setResourceType(resourceType);

        Title title1 = new Resource.Titles.Title();
        title1.setLang("English");
        title1.setTitleType(TitleType.ALTERNATIVE_TITLE);
        title1.setValue("Alternate title");
        Titles titles = new Titles();
        titles.getTitle().add(title1);
        res.setTitles(titles);

        res.setVersion("1.0.0");

        OutputStream output = new OutputStream() {
            private StringBuilder string = new StringBuilder();

            @Override
            public void write(int b) throws IOException {
                this.string.append((char) b);
            }

            //Netbeans IDE automatically overrides this toString()
            @Override
            public String toString() {
                return this.string.toString();
            }
        };
        JAXBContext jaxbContext = JAXBContext.newInstance("org.datacite.schema.kernel_4");
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, new Boolean(true));
        Schema schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(new URL("https://schema.datacite.org/meta/kernel-4.0/metadata.xsd"));
        marshaller.setSchema(schema);
        MyValidationEventHandler eh = new MyValidationEventHandler(Logger.getAnonymousLogger());
        marshaller.setEventHandler(eh);
        marshaller.marshal(res, output);
        if(!eh.hasError) {
            System.out.println(output);
        }

    }

    private static class MyValidationEventHandler implements ValidationEventHandler {

        private final Logger logger;
        private boolean hasError = false;

        public MyValidationEventHandler(final Logger logger) {
            this.logger = logger;
        }

        @Override
        public boolean handleEvent(final ValidationEvent event) {
            StringBuilder sb = new StringBuilder("\nEVENT");
            sb = sb.append("SEVERITY:  ").append(event.getSeverity()).append("\n");
            sb = sb.append("MESSAGE:  ").append(event.getMessage()).append("\n");
            sb = sb.append("LINKED EXCEPTION:  ").append(event.getLinkedException()).append("\n");
            sb = sb.append("LOCATOR\n");
            sb = sb.append("    LINE NUMBER:  ").append(event.getLocator().getLineNumber()).append("\n");
            sb = sb.append("    COLUMN NUMBER:  ").append(event.getLocator().getColumnNumber()).append("\n");
            sb = sb.append("    OFFSET:  ").append(event.getLocator().getOffset()).append("\n");
            sb = sb.append("    OBJECT:  ").append(event.getLocator().getObject()).append("\n");
            sb = sb.append("    NODE:  ").append(event.getLocator().getNode()).append("\n");
            sb = sb.append("    URL  ").append(event.getLocator().getURL()).append("\n");
            this.logger.warning(sb.toString());
            this.hasError = true;
            return true;
        }

        public boolean hasError() {
            return this.hasError;
        }
    }

}
