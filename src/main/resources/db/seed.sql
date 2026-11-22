-- Dados iniciais que o sistema espera encontrar (rode DEPOIS de MODELAGEM_FISICA - LIVRO CAIXA.sql)
USE livro_caixa;

INSERT INTO Perfil (descricao) VALUES ('Administrador'), ('Recepcionista');
INSERT INTO FormaPagamento (descricao) VALUES ('Espécie'), ('PIX'), ('RPV');
