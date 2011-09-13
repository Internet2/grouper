insert into subject (subjectId, subjectTypeId, subject.name) 
values ('12345', 'person', 'John Smith');

insert into subject (subjectId, subjectTypeId, subject.name) 
values ('98765', 'person', 'Sara Davis');

insert into subject (subjectId, subjectTypeId, subject.name) 
values ('54321', 'person', 'Ryan Jones');

insert into subject (subjectId, subjectTypeId, subject.name) 
values ('56789', 'person', 'Julia Clark');

commit;

insert into subjectattribute (subjectId, name, value, searchValue) 
values ('12345', 'description', 'John Smith', 'john smith');
insert into subjectattribute (subjectId, name, value, searchValue) 
values ('12345', 'name', 'John Smith', 'john smith');
insert into subjectattribute (subjectId, name, value, searchValue) 
values ('12345', 'loginid', 'js', 'js');
commit;

insert into subjectattribute (subjectId, name, value, searchValue) 
values ('98765', 'description', 'Sara Davis', 'sara davis');
insert into subjectattribute (subjectId, name, value, searchValue) 
values ('98765', 'name', 'Sara Davis', 'sara davis');
insert into subjectattribute (subjectId, name, value, searchValue) 
values ('98765', 'loginid', 'sd', 'sd');
commit;

insert into subjectattribute (subjectId, name, value, searchValue) 
values ('54321', 'description', 'Ryan Jones', 'ryan jones');
insert into subjectattribute (subjectId, name, value, searchValue) 
values ('54321', 'name', 'Ryan Jones', 'ryan jones');
insert into subjectattribute (subjectId, name, value, searchValue) 
values ('54321', 'loginid', 'rj', 'rj');
commit;

insert into subjectattribute (subjectId, name, value, searchValue) 
values ('56789', 'description', 'Julia Clark', 'julia clark');
insert into subjectattribute (subjectId, name, value, searchValue) 
values ('56789', 'name', 'Julia Clark', 'julia clark');
insert into subjectattribute (subjectId, name, value, searchValue) 
values ('56789', 'loginid', 'jc', 'jc');
commit;


