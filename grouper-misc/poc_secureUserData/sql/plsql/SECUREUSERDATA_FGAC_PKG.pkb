CREATE OR REPLACE PACKAGE BODY secureuserdata_fgac_pkg 
IS
   
    PROCEDURE show_context
    IS
     seconds_since_1970 number := (sysdate-c_1970) * 60 * 60 * 24;
    BEGIN
       set_context_if_needed();
       DBMS_OUTPUT.PUT_LINE ('Schema: ' || 
          SYS_CONTEXT (c_context, c_schema));
       DBMS_OUTPUT.PUT_LINE ('Context last updated: ' || 
           (seconds_since_1970 - to_number(SYS_CONTEXT (c_context, c_context_created)) || ' seconds ago'));
       DBMS_OUTPUT.PUT_LINE ('Select rows where clause: ' ||
          userdata_select_rows_predicate (USER, 'SECUREUSERDATA_USER'));
       DBMS_OUTPUT.PUT_LINE ('Update rows where clause: ' ||
          userdata_update_rows_predicate (USER, 'SECUREUSERDATA_USER'));
       DBMS_OUTPUT.PUT_LINE ('Select cols ids: ' ||
          userdata_select_cols_ids (USER, 'SECUREUSERDATA_USER'));
       DBMS_OUTPUT.PUT_LINE ('Update cols ids: ' ||
          userdata_update_cols_ids (USER, 'SECUREUSERDATA_USER'));
       DBMS_OUTPUT.PUT_LINE ('Select cols name: ' ||
          userdata_select_cols_name (USER, 'SECUREUSERDATA_USER'));
       DBMS_OUTPUT.PUT_LINE ('Update cols name: ' ||
          userdata_update_cols_name (USER, 'SECUREUSERDATA_USER'));
       DBMS_OUTPUT.PUT_LINE ('Select cols contact: ' ||
          userdata_select_cols_contact (USER, 'SECUREUSERDATA_USER'));
       DBMS_OUTPUT.PUT_LINE ('Update cols contact: ' ||
          userdata_update_cols_contact (USER, 'SECUREUSERDATA_USER'));
    END;

   PROCEDURE set_context_if_needed is
   
     CURSOR cols_cursor (schema_in IN varchar2) IS
       SELECT distinct colset, action FROM secureuserdata_col_permiss
         WHERE schema_name = schema_in;

     CURSOR rows_cursor (schema_in IN varchar2) IS
       SELECT distinct group_extension, action FROM secureuserdata_row_permiss
         WHERE schema_name = schema_in;

     context_last_set number;
     seconds_since_1970 number := (sysdate-c_1970) * 60 * 60 * 24;
     the_schema varchar2(32);
     col_context_var_name varchar2(100);
   begin
     
     --lets see when it was last set
     context_last_set := SYS_CONTEXT (c_context, c_context_created);

     if ((context_last_set is not null) and (seconds_since_1970 - to_number(context_last_set) < c_seconds_context_expires)) then
       -- we already have the context, dont set again
       return;
     end if;
     
     --carry this forward
     the_schema := SYS_CONTEXT (c_context, c_schema);
     
     --clear out the old security
     dbms_session.clear_context(c_context);
     
     --note the time context was calculated
     DBMS_SESSION.SET_CONTEXT (
           c_context, c_context_created, seconds_since_1970);
     
     --defaults to the calling schema
     if (the_schema is null or the_schema = '') then
       the_schema := user;
     end if;

     DBMS_SESSION.SET_CONTEXT (
           c_context, c_schema, the_schema);

     for the_row in cols_cursor(the_schema) loop

       --the action underscore col set will be on the end, the value is T or F
       col_context_var_name := c_col_prefix || the_row.action || '_' || the_row.colset;
       DBMS_SESSION.SET_CONTEXT (
           c_context, col_context_var_name, 'T');
     end loop;

     for the_row in rows_cursor(the_schema) loop

       --the action underscore col set will be on the end, the value is T or F
       if (the_row.group_extension = 'all') then
         if (the_row.action = 'read') then

           DBMS_SESSION.SET_CONTEXT (
             c_context, c_row_select_all, 'T');

         elsif (the_row.action = 'write') then

           DBMS_SESSION.SET_CONTEXT (
             c_context, c_row_update_all, 'T');
         
         end if;
       end if;
     end loop;

     --TODO take this out
     dbms_output.put_line('Set context, schema: ' || the_schema);
   end;

   PROCEDURE set_context_backdoor(as_schema varchar2) is
   begin
     if (user <> c_schema_owner) then
       RAISE_APPLICATION_ERROR(-20000,'Schema ' || user || 'not allowed to backdoor as ' || as_schema);
     end if;
     dbms_session.clear_context(c_context, c_context_created);
     DBMS_SESSION.SET_CONTEXT (
           c_context, c_schema, as_schema);
     set_context_if_needed();
   end;
   
   FUNCTION user_schema RETURN VARCHAR2 IS
     the_schema varchar2(32);
   begin
     the_schema := SYS_CONTEXT (c_context, c_schema);
     
     if (the_schema is null or the_schema = '') then
       set_context_if_needed();
       the_schema := SYS_CONTEXT (c_context, c_schema);
       if (the_schema is null or the_schema = '') then
         RAISE_APPLICATION_ERROR(-20001,'Cant find schema in context');
       end if;
     end if;
  
     return the_schema;    
   end; 


   FUNCTION userdata_select_rows_predicate (
      schema_in VARCHAR2, 
      name_in VARCHAR2)
   RETURN VARCHAR2 is
     the_schema varchar2(32);
     can_use_all varchar2(1);
   begin
     set_context_if_needed();
     the_schema := SYS_CONTEXT (c_context, c_schema);
     can_use_all := SYS_CONTEXT (c_context, c_row_select_all);
     
     --if we are the backdoor schema, or allowed all, then allow all
     if (the_schema = c_schema_owner or can_use_all = 'T') then
       return null;
     end if;
     
     return ' personid in (select sm.personid '
       || ' from fastdev2.secureuserdata_memberships sm, fastdev2.secureuserdata_row_permiss srp '
       || ' where sm.group_extension = srp.group_extension '
       || ' and srp.action = ''read'' '
       || ' and srp.schema_name = fastdev2.secureuserdata_fgac_pkg.user_schema() ) ';
   end;

   FUNCTION userdata_update_rows_predicate (
      schema_in VARCHAR2, 
      name_in VARCHAR2)
   RETURN VARCHAR2 is
     the_schema varchar2(32);
     can_use_all varchar2(1);
   begin
     set_context_if_needed();
     the_schema := SYS_CONTEXT (c_context, c_schema);
     can_use_all := SYS_CONTEXT (c_context, c_row_update_all);

     --if we are the backdoor schema, then allow all
     if (the_schema = c_schema_owner or can_use_all = 'T') then
       return null;
     end if;
     

     return ' personid in (select sm.personid '
       || ' from fastdev2.secureuserdata_memberships sm, fastdev2.secureuserdata_row_permiss srp '
       || ' where sm.group_extension = srp.group_extension '
       || ' and srp.action = ''write'' '
       || ' and srp.schema_name = fastdev2.secureuserdata_fgac_pkg.user_schema() ) ';
   end;

   FUNCTION userdata_select_cols_ids (
      schema_in VARCHAR2, 
      name_in VARCHAR2)
   RETURN VARCHAR2 is
   allowed varchar2(1);
   begin

     set_context_if_needed();

     --if we are the backdoor schema, then allow all
     if (SYS_CONTEXT (c_context, c_schema) = c_schema_owner) then
       return '1=1';
     end if;

     allowed := SYS_CONTEXT (c_context,c_col_prefix || 'read_ids');
     if (allowed = 'T') then
       return '1=1';
     end if;
     return '1=0';
   end;

   FUNCTION userdata_update_cols_ids (
      schema_in VARCHAR2, 
      name_in VARCHAR2)
   RETURN VARCHAR2 is
   allowed varchar2(1);
   begin
     set_context_if_needed();

     --if we are the backdoor schema, then allow all
     if (SYS_CONTEXT (c_context, c_schema) = c_schema_owner) then
       return '1=1';
     end if;

     allowed := SYS_CONTEXT (c_context,c_col_prefix || 'write_ids');
     if (allowed = 'T') then
       return '1=1';
     end if;
     return '1=0';
   end;

   FUNCTION userdata_select_cols_name (
      schema_in VARCHAR2, 
      name_in VARCHAR2)
   RETURN VARCHAR2 is
   allowed varchar2(1);
   begin
     set_context_if_needed();

     --if we are the backdoor schema, then allow all
     if (SYS_CONTEXT (c_context, c_schema) = c_schema_owner) then
       return '1=1';
     end if;

     allowed := SYS_CONTEXT (c_context,c_col_prefix || 'read_name');
     if (allowed = 'T') then
       return '1=1';
     end if;
     return '1=0';
   end;

   FUNCTION userdata_update_cols_name (
      schema_in VARCHAR2, 
      name_in VARCHAR2)
   RETURN VARCHAR2 is
   allowed varchar2(1);
   begin
     set_context_if_needed();

     --if we are the backdoor schema, then allow all
     if (SYS_CONTEXT (c_context, c_schema) = c_schema_owner) then
       return '1=1';
     end if;

     allowed := SYS_CONTEXT (c_context,c_col_prefix || 'write_name');
     if (allowed = 'T') then
       return '1=1';
     end if;
     return '1=0';
   end;

   FUNCTION userdata_select_cols_contact (
      schema_in VARCHAR2, 
      name_in VARCHAR2)
   RETURN VARCHAR2 is
   allowed varchar2(1);
   begin
     set_context_if_needed();

     --if we are the backdoor schema, then allow all
     if (SYS_CONTEXT (c_context, c_schema) = c_schema_owner) then
       return '1=1';
     end if;

     allowed := SYS_CONTEXT (c_context,c_col_prefix || 'read_contact');
     if (allowed = 'T') then
       return '1=1';
     end if;
     return '1=0';
   end;

   FUNCTION userdata_update_cols_contact (
      schema_in VARCHAR2, 
      name_in VARCHAR2)
   RETURN VARCHAR2 is
   allowed varchar2(1);
   begin
     set_context_if_needed();
     allowed := SYS_CONTEXT (c_context,c_col_prefix || 'write_contact');
     if (allowed = 'T') then
       return '1=1';
     end if;
     return '1=0';
   end;



END secureuserdata_fgac_pkg;
/

