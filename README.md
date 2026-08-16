# PGMR — Plataforma de Gestão e Manutenção Rodoviária

> **Enterprise Road Infrastructure Management Platform**

## Visão Geral

O **PGMR (Plataforma de Gestão e Manutenção Rodoviária)** é uma plataforma de software destinada à gestão de infraestruturas rodoviárias, concebida como um projeto de Engenharia de Software orientado para a gestão de ativos, inspeções, ocorrências e manutenção rodoviária.

O projeto tem como objetivo centralizar informação operacional numa única plataforma, permitindo gerir o estado das infraestruturas, planear intervenções, acompanhar ocorrências e disponibilizar indicadores para apoio à decisão.

A plataforma será inicialmente desenvolvida como um **MVP**, privilegiando uma arquitetura simples, modular e evolutiva. Funcionalidades mais avançadas, como GIS, analytics e Inteligência Artificial, serão introduzidas progressivamente à medida que o núcleo funcional do sistema estiver consolidado.

## Objetivos

* Centralizar a gestão de infraestruturas e ativos rodoviários.
* Registar e acompanhar ocorrências e anomalias.
* Planear e gerir inspeções rodoviárias.
* Gerir intervenções de manutenção preventiva e corretiva.
* Acompanhar ordens de trabalho e respetivos estados.
* Registar custos, prioridades e prazos de intervenção.
* Garantir rastreabilidade das operações através de auditoria.
* Disponibilizar indicadores e dashboards operacionais.
* Preparar a plataforma para futura integração com sistemas GIS e serviços de Inteligência Artificial.

## MVP

A primeira versão do PGMR será focada num conjunto reduzido de funcionalidades que permita demonstrar um fluxo operacional completo.

### Gestão de Estradas

* Registo de estradas.
* Identificação e classificação de estradas.
* Divisão das estradas em segmentos.
* Estado de conservação.
* Consulta do histórico de intervenções.

### Gestão de Ocorrências

* Registo de ocorrências.
* Classificação por tipo e gravidade.
* Localização da ocorrência.
* Registo de descrição e evidências fotográficas.
* Estados de processamento.
* Priorização de ocorrências.

### Gestão de Intervenções

* Criação de intervenções.
* Associação a estradas, segmentos ou ocorrências.
* Tipos de manutenção preventiva e corretiva.
* Definição de prioridade.
* Planeamento de datas.
* Registo de custos.
* Acompanhamento do estado da intervenção.

### Gestão de Utilizadores

* Autenticação.
* Autorização baseada em roles.
* Perfis de utilizador.
* Controlo de acesso às operações.

### Dashboard

* Número de estradas geridas.
* Ocorrências abertas e resolvidas.
* Intervenções em curso.
* Intervenções concluídas.
* Custos de manutenção.
* Indicadores operacionais básicos.

## Funcionalidades Futuras

### GIS

* Georreferenciação de ativos.
* Visualização de estradas num mapa.
* Visualização de ocorrências.
* Visualização de intervenções.
* Pesquisa por localização.
* Integração com dados geoespaciais através de PostGIS.

### Inspeções

* Planeamento de inspeções.
* Checklists digitais.
* Registo fotográfico.
* Avaliação do estado de conservação.
* Histórico de inspeções.
* Associação de inspeções a ativos e segmentos.

### Portal do Cidadão

* Reporte de ocorrências.
* Upload de fotografias.
* Localização GPS.
* Consulta do estado da ocorrência.
* Notificações.

### Analytics

* Dashboards operacionais.
* KPIs.
* Custos de manutenção.
* Tempos médios de resposta.
* Distribuição de ocorrências.
* Evolução do estado da infraestrutura.

### Inteligência Artificial

* Classificação automática de ocorrências.
* Priorização de intervenções.
* Pesquisa em linguagem natural.
* Geração automática de relatórios.
* Deteção de padrões anómalos.
* Predição de necessidades de manutenção.

## Arquitetura

O PGMR será inicialmente desenvolvido como um **Modular Monolith**.

```text
┌─────────────────────────────────────┐
│         Frontend                    │
│      React + TypeScript             │
└──────────────────┬──────────────────┘
                   │
                REST API
                   │
┌──────────────────▼──────────────────┐
│         Spring Boot Backend         │
│                                     │
│  Presentation → Application →       │
│  Domain → Infrastructure            │
└──────────────────┬──────────────────┘
                   │
┌──────────────────▼──────────────────┐
│             PostgreSQL              │
│              + JPA                  │
└─────────────────────────────────────┘
```

A arquitetura modular permitirá separar responsabilidades dentro da mesma aplicação, mantendo o sistema simples de desenvolver e testar. Componentes externos, como Redis, RabbitMQ ou serviços de Inteligência Artificial, apenas serão introduzidos quando existir uma necessidade funcional concreta.

## Stack Tecnológica

### Backend

* Java 25
* Spring Boot 4.1
* Spring Web
* Spring Security
* Spring Data JPA
* Hibernate
* Maven
* Lombok
* Flyway
* Bean Validation
* JUnit
* Mockito

### Frontend

* React
* TypeScript
* Vite
* React Router
* TanStack Query
* Material UI

### Base de Dados

* PostgreSQL
* PostGIS

### Infraestrutura

* Docker
* Docker Compose
* GitHub Actions

### APIs e Documentação

* REST
* OpenAPI
* Swagger UI

### IA — Futuro

* Python
* FastAPI
* scikit-learn
* PyTorch
* OpenCV

## Segurança

A segurança será considerada desde as primeiras fases do desenvolvimento.

O backend utilizará **Spring Security** para implementar:

* Autenticação de utilizadores.
* Autorização baseada em roles.
* Proteção dos endpoints da API.
* Password hashing.
* Gestão segura de sessões/tokens.
* Validação de dados de entrada.
* Auditoria de operações relevantes.

A autenticação baseada em JWT será introduzida quando o frontend estiver integrado com o backend.

## Qualidade e Testes

O projeto pretende aplicar práticas de Engenharia de Software durante todo o ciclo de desenvolvimento.

Serão utilizados:

* Testes unitários.
* Testes de integração.
* Testes dos endpoints REST.
* Validação de dados.
* Tratamento global de exceções.
* Logging estruturado.
* Code reviews através de Pull Requests.
* CI através de GitHub Actions.

## Princípios de Engenharia

O desenvolvimento do PGMR seguirá princípios de Engenharia de Software com foco em código sustentável e evolução incremental.

Entre os princípios adotados destacam-se:

* SOLID
* Clean Code
* Separation of Concerns
* Domain-Driven Design, quando aplicável
* Modular Architecture
* Security by Design
* Test-Driven Development, quando adequado
* Continuous Integration
* Documentação técnica contínua
* Desenvolvimento incremental

A aplicação de cada princípio será feita de forma pragmática, evitando introduzir complexidade arquitetural que não seja justificada pelos requisitos do sistema.

## Licença

Este projeto encontra-se em desenvolvimento. A licença será definida posteriormente.
