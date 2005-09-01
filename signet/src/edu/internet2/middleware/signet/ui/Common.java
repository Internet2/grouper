/*--
  $Id: Common.java,v 1.19 2005-09-01 17:59:58 acohen Exp $
  $Date: 2005-09-01 17:59:58 $
  
  Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
  Licensed under the Signet License, Version 1,
  see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet.ui;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;

import edu.internet2.middleware.signet.Assignment;
import edu.internet2.middleware.signet.Decision;
import edu.internet2.middleware.signet.Limit;
import edu.internet2.middleware.signet.LimitValue;
import edu.internet2.middleware.signet.PrivilegedSubject;
import edu.internet2.middleware.signet.choice.Choice;
import edu.internet2.middleware.signet.choice.ChoiceSet;

public class Common
{
  private static final String DAY_SUFFIX   = "_day";
  private static final String MONTH_SUFFIX = "_month";
  private static final String YEAR_SUFFIX  = "_year";
  
  private static final String HASDATE_SUFFIX  = "_hasDate";
  private static final String DATETEXT_SUFFIX = "_dateText";
  
  private static final String NODATE_VALUE  = "NO_DATE";
  private static final String YESDATE_VALUE = "YES_DATE";
  
  private static final String DATE_FORMAT = "MM-dd-yyyy";
  private static final String DATE_SAMPLE = "mm-dd-yyyy";
  /**
   * @param log
   * @param request
   */
  public static void showHttpParams
  	(String prefix, Log log, HttpServletRequest request)
  {
    Enumeration paramNames = request.getParameterNames();
    while (paramNames.hasMoreElements())
    {
      String paramName = (String)(paramNames.nextElement());
      String[] paramValues = request.getParameterValues(paramName);
      log.warn(prefix + ": " + paramName + "=" + printArray(paramValues));
    }
  }

  /**
   * @param log
   * @param request
   */
  public static void showHttpParams
  	(String prefix, Logger logger, HttpServletRequest request)
  {
    Enumeration paramNames = request.getParameterNames();
    while (paramNames.hasMoreElements())
    {
      String paramName = (String)(paramNames.nextElement());
      String[] paramValues = request.getParameterValues(paramName);
      logger.log
      	(Level.WARNING,
      	 prefix + ": " + paramName + "=" + printArray(paramValues));
    }
  }
  
  private static String printArray(String[] items)
  {
    StringBuffer out = new StringBuffer();
    out.append("[");
    for (int i = 0; i < items.length; i++)
    {
      if (i > 0)
      {
        out.append(", ");
      }
      
      out.append(items[i]);
    }
    
    out.append("]");
    
    return out.toString();
  }
  
  private static LimitValue[] getLimitValuesArray(Set limitValues)
  {
    LimitValue limitValuesArray[] = new LimitValue[0];
    
    return
      (LimitValue[])(limitValues.toArray(limitValuesArray));
  }
  
  public static LimitValue[] getLimitValuesInDisplayOrder(Set limitValues)
  {
    LimitValue[] limitValuesArray = getLimitValuesArray(limitValues);
    Arrays.sort(limitValuesArray);
    return limitValuesArray;
  }

  public static LimitValue[] getLimitValuesInDisplayOrder
    (Assignment assignment)
  {
    return getLimitValuesInDisplayOrder(assignment.getLimitValues());
  }
  
  public static Limit[] getLimitsInDisplayOrder(Set limits)
  {
    Limit[] limitsArray = new Limit[0];
    limitsArray = (Limit[])(limits.toArray(limitsArray));
    
    if (limitsArray.length > 0)
    {
      Arrays.sort(limitsArray);
    }
    
    return limitsArray;
  }
  
  public static Choice[] getChoicesInDisplayOrder(ChoiceSet choiceSet)
  {
    Choice[] choiceArray = new Choice[0];
    choiceArray = (Choice[])(choiceSet.getChoices().toArray(choiceArray));
    
    if (choiceArray.length > 0)
    {
      Arrays.sort(choiceArray);
    }
    return choiceArray;
  }
  
  public static String displayLimitValues
    (Set limits,
     Set limitValues)
  {
    StringBuffer strBuf = new StringBuffer();
    
    LimitValue[] limitValuesSortedArray
      = getLimitValuesInDisplayOrder(limitValues);
    Limit[] limitsSortedArray = getLimitsInDisplayOrder(limits);

    for (int limitIndex = 0;
         limitIndex < limitsSortedArray.length;
         limitIndex++)
    {
      Limit limit = limitsSortedArray[limitIndex];
      strBuf.append((limitIndex > 0) ? "\n<br />\n" : "");
      strBuf.append("<span class=\"label\">" + limit.getName() + ":</span> ");

      int limitValuesPrinted = 0;
      for (int limitValueIndex = 0;
           limitValueIndex < limitValuesSortedArray.length;
           limitValueIndex++)
      {
        LimitValue limitValue = limitValuesSortedArray[limitValueIndex];
        if (limitValue.getLimit().equals(limit))
        {
          strBuf.append((limitValuesPrinted++ > 0) ? ", " : "");
          strBuf.append(limitValue.getDisplayValue());
        }
      }
    }
    
    return strBuf.toString();
  }

  /**
   * Formats limit-values like this:
   *     <span class="label">Approval limit:<span> $100
   * 
   * Note that the colon is inside the span. The space between colon and
   * value can be inside or outside, whichever is easier.
   * 
   * @param assignment
   * @return
   */
  public static String displayLimitValues(Assignment assignment)
  {
    Set limits = assignment.getFunction().getLimits();
    Set limitValues = assignment.getLimitValues();
    
    return displayLimitValues(limits, limitValues);
  }
  
  public static String displayStatus(Assignment assignment)
  {
    StringBuffer statusStr = new StringBuffer();
    
    // An Assignment with no ID has not yet been persisted. An Assignment
    // that has not yet been persisted has no Status yet. That is, it's not
    // active, it's not pending, it's not nuthin' yet.
    boolean hasStatus = !(assignment.getId() == null);
    boolean canUse = assignment.canUse();
    boolean canGrant = assignment.canGrant();
    
    if (hasStatus)
    {
      statusStr.append(assignment.getStatus().getName());
    }
    
    if (hasStatus && canUse)
    {
      statusStr.append(", ");
    }
    
    if (canUse)
    {
      statusStr.append("can use");
    }
    
    if ((hasStatus || canUse) && canGrant)
    {
      statusStr.append(", ");
    }
    
    if (canGrant)
    {
      statusStr.append("can grant");
    }

    return statusStr.toString();
  }
  
  

  // This is a shameful little hack to temporarily simulate person-quicksearch
  // until it's implemented in the upcoming new version of the Subject interface:
  public static SortedSet filterSearchResults
  	(Set privilegedSubjects, String searchString)
  {
    SortedSet resultSet = new TreeSet();
    Iterator privilegedSubjectsIterator = privilegedSubjects.iterator();
    while (privilegedSubjectsIterator.hasNext())
    {
      PrivilegedSubject pSubject
      	= (PrivilegedSubject)(privilegedSubjectsIterator.next());
    
      if ((searchString == null)
          || (searchString.equals(""))
          || (pSubject.getSubject().getName().toUpperCase().indexOf
               (searchString.toUpperCase())
               	 != -1))
      {
        resultSet.add(pSubject);
      }
    }
    
    return resultSet;
  }
  
  public static boolean isSelected
    (Limit  limit,
     Choice choice,
     Set    assignmentLimitValues)
  {
    return isSelected(limit, choice, assignmentLimitValues, null);
  }
  
  public static boolean isSelected
    (Limit  limit,
     Choice choice,
     Set    assignmentLimitValues,
     Choice defaultChoice)
  {
    boolean limitPreSelected = false;
    
    Iterator assignmentLimitValuesIterator = assignmentLimitValues.iterator();
    while (assignmentLimitValuesIterator.hasNext())
    {
      LimitValue limitValue
        = (LimitValue)(assignmentLimitValuesIterator.next());
      if (limitValue.getLimit().equals(limit))
      {
        limitPreSelected = true;
        if (choice.getValue().equals(limitValue.getValue()))
        {
          // This particular Limit-value should appear as pre-selected,
          // because it's already part of the specified Assignment-Limit-values.
          return true;
        }
      }
    }
    
    // If we've gotten this far, then this Limit-value should not appear as
    // pre-selected, UNLESS it's the default, and this Limit does not appear
    // in the Set of pre-selected AssignmentLimitValues.
    if (choice.equals(defaultChoice) && (limitPreSelected == false))
    {
      return true;
    }
    
    // If we've gotten this far, this Limit-value should not appear as
    // pre-selected.
    return false;
  }
  
  public static String editLink
    (PrivilegedSubject  editor,
     Assignment         assignment)
  {
    StringBuffer outStr = new StringBuffer();
    
    Decision decision = editor.canEdit(assignment);
    if (decision.getAnswer() == true)
    {
      outStr.append("<a\n");
      outStr.append("  style=\"float: right;\"\n");
      outStr.append("  href=\"Conditions.do?assignmentId=" + assignment.getId() + "\">\n");
      outStr.append("  <img\n");
      outStr.append("    src=\"images/arrow_right.gif\"\n");
      outStr.append("      alt=\"\" />\n");
      outStr.append("  edit\n");
      outStr.append("</a>");
    }
    
    return outStr.toString();
  }
  
  public static String revokeBox
    (PrivilegedSubject  revoker,
     Assignment         assignment,
     UnusableStyle      unusableStyle)
  {
    StringBuffer outStr = new StringBuffer();
    
    Decision decision = revoker.canEdit(assignment);
    if (decision.getAnswer() == true)
    {
      outStr.append("<td align=\"center\" >\n");
      outStr.append("  <input\n");
      outStr.append("    name=\"revoke\"\n");
      outStr.append("    type=\"checkbox\"\n");
      outStr.append("    id=\"" + assignment.getId() + "\"\n");
      outStr.append("    value=\"" + assignment.getId() + "\"\n");
      outStr.append("    onclick=\"selectThis(this.checked);\" />\n");
      outStr.append("</td>");
    }
    else if (unusableStyle == UnusableStyle.TEXTMSG)
    {
      outStr.append("<td align=\"center\" class=\"status\" >\n");
      outStr.append("  You are not authorized to revoke this assignment.\n");
      outStr.append("</td>");
    }
    else // show the checkbox dimmed.
    {
      outStr.append("<td align=\"center\" >\n");
      outStr.append("  <input\n");
      outStr.append("    name=\"revoke\"\n");
      outStr.append("    type=\"checkbox\"\n");
      outStr.append("    id=\"" + assignment.getId() + "\"\n");
      outStr.append("    value=\"" + assignment.getId() + "\"\n");
      outStr.append("    disabled=\"true\"\n");
//      outStr.append("    title=\"" + revoker.editRefusalExplanation(assignment, "logged-in user") + "\"");
      outStr.append("/>");
      outStr.append("</td>");
    }
    
    return outStr.toString();
  }
  
  public static String assignmentPopupIcon(Assignment assignment)
  {
    StringBuffer outStr = new StringBuffer();
    
    outStr.append("<a\n");
    outStr.append("  style=\"float: right;\"\n");
    outStr.append("  href\n");
    outStr.append("    =\"javascript:openWindow\n");
    outStr.append("        ('Assignment.do?assignmentId=" + assignment.getId() + "',\n");
    outStr.append("         'popup',\n");
    outStr.append("         'scrollbars=yes,\n");
    outStr.append("          resizable=yes,\n");
    outStr.append("          width=500,\n");
    outStr.append("          height=250');\">\n");
    outStr.append("  <img\n");
    outStr.append("   src=\"images/maglass.gif\"\n");
    outStr.append("   alt=\"More info about this assignment...\" />\n");
    outStr.append("</a>");
  
    return outStr.toString();
  }
  
  /**
   * This method emits some HTML which should be placed between a <tr> and a
   * </tr> tag.
   * 
   * @param nameRoot
   * @param title
   * @param noDateLabel
   * @param dateValueLabel
   * @param defaultDateValue
   * @return
   */
  public static String dateSelection
    (HttpServletRequest request,
     String             nameRoot,
     String             title,
     String             noDateLabel,
     String             dateValueLabel,
     Date               defaultDateValue)
  {
    StringBuffer outStr = new StringBuffer();
    String defaultDateStr;
    
    
    if (defaultDateValue == null)
    {
      defaultDateStr = DATE_SAMPLE;
    }
    else
    {
      SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
      defaultDateStr = formatter.format(defaultDateValue);
    }
    
    String radioButtonGroupName = nameRoot + HASDATE_SUFFIX;
    
    outStr.append("<td>" + title + "</td>\n");
    outStr.append("<td>\n");
    
    outStr.append(displayErrorMessage(request, nameRoot));
    
    outStr.append("  <p>\n");
    outStr.append("    <input\n");
    outStr.append("      name=\"" + radioButtonGroupName + "\"\n");
    outStr.append("      type=\"radio\"\n");
    outStr.append("      value=\"" + NODATE_VALUE + "\"\n");
    outStr.append(       (defaultDateValue == null ? "checked" : "") + " />\n");
    outStr.append("    " + noDateLabel + "\n");
    outStr.append("  </p>\n");
    outStr.append("  <p>\n");
    outStr.append("    <input\n");
    outStr.append("      name=\"" + radioButtonGroupName + "\"\n");
    outStr.append("      type=\"radio\"\n");
    outStr.append("      value=\"" + YESDATE_VALUE + "\"\n");
    outStr.append(       (defaultDateValue != null ? "checked" : "") + " />\n");
    outStr.append("    " + dateValueLabel + "\n");
    outStr.append("    <input\n");
    outStr.append("      name=\"" + nameRoot + DATETEXT_SUFFIX + "\"\n");
    outStr.append("      type=\"text\"\n");
    outStr.append("      class=\"date\"\n");
    outStr.append("      value=\"" + defaultDateStr + "\"\n");
    outStr.append("      onfocus=\n");
    outStr.append("        \"if (this.value == '" + DATE_SAMPLE + "') this.value='';\n");
    outStr.append("        this.style.color='black';\n");
    outStr.append("        this.form." + radioButtonGroupName + "[0].checked=false;\n");
    outStr.append("        this.form." + radioButtonGroupName + "[1].checked='checked'\"/>\n");
    outStr.append("  </p>\n");
    outStr.append("</td>\n");
    
    return outStr.toString();
  }
  
  /*
   * This code is copied from:
   * 
   *    http://javaboutique.internet.com/tutorials/excep_struts/index-3.html
   */
  private static String displayErrorMessage
    (HttpServletRequest request,
     String             nameRoot)
  {
    StringBuffer outStr = new StringBuffer();
    // Get the ActionMessages 
    Object o = request.getAttribute(Globals.MESSAGE_KEY);
    
    if (o != null)
    {
      ActionMessages actionMessages = (ActionMessages)o;

      // Get the locale and message resources bundle
      Locale locale = 
        (Locale)(request.getSession().getAttribute(Globals.LOCALE_KEY));
      MessageResources messageResources
        = (MessageResources)(request.getAttribute(Globals.MESSAGES_KEY));

      // Loop thru all the labels in the ActionMessages  
      for (Iterator actionMessagesProperties = actionMessages.properties();
           actionMessagesProperties.hasNext();)
      {
        String property = (String)actionMessagesProperties.next();
        if (property.equals(nameRoot))
        {
          // TO DO - This code should really retrieve the text of the
          // error-message from the ActionMessage. For right now, I'll
          // just hard-code that error-text.
          outStr.append("<FONT COLOR=\"RED\">\n");
          outStr.append("  <br/>\n");
          outStr.append("  Dates must be in the format '" + DATE_SAMPLE + "'.\n");
          outStr.append("  Please try again.");
          outStr.append("  <br/>\n");
          outStr.append("</FONT>\n");

//          // Get all messages for this label
//          for (Iterator propertyValuesIterator = actionMessages.get(property);
//               propertyValuesIterator.hasNext();)
//          {
//            ActionMessage actionMessage
//              = (ActionMessage)propertyValuesIterator.next();
//            String key = actionMessage.getKey();
//            Object[] values = actionMessage.getValues();
//            String messageStr = messageResources.getMessage(locale, key, values);
//            outStr.append(messageStr);
//            outStr.append("<br/>");
//          }
        }
      }
    }
    
    return outStr.toString();
  }
  
  public static String dateSelection(String nameRoot, Date date)
  {
    Calendar calDate;
    int dayNumber   = -1;
    int monthNumber = -1;
    int yearNumber  = -1;
    Calendar calCurrentDate = Calendar.getInstance();
    calCurrentDate.setTime(new Date());
    int currentYear = calCurrentDate.get(Calendar.YEAR);
    
    if (date != null)
    {
      calDate = Calendar.getInstance();
      calDate.setTime(date);
      dayNumber = calDate.get(Calendar.DAY_OF_MONTH);
      monthNumber = calDate.get(Calendar.MONTH) + 1;
      yearNumber = calDate.get(Calendar.YEAR);
    }
    
    StringBuffer outStr = new StringBuffer();
    
    outStr.append("<select name=\"" + nameRoot + DAY_SUFFIX + "\">\n");
    outStr.append("  <option value=\"none\"></option>\n");
    
    for (int i = 1; i <= 31; i++)
    {
      outStr.append
        ("  <option" + (i == dayNumber ? " selected=\"selected\"" : "") + ">"
            + i
            + "</option>\n");
    }
    
    outStr.append("</select>\n");
    
    outStr.append("<select name=\"" + nameRoot + MONTH_SUFFIX + "\">\n");
    outStr.append("  <option value=\"none\"></option>\n");
    
    SimpleDateFormat dateFormat = new SimpleDateFormat();
    dateFormat.applyPattern("MMMMM");
    
    for (int i = 1; i <= 12; i++)
    {
      Calendar monthValue = Calendar.getInstance();
      monthValue.set(Calendar.MONTH, i-1);
      Date monthDate = monthValue.getTime();
      String monthName = dateFormat.format(monthDate).toString();
      outStr.append
        ("  <option"
            + (i == monthNumber ? " selected=\"selected\"" : "")
            + " label=\"" + monthName + "\""
            + " value=\"" + i + "\""
            + ">"
            + monthName
            + "</option>\n");
    }
    
    outStr.append("</select>\n");
    
    outStr.append("<select name=\"" + nameRoot + YEAR_SUFFIX + "\">\n");
    outStr.append("  <option value=\"none\"></option>\n");
    
    for (int i = (yearNumber == -1 ? currentYear : yearNumber);
         i < ((yearNumber == -1 ? currentYear : yearNumber) + 4);
         i++)
    {
      outStr.append
        ("  <option" + (i == yearNumber ? " selected=\"selected\"" : "") + ">"
            + i
            + "</option>\n");
    }
    
    outStr.append("</select>\n");
    
    return outStr.toString();
  }
  
  static public Date getDateParam
    (HttpServletRequest request,
     String             nameRoot)
  throws DataEntryException
  {
    return getDateParam
      (request,
       nameRoot,
       null);
  }
  
  static public Date getDateParam
    (HttpServletRequest request,
     String             nameRoot,
     Date               defaultDate)
  throws DataEntryException
  {
    Date date;
    
    // First, let's see if the date is present or not.
    String dateStringPresence = request.getParameter(nameRoot + HASDATE_SUFFIX);
    if (dateStringPresence.equals(NODATE_VALUE))
    {
      return defaultDate;
    }
    
    String dateStr = request.getParameter(nameRoot + DATETEXT_SUFFIX);
    SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
    try
    {
      date = formatter.parse(dateStr);
    }
    catch (ParseException pe)
    {
      throw new DataEntryException(pe, nameRoot, dateStr, DATE_SAMPLE);
    }
    
    return date;
  }
}
