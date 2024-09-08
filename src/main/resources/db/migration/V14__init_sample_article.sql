ALTER TABLE article MODIFY COLUMN content TEXT;
INSERT INTO article (id, title, preface, content,created_by,created_date)
VALUES ('1',
'Sample Title',
 'This is a sample preface.',
 'This is the content of the sample article. It contains multiple sentences. The article is meant to demonstrate basic functionality.',
'id_admin',
'2024-07-22T03:02:28.896970100'
);
