/**
 * Copyright 2014 Internet2
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
 */
/*
 * @author mchyzer
 * $Id: GrouperEmail.java,v 1.1 2008-11-08 03:42:33 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperConfigHibernate;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.morphString.Morph;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SubjectTypeEnum;


/**
 * <p>Use this utility to send email from Grouper.  Many of these methods are new as of v2.5.47+.  The original "set" methods have been there since v1.4+</p>
 * <p>Configured from the smtp external system: 
 * <a href="https://spaces.at.internet2.edu/display/Grouper/Grouper+smtp+external+system">https://spaces.at.internet2.edu/display/Grouper/Grouper+smtp+external+system</a></p>
 * <p>Unlike most other method chaining classes, you need to call assignRunAsRoot(true) before adding subject and group lookups if you dont want to check security</p>
 * <p>Sample call to send an email:
 * <blockquote>
 * <pre>new GrouperEmail().setTo("email@domain.com").setBody("email body").setSubject("email subject").send();</pre>
 * </blockquote>
 * </p>
 * <p>Send an email to a subject:
 * <blockquote>
 * <pre>new GrouperEmail().assignRunAsRoot(true).addSubjectIdentifierToSendTo("mySourceId", "someNetId").setBody("email body").setSubject("email subject").send();</pre>
 * </blockquote>
 * </p>
 * <p>Sample call to send an email:
 * <blockquote>
 * <pre>new GrouperEmail().assignRunAsRoot(true).addGroupToSendTo("a:b:c").setBody("email body").setSubject("email subject").send();</pre>
 * </blockquote>
 * </p>
 * <p>You need to configure email address in your person subject source to send to subjects</p>
 * <p>At least one "to" address is required.</p>
 * <p>To debug emails, set debug to true in the smtp external system, and set the log4j.properties entry:
 * <blockquote>
 * <pre>log4j.logger.edu.internet2.middleware.grouper.util.GrouperEmail = DEBUG</pre>
 * </blockquote>
 * </p>
 */
public class GrouperEmail {

  /**
   * add an email allowed to the list of allowed emails in config
   * @param address
   * @return true if added, false if already there
   * @since v2.5.48
   */
  public static boolean addAllowEmailToGroup(String address) {
    GrouperUtil.assertion(!StringUtils.isBlank(address), "Email address is required!");
    GrouperUtil.assertion(groupDereferencePattern.matcher(address).matches(), "Email address must match pattern: groupName@grouper or groupUuid@grouper");

    String emailAddressString = GrouperConfig.retrieveConfig().propertyValueString("mail.smtp.groupUuidAndNameEmailDereferenceAllow");
    Set<String> emailAddressSet = StringUtils.isBlank(emailAddressString) ? new LinkedHashSet<String>() : GrouperUtil.splitTrimToSet(emailAddressString, ",");
    if (emailAddressSet.contains(address)) {
      return false;
    }
    emailAddressSet.add(address);
    emailAddressString = GrouperUtil.join(emailAddressSet.iterator(), ",");

    Set<GrouperConfigHibernate> grouperConfigHibernates = GrouperDAOFactory.getFactory().getConfig().findAll(ConfigFileName.GROUPER_PROPERTIES, null, "mail.smtp.groupUuidAndNameEmailDereferenceAllow");
    if (GrouperUtil.length(grouperConfigHibernates) == 0) {
      GrouperConfigHibernate grouperConfigHibernate = new GrouperConfigHibernate();
      grouperConfigHibernate.setConfigEncrypted(false);
      grouperConfigHibernate.setConfigFileHierarchyDb("INSTITUTION");
      grouperConfigHibernate.setConfigFileNameDb(ConfigFileName.GROUPER_PROPERTIES.getConfigFileName());
      grouperConfigHibernate.setConfigKey("mail.smtp.groupUuidAndNameEmailDereferenceAllow");
      
      grouperConfigHibernate.setValueToSave(emailAddressString);
      grouperConfigHibernate.saveOrUpdate(true);
    } else {
      GrouperUtil.assertion(GrouperUtil.length(grouperConfigHibernates) == 1, "Why is there more than one entry for mail.smtp.groupUuidAndNameEmailDereferenceAllow?");
      GrouperConfigHibernate grouperConfigHibernate = grouperConfigHibernates.iterator().next();
      grouperConfigHibernate.setValueToSave(emailAddressString);
      grouperConfigHibernate.saveOrUpdate(false);
    }
    return true;
    
  }
  
  /**
   * remove an allowed email address from the list of allwoed emails in config
   * @param address
   * @return true if removed, false if not there
   * @since v2.5.48
   */
  public static boolean removeAllowEmailToGroup(String address) {
    GrouperUtil.assertion(!StringUtils.isBlank(address), "Email address is required!");
    GrouperUtil.assertion(groupDereferencePattern.matcher(address).matches(), "Email address must match pattern: groupName@grouper or groupUuid@grouper");
    
    String emailAddressString = GrouperConfig.retrieveConfig().propertyValueString("mail.smtp.groupUuidAndNameEmailDereferenceAllow");
    Set<String> emailAddressSet = StringUtils.isBlank(emailAddressString) ? new LinkedHashSet<String>() : GrouperUtil.splitTrimToSet(emailAddressString, ",");
    if (!emailAddressSet.contains(address)) {
      return false;
    }
    emailAddressSet.remove(address);
    emailAddressString = GrouperUtil.join(emailAddressSet.iterator(), ",");

    Set<GrouperConfigHibernate> grouperConfigHibernates = GrouperDAOFactory.getFactory().getConfig().findAll(ConfigFileName.GROUPER_PROPERTIES, null, "mail.smtp.groupUuidAndNameEmailDereferenceAllow");
    
    // dont remove since might be in an upstream config file?
    if (GrouperUtil.length(grouperConfigHibernates) == 0) {
      GrouperConfigHibernate grouperConfigHibernate = new GrouperConfigHibernate();
      grouperConfigHibernate.setConfigEncrypted(false);
      grouperConfigHibernate.setConfigFileHierarchyDb("INSTITUTION");
      grouperConfigHibernate.setConfigFileNameDb(ConfigFileName.GROUPER_PROPERTIES.getConfigFileName());
      grouperConfigHibernate.setConfigKey("mail.smtp.groupUuidAndNameEmailDereferenceAllow");
      
      grouperConfigHibernate.setValueToSave(emailAddressString);
      grouperConfigHibernate.saveOrUpdate(true);
    } else {
      GrouperUtil.assertion(GrouperUtil.length(grouperConfigHibernates) == 1, "Why is there more than one entry for mail.smtp.groupUuidAndNameEmailDereferenceAllow?");
      
      GrouperConfigHibernate grouperConfigHibernate = grouperConfigHibernates.iterator().next();
      grouperConfigHibernate.setValueToSave(emailAddressString);
      grouperConfigHibernate.saveOrUpdate(false);
    }

    return true;
  }
  
