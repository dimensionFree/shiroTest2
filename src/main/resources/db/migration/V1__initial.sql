INSERT INTO role (id, role_name) VALUES ('id_admin', 'admin');
INSERT INTO role (id, role_name) VALUES ('id_member', 'member');
INSERT INTO role_authorities (role_id, authorities) VALUES ('id_admin', '0');
INSERT INTO role_authorities (role_id, authorities) VALUES ('id_member', '3');
INSERT INTO role_authorities (role_id, authorities) VALUES ('id_member', '4');