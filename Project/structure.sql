CREATE DATABASE Atividade3;

USE Atividade3;

CREATE TABLE Client (
    Id INTEGER NOT NULL AUTO_INCREMENT,
    Name VARCHAR(200) NOT NULL,
    Cpf CHAR(11),
    Rg CHAR(20),
    Email VARCHAR(200),
    Phone CHAR(11),
    CEP CHAR(8),
    PRIMARY KEY (Id)
);
