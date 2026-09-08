# TapTapSend

Application web de gestion de transferts d'argent internationaux (façon Wise / Remitly), développée en **Java EE** avec une architecture en couches classique (Servlet → Service → DAO → Hibernate) et une interface moderne "fintech" en JSP/Bootstrap.

![Java](https://img.shields.io/badge/Java-17-orange)
![Hibernate](https://img.shields.io/badge/Hibernate-6.4-blueviolet)
![Tomcat](https://img.shields.io/badge/Tomcat-10.1-yellow)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-336791)
![Maven](https://img.shields.io/badge/Build-Maven-red)

---

## Sommaire

- [Aperçu](#aperçu)
- [Fonctionnalités](#fonctionnalités)
- [Stack technique](#stack-technique)
- [Architecture](#architecture)
- [Design system](#design-system)
- [Installation](#installation)
- [Lancer le projet](#lancer-le-projet)
- [Structure du projet](#structure-du-projet)
- [Sécurité](#sécurité)
- [Pistes d'amélioration](#pistes-damélioration)

---

## Aperçu

TapTapSend permet de gérer :
- des **clients** (émetteurs et destinataires de transferts, avec solde en EUR ou MGA),
- des **transferts d'argent** entre clients de pays différents,
- des **taux de change** EUR ↔ MGA configurables,
- des **grilles de frais** appliquées selon le montant transféré,
- un **tableau de bord** avec statistiques et activité récente.

## Fonctionnalités

### Gestion des clients
- CRUD complet (ajout, modification, suppression) via modales AJAX
- Recherche en temps réel (nom, téléphone, pays) côté client, sans rechargement de page
- Tri par colonne (nom, pays, solde) au clic sur l'en-tête
- Indicateur visuel du solde (normal / faible / à zéro)
- Numéro de téléphone cliquable (`tel:`) + copie en un clic

### Transferts d'argent
- Formulaire avec validation métier en temps réel :
  - émetteur et destinataire de pays différents obligatoires
  - cohérence de devise (EUR → MGA, MGA → EUR)
  - vérification du solde disponible avant envoi
- Aperçu live des profils émetteur/destinataire pendant la sélection
- Génération de relevé PDF par client et par période (iText)
- Recherche par date et tri par montant/date

### Taux de change et frais
- Grilles configurables (CRUD), utilisées automatiquement lors des transferts

### Tableau de bord
- Statistiques clés : recette totale, nombre de clients, nombre de transferts
- Graphique d'activité des 7 derniers jours (Chart.js)
- Liste des derniers transferts

### Interface
- Design "fintech" sur-mesure (thème Navy & Émeraude), responsive du mobile au desktop
- Navigation mobile avec menu hamburger (offcanvas)
- Modales avec squelette de chargement, pied d'action collant, plein écran automatique sur mobile
- Système de toasts et de confirmation de suppression personnalisés
- Vue tableau qui se transforme en cartes empilées sur petit écran

## Stack technique

| Couche | Technologie |
|---|---|
| Langage | Java 17 |
| Serveur | Apache Tomcat 10.1 (Jakarta EE 10, namespace `jakarta.*`) |
| ORM | Hibernate 6.4 / JPA |
| Base de données | PostgreSQL |
| Vues | JSP + JSTL |
| Frontend | Bootstrap 5 (grille + moteur JS des modales), CSS personnalisé (design system maison) |
| Icônes | Font Awesome 6 |
| Graphiques | Chart.js |
| PDF | iText 8 |
| Build | Maven |

## Architecture

Le projet suit une architecture en couches classique Java EE :

```
Servlet (controller)
    ↓
Service (logique métier)
    ↓
DAO (accès aux données)
    ↓
Hibernate / PostgreSQL
```

- **`controller/`** — Servlets, un par ressource (`ClientServlet`, `EnvoyerServlet`, `TauxServlet`, `FraisServlet`, `DashboardServlet`)
- **`service/`** — logique métier (validations, calculs de frais/taux, envoi d'emails)
- **`dao/`** — accès aux données via un `GenericDAO` générique + DAO spécifiques
- **`model/`** — entités JPA (`Client`, `Envoyer`, `Taux`, `FraisEnvoi`)
- **`WEB-INF/views/`** — JSP organisées par ressource, avec un `template.jsp` commun (layout sidebar/topbar/modales) et des vues incluses par `<jsp:include>`

Les formulaires sont chargés dans des modales via AJAX (GET pour le formulaire, POST en AJAX pour la soumission), ce qui évite les rechargements de page complets tout en gardant une architecture JSP classique côté serveur.

## Design system

L'interface a été entièrement redessinée avec un système de couleurs et de composants cohérent, tout en conservant Bootstrap comme moteur (grille CSS + comportement JS des modales/offcanvas, pour l'accessibilité clavier et le focus-trap), mais avec un skin visuel 100 % personnalisé.

**Palette "Navy & Émeraude"** — sidebar et headers de modale en navy profond (`#0F172A`), accent en émeraude (`#10B981`) pour les actions, boutons et éléments interactifs.

Composants clés (`assets/css/theme.css`) :
- Cartes statistiques avec icônes dégradées
- Tableaux avec vue carte responsive automatique (< 700px)
- Champs "floating label"
- Modales avec animation d'apparition, squelette de chargement, pied d'action collant
- Toasts et modale de confirmation personnalisés (sans dépendance externe)

## Installation

### Prérequis
- JDK 17
- Maven
- PostgreSQL (15 ou plus recommandé)
- Apache Tomcat **10.1+** (obligatoire — le projet utilise le namespace `jakarta.*`, incompatible avec Tomcat 9 et antérieur)

### 1. Cloner le projet

```bash
git clone https://github.com/VOTRE_UTILISATEUR/TapTapSend.git
cd TapTapSend
```

### 2. Configurer la base de données

Créer la base :

```sql
CREATE DATABASE taptapsend;
```

Copier le fichier de configuration gabarit et le compléter avec vos identifiants PostgreSQL :

```bash
cp src/main/resources/META-INF/persistence.xml.example src/main/resources/META-INF/persistence.xml
```

Éditer `src/main/resources/META-INF/persistence.xml` et renseigner votre utilisateur/mot de passe PostgreSQL local. Les tables sont créées automatiquement au premier lancement (`hibernate.hbm2ddl.auto=update`).

### 3. Configurer l'envoi d'email (optionnel)

```bash
cp src/main/resources/email.properties.example src/main/resources/email.properties
```

Renseigner un compte SMTP (ex. mot de passe d'application Gmail). Sans configuration valide, l'application continue de fonctionner normalement — l'échec d'envoi est simplement journalisé.

## Lancer le projet

### Build

```bash
mvn clean package
```

Le livrable est généré dans `target/taptapsend.war`.

### Déploiement sur Tomcat

```bash
cp target/taptapsend.war /chemin/vers/tomcat/webapps/
/chemin/vers/tomcat/bin/startup.sh   # startup.bat sur Windows
```

L'application est ensuite accessible sur :

```
http://localhost:8080/taptapsend/
```

### Cycle de développement

| Fichier modifié | Action nécessaire |
|---|---|
| JSP, CSS, JS statique | Rechargement navigateur (`F5` / `Ctrl+Shift+R`) suffit — pas de redéploiement |
| Classe Java (Servlet, Service, DAO) | `mvn clean package` + redéploiement du `.war` |

## Structure du projet

```
TapTapSend/
├── src/main/java/com/taptapsend/
│   ├── controller/     Servlets
│   ├── service/        Logique métier
│   ├── dao/             Accès aux données (JPA/Hibernate)
│   ├── model/            Entités JPA
│   └── util/              Utilitaires (HibernateUtil, envoi d'email...)
├── src/main/resources/
│   ├── META-INF/persistence.xml.example
│   └── email.properties.example
├── src/main/webapp/
│   ├── WEB-INF/views/
│   │   ├── template.jsp          Layout commun (sidebar, topbar, modales)
│   │   ├── partials/               Fragments réutilisés (liens de navigation)
│   │   ├── Dashboard/
│   │   ├── client/
│   │   ├── envoyer/
│   │   ├── taux/
│   │   └── frais/
│   └── assets/
│       ├── css/theme.css        Design system
│       └── js/script.js
└── pom.xml
```

## Sécurité

Points traités lors de la refonte :

- **Secrets hors du dépôt** — `persistence.xml` et `email.properties` sont ignorés par Git (`.gitignore`) ; seuls des fichiers `.example` avec des placeholders sont versionnés.
- **Protection XSS** — tout affichage de données utilisateur dans les JSP passe par `<c:out>` plutôt que par de l'interpolation EL brute (`${...}`), qui exécuterait du HTML/JS injecté dans un champ texte.
- **Validation métier côté serveur** — les règles de transfert (pays différents, cohérence de devise, solde suffisant) sont dupliquées côté client (JS, pour le confort utilisateur) et côté serveur (source de vérité).

Point encore à traiter en production : ajout d'une protection CSRF sur les formulaires POST (non présente actuellement, acceptable en contexte de démonstration/portfolio mais à ajouter avant tout déploiement réel).

## Pistes d'amélioration

- Pagination serveur pour les listes clients/envois si le volume de données dépasse quelques centaines de lignes
- Authentification et gestion des rôles (l'application n'a actuellement pas de couche d'accès utilisateur)
- Protection CSRF sur les formulaires
- Passage des identifiants de connexion à des variables d'environnement plutôt qu'à un fichier de configuration statique
- Mode sombre
- Tests unitaires sur la couche service (règles de transfert, calcul des frais)

---

## Licence

Projet personnel à but éducatif / portfolio.