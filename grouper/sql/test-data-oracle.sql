
-- 
-- Grouper Test Data
-- 

DELETE FROM Subject WHERE subjectID='member 0';
INSERT INTO Subject (subjectID, subjectTypeID)
  VALUES ('member 0', 'person');
DELETE FROM Subject WHERE subjectID='member 1';
INSERT INTO Subject (subjectID, subjectTypeID)
  VALUES ('member 1', 'person');

COMMIT;


