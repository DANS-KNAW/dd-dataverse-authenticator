CREATE TABLE IF NOT EXISTS builtinuser (
    id integer NOT NULL,
    encryptedpassword character varying(255),
    passwordencryptionversion integer,
    username character varying(255) NOT NULL
);

DELETE FROM builtinuser;

INSERT INTO builtinuser (id, encryptedpassword, passwordencryptionversion, username)
VALUES
    (1, '$2a$10$nBBwLlls757bzXY30ts8duy1ymEJKhGxZgdWDBEOwmMkXXvv2C3CC', 1, 'dataverseAdmin'),
    (2, 'hN/rof975YOfV0wcZVXCrpU8ZlY=', 0, 'user001'),
    (3, 'EwkfCo7O85qPEM/39U2hTw+3ehE=', 0, 'user002'),
    (4, 'CxMv+h/czMwZA554OwNVpibqxxA=', 0, 'user003');


