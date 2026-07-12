---
title: "Grouper custom template via GSH delegated VPN management - add"
space: Grouper
pageId: 28555787
version: 3
lastUpdated: 2026-07-01T05:37:28.711Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555787/Grouper+custom+template+via+GSH+delegated+VPN+management+-+add
---

This GSH template allows networking administrators to build out the structure for a new VPN

## Screenshots

When creating a VPN you can initialize the roles

## Configuration

## GSH script

```groovy
// 1. Check Owner pennkeys
    // for rules
    Group employeeGroup = GroupFinder.findByName(gsh_builtin_grouperSession, "penn:community:employeeOrContractorIncludingUphs", true);
    StringBuilder emailBody = new StringBuilder();
    Set<String> emailTos = new HashSet<String>();
    // convert owner pennkeys from comma separate string to an array
    String[] ownerPennkeys = null;
    // convert owner pennkeys into subjects
    Subject[] ownerSubjects = null;
    if (!StringUtils.isBlank(gsh_input_ownerPennkeys)) {
      ownerPennkeys = GrouperUtil.splitTrim(gsh_input_ownerPennkeys, ",");
      ownerSubjects = new Subject[ownerPennkeys.length];
      for (int i=0;i<ownerPennkeys.length;i++) {
        Subject subject = SubjectFinder.findByIdOrIdentifierAndSource(ownerPennkeys[i], "pennperson", false);
        if (subject == null) {
          gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_ownerPennkeys",
            "Error: Owner PennKey cannot be found '" + ownerPennkeys[i] + "'!");
        }
        ownerSubjects[i] = subject;
        
        // 2. Owners must be employees
        if (!new MembershipFinder().addGroup(employeeGroup).addSubject(ownerSubjects[i]).hasMembership()) {
          gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_ownerPennkeys",
            "Error: Owner PennKey '" + ownerPennkeys[i] + "' is not in group: " + employeeGroup.getName() + "!");
        }
        String emailOfOwner = GrouperEmail.retrieveEmailAddress(ownerSubjects[i]);
     
        // 3. email goes to owners
        if (!StringUtils.isBlank(emailOfOwner)) {
          // dont email owners for now, we can tell them
          // emailTos.add(emailOfOwner);
        }
      
      }
    }
    
    // convert admin pennkeys from comma separate string to an array
    String[] adminPennkeys = null;
    // convert admin pennkeys into subjects
    Subject[] adminSubjects = null;
    if (!StringUtils.isBlank(gsh_input_adminPennkeys)) {
      adminPennkeys = GrouperUtil.splitTrim(gsh_input_adminPennkeys, ",");
      adminSubjects = new Subject[adminPennkeys.length];
      for (int i=0;i<adminPennkeys.length;i++) {
        Subject subject = SubjectFinder.findByIdOrIdentifierAndSource(adminPennkeys[i], "pennperson", false);
        if (subject == null) {
          gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_adminPennkeys",
            "Error: Admin PennKey cannot be found '" + adminPennkeys[i] + "'!");
        }
        adminSubjects[i] = subject;
        
        // 4. admins must be employees
        if (!new MembershipFinder().addGroup(employeeGroup).addSubject(adminSubjects[i]).hasMembership()) {
          gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_adminPennkeys",
            "Error: Admin PennKey '" + adminPennkeys[i] + "' is not in group: " + employeeGroup.getName() + "!");
        }
      }
    }
    
    // convert vpn pennkeys from comma separate string to an array
    String[] vpnEmployeePennkeys = null;
    // convert vpn pennkeys into subjects
    Subject[] vpnEmployeeSubjects = null;
    if (!StringUtils.isBlank(gsh_input_vpnEmployeePennkeys)) {
      vpnEmployeePennkeys = GrouperUtil.splitTrim(gsh_input_vpnEmployeePennkeys, ",");
      vpnEmployeeSubjects = new Subject[vpnEmployeePennkeys.length];
      for (int i=0;i<vpnEmployeePennkeys.length;i++) {
        Subject subject = SubjectFinder.findByIdOrIdentifierAndSource(vpnEmployeePennkeys[i], "pennperson", false);
        if (subject == null) {
          gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_vpnPennkeys",
            "Error: VPN PennKey cannot be found '" + vpnEmployeePennkeys[i] + "'!");
        }
        vpnEmployeeSubjects[i] = subject;
        
        // 5. vpns must be employees
        if (!new MembershipFinder().addGroup(employeeGroup).addSubject(vpnEmployeeSubjects[i]).hasMembership()) {
          gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_vpnPennkeys",
            "Error: VPN PennKey '" + vpnEmployeePennkeys[i] + "' is not in group: " + employeeGroup.getName() + "!");
        }
      }
    }
    // convert owner ref groups from comma separate string to an array
    String[] ownerRefGroupNames = null;
    // convert owner ref group names into groups
    Group[] ownerRefGroups = null;
    if (!StringUtils.isBlank(gsh_input_ownerRefGroupName)) {
      ownerRefGroupNames = GrouperUtil.splitTrim(gsh_input_ownerRefGroupName, ",");
      ownerRefGroups = new Group[ownerRefGroupNames.length];
      for (int i=0;i<ownerRefGroupNames.length;i++) {
        Group group = GroupFinder.findByName(gsh_builtin_grouperSession, ownerRefGroupNames[i], false);
        
        // 6. Make sure owner ref groups exist 
        if (group == null) {
          gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_ownerRefGroupName",
            "Error: Owner ref group cannot be found '" + ownerRefGroupNames[i] + "'!");
        } else {
          ownerRefGroups[i] = group;
        }
      }
    }
    // convert admin ref groups from comma separate string to an array
    String[] adminRefGroupNames = null;
    // convert admin ref group names into groups
    Group[] adminRefGroups = null;
    if (!StringUtils.isBlank(gsh_input_adminRefGroupName)) {
      adminRefGroupNames = GrouperUtil.splitTrim(gsh_input_adminRefGroupName, ",");
      adminRefGroups = new Group[adminRefGroupNames.length];
      for (int i=0;i<adminRefGroupNames.length;i++) {
        Group group = GroupFinder.findByName(gsh_builtin_grouperSession, adminRefGroupNames[i], false);
        
        // 7. Make sure admin ref groups exist 
        if (group == null) {
          gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_adminRefGroupName",
            "Error: Admin ref group cannot be found '" + adminRefGroupNames[i] + "'!");
        } else {
          adminRefGroups[i] = group;
        }
      }
    }

    // convert VPN ref groups from comma separate string to an array
    String[] vpnRefGroupNames = null;
    // convert vpn ref group names into groups
    Group[] vpnRefGroups = null;
    if (!StringUtils.isBlank(gsh_input_vpnOrgsOrCenters)) {
      vpnRefGroupNames = GrouperUtil.splitTrim(gsh_input_vpnOrgsOrCenters, ",");
      vpnRefGroups = new Group[vpnRefGroupNames.length];
      for (int i=0;i<vpnRefGroupNames.length;i++) {
        
        // 8. convert org and center name to group name.
        if (!vpnRefGroupNames[i].matches("^[0-9a-zA-Z]{2,4}\044")) { // groovy doesnt like dollar in string without escape.  \044 is dollar
          gsh_builtin_gshTemplateOutput.addValidationLine("group",
              "Error: VPN ref group must be two chars for center or four chars for org '" + vpnRefGroupNames[i] + "'!");
          continue;
        }
        Group group = null;
        
        if (vpnRefGroupNames[i].length() == 2) {
          // penn:community:employee:center:91_center:91_center, e.g. 99
          vpnRefGroupNames[i] = "penn:community:employee:center:" + vpnRefGroupNames[i] + "_center:" + vpnRefGroupNames[i] + "_center";
          group = GroupFinder.findByName(gsh_builtin_grouperSession, vpnRefGroupNames[i], false);
        } else if (vpnRefGroupNames[i].length() == 4) {
          String org = vpnRefGroupNames[i];
          // penn:community:employee:org:9142:9142_personorg, e.g. 9104
          vpnRefGroupNames[i] = "penn:community:employee:org:" + org + ":" + org + "_personorg";
          group = GroupFinder.findByName(gsh_builtin_grouperSession, vpnRefGroupNames[i], false);
          
          // could be a rollup org
          if (group == null) {
            vpnRefGroupNames[i] = "penn:community:employee:org:" + org + ":" + org + "_rolluporg";
            group = GroupFinder.findByName(gsh_builtin_grouperSession, vpnRefGroupNames[i], false);
          }
        }
        
        
        
        
        // 9. Check all orgs / centers exist
        if (group == null) {
          gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_vpnOrgsOrCenters",
            "Error: VPN org or center group cannot be found '" + vpnRefGroupNames[i] + "'!");
        } else {
          vpnRefGroups[i] = group;
        }
      }
    }
    
    // Do not proceed is there is an error
    if (GrouperUtil.length(gsh_builtin_gshTemplateOutput.getValidationLines()) > 0) {
      gsh_builtin_gshTemplateOutput.assignIsError(true);
      GrouperUtil.gshReturn();
    }
    
    // 10. email to the user who performed this so they can support it
    String emailOfUserRunningTemplate = GrouperEmail.retrieveEmailAddress(gsh_builtin_subject);
    if (!StringUtils.isBlank(emailOfUserRunningTemplate)) {
      emailTos.add(emailOfUserRunningTemplate);
    } else {
      gsh_builtin_gshTemplateOutput.addValidationLine(
          "Error: Cannot find your email address.  Please edit it in the directory and try again an hour later");
    }
    // 11. main body of email
    emailBody.append("You are an owner of the " + gsh_input_projectFriendlyName + " SRA VPN.\n");
    emailBody.append("This is an organizational VPN to secure network resources to a specific population.\n");
    emailBody.append("There are three roles for the VPN:\n");
    emailBody.append(" - The owner role should have two or three people who manage the owners, admins, VPN users, and review the access monthly.\n");
    emailBody.append(" - The admin role manages the VPN users.  Usually the admins are the same people as the owners, but admins can contain more IT support specialists.\n");
    emailBody.append(" - The VPN role can use the VPN.\n");
    emailBody.append("Each role can have an automatic population based on other groups.  For owners and admins this could be another group of IT employees.  ");
    emailBody.append("The VPN automatic population will generally be based on active employees in payroll ORG(s) or CENTER(s).\n");
    emailBody.append("Each role has manual groups for the owners and admins (if applicable) to maintain.  Owners can manage manual groups of owners and admins.  ");
    emailBody.append("Any owner or admin in the manual group, who is no longer an employee, will be automatically removed from the manual owner or admin group.  ");
    emailBody.append("The 'VPN employee manual' group is an ad hoc group of employees who have access to the VPN, but will be automatically removed ");
    emailBody.append("when they are no longer an employee.  The 'VPN guest' group can contain people who are not employees.  You might want to set an 'end date' on those memberships.\n");
    emailBody.append("In rare cases someone is automatically allowed to use the VPN but needs to be manually excluded.  There is an Excludes list for that.\n");
    emailBody.append("There is a management interface for admins and owners to easily manage and review the access for the VPN.\n\n");
    
    String projectStemName = "penn:isc:ts:networking:service:sraVpn:service:" + gsh_input_projectSystemName;
    
    // 12. Add project folder
    Stem projectFolder = null;
    boolean projectFolderAdded = false;
    String reportConfigName = gsh_input_projectSystemName + "SraVpnReport";
    PROJECT_FOLDER: {
      StemSave projectFolderSave = new StemSave().assignName(projectStemName).assignDisplayExtension(gsh_input_projectFriendlyName + " SRA VPN").assignDescription("This folder holds groups involved in the " + gsh_input_projectFriendlyName + " SRA VPN.  Owners are two or three people who review all the memberships lists and manage the Owners and Admins.  Admins can manage the VPN memberships.  Owners and admins can be automatic.  If owners and admins are manual, then they will lose access when no longer Penn employees.  Automatic VPN members will gain/lose access based on their org or other attribute.  EmployeesManual are a manual list who will have access as long as they are employees.  Guests will have access and do not need to be employees.");
      projectFolder = projectFolderSave.save();
      gsh_builtin_gshTemplateOutput.addOutputLine("Project folder " + projectFolderSave.getSaveResultType() + ": " + projectStemName);
      
      
      projectFolderAdded = projectFolderSave.getSaveResultType() == SaveResultType.INSERT;
      // 13. Add report
      if (projectFolderAdded) {
        AttributeAssign attributeAssignMarker = new AttributeAssignToStemSave().assignStem(projectFolder).assignNameOfAttributeDefName("penn:etc:reportConfig:reportConfigMarker").save();
        AttributeAssign attributeAssign = new AttributeAssignToAssignmentSave().assignAttributeAssign(attributeAssignMarker).assignAttributeDefNameName("penn:etc:reportConfig:reportConfigDescription").save().getAttributeAssign(); 
        new AttributeAssignValueSave().assignAttributeAssign(attributeAssign).assignValue("Contains users, admins, and owner of the VPN.  can_use_vpn means the user has access (if they have been in for 15 minutes and have been provisioned to LDAP).  can_use_vpn_since is the timestamp when their VPN access started.  could_use_vpn_until is when the vpn access was removed for this user.  vpn_to_start_timestamp is the timestamp in the future the user will have access.  vpn_to_end_timestamp is their end date of using the VPN.  is_employee_manual means they are in the ad hoc population (otherwise to get access they would need to be in the automatic population), and if they are ever not a Penn employee they will lose access.  is_guest means they can use the VPN and they do not need a Penn affiliation to do so.  is_exclude means they are in the manual group of people excludes from the VPN (when the automatic population is wrong).  active_employee is true if the user is an active employee at Penn or UPHS.  is_owner is T (true) if the user is an owner of the VPN (can manage all lists).  is_admin is T if the user can manage the VPN user lists.  person_description is the user's name and affiliation.").save();
        attributeAssign = new AttributeAssignToAssignmentSave().assignAttributeAssign(attributeAssignMarker).assignAttributeDefNameName("penn:etc:reportConfig:reportConfigEnabled").save().getAttributeAssign(); 
        new AttributeAssignValueSave().assignAttributeAssign(attributeAssign).assignValue("true").save();
        attributeAssign = new AttributeAssignToAssignmentSave().assignAttributeAssign(attributeAssignMarker).assignAttributeDefNameName("penn:etc:reportConfig:reportConfigFilename").save().getAttributeAssign(); 
        new AttributeAssignValueSave().assignAttributeAssign(attributeAssign).assignValue(gsh_input_projectSystemName + "SraVpnReport_" + '$' + '$' + "timestamp" + '$' + '$' + ".csv").save();
        attributeAssign = new AttributeAssignToAssignmentSave().assignAttributeAssign(attributeAssignMarker).assignAttributeDefNameName("penn:etc:reportConfig:reportConfigFormat").save().getAttributeAssign(); 
        new AttributeAssignValueSave().assignAttributeAssign(attributeAssign).assignValue("CSV").save();
        attributeAssign = new AttributeAssignToAssignmentSave().assignAttributeAssign(attributeAssignMarker).assignAttributeDefNameName("penn:etc:reportConfig:reportConfigName").save().getAttributeAssign(); 
        new AttributeAssignValueSave().assignAttributeAssign(attributeAssign).assignValue(reportConfigName).save();
        attributeAssign = new AttributeAssignToAssignmentSave().assignAttributeAssign(attributeAssignMarker).assignAttributeDefNameName("penn:etc:reportConfig:reportConfigType").save().getAttributeAssign(); 
        new AttributeAssignValueSave().assignAttributeAssign(attributeAssign).assignValue("SQL").save();
        attributeAssign = new AttributeAssignToAssignmentSave().assignAttributeAssign(attributeAssignMarker).assignAttributeDefNameName("penn:etc:reportConfig:reportConfigQuartzCron").save().getAttributeAssign(); 
        // some random time between 7 and 9
        int seconds = (int)(Math.random()*60);
        int minutes = (int)(Math.random()*60);
        int hours = 7 + (int)(Math.random()*2);
        
        new AttributeAssignValueSave().assignAttributeAssign(attributeAssign).assignValue(seconds + " " + minutes + " " + hours + " * * ?").save();
        attributeAssign = new AttributeAssignToAssignmentSave().assignAttributeAssign(attributeAssignMarker).assignAttributeDefNameName("penn:etc:reportConfig:reportConfigQuery").save().getAttributeAssign(); 
        new AttributeAssignValueSave().assignAttributeAssign(attributeAssign).assignValue("select penn_id, penn_key, can_use_vpn, can_use_vpn_since, could_use_vpn_until, vpn_to_start_timestamp, vpn_to_end_timestamp, is_employee_manual, is_guest, is_exclude, active_employee, is_owner, is_admin, person_description from penn_sra_vpn_report_v where vpn_system_name = '" + gsh_input_projectSystemName + "'").save();
        gsh_builtin_gshTemplateOutput.addOutputLine("Report: added to project folder: " + projectStemName);
        
        GrouperReportConfigurationBean reportConfigBean = GrouperReportConfigService.getGrouperReportConfigBean(attributeAssignMarker.getId());
        try {GrouperReportConfigService.scheduleJob(reportConfigBean, projectFolder); } catch (org.quartz.SchedulerException se) {throw new RuntimeException("error", se);}
        gsh_builtin_gshTemplateOutput.addOutputLine("Report: scheduled");
        
      }
    }
    emailBody.append("https://grouper.apps.upenn.edu/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Template.newTemplate&stemId=" + projectFolder.getId() + "&templateType=sraVpnManage");
    // 14. Create owners group
    Group ownersGroup = null;
    OWNERS_GROUP: {
      GroupSave groupSave = new GroupSave().assignName(projectStemName + ":" + gsh_input_projectSystemName + "SraVpnOwners").assignDisplayExtension(gsh_input_projectFriendlyName + " SRA VPN Owners").assignDescription("Owners manage the Owners, and Admins.  They receive periodic requests to review the VPN access report.  There should be more than one and not more than a few Owners.  Do not edit this group's memberships, edit the Owner manual group list.  To add a new reference group here open a ticket with help@isc.upenn.edu");
      ownersGroup = groupSave.save();
      gsh_builtin_gshTemplateOutput.addOutputLine("Owners group " + groupSave.getSaveResultType() + ": " + ownersGroup.getName());
    }
    
    // 15. Add owner ref groups to owners
    for (Group memberGroup : GrouperUtil.nonNull(ownerRefGroups, Group.class)) {
      MembershipSave membershipSave = new MembershipSave().assignGroup(ownersGroup).assignSubject(memberGroup.toSubject());
      membershipSave.save();
      gsh_builtin_gshTemplateOutput.addOutputLine("Adding: " + memberGroup.getName() + " to Owners group: " + membershipSave.getSaveResultType());
    }
    
    // 16. Add read on owner ref groups to owners
    for (Group refGroup : GrouperUtil.nonNull(ownerRefGroups, Group.class)) {
      PrivilegeGroupSave privilegeGroupSave = new PrivilegeGroupSave().assignGroup(refGroup).assignFieldName("readers").assignSubject(ownersGroup.toSubject());
      privilegeGroupSave.save();
      gsh_builtin_gshTemplateOutput.addOutputLine("Allowing: READ on " + refGroup.getName() + " to Owners group: " + privilegeGroupSave.getSaveResultType());
    }
    
    // 17. Add grouperSecurity type to Owners
    OWNER_GROUP_TYPE: {
      GdgTypeGroupSave gdgTypeGroupSave = new GdgTypeGroupSave().assignGroup(ownersGroup).assignType("grouperSecurity");
      gdgTypeGroupSave.save();
      gsh_builtin_gshTemplateOutput.addOutputLine("Add: grouperSecurity type on Owners group: " + gdgTypeGroupSave.getSaveResultType());
    }
    
    // 18. Create Owners manual group
    Group ownersManualGroup = null;
    OWNER_MANUAL_GROUP: {
      GroupSave ownersManualGroupSave = new GroupSave().assignName(projectStemName + ":" + gsh_input_projectSystemName + "SraVpnOwnersManual").assignDisplayExtension(gsh_input_projectFriendlyName + " SRA VPN Owners Manual").assignDescription("Owners manage the Owners, and Admins.  They receive periodic requests to review the VPN access report.  There should be more than one and not more than a few Owners.  Manually manage the list of employees who are owners here.");
      ownersManualGroup = ownersManualGroupSave.save();
      if (ownersManualGroupSave.getSaveResultType() == SaveResultType.INSERT) {
        gsh_builtin_gshTemplateOutput.addOutputLine("Owners manual group created: " + ownersManualGroup.getName());
         
        // 19. since we just created this, add a rule that removes members if not employees
        RuleApi.groupIntersection(gsh_builtin_grouperSession.getSubject(), ownersManualGroup, employeeGroup);
        gsh_builtin_gshTemplateOutput.addOutputLine("Rule added on owners manual group to require membership in: " + employeeGroup.getName());
      } else {
        gsh_builtin_gshTemplateOutput.addOutputLine("Owners manual group " + ownersManualGroupSave.getSaveResultType() + ": " + ownersManualGroup.getName());
      }
    }    
    
    // 19. Add Owners Manual to Owners
    ADD_MANUAL_TO_OWNERS: {
      MembershipSave membershipSave = new MembershipSave().assignGroup(ownersGroup).assignSubject(ownersManualGroup.toSubject());
      membershipSave.save();
      gsh_builtin_gshTemplateOutput.addOutputLine("Adding: Owners manual group to Owners group: " + membershipSave.getSaveResultType());
    }
    
    // 20. Add Owner to manage owner manual group (read/update)
    OWNER_MANUAL_PRIVILEGES: {
      PrivilegeGroupSave privilegeGroupSave = new PrivilegeGroupSave().assignGroup(ownersManualGroup).assignFieldName("updaters").assignSubject(ownersGroup.toSubject());
      privilegeGroupSave.save();
      gsh_builtin_gshTemplateOutput.addOutputLine("Allowing: Owners group to update Owners manual group: " + privilegeGroupSave.getSaveResultType());
    }
    
    // 21. Add manual and grouperSecurity type to Owners manual
    OWNER_MANUAL_TYPES: {
      GdgTypeGroupSave gdgTypeGroupSave = new GdgTypeGroupSave().assignGroup(ownersManualGroup).assignType("manual");
      gdgTypeGroupSave.save();
      gsh_builtin_gshTemplateOutput.addOutputLine("Add: manual type on Owners manual group: " + gdgTypeGroupSave.getSaveResultType());
    }
    
    // 22. Add owner pennkeys to owner group
    for (Subject subject : GrouperUtil.nonNull(ownerSubjects, Subject.class)) {
      MembershipSave membershipSave = new MembershipSave().assignGroup(ownersManualGroup).assignSubject(subject);
      membershipSave.save();
      gsh_builtin_gshTemplateOutput.addOutputLine("Adding: " + subject.getDescription() + " to Owner manual group: " + membershipSave.getSaveResultType());
    }
    
    // 23. Create admins group
    Group adminsGroup = null;
    CREATE_ADMINS: {
      GroupSave groupSave = new GroupSave().assignName(projectStemName + ":" + gsh_input_projectSystemName + "SraVpnAdmins").assignDisplayExtension(gsh_input_projectFriendlyName + " SRA VPN Admins").assignDescription("Admins manage the VPN users: EmployeesManual, Guests, Excludes.  They can see the access report and all groups.  Do not edit this group's memberships, edit the Admin manual group list.  To add a new (automatic) reference group here open a ticket with help@isc.upenn.edu");
      adminsGroup = groupSave.save();
      gsh_builtin_gshTemplateOutput.addOutputLine("Admins group " + groupSave.getSaveResultType() + ": " + adminsGroup.getName());
    }
    
    // 24. Add admin ref groups to admins
    for (Group memberGroup : GrouperUtil.nonNull(adminRefGroups, Group.class)) {
      MembershipSave membershipSave = new MembershipSave().assignGroup(adminsGroup).assignSubject(memberGroup.toSubject());
      membershipSave.save();
      gsh_builtin_gshTemplateOutput.addOutputLine("Adding: " + memberGroup.getName() + " to Admins group: " + membershipSave.getSaveResultType());
    }
    // 25. Add read on admin ref groups to owners
    for (Group refGroup : GrouperUtil.nonNull(adminRefGroups, Group.class)) {
      PrivilegeGroupSave privilegeGroupSave = new PrivilegeGroupSave().assignGroup(refGroup).assignFieldName("readers").assignSubject(ownersGroup.toSubject());
      privilegeGroupSave.save();
      gsh_builtin_gshTemplateOutput.addOutputLine("Allowing: READ on " + refGroup.getName() + " to Owners group: " + privilegeGroupSave.getSaveResultType());
    }
    
    // 26. Add Owners group to admins
    ADD_OWNERS_TO_ADMINS: {
      MembershipSave membershipSave = new MembershipSave().assignGroup(adminsGroup).assignSubject(ownersGroup.toSubject());
      membershipSave.save();
      gsh_builtin_gshTemplateOutput.addOutputLine("Adding: Owners group to Admins group: " + membershipSave.getSaveResultType());
    }
    // 27. Add grouperSecurity type to Admins
    ADMIN_GROUP_TYPE: {
      GdgTypeGroupSave gdgTypeGroupSave = new GdgTypeGroupSave().assignGroup(adminsGroup).assignType("grouperSecurity");
      gdgTypeGroupSave.save();
      gsh_builtin_gshTemplateOutput.addOutputLine("Add: grouperSecurity type on Admins group: " + gdgTypeGroupSave.getSaveResultType());
    }
    
    // 28. Create Admins manual group
    Group adminsManualGroup = null;
    ADMIN_MANUAL_GROUP: {
      GroupSave adminsManualGroupSave = new GroupSave().assignName(projectStemName + ":" + gsh_input_projectSystemName + "SraVpnAdminsManual").assignDisplayExtension(gsh_input_projectFriendlyName + " SRA VPN Admins Manual").assignDescription("Admins manage the VPN users.  They can add or remove members of the manual groups: EmployeesManual, Guests, Excludes.  Admins are automatically removed when they are no longer employees.  Manage the list of employees who are admins here.");
      adminsManualGroup = adminsManualGroupSave.save();
      if (adminsManualGroupSave.getSaveResultType() == SaveResultType.INSERT) {
        gsh_builtin_gshTemplateOutput.addOutputLine("Admins manual group created: " + adminsManualGroup.getName());
         
        // 29. since we just created this, add a rule that removes members if not employees
        RuleApi.groupIntersection(gsh_builtin_grouperSession.getSubject(), adminsManualGroup, employeeGroup);
        gsh_builtin_gshTemplateOutput.addOutputLine("Rule added on Admins manual group to require membership in: " + employeeGroup.getName());
      } else {
        gsh_builtin_gshTemplateOutput.addOutputLine("Admins manual group " + adminsManualGroupSave.getSaveResultType() + ": " + adminsManualGroup.getName());
      }
    }    
    
    // 30. Add manual type to Admins manual
    ADMINS_MANUAL_TYPES: {
      GdgTypeGroupSave gdgTypeGroupSave = new GdgTypeGroupSave().assignGroup(adminsManualGroup).assignType("manual");
      gdgTypeGroupSave.save();
      gsh_builtin_gshTemplateOutput.addOutputLine("Add: manual type on Admins manual group: " + gdgTypeGroupSave.getSaveResultType());
    }
    // 31. Add Owner to manage admin manual group (read/update)
    ADMINS_MANUAL_PRIVILEGES: {
      PrivilegeGroupSave privilegeGroupSave = new PrivilegeGroupSave().assignGroup(adminsManualGroup).assignFieldName("updaters").assignSubject(ownersGroup.toSubject());
      privilegeGroupSave.save();
      gsh_builtin_gshTemplateOutput.addOutputLine("Allowing: Owners group to update Admin manual group: " + privilegeGroupSave.getSaveResultType());
    }
    
    // 32. Add Admin manual group to admins
    ADMINS_MANUAL_TO_ADMIN: {
      MembershipSave membershipSave = new MembershipSave().assignGroup(adminsGroup).assignSubject(adminsManualGroup.toSubject());
      membershipSave.save();
      gsh_builtin_gshTemplateOutput.addOutputLine("Adding: Admin manual group to Admins group: " + membershipSave.getSaveResultType());
    }
    
    // 33. Add READ on VPN ref groups to admins
    for (Group refGroup : GrouperUtil.nonNull(vpnRefGroups, Group.class)) {
      PrivilegeGroupSave privilegeGroupSave = new PrivilegeGroupSave().assignGroup(refGroup).assignFieldName("readers").assignSubject(adminsGroup.toSubject());
      privilegeGroupSave.save();
      gsh_builtin_gshTemplateOutput.addOutputLine("Allowing: READ on " + refGroup.getName() + " to Admins group: " + privilegeGroupSave.getSaveResultType());
    }
    // 34. Add inherited READ on folder to admins
    ADMIN_PRIV_READ_INHERIT: {
      PrivilegeGroupInheritanceSave privilegeGroupInheritanceSave = new PrivilegeGroupInheritanceSave().assignStem(projectFolder).addPrivilegeName("read").assignSubject(adminsGroup.toSubject());
      privilegeGroupInheritanceSave.save();
      gsh_builtin_gshTemplateOutput.addOutputLine("Allowing: Admins group can READ all groups in project folder: " + privilegeGroupInheritanceSave.getSaveResultType());
    }      
    
    // 35. Add admin pennkeys to admin manual group
    for (Subject subject : GrouperUtil.nonNull(adminSubjects, Subject.class)) {
      MembershipSave membershipSave = new MembershipSave().assignGroup(adminsManualGroup).assignSubject(subject);
      membershipSave.save();
      gsh_builtin_gshTemplateOutput.addOutputLine("Adding: " + subject.getDescription() + " to Admin manual group: " + membershipSave.getSaveResultType());
    }
    // 36. Create group include employees
    Group employeesManualGroup = null;
    EMPLOYEES_MANUAL_GROUP: {
      GroupSave employeesManualGroupSave = new GroupSave().assignName(projectStemName + ":" + gsh_input_projectSystemName + "SraVpnEmployeesManual").assignDisplayExtension(gsh_input_projectFriendlyName + " SRA VPN Employees Manual").assignDescription("If employees who use the VPN cannot be managed automatically (e.g. by payroll ORG), then manually manage the list of employees who can use the VPN here.  If you need to add someone to the VPN who is not a VPN, add them to the Guest group.  If there is a way to automate this population (e.g. by query against pcom or warehouse or another PennGroup), please contact help@isc.upenn.edu.");
      employeesManualGroup = employeesManualGroupSave.save();
      if (employeesManualGroupSave.getSaveResultType() == SaveResultType.INSERT) {
        gsh_builtin_gshTemplateOutput.addOutputLine("Employees manual group created: " + employeesManualGroup.getName());
         
        // 37. since we just created this, add a rule that removes members if not employees
        RuleApi.groupIntersection(gsh_builtin_grouperSession.getSubject(), employeesManualGroup, employeeGroup);
        gsh_builtin_gshTemplateOutput.addOutputLine("Rule added on employees manual group to require membership in: " + employeeGroup.getName());
      } else {
        gsh_builtin_gshTemplateOutput.addOutputLine("Employees manual group " + employeesManualGroupSave.getSaveResultType() + ": " + adminsManualGroup.getName());
      }
    }
    // 38. Add manual and ref type to Employees manual
    EMPLOYEES_MANUAL_TYPES: {
      GdgTypeGroupSave gdgTypeGroupSave = new GdgTypeGroupSave().assignGroup(employeesManualGroup).assignType("manual");
      gdgTypeGroupSave.save();
      gsh_builtin_gshTemplateOutput.addOutputLine("Add: manual type on Employees manual group: " + gdgTypeGroupSave.getSaveResultType());
      gdgTypeGroupSave = new GdgTypeGroupSave().assignGroup(employeesManualGroup).assignType("ref");
      gdgTypeGroupSave.save();
      gsh_builtin_gshTemplateOutput.addOutputLine("Add: ref type on Employees manual group: " + gdgTypeGroupSave.getSaveResultType());
    }
    // 39. Add UPDATE on employees group to admins
    ADMINS_MANUAL_PRIVILEGES: {
      PrivilegeGroupSave privilegeGroupSave = new PrivilegeGroupSave().assignGroup(employeesManualGroup).assignFieldName("updaters").assignSubject(adminsGroup.toSubject());
      privilegeGroupSave.save();
      gsh_builtin_gshTemplateOutput.addOutputLine("Allowing: Admins to update Employees manual group: " + privilegeGroupSave.getSaveResultType());
    }
    // 40. Add admin pennkeys to admin manual group
    for (Subject subject : GrouperUtil.nonNull(vpnEmployeeSubjects, Subject.class)) {
      MembershipSave membershipSave = new MembershipSave().assignGroup(employeesManualGroup).assignSubject(subject);
      membershipSave.save();
      gsh_builtin_gshTemplateOutput.addOutputLine("Adding: " + subject.getDescription() + " to VPN employee manual group: " + membershipSave.getSaveResultType());
    }
    // 41. Create group Guest
    Group guestsGroup = null;
    GUESTS_GROUP: {
      GroupSave guestGroupSave = new GroupSave().assignName(projectStemName + ":" + gsh_input_projectSystemName + "SraVpnGuests").assignDisplayExtension(gsh_input_projectFriendlyName + " SRA VPN Guests").assignDescription("Non-employees who need access to the VPN.  This access needs to be removed when not needed.  If you know when the access should end you should put an end-date on the membership.");
      guestsGroup = guestGroupSave.save();
      gsh_builtin_gshTemplateOutput.addOutputLine("Guests group " + guestGroupSave.getSaveResultType() + ": " + guestsGroup.getName());
    }
    
    // 42. Create group Guest
    GUESTS_TYPES: {
      GdgTypeGroupSave gdgTypeGroupSave = new GdgTypeGroupSave().assignGroup(guestsGroup).assignType("manual");
      gdgTypeGroupSave.save();
      gsh_builtin_gshTemplateOutput.addOutputLine("Add: manual type on Guests group: " + gdgTypeGroupSave.getSaveResultType());
      gdgTypeGroupSave = new GdgTypeGroupSave().assignGroup(guestsGroup).assignType("ref");
      gdgTypeGroupSave.save();
      gsh_builtin_gshTemplateOutput.addOutputLine("Add: ref type on Guests group: " + gdgTypeGroupSave.getSaveResultType());
    }
    // 43. Add UPDATE on guests group to admins
    ADMINS_GUESTS_PRIVILEGES: {
      PrivilegeGroupSave privilegeGroupSave = new PrivilegeGroupSave().assignGroup(guestsGroup).assignFieldName("updaters").assignSubject(adminsGroup.toSubject());
      privilegeGroupSave.save();
      gsh_builtin_gshTemplateOutput.addOutputLine("Allowing: Admins to update Guests group: " + privilegeGroupSave.getSaveResultType());
    }
    // 44. Create group excludes
    Group excludesGroup = null;
    EXCLUDES_GROUP: {
      GroupSave excludesGroupSave = new GroupSave().assignName(projectStemName + ":" + gsh_input_projectSystemName + "SraVpnExcludes").assignDisplayExtension(gsh_input_projectFriendlyName + " SRA VPN Excludes").assignDescription("Manual excludes list.  This is for people who are automatically included and need to be excluded");
      excludesGroup = excludesGroupSave.save();
      gsh_builtin_gshTemplateOutput.addOutputLine("Excludes group " + excludesGroupSave.getSaveResultType() + ": " + excludesGroup.getName());
    }
    
    // 45. Create group Guest
    EXCLUDES_TYPES: {
      GdgTypeGroupSave gdgTypeGroupSave = new GdgTypeGroupSave().assignGroup(excludesGroup).assignType("manual");
      gdgTypeGroupSave.save();
      gsh_builtin_gshTemplateOutput.addOutputLine("Add: manual type on Excludes group: " + gdgTypeGroupSave.getSaveResultType());
      gdgTypeGroupSave = new GdgTypeGroupSave().assignGroup(excludesGroup).assignType("ref");
      gdgTypeGroupSave.save();
      gsh_builtin_gshTemplateOutput.addOutputLine("Add: ref type on Excludes group: " + gdgTypeGroupSave.getSaveResultType());
    }
    // 46. Add UPDATE on guests group to admins
    ADMINS_EXCLUDES_PRIVILEGES: {
      PrivilegeGroupSave privilegeGroupSave = new PrivilegeGroupSave().assignGroup(excludesGroup).assignFieldName("updaters").assignSubject(adminsGroup.toSubject());
      privilegeGroupSave.save();
      gsh_builtin_gshTemplateOutput.addOutputLine("Allowing: Admins to update Excludes group: " + privilegeGroupSave.getSaveResultType());
    }
    // 47. Add allow group
    Group allowGroup = null;
    ALLOW_GROUP: {
      GroupSave allowGroupSave = new GroupSave().assignName(projectStemName + ":" + gsh_input_projectSystemName + "SraVpnAllow").assignDisplayExtension(gsh_input_projectFriendlyName + " SRA VPN Allow").assignDescription("This is the allow part of the VPN policy.  This is an internal group that can be ignored.  You do not have access to manage the memberships.  It consists of all the allows to the VPN before the denies.  This group does not directly include individuals, it includes ref groups: the automatic employee groups (orgs/centers), Employees manual group, and Guests.  To add/remove a reference group, open a ticket with help@isc.upenn.edu");
      allowGroup = allowGroupSave.save();
      gsh_builtin_gshTemplateOutput.addOutputLine("Allow group " + allowGroupSave.getSaveResultType() + ": " + allowGroup.getName());
    }
    
    // 48. Add intermediate type to allow group
    ALLOW_TYPES: {
      GdgTypeGroupSave gdgTypeGroupSave = new GdgTypeGroupSave().assignGroup(allowGroup).assignType("intermediate");
      gdgTypeGroupSave.save();
      gsh_builtin_gshTemplateOutput.addOutputLine("Add: intermediate type on Allow group: " + gdgTypeGroupSave.getSaveResultType());
    }
    
    // 49. Add members to allow group, ref groups, employees manual, guests
    ALLOW_MEMBERS: {
      for (Group memberGroup : GrouperUtil.nonNull(vpnRefGroups, Group.class)) {
        MembershipSave membershipSave = new MembershipSave().assignGroup(allowGroup).assignSubject(memberGroup.toSubject());
        membershipSave.save();
        gsh_builtin_gshTemplateOutput.addOutputLine("Adding: " + memberGroup.getName() + " to Allow group: " + membershipSave.getSaveResultType());
      }
      MembershipSave membershipSave = new MembershipSave().assignGroup(allowGroup).assignSubject(employeesManualGroup.toSubject());
      membershipSave.save();
      gsh_builtin_gshTemplateOutput.addOutputLine("Adding: Employee manual group to Allow group: " + membershipSave.getSaveResultType());
      
      membershipSave = new MembershipSave().assignGroup(allowGroup).assignSubject(guestsGroup.toSubject());
      membershipSave.save();
      gsh_builtin_gshTemplateOutput.addOutputLine("Adding: Guests to Allow group: " + membershipSave.getSaveResultType());
    }
    // 50. Add deny group
    Group denyGroup = null;
    DENY_GROUP: {
      GroupSave denyGroupSave = new GroupSave().assignName(projectStemName + ":" + gsh_input_projectSystemName + "SraVpnDeny").assignDisplayExtension(gsh_input_projectFriendlyName + " SRA VPN Deny").assignDescription("This is the deny part of the VPN policy.  This is an internal group that can be ignored.  You do not have access to manage the memberships.  It consists of all the denies to the VPN, which take precendence before the allows.  This group does not directly include individuals, it includes ref groups: the automatic lockout group, and the Excludes (manual) group.  To add/remove a reference group, open a ticket with help@isc.upenn.edu");
      denyGroup = denyGroupSave.save();
      gsh_builtin_gshTemplateOutput.addOutputLine("Deny group " + denyGroupSave.getSaveResultType() + ": " + denyGroup.getName());
    }
    
    // 51. Add intermediate type to deny group
    DENY_TYPES: {
      GdgTypeGroupSave gdgTypeGroupSave = new GdgTypeGroupSave().assignGroup(denyGroup).assignType("intermediate");
      gdgTypeGroupSave.save();
      gsh_builtin_gshTemplateOutput.addOutputLine("Add: intermediate type on Deny group: " + gdgTypeGroupSave.getSaveResultType());
    }
    
    // 52. Add members to deny group, lockout, and Excludes
    DENY_MEMBERS: {
      
      MembershipSave membershipSave = new MembershipSave().assignGroup(denyGroup).assignSubjectSourceId("g:gsa").assignSubjectIdentifier("penn:isc:security:lockout:employeeLockout");
      membershipSave.save();
      gsh_builtin_gshTemplateOutput.addOutputLine("Adding: Lockout group to Deny group: " + membershipSave.getSaveResultType());
      
      membershipSave = new MembershipSave().assignGroup(denyGroup).assignSubject(excludesGroup.toSubject());
      membershipSave.save();
      gsh_builtin_gshTemplateOutput.addOutputLine("Adding: Excludes to Deny group: " + membershipSave.getSaveResultType());
    }
    // 53. Add overall group
    Group overallGroup = null;
    OVERALL_GROUP: {
      GroupSave overallGroupSave = new GroupSave().assignName(projectStemName + ":" + gsh_input_projectSystemName + "SraVpnOverall").assignDisplayExtension(gsh_input_projectFriendlyName + " SRA VPN Overall").assignDescription("This is the VPN policy group of who can use the VPN.  This takes into consideration the allows (automatic and manual), and the denies (automatic and manual).  After membership changes in this group it takes 15-60 minutes to reach LDAP (and the VPN).  You do not have access to manage the memberships.  This group does not directly include individuals, it is the allows minus the denies.  To change the policy, open a ticket with help@isc.upenn.edu");
      overallGroup = overallGroupSave.save();
      if (overallGroupSave.getSaveResultType() == SaveResultType.INSERT) {
        gsh_builtin_gshTemplateOutput.addOutputLine("Overall group created: " + employeesManualGroup.getName());
         
        // 54. since we just created this, provision to AD bushy
        AttributeAssignToGroupSave attributeAssignToGroupSave = new AttributeAssignToGroupSave().assignGroup(overallGroup).assignNameOfAttributeDefName("penn:etc:pspng:provision_to");
        AttributeAssign attributeAssign = attributeAssignToGroupSave.save();
        new AttributeAssignValueSave().assignAttributeAssign(attributeAssign).assignValue("pspng_activedirectoryFull").save();
        gsh_builtin_gshTemplateOutput.addOutputLine("Provisioning: Overall group to Kite AD: " + attributeAssignToGroupSave.getSaveResultType());
        
      } else {
        gsh_builtin_gshTemplateOutput.addOutputLine("Overall group " + overallGroupSave.getSaveResultType() + ": " + overallGroup.getName());
      }
      gsh_builtin_gshTemplateOutput.addOutputLine("Kite: CN=" + gsh_input_projectSystemName + "SraVpnOverall,OU=" + gsh_input_projectSystemName + ",OU=service,OU=sraVpn,OU=service,OU=networking,OU=ts,OU=isc,OU=penn,OU=GrouperFull,OU=LocalAuth,DC=kite,DC=upenn,DC=edu");
    }
    
    // 54.5 Add overall group to SRA overall group
    SRA_OVERALL_GROUP: {
        Group sraOverallGroup = GroupFinder.findByName(gsh_builtin_grouperSession, "penn:isc:ts:networking:service:sraVpn:sraVpnAllUsers", true);
        MembershipSave membershipSave = new MembershipSave().assignGroup(sraOverallGroup).assignSubject(overallGroup.toSubject());
        membershipSave.save();
        gsh_builtin_gshTemplateOutput.addOutputLine("Adding: " + overallGroup.getName() + " to SRA overall users group: " + membershipSave.getSaveResultType());
    }
    // 55. Add policy type to overall group
    OVERALL_TYPES: {
      GdgTypeGroupSave gdgTypeGroupSave = new GdgTypeGroupSave().assignGroup(overallGroup).assignType("policy");
      gdgTypeGroupSave.save();
      gsh_builtin_gshTemplateOutput.addOutputLine("Add: policy type on Overall group: " + gdgTypeGroupSave.getSaveResultType());
    }
    
    // 56. Add members to deny group: lockout, Excludes
    OVERALL_COMPOSITE: {
      CompositeSave compositeSave = new CompositeSave().assignOwnerName(overallGroup.getName()).assignLeftFactorName(allowGroup.getName()).assignType("complement").assignRightFactorName(denyGroup.getName());
      compositeSave.save();
      gsh_builtin_gshTemplateOutput.addOutputLine("Adding: Overall group is composite Allow minus Deny: " + compositeSave.getSaveResultType());
    }
    
    // Owners group should attest, whoever in group gets emails
    String ownersAttestationEmailAddress = ownersGroup.getName() + "@grouper";
  
    // 57. Add to grouper.properties that the owners group can receive email
    GrouperEmail.addAllowEmailToGroup(ownersAttestationEmailAddress);
    // 58. Add attestation to the super admins group to the project folder
    ATTESTATION: {
      AttestationStemSave attestationStemSave = new AttestationStemSave().assignStem(projectFolder).assignGroupCanAttest(adminsGroup);
      attestationStemSave.assignReportConfigName(reportConfigName).addEmailAddress(ownersAttestationEmailAddress).assignSendEmail(true);
      attestationStemSave.assignAttestationType(AttestationType.report).assignDaysUntilRecertify(30);
      // dont reset the date if its likely already there
      if (projectFolderAdded) {
        attestationStemSave.assignMarkAsAttested(true);
      }
      attestationStemSave.save();
      gsh_builtin_gshTemplateOutput.addOutputLine("Attestation: on report: " + attestationStemSave.getSaveResultType());
    }
    // 59. Admins group needs to be in the "manage vpn" template runner group
    TEMPLATE_RUNNER: {
      MembershipSave membershipSave = new MembershipSave().assignGroupName("penn:isc:ts:networking:service:sraVpn:sraVpnManageVpnTemplateRunners").assignSubject(adminsGroup.toSubject());
      membershipSave.save();
      gsh_builtin_gshTemplateOutput.addOutputLine("Adding: Admins group to Manage VPN template runners group: " + membershipSave.getSaveResultType());
      
    }
    
    // 60. add template URL in email
    
    // 61. send email
    if (emailTos.size() > 0) {
      String emailTo = StringUtils.join(emailTos.iterator(), ",");
      new GrouperEmail().setTo(emailTo).setSubject("SRA VPN for " + gsh_input_projectFriendlyName).setBody(emailBody.toString()).send();
      gsh_builtin_gshTemplateOutput.addOutputLine("Sent email with links and instructions to: " + emailTo);
    }
   
    // done!
    gsh_builtin_gshTemplateOutput.addOutputLine("Finished running SRA VPN template for project: " + gsh_input_projectFriendlyName);
    
    // 62. kick off dependent jobs on daemon (hopefully this tx commits before they run)
    GrouperLoader.runOnceByJobName(gsh_builtin_grouperSession, "OTHER_JOB_sraVpnMships", true);
    GrouperLoader.runOnceByJobName(gsh_builtin_grouperSession, "OTHER_JOB_sraVpnTimes", true);
    GrouperLoader.runOnceByJobName(gsh_builtin_grouperSession, "OTHER_JOB_sraVpnPit", true);
```
