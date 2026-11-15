CREATE TABLE categoria (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome       VARCHAR(80) NOT NULL UNIQUE,
    criada_em  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE despesa (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    descricao     VARCHAR(150) NOT NULL,
    valor         NUMERIC(12,2) NOT NULL CHECK (valor > 0),
    data          DATE NOT NULL,
    categoria_id  BIGINT NOT NULL REFERENCES categoria(id),
    criada_em     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_despesa_data ON despesa(data);
CREATE INDEX idx_despesa_categoria ON despesa(categoria_id);

CREATE TABLE orcamento_mensal (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ano           SMALLINT NOT NULL,
    mes           SMALLINT NOT NULL CHECK (mes BETWEEN 1 AND 12),
    valor_limite  NUMERIC(12,2) NOT NULL CHECK (valor_limite > 0),
    categoria_id  BIGINT NOT NULL REFERENCES categoria(id),
    UNIQUE (ano, mes, categoria_id)
);