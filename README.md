# SafetYSec - Sistema de Monitorização e Segurança

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg)](https://developer.android.com/jetpack/compose)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-orange.svg)](https://firebase.google.com/)

## 📱 Sobre o Projeto

**SafetYSec** é uma aplicação Android desenvolvida para garantir a proteção e segurança de utilizadores que, devido à idade ou condições de saúde, necessitam de acompanhamento ou supervisão.

### 🎯 Objetivo

Proporcionar um sistema de monitorização em tempo real que conecta **Monitores** (cuidadores/responsáveis) com **Protegidos** (utilizadores supervisionados), oferecendo:
- Deteção automática de situações de risco
- Notificações instantâneas
- Geolocalização em tempo real
- Histórico completo de eventos
- Interface intuitiva e acessível

---

## ✨ Funcionalidades Principais

### 👥 Sistema Dual de Perfis

#### 🛡️ Monitor (Cuidador)
- Dashboard com alertas recentes e estatísticas
- Visualização do estado atual dos protegidos
- Criação e gestão de regras de monitorização
- Histórico detalhado de alertas por protegido
- Visualização de localização GPS e detalhes de eventos
- Associação múltipla com protegidos via OTP

#### 👤 Protegido (Supervisionado)
- Dashboard personalizado
- Botão de pânico para alertas manuais
- Gestão de janelas temporais de monitorização
- Autorização individual de regras
- Histórico pessoal de alertas
- Lista de monitores autorizados
- Código de cancelamento de alertas personalizável

---

## 🚨 Regras de Monitorização

### 1. **Deteção de Quedas** 🤕
- Utiliza acelerómetro do dispositivo
- Algoritmo de deteção de impacto
- Alerta automático em caso de queda

### 2. **Deteção de Acidentes** 🚗
- Monitorização de desaceleração brusca
- Identificação de possíveis acidentes de viação
- Notificação imediata aos monitores

### 3. **Geofencing Múltiplo** 📍
- **Suporte para múltiplas áreas seguras**
- Configuração de coordenadas GPS e raio
- Alerta quando fora de TODAS as áreas definidas
- Cada área com nome e parâmetros independentes

### 4. **Controlo de Velocidade** 🏃
- Definição de velocidade máxima permitida
- Monitorização via GPS
- Alerta em caso de excesso de velocidade

### 5. **Inatividade Prolongada** 😴
- Deteção de ausência de movimento
- Tempo configurável (em minutos)
- Verificação periódica de sensores

### 6. **Botão de Pânico** 🆘
- Ativação manual pelo protegido
- Alerta instantâneo a todos os monitores
- Botão flutuante sempre acessível

---

## 🔒 Segurança e Autenticação

### Autenticação Multi-Factor (MFA)
- ✅ **OTP de 6 dígitos** gerado automaticamente
- ✅ Expiração em 10 minutos
- ✅ Máximo de 5 tentativas de verificação
- ✅ Armazenamento seguro no Firebase Firestore
- ✅ Modo desenvolvimento (mostra OTP no ecrã para testes)

### Recuperação de Acesso
- ✅ Integração com Firebase Authentication
- ✅ Email de reset de password
- ✅ Link seguro com token único

### Associação Monitor-Protegido
- OTP one-time password
- Código único com expiração
- Verificação em tempo real

---

## 🎨 Interface e Experiência

### Design System
- **Material Design 3** com cores personalizadas
- **Tema Claro/Escuro** com persistência de preferências
- Gradientes e elevações para hierarquia visual
- Ícones consistentes e intuitivos

### Navegação
- Bottom Navigation para acesso rápido
- Top App Bars dinâmicas por contexto
- Floating Action Buttons contextuais
- Navegação por gestos e retrocesso inteligente

### Responsividade
- ✅ **Suporte completo Portrait e Landscape**
- Layout adaptativo em detalhes de alertas
- Componentes que se reorganizam automaticamente
- Testes em múltiplas resoluções

### Internacionalização
- 🇬🇧 **Inglês** (idioma padrão)
- 🇵🇹 **Português** (tradução completa)
- Seleção automática baseada no dispositivo

---

## 📊 Gestão de Alertas

### Fluxo de Alerta
1. **Deteção** - Regra é violada (ex: queda detetada)
2. **Notificação ao Protegido** - 10 segundos para cancelar
3. **Código de Cancelamento** - Protegido pode impedir envio
4. **Notificação aos Monitores** - Se não cancelado
5. **Dados Incluídos:**
   - Tipo de evento
   - Data e hora
   - Localização GPS
   - Identificação do protegido
   - Distância à área mais próxima (geofencing)

### Gravação de Vídeo
⚠️ **Limitação Conhecida:** 
- Código implementado em `VideoRecordingHelper.kt`
- Requer Firebase Storage (plano pago)
- Alternativa: armazenamento local (não implementado)

---

## 🏗️ Arquitetura Técnica

### Padrões de Design
```
SafetYSec/
├── model/          # Data classes e enums
├── view/           # UI com Jetpack Compose
│   ├── auth/       # Autenticação e MFA
│   ├── monitor/    # Ecrãs do Monitor
│   ├── protected/  # Ecrãs do Protegido
│   ├── alert/      # Sistema de alertas
│   └── components/ # Componentes reutilizáveis
├── viewmodel/      # Lógica de apresentação
├── repository/     # Camada de dados
├── service/        # Serviços em background
├── utils/          # Utilitários e helpers
└── ui/theme/       # Sistema de design
```

### Stack Tecnológica

#### Frontend
- **Jetpack Compose** - UI moderna e reativa
- **Material 3** - Componentes e design system
- **Coil** - Carregamento de imagens
- **Navigation Component** - Navegação entre ecrãs

#### Backend & Cloud
- **Firebase Authentication** - Gestão de utilizadores
- **Cloud Firestore** - Base de dados NoSQL em tempo real
- **Firebase Cloud Messaging (FCM)** - Notificações push
- **Firebase Storage** - Armazenamento de vídeos (não disponível no plano free)

#### Sensores e Localização
- **Google Play Services Location** - GPS e geofencing
- **Sensor Manager** - Acelerómetro e giroscópio
- **FusedLocationProvider** - Localização otimizada

#### Arquitetura
- **MVVM** (Model-View-ViewModel)
- **Repository Pattern** - Abstração de dados
- **Clean Architecture** - Separação de responsabilidades
- **Kotlin Coroutines** - Programação assíncrona
- **StateFlow** - Gestão de estado reativo

---

## 📱 Requisitos do Sistema

### Mínimos
- Android 7.0 (API 24) ou superior
- GPS ativado
- Acelerómetro (obrigatório)
- Conexão à Internet
- 50 MB de espaço disponível

### Recomendados
- Android 10.0 (API 29) ou superior
- 4 GB RAM
- Localização de alta precisão
- Notificações ativadas

### Permissões Necessárias
```xml
- INTERNET
- ACCESS_FINE_LOCATION
- ACCESS_COARSE_LOCATION
- ACCESS_BACKGROUND_LOCATION
- CAMERA (para vídeo)
- RECORD_AUDIO (para vídeo)
- POST_NOTIFICATIONS
- VIBRATE
- FOREGROUND_SERVICE
- WAKE_LOCK
```

---

## 🚀 Como Executar

### Pré-requisitos
1. **Android Studio** Hedgehog (2023.1.1) ou superior
2. **JDK** 17
3. **Conta Firebase** configurada
4. **Dispositivo/Emulador** com Google Play Services

### Configuração

#### 1. Clonar o Repositório
```bash
cd C:\Users\<seu_usuario>\AndroidStudioProjects
# Extrair o projeto SafetYSec
```

#### 2. Firebase Setup
1. Aceder ao [Firebase Console](https://console.firebase.google.com/)
2. Criar novo projeto ou usar existente
3. Adicionar app Android:
   - Package name: `pt.isec.a2022143267.safetysec`
   - SHA-1: (obter com `./gradlew signingReport`)
4. Descarregar `google-services.json`
5. Colocar em `app/google-services.json`

#### 3. Configurar Firebase
```bash
# No Firebase Console:
- Authentication > Enable Email/Password
- Firestore Database > Create Database (test mode)
- Cloud Messaging > Configure
```

#### 4. Compilar e Executar
```bash
# Via Android Studio
Run > Run 'app' (Shift+F10)

# Via linha de comandos
./gradlew assembleDebug
./gradlew installDebug
```

---

## 📖 Guia de Utilização

### Primeiro Acesso

#### Para Monitor
1. Registar conta escolhendo perfil "Monitor"
2. Verificar MFA (código mostrado no ecrã em dev mode)
3. Aceder ao dashboard
4. Gerar OTP nas definições para associação
5. Partilhar código OTP com o Protegido

#### Para Protegido
1. Registar conta escolhendo perfil "Protegido"
2. Verificar MFA
3. Ir a "Monitores" > "Adicionar Monitor"
4. Inserir OTP fornecido pelo Monitor
5. Autorizar regras de monitorização desejadas
6. Configurar janelas temporais
7. Definir código de cancelamento (4 dígitos)

### Funcionalidades Diárias

#### Monitor
- **Dashboard:** Ver alertas recentes e estatísticas
- **Protegidos:** Lista com estado e último alerta
- **Detalhes:** Click em alerta para ver localização e dados
- **Regras:** Editar parâmetros (velocidade, raio, etc.)
- **Histórico:** Ver todos os alertas de cada protegido

#### Protegido
- **Botão Pânico:** Pressionar para alerta manual
- **Regras:** Ativar/desativar individualmente
- **Janelas:** Definir horários de monitorização
- **Histórico:** Consultar alertas passados
- **Definições:** Alterar código de cancelamento

---

## 🧪 Estado de Desenvolvimento

### ✅ Implementado e Funcional

#### Core Features
- [x] Sistema de autenticação completo
- [x] MFA com OTP dinâmico (6 dígitos, 10 min)
- [x] Recuperação de password via email
- [x] Perfis Monitor e Protegido
- [x] Associação via OTP one-time
- [x] Dashboard para ambos perfis
- [x] CRUD completo de regras
- [x] 6 regras de monitorização funcionais
- [x] **Geofencing com múltiplas áreas** ✨
- [x] Janelas temporais configuráveis
- [x] Sistema de alertas em tempo real
- [x] Countdown 10 segundos com cancelamento
- [x] Código de cancelamento personalizável
- [x] Histórico de alertas
- [x] Notificações push (FCM)

#### UI/UX
- [x] Jetpack Compose 100%
- [x] Material Design 3
- [x] Tema Claro/Escuro com persistência
- [x] Inglês e Português
- [x] Portrait e Landscape
- [x] Navegação fluida
- [x] Ícones dinâmicos
- [x] Floating Action Buttons contextuais

#### Técnico
- [x] MVVM + Clean Architecture
- [x] Repository Pattern
- [x] Firebase Firestore
- [x] Firebase Auth
- [x] Firebase Cloud Messaging
- [x] Foreground Service para monitorização
- [x] Sensor Manager (acelerómetro)
- [x] FusedLocationProvider
- [x] Kotlin Coroutines
- [x] StateFlow

### ⚠️ Limitações Conhecidas

#### Video Recording
- **Status:** Código implementado mas não funcional
- **Motivo:** Firebase Storage não disponível no plano gratuito
- **Ficheiro:** `utils/VideoRecordingHelper.kt`
- **Soluções alternativas:**
  1. Upgrade para Firebase Blaze (pay-as-you-go)
  2. Implementar armazenamento local
  3. Usar servidor próprio

#### Real-time Updates
- **Status:** Parcialmente implementado
- **Limitação:** Estado dos protegidos não atualiza em tempo real no dashboard do monitor
- **Workaround:** Atualização manual (pull-to-refresh ou reabrir ecrã)

#### Statistics Enhancement
- **Status:** Estatísticas básicas apenas
- **Implementado:** Contagem de alertas por tipo
- **Em falta:** Gráficos, trends, filtros por data

---

## 🐛 Troubleshooting

### Problemas Comuns

#### 1. Build Falha
```bash
# Limpar build
./gradlew clean

# Invalidar caches do Android Studio
File > Invalidate Caches > Invalidate and Restart
```

#### 2. Firebase Não Conecta
- Verificar `google-services.json` na pasta `app/`
- Confirmar package name: `pt.isec.a2022143267.safetysec`
- Ativar Authentication e Firestore no console

#### 3. Permissões Negadas
- Ir a Definições do Android > Apps > SafetYSec > Permissões
- Ativar todas as permissões necessárias
- Reiniciar app

#### 4. GPS Não Funciona
- Ativar localização no dispositivo
- Modo de precisão: Alta precisão
- Verificar se app tem permissão de localização em background

#### 5. Alertas Não Chegam
- Verificar notificações ativadas
- Firebase Cloud Messaging configurado
- Dispositivo com Google Play Services
- Internet ativa

---

## 📚 Documentação Adicional

### Estrutura de Dados (Firestore)

#### Coleção: `users`
```json
{
  "id": "userId",
  "name": "Nome Utilizador",
  "email": "email@example.com",
  "userType": "MONITOR" | "PROTECTED",
  "createdAt": "Timestamp",
  "cancelCode": "1234",
  "fcmToken": "...",
  "mfaData": {
    "otp": "123456",
    "expiresAt": 1234567890,
    "attempts": 0
  },
  "associationOTP": "654321",
  "otpExpiresAt": 1234567890
}
```

#### Coleção: `rules`
```json
{
  "id": "ruleId",
  "monitorId": "userId",
  "protectedId": "userId",
  "ruleType": "GEOFENCING",
  "name": "Casa e Trabalho",
  "isEnabled": true,
  "parameters": {
    "radius": 100.0,
    "maxSpeed": 80.0,
    "inactivityMinutes": 30,
    "geoPoints": [
      {
        "center": {"latitude": 40.123, "longitude": -8.456},
        "radius": 150.0,
        "name": "Casa"
      },
      {
        "center": {"latitude": 40.234, "longitude": -8.567},
        "radius": 200.0,
        "name": "Trabalho"
      }
    ]
  }
}
```

#### Coleção: `alerts`
```json
{
  "id": "alertId",
  "protectedId": "userId",
  "monitorIds": ["userId1", "userId2"],
  "ruleId": "ruleId",
  "alertType": "GEOFENCING",
  "status": "ACTIVE" | "RESOLVED" | "CANCELLED",
  "timestamp": "Timestamp",
  "location": {"latitude": 40.123, "longitude": -8.456},
  "additionalData": {
    "distance": "350.5",
    "closestArea": "Casa",
    "totalAreas": "2"
  },
  "videoUrl": "",
  "cancelledAt": "Timestamp"
}
```

---

## 👥 Equipa de Desenvolvimento

### Desenvolvimento
- **Estudante:** [Número de Aluno]
- **Curso:** Licenciatura em Engenharia Informática
- **Unidade Curricular:** Arquiteturas Móveis
- **Ano Letivo:** 2025/2026

### Orientação
- **Docente:** [Nome do Professor]
- **Instituição:** [Nome da Instituição]

---

## 📄 Licença

Este projeto foi desenvolvido no âmbito académico para a Unidade Curricular de Arquiteturas Móveis.

**Direitos de Autor © 2026 - Todos os direitos reservados**

---

## 🔗 Links Úteis

- [Documentação Android](https://developer.android.com/docs)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Firebase Documentation](https://firebase.google.com/docs)
- [Material Design 3](https://m3.material.io/)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)

---

## 📊 Estatísticas do Projeto

```
Linguagem:     Kotlin 100%
Linhas:        ~15,000
Ficheiros:     50+ arquivos .kt
Ecrãs:         25+ telas
Componentes:   100+ componentes Compose
Tempo:         ~160 horas desenvolvimento
```

---

## 🎯 Roadmap Futuro

### Versão 1.1 (Melhorias)
- [ ] Gráficos e estatísticas avançadas
- [ ] Exportação de relatórios PDF
- [ ] Chat entre Monitor e Protegido
- [ ] Notificações por email
- [ ] Widget de home screen

### Versão 2.0 (Novas Features)
- [ ] Integração com smartwatches
- [ ] Reconhecimento de voz
- [ ] ML para deteção de padrões
- [ ] Modo offline com sincronização
- [ ] Suporte multi-idioma (mais línguas)

---

## 📞 Suporte

Para questões técnicas ou bugs encontrados:
1. Verificar secção **Troubleshooting**
2. Consultar **Relatório Técnico** (RELATORIO_TECNICO.md)
3. Contactar a equipa de desenvolvimento

---

**Última Atualização:** 02 de Janeiro de 2026  
**Versão:** 1.0.0  
**Status:** ✅ Pronto para Submissão

