define(`_SHUTDOWN',dnl
ifdef(`HSQLDB', `SHUTDOWN COMPACT;' `')dnl
)dnl

-- 
-- Grouper Test Data
-- 

INSERT INTO grouper_subject (subjectID, subjectTypeID)
  VALUES ('blair', 'person');
INSERT INTO grouper_subject (subjectID, subjectTypeID)
  VALUES ('notblair', 'person');

COMMIT;
_SHUTDOWN()

