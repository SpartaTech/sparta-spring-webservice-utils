/*
 * Copyright (c) Bright House Networks. All Rights Reserved.
 * This software is the confidential and proprietary information of
 * Bright House Networks or its affiliates. You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Bright House Networks.
 */
package org.sparta.springwsutils;

import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.context.MessageContext;
import org.springframework.xml.transform.TransformerObjectSupport;

/**
 *
 * Logs payload for client webservice. Has a finer granularity than WebServiceTemplate (org.springframework.ws.client.MessageTracing).
 * Enables control for different clients defining different packages.
 * 
 *
 * @author dxdiehl
 *
 * History:
 *    Oct 8, 2015 - dxdiehl
 *
 */
public class PayloadLoggingClientInterceptor extends TransformerObjectSupport implements ClientInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(PayloadLoggingClientInterceptor.class);
    
    private static String LOG_TIME_PROPERTY = "LogTimingClientInterceptor-START";

    private transient Logger loggerRequest = LoggerFactory.getLogger(PayloadLoggingClientInterceptor.class.getCanonicalName() + ".request");
    private transient Logger loggerResponse = LoggerFactory.getLogger(PayloadLoggingClientInterceptor.class.getCanonicalName() + ".response");

    private boolean enableLogRequest = true;
    private boolean enableLogResponse = true;
    public boolean enableLogTiming = false;
    public boolean removeNewline = false;
    

    /**
     * Setter method for removeNewline.
     *
     * @param removeNewline the removeNewline to set
     */
    public void setRemoveNewline(boolean removeNewline) {
        this.removeNewline = removeNewline;
    }
    
    /**
     * Set this boolean to enable/disable log for Request
     *
     * @param enableLogRequest the enableLogRequest to set
     */
    public void setEnableLogRequest(boolean enableLogRequest) {
        this.enableLogRequest = enableLogRequest;
    }

    /**
     * Set this boolean to enable/disable log for Response
     *
     * @param enableLogResponse the enableLogResponse to set
     */
    public void setEnableLogResponse(boolean enableLogResponse) {
        this.enableLogResponse = enableLogResponse;
    }

    /**
     * Set the log name for Request calls
     * @param loggerName string representing log name
     */
    public void setRequestLoggerName(String loggerName) {
        this.loggerRequest = LoggerFactory.getLogger(loggerName);
    }
    
    /**
     * Set the log name for Response calls
     * @param loggerName string representing log name
     */
    public void setResponseLoggerName(String loggerName) {
        this.loggerResponse = LoggerFactory.getLogger(loggerName);
    }
    
    /**
     * Setter method for enableLogTiming.
     *
     * @param enableLogTiming the enableLogTiming to set
     */
    public void setEnableLogTiming(boolean enableLogTiming) {
        this.enableLogTiming = enableLogTiming;
    }


    @Override
    public boolean handleRequest(MessageContext messageContext) throws WebServiceClientException {
        if (enableLogTiming) {
            messageContext.setProperty(LOG_TIME_PROPERTY, System.currentTimeMillis());
        }
        if (enableLogRequest && loggerRequest.isDebugEnabled()) {
            try {
                loggerRequest.debug("Request: {}", getMessage(messageContext.getRequest()));
            } catch(Exception e) {
                LOG.error("Cannot Log Request", e);
            }
        }
        return true;
    }
    
    
    @Override
    public boolean handleResponse(MessageContext messageContext) throws WebServiceClientException {
        logResponse(messageContext);
        return true;
    }

    @Override
    public boolean handleFault(MessageContext messageContext) throws WebServiceClientException {
        logResponse(messageContext);
        return true;
    }

    @Override
    public void afterCompletion(MessageContext messageContext, Exception ex) throws WebServiceClientException {
        //For fault response it's called two times hence we cannot use it.
    }

    private void logResponse(MessageContext messageContext) {
        long totalTime = -1;
        if (enableLogTiming && loggerResponse.isDebugEnabled()) {
            try { 
                long start = (long) messageContext.getProperty(LOG_TIME_PROPERTY);
                messageContext.removeProperty(LOG_TIME_PROPERTY);
                totalTime = System.currentTimeMillis()-start;
                
                if (!enableLogResponse) {
                    loggerResponse.debug("Execution timing: {}ms ", totalTime);
                }
            } catch (Exception e) {
                logger.error("Cannot log timing because start time was not in messageContext.", e);
            }
        }
        
        if (enableLogResponse && loggerResponse.isDebugEnabled()) {
            try {
                if (enableLogTiming) {
                    loggerResponse.debug("Response({}ms): {}", totalTime, getMessage(messageContext.getResponse()));
                } else {
                    loggerResponse.debug("Response: {}", getMessage(messageContext.getResponse()));
                }
            } catch(Exception e) {
                LOG.error("Cannot Log Response", e);
            }
        }
    }
    
    
    /**
     * Create a transformer to get the text message from a Source
     * 
     * @return new transformer
     * @throws TransformerConfigurationException
     */
    private Transformer createNonIndentingTransformer() throws TransformerConfigurationException {
        Transformer transformer = createTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "no");
        return transformer;
    }

   
    /**
     * Retrieves the XML message from the WebServiceMessage obj using the transformer,
     * returns null in case source is null
     * 
     * @param source to retrieve the SOAP message
     * @return String XML formatted
     * @throws Exception
     */
    private String getMessage(WebServiceMessage message) throws Exception {
        Source source = message.getPayloadSource();
        if (source != null) {
            Transformer transformer = createNonIndentingTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(source, new StreamResult(writer));
            
            //Retrieve msg and remove break line if requested
            String msg = writer.toString();  
            if (removeNewline) {
                msg = StringUtils.remove(msg, System.getProperty("line.separator"));
            }
            
            //Return processed message
            return msg;
        }
        return null;
    }
}
