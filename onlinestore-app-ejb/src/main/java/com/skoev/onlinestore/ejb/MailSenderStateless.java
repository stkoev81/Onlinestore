/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.skoev.onlinestore.ejb;

import javax.ejb.Stateless;
import javax.ejb.LocalBean;
import java.util.*; 
import com.skoev.onlinestore.entities.order.*; 
import com.skoev.onlinestore.entities.user.*;
import com.skoev.onlinestore.entities.initialize.*; 

import javax.mail.*; 
import javax.annotation.Resource; 
import javax.mail.internet.MimeMessage;
import javax.mail.internet.InternetAddress;
import javax.persistence.*; 
/**
 *
 * @author stephan
 */
@Stateless
@LocalBean
public class MailSenderStateless {
@Resource(name = "mail/gmailAccount") 
private Session mailSession;

@PersistenceContext(unitName = "EJB_PU") 
private EntityManager em; 

    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")
    
   /*
     * prepare a message 
     * send message
     */
    
     public void sendEmail(String text,String title, String emailAddressTo){
         
      MimeMessage message = new MimeMessage(mailSession);
      
        try{
        Address toAddress = new InternetAddress(emailAddressTo); 
        message.setRecipient(Message.RecipientType.TO, toAddress);
                
        message.setSubject(title);
        message.setText(text); 
        message.saveChanges();   

        Transport tr = mailSession.getTransport();
        
        String serverPassword = mailSession.getProperty("mail.password"); 
        int serverPort = Integer.parseInt( mailSession.getProperty("mail.port")); 
        
        //tr.connect(serverName,serverPort,userName,serverPassword);
        tr.connect(null,serverPort,null,serverPassword);
        tr.sendMessage(message, message.getAllRecipients()); 
        tr.close(); 
        }
        catch (Exception e) {
            throw new RuntimeException(); 
        }
     }
    
    
     public void sendPasswordReminder(List<UserEntity> accounts, String email
             , String password){
         String title = "Password reset"; 
         String multipleAccounts="";
         if (accounts.size()>1){
             multipleAccounts =" You have multiple accounts using this e-mail "
                     + "address.";
         }
         String usernames = ""; 
         for (UserEntity a:accounts){             
             usernames = usernames + ", " + a.getUsername();
         }
         usernames = usernames.substring(2);     
         
         String text = "Your password for the Java EE Online Bookstore has been"
                 + " reset." + multipleAccounts + "\n"                 
                 +"Username(s): " + usernames + "\n"
                 +"New password: "+ password + "\n"
                 +"You can now log in and change your password.";
         sendEmail(text, title, email); 
         
     }
     
     
     // maybe get message templates from the database
    // crate a message entity which has transient properties: customer, order
    // information. It also has a display mehtod which returns a string once the 
    // transient properties have been set. 
    // maybe there could be messages for evey status update or only for selected
    // status updates.
     public void sendAlert(String text, String title){
         // retrieve alertsOn from dataBase
         // retrieve alert address from database
         // alerts are sent when: a new order is submitted; when a product is changed.         
         
         SettingsEntity settings = em.find(SettingsEntity.class, 1);
         String alertAddress = settings.getAlertEmailAddress(); 
         boolean alertsOn = settings.isAlertsOn(); 
                  
         if (alertsOn)
             sendEmail(text,title, alertAddress); 
    
     
     }
     private void checkAndSend(String text, String title, String emailAddress){                 
         //notification: send it if and only if Status notifications are on, regardless of the status of alerts.
         SettingsEntity settings = em.find(SettingsEntity.class, 1); 
         boolean notificationsOn = settings.isNotificationsOn(); 
         if(notificationsOn && !"email@example.com".equals(emailAddress))
             sendEmail(text, title, emailAddress); 
         
     }
        
     
     
     public void sendStatusEmail(OrderStatusEnum newStatus, OrderEntity order) {
         
        
        String title = "Your order from the Java EE Online Bookstore"; 
        String emailAddress = order.getUi().getEmail();
        
        String text = "Order ID: " + order.getId() + "\n"
                      + "Placed on: " + order.getOrderDate() + "\n\n";  
        
        String customerName = order.getUi().getFirstName(); 
        if (customerName==null){
            customerName="Customer"; 
        }

        switch(newStatus)  {
          case OUT_OF_STOCK: 
             text = text + "Dear " + customerName +  "\n"
              + "Unfortunately your oder was not available for shipping. We "
              + "aplogize for the inconvenience. You will receive a full refund.";
              break;

          case PAYMENT_FAILED:
              text = text + "Dear " + customerName +  "\n"
              + "Unfortunately there was a problem with the payment information"
              + "you provided. Your payment could not be processed. Your order has"
              + "been canceled";
              break;
              
          case SHIPPED: 
              text = text + "Dear " + customerName +  "\n"
              + "Your order has shipped. You should receive it in a few days.";
              break;
              
          case ORDER_RECEIVED: 
              text = text + "Dear " + customerName +  "\n"
              + "Your order has been received, and we will start processing it soon. "
                      + "We will let you know whent it ships.";            
              sendAlert("Order ID: " + order.getId(), "Order placed");              
              break;
              
          default: return;               
        }  

        checkAndSend(text, title, emailAddress);
         
        
        
    }
     
    public void sendActivationEmail(UserEntity user, String activationURL){
        String text = "Your account has bean created. Please click on this link"
                + "to activate the account: " + activationURL; 
        String title = "Account Activation"; 
        sendEmail(text,title,user.getUi().getEmail()); 
            
        }
    
  
    
}
