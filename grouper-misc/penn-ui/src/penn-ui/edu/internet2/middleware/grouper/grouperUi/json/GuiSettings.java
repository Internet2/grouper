/*
 * @author mchyzer
 * $Id: GuiSettings.java,v 1.1 2009-07-31 14:27:27 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.json;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 *
 */
public class GuiSettings {

  /** text strings for screen */
  private Map<String, String> text = new LinkedHashMap<String, String>();

  /** templates strings for screen */
  private Map<String, String> templates = new LinkedHashMap<String, String>();

  /** need to send this key back with each request in authnKey param */
  private String authnKey = null;
  
  /** logged in subject */
  private GuiSubject loggedInSubject = null;
  
  /**
   * logged in subject
   * @return logged in subject
   */
  public GuiSubject getLoggedInSubject() {
    return this.loggedInSubject;
  }

  /**
   * logged in subject
   * @param loggedInSubject1
   */
  public void setLoggedInSubject(GuiSubject loggedInSubject1) {
    this.loggedInSubject = loggedInSubject1;
  }

  /**
   * need to send this key back with each request in authnKey param
   * @return the authn key
   */
  public String getAuthnKey() {
    return this.authnKey;
  }

  /**
   * need to send this key back with each request in authnKey param
   * @param authnKey1
   */
  public void setAuthnKey(String authnKey1) {
    this.authnKey = authnKey1;
  }

  /**
   * instantiated test for the settings bean
   * @return the text
   */
  public Map<String, String> getText() {
    return this.text;
  }

  /**
   * templates strings for screen
   * @return
   */
  public Map<String, String> getTemplates() {
    return this.templates;
  }

}
