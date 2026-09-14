-- =============================================================================
-- MÓDULO: APTITUD FÍSICA
-- =============================================================================
-- Esquema: SLB_APTITUDFISICA
-- Tablas: CATALOGO_PRUEBA, EVALUACION_APTITUD, DETALLE_PRUEBA_FISICA

-- =============================================================================
-- 1. CREACIÓN DEL USUARIO / ESQUEMA DEL MÓDULO
-- =============================================================================
CREATE USER SLB_APTITUDFISICA IDENTIFIED BY "123456"
  DEFAULT TABLESPACE USERS
  TEMPORARY TABLESPACE TEMP
  QUOTA UNLIMITED ON USERS;

-- =============================================================================
-- 2. ASIGNACIÓN DE PRIVILEGIOS
-- =============================================================================
GRANT CREATE SESSION TO SLB_APTITUDFISICA;
GRANT CREATE TABLE TO SLB_APTITUDFISICA;
GRANT CREATE SEQUENCE TO SLB_APTITUDFISICA;
GRANT CREATE TRIGGER TO SLB_APTITUDFISICA;

-- =============================================================================
-- 3. CAMBIO DE CONTEXTO AL ESQUEMA CREADO
-- =============================================================================
ALTER SESSION SET CURRENT_SCHEMA = SLB_APTITUDFISICA;

-- =============================================================================
-- 4. CREACIÓN DE TABLAS DEL MÓDULO
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 4.1. CATALOGO_PRUEBA
-- -----------------------------------------------------------------------------
CREATE TABLE CATALOGO_PRUEBA (
    ID_PRUEBA NUMBER GENERATED ALWAYS AS IDENTITY CONSTRAINT PK_CATALOGO_PRUEBA PRIMARY KEY,
    NOMBRE_PRUEBA VARCHAR2(100) NOT NULL,
    UNIDAD_MEDIDA VARCHAR2(30) NOT NULL,
    DESCRIPCION CLOB,
    ACTIVO NUMBER(1) NOT NULL,
    FECHA_CREACION TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- -----------------------------------------------------------------------------
-- 4.2. EVALUACION_APTITUD
-- ID_PERSONA: referencia lógica a SLB_PERSONAL.PERSONA (sin FK entre esquemas).
-- -----------------------------------------------------------------------------
CREATE TABLE EVALUACION_APTITUD (
    ID_EVALUACION_APTITUD NUMBER GENERATED ALWAYS AS IDENTITY CONSTRAINT PK_EVALUACION_APTITUD PRIMARY KEY,
    ID_PERSONA NUMBER NOT NULL,
    FECHA_REGISTRO DATE NOT NULL,
    PUNTAJE_GLOBAL NUMBER(5, 2) NOT NULL,
    DIAGNOSTICO_APTITUD VARCHAR2(50) NOT NULL,
    SINCRONIZADO NUMBER(1) NOT NULL,
    FECHA_SINCRONIZACION TIMESTAMP,
    FECHA_CREACION TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- -----------------------------------------------------------------------------
-- 4.3. DETALLE_PRUEBA_FISICA
-- -----------------------------------------------------------------------------
CREATE TABLE DETALLE_PRUEBA_FISICA (
    ID_DETALLE_APTITUD NUMBER GENERATED ALWAYS AS IDENTITY CONSTRAINT PK_DETALLE_PRUEBA_FISICA PRIMARY KEY,
    VALOR_OBTENIDO NUMBER(6, 2) NOT NULL,
    PUNTAJE_PARCIAL NUMBER(5, 2) NOT NULL,
    ID_EVALUACION_APTITUD NUMBER NOT NULL,
    ID_PRUEBA NUMBER NOT NULL,
    FECHA_CREACION TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT FK_DETALLE_PRUEBA_FISICA_EVALUACION_APTITUD FOREIGN KEY (ID_EVALUACION_APTITUD)
        REFERENCES EVALUACION_APTITUD(ID_EVALUACION_APTITUD) ON DELETE CASCADE,
    CONSTRAINT FK_DETALLE_PRUEBA_FISICA_CATALOGO_PRUEBA FOREIGN KEY (ID_PRUEBA)
        REFERENCES CATALOGO_PRUEBA(ID_PRUEBA)
);