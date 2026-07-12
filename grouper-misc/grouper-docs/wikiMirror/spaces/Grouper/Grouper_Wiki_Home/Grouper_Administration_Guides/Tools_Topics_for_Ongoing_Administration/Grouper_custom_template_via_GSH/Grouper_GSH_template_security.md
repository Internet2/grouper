---
title: "Grouper GSH template security"
space: Grouper
pageId: 28548336
version: 15
lastUpdated: 2026-07-12T05:26:42.918Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548336/Grouper+GSH+template+security
---

Security is very important for [Grouper Custom Templates via GSH](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544720/Grouper+custom+template+via+GSH).

## Configuring a GSH template

1. Can be enabled/disabled. Easily make a GSH script non-executable.
2. This will show up in a "more actions" drop down in the UI for users who can execute it.
  
  
  
  1. Can be shown on all folders/groups under a certain folder, or specific folders or groups, all all folders/groups
3. Only certain users/groups can execute each script
  
  
  
  1. Admins only, people in a certain group, or people who have privileges on the object (e.g. where it shows)
4. The script will "run as" a certain user
  
  
  
  1. Current user (preferred), a specified user (e.g. a service principal, second best choice), or grouper system
  2. If the script will "build out" structures in a certain app folder for example, or an org folder, and the current user has rights in that folder, then the script should run as that user.
  3. If the current user doesn't have rights to everything (e.g. adding a ref group to a policy group), then a service principal should be created with limited rights
  4. If the script is written securely and the administrator so chooses, it can run as GrouperSystem (not recommended)
5. Run script in transaction
  
  
  
  1. A script that errors out partway through will rollback all work the user did by default. If no transaction is desired (e.g. if a LOT of steps will use too many DB resources), it can be set
6. Show errors on screen option. When troubleshooting the template can show error messages on UI. For Grouper admins the error message will always show on UI.
7. Auditing
  
  
  
  1. There is an audit message that someone executed a template (or added a template, edited a template, deleted a template)
    
    
    
    1. Note, each config a user edits will be audited in the configuration point-in-time logs
  2. If the template is transactional, the overall exec audit will only be saved if the template is successful (e.g. on commit)
  3. If the template is not transactional, the overall exec audit will be saved whether or not the template is successful (since partial work will be saved)
  4. By default all individual actions will be audited as changed by the calling user (no matter what the "run as" is)
  5. The template could be configured to not audit individual actions, but instead to only audit the overall template execution. e.g. instead of 6 audits (5 audits for 5 groups created and 1 overall, there would only be the 1 overall audit message)
8. List the inputs
  
  
  
  1. All input names must start with prefix: gsh_input_ and be alphaNumeric or underscore
  2. All Grouper provided variables start with prefix: gsh_builtin_ and be alphaNumeric or underscore
  3. These inputs have value types, the non-string types are validated, e.g. a boolean can only be null/true/false, numeric can only be null/numeric. These are stored as primitive objects on the server side. If null not wanted, mark as "required"
  4. Inputs have a form element type. Values from a drop down are checked on server side to assert the values were not spoofed in browser. Note: booleans are rendered as radio buttons
  5. Each input has a validation. This is a required field, so to have no validation (e.g. in a description field), it would have be explicitly set. The script will not execute if there is an invalid input. Validations are checked on server for WS or UI. Descriptive validation messages will be returned (inputs and messages are escaped for HTML since they are intended to show on a UI to a user)
  6. Built in validations make it easy for users not to worry about writing an erroneous regex. There is also a built-in validation message (to users if invalid input) for these to make it easier to configure. A custom validation message can be added too.
  7. Inputs can have hide/show. E.g. if a collaboration group is not marked to make an email list, then the boolean for if the email list is moderated will not be shown on screen or processed on server if spoofed
