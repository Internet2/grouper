---
title: "How to Setup an ultra-lite GSH Grouper Development Environment for Grouper v2.6"
space: Grouper
pageId: 48792941
version: 4
lastUpdated: 2026-07-12T06:45:55.475Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/48792941/How+to+Setup+an+ultra-lite+GSH+Grouper+Development+Environment+for+Grouper+v2.6
---

This is considered an "ultra-lite" dev env since we are not cloning git, or making pull requests, or running the UI. We just want to make GSH templates or JEXL scripts or hooks or provisioners.

Note, if using Java 17, pass this argument to tests and tomcat

```
--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.sql/java.sql=ALL-UNNAMED
```

For Grouper v4+, use Java 17

1. Install Java11 (corretto preferable)
2. Install Java8 (corretto preferable)
3. Install [eclipse](https://www.eclipse.org/downloads/), in installer select "Eclipse IDE for Enterprise Java and Web Developers", select the Java11 you just installed
  
  1. Make sure eclipse ini has at least 3 gig memory
4. Add Java8 JRE (might want to make it the default, not shown, but change the checkbox)
5. Make a new Maven project [without archetype](https://crunchify.com/how-to-create-new-simple-maven-project-in-eclipse-without-archtype-detailed-steps-included/)
6. Right click on Project, Maven → Add dependency
  
  1. GroupId: edu.internet2.middleware.grouper
  2. ArtifactId: grouper
  3. Version: 2.6.16
7. I use the java perspective, so switch to that
8. Add in to grouper.hibernate.properties that it is ui, and put in a local pass for a subject (remote database) or GrouperSystem (hsql database or remote)
  
  
  ```
  grouper.is.ui = true
  
  # UI basic auth is for quick start. Set to false when you migrate to shib or something else
  grouper.is.ui.basicAuthn = true
  
  grouperPasswordConfigOverride_UI_mchyzer_pass = pass
  grouperPasswordConfigOverride_UI_GrouperSystem_pass = pass
  ```
9. Set java8 for project (if you didnt set java8 as default above)
10. Add in properties files to the resources dir (copy from grouper container and point to your database (non prod)), or install postgres locally and create a database and credential
11. 
12. 
13. Create a new Java class with a main()
14. When you run it make sure you use Java 8 (if not the default above or if you get XML errors)
15. For GSH templates, put something like this at the top of your main()
  
  
  ```
  		GrouperStartup.startup();
     
  	    String gsh_input_bannerEnvironment = "whatever";
  	    String gsh_input_outsystemsEnvironment = "a";
  	    Boolean gsh_input_readonly = false;
  	    String gsh_input_csv = "b";
  
  	    GrouperSession gsh_builtin_grouperSession = GrouperSession.startRootSession();
  	    Subject gsh_builtin_subject = SubjectFinder.findByIdentifierAndSource("mchyzer", "pennperson", true);
  	    GshTemplateOutput gsh_builtin_gshTemplateOutput = new GshTemplateOutput();
  
  
  ```
16. When you put it in Grouper GSH template, comment out the package declaration at top, top part of the main and bottom curlies (WINDOWS: highlight and CTRL-/ ). (MAC: highlight and COMMAND-/ )
  
  
  ```
  //package test;
  
  import edu.internet2.middleware.grouper.Group;
  import edu.internet2.middleware.grouper.GroupFinder;
  import edu.internet2.middleware.grouper.GrouperSession;
  import edu.internet2.middleware.grouper.SubjectFinder;
  import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateOutput;
  import edu.internet2.middleware.grouper.misc.GrouperStartup;
  import edu.internet2.middleware.subject.Subject;
  
  //public class Test {
  //
  //	public static void main(String[] args) {
  //
  //		GrouperStartup.startup();
  //   
  //	    String gsh_input_bannerEnvironment = "whatever";
  //	    String gsh_input_outsystemsEnvironment = "a";
  //	    Boolean gsh_input_readonly = false;
  //	    String gsh_input_csv = "b";
  //
  //	    GrouperSession gsh_builtin_grouperSession = GrouperSession.startRootSession();
  //	    Subject gsh_builtin_subject = SubjectFinder.findByIdentifierAndSource("mchyzer", "pennperson", true);
  //	    GshTemplateOutput gsh_builtin_gshTemplateOutput = new GshTemplateOutput();
  
  	    
  	    Group group = GroupFinder.findByName(gsh_builtin_grouperSession, "test:test", false);
  		
  //	}
  //
  //}
  
  
  ```
17. To test a JEXL script, setup variables (will need to look in source code or ask slack channel), and print out the result
  
  
  ```
  package test;
  
  import java.util.HashMap;
  import java.util.Map;
  
  import edu.internet2.middleware.grouper.GrouperSession;
  import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
  import edu.internet2.middleware.grouper.misc.GrouperStartup;
  import edu.internet2.middleware.grouper.util.GrouperUtil;
  
  public class Test {
  
  	public static void main(String[] args) {
  
  		GrouperStartup.startup();
  		
  		GrouperSession grouperSession = GrouperSession.startRootSession();
  		
  		Map<String, Object> elVariableMap = new HashMap<String, Object>();
  
  		ProvisioningGroup grouperProvisioningGroup = new ProvisioningGroup();
  		grouperProvisioningGroup.setName("penn:isc:ait:apps:twoFactor:groups:requiredUsersStaff:twoFactorStaff");
  
  		elVariableMap.put("grouperProvisioningGroup", grouperProvisioningGroup);
  		
  		String script = "${edu.internet2.middleware.grouper.util.GrouperUtil.stringFormatNameReverseReplaceTruncate(grouperProvisioningGroup.name, '_', 64).replaceAll('[^a-zA-Z0-9]', '_')}";
  
  		Object result = GrouperUtil.substituteExpressionLanguageScript(script, elVariableMap, true, false, false);
  		
  		System.out.println(result);
  		
  	}
  
  }
  
  
  ```
  
  Output
  
  
  ```
  twoFactorStaff_requiredUsersStaff_groups_twoFactor_apps_ait_isc_
  ```
