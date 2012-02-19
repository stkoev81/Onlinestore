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
import java.util.logging.*;

//TODO: delete all the MarkerException stuff
//TODO: delete the UserFriendly exception stuff
/**
 * This stateless EJB is responsible for sending emails. These included status 
 * messages
 * to the customer about their orders and also alert messages to an 
 * administrator
 * that something in the system has been changed. These messages can be 
 * turned off by setting the flags in
 * {@link com.skoev.onlinestore.entities.initialize.SettingsEntity}.
 * <br/><br/>
 * If an exception occurs while trying to send the email, that exception is 
 * logged and an EmailException is thrown. This allows the calling methods
 * to present a simple message to the user about the failure without worrying
 * about the detailed cause.
 * 
 * <br/><br/>
 * This class uses resource injection to obtain a mail session with the 
 * jndi name "mail/gmailAccount". Therefore, this jndi resource must be 
 * configured on the server before using this class. 
 */
@Stateless
@LocalBean
public class MailSenderStateless {

    /**
     * Injected mail session
     */
    @Resource(name = "mail/gmailAccount")
    private Session mailSession;
    /**
     * Injected EntityManager
     */
    @PersistenceContext(unitName = "EJB_PU")
    private EntityManager em;
    /**
     * Logger used to log email failure
     */
    private static final Logger logger = Logger.getLogger(
            MailSenderStateless.class.getName());

    /**
     * Method for sending email. This method is called by {@link #checkAndSend},
     * {@link #sendActivationEmail}, {@link #sendAlert}, and
     * {@link #sendPasswordReminder} 
     * @param text Text of the email message
     * @param title Title of the email message
     * @param emailAddressTo Recipient's address
     * @throws EmailException If there is a problems while trying to send the
     * email.
     */
    public void sendEmail(String text, String title, String emailAddressTo)
            throws EmailException {

        MimeMessage message = new MimeMessage(mailSession);

        try {
            Address toAddress = new InternetAddress(emailAddressTo);
            message.setRecipient(Message.RecipientType.TO, toAddress);
            message.setSubject(title);
            message.setText(text);
            message.saveChanges();
            Transport tr = mailSession.getTransport();
            String serverPassword = mailSession.getProperty("mail.password");
            int serverPort = Integer.parseInt(mailSession.getProperty(
                    "mail.port"));

            //tr.connect(serverName,serverPort,userName,serverPassword);
            tr.connect(null, serverPort, null, serverPassword);
            tr.sendMessage(message, message.getAllRecipients());
            tr.close();
        } catch (Exception e) {
            logger.log(Level.SEVERE
                    , "exception caught while trying to send mail", e);
            throw new EmailException();
        }
    }

    /**
     * Sends a password reminder email to a user. The calling method should 
     * reset the password and pass it as an argument. If an email address is 
     * associated with more than one user account, the calling method should
     * reset the password for all of them and set it to the same value.
     * 
     * <br/><br/>
     * This method composes a message and then calls {@link #sendEmail} 
     * 
     * @param accounts The user accounts associated with this email address.
     * @param email The recipient's email address
     * @param password The password
     * @throws EmailException If there is a problem while trying to send the
     * email
     */
    public void sendPasswordReminder(List<UserEntity> accounts, String email
            , String password) throws EmailException {
        String title = "Password reset";
        String multipleAccounts = "";
        if (accounts.size() > 1) {
            multipleAccounts = " You have multiple accounts using this e-mail "
                    + "address.";
        }
        String usernames = "";
        for (UserEntity a : accounts) {
            usernames = usernames + ", " + a.getUsername();
        }
        usernames = usernames.substring(2);

        String text = "Your password for the Java EE Online Bookstore has been"
                + " reset." + multipleAccounts + "\n"
                + "Username(s): " + usernames + "\n"
                + "New password: " + password + "\n"
                + "You can now log in and change your password.";
        sendEmail(text, title, email);

    }

