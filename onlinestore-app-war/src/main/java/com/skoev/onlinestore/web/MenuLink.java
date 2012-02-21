package com.skoev.onlinestore.web;

/**
 * This class represents a single link in a menu. Collections of this type of 
 * objects are used to create navigation menus in the online store. 
 * 
 * @see com.skoev.onlinestore.beans.requestscope.Menus
 */
public class MenuLink {
    /**
     * Page name that will be displayed to the user
     */
    private String linkName;
    /**
     * Location to which this link points
     */
    private String linkTarget; 
    /**
     * Whether the page to which this link points is currently being viewed; 
     * If so, the link can be rendered differently to distinguish from other
     * links in the menu
     * 
     */
    private boolean currentLink;
    
    public MenuLink(String linkName, String linkTarget, boolean currentLink) {
        this.linkName = linkName;
        this.linkTarget = linkTarget;
        this.currentLink = currentLink;
    }
    public MenuLink(String linkName, String linkTarget, String pageName) {
        this.linkName = linkName;
        this.linkTarget = linkTarget;
        this.currentLink = pageName.equals(linkTarget);
    }

    public boolean isCurrentLink() {
        return currentLink;
    }

    public String getLinkName() {
        return linkName;
    }

    public String getLinkTarget() {
        return linkTarget;
    }
    
    
    
    
    
    
    
    
    
    
}
