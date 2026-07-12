---
title: "Grouper UI csrf xsrf prevention for Grouper developers"
space: Grouper
pageId: 28549531
version: 2
lastUpdated: 2026-07-01T05:41:49.373Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549531/Grouper+UI+csrf+xsrf+prevention+for+Grouper+developers
---

These instructions install [OWASP CSRF guard](https://www.owasp.org/index.php/Category:OWASP_CSRFGuard_Project) in the Grouper UI (only Grouper developers need to be concerned with this, since this is built in to grouper). These instructions are intended for Grouper v2.1, though it will probably work on v2.0, and could be adapted for previous versions as well.

1. Put the jar(s) in the UI pom

2. Map the filter in CommonServletContainerInitializer

3. Create conf/Owasp.CsrfGuard.properties (which will go in WEB-INF/classes/Owasp.CsrfGuard.properties), which is the default properties file (extract from csrfguard.jar from META-INF/csrfguard.properties

4. Create conf/Owasp.CsrfGuard.overlay.properties (which will go in WEB-INF/classes/Owasp.CsrfGuard.overlay.properties)

```
org.owasp.csrfguard.Logger=edu.internet2.middleware.grouper.grouperUi.csrf.CsrfGuardLogger

org.owasp.csrfguard.TokenPerPage=false

...

```

6. Create WEB-INF/tld/[csrfguard.tld](https://github.com/esheri3/OWASP-CSRFGuard/blob/master/csrfguard/src/main/resources/csrfguard.tld)

```
<?xml version="1.0"?>
  <!--
    The OWASP CSRFGuard Project, BSD License Eric Sheridan (eric@infraredsecurity.com),
    Copyright (c) 2011 All rights reserved. Redistribution and use in source and binary
    forms, with or without modification, are permitted provided that the following
    conditions are met: 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer. 2. Redistributions in
    binary form must reproduce the above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other materials provided with the
    distribution. 3. Neither the name of OWASP nor the names of its contributors may be
    used to endorse or promote products derived from this software without specific prior
    written permission. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
    CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
    TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
    DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY
    DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
    BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
    PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
    WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
    OF SUCH DAMAGE.
  -->
<taglib xmlns="http://java.sun.com/xml/ns/j2ee" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://java.sun.com/xml/ns/j2ee/web-jsptaglibrary_2_0.xsd" version="2.0">
  <tlib-version>1.2</tlib-version>
  <jsp-version>2.0</jsp-version>
  <short-name>Owasp CsrfGuard Tag Library</short-name>
...

```

7. Edit grouperExternal/appHtml/grouper.html Add this entry under all the existing js files

```
    <script src="../../grouperExternal/public/OwaspJavaScriptServlet"></script> 

```

8. Create grouperExternal/public/csrfError.html. Note, would be nice to have this in externalized text...

```
 CSRF token is missing, <a href="../../">start over</a>

```

9. Edit grouperUi/appHtml/grouper.html Add this entry under all the existing js files

```
    <script src="../../grouperExternal/public/OwaspJavaScriptServlet"></script> 

```

10. Edit WEB-INF/grouperUi/templates/common/commonTaglib.jsp, add this line

```
<%@ taglib uri="/WEB-INF/tld/csrfguard.tld" prefix="csrf" %>

```

11. Edit WEB-INF/grouperUi/templates/simpleMembershipUpdate/simpleMembershipUpdateImport.jsp, add this line below the form tag

```
   <input type="hidden" name="<csrf:token-name/>" value="<csrf:token-value uri="/grouper/grouperUi/app/SimpleMembershipUpdateImportExport.importCsv"/>"/>

```

12. Edit jsp/head.jsp, add this line below the other script tags

```
  <script src="grouperExternal/public/OwaspJavaScriptServlet"></script>

```

13. Edit WEB-INF/grouperUi2/assetsJsp/commonBottom.jsp, add this below the script tags

```
 <script src="../../grouperExternal/public/OwaspJavaScriptServlet"></script>

```

14. Edit WEB-INF/grouperUi2/groupImport/groupImport.jsp

FROM

```
            <form id="importGroupFormId" enctype="multipart/form-data" method="post" >

```

TO

```
             <form id="importGroupFormId" enctype="multipart/form-data" method="post" >
               <%-- note this wont work for token per page --%>
               <input type="hidden" name="<csrf:token-name/>" value="<csrf:token-value />"/>

```

Note, you can test that it works by setting up a static HTML page to remove a member from a group, or you can comment out the JS include in the the head.jsp or other files and try to use the UI, you will get an error
