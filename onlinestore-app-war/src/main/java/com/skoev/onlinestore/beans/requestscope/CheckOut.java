package com.skoev.onlinestore.beans.requestscope;

import java.util.*;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.faces.event.*;
import com.skoev.onlinestore.beans.sessionscope.*;
import com.skoev.onlinestore.ejb.EmailException;

//TODO: fix the uploadFile IOException handling
/**
 * This class is used to check out from the online store after products have 
 * been added to cart by {@link CartBeanRequest}. It is a request scoped CDI 
 * managed bean. Internally, it obtains a  * 
 * {@link com.skoev.onlinestore.ejb.CartStateful} instance from {@link
 * com.skoev.onlinestore.beans.sessionscope.CartBeanSession} and calls the 
 * methods on that instance to perform the order placement operation. 
 * 
 * @see com.skoev.onlinestore.beans.sessionscope.CartBeanSession
 * @see com.skoev.onlinestore.ejb.CartStateful
 * @see CartBeanRequest
 */
@Named
@RequestScoped
public class CheckOut {
    private String cardMonth;
    private String cardYear;
    @Inject
    private RequestInfo requestInfo;
    @Inject
    private CartBeanSession cartBeanSession;

    /**
     * PostAddToView listener that gets called when the customer opens the 
     * CheckOut page. It initializes the order with the user information 
     * of the logged in user (if the user is logged in). As a result, the 
     * checkout form displayed to the user is populated with his information. 
     * @param event 
     */
    public void checkOutPageListener(ComponentSystemEvent event) {        
        cartBeanSession.getCartStateful().initOrder(requestInfo.getRequest()
                .getUserPrincipal());
        
        // Most of the form fields in the JSF page are wired directly to 
        // UserInfo properties. The only exceptions are cardMonth and cardDate;
        // they are represented by a Date object in UserInfo and have to be 
        // thranslated here. 
        Date date = cartBeanSession.getCartStateful().getUi()
                .getCardExpirationDate();
        Calendar cal = Calendar.getInstance();
        if (date != null) {
            cal.setTime(date);
            cardMonth = Integer.toString(cal.get(Calendar.MONTH) + 1);
            cardYear = Integer.toString(cal.get(Calendar.YEAR));
        }
    }

    /**
     * This method places an order in the online store by calling 
     * {@link com.skoev.onlinestore.ejb.CartStateful#placeOrder()}. 
     * 
     * @return the name of the "Email Error" page if there was an EmailException; 
     * the name of the "Order Confirmation" page otherwise.
     *
     */
    public String checkOutAction() {
        // As in checkOutPageListener(), the cardMonth and cardYear need to 
        // be translated to a Date object. 
        Calendar cardExp = Calendar.getInstance();
        cardExp.clear();
        cardExp.set(Calendar.YEAR, Integer.parseInt(cardYear));
        cardExp.set(Calendar.MONTH, Integer.parseInt(cardMonth) - 1);
        cardExp.set(Calendar.DATE, cardExp.getActualMaximum(Calendar.DATE));
        cartBeanSession.getCartStateful().getUi().setCardExpirationDate(
                cardExp.getTime());
        try {
            cartBeanSession.getCartStateful().placeOrder();
        } catch (EmailException ee) {
            return "/Errors/EmailError.xhtml?faces-redirect=true";
        }

        return "/BrowseStore/OrderConfirmation.xhtml?faces-redirect=true";
    }

    public String getCardMonth() {
        return cardMonth;
    }

    public String getCardYear() {
        return cardYear;
    }

    public void setCardMonth(String cardMonth) {
        this.cardMonth = cardMonth;
    }

    public void setCardYear(String cardYear) {
        this.cardYear = cardYear;
    }
}
