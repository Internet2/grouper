<!--
  $Id: limits.jsp,v 1.3 2005-02-24 01:09:39 jvine Exp $
  $Date: 2005-02-24 01:09:39 $
  
  Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
  Licensed under the Signet License, Version 1,
  see doc/license.txt in this distribution.
-->

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
    <meta name="robots" content="noindex, nofollow" />
    <title>
      Signet
    </title>
    <link href="styles/signet.css" rel="stylesheet" type="text/css" />
    <script language="JavaScript" type="text/javascript" src="scripts/signet.js">
    </script>
  </head>

  <body>

<%@ page import="java.text.DateFormat" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Set" %>

<%@ page import="edu.internet2.middleware.signet.PrivilegedSubject" %>
<%@ page import="edu.internet2.middleware.signet.Subsystem" %>
<%@ page import="edu.internet2.middleware.signet.Category" %>
<%@ page import="edu.internet2.middleware.signet.Assignment" %>
<%@ page import="edu.internet2.middleware.signet.Function" %>
<%@ page import="edu.internet2.middleware.signet.tree.TreeNode" %>
<%@ page import="edu.internet2.middleware.signet.Signet" %>

<% 
  Signet signet
     = (Signet)
         (request.getSession().getAttribute("signet"));
         
  PrivilegedSubject loggedInPrivilegedSubject
     = (PrivilegedSubject)
         (request.getSession().getAttribute("loggedInPrivilegedSubject"));
   
   PrivilegedSubject currentGranteePrivilegedSubject
     = (PrivilegedSubject)
         (request.getSession().getAttribute("currentGranteePrivilegedSubject"));
         
   Subsystem currentSubsystem
     = (Subsystem)
         (request.getSession().getAttribute("currentSubsystem"));
         
   Category currentCategory
     = (Category)
         (request.getSession().getAttribute("currentCategory"));
         
   Function currentFunction
     = (Function)
         (request.getSession().getAttribute("currentFunction"));
         
   TreeNode currentScope
     = (TreeNode)
         (request.getSession().getAttribute("currentScope"));
         
   DateFormat dateFormat = DateFormat.getDateInstance();
   
   String personViewHref
     = "PersonView.do?granteeSubjectTypeId="
       + currentGranteePrivilegedSubject.getSubjectTypeId()
       + "&granteeSubjectId="
       + currentGranteePrivilegedSubject.getSubjectId();
       
   String functionsHref
     = "Functions.do?select="
       + currentSubsystem.getId();
       
   String orgBrowseHref
   	= "OrgBrowse.do?step3="
   		+ currentFunction.getId();
%>

    <form name="form1" action="Confirm.do">
      <div id="Header">  
        <div id="Logo">
        <img src="images/organisation-logo.jpg" width="83" height="60" alt="logo" />        </div> 
        <!-- Logo -->
        <div id="Signet">
					<img src="images/signet.gif" alt="Signet" height="60" width="49">
        </div> <!-- Signet -->
      </div> <!-- Header -->
      
      <div id="Navbar">
        <span class="logout">
          <a href="NotYetImplemented.do">
            <%=loggedInPrivilegedSubject.getName()%>: Logout
          </a>
        </span> <!-- logout -->
        <span class="select">
          <a href="Start.do">
            Home
          </a>
          >  <!-- This single right-angle-bracket is just a text element, not an HTML token. -->
          <a href="<%=personViewHref%>"
            >  <!-- This single right-angle-bracket is just a text element, not an HTML token. -->
            <%=currentGranteePrivilegedSubject.getName()%>
          </a>
          &gt; Grant new privilege
        </span> <!-- select -->
      </div>  <!-- Navbar -->
      
      <div id="Layout">
        <div id="Content">
          <div id="ViewHead">
						Granting new privilege to
           	<h1>
             	<%=currentGranteePrivilegedSubject.getName()%>
     	    	</h1>
     	    	<span class="dropback"><%=currentGranteePrivilegedSubject.getDescription()%></span><!--,	Technology Strategy and Support Operations-->
         	</div>

         	<div class="section">
          	<h2>
           	New <%=currentSubsystem.getName()%> privilege
         		</h2>
          	<ul class="none">
             	<li>
               	<%=currentCategory.getName()%>
               	<ul class="arrow">
                 	<li>
                   	<%=currentFunction.getName()%>
                 	</li>
               	</ul>
             	</li>
           	</ul>
           	<input
                name="Button"
                type="button"
                class="button2"
                onclick=(parent.location='<%=functionsHref%>')
                value="&lt;&lt; Change privilege" />
         	</div>
					            	

         	
         	<div class="section">
         		<h2>
           	Scope
         		</h2>
           	<ul class="none">
              	
              <%=signet.displayAncestry
                    (currentScope,
                     "<ul class=\"arrow\">\n",  // childSeparatorPrefix
                     "\n<li>\n",                // levelPrefix
                     "\n</li>\n",               // levelSuffix
                     "\n</ul>")                 // childSeparatorSuffix
                 %>
              	
            </ul>
            	
            <input
                name="Button"
                type="button"
                class="button2"
                onclick=(parent.location='<%=orgBrowseHref%>')
                value="&lt;&lt; Change scope" />
         	</div>
         	<!-- section -->
          	
          <!--
          

          <div class="section">	
          <h2>
            Limits
          </h2>
            <fieldset>
              <legend>
                Approval limit
              </legend>
              <p>
                Metadata explanatory text goes here
              </p>
              <blockquote>
                <select class="money" name="money">
                  <option selected>$500</option>
                  <option>$1000</option>
                  <option>$1500</option>
                  <option>$5000</option>
                  <option>$10000</option>
                  <option>$25000</option>
                  <option>$50000</option>
                  <option>$100000</option>
                </select>
              </blockquote>
            </fieldset>
            <fieldset>
              <legend>
                Student type
              </legend>
              <p>
                Metadata explanatory text goes here
              </p>
              <blockquote>
                <input name="ug" type="checkbox" value="undergraduate" />
                <label for="ug">
                  Undergraduate
                </label>
                <br />
                <input name="grad" type="checkbox" value="graduate" />
                <label for="grad">
                  Graduate
                </label>
              </blockquote>
            </fieldset>
          </div>
          
