-- =============================================================================
-- MÓDULO: PERSONAL
-- =============================================================================
-- Esquema: SLB_PERSONAL
-- Tablas: PERSONA, PREFERENCIA_COMUNICACION, CREDENCIAL_PROGRAMA

-- =============================================================================
-- 1. CREACIÓN DEL USUARIO / ESQUEMA DEL MÓDULO
-- =============================================================================
CREATE USER SLB_PERSONAL IDENTIFIED BY "123456"
  DEFAULT TABLESPACE USERS
  TEMPORARY TABLESPACE TEMP
  QUOTA UNLIMITED ON USERS;

-- =============================================================================
-- 2. ASIGNACIÓN DE PRIVILEGIOS
-- =============================================================================
GRANT CREATE SESSION TO SLB_PERSONAL;
GRANT CREATE TABLE TO SLB_PERSONAL;
GRANT CREATE SEQUENCE TO SLB_PERSONAL;
GRANT CREATE TRIGGER TO SLB_PERSONAL;

-- =============================================================================
-- 3. CAMBIO DE CONTEXTO AL ESQUEMA CREADO
-- =============================================================================
ALTER SESSION SET CURRENT_SCHEMA = SLB_PERSONAL;

-- =============================================================================
-- 4. CREACIÓN DE TABLAS DEL MÓDULO
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 4.1. PERSONA
-- -----------------------------------------------------------------------------
CREATE TABLE PERSONA (
    ID_PERSONA NUMBER GENERATED ALWAYS AS IDENTITY CONSTRAINT PK_PERSONA PRIMARY KEY,
    NOMBRES VARCHAR2(100) NOT NULL,
    APELLIDO_PATERNO VARCHAR2(100) NOT NULL,
    APELLIDO_MATERNO VARCHAR2(100) NOT NULL,
    CELULAR VARCHAR2(15) NOT NULL CONSTRAINT UQ_PERSONA_CELULAR UNIQUE,
    FECHA_NACIMIENTO DATE NOT NULL,
    SEXO VARCHAR2(10) NOT NULL,
    TALLA_POLO VARCHAR2(10),
    ID_TEAM NUMBER,
    ACTIVO NUMBER(1) NOT NULL,
    FECHA_CREACION TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- -----------------------------------------------------------------------------
-- 4.2. PREFERENCIA_COMUNICACION
-- -----------------------------------------------------------------------------
CREATE TABLE PREFERENCIA_COMUNICACION (
    ID_PREFERENCIA NUMBER GENERATED ALWAYS AS IDENTITY CONSTRAINT PK_PREFERENCIA_COMUNICACION PRIMARY KEY,
    CANAL_PREFERIDO VARCHAR2(50) NOT NULL,
    HORARIO_CONTACTO_INICIO VARCHAR2(8),
    HORARIO_CONTACTO_FIN VARCHAR2(8),
    ACEPTA_RECORDATORIOS NUMBER(1) NOT NULL,
    ID_PERSONA NUMBER NOT NULL,
    FECHA_CREACION TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT UQ_PREFERENCIA_COMUNICACION_PERSONA UNIQUE (ID_PERSONA),
    CONSTRAINT FK_PREFERENCIA_COMUNICACION_PERSONA FOREIGN KEY (ID_PERSONA)
        REFERENCES PERSONA(ID_PERSONA) ON DELETE CASCADE
);

-- -----------------------------------------------------------------------------
-- 4.3. CREDENCIAL_PROGRAMA
-- -----------------------------------------------------------------------------
CREATE TABLE CREDENCIAL_PROGRAMA (
    ID_CREDENCIAL NUMBER GENERATED ALWAYS AS IDENTITY CONSTRAINT PK_CREDENCIAL_PROGRAMA PRIMARY KEY,
    CODIGO_QR_HASH VARCHAR2(255) NOT NULL CONSTRAINT UQ_CREDENCIAL_PROGRAMA_CODIGO_QR UNIQUE,
    TIPO_CREDENCIAL VARCHAR2(50) NOT NULL,
    FECHA_EMISION TIMESTAMP NOT NULL,
    ESTADO VARCHAR2(20) NOT NULL,
    ID_PERSONA NUMBER NOT NULL,
    FECHA_CREACION TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT FK_CREDENCIAL_PROGRAMA_PERSONA FOREIGN KEY (ID_PERSONA)
        REFERENCES PERSONA(ID_PERSONA) ON DELETE CASCADE
);