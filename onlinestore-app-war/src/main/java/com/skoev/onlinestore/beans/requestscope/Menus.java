/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.skoev.onlinestore.beans.requestscope;


import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import com.skoev.onlinestore.web.*;
import java.util.*; 
import javax.inject.Inject;
import javax.annotation.PostConstruct; 

/**
 *
 * 
 * @see com.skoev.onlinestore.web.MenuLink
 */
@Named(value = "menus")
@RequestScoped
public class Menus {
    private List<MenuLink> productMenu = new LinkedList<MenuLink>(); 
    private List<MenuLink> sidebarMenu = new LinkedList<MenuLink>(); 
    
    @Inject
    private RequestInfo requestInfo; 
    @Inject
    private LogIn logIn; 
    

    /** Creates a new instance of Menus */
    public Menus() {   
         
    }
    @PostConstruct
    public void initialize(){
        String page = requestInfo.getRequest().getServletPath(); 
        productMenu.add(new MenuLink("Books", "/BrowseStore/BrowseBooks.xhtml", page)); 
        productMenu.add(new MenuLink("CDs", "/BrowseStore/BrowseCDs.xhtml", page));
        
        if(logIn.isLoggedIn()){
            sidebarMenu.add(new MenuLink("Log out","/LogIn/LogOut.xhtml",page));
            sidebarMenu.add(new MenuLink("Your actions","/InsideAccount/Home.xhtml",page));
               sidebarMenu.add(new MenuLink("Browse store","/BrowseStore/BrowseBooks.xhtml",page));             
             if (logIn.isInRole("CUSTOMER")){
                  sidebarMenu.add(new MenuLink("View cart","/BrowseStore/ViewCart.xhtml",page));
                 sidebarMenu.add(new MenuLink("Check out","/BrowseStore/CheckOut.xhtml",page));
             }
        }
        else {
            sidebarMenu.add(new MenuLink("Log in","/LogIn/LogIn.xhtml",page));
            sidebarMenu.add(new MenuLink("Password reminder","/LogIn/PasswordReminder.xhtml",page));
            sidebarMenu.add(new MenuLink("Create account","/AccountCreation/CreateAccount.xhtml",page));
             sidebarMenu.add(new MenuLink("Browse store","/BrowseStore/BrowseBooks.xhtml",page));
                  sidebarMenu.add(new MenuLink("View cart","/BrowseStore/ViewCart.xhtml",page));
                 sidebarMenu.add(new MenuLink("Check out","/BrowseStore/CheckOut.xhtml",page)); 
        }        
        
    }

    public List<MenuLink> getProductMenu() {
        return productMenu;
    }

    public List<MenuLink> getSidebarMenu() {
        return sidebarMenu;
    }
    
    
    
    
            
    
}
