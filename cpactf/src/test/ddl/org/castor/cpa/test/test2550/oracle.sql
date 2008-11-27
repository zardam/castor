DROP TABLE TEST2550_ENTITY CASCADE CONSTRAINTS;

DROP SEQUENCE TEST2550_ENTITY_SEQ

CREATE TABLE TEST2550_ENTITY (
    ID      INT           NOT NULL,
    NAME    VARCHAR(200)  NOT NULL
);

ALTER TABLE TEST2550_ENTITY
ADD PRIMARY KEY (ID);

CREATE SEQUENCE TEST2550_ENTITY_SEQ
MAXVALUE 2147483647 INCREMENT BY 1 START WITH 1;