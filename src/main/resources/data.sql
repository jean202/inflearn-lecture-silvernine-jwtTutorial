
INSERT INTO USER (username, password, nickname, activated) VALUES ('admin', '$2a$08$lDnHPz7eUkSi6ao14Twuau08mzhWrL4kyZGGU5xfiGALO/Vxd5DOi', 'adminUser', 1);
INSERT INTO USER (username, password, nickname, activated) VALUES ('user', '$2a$12$UrPkJz/jGehUnspJJKY0RucCgkoXNqySP4ZRy21kQz7cgHOf.3pkK', 'userUser', 1);

INSERT INTO AUTHORITY (AUTHORITY_NAME) VALUES ('ROLE_USER');
INSERT INTO AUTHORITY (AUTHORITY_NAME) VALUES ('ROLE_ADMIN');

INSERT INTO USER_AUTHORITY (USER_ID, AUTHORITY_NAME) VALUES (1, 'ROLE_USER');
INSERT INTO USER_AUTHORITY (USER_ID, AUTHORITY_NAME) VALUES (1, 'ROLE_ADMIN');

INSERT INTO USER_AUTHORITY (user_id, authority_name) values (2, 'ROLE_USER');