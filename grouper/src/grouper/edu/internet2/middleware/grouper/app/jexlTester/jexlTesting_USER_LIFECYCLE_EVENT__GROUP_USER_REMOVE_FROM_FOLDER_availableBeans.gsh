Map<String, Object> elVariableMap = new HashMap<String, Object>();

// Variables exposed for the groupUserRemoveFromFolder trigger.
// The daemon evaluates the template once per child group in the folder,
// so both the per-child group variables and the parent stem variables are bound.
elVariableMap.put("groupName",             "test:departments:accounting");
elVariableMap.put("groupDisplayName",      "Test:Departments:Accounting");
elVariableMap.put("groupExtension",        "accounting");
elVariableMap.put("groupDisplayExtension", "Accounting");
elVariableMap.put("groupDescription",      "Accounting department");

elVariableMap.put("stemName",              "test:departments");
elVariableMap.put("stemDisplayName",       "Test:Departments");
elVariableMap.put("stemExtension",         "departments");
elVariableMap.put("stemDisplayExtension",  "Departments");
elVariableMap.put("stemDescription",       "All departments");
