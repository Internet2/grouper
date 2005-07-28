define(`_SHUTDOWN',dnl
ifdef(`hsqldb', `SHUTDOWN COMPACT;' `')dnl
)dnl

-- 
-- Grouper Test Data
-- 

DELETE FROM Subject WHERE subjectID='member 0';
INSERT INTO Subject (subjectID, subjectTypeID)
  VALUES ('member 0', 'person');
DELETE FROM Subject WHERE subjectID='member 1';
INSERT INTO Subject (subjectID, subjectTypeID)
  VALUES ('member 1', 'person');
DELETE FROM Subject WHERE subjectID='member 2';
INSERT INTO Subject (subjectID, subjectTypeID)
  VALUES ('member 2', 'person');
DELETE FROM Subject WHERE subjectID='member 3';
INSERT INTO Subject (subjectID, subjectTypeID)
  VALUES ('member 3', 'person');
DELETE FROM Subject WHERE subjectID='member 4';
INSERT INTO Subject (subjectID, subjectTypeID)
  VALUES ('member 4', 'person');

COMMIT;
_SHUTDOWN()

