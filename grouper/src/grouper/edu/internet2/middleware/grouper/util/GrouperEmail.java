/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/*
 * @author mchyzer
 * $Id: GrouperEmail.java,v 1.1 2008-11-08 03:42:33 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.util;

import java.util.ArrayList;
import java.util.List;
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

  /**
   * keep list emails (max 100) if testing...
   */
  private static List<GrouperEmail> testingEmails = new ArrayList();
  
  /**
   * 
   * @return the list of emails
   */
  public static List<GrouperEmail> testingEmails() {
    return testingEmails;
  }
  
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
   * who this email is going to (comma separated)
   * @return to
   */
  public String getTo() {
    return this.to;
  }

  /**
   * subject of email
   * @return subject
   */
  public String getSubject() {
    return this.subject;
  }

  /**
   * email address this is from
   * @return from
   */
  public String getFrom() {
    return this.from;
  }

  /**
   * body of email (currently HTML is not supported, only plain text)
   * @return body
   */
  public String getBody() {
    return this.body;
  }

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
      String smtpServer = GrouperConfig.getProperty("mail.smtp.server");
      if (StringUtils.isBlank(smtpServer)) {
        throw new RuntimeException("You need to specify the from smtp server mail.smtp.server in grouper.properties");
      }
      
      String theFrom = StringUtils.defaultIfEmpty(this.from, GrouperConfig.getProperty("mail.from.address"));
      if (!StringUtils.equals("testing", smtpServer) && StringUtils.isBlank(theFrom)) {
        throw new RuntimeException("You need to specify the from email address mail.from.address in grouper.properties");
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
      
      boolean useSsl = GrouperConfig.getPropertyBoolean("mail.smtp.ssl", false);
      if (useSsl) {
        
        properties.put("mail.smtp.starttls.enable","true");
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.socketFactory.fallback", "false");
        
      }

      if (LOG.isDebugEnabled()) {
        properties.put("mail.smtp.debug", "true");
      }
      
      //leave blank for default (probably 25), if ssl is true, default is 465, else specify
      String port = GrouperConfig.getProperty("mail.smtp.port");
      if (!StringUtils.isBlank(port)) {
        properties.put("mail.smtp.socketFactory.port", port);
      } else {
        if (useSsl) {
          properties.put("mail.smtp.socketFactory.port", "465");
        }
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
      
      if (!StringUtils.isBlank(theFrom)) {
        message.addFrom(new InternetAddress[] { new InternetAddress(theFrom) });
      }
      
      String theSubject = StringUtils.defaultString(subjectPrefix) + this.subject;
      message.setSubject(theSubject);
      
      //GRP-912: mail body is badly quoted-printable encoded => accents issues
      String emailContentType = GrouperConfig.getProperty("grouperEmailContentType");
      emailContentType = StringUtils.isBlank(emailContentType) ? "text/plain; charset=utf-8" : emailContentType;
      message.setContent(this.body, emailContentType);
      
      testingEmailCount++;
      
      //if you dont have a server, but want to test, then set this
      if (!StringUtils.equals("testing", smtpServer)) {
        Transport.send(message);
      } else {
        LOG.error("Not sending email since smtp server is 'testing'. \nTO: " + this.to + "\nFROM: " + theFrom + "\nSUBJECT: " + theSubject + "\nBODY: " + this.body + "\n");
        synchronized (GrouperEmail.class) {
          
          testingEmails.add(this);
          while (testingEmails.size() > 100) {
            testingEmails.remove(0);
          }
          
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
}
