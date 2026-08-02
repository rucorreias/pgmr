# PGMR — Plataforma de Gestão e Manutenção Rodoviária

> **Enterprise Road Infrastructure Management Platform**

## Visão Geral

O **PGMR (Plataforma de Gestão e Manutenção Rodoviária)** é uma plataforma de software destinada à gestão inteligente de infraestruturas rodoviárias, concebida para apoiar entidades públicas, municípios e concessionárias na gestão do ciclo de vida dos seus ativos.

O objetivo é centralizar a gestão de ativos rodoviários, inspeções, ocorrências, manutenção e comunicação com os cidadãos numa única plataforma moderna, intuitiva e escalável.

A longo prazo, o sistema integrará componentes de Inteligência Artificial e Machine Learning para apoiar a tomada de decisão, otimizar recursos e melhorar a eficiência operacional.

---

## Objetivos

* Centralizar a gestão de infraestruturas rodoviárias.
* Melhorar o planeamento e execução de inspeções.
* Gerir manutenção preventiva, corretiva e preditiva.
* Facilitar a comunicação entre cidadãos e entidades gestoras.
* Disponibilizar uma plataforma baseada em mapas (GIS).
* Garantir rastreabilidade e auditoria das operações.
* Fornecer indicadores operacionais e dashboards.
* Integrar Inteligência Artificial como sistema de apoio à decisão.

---

## Funcionalidades Planeadas

### Gestão de Ativos

* Inventário de ativos rodoviários
* Georreferenciação
* Histórico de intervenções
* Estado de conservação

### Inspeções

* Planeamento de inspeções
* Checklists digitais
* Registo fotográfico
* Histórico de inspeções

### Manutenção

* Manutenção preventiva
* Manutenção corretiva
* Ordens de trabalho
* Planeamento de equipas
* Gestão de materiais

### Portal do Cidadão

* Reporte de ocorrências
* Fotografias
* Localização GPS
* Acompanhamento do estado da ocorrência
* Notificações

### Inteligência Artificial (Futuro)

* Priorização automática de ocorrências
* Apoio à decisão
* Geração automática de relatórios
* Pesquisa em linguagem natural
* Predição de necessidades de manutenção
* Deteção de padrões anómalos

### Analytics

* Dashboards
* KPIs
* Custos
* Tempos médios de resposta
* Estado global da infraestrutura

---

## Arquitetura (Inicial)

```text
Frontend (React + TypeScript)

        │

Backend (Spring Boot)

        │

PostgreSQL + PostGIS

        │

Redis

        │

RabbitMQ

        │

Future AI Services (Python/FastAPI)
```

O projeto será inicialmente desenvolvido como um **Modular Monolith**, privilegiando simplicidade, manutenibilidade e facilidade de evolução. A arquitetura deverá permitir uma futura migração para microserviços caso os requisitos do sistema o justifiquem.

---

## Stack Tecnológica

### Backend

* Java 21
* Spring Boot 3
* Spring Security
* Spring Data JPA
* Maven
* Flyway

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

### IA (Planeado)

* Python
* FastAPI
* PyTorch
* scikit-learn
* OpenCV

---

## Estado Atual

O projeto encontra-se em fase inicial de conceção e arquitetura.

Nesta fase serão desenvolvidos:

* documentação técnica;
* definição da arquitetura;
* especificação de requisitos;
* estrutura do repositório;
* configuração do ambiente de desenvolvimento.

A implementação funcional será realizada de forma incremental.

---

## Estrutura do Repositório

```text
PGMR/
├── backend/
├── frontend/
├── docs/
├── docker/
├── scripts/
└── .github/
```

---

## Filosofia do Projeto

Este projeto tem como principal objetivo aplicar boas práticas de Engenharia de Software durante todo o ciclo de desenvolvimento.

Entre os princípios adotados destacam-se:

* Clean Architecture
* Domain-Driven Design (DDD)
* SOLID
* Clean Code
* Test-Driven Development (quando aplicável)
* Documentação contínua
* Desenvolvimento incremental
* Segurança desde a conceção (Security by Design)

---

## Licença

Este projeto encontra-se em desenvolvimento. A licença será definida posteriormente.
