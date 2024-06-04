DELETE FROM role_authorities WHERE role_id='role_id_admin';
DELETE FROM role_authorities WHERE role_id='role_id_member';
DELETE FROM role WHERE id='role_id_admin';
DELETE FROM role WHERE id='role_id_member';


INSERT INTO role (id, role_name) VALUES ('role_id_admin', 'admin');
INSERT INTO role (id, role_name) VALUES ('role_id_member', 'member');
INSERT INTO role_authorities (role_id, authorities) VALUES ('role_id_admin', '0');
INSERT INTO role_authorities (role_id, authorities) VALUES ('role_id_member', '3');
INSERT INTO role_authorities (role_id, authorities) VALUES ('role_id_member', '4');