package com.skoev.onlinestore.beans.requestscope;

import javax.ejb.EJB;
import javax.inject.Named;
import javax.inject.Inject;
import javax.enterprise.context.RequestScoped;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.faces.event.*;
import javax.faces.application.FacesMessage;
import java.io.Serializable;
import java.io.IOException;
import com.skoev.onlinestore.entities.user.*;
import com.skoev.onlinestore.ejb.EntityAccessorStateless;

/**
 * Contains methods related to logging in and out of a user account. It is a 
 * request scoped CDI managed bean. 
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
    /**
     * Username used by {@link #logIn()} 
     */
    private String username;
    /**
     * Password used by {@link #logIn}
     */
    private String passwd;

    /**
     * Check if anyone is currently logged in.
     * @return 
     */
    public boolean isLoggedIn() {
        if (requestInfo.getRequest().getUserPrincipal() != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks if the present user has been authenticated for a certain role. 
     * @param role
     * @return 
     */
    public boolean isInRole(String role) {
        return requestInfo.getRequest().isUserInRole(role);
    }

    /**
     * postAddComponent event listener for the login page used to check if 
     * cookies are enabled. If the request is a
     * GET, then it sets a test cookie; if the request is POST and no cookie is 
     * found, forwards user to a warning page telling him that cookies are 
     * required.
     * @param event 
     */
    public void checkCookies(ComponentSystemEvent event) {
        //if initial request, set a cookie
        if (!requestInfo.getContext().isPostback() && 
                requestInfo.getRequest().getCookies() == null) {
            Cookie cookie = new Cookie("cookiesEnabled", "true");
            cookie.setPath(requestInfo.getContextPath());
            requestInfo.getResponse().addCookie(cookie);
            return;
        } //if postback and cookies, redirect to page alerting the user
        else if (requestInfo.getRequest().getCookies() == null) {
            try {
                requestInfo.getResponse().sendRedirect(requestInfo
                            .getContextPath() + "/Errors/CookiesDisabled.xhtml");
                        
                requestInfo.getContext().responseComplete();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * preRenderView listener for the login page that check if user is
     * already logged in. If the user
     * tries to go to the login page but is already logged in, he is forwarded 
     * to the home page. 
     * 
     * @param event 
     */
    public void checkLoggedIn(ComponentSystemEvent event) {
        //if user is already logged in, redirect to the home page
        if (requestInfo.getRequest().getUserPrincipal() != null) {
            try {
                requestInfo.getResponse().sendRedirect(requestInfo
                        .getContextPath() + "/InsideAccount/Home.xhtml");
                requestInfo.getContext().responseComplete();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Perform programmatic login with the provided username and password. If 
     * successful, redirects to home page; if unsuccesful, prints a FacesMessage 
     * explaining why. 
     * @return null (to stay on the same page if not redirected)
     */
    public String logIn() {
        FacesMessage message = null;
        UserEntity user = entityAccessor.findEntity(UserEntity.class, username);
        if (user == null) {
            message = new FacesMessage("Error! This username doesn't exist.");
        } else if (!user.getActivated()) {
            message = new FacesMessage("Error! This username has not been "
                    + "activated yet. To activate it, please follow the link "
                    + "that was e-mailed to you when creating your acccount.");
        } else if (user.getDisabled()) {
            message = new FacesMessage("Error! This account has been disabled");
        } else {
            try {
                // throws ServletException if login is incorrect
                requestInfo.getRequest().login(username, passwd);
                // if login is correct, redirect to the home page
                requestInfo.getResponse().sendRedirect(
                     requestInfo.getContextPath()+ "/InsideAccount/Home.xhtml");
                requestInfo.getContext().responseComplete();
            } catch (ServletException e) {
                message = new FacesMessage("Error! Incorrect password.");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (message != null) {
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            requestInfo.getContext().addMessage(null, message);
        }
        return null;
    }

    /**
     * Performs programmatic log out and invalidates the session. 
     * @param event 
     */
    public void logOut(ComponentSystemEvent event) {
        try {
            requestInfo.getRequest().logout();
        } catch (ServletException e) {
            throw new RuntimeException();
        }
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
