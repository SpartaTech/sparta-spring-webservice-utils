package org.sparta.springwsutils;

import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;

/**
 *
 * Inject Values in the MDC for the log to user it
 *
 * @author dxdiehl
 *
 * History:
 *    Jun 26, 2015 - dxdiehl
 *
 */
public class MDCInfoInjectInterceptor implements EndpointInterceptor {

    /* (non-Javadoc)
     * @see org.springframework.ws.server.EndpointInterceptor#handleRequest(org.springframework.ws.context.MessageContext, java.lang.Object)
     */
    @Override
    public boolean handleRequest(MessageContext messageContext, Object endpoint) throws Exception {
        MDC.put("UUID", UUID.randomUUID().toString());
        return true;
    }

    /* (non-Javadoc)
     * @see org.springframework.ws.server.EndpointInterceptor#handleResponse(org.springframework.ws.context.MessageContext, java.lang.Object)
     */
    @Override
    public boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception {
        return true;
    }

    /* (non-Javadoc)
     * @see org.springframework.ws.server.EndpointInterceptor#handleFault(org.springframework.ws.context.MessageContext, java.lang.Object)
     */
    @Override
    public boolean handleFault(MessageContext messageContext, Object endpoint) throws Exception {
        return true;
    }

    /* (non-Javadoc)
     * @see org.springframework.ws.server.EndpointInterceptor#afterCompletion(org.springframework.ws.context.MessageContext, java.lang.Object, java.lang.Exception)
     */
    @Override
    public void afterCompletion(MessageContext messageContext, Object endpoint, Exception ex) throws Exception {
        MDC.clear();
    }

}
