begin
  dbms_rls.drop_policy (object_schema => 'FASTDEV1',
   object_name => 'secureuserdata_user',
   policy_name => 'sud_fgac_select_rows');
end;
begin
  dbms_rls.drop_policy (object_schema => 'FASTDEV1',
   object_name => 'secureuserdata_user',
   policy_name => 'sud_fgac_update_rows');
end;
begin
  dbms_rls.drop_policy (object_schema => 'FASTDEV1',
   object_name => 'secureuserdata_user',
   policy_name => 'sud_fgac_select_cols_ids');
end;
begin
  dbms_rls.drop_policy (object_schema => 'FASTDEV1',
   object_name => 'secureuserdata_user',
   policy_name => 'sud_fgac_update_cols_ids');
end;
begin
  dbms_rls.drop_policy (object_schema => 'FASTDEV1',
   object_name => 'secureuserdata_user',
   policy_name => 'sud_fgac_select_cols_name');
end;
begin
  dbms_rls.drop_policy (object_schema => 'FASTDEV1',
   object_name => 'secureuserdata_user',
   policy_name => 'sud_fgac_update_cols_name');
end;
begin
  dbms_rls.drop_policy (object_schema => 'FASTDEV1',
   object_name => 'secureuserdata_user',
   policy_name => 'sud_fgac_select_cols_contact');
end;
begin
  dbms_rls.drop_policy (object_schema => 'FASTDEV1',
   object_name => 'secureuserdata_user',
   policy_name => 'sud_fgac_update_cols_contact');
end;
