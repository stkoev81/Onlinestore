package com.skoev.onlinestore.beans.requestscope;

import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import com.skoev.onlinestore.web.*;
import java.util.*; 
import javax.inject.Inject;
import javax.annotation.PostConstruct; 

/**
 * This class backs two navigation menus in the online store: a product menu
 * and a sidebar menu. This is a request scoped CDI managed bean.  
 * <br/> <br/>
 * Menus can be defined in JSF pages as a simple sequence of static links 
 * without using any Java code. 
 * However, wiring the links to this object allows the menus to be dynamic. 
 * This means that a link can be rendered differently if it points to the 
 * current page. Also, some links can be hidden depending on the user role 
 * of the user accessing the page.  
 * 
 * @see com.skoev.onlinestore.web.MenuLink
 */
@Named(value = "menus")
@RequestScoped
public class Menus {
    /**
     * Product navigation menu
     */
    private List<MenuLink> productMenu = new LinkedList<MenuLink>(); 
    /**
     * Sidebar navigation menu
     */
    private List<MenuLink> sidebarMenu = new LinkedList<MenuLink>(); 
    
    @Inject
    private RequestInfo requestInfo; 
    @Inject
    private LogIn logIn; 
    
    public Menus() {   
         
    }
    
    /**
     * Populates the menu with {@link com.skoev.onlinestore.web.MenuLink} 
     * objects. This methods is called automatically by the container shortly
     * after the bean is constructed. After the call, the menus can be accessed
     * with {@link #getProductMenu()} and {@link #getSidebarMenu()}. 
     */
    @PostConstruct
    public void initialize(){
        String page = requestInfo.getRequest().getServletPath(); 
        productMenu.add(new MenuLink("Books", "/BrowseStore/BrowseBooks.xhtml"
                , page)); 
        productMenu.add(new MenuLink("CDs", "/BrowseStore/BrowseCDs.xhtml"
                , page));
        if(logIn.isLoggedIn()){
            sidebarMenu.add(new MenuLink("Log out","/LogIn/LogOut.xhtml",page));
            sidebarMenu.add(new MenuLink("Your actions"
                    ,"/InsideAccount/Home.xhtml",page));
            sidebarMenu.add(new MenuLink("Browse store"
                    ,"/BrowseStore/BrowseBooks.xhtml",page));             
            if (logIn.isInRole("CUSTOMER")){
                sidebarMenu.add(new MenuLink("View cart"
                        ,"/BrowseStore/ViewCart.xhtml",page));
                sidebarMenu.add(new MenuLink("Check out"
                        ,"/BrowseStore/CheckOut.xhtml",page));
            }
        }
        else {
            sidebarMenu.add(new MenuLink("Log in","/LogIn/LogIn.xhtml",page));
            sidebarMenu.add(new MenuLink("Password reminder"
                    ,"/LogIn/PasswordReminder.xhtml",page));
            sidebarMenu.add(new MenuLink("Create account"
                    ,"/AccountCreation/CreateAccount.xhtml",page));
            sidebarMenu.add(new MenuLink("Browse store"
                    ,"/BrowseStore/BrowseBooks.xhtml",page));
            sidebarMenu.add(new MenuLink("View cart"
                    ,"/BrowseStore/ViewCart.xhtml",page));
            sidebarMenu.add(new MenuLink("Check out"
                    ,"/BrowseStore/CheckOut.xhtml",page)); 
        }        
    }

    public List<MenuLink> getProductMenu() {
        return productMenu;
    }

    public List<MenuLink> getSidebarMenu() {
        return sidebarMenu;
    }
    
    
    
    
            
    
}
