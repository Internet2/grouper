/*--
  $Id: Common.java,v 1.18 2005-08-25 20:31:35 acohen Exp $
  $Date: 2005-08-25 20:31:35 $
  
  Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
  Licensed under the Signet License, Version 1,
  see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet.ui;

import java.text.SimpleDateFormat;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.signet.Assignment;
import edu.internet2.middleware.signet.Decision;
import edu.internet2.middleware.signet.Limit;
import edu.internet2.middleware.signet.LimitValue;
import edu.internet2.middleware.signet.PrivilegedSubject;
import edu.internet2.middleware.signet.choice.Choice;
import edu.internet2.middleware.signet.choice.ChoiceSet;

public class Common
{
  private static final String daySuffix   = "_day";
  private static final String monthSuffix = "_month";
  private static final String yearSuffix  = "_year";
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
    
    outStr.append("<select name=\"" + nameRoot + daySuffix + "\">\n");
    outStr.append("  <option value=\"none\"></option>\n");
    
    for (int i = 1; i <= 31; i++)
    {
      outStr.append
        ("  <option" + (i == dayNumber ? " selected=\"selected\"" : "") + ">"
            + i
            + "</option>\n");
    }
    
    outStr.append("</select>\n");
    
    outStr.append("<select name=\"" + nameRoot + monthSuffix + "\">\n");
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
    
    outStr.append("<select name=\"" + nameRoot + yearSuffix + "\">\n");
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
  
  private static boolean paramIsPresent(String paramVal)
  {
    return
      ((paramVal != null)
       && (paramVal.length() > 0)
       && (!paramVal.equalsIgnoreCase("none")));
  }
  
  private static boolean allDatePartsPresent
    (String day,
     String month,
     String year)
  {
    return
      (paramIsPresent(day) && paramIsPresent(month) && paramIsPresent(year));
  }
  
  static public Date getDateParam
    (HttpServletRequest request,
     String             nameRoot)
  {
    Date date = null;
    
    // In this first version of this method, we'll just interpret
    // any incomplete date as a NULL date.
    String dayStr = request.getParameter(nameRoot + daySuffix);
    String monthStr = request.getParameter(nameRoot + monthSuffix);
    String yearStr = request.getParameter(nameRoot + yearSuffix);
    
    if (allDatePartsPresent(dayStr, monthStr, yearStr))
    {
      // dayNumber is between 1 and 31.
      int dayNumber
        = Integer.parseInt(request.getParameter(nameRoot + daySuffix));
      // dayNumber is between 1 and 12.
      int monthNumber
        = Integer.parseInt(request.getParameter(nameRoot + monthSuffix));
      int yearNumber
        = Integer.parseInt(request.getParameter(nameRoot + yearSuffix));
      
      Calendar calendar = Calendar.getInstance();
      calendar.clear();
      calendar.set(yearNumber, monthNumber-1, dayNumber);
      date = calendar.getTime();
    }
    
    return date;
  }
}