  /**
   * add a subject (person) to send email to.  The email will not send without "to" address(es)
   * @param subject
   * @return this for chaining
   * @since 2.5.47
   */
  public GrouperEmail addSubjectToSendTo(Subject subject) {
    
    if (subject == null) {
      return this;
    }
    
    String emailAddress = retrieveEmailAddress(subject);
    
    return this.addEmailAddressToSendTo(emailAddress);
  }

  /**
   * find a subject by sourceId and subjectId, if not found, do nothing.  If found, find email address.
   * If not found, do nothing.  If found, add to the "to" address list.  The email will not send without "to" address(es)
   * @param sourceId
   * @param subjectId
   * @return this for chaining
   * @since 2.5.47
   */
  public GrouperEmail addSubjectIdentifierToSendTo(String sourceId, String subjectIdentifier) {
    
    Subject subject = (Subject)GrouperSession.internal_callbackRootGrouperSession(this.runAsRoot, new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        return SubjectFinder.findByIdentifierAndSource(subjectIdentifier, sourceId, false);
      }
    });
    
    return addSubjectToSendTo(subject);
  }

  /**
   * find a subject by sourceId and subjectId, if not found, do nothing.  If found, find email address.
   * If not found, do nothing.  If found, add to the "to" address list.  The email will not send without "to" address(es)
   * @param sourceId
   * @param subjectId
   * @return this for chaining
   * @since 2.5.47
   */
  public GrouperEmail addSubjectIdToSendTo(String sourceId, String subjectId) {
    
    Subject subject = (Subject)GrouperSession.internal_callbackRootGrouperSession(this.runAsRoot, new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        return SubjectFinder.findByIdAndSource(subjectId, sourceId, false);
      }
    }); 
    
    return addSubjectToSendTo(subject);
  }
  
  /**
   * get an email address for a subject or null if email not found
   * @param subject to send email to
   * @return email address
   */
  public static String retrieveEmailAddress(Subject subject) {
    if (subject != null) {
      if (!StringUtils.equals(subject.getType().getName(), SubjectTypeEnum.GROUP.getName())) { 
        String emailAttributeName = GrouperEmailUtils.emailAttributeNameForSource(subject.getSourceId());
        if (!StringUtils.isBlank(emailAttributeName)) {
          String emailAddress = subject.getAttributeValue(emailAttributeName);
          if (!StringUtils.isBlank(emailAddress)) {
            return emailAddress;
          }
        }
      }
    }
    return null;
  }

  /**
   * secure? method that retrieves email addresses from a group
   * @param group name
   * @param secure false to run as root
   * @param exceptionIfNotFound exception if group not found
   * @return the email addresses found for users in the group
   */
  public static Set<String> retrieveEmailAddresses(final String groupName, boolean secure, boolean exceptionIfNotFound) {
    
    Group group = (Group)GrouperSession.internal_callbackRootGrouperSession(!secure, new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        return GroupFinder.findByName(grouperSession, groupName, exceptionIfNotFound);
      }
    });
    
    return retrieveEmailAddresses(group, secure);
    
  }

  /**
   * secure? method that retrieves email addresses from a group
   * @param group
   * @return the email addresses found for users in the group
   */
  public static Set<String> retrieveEmailAddresses(final Group group, boolean secure) {
    
    final Set<String> emailAddresses = new TreeSet<String>();
    if (group != null) {
      
      if (secure && !group.canHavePrivilege(GrouperSession.staticGrouperSession().getSubject(), "readers", false)) {
        throw new RuntimeException("User '" + SubjectHelper.getPretty(GrouperSession.staticGrouperSession().getSubject()) + "' cannot read: " + group.getName());
      }
      
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

          for (Member member : GrouperUtil.nonNull(group.getMembers())) {
            
            Subject subject = member.getSubject();
            String emailAddress = GrouperEmail.retrieveEmailAddress(subject);
            if (!StringUtils.isBlank(emailAddress)) {
              emailAddresses.add(emailAddress);
            }
            
          }
          return null;
        }
      });
      
    }
    return emailAddresses;
  }
  
  
  /**
   * keep list emails (max 100) if testing...
   */
  private static List<GrouperEmail> testingEmails = new ArrayList<GrouperEmail>();
  
  /**
   * 
   * @return the list of emails
   */
  public static List<GrouperEmail> testingEmails() {
    return testingEmails;
  }
  
  /** keep count for testing */
  public static long testingEmailCount = 0;
  
  /** 
   * set the to addresses (comma separated or semicolon separated)
   */
  private String to;

  /** optional Cc addresses */
  private String cc;

  /** optional Bcc addresses */
  private String bcc;

  /** optional Reply-To addresses */
  private String replyTo;

  /** subject of email */
  private String subject;
  
  /** email address this is from */
  private String from;
  
  /** body of email (HTML if the email starts with open HTML bracket &lt;) */
  private String body;
  
  /**
   * set the to addresses (comma separated or semicolon separated)
   * @return to
   */
  public String getTo() {
    return this.to;
  }

  /**
   * optional comma-separated or semicolon separated list of Cc addresses to send to
   * @return
   */
  public String getCc() {
    return this.cc;
  }

  /**
   * optional comma-separated or semicolon separated list of Bcc (blind carbon copy) addresses to send to for all emails
   * @return
   */
  public String getBcc() {
    return this.bcc;
  }

  /**
   * optional comma-separated list of addresses for Reply-To header
   * @return
   */  public String getReplyTo() {
    return replyTo;
  }

  /**
   * subject of email
   * @return subject
   */
  public String getSubject() {
    return this.subject;
  }

  /**
   * set the from address.  generally this will not be set and will come from the config default
   * @return from
   */
  public String getFrom() {
    return this.from;
  }

  /**
   * body of email (HTML if the email starts with open HTML bracket &lt;)
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
   * set the to addresses (comma separated or semicolon separated).  The email will not send without "to" address(es)
   * @param theToAddress 
   * @return this for chaining
   */
  public GrouperEmail setTo(String theToAddress) {
    this.to = theToAddress;
    return this;
  }

  /**
   * optional comma-separated or semicolon separated list of Cc addresses to send to
   * @param theCc
   */
  public GrouperEmail setCc(String theCc) {
    this.cc = theCc;
    return this;
  }

  /**
   * optional comma-separated or semicolon separated list of Bcc (blind carbon copy) addresses to send to for all emails
   * @param theBcc
   */
  public GrouperEmail setBcc(String theBcc) {
    this.bcc = theBcc;
    return this;
  }

  /**
   * optional comma-separated list of addresses for Reply-To header
   * @param theReplyTo
   */
  public GrouperEmail setReplyTo(String theReplyTo) {
    this.replyTo = theReplyTo;
    return this;
  }

  /**
   * set email subject
   * @param theSubject
   * @return this for chaining
   */
  public GrouperEmail setSubject(String theSubject) {
    this.subject = theSubject;
    return this;
  }
  
  /**
   * body of email (HTML if the email starts with open HTML bracket &lt;)
   * @param theBody
   * @return this for chaining
   */
  public GrouperEmail setBody(String theBody) {
    this.body = theBody;
    return this;
  }
 
  /**
   * set the from address.  generally this will not be set and will come from the config default
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
    new GrouperEmail().setBody("hey there").setSubject("my subject").setTo("mchyzer@isc.upenn.edu").send();
  }
  
  /**
   * email content type
   */
  private String emailContentType;
  
  /**
   * email content type
   * @return email content type
   * @since 2.5.47
   */
  String getEmailContentType() {
    return emailContentType;
  }

  private boolean mailSent = false;
  
  /**
   * if mail was sent
   * @return true
   */
  public boolean isMailSent() {
    return mailSent;
  }

  /**
   * send the email
   */
  public void send() {
    this.mailSent = false;
    try {
      //mail.smtp.server = whatever.school.edu
      //#mail.from.address = noreply@school.edu
      String smtpServer = GrouperConfig.retrieveConfig().propertyValueString("mail.smtp.server");
      if (StringUtils.isBlank(smtpServer)) {
        throw new RuntimeException("You need to specify the from smtp server mail.smtp.server in grouper.properties");
      }
      
      String theFrom = StringUtils.defaultIfEmpty(this.from, GrouperConfig.retrieveConfig().propertyValueString("mail.smtp.from.address"));
      theFrom = StringUtils.defaultIfEmpty(theFrom, GrouperConfig.retrieveConfig().propertyValueString("mail.from.address"));
      if (!StringUtils.equals("testing", smtpServer) && StringUtils.isBlank(theFrom)) {
        throw new RuntimeException("You need to specify the from email address mail.smtp.from.address in grouper.properties");
      }
      
      String subjectPrefix = StringUtils.defaultString(GrouperConfig.retrieveConfig().propertyValueString("mail.smtp.subject.prefix"));
      if (StringUtils.isBlank(subjectPrefix)) {
        subjectPrefix = StringUtils.defaultString(GrouperConfig.retrieveConfig().propertyValueString("mail.subject.prefix"));
      }
      
      Properties properties = new Properties();
      
      properties.put("mail.host", smtpServer);
      
      String mailTransportProtocol = GrouperConfig.retrieveConfig().propertyValueString("mail.smtp.transport.protocol");
      if (StringUtils.isBlank(mailTransportProtocol)) {
        mailTransportProtocol = GrouperConfig.retrieveConfig().propertyValueString("mail.transport.protocol");
      }
      if (StringUtils.isBlank(mailTransportProtocol)) {
        mailTransportProtocol = "smtp";
      }
      properties.put("mail.transport.protocol", mailTransportProtocol);

      boolean mailUseProtocolInPropertyNames = true;
      {
        String mailUseProtocolInPropertyNamesString = GrouperConfig.retrieveConfig().propertyValueString("mail.smtp.use.protocol.in.property.names");
        if (StringUtils.isBlank(mailUseProtocolInPropertyNamesString)) {
          mailUseProtocolInPropertyNamesString = GrouperConfig.retrieveConfig().propertyValueString("mail.use.protocol.in.property.names");
        }
        if (!StringUtils.isBlank(mailUseProtocolInPropertyNamesString)) {
          mailUseProtocolInPropertyNames = GrouperUtil.booleanValue(mailUseProtocolInPropertyNamesString);
        }
      }
      String propertyProtocol = mailUseProtocolInPropertyNames ? mailTransportProtocol : "smtp";
      Authenticator authenticator = null;
      
      {
        final String SMTP_USER = GrouperConfig.retrieveConfig().propertyValueString("mail.smtp.user"); 
        
        String smtpPass = GrouperConfig.retrieveConfig().propertyValueString("mail.smtp.pass"); 
        
        final String SMTP_PASS = StringUtils.isBlank(smtpPass) ? null : Morph.decryptIfFile(smtpPass);
        
        if (!StringUtils.isBlank(SMTP_USER)) {
          properties.setProperty("mail." + propertyProtocol + ".submitter", SMTP_USER);
          properties.setProperty("mail." + propertyProtocol + ".auth", "true");
          
          authenticator = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
              return new PasswordAuthentication(SMTP_USER, SMTP_PASS);
            }
          };
        }
      }
      
      boolean useSsl = GrouperConfig.retrieveConfig().propertyValueBoolean("mail.smtp.ssl", false);
      if (useSsl) {
        properties.put("mail." + propertyProtocol + ".socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail." + propertyProtocol + ".socketFactory.fallback", "false");
      }
        
      {
        String mailSmtpSslProtocols = GrouperConfig.retrieveConfig().propertyValueString("mail.smtp.ssl.protocols");
        if (!StringUtils.isBlank(mailSmtpSslProtocols)) {
          properties.put("mail." + propertyProtocol + ".ssl.protocols", mailSmtpSslProtocols);
        }
      }
      
      {
        String mailSmtpSocketFactoryClass = GrouperConfig.retrieveConfig().propertyValueString("mail.smtp.socketFactory.class");
        if (!StringUtils.isBlank(mailSmtpSocketFactoryClass)) {
          properties.put("mail." + propertyProtocol + ".socketFactory.class", mailSmtpSocketFactoryClass);
        }
      }
      
      {
        Boolean mailSmtpSocketFactoryFallback = GrouperConfig.retrieveConfig().propertyValueBoolean("mail.smtp.socketFactory.fallback");
        if (mailSmtpSocketFactoryFallback != null) {
          properties.put("mail." + propertyProtocol + ".socketFactory.fallback", mailSmtpSocketFactoryFallback ? "true" : "false");
        }
      }
      
      {
        Boolean mailSmtpStartTlsEnable = GrouperConfig.retrieveConfig().propertyValueBoolean("mail.smtp.starttls.enable");
        if (mailSmtpStartTlsEnable != null) {
          properties.put("mail." + propertyProtocol + ".starttls.enable", mailSmtpStartTlsEnable ? "true" : "false");
        }
      }
      
      {
        String mailSmtpSslTrust = GrouperConfig.retrieveConfig().propertyValueString("mail.smtp.ssl.trust");
        if (!StringUtils.isBlank(mailSmtpSslTrust)) {
          properties.put("mail." + propertyProtocol + ".ssl.trust", mailSmtpSslTrust);
        }
      }

      // setting both mail.smtp.ssl and mail.smtp.starttls.enable probably isn't what the user wants;
      // the ssl will override, as seen in the java client debug message "STARTTLS requested but already using SSL"
      if (GrouperConfig.retrieveConfig().propertyValueBoolean("mail.smtp.ssl", false)
        && GrouperConfig.retrieveConfig().propertyValueBoolean("mail.smtp.starttls.enable", false)) {
        LOG.warn("Grouper properties mail.smtp.ssl and mail.smtp.starttls.enable are both true; the starttls option will likely not work since the ssl session is established first");
      }
      boolean mailSmtpDebug = false;
      {
        String mailSmtpDebugString = GrouperConfig.retrieveConfig().propertyValueString("mail.smtp.debug");
        if (StringUtils.isBlank(mailSmtpDebugString)) {
          mailSmtpDebugString = GrouperConfig.retrieveConfig().propertyValueString("mail.debug");
        }
        if (!StringUtils.isBlank(mailSmtpDebugString)) {
          mailSmtpDebug = GrouperUtil.booleanValue(mailSmtpDebugString);
        }
      }
      
      if (mailSmtpDebug || LOG.isDebugEnabled()) {
        properties.put("mail." + propertyProtocol + ".debug", "true");
        properties.put("mail.debug", "true");
      }
      
      //leave blank for default (probably 25), if ssl is true, default is 465, else specify
      {
        Integer port = GrouperConfig.retrieveConfig().propertyValueInt("mail.smtp.port");
        if (port != null) {
          properties.put("mail." + propertyProtocol + ".socketFactory.port", port);
          properties.put("mail." + propertyProtocol + ".port", port);
        } else {
          if (useSsl) {
            properties.put("mail." + propertyProtocol + ".socketFactory.port", "465");
            properties.put("mail." + propertyProtocol + ".port", "465");
          }
        }
      }
      
      Session session = Session.getInstance(properties, authenticator);
      Message message = new MimeMessage(session);
      
      String overrideAddresses = GrouperConfig.retrieveConfig().propertyValueString("mail.smtp.sendAllMessagesHere");
      if (StringUtils.isBlank(overrideAddresses)) {
        overrideAddresses = GrouperConfig.retrieveConfig().propertyValueString("mail.sendAllMessagesHere");
      }
      boolean sendAllMessagesHereOverride = !StringUtils.equals("testing", smtpServer) 
          && !StringUtils.isBlank(overrideAddresses);
      StringBuilder sendAllMessagesHereMessage = new StringBuilder();
      
      String theTo = this.to;
      
      boolean hasRecipient = false;
      
      //GRP-912: mail body is badly quoted-printable encoded => accents issues
      this.emailContentType = GrouperConfig.retrieveConfig().propertyValueString("mail.smtp.grouperEmailContentType");
      if (StringUtils.isBlank(this.emailContentType)) {
        this.emailContentType = GrouperConfig.retrieveConfig().propertyValueString("grouperEmailContentType");
      }
      if (StringUtils.isBlank(this.emailContentType)) {
        this.emailContentType = "text/plain; charset=utf-8";
      }
      String bodyNewline = "\n";
      if (StringUtils.trimToEmpty(this.body).startsWith("<") && !this.emailContentType.toLowerCase().contains("html")) {
        this.emailContentType = "text/html; charset=utf-8";
      }
      if (StringUtils.trimToEmpty(this.body).startsWith("<") || this.emailContentType.toLowerCase().contains("html")) {
        bodyNewline = "<br />";
      }

      if (!StringUtils.isBlank(theTo)) {
        theTo = StringUtils.replace(theTo, ";", ",");
        String[] theTos = GrouperUtil.splitTrim(theTo, ",");
        
        theTos = dereferenceGroups(theTos);
        this.to = GrouperUtil.join(theTos, ",");
        for (String aTo : theTos) {
          if (!StringUtils.isBlank(aTo) && !StringUtils.equals("null", aTo)) {

            if (sendAllMessagesHereOverride) {
              if (hasRecipient) {
                sendAllMessagesHereMessage.append(", ");
              } else {
                sendAllMessagesHereMessage.append("TO: ");
              }
              sendAllMessagesHereMessage.append(aTo);
            } else {
              message.addRecipient(RecipientType.TO, new InternetAddress(aTo));
            }
            hasRecipient = true;
          }
        }
        if (sendAllMessagesHereOverride) {
          sendAllMessagesHereMessage.append("" + bodyNewline + "");
          // refactor so the email goes here
          overrideAddresses = StringUtils.replace(overrideAddresses, ";", ",");
          List<InternetAddress> overrideAddressesList = new ArrayList<>();
          for (String address : GrouperUtil.splitTrim(overrideAddresses, ",")) {
            if (!StringUtils.isBlank(address)) {
              overrideAddressesList.add(new InternetAddress(address));
            }
          }
          message.setRecipients(RecipientType.TO, GrouperUtil.toArray(overrideAddressesList, InternetAddress.class));
        }
      }

      if (!hasRecipient) {
        LOG.error("Cant find recipient for email");
        return;
      }

      // add CC addresses if any
      if (!StringUtils.isBlank(this.cc)) {
        String theCc = StringUtils.replace(this.cc, ";", ",");

        boolean foundCc = false;
        String[] theCcs = GrouperUtil.splitTrim(theCc, ",");
        
        theCcs = dereferenceGroups(theCcs);
        this.cc = GrouperUtil.join(theCcs, ",");

        for (String address : theCcs) {
          if (!StringUtils.isBlank(address)) {

            if (sendAllMessagesHereOverride) {
              if (foundCc) {
                sendAllMessagesHereMessage.append(", ");
              } else {
                sendAllMessagesHereMessage.append("CC: ");
              }
              sendAllMessagesHereMessage.append(address);
              foundCc = true;
              
            } else {
              message.addRecipient(RecipientType.CC, new InternetAddress(address));
            }
          }
        }
        if (foundCc && sendAllMessagesHereOverride) {
          sendAllMessagesHereMessage.append("" + bodyNewline + "");
        }
      }

      // add BCC addresses if any
      if (!StringUtils.isBlank(this.bcc)) {
        String theBcc = StringUtils.replace(this.bcc, ";", ",");
        boolean foundBcc = false;
        String[] theBccs = GrouperUtil.splitTrim(theBcc, ",");
        theBccs = dereferenceGroups(theBccs);
        this.bcc = GrouperUtil.join(theBccs, ",");

        for (String address : theBccs) {
          if (!StringUtils.isBlank(address)) {
            if (sendAllMessagesHereOverride) {
              if (foundBcc) {
                sendAllMessagesHereMessage.append(", ");
              } else {
                sendAllMessagesHereMessage.append("BCC: ");
              }
              sendAllMessagesHereMessage.append(address);
              foundBcc = true;
              
            } else {
              message.addRecipient(RecipientType.BCC, new InternetAddress(address));
            }
          }
        }
        if (foundBcc && sendAllMessagesHereOverride) {
          sendAllMessagesHereMessage.append("" + bodyNewline + "");
        }
      }

      // add Reply-To addresses if any
      if (!StringUtils.isBlank(this.replyTo)) {
        String theReplyTo = StringUtils.replace(this.replyTo, ";", ",");
        List<InternetAddress> replyToList = new ArrayList<>();
        for (String address : GrouperUtil.splitTrim(theReplyTo, ",")) {
          if (!StringUtils.isBlank(address)) {
            replyToList.add(new InternetAddress(address));
          }
        }

        if (replyToList.size() > 0) {
          message.setReplyTo(GrouperUtil.toArray(replyToList, InternetAddress.class));
        }
      }

      if (!StringUtils.isBlank(theFrom)) {
        message.addFrom(new InternetAddress[] { new InternetAddress(theFrom) });
      }
      
      String theSubject = StringUtils.defaultString(subjectPrefix) + this.subject;
      message.setSubject(theSubject);
      
      if (sendAllMessagesHereOverride) {
        sendAllMessagesHereMessage.append("BODY:" + bodyNewline + bodyNewline).append(this.body);
        message.setContent(sendAllMessagesHereMessage.toString(), emailContentType);
      } else {
        message.setContent(this.body, emailContentType);
      }
      testingEmailCount++;
      
      //if you dont have a server, but want to test, then set this
      if (!StringUtils.equals("testing", smtpServer)) {
        if (GrouperConfig.retrieveConfig().propertyValueBoolean("mail.smtp.enabled", true)) {
          Transport.send(message);
          this.mailSent = true;
        } else {
          LOG.debug("Not sending mail since grouper.properties: mail.smtp.enabled = false");
        }
      } else {
        LOG.error("Not sending email since smtp server is 'testing'. "+bodyNewline+"TO: " + this.to + "" + bodyNewline + "FROM: " + theFrom + "" + bodyNewline + "SUBJECT: " + theSubject + "" + bodyNewline + "BODY: " + this.body + "" + bodyNewline + "");
        synchronized (GrouperEmail.class) {
          
          testingEmails.add(this);
          while (testingEmails.size() > 100) {
            testingEmails.remove(0);
          }
          
        }
      }
    } catch (RuntimeException e) {
      throw (RuntimeException)e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * groupUuid@grouper or groupName@grouper
   */
  private static Pattern groupDereferencePattern = Pattern.compile("^([^@]+)@grouper$");
  
  /**
   * 
   * @param theEmails
   * @return the new list of dereferences emails
   */
  private String[] dereferenceGroups(String[] theEmails) {

    //  # comma separated group name and uuid's to be allow email addresses to dereference.
    //  # for instance: a:b:c@grouper, def345@grouper<br />
    //  # If a configuration enters in one of those email addresses, and it is in this allow list, then
    //  # dereference the group and member and send email to their individual email addresses.  Note that 
    //  # groups in this list can have their members discovered so treat their membership as non private.
    //  # using the uuid@grouper gives a little bit of obscurity since the uuid of the group needs to be known
    //  # is it is still security by obscurity which is not true security.  There is a max of 100 members it will
    //  # send to
    //  # {valueType: "string", multiple: true}
    //  mail.smtp.groupUuidAndNameEmailDereferenceAllow =

    String groupUuidAndNameEmailDereferenceAllowString = GrouperConfig.retrieveConfig().propertyValueString("mail.smtp.groupUuidAndNameEmailDereferenceAllow");
    if (GrouperUtil.length(theEmails) == 0) {
      return theEmails;
    }
    
    Set<String> groupUuidAndNameEmailDereferenceAllowSet = null;
    
    List<String> theEmailsListResult = new ArrayList<String>();
    
    for (String theEmail : theEmails) {
      
      Matcher matcher = groupDereferencePattern.matcher(theEmail);
      
      if (matcher.matches()) {
        
        String groupIdOrName = matcher.group(1);
        
        if (groupUuidAndNameEmailDereferenceAllowSet == null) {
          groupUuidAndNameEmailDereferenceAllowSet = GrouperUtil.nonNull(GrouperUtil.splitTrimToSet(groupUuidAndNameEmailDereferenceAllowString, ","));
        }
        if (!groupUuidAndNameEmailDereferenceAllowSet.contains(theEmail) && !groupUuidAndNameEmailDereferenceAllowSet.contains(groupIdOrName)) {
          throw new RuntimeException("grouper.properties mail.smtp.groupUuidAndNameEmailDereferenceAllow does not contain '" + theEmail + "'");
        }
        
        Set<String> emails = null;
        if (groupIdOrName.contains(":")) {
          emails = new TreeSet<String>(retrieveEmailAddresses(groupIdOrName, false, true));
        } else {
          emails = new TreeSet<String>(retrieveEmailAddressesByGroupUuid(groupIdOrName, false, true));
        }
        if (emails.size() > 100) {
          throw new RuntimeException("Cannot send email to '" + theEmail + "' since size (" + emails.size() + ") is more than 100");
        }
        theEmailsListResult.addAll(emails);
      } else {
        theEmailsListResult.add(theEmail);
      }
      
    }
    return GrouperUtil.toArray(theEmailsListResult, String.class);
  }

  /**
   * find a subject by sourceId and subjectId, if not found, do nothing.  If found, find email address.
   * If not found, do nothing.  If found, add to the "cc" address list
   * @param sourceId
   * @param subjectId
   * @return this for chaining
   * @since 2.5.47
   */
  public GrouperEmail addSubjectIdentifierToCc(String sourceId, String subjectIdentifier) {
    
    Subject subject = (Subject)GrouperSession.internal_callbackRootGrouperSession(this.runAsRoot, new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        return SubjectFinder.findByIdentifierAndSource(subjectIdentifier, sourceId, false);
      }
    }); 
    
    return addSubjectToCc(subject);
  }

  /**
   * find a subject by sourceId and subjectId, if not found, do nothing.  If found, find email address.
   * If not found, do nothing.  If found, add to the "cc" address list
   * @param sourceId
   * @param subjectId
   * @return this for chaining
   * @since 2.5.47
   */
  public GrouperEmail addSubjectIdToCc(String sourceId, String subjectId) {
    
    Subject subject = (Subject)GrouperSession.internal_callbackRootGrouperSession(this.runAsRoot, new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        return SubjectFinder.findByIdAndSource(subjectId, sourceId, false);
      }
    });
    
    return addSubjectToCc(subject);
  }

  /**
   * add subject (e.g. person) to cc
   * @param subject
   * @return this for chaining
   * @since 2.5.47
   */
  public GrouperEmail addSubjectToCc(Subject subject) {
    
    if (subject == null) {
      return this;
    }
    
    String emailAddress = retrieveEmailAddress(subject);
    
    return this.addEmailAddressToCc(emailAddress);
  }

  /**
   * find a subject by sourceId and subjectId, if not found, do nothing.  If found, find email address.
   * If not found, do nothing.  If found, add to the "bcc" address list
   * @param sourceId
   * @param subjectId
   * @return this for chaining
   * @since 2.5.47
   */
  public GrouperEmail addSubjectIdentifierToBcc(String sourceId, String subjectIdentifier) {
    
    Subject subject = (Subject)GrouperSession.internal_callbackRootGrouperSession(this.runAsRoot, new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        return SubjectFinder.findByIdentifierAndSource(subjectIdentifier, sourceId, false);
      }
    });
    
    return addSubjectToBcc(subject);
  }

  /**
   * find a subject by sourceId and subjectId, if not found, do nothing.  If found, find email address.
   * If not found, do nothing.  If found, add to the "bcc" address list
   * @param sourceId
   * @param subjectId
   * @return this for chaining
   * @since 2.5.47
   */
  public GrouperEmail addSubjectIdToBcc(String sourceId, String subjectId) {
    
    Subject subject = (Subject)GrouperSession.internal_callbackRootGrouperSession(this.runAsRoot, new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        return SubjectFinder.findByIdAndSource(subjectId, sourceId, false);
      }
    });
    
    return addSubjectToBcc(subject);
  }

  /**
   * add a group of people to send to.  The email will not send without "to" address(es)
   * @param subject
   * @return this for chaining
   * @since 2.5.47
   */
  public GrouperEmail addGroupToSendTo(Group group) {
    Set<String> emails = retrieveEmailAddresses(group, !this.runAsRoot);
    for (String email : GrouperUtil.nonNull(emails)) {
      this.addEmailAddressToSendTo(email);
    }
    return this;
  }

  /**
   * add a group of people to cc
   * @param subject
   * @return this for chaining
   * @since 2.5.47
   */
  public GrouperEmail addGroupToCc(Group group) {
    Set<String> emails = retrieveEmailAddresses(group, !this.runAsRoot);
    for (String email : GrouperUtil.nonNull(emails)) {
      this.addEmailAddressToCc(email);
    }
    return this;
  }

  /**
   * set this to true to run as a root session.  Note you need to set this before adding subjects and groups to look up
   */
  private boolean runAsRoot;
  
  /**
   * set this to true to run as a root session.  Note you need to set this before adding subjects and groups to look up
   * @param runAsRoot
   * @return this for chaining
   * @since 2.5.47
   */
  public GrouperEmail assignRunAsRoot(boolean runAsRoot) {
    this.runAsRoot = runAsRoot;
    return this;
  }
  

  /**
   * add a group of people to bcc
   * @param subject
   * @return this for chaining
   * @since 2.5.47
   */
  public GrouperEmail addGroupToBcc(Group group) {
    Set<String> emails = retrieveEmailAddresses(group, !this.runAsRoot);
    for (String email : GrouperUtil.nonNull(emails)) {
      this.addEmailAddressToBcc(email);
    }
    return this;
  }

  /**
   * add email address (if not blank) to send to.  The email will not send without "to" address(es)
   * @param emailAddress
   * @return this for chaining
   * @since 2.5.47
   */
  public GrouperEmail addEmailAddressToSendTo(String emailAddress) {
    if (StringUtils.isBlank(emailAddress)) {
      return this;
    }
    
    if (!StringUtils.isBlank(this.to)) {
      this.to += ",";
    } else {
      this.to = "";
    }
    
    this.to += emailAddress;
    
    return this;
    
  }
  
  /**
   * add email address (if not blank) to cc
   * @param emailAddress
   * @return this for chaining
   * @since 2.5.47
   */
  public GrouperEmail addEmailAddressToCc(String emailAddress) {
    if (StringUtils.isBlank(emailAddress)) {
      return this;
    }
    
    if (!StringUtils.isBlank(this.cc)) {
      this.cc += ",";
    } else {
      this.cc = "";
    }
    
    this.cc += emailAddress;
    
    return this;
    
  }
  
  /**
   * add email address (if not blank) to bcc
   * @param emailAddress
   * @return this for chaining
   * @since 2.5.47
   */
  public GrouperEmail addEmailAddressToBcc(String emailAddress) {
    if (StringUtils.isBlank(emailAddress)) {
      return this;
    }
    
    if (!StringUtils.isBlank(this.bcc)) {
      this.bcc += ",";
    } else {
      this.bcc = "";
    }
    
    this.bcc += emailAddress;
    
    return this;
    
  }
  
  /**
   * add a subject (e.g. person) to bcc
   * @param subject
   * @return this for chaining
   * @since 2.5.47
   */
  public GrouperEmail addSubjectToBcc(Subject subject) {
    
    if (subject == null) {
      return this;
    }
    
    String emailAddress = retrieveEmailAddress(subject);
    
    return this.addEmailAddressToBcc(emailAddress);
  }

  /**
   * add a group of people to bcc
   * @param groupName full system name of group
   * @param exceptionIfNotFound true if exception if group not found
   * @return this for chaining
   * @since 2.5.47
   */
  public GrouperEmail addGroupNameToBcc(String groupName, boolean exceptionIfNotFound) {
    
    Set<String> emails = retrieveEmailAddresses(groupName, !this.runAsRoot, exceptionIfNotFound);
    for (String email : GrouperUtil.nonNull(emails)) {
      this.addEmailAddressToBcc(email);
    }
    return this;
  }

  /**
   * add a group of people to cc
   * @param groupName full system name of group
   * @param exceptionIfNotFound true if exception if group not found
   * @return this for chaining
   * @since 2.5.47
   */
  public GrouperEmail addGroupNameToCc(String groupName, boolean exceptionIfNotFound) {
    
    Set<String> emails = retrieveEmailAddresses(groupName, !this.runAsRoot, exceptionIfNotFound);
    for (String email : GrouperUtil.nonNull(emails)) {
      this.addEmailAddressToCc(email);
    }
    return this;
  }

  /**
   * add a group of people to send to
   * @param groupName full system name of group
   * @param exceptionIfNotFound true if exception if group not found
   * @return this for chaining
   * @since 2.5.47
   */
  public GrouperEmail addGroupNameToSendTo(String groupName, boolean exceptionIfNotFound) {
    
    Set<String> emails = retrieveEmailAddresses(groupName, !this.runAsRoot, exceptionIfNotFound);
    for (String email : GrouperUtil.nonNull(emails)) {
      this.addEmailAddressToSendTo(email);
    }
    return this;
  }

  /**
   * add a group of people to bcc
   * @param groupUuid group uuid of group
   * @param exceptionIfNotFound true if exception if group not found
   * @return this for chaining
   * @since 2.5.47
   */
  public GrouperEmail addGroupUuidToBcc(String groupUuid, boolean exceptionIfNotFound) {
    
    Set<String> emails = retrieveEmailAddressesByGroupUuid(groupUuid, !this.runAsRoot, exceptionIfNotFound);
    for (String email : GrouperUtil.nonNull(emails)) {
      this.addEmailAddressToBcc(email);
    }
    return this;
  }

  /**
   * add a group of people to cc
   * @param groupUuid group uuid of group
   * @param exceptionIfNotFound true if exception if group not found
   * @return this for chaining
   * @since 2.5.47
   */
  public GrouperEmail addGroupUuidToCc(String groupUuid, boolean exceptionIfNotFound) {
    
    Set<String> emails = retrieveEmailAddressesByGroupUuid(groupUuid, !this.runAsRoot, exceptionIfNotFound);
    for (String email : GrouperUtil.nonNull(emails)) {
      this.addEmailAddressToCc(email);
    }
    return this;
  }

  /**
   * add a group of people to send to
   * @param groupUuid group uuid of group
   * @param exceptionIfNotFound true if exception if group not found
   * @return this for chaining
   * @since 2.5.47
   */
  public GrouperEmail addGroupUuidToSendTo(String groupUuid, boolean exceptionIfNotFound) {
    
    Set<String> emails = retrieveEmailAddressesByGroupUuid(groupUuid, !this.runAsRoot, exceptionIfNotFound);
    for (String email : GrouperUtil.nonNull(emails)) {
      this.addEmailAddressToSendTo(email);
    }
    return this;
  }

  /**
   * secure? method that retrieves email addresses from a group
   * @param group
   * @param secure is false if run as root
   * @param exceptionIfNotFound true if exception if not found
   * @return the email addresses found for users in the group
   * @since 2.5.47
   */
  @SuppressWarnings("unchecked")
  public static Set<String> retrieveEmailAddressesByGroupUuid(final String groupUuid, boolean secure, boolean exceptionIfNotFound) {
    
    return (Set<String>)GrouperSession.internal_callbackRootGrouperSession(!secure, new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        Group group = GroupFinder.findByUuid(grouperSession, groupUuid, exceptionIfNotFound);
        return (Set<String>)retrieveEmailAddresses(group, secure);
      }
    });
  }

  public static List<String> externalSystemTest() {
    
    GrouperEmail grouperEmail = new GrouperEmail().assignRunAsRoot(true);
    
    String externalSystemTestToType = GrouperConfig.retrieveConfig().propertyValueStringRequired("mail.smtp.externalSystemTestToType");
    
    if (StringUtils.equals("emailAddress", externalSystemTestToType)) {
      String externalSystemTestToAddress = GrouperConfig.retrieveConfig().propertyValueStringRequired("mail.smtp.externalSystemTestToAddress");
      grouperEmail.addEmailAddressToSendTo(externalSystemTestToAddress);
      
    } else if (StringUtils.equals("emailToSubject", externalSystemTestToType)) {

      String externalSystemTestToSubjectSourceId = GrouperConfig.retrieveConfig().propertyValueStringRequired("mail.smtp.externalSystemTestToSubjectSourceId");
      
      String externalSystemTestToSubjectIdType = GrouperConfig.retrieveConfig().propertyValueStringRequired("mail.smtp.externalSystemTestToSubjectIdType");
      
      if (StringUtils.equals("subjectId", externalSystemTestToSubjectIdType)) {
        String externalSystemTestToSubjectId = GrouperConfig.retrieveConfig().propertyValueStringRequired("mail.smtp.externalSystemTestToSubjectId");
        grouperEmail.addSubjectIdToSendTo(externalSystemTestToSubjectSourceId, externalSystemTestToSubjectId);

      } else if (StringUtils.equals("subjectIdentifier", externalSystemTestToSubjectIdType)) {
        String externalSystemTestToSubjectIdentifier = GrouperConfig.retrieveConfig().propertyValueStringRequired("mail.smtp.externalSystemTestToSubjectIdentifier");
        grouperEmail.addSubjectIdentifierToSendTo(externalSystemTestToSubjectSourceId, externalSystemTestToSubjectIdentifier);

      } else {
        throw new RuntimeException("Invalid mail.smtp.externalSystemTestToSubjectIdType: '" + externalSystemTestToSubjectIdType + "'");
        
      }
      
    } else if (StringUtils.equals("emailToGroup", externalSystemTestToType)) {
      
      String externalSystemTestToGroupIdType = GrouperConfig.retrieveConfig().propertyValueStringRequired("mail.smtp.externalSystemTestToGroupIdType");
      if (StringUtils.equals("groupUuid", externalSystemTestToGroupIdType)) {
        String externalSystemTestToGroupId = GrouperConfig.retrieveConfig().propertyValueStringRequired("mail.smtp.externalSystemTestToGroupId");
        grouperEmail.addGroupUuidToSendTo(externalSystemTestToGroupId, true);
      } else if (StringUtils.equals("groupName", externalSystemTestToGroupIdType)) {
        String externalSystemTestToGroupName = GrouperConfig.retrieveConfig().propertyValueStringRequired("mail.smtp.externalSystemTestToGroupName");
        grouperEmail.addGroupNameToSendTo(externalSystemTestToGroupName, true);
      } else {
        throw new RuntimeException("Invalid mail.smtp.externalSystemTestToGroupIdType: '" + externalSystemTestToGroupIdType + "'");
        
      }

    } else {
      throw new RuntimeException("Invalid mail.smtp.externalSystemTestToType: '" + externalSystemTestToType + "'");
    }
    
    String externalSystemTestSubject = GrouperConfig.retrieveConfig().propertyValueStringRequired("mail.smtp.externalSystemTestSubject");
    grouperEmail.setSubject(externalSystemTestSubject);

    String externalSystemTestBody = GrouperConfig.retrieveConfig().propertyValueStringRequired("mail.smtp.externalSystemTestBody");
    grouperEmail.setBody(externalSystemTestBody);
    
    String externalSystemTestCcAddress = GrouperConfig.retrieveConfig().propertyValueString("mail.externalSystemTestCcAddress");
    if (!StringUtils.isBlank(externalSystemTestCcAddress)) {
      grouperEmail.setCc(externalSystemTestCcAddress);
    }
    String externalSystemTestBccAddress = GrouperConfig.retrieveConfig().propertyValueString("mail.externalSystemTestBccAddress");
    if (!StringUtils.isBlank(externalSystemTestBccAddress)) {
      grouperEmail.setBcc(externalSystemTestBccAddress);
    }
    String externalSystemTestFromAddress = GrouperConfig.retrieveConfig().propertyValueString("mail.externalSystemTestFromAddress");
    if (!StringUtils.isBlank(externalSystemTestFromAddress)) {
      grouperEmail.setFrom(externalSystemTestFromAddress);
    }
    String externalSystemTestReplyAddresses = GrouperConfig.retrieveConfig().propertyValueString("mail.externalSystemTestReplyAddresses");
    if (!StringUtils.isBlank(externalSystemTestReplyAddresses)) {
      grouperEmail.setReplyTo(externalSystemTestReplyAddresses);
    }
    grouperEmail.send();
    if (!grouperEmail.isMailSent()) {
      return GrouperUtil.toList("Mail not sent.  Maybe couldnt find recipient, or another reason");
    }
    return null;
  }

}
