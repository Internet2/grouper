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
/**
 * @author Kate
 * $Id: GrouperUiFunctions.java,v 1.1 2009-10-11 22:04:17 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ui.tags;

import java.util.Date;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiSubject;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiHideShow;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.ui.util.MapBundleWrapper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;


/**
 * EL functions
 */
public class GrouperUiFunctions {

  /**
   * if null or blank
   * @param string
   * @return if null or blank
   */
  public static boolean isBlank(String string) {
    return GrouperUtil.isBlank(string);
  }
  
  /**
   * if an owner has a privilege by the authenticated user
   * @param owner 
   * @param privilegeOrListName 
   * @return true if has privilege, false if not
   */
  public static boolean canHavePrivilege(GrouperObject owner, String privilegeOrListName) {
    
    Subject subject = GrouperSession.staticGrouperSession().getSubject();
    
    //dont check security, this is on behalf of the UI, assume its allowed to check
    
    if (owner instanceof Group) {
      return ((Group)owner).canHavePrivilege(subject, privilegeOrListName, false);
    }
    if (owner instanceof Stem) {
      return ((Stem)owner).canHavePrivilege(subject, privilegeOrListName, false);
    }
    if (owner instanceof AttributeDef) {
      return ((AttributeDef)owner).getPrivilegeDelegate().canHavePrivilege(subject, privilegeOrListName, false);
    }
    if (owner instanceof AttributeDefName) {
      return ((AttributeDefName)owner).getAttributeDef().getPrivilegeDelegate().canHavePrivilege(subject, privilegeOrListName, false);
    }
    throw new RuntimeException("Cant find owner for '" + (owner == null ? null : owner.getClass()) + "'");
    
  }
  
  /**
   * Escapes XML ( ampersand, lessthan, greater than, double quote), and single quote with slash
   * @param input 
   * @return the escaped string
   */
  public static String escapeJavascript(String input) {
    
    input = GrouperUiUtils.escapeJavascript(input, true);
    return input;
  }

  /**
   * concat for EL
   * @param a
   * @param b
   * @return the concatenated strings
   */
  public static String concat2(Object a, Object b) {
    if (a == null) {
      a = "";
    }
    if (b == null) {
      b = "";
    }
    return GrouperUtil.stringValue(a) + GrouperUtil.stringValue(b);
  }

  /**
   * WordUtils.capitalizeFully
   * @param string
   * @return the concatenated strings
   */
  public static String capitalizeFully(String string) {
    return WordUtils.capitalizeFully(string);
  }

  /**
   * Escapes URL
   * @param input 
   * @return the escaped string
   */
  public static String escapeUrl(String input) {
    if (input == null) {
      return "";
    }
    input = GrouperUtil.escapeUrlEncode(input);
    return input;
  }

  /**
   * prints out a message, assumes it is there
   * @param key
   * @param escapeHtml (true to escape html)
   * @param escapeSingleQuotes if escaping html, should we also escape single quotes?
   * @return the message string
   */
  public static String message(String key, boolean escapeHtml, boolean escapeSingleQuotes) {
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    MapBundleWrapper mapBundleWrapper = (MapBundleWrapper)httpServletRequest.getSession().getAttribute("navNullMap");
    
    String value = (String)mapBundleWrapper.get(key);

    if (escapeHtml) {
      value = GrouperUiUtils.escapeHtml(value, true, escapeSingleQuotes);
    }
    
    return value;
  }
  
  /**
   * <pre>
   * print out the style value for a hide show
   * 
   * Each hide show has a name, and it should be unique in the app, so be explicit, 
   * below you see "hideShowName", that means whatever name you pick
   * 
   * First add this css class to elements which should show when the state is show:
   * shows_hideShowName
   * 
   * Then add this to things which are in the opposite toggle state: hides_hideShowName
   * 
   * Then add this to the button(s):
   * buttons_hideShowName
   *  
   * In the business logic, you must init the hide show before the JSP draws (this has name,
   * text when shown, hidden, if show initially, and if store in session):
   * GuiHideShow.init("simpleMembershipUpdateAdvanced", false, 
   *    GrouperUiUtils.message("simpleMembershipUpdate.hideAdvancedOptionsButton"), 
   *       GrouperUiUtils.message("simpleMembershipUpdate.showAdvancedOptionsButton"), true);
   *
   * Finally, use these EL functions to display the state correctly in JSP:
   * Something that is hidden/shown
   * style="${grouper:hideShowStyle('hideShowName', true)}
   * 
   * Button text:
   * ${grouper:hideShowButtonText('hideShowName')}
   * 
   * In the button, use this onclick:
   * onclick="return guiHideShow(event, 'hideShowName');"
   * </pre>
   * @param hideShowName
   * @param showWhenShowing true if the section should show when the hide show is showing
   * @return the style
   */
  public static String hideShowStyle(String hideShowName, boolean showWhenShowing) {
    
    //we need to find the hide show, either it is something we are initializing, or something sent from browser
    GuiHideShow guiHideShow = GuiHideShow.retrieveHideShow(hideShowName, true);
    
    if (guiHideShow.isShowing() != showWhenShowing) {
      return "display: none;";
    }
    
    //note, dont make assumptions, do the default
    return "";
    
  }
  
