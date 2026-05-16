Map<String, Object> elVariableMap = new HashMap<String, Object>();

// Plain text doesn't reference any variables, but the variable map is still seeded
// so you can switch to a more complex template without losing context.
elVariableMap.put("groupDisplayExtension", "Accounting");
