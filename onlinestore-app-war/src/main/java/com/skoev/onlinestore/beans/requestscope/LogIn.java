/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.skoev.onlinestore.beans.requestscope;

import com.skoev.onlinestore.entities.user.*; 
import com.skoev.onlinestore.ejb.EntityAccessorStateless;
import javax.ejb.EJB;
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
import javax.inject.Inject;

/**
 * Contains methods related to logging in. 
 * 
 */
@Named
@RequestScoped
public class LogIn implements Serializable {
    @EJB
    private EntityAccessorStateless entityAccessor;
    @Inject
    private RequestInfo requestInfo; 
    

    
    public LogIn() {       
    }
    
    
    private String username; 
    private String passwd; 
    
    
    /**
     * Check if user is currently logged in.
     * @return 
     */
    public boolean isLoggedIn(){        
        if( requestInfo.getRequest().getUserPrincipal()!=null)
            return true; 
        else 
            return false;             
    }
    
    /**
     * Checks if the user has been authenticated for a certain role. 
     * @param role
     * @return 
     */
    public boolean isInRole(String role){
        return requestInfo.getRequest().isUserInRole(role);
    }
    
    
    /**
     * postAddComponent event listener for the login page; it the request is a
     * GET, then it sets a test cookie; if the request is POST and no cookie is 
     * found, forwards user to a warning page telling him that cookies are 
     * required.
     * @param event 
     */
    public void checkCookies(ComponentSystemEvent event){       
        //if initial request, set a cookie
        if (!requestInfo.getContext().isPostback() && requestInfo.getRequest()
                .getCookies() == null){
            Cookie cookie = new Cookie("cookiesEnabled","true"); 
            cookie.setPath(requestInfo.getContextPath()); 
            requestInfo.getResponse().addCookie(cookie);
            return; 
        }
        //if postback and cookies, redirect to page alerting the user
        else if (requestInfo.getRequest().getCookies() == null){
            try{
            requestInfo.getResponse().sendRedirect(requestInfo.getContextPath()
                   +"/Errors/CookiesDisabled.xhtml"); 
            requestInfo.getContext().responseComplete(); }
            catch (IOException e){
              throw new RuntimeException(e);
            }               
       }
                               
    }
    
    
    /**
     * preRenderView listener for the login page; if the user
     * tries to go to the login page but is already logged in, he is forwarded 
     * to the home page. 
     * 
     * @param event 
     */
    public void checkLoggedIn(ComponentSystemEvent event){
        //if user is already logged in, redirect to the home page
        if(requestInfo.getRequest().getUserPrincipal() != null){
             try{
               requestInfo.getResponse().sendRedirect(requestInfo.getContextPath()
                       +"/InsideAccount/Home.xhtml");
               requestInfo.getContext().responseComplete();
             }
              catch (IOException e){
                  throw new RuntimeException(e);
              }               
              
        }                       
    }

    
    
    
    /**
     * Perform programmatic login with the provided username and password; if 
     * successful, forward to home page; if unsuccesful, print a FacesMessage 
     * explaining why. 
     * @return 
     */
    public String logIn(){
        FacesMessage message = null; 
        UserEntity user = entityAccessor.findEntity(UserEntity.class,username); 
        if (user==null){
            message = new FacesMessage("Error! This username doesn't exist."); 
        }
        else if(!user.getActivated()){
            message = new FacesMessage("Error! This username has not been "
                    + "activated yet. To activate it, please follow the link "
                    + "that was e-mailed to you when creating your acccount.");
        }
        else if(user.getDisabled()){
            message = new FacesMessage("Error! This account has been disabled");
        }
        else {
            try {                     
                // throws ServletException if login is incorrect
                requestInfo.getRequest().login(username, passwd);   
                // if login is correct, redirect to the home page
                requestInfo.getResponse().sendRedirect(requestInfo.getContextPath()
                       +"/InsideAccount/Home.xhtml"); 
                requestInfo.getContext().responseComplete();
            }
            
            catch (ServletException e){
                message = new FacesMessage("Error! Incorrect password.");         
            }
            catch (IOException e){
                throw new RuntimeException(e);
            }
        }
        
        if (message!=null){
            message.setSeverity(FacesMessage.SEVERITY_ERROR);      
            requestInfo.getContext().addMessage(null,message);   
        }      
        return null; 
    }  
    
    /**
     * Perform programmatic log out and invalidate the session. 
     * @param event 
     */
    public void logOut(ComponentSystemEvent event){
        try{
            requestInfo.getRequest().logout();
        }
        catch (ServletException e){throw new RuntimeException();}
        requestInfo.getRequest().getSession().invalidate();               
   }
   
    public String getPasswd() {
        return passwd;
    }

    public String getUsername() {
        return username;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    
    
    
    
}


//
//   public void checkCookies(ComponentSystemEvent event){
//        FacesContext context = FacesContext.getCurrentInstance();
//        HttpServletResponse response = (HttpServletResponse)
//                context.getExternalContext().getResponse();        
//        HttpServletRequest request = (HttpServletRequest)context
//                .getExternalContext().getRequest();     
//              
//        if (!context.isPostback()){
//            Cookie cookie = new Cookie("cookiesEnabled","true"); 
//            cookie.setPath(request.getContextPath()); 
//            response.addCookie(cookie);
//            return; 
//        }
//                
//        else if (request.getCookies() == null){
//             try{
//               response.sendRedirect(request.getContextPath()
//                       +"/Errors/CookiesDisabled.xhtml"); }
//              catch (Exception e){
//                  throw new RuntimeException(e);
//              }               
//             context.responseComplete(); 
//             
//                        
//        }
//                               
//    }
//    
//   public void checkLoggedIn(ComponentSystemEvent event){
//        FacesContext context = FacesContext.getCurrentInstance();
//        HttpServletResponse response = (HttpServletResponse)
//                context.getExternalContext().getResponse();        
//        HttpServletRequest request = (HttpServletRequest)context
//                .getExternalContext().getRequest();     
//              
//    
//        if(request.getUserPrincipal() != null){
//             try{
//               response.sendRedirect(request.getContextPath()
//                       +"/InsideAccount/Home.xhtml"); }
//              catch (Exception e){
//                  throw new RuntimeException(e);
//              }               
//             context.responseComplete(); 
//            
//            
//        }                       
//    }
//    
//   
//    public String doLogin(){
//        String result = "/InsideAccount/Home"; 
//        HttpServletRequest request = (HttpServletRequest) FacesContext
//                .getCurrentInstance().getExternalContext().getRequest();
//                
//        try { 
//            request.login(username, passwd);             
//        }
//        catch (ServletException e){
//            result = "Failure"; 
//        }
//        
//        return result; 
//    }   