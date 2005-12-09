/*--
  $Id: Common.java,v 1.52 2005-12-09 22:26:33 acohen Exp $
  $Date: 2005-12-09 22:26:33 $
  
  Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
  Licensed under the Signet License, Version 1,
  see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet.ui;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;

import edu.internet2.middleware.signet.Assignment;
import edu.internet2.middleware.signet.AssignmentHistory;
import edu.internet2.middleware.signet.Decision;
import edu.internet2.middleware.signet.Grantable;
import edu.internet2.middleware.signet.History;
import edu.internet2.middleware.signet.Limit;
import edu.internet2.middleware.signet.LimitValue;
import edu.internet2.middleware.signet.ObjectNotFoundException;
import edu.internet2.middleware.signet.PrivilegedSubject;
import edu.internet2.middleware.signet.Proxy;
import edu.internet2.middleware.signet.ProxyHistory;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.Status;
import edu.internet2.middleware.signet.Subsystem;
import edu.internet2.middleware.signet.choice.Choice;
import edu.internet2.middleware.signet.choice.ChoiceSet;

import edu.internet2.middleware.signet.ui.Constants;
import edu.internet2.middleware.subject.provider.SubjectTypeEnum;

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

  final private static java.text.SimpleDateFormat[] DATERS =  {
    new java.text.SimpleDateFormat("MM/d/yy"),
    new java.text.SimpleDateFormat("MM-d-yy"),
    new java.text.SimpleDateFormat("MM.d.yy"),
    new java.text.SimpleDateFormat("dd-MMM-yy"),
    new java.text.SimpleDateFormat("dd-MMMM-yy"),
    new java.text.SimpleDateFormat("yyyy-MM-dd"),
    new java.text.SimpleDateFormat("MMMM d, yyyy")
    };
  
  private static final Set activeAndPendingStatus = new HashSet();
  private static final Set inactiveStatus = new HashSet();
  
  private static final Comparator grantableReportComparator
    = new GrantableReportComparator();
  
  private static final Map dateFormatMap = new HashMap();

  /**
   *  
   */
  static
  /* runs at class load time */
  {
    activeAndPendingStatus.add(Status.ACTIVE);
    activeAndPendingStatus.add(Status.PENDING);
    
    inactiveStatus.add(Status.INACTIVE);
  }
  
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
  
  private static SimpleDateFormat getDateFormat(String formatStr)
  {
    SimpleDateFormat formatter
      = (SimpleDateFormat)(dateFormatMap.get(formatStr));
    
    if (formatter == null)
    {
      formatter = new SimpleDateFormat(formatStr);
      dateFormatMap.put(formatStr, formatter);
    }
    
    return formatter;
  }
  
  public static String displayDatetime(String formatStr, Date datetimeVal)
  {
    SimpleDateFormat formatter = getDateFormat(formatStr);
    String dateStr = formatter.format(datetimeVal);
    
    // Here's a hack: If the dateStr includes "AM" or "PM", then make those
    // characters lower-case.
    
    dateStr = dateStr.replaceAll("AM", "am");
    dateStr = dateStr.replaceAll("PM", "pm");

    return dateStr;
  }
  
  public static String describeChange
    (AssignmentHistory[]  historyArray,
     int                  historyIndex)
  {
    StringBuffer changeStr = new StringBuffer();
    AssignmentHistory newerHistoryRecord = historyArray[historyIndex];
    AssignmentHistory olderHistoryRecord = null;
    
    if (historyIndex != (historyArray.length - 1))
    {
      olderHistoryRecord = historyArray[historyIndex + 1];
    }

    StringBuffer editorDescription = describeEditor(newerHistoryRecord);
    
    Difference diff = diff(newerHistoryRecord, olderHistoryRecord);
    
    if (diff.equals(Difference.GRANT))
    {
      changeStr.append("Granted by ");
      changeStr.append(editorDescription);
    }
    else if (diff.equals(Difference.REVOKE))
    {
      changeStr.append("Revoked by ");
      changeStr.append(editorDescription);
    }
    else
    {
      changeStr.append("Modified by ");
      changeStr.append(editorDescription);
      changeStr.append
        (describeModifications(newerHistoryRecord, olderHistoryRecord));
    }
    
    return changeStr.toString();
  }
  
  private static StringBuffer describeChange
    (String label,
     String timeWord,
     String nullDescription,
     Date   newer,
     Date   older)
  {
    StringBuffer description = new StringBuffer();

    // Include the simple "!=" test to account for two null Date values.
    if ((older != newer)
        && ((older == null) || (newer == null) || !older.equals(newer)))
    {
      description.append("<p>\n");
      description.append("  <span class=\"status\">\n");
      description.append("    changed\n");
      description.append("  </span>\n");
      description.append("  <span class=\"label\">\n");
      description.append(     label);
      description.append(     " from\n");
      description.append("  </span>\n");
      description.append(     "'");
      description.append(     timeWord);
      description.append(     " ");
      description.append(     older == null
                                ? nullDescription
                                : displayDatetime
                                    (Constants.DATETIME_FORMAT_24_DAY,
                                     older));
      description.append(     "'\n");
      description.append("  <span class=\"label\">\n");
      description.append("    to\n");
      description.append("  </span>\n");
      description.append(     "'");
      description.append(     timeWord);
      description.append(     " ");
      description.append(     newer == null
                                ? nullDescription
                                : displayDatetime
                                    (Constants.DATETIME_FORMAT_24_DAY,
                                     newer));
      description.append(     "'\n");
      description.append("</p>\n");
    }
    
    return description;
  }
  
  private static StringBuffer describeModifications
    (History newer,
     History older)
  {
    StringBuffer description = new StringBuffer();
    description.append
      (describeChange
        ("Effective Date",
         "",
         "immediate",
         newer.getEffectiveDate(),
         older.getEffectiveDate()));
    description.append
      (describeChange
        ("Duration",
         "until",
         "revoked",
         newer.getExpirationDate(),
         older.getExpirationDate()));
    
    if (newer instanceof AssignmentHistory)
    {
      description.append
        (describeChange
          ("Can Use",
           ((AssignmentHistory)newer).canUse(),
           ((AssignmentHistory)older).canUse()));
      description.append
        (describeChange
          ("Can Grant",
           ((AssignmentHistory)newer).canGrant(),
           ((AssignmentHistory)older).canGrant()));

//      description.append
//        (describeLimitChanges
//          (newer.getLimitValues(), older.getLimitValues()));
    }
    else
    {
      description.append
        (describeChange
          ("Can Use",
           ((ProxyHistory)newer).canUse(),
           ((ProxyHistory)older).canUse()));
      description.append
        (describeChange
          ("Can Extend",
           ((ProxyHistory)newer).canExtend(),
           ((ProxyHistory)older).canExtend()));
    }
    
    return description;
  }

  private static StringBuffer describeChange
    (String label,
     boolean newer,
     boolean older)
  {
    StringBuffer description = new StringBuffer();
    
    if (newer != older)
    {
      description.append("<p>\n");
      description.append("  <span class=\"status\">\n");
      description.append("    changed\n");
      description.append("  </span>\n");
      description.append("  <span class=\"label\">\n");
      description.append(     label);
      description.append("    from\n");
      description.append("  </span>\n");
      description.append("    '");
      description.append(     older);
      description.append(     "'\n");
      description.append("  <span class=\"label\">\n");
      description.append("    to\n");
      description.append("  </span>\n");
      description.append("    '");
      description.append(     newer);
      description.append(     "'\n");
      description.append("</p>\n");
    }
    
    return description;
  }

  private static StringBuffer describeEditor(History history)
  {
    StringBuffer description = new StringBuffer();
    
    if (history.getProxySubject() == null)
    {
      description.append(history.getGrantor().getName());
    }
    else
    {
      description.append(history.getProxySubject().getName());
      description.append(", acting as ");
      description.append(history.getGrantor().getName());
    }
    
    return description;
  }
  
  public static String describeChange
    (ProxyHistory[]  historyArray,
     int             historyIndex)
  {
    StringBuffer changeStr = new StringBuffer();
    ProxyHistory newerHistoryRecord = historyArray[historyIndex];
    ProxyHistory olderHistoryRecord = null;
    
    if (historyIndex != (historyArray.length - 1))
    {
      olderHistoryRecord = historyArray[historyIndex + 1];
    }
    
    StringBuffer editorDescription = describeEditor(newerHistoryRecord);
    
    Difference diff = diff(newerHistoryRecord, olderHistoryRecord);
    
    if (diff.equals(Difference.GRANT))
    {
      changeStr.append("Granted by ");
      changeStr.append(editorDescription);
    }
    else if (diff.equals(Difference.REVOKE))
    {
      changeStr.append("Revoked by ");
      changeStr.append(editorDescription);
    }
    else
    {
      changeStr.append("Modified by ");
      changeStr.append(editorDescription);
      changeStr.append
        (describeModifications(newerHistoryRecord, olderHistoryRecord));
    }
    
    return changeStr.toString();
  }
  
  static private Difference diff
    (History newer,
     History older)
  {
    Difference diff;
    
    if (older == null)
    {
      // If it's the first record, it must be the initial grant.
      diff = Difference.GRANT;
    }
    else if (older.getStatus().equals(Status.ACTIVE)
             && newer.getStatus().equals(Status.INACTIVE))
    {
      diff = Difference.REVOKE;
    }
    else
    {
      diff = Difference.MODIFY;
    }
    
    return diff;
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
   * Formats Assignment-limit-values like this:
   *     <span class="label">Approval limit:<span> $100
   * 
   * Note that the colon is inside the span. The space between colon and
   * value can be inside or outside, whichever is easier.
   * 
   * @param grantable
   * @return
   */
  public static String displayLimitValues(Grantable grantable)
  {
    String outStr;
    
    if (grantable instanceof Assignment)
    {
      Assignment assignment = (Assignment)grantable;
      Set limits = assignment.getFunction().getLimits();
      Set limitValues = assignment.getLimitValues();
    
      outStr = displayLimitValues(limits, limitValues);
    }
    else
    {
      Proxy proxy = (Proxy)grantable;
      outStr
        = "<span class=\"label\">"
          + displayLimitType(proxy)
          + " </span>"
          + displaySubsystem(proxy);
    }
    
    return outStr;
  }
  
  /**
   * 
   * @param currentLoggedInPrivilegedSubject
   *    The current PrivilegedSubject who's logged in as the user.
   * @param htmlSelectId
   *    The HTML ID which should be used to identify this SELECT element.
   * @param onChange
   *    The name of the JavaScript method which should be invoked when
   *    this SELECT element's "onChange" event is fired. That method will
   *    receive two parameters:
   *      1) The SELECT element's HTML ID.
   *      2) The ID of the current PrivilegedSubject who's being "acted for".
   * @return
   *   A String which represents this SELECT element in HTML.
   */
  public static String displayActingForOptions
    (PrivilegedSubject  currentLoggedInPrivilegedSubject,
     String             htmlSelectId,
     String             onChange)
  {
    StringBuffer outStr = new StringBuffer();
    
    Set proxiesReceived
      = currentLoggedInPrivilegedSubject.getProxiesReceived();
    proxiesReceived = filterProxies(proxiesReceived, Status.ACTIVE);
    
    if (proxiesReceived.size() > 0)
    {    
      outStr.append("<LABEL for=\"" + htmlSelectId + "\">\n");
      outStr.append("  Act in Signet as:\n");
      outStr.append("</LABEL>\n");
      outStr.append("<SELECT\n");
      outStr.append("  name=\"" + htmlSelectId + "\"\n");
      outStr.append("  id=\"" + htmlSelectId + "\"\n");
      outStr.append("  onchange=\"" + onChange + "('" + htmlSelectId  + "', '" + Common.buildCompoundId(currentLoggedInPrivilegedSubject.getEffectiveEditor()) + "');" + "\"\n");
      outStr.append("  class=\"long\">\n");
        
      outStr.append(Common.displayProxyOptions(currentLoggedInPrivilegedSubject));

      outStr.append("</SELECT>\n");
      outStr.append("<INPUT\n");
      outStr.append("  name=\"" + Constants.ACTAS_BUTTON_NAME + "\"\n");
      outStr.append("  id=\"" + Constants.ACTAS_BUTTON_ID + "\"\n");
      outStr.append("  disabled=\"true\"\n");
      outStr.append("  type=\"submit\"\n");
      outStr.append("  class=\"button1\"\n");
      outStr.append("  value=\"Switch\"\n");
      outStr.append("/>\n");

    }
    
    return outStr.toString();
  }
  
  /**
   * 
   * @param pSubject
   * @param actingAs
   * @param actingAsButtonId
   *    The ID of the "acting as" button.
   * @return
   */
  private static String displayProxyOptions
    (PrivilegedSubject pSubject)
  {
    StringBuffer outStr = new StringBuffer();
    
    outStr.append
      ("<OPTION\n");
    
    if (pSubject.equals(pSubject.getEffectiveEditor()))
    {
      // We're acting as no one but ourselves.
      outStr.append
        ("  SELECTED\n");
    }

    outStr.append("  value=\"" + Common.buildCompoundId(pSubject) + "\"\n");
    outStr.append(">\n");
    outStr.append("  myself (" + pSubject.getName() + ")\n");
    outStr.append("</OPTION>\n");
    
    Set proxiesReceived = pSubject.getProxiesReceived();
    proxiesReceived = filterProxies(proxiesReceived, Status.ACTIVE);
    Iterator proxiesReceivedIterator = proxiesReceived.iterator();
    
    // Get a Set of unique Proxy-grantors to choose from.
    Set proxyGrantors = new TreeSet();
    while (proxiesReceivedIterator.hasNext())
    {
      Proxy proxy = (Proxy)(proxiesReceivedIterator.next());
      proxyGrantors.add(proxy.getGrantor());
    }
    
    Iterator proxyGrantorsIterator = proxyGrantors.iterator();
    while (proxyGrantorsIterator.hasNext())
    {
      PrivilegedSubject grantor
        = (PrivilegedSubject)(proxyGrantorsIterator.next());
      boolean isCurrent = (grantor.equals(pSubject.getEffectiveEditor()));
      outStr.append("<OPTION\n");
      if (isCurrent)
      {
        outStr.append(" SELECTED\n");
      }
      
      outStr.append("  value=\"" + Common.buildCompoundId(grantor) + "\"\n");
      outStr.append(">\n");
      outStr.append("  " + grantor.getName() + "\n");
      outStr.append("</OPTION>\n");
    }
    
    return outStr.toString();
  }
  
  public static String displayStatus(Grantable grantable)
  {
    String outStr;
    
    if (grantable instanceof Assignment)
    {
      outStr = displayStatus((Assignment)grantable);
    }
    else
    {
      outStr = displayStatus((Proxy)grantable);
    }
    
    return outStr;
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
  
  public static String displayStatusForDetailPopup(Grantable grantable)
  {
    StringBuffer statusStr = new StringBuffer();
    
    // An Assignment or Proxy with no ID has not yet been persisted. An
    // Assignment or Proxy that has not yet been persisted has no Status yet.
    // That is, it's not active, it's not pending, it's not nuthin' yet.
    boolean hasStatus = !(grantable.getId() == null);
    
    if (hasStatus)
    {
      statusStr.append(grantable.getStatus().getName());
    }

    return statusStr.toString();
  }
  
  public static String displayStatus(Proxy proxy)
  {
    StringBuffer statusStr = new StringBuffer();
    
    // A Proxy with no ID has not yet been persisted. A Proxy
    // that has not yet been persisted has no Status yet. That is, it's not
    // active, it's not pending, it's not nuthin' yet.
    boolean hasStatus = !(proxy.getId() == null);
    boolean canUse = proxy.canUse();
    boolean canExtend = proxy.canExtend();
    
    if (hasStatus)
    {
      statusStr.append(proxy.getStatus().getName());
    }
    
    if (hasStatus && canUse)
    {
      statusStr.append(", ");
    }
    
    if (canUse)
    {
      statusStr.append("can use");
    }
    
    if ((hasStatus || canUse) && canExtend)
    {
      statusStr.append(", ");
    }
    
    if (canExtend)
    {
      statusStr.append("can extend");
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
          || (pSubject.getName().toUpperCase().indexOf
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
    (PrivilegedSubject  loggedInPrivilegedSubject,
     Grantable          grantable)
  {
    StringBuffer outStr = new StringBuffer();
    String editAction
      = (grantable instanceof Proxy ? "Designate.do" : "Conditions.do");
    String paramName
      = (grantable instanceof Proxy
          ? Constants.PROXYID_HTTPPARAMNAME
          : "assignmentId");
    
    Decision decision = loggedInPrivilegedSubject.canEdit(grantable);
    boolean canEdit = decision.getAnswer();
    
    // Here's a notable exception: Since the UI cannot grant non-Subsystem-
    // specific Proxies, it cannot edit them either.
    
    if ((grantable instanceof Proxy)
        && (((Proxy)grantable).getSubsystem() == null))
    {
      canEdit = false; 
    }
    
    if (canEdit == true)
    {
      outStr.append("<a\n");
      outStr.append("  style=\"float: right;\"\n");
      outStr.append("  href=\"" + editAction + "?" + paramName + "=" + grantable.getId() + "\">\n");
      outStr.append("  <img\n");
      outStr.append("    src=\"images/arrow_right.gif\"\n");
      outStr.append("      alt=\"\" />\n");
      outStr.append("  edit\n");
      outStr.append("</a>");
    }
    
    return outStr.toString();
  }
  
  public static String paramStr(Grantable grantableInstance)
  {
    String typeStr;
    
    if (grantableInstance instanceof Assignment)
    {
      typeStr = Constants.ASSIGNMENT_HTTPPARAMPREFIX;
    }
    else
    {
      typeStr = Constants.PROXY_HTTPPARAMPREFIX;
    }
    
    return typeStr + grantableInstance.getId();
  }
  
  public static Grantable getGrantableFromParamStr
    (Signet signet, String paramStr)
  throws NumberFormatException, ObjectNotFoundException
  {
    Grantable grantableInstance;
    
    if (paramStr.startsWith(Constants.ASSIGNMENT_HTTPPARAMPREFIX))
    {
      String idStr
        = paramStr.substring(Constants.ASSIGNMENT_HTTPPARAMPREFIX.length());
      grantableInstance = signet.getAssignment(Integer.parseInt(idStr));
    }
    else
    {

      String idStr
        = paramStr.substring(Constants.PROXY_HTTPPARAMPREFIX.length());
      grantableInstance = signet.getProxy(Integer.parseInt(idStr));
    }
    
    return grantableInstance;
  }
  
  public static String revokeBox
    (PrivilegedSubject  loggedInPrivilegedSubject,
     Grantable          grantableInstance,
     UnusableStyle      unusableStyle)
  {
    StringBuffer outStr = new StringBuffer();
    
    Decision decision = loggedInPrivilegedSubject.canEdit(grantableInstance);
    if (decision.getAnswer() == true)
    {
      outStr.append("<td align=\"center\" >\n");
      outStr.append("  <input\n");
      outStr.append("    name=\"revoke\"\n");
      outStr.append("    type=\"checkbox\"\n");
      outStr.append("    id=\"" + paramStr(grantableInstance) + "\"\n");
      outStr.append("    value=\"" + paramStr(grantableInstance) + "\"\n");
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
      outStr.append("    id=\"" + paramStr(grantableInstance) + "\"\n");
      outStr.append("    value=\"" + paramStr(grantableInstance) + "\"\n");
      outStr.append("    disabled=\"true\"\n");
//      outStr.append("    title=\"" + revoker.editRefusalExplanation(assignment, "logged-in user") + "\"");
      outStr.append("/>");
      outStr.append("</td>");
    }
    
    return outStr.toString();
  }
  
  public static String popupIcon(Grantable grantable)
  {
    String outStr;
    
    if (grantable instanceof Assignment)
    {
      outStr = assignmentPopupIcon((Assignment)grantable);
    }
    else
    {
      outStr = proxyPopupIcon((Proxy)grantable);
    }
    
    return outStr;
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
  
  public static String proxyPopupIcon(Proxy proxy)
  {
    StringBuffer outStr = new StringBuffer();
    
    outStr.append("<a\n");
    outStr.append("  style=\"float: right;\"\n");
    outStr.append("  href\n");
    outStr.append("    =\"javascript:openWindow\n");
    outStr.append("        ('Proxy.do?proxyId=" + proxy.getId() + "',\n");
    outStr.append("         'popup',\n");
    outStr.append("         'scrollbars=yes,\n");
    outStr.append("          resizable=yes,\n");
    outStr.append("          width=500,\n");
    outStr.append("          height=250');\">\n");
    outStr.append("  <img\n");
    outStr.append("   src=\"images/maglass.gif\"\n");
    outStr.append("   alt=\"More info about this proxy designation...\" />\n");
    outStr.append("</a>");
  
    return outStr.toString();
  }

  public static String dateSelection
  (HttpServletRequest request,
   String             nameRoot,
   String             title,
   String             noDateLabel,
   String             dateValueLabel,
   Date               defaultDateValue)
  {
    return dateSelection
      (request,
       nameRoot,
       title,
       noDateLabel,
       dateValueLabel,
       defaultDateValue,
       true);  // editable
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
     Date               defaultDateValue,
     boolean            editable)
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
    if (editable == false)
    {
      outStr.append("      disabled=\"disabled\"\n");
    }
    outStr.append(       (defaultDateValue == null ? "checked" : "") + " />\n");
    outStr.append("    " + noDateLabel + "\n");
    outStr.append("  </p>\n");
    outStr.append("  <p>\n");
    outStr.append("    <input\n");
    outStr.append("      name=\"" + radioButtonGroupName + "\"\n");
    outStr.append("      type=\"radio\"\n");
    outStr.append("      value=\"" + YESDATE_VALUE + "\"\n");
    if (editable == false)
    {
      outStr.append("      disabled=\"disabled\"\n");
    }
    outStr.append(       (defaultDateValue != null ? "checked" : "") + " />\n");
    outStr.append("    " + dateValueLabel + "\n");
    outStr.append("    <input\n");
    outStr.append("      name=\"" + nameRoot + DATETEXT_SUFFIX + "\"\n");
    outStr.append("      type=\"text\"\n");
    outStr.append("      class=\"date\"\n");
    outStr.append("      value=\"" + defaultDateStr + "\"\n");
    if (editable == false)
    {
      outStr.append("      disabled=\"disabled\"\n");
    }
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
    Date            date = null;
    ParseException  parseException = null;
    
    // First, let's see if the date is present or not.
    String dateStringPresence = request.getParameter(nameRoot + HASDATE_SUFFIX);
    if ((dateStringPresence == null)
        || dateStringPresence.equals(NODATE_VALUE)
        || dateStringPresence.equals(""))
    {
      return defaultDate;
    }
    
    String dateStr = request.getParameter(nameRoot + DATETEXT_SUFFIX);

    // This date-parsing code was originally written by Craig Jurney for the
    // Sponsorship Manager.
    for (int i = 0; i < DATERS.length && date == null; ++i)
    {
      DATERS[i].setLenient(false);
      
      try
      {
        date = DATERS[i].parse(dateStr);
      }
      catch (java.text.ParseException pe)
      {
        parseException = pe;
        continue;
      }
    }

    if (date == null)
    {
      throw new DataEntryException
        (parseException, nameRoot, dateStr, DATE_SAMPLE);
    }
    
    return date;
  }
  
//  static public String subsystemSelectionMultiple
//    (PrivilegedSubject loggedInPrivilegedSubject,
//     PrivilegedSubject actingAs,
//     String groupName,
//     String onClickAction)
//  {
//    StringBuffer outStr = new StringBuffer();
//
//    PrivilegedSubject pSubject
//      = (actingAs == null ? loggedInPrivilegedSubject : actingAs);
//    Set grantableSubsystems = pSubject.getGrantableSubsystems();
//    
//    Iterator grantableSubsystemsIterator = grantableSubsystems.iterator();
//    while (grantableSubsystemsIterator.hasNext())
//    {
//      Subsystem subsystem = (Subsystem)(grantableSubsystemsIterator.next());
//      outStr.append("<input\n");
//      outStr.append("  name=\"" + groupName + "\"\n");
//      outStr.append("  type=\"checkbox\"\n");
//      
//      if (onClickAction != null)
//      {
//        outStr.append("  onClick=\"" + onClickAction + "\"\n");
//      }
//      
//      outStr.append("  value=\"" + subsystem.getId() + "\" />\n");
//      outStr.append(subsystem.getName() + "\n");
//      outStr.append("<br />\n");
//    }
//    
//    return outStr.toString();
//  }
  
  static public String buildCompoundId(PrivilegedSubject pSubject)
  {
    return
      pSubject.getSubjectTypeId()
      + Constants.COMPOSITE_ID_DELIMITER
      + pSubject.getSubjectId();
  }
  
  static public String[] parseCompoundId(String compoundId)
  {
    StringTokenizer tokenizer
      = new StringTokenizer(compoundId, Constants.COMPOSITE_ID_DELIMITER);
    String subjectTypeId = tokenizer.nextToken();
    String subjectId = tokenizer.nextToken();

    String[] result = {subjectTypeId, subjectId};

    return result;
  }

  static public boolean paramIsPresent(String param)
  {
    if ((param != null) && (param != ""))
    {
      return true;
    }

    return false;
  }
  
  static public PrivilegedSubject getSubjectFromSelectList
    (Signet             signet,
     HttpServletRequest request,
     String             selectListName,
     String             sessionAttrName)
  throws ObjectNotFoundException
  {
    PrivilegedSubject pSubject = null;
    
    String compositeId = request.getParameter(selectListName);
    if (compositeId != null)
    {
      String[] idParts = parseCompoundId(compositeId);
      pSubject = signet.getPrivilegedSubject(idParts[0], idParts[1]);
    }
    
    if (sessionAttrName != null)
    {
      request.getSession().setAttribute(sessionAttrName, pSubject);
    }
    
    return pSubject;
  }
  
  static public PrivilegedSubject getGrantee
    (Signet             signet,
     HttpServletRequest request)
  throws ObjectNotFoundException
  {
    PrivilegedSubject grantee;
    
    // Find the PrivilegedSubject specified by the "grantee" parameters, and
    // stash it in the Session. If those parameters are not present, then
    // it must already be stashed in the Session by some prior action.
    String granteeSubjectTypeId = request.getParameter("granteeSubjectTypeId");
    String granteeSubjectId = request.getParameter("granteeSubjectId");
    if (granteeSubjectId != null)
    {
      grantee
        = signet.getPrivilegedSubject(granteeSubjectTypeId, granteeSubjectId);
      request
        .getSession()
          .setAttribute(Constants.CURRENTPSUBJECT_ATTRNAME, grantee);
    }
    else
    {
      grantee
        = (PrivilegedSubject)
            (request
              .getSession()
                .getAttribute(Constants.CURRENTPSUBJECT_ATTRNAME));
    }
    
    return grantee;
  }
  
  static public Subsystem getSubsystem
    (Signet             signet,
     HttpServletRequest request,
     String             paramName,
     String             attrName)
  throws ObjectNotFoundException
  {    
    Subsystem subsystem = null;
    
    String subsystemId = request.getParameter(paramName);
    if ((subsystemId != null)
        && (!subsystemId.equals(Constants.SUBSYSTEM_PROMPTVALUE)))
    {
      subsystem = signet.getSubsystem(subsystemId);
      request.getSession().setAttribute(attrName, subsystem);
    }
    else
    {
      subsystem
        = (Subsystem)
            (request.getSession().getAttribute(attrName));
    }
    
    return subsystem;
  }
  
  static public PrivDisplayType getAndSetPrivDisplayType
    (HttpServletRequest request,
     String             paramName,
     String             attrName,
     PrivDisplayType    defaultValue)
  {
    PrivDisplayType privDisplayType = null;
    
    String privDisplayTypeName = request.getParameter(paramName);
    if (privDisplayTypeName != null)
    {
      privDisplayType
        = (PrivDisplayType)
            (PrivDisplayType.getInstanceByName(privDisplayTypeName));
    }
    else
    {
      privDisplayType
        = (PrivDisplayType)
            (request.getSession().getAttribute(attrName));
    }
    
    if (privDisplayType == null)
    {
      privDisplayType = defaultValue;
    }

    request.getSession().setAttribute(attrName, privDisplayType);
    return privDisplayType;
  }
  
  static public Subsystem getAndSetSubsystem
    (Signet             signet,
     HttpServletRequest request,
     String             paramName,
     String             attrName,
     Subsystem          defaultValue)
  throws ObjectNotFoundException
  {
    Subsystem subsystem = null;
    
    String subsystemId = request.getParameter(paramName);
    
    // If there's no subsystemID at all, it means we should look for a Subsystem
    // in the Session. If there is a subsystemID, and its value is the four-
    // character string "null", then it denotes the wildcard subsystem. This is
    // not so great, because a Signet installation could have a legitimate
    // Subsystem whose ID is that same four-character string. Also, it's
    // unnecessarily confusing.
    if (subsystemId != null)
    {
      if (subsystemId.equals("null"))
      {
        subsystem = Constants.WILDCARD_SUBSYSTEM;
      }
      else
      {
        subsystem = signet.getSubsystem(subsystemId);
      }
    }
    else
    {
      subsystem
        = (Subsystem)
            (request.getSession().getAttribute(attrName));
    }
    
    if (subsystem == null)
    {
      subsystem = defaultValue;
    }

    request.getSession().setAttribute(attrName, subsystem);
    return subsystem;
  }
  
  static public PrivilegedSubject getAndSetPrivilegedSubject
    (Signet             signet,
     HttpServletRequest request,
     String             paramName,
     String             attrName,
     PrivilegedSubject  defaultValue)
  throws ObjectNotFoundException
  {
    PrivilegedSubject pSubject = null;
    
    String compoundSubjectId = request.getParameter(paramName);
    
    // If there's no compoundSubjectID at all, it means we should look for a
    // PrivilegedSubject in the Session.
    if (compoundSubjectId != null)
    {
      String subjectIdParts[] = parseCompoundId(compoundSubjectId);
      pSubject
        = signet.getPrivilegedSubject(subjectIdParts[0], subjectIdParts[1]);
    }
    else
    {
      pSubject
        = (PrivilegedSubject)
            (request.getSession().getAttribute(attrName));
    }
    
    if (pSubject == null)
    {
      pSubject = defaultValue;
    }

    request.getSession().setAttribute(attrName, pSubject);
    return pSubject;
  }
  
  static public Set getSubsystemSelections
    (Signet signet,
     HttpServletRequest request,
     String             groupName)
  throws ObjectNotFoundException
  {
    Set subsystems = new HashSet();
    String subsystemIds[] = request.getParameterValues(groupName);
    
    for (int i = 0; i < 0; i++)
    {
      Subsystem subsystem = signet.getSubsystem(subsystemIds[i]);
      subsystems.add(subsystem);
    }
    
    return subsystems;
  }
  
  static public String subsystemSelectionSingle
    (String selectName,
     String promptValue,
     String promptText,
     String onClickScript,
     Set    subsystems)
  {
    return subsystemSelectionSingle
      (selectName, promptValue, promptText, onClickScript, subsystems, null);
  }
  
  
  static public String subsystemSelectionSingle
    (String     selectName,
     String     promptValue,
     String     promptText,
     String     onClickScript,
     Set        subsystems,
     Subsystem  selectedSubsystem)
  {
    StringBuffer outStr = new StringBuffer();
    
    outStr.append("<span style=\"white-space: nowrap;\">\n");
    outStr.append("  <!-- keep select & button together -->\n");
    outStr.append("  <select\n");
    outStr.append("    name=\"" + selectName + "\"\n");
    outStr.append("    id=\"" + selectName + "\">\n");

    outStr.append("    <option\n");
    
    if (selectedSubsystem == null)
    {
      outStr.append("      selected=\"selected\"\n");
    }
    
    outStr.append("      value=\"" + promptValue + "\"\n");
    outStr.append("      onclick=\"" + onClickScript + "\">\n");
    outStr.append("      " + promptText + "\n");
    outStr.append("    </option>\n");

    Iterator subsystemsIterator = subsystems.iterator();
    while (subsystemsIterator.hasNext())
    {
      Subsystem subsystem = (Subsystem)(subsystemsIterator.next());

      outStr.append("    <option\n");
      
      if (subsystem.equals(selectedSubsystem))
      {
        outStr.append("      selected=\"selected\"\n");
      }
      
      outStr.append("      value=\"" + subsystem.getId() + "\"\n");
      outStr.append("      onclick=\"" + onClickScript + "\">\n");
      outStr.append("      " + subsystem.getName() + "\n");
      outStr.append("    </option>\n");
    }

    outStr.append("  </select>\n");
    outStr.append("</span>\n");
    
    return outStr.toString();
  }
  
  static public String displayLogoutHref(HttpServletRequest request)
  {
    StringBuffer outStr = new StringBuffer();
    
    PrivilegedSubject loggedInPrivilegedSubject
      = (PrivilegedSubject)
          (request.getSession().getAttribute(Constants.LOGGEDINUSER_ATTRNAME));
    
    outStr.append("<a href=\"NotYetImplemented.do\">\n");
    outStr.append(loggedInPrivilegedSubject.getName());

    if (!loggedInPrivilegedSubject.equals
          (loggedInPrivilegedSubject.getEffectiveEditor()))
    {
      outStr.append
        (" <span id=\"actingas\" class=\"actingas\">acting as "
         + loggedInPrivilegedSubject.getEffectiveEditor().getName()
         + "</span>");
    }
    
    outStr.append(" : Logout\n");
    outStr.append("</a>\n");
    
    return outStr.toString();
  }
  
  static public String displayOption
    (PrivDisplayType option,
     PrivDisplayType currentInEffect)
  {
    StringBuffer outStr = new StringBuffer();
    
    outStr.append("<option");

    if (option == currentInEffect)
    {
      outStr.append(" selected=\"selected\"");
    }

    outStr.append(" value=\"" + option.name + "\"");
    outStr.append(">");
    outStr.append(option.getDescription());
    outStr.append("</option>\n");
    
    return outStr.toString();
  }
  
  static public String subsystemLinks
    (PrivilegedSubject  pSubject,
     PrivDisplayType    privDisplayType,
     Subsystem          currentSubsystem)
  {
    Set subsystems;
    Set proxies;
    Set assignments;
    
    if (privDisplayType.equals(PrivDisplayType.CURRENT_GRANTED))
    {
      proxies = pSubject.getProxiesGranted();
      proxies = filterProxies(proxies, Status.ACTIVE);
      
      assignments = pSubject.getAssignmentsGranted();
      assignments = filterAssignments(assignments, Status.ACTIVE);
    }
    else if (privDisplayType.equals(PrivDisplayType.FORMER_GRANTED))
    {
      proxies = pSubject.getProxiesGranted();
      proxies = filterProxies(proxies, Status.INACTIVE);
      
      assignments = pSubject.getAssignmentsGranted();
      assignments = filterAssignments(assignments, Status.INACTIVE);
    }
    else if (privDisplayType.equals(PrivDisplayType.CURRENT_RECEIVED))
    {
      proxies = pSubject.getProxiesReceived();
      proxies = filterProxies(proxies, Status.ACTIVE);
      
      assignments = pSubject.getAssignmentsReceived();
      assignments = filterAssignments(assignments, Status.ACTIVE);
    }
    else if (privDisplayType.equals(PrivDisplayType.FORMER_RECEIVED))
    {
      proxies = pSubject.getProxiesReceived();
      proxies = filterProxies(proxies, Status.INACTIVE);
      
      assignments = pSubject.getAssignmentsReceived();
      assignments = filterAssignments(assignments, Status.INACTIVE);
    }
    else
    {
        throw new IllegalArgumentException
          ("This method needs to be extended to support a PrivDisplayType"
           + " value of '"
           + privDisplayType
           + "'");
    }
    
    subsystems = getSubsystems(assignments, proxies);
    
    StringBuffer outStr = new StringBuffer();
    outStr.append(subsystemLink(Constants.WILDCARD_SUBSYSTEM, currentSubsystem));
    
    Iterator subsystemsIterator = subsystems.iterator();
    while (subsystemsIterator.hasNext())
    {
      outStr.append(" | ");
      Subsystem subsystem = (Subsystem)(subsystemsIterator.next());
      outStr.append(subsystemLink(subsystem, currentSubsystem));
    }
    
    return outStr.toString();
  }
  
  private static String subsystemLink
    (Subsystem subsystem,
     Subsystem currentSubsystem)
  {
    StringBuffer outStr = new StringBuffer();
    String name;
    
    if (subsystem.equals(Constants.WILDCARD_SUBSYSTEM))
    {
      name = "All";
    }
    else
    {
      name = subsystem.getName();
    }

    if (subsystem.equals(currentSubsystem))
    {
      outStr.append("<b>");
      outStr.append(name);
      outStr.append("</b>");
    }
    else
    {
      outStr.append("<a href=\"PersonView.do?");
      outStr.append(Constants.SUBSYSTEM_HTTPPARAMNAME);
      outStr.append("=");
      outStr.append(subsystem.getId());
      outStr.append("\">");
      outStr.append(name);
      outStr.append("</a>");
    }
    
    return outStr.toString();
  }
  
  // Tosses out any NULL Subsystems.
  private static Set getSubsystems
    (Set assignments,
     Set proxies)
  {
    Set subsystems = new TreeSet(new SubsystemNameComparator());
    
    Iterator assignmentsIterator = assignments.iterator();
    while (assignmentsIterator.hasNext())
    {
      Assignment assignment = (Assignment)(assignmentsIterator.next());
      subsystems.add(assignment.getFunction().getSubsystem());
    }
    
    Iterator proxiesIterator = proxies.iterator();
    while (proxiesIterator.hasNext())
    {
      Proxy proxy = (Proxy)(proxiesIterator.next());
      
      if (proxy.getSubsystem() != null)
      {
        subsystems.add(proxy.getSubsystem());
      }
    }
    
    return subsystems;
  }
  
  public static String displayLimitType(Proxy proxy)
  {
    String displayStr;
    
    if (proxy.canExtend() && proxy.canUse())
    {
      displayStr = "Extend proxy for and grant privileges in";
    }
    else if (proxy.canExtend())
    {
      displayStr = "Extend proxy for";
    }
    else
    {
      displayStr = "Grant privileges in";
    }
    
    return displayStr;
  }
  
  public static String displaySubsystem(Proxy proxy)
  {
    String    displayStr;
    Subsystem subsystem = proxy.getSubsystem();
    
    if ((subsystem == null) || (subsystem == Constants.WILDCARD_SUBSYSTEM))
    {
      displayStr = "any subsystem";
    }
    else
    {
      displayStr = subsystem.getName();
    }
    
    return displayStr;
  }
  
  private static Set getAssignmentsGrantedForReport
    (PrivilegedSubject  pSubject,
     Subsystem          subsystem,
     Set                statusSet)
  {
    Set assignments = pSubject.getAssignmentsGranted();
    assignments = filterAssignments(assignments, statusSet);
    assignments = filterAssignments(assignments, subsystem);
    
    return assignments;
  }
  
  private static Set getAssignmentsReceivedForReport
    (PrivilegedSubject  pSubject,
     Subsystem          subsystem,
     Set                statusSet)
  {
    Set assignments = pSubject.getAssignmentsReceived();
    assignments = filterAssignments(assignments, statusSet);
    assignments = filterAssignments(assignments, subsystem);
    
    return assignments;
  }
  
  private static Set getProxiesGrantedForReport
    (PrivilegedSubject  pSubject,
     Subsystem          subsystem,
     Set                statusSet)
  {
    Set proxies = pSubject.getProxiesGranted();
    proxies = filterProxies(proxies, statusSet);
    proxies = filterProxies(proxies, subsystem);
    
    return proxies;
  }
  
  private static Set getProxiesReceivedForReport
    (PrivilegedSubject  pSubject,
     Subsystem          subsystem,
     Set                statusSet)
  {
    Set proxies = pSubject.getProxiesReceived();
    proxies = filterProxies(proxies, statusSet);
    proxies = filterProxies(proxies, subsystem);
    
    return proxies;
  }
  
  public static SortedSet getGrantablesForReport
    (PrivilegedSubject  pSubject,
     Subsystem          subsystemFilter,
     PrivDisplayType    privDisplayType)
  {
    SortedSet grantables = new TreeSet(grantableReportComparator);
    
    if (privDisplayType.equals(PrivDisplayType.CURRENT_GRANTED))
    {
      grantables.addAll
        (getAssignmentsGrantedForReport
          (pSubject, subsystemFilter, activeAndPendingStatus));

      grantables.addAll
        (getProxiesGrantedForReport
            (pSubject, subsystemFilter, activeAndPendingStatus));
    }
    else if (privDisplayType.equals(PrivDisplayType.CURRENT_RECEIVED))
    {
      grantables.addAll
        (getAssignmentsReceivedForReport
          (pSubject, subsystemFilter, activeAndPendingStatus));

      grantables.addAll
        (getProxiesReceivedForReport
          (pSubject, subsystemFilter, activeAndPendingStatus));
    }
    else if (privDisplayType.equals(PrivDisplayType.FORMER_GRANTED))
    {
      grantables.addAll
        (getAssignmentsGrantedForReport
          (pSubject, subsystemFilter, inactiveStatus));

      grantables.addAll
        (getProxiesGrantedForReport
            (pSubject, subsystemFilter, inactiveStatus));
    }
    else if (privDisplayType.equals(PrivDisplayType.FORMER_RECEIVED))
    {
      grantables.addAll
        (getAssignmentsReceivedForReport
          (pSubject, subsystemFilter, inactiveStatus));

      grantables.addAll
        (getProxiesReceivedForReport
          (pSubject, subsystemFilter, inactiveStatus));
    }
    else
    {
      throw new IllegalArgumentException
        ("Unrecognized PrivDisplayTypeValue: " + privDisplayType);
    }
  
    return grantables;
  }
  
  public static Set removeGroups(Set setWithGroups)
  {
    Set setWithoutGroups = new HashSet();
    Iterator setWithGroupsIterator = setWithGroups.iterator();
    while (setWithGroupsIterator.hasNext())
    {
      PrivilegedSubject candidate
        = (PrivilegedSubject)(setWithGroupsIterator.next());
      
      if (!(candidate.getSubjectTypeId().equals
             (SubjectTypeEnum.GROUP.getName())))
      {
        setWithoutGroups.add(candidate);
      }
    }
    
    return setWithoutGroups;
  }
  
  public static String proxyPrivilegeDisplayName(Signet signet, Proxy proxy)
  {
    String displayName = "Proxy";
    
    if (proxy.getGrantor().equals(signet.getSignetSubject()))
    {
      if (proxy.getSubsystem() == null)
      {
        displayName = "System Administrator";
      }
      else
      {
        displayName = "Subsystem Owner";
      }
    }
    
    return displayName;
  }
  
  private static String timeWord
    (PrivDisplayType  type,
     boolean          initialCap)
  {
    if ((type.equals(PrivDisplayType.CURRENT_GRANTED)
        || (type.equals(PrivDisplayType.CURRENT_RECEIVED))))
    {
      return (initialCap ? "Current" : "current");
    }
    else if ((type.equals(PrivDisplayType.FORMER_GRANTED)
             || (type.equals(PrivDisplayType.FORMER_RECEIVED))))
    {
      return (initialCap ? "Former" : "former");
    }
    
    return ("UNRECOGNIZED: PrivilegedDisplayType.getName()='"
            + type.getName()
            + "'");
  }
  
  private static String directionPhrase(PrivDisplayType type)
  {
    if (type.equals(PrivDisplayType.CURRENT_GRANTED)
        || type.equals(PrivDisplayType.FORMER_GRANTED))
    {
      return "assigned by" ;
    }
    else if (type.equals(PrivDisplayType.CURRENT_RECEIVED)
             || type.equals(PrivDisplayType.FORMER_RECEIVED))
    {
      return "assigned to";
    }
    
    return ("UNRECOGNIZED: PrivilegedDisplayType.getName()='"
            + type.getName()
            + "'");
  }
  
  public static String titleForPrintReport
    (Subsystem          subsystem,
     PrivDisplayType    privDisplayType,
     PrivilegedSubject  pSubject)
  {
    StringBuffer outStr = new StringBuffer();
    
    if (subsystem == Constants.WILDCARD_SUBSYSTEM)
    {
      outStr.append("All ");
      outStr.append(timeWord(privDisplayType, false));
    }
    else
    {
      outStr.append(timeWord(privDisplayType, true));
      outStr.append(" ");
      outStr.append(subsystem.getName());
    }

    outStr.append(" privileges ");
    outStr.append(directionPhrase(privDisplayType));
    outStr.append(" ");
    outStr.append(pSubject.getName());
    
    return outStr.toString();
  }
  
  static Set filterProxies(Set all, Status status)
  {
    Set statusSet = new HashSet();
    statusSet.add(status);
    return filterProxies(all, statusSet);
  }
  
  static Set filterAssignments(Set all, Status status)
  {
    Set statusSet = new HashSet();
    statusSet.add(status);
    return filterAssignments(all, statusSet);
  }

  static Set filterAssignments(Set all, Set statusSet)
  {
    if (statusSet == null)
    {
      return all;
    }

    Set subset = new HashSet();
    Iterator iterator = all.iterator();
    while (iterator.hasNext())
    {
      Assignment candidate = (Assignment) (iterator.next());
      if (statusSet.contains(candidate.getStatus()))
      {
        subset.add(candidate);
      }
    }

    return subset;
  }

  static Set filterAssignments(Set all, Subsystem subsystem)
  {
    if (subsystem == null)
    {
      return all;
    }

    Set subset = new HashSet();
    Iterator iterator = all.iterator();
    while (iterator.hasNext())
    {
      Assignment candidate = (Assignment) (iterator.next());
      if (candidate.getFunction().getSubsystem().equals(subsystem))
      {
        subset.add(candidate);
      }
    }

    return subset;
  }

  static Set filterProxies(Set all, Set statusSet)
  {
    if (statusSet == null)
    {
      return all;
    }

    Set subset = new HashSet();
    Iterator iterator = all.iterator();
    while (iterator.hasNext())
    {
      Proxy candidate = (Proxy) (iterator.next());
      if (statusSet.contains(candidate.getStatus()))
      {
        subset.add(candidate);
      }
    }

    return subset;
  }

  static Set filterProxies(Set all, Subsystem subsystem)
  {
    if (subsystem == null)
    {
      return all;
    }

    Set subset = new HashSet();
    Iterator iterator = all.iterator();
    while (iterator.hasNext())
    {
      Proxy candidate = (Proxy) (iterator.next());
      if ((candidate.getSubsystem() == null)
          || candidate.getSubsystem().equals(subsystem))
      {
        subset.add(candidate);
      }
    }

    return subset;
  }
  
  public static String privilegeStr
    (Signet     signet,
     Grantable  grantable)
  {
    String privilegeStr;
    
    if (grantable instanceof Assignment)
    {
      Assignment assignment = (Assignment)grantable;
      privilegeStr
        = assignment.getFunction().getSubsystem().getName()
          + " : "
          + assignment.getFunction().getCategory().getName()
          + " : "
          + assignment.getFunction().getName();
    }
    else
    {
      Proxy proxy = (Proxy)grantable;
      privilegeStr = proxyPrivilegeDisplayName(signet, proxy);
    }
    
    return privilegeStr;
  }
  
  public static String scopeStr(Grantable grantable)
  {
    String scopeStr;
    
    if (grantable instanceof Assignment)
    {
      Assignment assignment = (Assignment)grantable;
      scopeStr = assignment.getScope().getName();
    }
    else
    {
      Proxy proxy = (Proxy)grantable;
      scopeStr
        = "<span class=\"label\">acting as </span>"
          + proxy.getGrantor().getName();
    }
      
    return scopeStr;
  }
  
//  public static Set getExtensibleProxies
//    (PrivilegedSubject proxyGrantor,
//     PrivilegedSubject proxyGrantee)
//  {
//System.out.println("DEBUG: ENTERING Common.getExtensibleProxies(): proxyGrantor='" + proxyGrantor + "', proxyGrantee='" + proxyGrantee + "'");
//    Set proxies
//      = proxyGrantee.getProxiesReceived(Status.ACTIVE, null, proxyGrantor);
//    Set extensibleProxies = new HashSet();
//    
//    Iterator proxiesIterator = proxies.iterator();
//    while (proxiesIterator.hasNext())
//    {
//      Proxy proxy = (Proxy)(proxiesIterator.next());
//      if (proxy.canExtend())
//      {
//        extensibleProxies.add(proxy);
//      }
//    }
//    
//    return extensibleProxies;
//  }
  
//  public static Set getSubsystemsFromProxies(Set proxies)
//  {
//System.out.println("DEBUG: ENTERING Common.getSubsystemsFromProxies(): proxies=" + proxies);
//    Set subsystems = new HashSet();
//    
//    Iterator proxiesIterator = proxies.iterator();
//    while (proxiesIterator.hasNext())
//    {
//      Proxy proxy = (Proxy)(proxiesIterator.next());
//      
//      if (proxy.getSubsystem() == null)
//      {
//        subsystems.addAll(proxy.getGrantor().getGrantableSubsystemsForProxy());
//      }
//      subsystems.add(proxy.getSubsystem());
//    }
//System.out.println("DEBUG: EXITING Common.getSubsystemsFromProxies(): subsystems=" + subsystems);
//    return subsystems;
//  }
}
