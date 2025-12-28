String prefixLower = gsh_input_prefix.toLowerCase();
Group excludeAdHocGroup = new GroupSave(gsh_builtin_grouperSession).assignName("penn:isc:ait:apps:zoom:service:ref:excludeAdHoc:" + prefixLower + "AdhocExcludeFromZoom").assignCreateParentStemsIfNotExist(true).save();
Group excludeLoadedGroup = new GroupSave(gsh_builtin_grouperSession).assignName("penn:isc:ait:apps:zoom:service:ref:loadedGroupsForExclude:" + prefixLower + "ExcludeLoaded").assignCreateParentStemsIfNotExist(true).save();
Group excludeGroup = new GroupSave(gsh_builtin_grouperSession).assignName("penn:isc:ait:apps:zoom:service:ref:excludeFromZoom:" + prefixLower + "ExcludeFromZoom").assignCreateParentStemsIfNotExist(true).save();
excludeGroup.addMember(excludeAdHocGroup.toSubject(), false);
excludeGroup.addMember(excludeLoadedGroup.toSubject(), false);
Group excludedFromZoom = new GroupSave(gsh_builtin_grouperSession).assignName("penn:isc:ait:apps:zoom:service:ref:usersExcludedFromZoom").assignCreateParentStemsIfNotExist(true).save();
excludedFromZoom.addMember(excludeGroup.toSubject(), false);
Group schoolLspGroup = new GroupSave(gsh_builtin_grouperSession).assignName("penn:isc:ait:apps:zoom:security:schoolCenterAdminsAndLsps:zoom" + gsh_input_prefix + "Lsps").assignCreateParentStemsIfNotExist(true).save();

Group lsps = new GroupSave(gsh_builtin_grouperSession).assignName("penn:isc:ait:apps:zoom:security:zoomSchoolCenterLspsPreCheck").assignCreateParentStemsIfNotExist(true).save();
lsps.addMember(schoolLspGroup.toSubject(), false);
Group schoolAdminGroup = new GroupSave(gsh_builtin_grouperSession).assignName("penn:isc:ait:apps:zoom:security:schoolCenterAdminsAndLsps:zoom" + gsh_input_prefix + "Admins").assignCreateParentStemsIfNotExist(true).save();
Group admins = new GroupSave(gsh_builtin_grouperSession).assignName("penn:isc:ait:apps:zoom:security:zoomSchoolCenterAdminsPreCheck").assignCreateParentStemsIfNotExist(true).save();
admins.addMember(schoolAdminGroup.toSubject(), false);









