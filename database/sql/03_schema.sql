--
-- Tables
--

-- Anagrafiche

CREATE TABLE IF NOT EXISTS ANA_REGIONI (
  ID_REGIONE          VARCHAR(3)     NOT NULL,
  DESCRIZIONE         VARCHAR(50),
  CONSTRAINT PK_ANA_REGIONI PRIMARY KEY (ID_REGIONE));

CREATE TABLE IF NOT EXISTS ANA_PROVINCE (
  ID_PROVINCIA        VARCHAR(3)     NOT NULL,
  DESCRIZIONE         VARCHAR(50),
  ID_REGIONE          VARCHAR(3)     NOT NULL,
  CONSTRAINT PK_ANA_PROVINCE PRIMARY KEY (ID_PROVINCIA),
  CONSTRAINT FK_ANA_PRO_REG  FOREIGN KEY (ID_REGIONE) REFERENCES ANA_REGIONI(ID_REGIONE));

CREATE TABLE IF NOT EXISTS ANA_COMUNI (
  ID_COMUNE           VARCHAR(6)     NOT NULL,
  FISCALE             VARCHAR(4),
  PROVINCIA           VARCHAR(3),
  DESCRIZIONE         VARCHAR(50),
  ID_REGIONE          VARCHAR(3)     NOT NULL,
  CONSTRAINT PK_ANA_COMUNI PRIMARY KEY (ID_COMUNE),
  CONSTRAINT FK_ANA_COM_REG FOREIGN KEY (ID_REGIONE) REFERENCES ANA_REGIONI(ID_REGIONE));

--
-- Indexes
--

CREATE INDEX IDX_ANA_COM_DES ON ANA_COMUNI(DESCRIZIONE);