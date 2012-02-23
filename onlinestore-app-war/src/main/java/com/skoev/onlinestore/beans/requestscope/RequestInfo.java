package com.skoev.onlinestore.beans.requestscope;

import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse; 

/**
 * Contains methods that provide information about the current request. This is 
 * a request scoped CDI managed bean. 
 */
@Named
@RequestScoped
public class RequestInfo {
    private FacesContext context = FacesContext.getCurrentInstance(); 
    private HttpServletResponse response = (HttpServletResponse)
                context.getExternalContext().getResponse(); 
    private HttpServletRequest request = (HttpServletRequest)context
                .getExternalContext().getRequest(); 
    private String contextPath = request.getContextPath();

    public FacesContext getContext() {
        return context;
    }

    public String getContextPath() {
        return contextPath;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }
    
    
    
    
    
    
    
    
}
