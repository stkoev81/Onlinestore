/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.skoev.onlinestore.web;
/**
 * This class represents a hyperlink in a pager component (which is used
 * to scroll through large list of items); every link in the pager points to the same page
 *  but it has a different query string, thus part of the list is displayed
 * when clicking each link. 
 * 
 */
public  class PagerLink{
    /**
     * Page name that will be displayed to the user
     */
    private String linkName;   
    /**
     * Query string parameter for this link
     */
    private String linkParam;
    /**
     * Whether the data corresponding to this link is currently being viewed; if
     * so, the link will be rendered differently to distinguish it from the other
     * links in the pager. 
     */
    private boolean currentLink;     
        
        public PagerLink(String linkParam, String linkName, boolean currentLink){
            this.linkParam = linkParam; 
            this.linkName = linkName; 
            this.currentLink = currentLink; 
        }
        public PagerLink(Integer linkParam, String linkName, boolean currentLink){
            this.linkParam = linkParam.toString(); 
            this.linkName = linkName; 
            this.currentLink = currentLink; 
        }
        
        public PagerLink(Integer linkParam, Integer linkName, boolean currentLink){
            this.linkParam = linkParam.toString(); 
            this.linkName = linkName.toString(); 
            this.currentLink = currentLink; 
        }

    public boolean isCurrentLink() {
        return currentLink;
    }

    public String getLinkName() {
        return linkName;
    }

    public String getLinkParam() {
        return linkParam;
    }          
  
    }
    