CREATE TABLE IF NOT EXISTS builtinuser (
    id integer NOT NULL PRIMARY KEY,
    encryptedpassword character varying(255),
    passwordencryptionversion integer,
    username character varying(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS authenticateduser (
    id integer NOT NULL PRIMARY KEY,
    deactivated bool NOT NULL DEFAULT FALSE,
    useridentifier varchar(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS apitoken (
    id integer NOT NULL PRIMARY KEY,
    disabled bool NOT NULL DEFAULT FALSE,
    tokenstring varchar(255) NOT NULL,
    authenticateduser_id integer NOT NULL REFERENCES authenticateduser(id)
);

--- test data
DELETE FROM builtinuser;
DELETE FROM apitoken;
DELETE FROM authenticateduser;

INSERT INTO builtinuser (id, encryptedpassword, passwordencryptionversion, username)
VALUES
    (1, '$2a$10$nBBwLlls757bzXY30ts8duy1ymEJKhGxZgdWDBEOwmMkXXvv2C3CC', 1, 'dataverseAdmin'),
    (2, 'hN/rof975YOfV0wcZVXCrpU8ZlY=', 0, 'user001'),
    (3, 'EwkfCo7O85qPEM/39U2hTw+3ehE=', 0, 'user002'),
    (4, 'CxMv+h/czMwZA554OwNVpibqxxA=', 0, 'user003');

--- user001 is OK, permission granted
--- user002 is disabled and deactivated, permission denied
--- user003 is disabled and not deactivated, permission denied
--- user004 is not disabled and deactivated, permission denied
INSERT INTO authenticateduser (id, deactivated, useridentifier)
VALUES
    (1, false, 'user001'),
    (2, true, 'user002'),
    (3, false, 'user003'),
    (4, true, 'user004');

INSERT INTO apitoken (id, disabled, tokenstring, authenticateduser_id)
VALUES
    (1, false, 'token1', 1),
    (2, true, 'token2', 2),
    (3, true, 'token3', 3),
    (4, false, 'token4', 4);
