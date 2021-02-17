
if (!gsh_input_myExtension.startsWith("zoom")) {

	gsh_builtin_gshTemplateOutput.addValidationLine(gsh_input_myExtension, "Extension must start with zoom"); 
	
	gsh_builtin_gshTemplateOutput.assignIsError(true); 
	
	GrouperUtil.gshReturn();

}

new GroupSave(gsh_builtin_grouperSession).assignName("test:test-"+gsh_input_myExtension).assignCreateParentStemsIfNotExist(true).assignDescription("test description").save(); 

Group group = GroupFinder.findByName(gsh_builtin_grouperSession, "test:test-"+gsh_input_myExtension, true); 

gsh_builtin_gshTemplateOutput.addOutputLine("Created group: "+group.getName());