9. If the script is executed by WS, the credential calling the WS can be in a special group (for that GSH template) where it can provide the user who is running the script (a limited "act as"). Another audit will be saved that specifies that the credential acted as the user.
10. The script
  
  
  
  1. There is a script that the Grouper administrator configures
  2. Not all Grouper admins can configure scripts, just ones in special group (note, any admin could add themselves to that group)
  3. Built in variables are prepended to the script. Note, all strings written to generated part of script are escaped with apache commons StringEscapeUtils.escapeJava() so double quotes don't break out of the script
    
    
    
    | Variable | Type | Description |
    | --- | --- | --- |
    | gsh_builtin_gshTemplateOutput | GshTemplateOutput | Script uses this to:    1. Send validation errors back to user on inputs 2. Tell Grouper there's an error 3. Send output to screen (e.g. created group A, assigned priv B to folder C, etc) |
    | gsh_builtin_gshTemplateRuntime | GshTemplateRuntime | Internal class to manage other things, ignore |
    | gsh_builtin_grouperSession | GrouperSession | Grouper session is started as configured above (as current user, certain user, or GrouperSystem) |
    | gsh_builtin_subject | Subject | User who is executing the template (or the actAs if the WS is acting as a user) |
    | gsh_builtin_subjectId | String | Subject id of gsh_builtin_subject |
    | gsh_builtin_ownerStem | Stem | Stem where script was executed in UI (this is provided in WS call if WS) (if executed as stem) |
    | gsh_builtin_ownerStemName | String | Stem name of gsh_builtin_ownerStem |
    | gsh_builtin_ownerGroup | Group | Group where script was executed in UI (this is provided in WS call if WS) (if executed as group) |
    | gsh_builtin_ownerGroupName | String | Group name of gsh_builtin_ownerGroup |
  4. Input variables are added to script (after built ins and before the GSH configured script) (same string escaping as above) e.g.
    
    
    ```
    String gsh_input_myExtension = "some java escaped and validated string";
    int gsh_input_myNumber = 1234;
    boolean gsh_input_isTrue = false;
    ```
  5. First step is validation in GSH. The script can do more validation (then just JEXL or regex) to make sure everything is ok.
    
    
    
    1. For instance, if executing with inputs to create a folder, and it exists, the script can either continue and edit that folder, or decide to exit and give the user some feedback
      
      
      ```
      // validate first: check if working group folder exists
      Stem workingGroupFolder = StemFinder.findByName(gsh_builtin_grouperSession, "ref:incommon-collab:" + gsh_input_workingGroupExtension, false);
      if (workingGroupFolder != null) {
        gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_workingGroupExtension",
          "Error: working group extension '" + gsh_input_workingGroupExtension + "' already exists!");
      }
      
      if (GrouperUtil.length(gsh_builtin_gshTemplateOutput.getOutputLines()) > 0) {
        gsh_builtin_gshTemplateOutput.assignIsError(true);
        quit;
      }
      
      ... make the folder and do whatever else
      ```
    2. Output and validation messages are escaped for HTML automatically. There is another method the GSH script writer can use if HTML validation is not desired, and in that case they must have strict validation on any input variables they append to the message
  6. GSH script writers should not use non-validated inputs dynamically. We will have a list of best practices here. In general there isnt a need for dynamic scripts in the GSH
    
    
    
    1. Use bind variables for all SQL
    2. Escape LDAP calls
    3. Let the Grouper team review things you are unsure about something and it can be documented or turned into a method chained call
  7. There will be "method chained" classes available for all common activities done in GSH templates. e.g. here is a script to add a folder, create a group, and assign inherited privileges. The StemSave, GroupSave, MembershipSave, and PrivilegeGroupInheritanceSave are the method chained classes to make doing Grouper things easy. These are "secure" operations that make sure the GrouperSession (i.e. current user if running as current user) is allowed to perform the operation.
    
    
    ```
      String workingGroupFolderName = "ref:incommon-collab:" + gsh_input_workingGroupExtension;
      
      // default value for display extension is just the extension
      String displayExtension = GrouperUtil.defaultIfBlank(gsh_input_workingGroupDisplayExtension, gsh_input_workingGroupExtension);
    
      // Create working group folder
      Stem workingGroupFolder = new StemSave().assignName(workingGroupFolderName).assignDisplayExtension(displayExtension)
        .assignDescription("Folder holds working group roles.  " + gsh_input_workingGroupDescription).save();
      gsh_builtin_gshTemplateOutput.addOutputLine("Folder created: " + workingGroupFolderName);
      
      // Create admins group
      Group adminsGroup = new GroupSave().assignName(workingGroupFolderName + ":admins")
        .assignDisplayExtension(displayExtension + " admins")
        .assignDescription("Admins role means can manage / attest the working group.  " + gsh_input_workingGroupDescription).save();
      gsh_builtin_gshTemplateOutput.addOutputLine("Group created: " + adminsGroup.getName());
      
      // If the initial admin is passed in and in the right subject source
      if (gsh_builtin_subject != null && GrouperUtil.equals("ldap", gsh_builtin_subject.getSourceId())) {
      
        // add the initial admin to be in the admins group
        new MembershipSave().assignGroup(adminsGroup).assignSubject(gsh_builtin_subject).save();
        gsh_builtin_gshTemplateOutput.addOutputLine("Added admin: " + gsh_builtin_subject.getId() + " to group: " + adminsGroup.getName());
      } else {
        // if not resolvable thats just a warning
        gsh_builtin_gshTemplateOutput.addOutputLine("warning",
          "Warning: admin subject not resolvable or in wrong source '" + gsh_builtin_subjectId + "'");
      
      }
      
      // Assign privs for admins on folder
      new PrivilegeGroupInheritanceSave().assignStem(workingGroupFolder)
          .addPrivilegeName("admin").assignSubject(adminsGroup.toSubject()).save();
      gsh_builtin_gshTemplateOutput.addOutputLine("Assigned group admin privileges to: " + adminsGroup.getName() + " inherited from folder: " + workingGroupFolder.getName());
    
    
    ```
  8. Note the output above. Each step should have an output line (gsh_builtin_gshTemplateOutput.addOutputLine()) that can take a type of output (success, warning, error), and a message. Again, by default this is escaped for HTML unless the "html allowed" method is purposefully called by GSH script writer, where they must only append html escaped input (or validated as such)
11. The caller (either UI user or WS client) will either
  
  
  
  1. Get a SUCCESS with a list of output messages
  2. or get a validation error where the inputs with a problem are highlighted
  3. Or just get an error
