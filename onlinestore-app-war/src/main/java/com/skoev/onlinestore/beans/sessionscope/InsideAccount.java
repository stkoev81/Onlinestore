package com.skoev.onlinestore.beans.sessionscope;

import java.util.*;
import javax.enterprise.context.SessionScoped;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.faces.context.FacesContext;
import javax.faces.application.FacesMessage;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import com.skoev.onlinestore.entities.user.*;
import com.skoev.onlinestore.ejb.EntityAccessorStateless;

/**
 * Contains methods relevant to pages inside the account, {@literal i.e.} 
 * once the user is logged in. This is a session scoped CDI managed bean. 
 */
@Named
@SessionScoped
public class InsideAccount implements Serializable {
    private String username;
    private List<String> groups = new LinkedList<String>();
    private UserEntity user;
    private boolean demo;
    @EJB
    private EntityAccessorStateless entityAccessor;

    public InsideAccount() {
    }

    /**
     * Retrieves from the database the groups of which this user is a member and
     * records them in the {@link #groups} field.  
     * Also, checks if this user account is in demo mode or not and sets the 
     * {@link #demo } flag. This method is called automatically by the container
     * right after this object is created. 
     */
    @PostConstruct
    public void initialize() {
        HttpServletRequest request = getRequest();
        demo = request.isUserInRole("DEMO");
        username = request.getUserPrincipal().getName();
        user = entityAccessor.findEntity(UserEntity.class, username);
        List<GroupEntity> geList = user.getGroupMemberships();
        for (GroupEntity ge : geList) {
            groups.add(ge.getGroupname());
        }
        groups.remove("DEMO");
    }

    /** 
     * Deletes a user account in the database; if this is an employee account or
     * a demo account, it is not eligible for deletion by and a message about
     * that is shown to the user.
     * 
     * @return null (in order to remain on the same JSF page)
     */
    public String deleteAccount() {
        FacesContext context = FacesContext.getCurrentInstance();
        List<String> reasons = new LinkedList<String>();
        if (isDemo()) {
            reasons.add("This is a demo account. ");
        }
        if (isEmployee()) {
            reasons.add("This is an employee account. ");
        }

        if (!reasons.isEmpty()) {
            String text = "Error! This account cannot be deleted for the "
                    + "following reason(s): ";
            for (String s : reasons) {

                text = text + s;
            }
            FacesMessage message = new FacesMessage(text);
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            context.addMessage(null, message);

        } else {
            entityAccessor.deleteUserAccout(user);
            HttpServletRequest request = getRequest();
            try {
                request.logout();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            request.getSession().invalidate();

            FacesMessage message = new FacesMessage("Account has been deleted."
                    + " You are now logged out.");
            message.setSeverity(FacesMessage.SEVERITY_INFO);
            context.addMessage(null, message);
        }
        return null;
    }

    /**
     * Returns the current HttpServletRequest
     * @return 
     */
    private HttpServletRequest getRequest() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context
                .getExternalContext().getRequest();
        return request;
    }

    /**
     * Checks if current account is an "employee" account. 
     * @return 
     */
    public boolean isEmployee() {
        return !groups.contains("CUSTOMER") || groups.size() > 1;
    }

    /**
     * Checks if current account is a "demo" account
     * @return 
     */
    public boolean isDemo() {
        return demo;
    }

    public List<String> getGroups() {
        return groups;
    }

    public String getUsername() {
        return username;
    }
}
