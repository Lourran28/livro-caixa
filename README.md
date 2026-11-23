# Livro Caixa (desktop – Java + MySQL)

Sistema desktop para controle financeiro do Escritório de Advocacia JR (entradas de consultas, parcelas, saídas e relatórios de fluxo de caixa), desenvolvido em **Java 17**, **Maven** e **MySQL 8** (JDBC puro).

## Estrutura (MVC + DAO)
```
src/main/java/br/com/livrocaixa/
├── App.java            ponto de entrada (por ora só testa a conexão)
├── config/             ConexaoFactory (JDBC, lê db.properties)
├── model/              entidades + regras de domínio                        ✔ pronto
├── dao/                interfaces (contratos de persistência)               ✔ pronto (implementações JDBC ⏳)
├── controller/         regras de aplicação, sessão, permissões e dto/       ✔ pronto
├── view/               telas                                                ⏳
├── exception/          ValidacaoException, AcessoNegadoException,
│                       PersistenciaException                                ✔ pronto
└── util/               CpfValidator, Validacao                              ✔ pronto
src/main/resources/
├── db.properties.example   modelo da configuração de rede
└── db/seed.sql             Perfis e Formas de pagamento iniciais
src/test/java/br/com/livrocaixa/
└── controller/         testes unitários (JUnit 5) e dublês em memória       ✔ pronto
```

## Camada Controller e Contratos DAO (implementado)
- **Autenticação e permissões (RNF001):** `SessaoUsuario` (singleton de sessão), `ControllerBase` (validação centralizada `exigirLogin`, `exigirAdministrador`, `exigirAdministradorOuRecepcionista` e tradução de erros de integridade referencial) e `AutenticacaoController`.
  - **Administrador:** acesso integral ao sistema.
  - **Recepcionista:** acesso restrito a registro de saídas (`SaidaController`) e manutenção de tipos de saída (`TipoSaidaController`).
- **Cadastros básicos (UC002):** `PerfilController`, `FormaPagamentoController`, `TipoConsultaController`, `TipoSaidaController`, `FuncionarioController` e `ClienteController` (com validação de integridade e unicidade de CPF).
- **Consultas, saídas e parcelamentos (UC003–UC005, UC007):**
  - `ConsultaController`: prepara consulta/fatura e parcelas (à vista ou a prazo, com ou sem entrada), permite edição manual de valores de parcelas antes do salvamento transacional e delega cadastros rápidos de cliente/tipo de consulta.
  - `SaidaController`: registra gastos vinculados ao funcionário logado e permite cadastro rápido de tipo de saída.
  - `ParcelamentoController`: consulta histórico por cliente, parcelas atrasadas, movimentações por funcionário (`MovimentacoesFuncionario`), baixa parcial/integral, estorno (`desfazerBaixa`) e cancelamento definitivo de parcela.
- **Relatórios (UC006):** `RelatorioController` gera o DTO imutável `RelatorioFluxoCaixa` por ano, mês ou data específica (retornando valores zerados quando sem movimentações) e delega a exportação PDF para a interface `ExportadorRelatorio`.
- **Contratos DAO (`dao/`):** 10 interfaces (`PerfilDAO`, `FormaPagamentoDAO`, `TipoConsultaDAO`, `TipoSaidaDAO`, `FuncionarioDAO`, `ClienteDAO`, `ConsultaDAO`, `ParcelaDAO`, `SaidaDAO`, `PagamentoDAO`) sem acoplamento a JDBC/SQL, utilizando `PersistenciaException` (unchecked) e documentando operações transacionais e carregamento de pagamentos das parcelas.
- **Exceções (`exception/`):** adicionadas `AcessoNegadoException` (bloqueios de permissão/autenticação) e `PersistenciaException` (encapsulamento de erros de banco e violações de chave estrangeira `ON DELETE RESTRICT`).

## Banco de dados (máquina SERVIDOR)
1. Rode `MODELAGEM FISICA - LIVRO CAIXA.sql` e depois `src/main/resources/db/seed.sql`.
2. Crie um usuário de rede (ajuste a faixa de IP da sua rede):
```sql
CREATE USER 'livrocaixa'@'192.168.%' IDENTIFIED BY 'senha_forte';
GRANT SELECT, INSERT, UPDATE, DELETE ON livro_caixa.* TO 'livrocaixa'@'192.168.%';
```
3. No `my.ini`/`my.cnf` do MySQL: `bind-address = 0.0.0.0` e reinicie o serviço.
4. Libere a porta **3306** no firewall do servidor e dê um **IP fixo** a essa máquina.

## Configuração do app
Copie `db.properties.example` para `db.properties` (ao lado do .jar, ou na raiz do projeto ao rodar pelo VS Code):
- servidor: `db.host=localhost`
- cliente: `db.host=<IP do servidor>`

## VS Code, compilação e testes
Instale o *Extension Pack for Java* (sugerido em `.vscode/extensions.json`) e o JDK 17+.

- Compilar o projeto: `mvn -q compile`
- Executar testes unitários dos controllers (JUnit 5 + dublês em memória, não exige MySQL ativo): `mvn test`
- Gerar o executável: `mvn package` → `target/livro-caixa-0.1.0-SNAPSHOT.jar`
- Executar: `java -jar target/livro-caixa-0.1.0-SNAPSHOT.jar`
