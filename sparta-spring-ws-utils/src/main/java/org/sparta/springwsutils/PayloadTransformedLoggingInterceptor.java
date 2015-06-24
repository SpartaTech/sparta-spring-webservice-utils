package org.sparta.springwsutils;

import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.ws.server.endpoint.interceptor.PayloadLoggingInterceptor;
import org.springframework.xml.transform.ResourceSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 *
 * PayloadMasked using xslt to transform fields
 *
 * @author danieldiehl
 *
 * History:
 *    Jun 22, 2015 - dxdiehl
 *
 */
public class PayloadTransformedLoggingInterceptor extends PayloadLoggingInterceptor implements InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(PayloadTransformedLoggingInterceptor.class); 
    
    private Resource xslt;
    private Templates templates;

    
    private Transformer createNonIndentingTransformer() throws TransformerConfigurationException {
        Transformer transformer = templates.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "no");
        return transformer;
    }
    
    /* (non-Javadoc)
     * @see org.springframework.ws.server.endpoint.AbstractLoggingInterceptor#logMessageSource(java.lang.String, javax.xml.transform.Source)
     */
    @Override
    protected void logMessageSource(String logMessage, Source source) throws TransformerException {
        if (source != null) {
            Transformer transformer = createNonIndentingTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(source, new StreamResult(writer));
            String message = logMessage + writer.toString();
            logMessage(message);
        }                
    }

    /* (non-Javadoc)
     * @see org.springframework.ws.server.endpoint.AbstractLoggingInterceptor#logMessage(java.lang.String)
     */
    @Override
    protected void logMessage(String message) {
        LOG.info(message);
    }
    
    
    /* (non-Javadoc)
     * @see org.springframework.ws.server.endpoint.AbstractLoggingInterceptor#isLogEnabled()
     */
    @Override
    protected boolean isLogEnabled() {
        return LOG.isInfoEnabled();
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        if (xslt == null) {
            throw new IllegalArgumentException("Setting 'xslt' is required");
        }
        
        TransformerFactory transformerFactory = getTransformerFactory();
        XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        xmlReader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
        if (xslt != null) {
            Assert.isTrue(xslt.exists(), "xslt \"" + xslt + "\" does not exit");
            if (logger.isInfoEnabled()) {
                logger.info("Transforming resquest/response using " + xslt);
            }
            Source source = new ResourceSource(xmlReader, xslt);
            templates = transformerFactory.newTemplates(source);
        }
    }

    /**
     * Setter method for xslt.
     *
     * @param xslt the xslt to set
     */
    public final void setXslt(Resource xslt) {
        this.xslt = xslt;
    }
}
