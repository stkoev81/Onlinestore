/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.skoev.onlinestore.beans.requestscope;



import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse; 
import javax.servlet.ServletException; 
import javax.servlet.http.Cookie; 
import javax.faces.event.*; 
import java.io.Serializable; 
import java.io.IOException;
import javax.faces.application.FacesMessage;
import java.security.Principal; 

/**
 * Contains methods that provide information about the current request. 
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
