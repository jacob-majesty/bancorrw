# Sistema Bancário Simplificado - Backend

## Objetivo
Implementar o backend (motor de funcionamento) de um sistema bancário simplificado, com:
- Cadastro de clientes
- Contas correntes
- Contas investimento

## Recursos Fornecidos
- **Diagrama de classes** (referência para desenvolvimento)
- **Projeto no IntelliJ** com testes unitários (`*.java` em `/src/test`)
- **Script DDL** para criação das tabelas no MySQL

## Requisitos Técnicos
- ✅ Todos os testes unitários devem passar (**verde**)
- ✅ Persistência em Banco de Dados MySQL (usar script fornecido)
- ✅ IntelliJ IDEA (versão Community ou Ultimate)

## Configuração Inicial
1. **Banco de Dados**:
   ```sql
   -- Executar o script DDL fornecido no MySQL
   mysql -u usuario -p < script_ddl.sql
