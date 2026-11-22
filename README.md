# Livro Caixa (desktop – Java + MySQL)

## Estrutura (MVC + DAO)
```
src/main/java/br/com/livrocaixa/
├── App.java            ponto de entrada (por ora só testa a conexão)
├── config/             ConexaoFactory (JDBC, lê db.properties)
├── model/              entidades + regras de domínio  ✔ pronto
├── dao/                acesso ao MySQL                ⏳ próximo passo
├── controller/         ligam View ↔ DAO/Model         ⏳
├── view/               telas                          ⏳
├── exception/          ValidacaoException
└── util/               CpfValidator, Validacao
src/main/resources/
├── db.properties.example   modelo da configuração de rede
└── db/seed.sql             Perfis e Formas de pagamento iniciais
```

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

## VS Code
Instale o *Extension Pack for Java* (sugerido em `.vscode/extensions.json`) e o JDK 17+.
Gerar o executável: `mvn package` → `target/livro-caixa-0.1.0-SNAPSHOT.jar`
Executar: `java -jar target/livro-caixa-0.1.0-SNAPSHOT.jar`