    /**
     * Optionally sends an email alert to an administrator. This method can be
     * be called when some change is made to the system. It looks up the the 
     * {@link com.skoev.onlinestore.entities.initialize.SettingsEntity} to 
     * determine if alerts are on and what it the address of the administrator. 
     * Then, it calls {@link #sendEmail}
     *     
     * @param text Text of the email message
     * @param title Title of the email message
     * @throws EmailException If there is a problem while trying to send the
     * email
     */
    public void sendAlert(String text, String title) throws EmailException {
        // retrieve alertsOn from dataBase
        // retrieve alert address from database

        SettingsEntity settings = em.find(SettingsEntity.class, 1);
        String alertAddress = settings.getAlertEmailAddress();
        boolean alertsOn = settings.isAlertsOn();

        if (alertsOn) {
            sendEmail(text, title, alertAddress);
        }
    }

    /**
     * Checks if an email is supposed to be sent, and if so, sends it. 
     * This method is called by {@link #sendActivationEmail}. It calls 
     * {@link #sendEmail}. 
     * 
     * @param text
     * @param title
     * @param emailAddress
     * @throws EmailException
     */
    private void checkAndSend(String text, String title, String emailAddress) 
            throws EmailException {
        //notification: send it if and only if Status notifications are on
        //regardless of the status of alerts.
        SettingsEntity settings = em.find(SettingsEntity.class, 1);
        boolean notificationsOn = settings.isNotificationsOn();
        if (notificationsOn && !"email@example.com".equals(emailAddress)) {
            sendEmail(text, title, emailAddress);
        }
    }

    /**
     * This method sends an email to a customer when an order has been updated.
     * It checks the newStatus of the order and determines what message, if any, 
     * should be sent. A message is sent only for certain statuses, and it is 
     * sent only if notifications are on in 
     * {@link com.skoev.onlinestore.entities.initialize.SettingsEntity} and 
     * the customer's address is different from "email@example.com" (this 
     * dummy address is used in the DEMO accounts).  
     * 
     * @param newStatus The status to which the order has been updated
     * @param order The order
     * @throws EmailException If there is a problem while trying to send the 
     * email. 
     */
    public void sendStatusEmail(OrderStatusEnum newStatus, OrderEntity order) 
            throws EmailException {

        String title = "Your order from the Java EE Online Bookstore";
        String emailAddress = order.getUi().getEmail();

        String text = "Order ID: " + order.getId() + "\n"
                + "Placed on: " + order.getOrderDate() + "\n\n";

        String customerName = order.getUi().getFirstName();
        if (customerName == null) {
            customerName = "Customer";
        }

        switch (newStatus) {
            case OUT_OF_STOCK:
                text = text + "Dear " + customerName + "\n"
                        + "Unfortunately your oder was not available for "
                        + "shipping. We "
                        + "aplogize for the inconvenience. You will receive a "
                        + "full refund.";
                break;

            case PAYMENT_FAILED:
                text = text + "Dear " + customerName + "\n"
                        + "Unfortunately there was a problem with the payment "
                        + "information"
                        + "you provided. Your payment could not be processed. "
                        + "Your order has"
                        + "been canceled";
                break;

            case SHIPPED:
                text = text + "Dear " + customerName + "\n"
                        + "Your order has shipped. You should receive it in a "
                        + "few days.";
                break;

            case ORDER_RECEIVED:
                text = text + "Dear " + customerName + "\n"
                        + "Your order has been received, and we will start"
                        + " processing it soon. "
                        + "We will let you know whent it ships.";
                sendAlert("Order ID: " + order.getId(), "Order placed");
                break;

            default:
                return;
        }

        checkAndSend(text, title, emailAddress);

    }

    /**
     * This method sends and activation email to a customer. It should be called
     * after a new account is created. The email contains an activation URL; 
     * once
     * the user visits that URL, the account is activated and the user can log 
     * in to it. This mechanism ensures that users can create accounts only if 
     * they provide a valid email address.    
     * <br/><br/>
     * This method composes an email message and then calls {@link #sendEmail}
     * 
     * @param user The user account that needs to be activated.
     * @param activationURL The URL that the user must visit to activate the 
     * account. 
     * @throws EmailException If there is a problem while trying to send the 
     * email
     */
    public void sendActivationEmail(UserEntity user, String activationURL)
            throws EmailException {
        String text = "Your account has bean created. Please click on this link"
                + "to activate the account: " + activationURL;
        String title = "Account Activation";
        sendEmail(text, title, user.getUi().getEmail());
    }
}
