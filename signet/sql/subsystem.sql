# --- Create "financial" subsystem and its functions/categories for Signet Demo1 database (Fall, 2004)

insert into Subsystem
   values ('financial','Dec 1 2004 12:00am','active','Financial Systems','Help text...','adminorgs','Dec 1 2004 12:00am','signet',NULL,NULL,'signet',NULL,NULL,NULL)

insert into Category
   values ('ap','Dec 1 2004 12:00am','financial','active','Accounts Payable','Dec 1 2004 12:00am','signet',NULL,NULL,'signet',NULL,'Demo1 load',NULL)
insert into Category
   values ('appr','Dec 1 2004 12:00am','financial','active','Approvals','Dec 1 2004 12:00am','signet',NULL,NULL,'signet',NULL,'Demo1 load',NULL)
insert into Category
   values ('glcon','Dec 1 2004 12:00am','financial','active','General Ledger','Dec 1 2004 12:00am','signet',NULL,NULL,'signet',NULL,'Demo1 load',NULL)
   
insert into Function
   values ('ap_card','Dec 1 2004 12:00am','financial','ap','active','AP Card Coordinator','Help text...','Dec 1 2004 12:00am','signet',NULL,NULL,'signet',NULL,'Demo1 load',NULL)
insert into Function
   values ('ap_manager','Dec 1 2004 12:00am','financial','ap','active','AP Manager','Help text...','Dec 1 2004 12:00am','signet',NULL,NULL,'signet',NULL,'Demo1 load',NULL)
insert into Function
   values ('ap_supervisor','Dec 1 2004 12:00am','financial','ap','active','AP Supervisor','Help text...','Dec 1 2004 12:00am','signet',NULL,NULL,'signet',NULL,'Demo1 load',NULL)
insert into Function
   values ('appr_lab_animals','Dec 1 2004 12:00am','financial','appr','active','Lab Animals','Help text...','Dec 1 2004 12:00am','signet',NULL,NULL,'signet',NULL,'Demo1 load',NULL)
insert into Function
   values ('appr_requisitions','Dec 1 2004 12:00am','financial','appr','active','Requisitions','Help text...','Dec 1 2004 12:00am','signet',NULL,NULL,'signet',NULL,'Demo1 load',NULL)
insert into Function
   values ('glcon_gl_administrator','Dec 1 2004 12:00am','financial','glcon','active','Consolidated GL Administrator','Help text...','Dec 1 2004 12:00am','signet',NULL,NULL,'signet',NULL,'Demo1 load',NULL)

