define(`_SHUTDOWN',dnl
ifdef(`HSQLDB', `SHUTDOWN COMPACT;' `')dnl
)dnl

-- 
-- Grouper Test Data
-- 

INSERT INTO grouper_subject (subjectID, subjectTypeID)
  VALUES ('member 0', 'person');
INSERT INTO grouper_subject (subjectID, subjectTypeID)
  VALUES ('member 1', 'person');

COMMIT;
_SHUTDOWN()