-->
		 	
         	<div class="section">
          <h2>
           	Conditions
         	</h2>
		<!--
            <fieldset>
              <legend>
                Privilege will be effective:
              </legend>
              <blockquote>  		
              <p>
                <select name="2day">
                  <option value="none"></option>
                  <option>1</option>
                  <option>2</option>
                  <option>3</option>
                  <option>4</option>
                  <option>5</option>
                  <option>6</option>
                  <option>7</option>
                  <option>8</option>
                  <option>9</option>
                  <option>10</option>
                  <option>11</option>
                  <option>12</option>
                  <option>13</option>
                  <option>14</option>
                  <option>15</option>
                  <option>16</option>
                  <option>17</option>
                  <option>18</option>
                  <option>19</option>
                  <option>20</option>
                  <option>21</option>
                  <option>22</option>
                  <option>23</option>
                  <option>24</option>
                  <option selected="selected">25</option>
                  <option>26</option>
                  <option>27</option>
                  <option>28</option>
                  <option>29</option>
                  <option>30</option>
                  <option>31</option>
                </select>
                <select name="2month">
                  <option value="none"></option>
                  <option>January</option>
                  <option>February</option>
                  <option>March</option>
                  <option>April</option>
                  <option>May</option>
                  option>June</option>
                  <option>July</option>
                  <option>August</option>
                  <option selected="selected">September</option>
                  <option>October</option>
                  <option>November</option>
                  <option>December</option>
                </select>
                <select name="2year">
                  <option value="none"></option>
                  <option selected="selected">2004</option>
                  <option>2005</option>
                  <option>2006</option>
                  <option>2007</option>
                  <option>2008</option>
                </select>
              </p>
              <p>
                <span>
                  <a href="javascript:openWindow('prerequisites.html','popup','scrollbars=yes,resizable=yes,width=500,height=400');" class="status">
                    Prerequisites
                  </a>
                </span>
                apply.
              </p>
            </blockquote>
 	</fieldset>
 	<fieldset>
 	  <legend>
 	    Duration:
 	  </legend>
 	  <blockquote>
 	    <input name="radiobutton" type="radio" value="radiobutton" checked="checked" />
  	    while 
  	    <select name="2month">
  	      <option>actively enrolled</option>
  	      <option selected="selected">employed at KITN</option>
  	      <option>in &lt;name&gt; department</option>
  	    </select>
  	    <br />
  	    <label for="2month">
  	      <input name="radiobutton" type="radio" value="radiobutton" />
  	      until:
  	    </label>
  	    <select name="2day">
  	    <option value="none" selected="selected"></option>
  	    <option>1</option>
  	    <option>2</option>
  	    <option>3</option>
  	    <option>4</option>
  	    <option>5</option>
  	    <option>6</option>
  	    <option>7</option>
  	    <option>8</option>
  	    <option>9</option>
  	    <option>10</option>
  	    <option>11</option>
  	    <option>12</option>
  	    <option>13</option>
  	    <option>14</option>
  	    <option>15</option>
  	    <option>16</option>
  	    <option>17</option>
  	    <option>18</option>
  	    <option>19</option>
  	    <option>20</option>
  	    <option>21</option>
  	    <option>22</option>
  	    <option>23</option>
  	    <option>24</option>
  	    <option>25</option>
  	    <option>26</option>
  	    <option>27</option>
  	    <option>28</option>
  	    <option>29</option>
  	    <option>30</option>
  	    <option>31</option>
  	  </select>
  	  <select name="2month">
  	    <option value="none"></option>
  	    <option>January</option>
  	    <option>February</option>
  	    <option>March</option>
  	    <option>April</option>
  	    <option>May</option>
  	    <option>June</option>
  	    <option>July</option>
  	    <option>August</option>
  	    <option>September</option>
  	    <option>October</option>
  	    <option>November</option>
  	    <option>December</option>
  	  </select>
  	  <select name="2year">
  	    <option value="none"></option>
  	    <option>2004</option>
  	    <option>2005</option>
  	    <option>2006</option>
  	    <option>2007</option>
  	    <option>2008</option>
  	  </select>
        </blockquote>
      </fieldset>
      
-->
      		
      <fieldset>
     		<legend>
     		Privilege holder can:
   		</legend>
     		<blockquote>
       		<input name="can_use" type="checkbox" value="checkbox" checked="checked" />
       		use this privilege
       		<br />
       		<input name="can_grant" type="checkbox" value="checkbox" />
       		grant this privilege to others
     		</blockquote>
      		</fieldset>
      		</div><div class="section">
        		<input
          name="Button"
          type="submit"
          class="button-def"
          value="Complete assignment" />
      		<p>
        		<a href="<%=personViewHref%>">
         		<img src="images/icon_arrow_left.gif" width="16" height="16" class="icon" />CANCEL and return to <%=currentGranteePrivilegedSubject.getName()%>'s view
        		</a>
      		</p>
            		</div>
            	<jsp:include page="footer.jsp" flush="true" />
		</div>
		<div id="Sidebar">
  <div class="helpbox">
   	<h2>Help</h2>
   	<!-- actionheader -->
   	<jsp:include page="grant-help.jsp" flush="true" />          
	</div>  <!-- end helpbox -->
</div>
</div>	
</form>
</body>
</html>