  /**
   * <pre>
   * print out the button text for a hide show
   * 
   * Each hide show has a name, and it should be unique in the app, so be explicit, 
   * below you see "hideShowName", that means whatever name you pick
   * 
   * First add this css class to elements which should show when the state is show:
   * shows_hideShowName
   * 
   * Then add this to things which are in the opposite toggle state: hides_hideShowName
   * 
   * Then add this to the button(s):
   * buttons_hideShowName
   *  
   * In the business logic, you must init the hide show before the JSP draws (this has name,
   * text when shown, hidden, if show initially, and if store in session):
   * GuiHideShow.init("simpleMembershipUpdateAdvanced", false, 
   *    GrouperUiUtils.message("simpleMembershipUpdate.hideAdvancedOptionsButton"), 
   *       GrouperUiUtils.message("simpleMembershipUpdate.showAdvancedOptionsButton"), true);
   *
   * Finally, use these EL functions to display the state correctly in JSP:
   * Something that is hidden/shown
   * style="${grouper:hideShowStyle('hideShowName', true)}
   * 
   * Button text:
   * ${grouper:hideShowButtonText('hideShowName')}
   * 
   * In the button, use this onclick:
   * onclick="return guiHideShow(event, 'hideShowName');"
   * </pre>
   * @param hideShowName
   * @return the text
   */
  public static String hideShowButtonText(String hideShowName) {
    
    //we need to find the hide show, either it is something we are initializing, or something sent from browser
    GuiHideShow guiHideShow = GuiHideShow.retrieveHideShow(hideShowName, true);
    
    if (guiHideShow.isShowing()) {
      return guiHideShow.getTextWhenShowing();
    }
    
    return guiHideShow.getTextWhenHidden();
    
  }
  
  /**
   * 
   * @param string
   * @param length
   * @param tooltip
   * @param escapeHtml should probably always be true
   * @return the string
   */
  public static String abbreviate(String string, int length, boolean tooltip, boolean escapeHtml) {

    if (StringUtils.isEmpty(string)) {
      return string;
    }
    
    //if under the size limit, that is ok
    if (string.length() < length) {
      if (escapeHtml) {
        return escapeHtml(string);
      }
      return string;
    }
    
    String abbreviatedString = StringUtils.abbreviate(string, length);
    
    //if not tooltipping, thats ok
    if (!tooltip) {
      return abbreviatedString;
    }
    
    //if tooltipping, do that
    return "<span class=\"tooltip\" onmouseover=\"Tip('" + escapeJavascript(string)
      + "')\" onmouseout=\"UnTip()\">" 
      + (escapeHtml ? escapeHtml(abbreviatedString) : abbreviatedString) + "</span>"; 
        
  }

  /**
   * Escapes XML ( ampersand, lessthan, greater than, double quotes)
   * e.g. grouper:escapeHtml(someVar.someField)
   * @param input 
   * @return the escaped string
   */
  public static String escapeHtml(String input) {
    
    input = GrouperUtil.xmlEscape(input, true);
    return input;
  }

  /**
   * get a subject string label short 2 from member id
   * @param memberId
   * @return the subject string label
   */
  public static String subjectStringLabelShort2fromMemberId(String memberId) {
    
    if (StringUtils.isBlank(memberId)) {
      return "";
    }
    String subjectId = null;
    try {
      Member member = MemberFinder.findByUuid( GrouperSession.staticGrouperSession(), 
          memberId, true );
      subjectId = member.getSubjectId();
      Subject subject = member.getSubject();

      return new GuiSubject(subject).getScreenLabelShort2();
    } catch (SubjectNotFoundException snfe) {
      GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setSubjectId(subjectId);
      try {
        return TextContainer.retrieveFromRequest().getText().get("guiSubjectNotFound");
      } finally {
        GrouperRequestContainer.retrieveFromRequestOrCreate().getCommonRequestContainer().setSubjectId(null);
      }
    }

  }

  /**
   * convert a date long to a string based on the user's locale
   * @param dateLong
   * @return the date string for the user's locale
   */
  public static String formatDateLong(Long dateLong) {
    if (dateLong == null || dateLong == 0L) {
      return "";
    }
    Date date = new Date(dateLong);

    Locale locale = GrouperUiFilter.retrieveLocale();

    //probably doesnt need to be escaped, but who knows...
    return GrouperUtil.xmlEscape(StringUtils.defaultString(GrouperUiUtils.dateToString(locale, date)));

  }

  /**
   * concat for EL
   * @param a
   * @param b
   * @param c
   * @return the concatenated strings
   */
  public static String concat3(Object a, Object b, Object c) {
    if (a == null) {
      a = "";
    }
    if (b == null) {
      b = "";
    }
    if (c == null) {
      c = "";
    }
    return GrouperUtil.stringValue(a) + GrouperUtil.stringValue(b) + GrouperUtil.stringValue(c);
  }
}
