/*
 * @author mchyzer
 * $Id: GrouperEmail.java,v 1.1 2008-11-08 03:42:33 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.util;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.morphString.Morph;


/**
 * use this chaining utility to send email
 */
public class GrouperEmail {

  /** keep count for testing */
  public static long testingEmailCount = 0;
  
  /** who this email is going to (comma separated) */
  private String to;
  
  /** subject of email */
  private String subject;
  
  /** email address this is from */
  private String from;
  
  /** body of email (currently HTML is not supported, only plain text) */
  private String body;
  
  /**
   * 
   */
  public GrouperEmail() {
    //empty 
  }
  
  /**
   * set the to address
   * @param theToAddress 
   * @return this for chaining
   */
  public GrouperEmail setTo(String theToAddress) {
    this.to = theToAddress;
    return this;
  }
  
  /**
   * set subject
   * @param theSubject
   * @return this for chaining
   */
  public GrouperEmail setSubject(String theSubject) {
    this.subject = theSubject;
    return this;
  }
  
  /**
   * set the body
   * @param theBody
   * @return this for chaining
   */
  public GrouperEmail setBody(String theBody) {
    this.body = theBody;
    return this;
  }
 
  /**
   * set the from address
   * @param theFrom
   * @return the from address
   */
  public GrouperEmail setFrom(String theFrom) {
    this.from = theFrom;
    return this;
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperEmail.class);

  
  /**
   * try an email
   * @param args
   */
  public static void main(String[] args) {
    new GrouperEmail().setBody("hey").setSubject("subject").setTo("a@b.c,d@e.f").send();
  }
  
  /**
   * send the email
   */
  public void send() {
    
    try {
      //mail.smtp.server = whatever.school.edu
      //#mail.from.address = noreply@school.edu
      String theFrom = StringUtils.defaultIfEmpty(this.from, GrouperConfig.getProperty("mail.from.address"));
      if (StringUtils.isBlank(theFrom)) {
        throw new RuntimeException("You need to specify the from email address mail.from.address in grouper.properties");
      }
      
      String smtpServer = GrouperConfig.getProperty("mail.smtp.server");
      if (StringUtils.isBlank(smtpServer)) {
        throw new RuntimeException("You need to specify the from smtp server mail.smtp.server in grouper.properties");
      }
      
      String subjectPrefix = StringUtils.defaultString(GrouperConfig.getProperty("mail.subject.prefix"));
      
      final String SMTP_USER = GrouperConfig.getProperty("mail.smtp.user"); 
      
      String smtpPass = GrouperConfig.getProperty("mail.smtp.pass"); 
      
      final String SMTP_PASS = StringUtils.isBlank(smtpPass) ? null : Morph.decryptIfFile(smtpPass);
      
      Properties properties = new Properties();
      
      properties.put("mail.host", smtpServer);
      properties.put("mail.transport.protocol", "smtp");
      
      Authenticator authenticator = null;
  
      //this has never been tested... :)
      if (!StringUtils.isBlank(SMTP_USER)) {
        properties.setProperty("mail.smtp.submitter", SMTP_USER);
        properties.setProperty("mail.smtp.auth", "true");
        
        authenticator = new Authenticator() {
          protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(SMTP_USER, SMTP_PASS);
          }
        };
      }
      
      Session session = Session.getInstance(properties, authenticator);
      Message message = new MimeMessage(session);
      
      String theTo = this.to;
      
      boolean hasRecipient = false;
      
      if (!StringUtils.isBlank(theTo)) {
        
        theTo = StringUtils.replace(theTo, ";", ",");
        String[] theTos = GrouperUtil.splitTrim(theTo, ",");
        for (String aTo : theTos) {
          if (!StringUtils.isBlank(aTo) && !StringUtils.equals("null", aTo)) {
            hasRecipient = true;
            message.addRecipient(RecipientType.TO, new InternetAddress(aTo));
          }
        }
        
      }
      
      if (!hasRecipient) {
        LOG.debug("Cant find recipient for email");
        return;
      }
      
      message.addFrom(new InternetAddress[] { new InternetAddress(theFrom) });
  
      String theSubject = StringUtils.defaultString(subjectPrefix) + this.subject;
      message.setSubject(theSubject);
      
      message.setContent(this.body, "text/plain");
      
      testingEmailCount++;
      
      //if you dont have a server, but want to test, then set this
      if (!StringUtils.equals("testing", smtpServer)) {
        Transport.send(message);
      } else {
        LOG.error("Not sending email since smtp server is 'testing'");
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
}
