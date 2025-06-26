# Makefile pour le projet SVS Backend - Dakar
# 🚢 API Backend pour la gestion des factures et dépenses maritimes

# Variables
APP_NAME = svs-backend
VERSION = 1.0.0
JAVA_VERSION = 17
MAVEN_OPTS = -Dmaven.test.skip=false
DOCKER_COMPOSE_FILE = docker-compose.yml
DB_CONTAINER = svs-postgres
ADMINER_CONTAINER = svs-adminer

# Couleurs pour les messages
GREEN = \033[0;32m
YELLOW = \033[1;33m
RED = \033[0;31m
NC = \033[0m # No Color
BLUE = \033[0;34m

.PHONY: help install clean build test run run-dev run-prod stop logs docker-up docker-down docker-restart db-init db-reset lint format check-deps swagger health status

# Commande par défaut
help: ## 📋 Afficher l'aide
	@echo "$(BLUE)🚢 Makefile pour SVS Backend - Dakar$(NC)"
	@echo ""
	@echo "$(GREEN)Commandes disponibles :$(NC)"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "  $(YELLOW)%-20s$(NC) %s\n", $$1, $$2}'
	@echo ""

# =============================================================================
# GESTION DES DÉPENDANCES
# =============================================================================

install: ## 📦 Installer les dépendances Maven
	@echo "$(GREEN)📦 Installation des dépendances...$(NC)"
	mvn clean install -DskipTests
	@echo "$(GREEN)✅ Dépendances installées avec succès$(NC)"

check-deps: ## 🔍 Vérifier les dépendances Maven
	@echo "$(GREEN)🔍 Vérification des dépendances...$(NC)"
	mvn dependency:analyze
	mvn versions:display-dependency-updates

# =============================================================================
# COMPILATION ET BUILD
# =============================================================================

clean: ## 🧹 Nettoyer le projet
	@echo "$(GREEN)🧹 Nettoyage du projet...$(NC)"
	mvn clean
	@echo "$(GREEN)✅ Projet nettoyé$(NC)"

compile: ## ⚙️ Compiler le projet
	@echo "$(GREEN)⚙️ Compilation...$(NC)"
	mvn compile
	@echo "$(GREEN)✅ Compilation terminée$(NC)"

build: ## 🔨 Construire le projet (compile + package)
	@echo "$(GREEN)🔨 Construction du projet...$(NC)"
	mvn clean package -DskipTests
	@echo "$(GREEN)✅ Projet construit : target/$(APP_NAME)-$(VERSION).jar$(NC)"

build-full: ## 🔨 Construire le projet avec tests
	@echo "$(GREEN)🔨 Construction complète avec tests...$(NC)"
	mvn clean package
	@echo "$(GREEN)✅ Construction complète terminée$(NC)"

# =============================================================================
# TESTS
# =============================================================================

test: ## 🧪 Lancer tous les tests
	@echo "$(GREEN)🧪 Exécution des tests...$(NC)"
	mvn test

test-unit: ## 🧪 Tests unitaires uniquement
	@echo "$(GREEN)🧪 Tests unitaires...$(NC)"
	mvn test -Dtest="**/*Test"

test-integration: ## 🧪 Tests d'intégration uniquement
	@echo "$(GREEN)🧪 Tests d'intégration...$(NC)"
	mvn test -Dtest="**/*IT"

test-coverage: ## 📊 Tests avec couverture de code
	@echo "$(GREEN)📊 Tests avec couverture...$(NC)"
	mvn clean jacoco:prepare-agent test jacoco:report
	@echo "$(GREEN)✅ Rapport de couverture : target/site/jacoco/index.html$(NC)"

# =============================================================================
# LANCEMENT DE L'APPLICATION
# =============================================================================

run: ## 🚀 Démarrer l'application (profil dev)
	@echo "$(GREEN)🚀 Démarrage de l'application SVS Backend...$(NC)"
	@echo "$(BLUE)📍 URL: http://localhost:8080/api$(NC)"
	@echo "$(BLUE)📚 Swagger: http://localhost:8080/api/swagger-ui.html$(NC)"
	@echo "$(BLUE)🏢 Companies API: http://localhost:8080/api/companies$(NC)"
	mvn spring-boot:run -Dspring-boot.run.profiles=dev

run-dev: ## 🚀 Démarrer en mode développement
	@echo "$(GREEN)🚀 Mode développement avec hot-reload...$(NC)"
	mvn spring-boot:run -Dspring-boot.run.profiles=dev -Dspring-boot.run.jvmArguments="-Dspring.devtools.restart.enabled=true"

run-prod: ## 🚀 Démarrer en mode production
	@echo "$(GREEN)🚀 Mode production...$(NC)"
	java -jar -Dspring.profiles.active=prod target/$(APP_NAME)-$(VERSION).jar

run-background: ## 🚀 Démarrer en arrière-plan
	@echo "$(GREEN)🚀 Démarrage en arrière-plan...$(NC)"
	nohup mvn spring-boot:run -Dspring-boot.run.profiles=dev > logs/app.log 2>&1 &
	@echo "$(GREEN)✅ Application démarrée en arrière-plan$(NC)"

stop: ## ⏹️ Arrêter l'application
	@echo "$(YELLOW)⏹️ Arrêt de l'application...$(NC)"
	pkill -f "svs-backend" || true
	@echo "$(GREEN)✅ Application arrêtée$(NC)"

# =============================================================================
# GESTION BASE DE DONNÉES
# =============================================================================

db-start: ## 🗄️ Démarrer uniquement PostgreSQL
	@echo "$(GREEN)🗄️ Démarrage de PostgreSQL...$(NC)"
	docker-compose -f $(DOCKER_COMPOSE_FILE) up -d postgresql
	@echo "$(GREEN)✅ PostgreSQL démarré$(NC)"

db-stop: ## 🗄️ Arrêter PostgreSQL
	@echo "$(YELLOW)🗄️ Arrêt de PostgreSQL...$(NC)"
	docker-compose -f $(DOCKER_COMPOSE_FILE) stop postgresql
	@echo "$(GREEN)✅ PostgreSQL arrêté$(NC)"

db-connect: ## 🗄️ Se connecter à PostgreSQL
	@echo "$(GREEN)🗄️ Connexion à PostgreSQL...$(NC)"
	docker exec -it $(DB_CONTAINER) psql -U postgres -d svs_db

db-reset: ## 🗄️ Réinitialiser la base de données
	@echo "$(YELLOW)🗄️ Réinitialisation de la base...$(NC)"
	docker exec -it $(DB_CONTAINER) psql -U postgres -c "DROP DATABASE IF EXISTS svs_db;"
	docker exec -it $(DB_CONTAINER) psql -U postgres -c "CREATE DATABASE svs_db;"
	@echo "$(GREEN)✅ Base de données réinitialisée$(NC)"

db-backup: ## 💾 Sauvegarder la base de données
	@echo "$(GREEN)💾 Sauvegarde de la base...$(NC)"
	docker exec -t $(DB_CONTAINER) pg_dump -U postgres svs_db > backup_$(shell date +%Y%m%d_%H%M%S).sql
	@echo "$(GREEN)✅ Sauvegarde créée$(NC)"

db-fix-constraints: ## 🔧 Corriger les contraintes de base de données
	@echo "$(GREEN)🔧 Correction des contraintes de base de données...$(NC)"
	docker exec -i $(DB_CONTAINER) psql -U postgres -d svs_db << 'EOF'
	-- Supprimer les contraintes CHECK problématiques
	ALTER TABLE ships DROP CONSTRAINT IF EXISTS ships_type_navire_check;
	ALTER TABLE ships DROP CONSTRAINT IF EXISTS ships_pavillon_check;
	ALTER TABLE ships DROP CONSTRAINT IF EXISTS ships_classification_check;

	-- Vérifier les contraintes restantes
	SELECT conname as constraint_name FROM pg_constraint
	WHERE conrelid = (SELECT oid FROM pg_class WHERE relname = 'ships')
	    AND contype = 'c';
	EOF
	@echo "$(GREEN)✅ Contraintes corrigées$(NC)"

db-check-constraints: ## 🔍 Vérifier les contraintes existantes
	@echo "$(GREEN)🔍 Vérification des contraintes...$(NC)"
	docker exec -i $(DB_CONTAINER) psql -U postgres -d svs_db << 'EOF'
	SELECT
	    tc.constraint_name,
	    tc.constraint_type,
	    cc.check_clause
	FROM information_schema.table_constraints tc
	LEFT JOIN information_schema.check_constraints cc
	    ON tc.constraint_name = cc.constraint_name
	WHERE tc.table_name = 'ships'
	    AND tc.table_schema = 'public';
	EOF

db-recreate-schema: ## 🗑️ Recréer le schéma de base (ATTENTION: efface les données)
	@echo "$(RED)⚠️ ATTENTION: Cette commande va supprimer toutes les données!$(NC)"
	@read -p "Êtes-vous sûr? (y/N): " confirm && [ "$$confirm" = "y" ] || exit 1
	@echo "$(YELLOW)🗑️ Suppression et recréation du schéma...$(NC)"
	docker exec -i $(DB_CONTAINER) psql -U postgres << 'EOF'
	DROP DATABASE IF EXISTS svs_db;
	CREATE DATABASE svs_db;
	EOF
	@echo "$(GREEN)✅ Schéma recréé$(NC)"

test-ship-creation: ## 🧪 Tester la création d'un navire
	@echo "$(GREEN)🧪 Test de création d'un navire...$(NC)"
	@curl -X POST http://localhost:8080/api/v1/ships \
		-H "Content-Type: application/json" \
		-d '{"nom":"Test Ship","numeroIMO":"9123456","pavillon":"Sénégal","typeNavire":"Cargo","compagnieId":1,"portAttache":"Dakar","numeroAppel":"6V7ABC","numeroMMSI":"663123456","classification":"Bureau Veritas"}' \
		| jq '.' || echo "$(RED)❌ Création échouée$(NC)"

# =============================================================================
# LIQUIBASE
# =============================================================================

liquibase-update: ## 🔄 Appliquer les migrations Liquibase
	@echo "$(GREEN)🔄 Application des migrations...$(NC)"
	mvn liquibase:update

liquibase-status: ## 📊 Statut des migrations Liquibase
	@echo "$(GREEN)📊 Statut des migrations...$(NC)"
	mvn liquibase:status

liquibase-rollback: ## ⏪ Rollback Liquibase (1 changeset)
	@echo "$(YELLOW)⏪ Rollback des migrations...$(NC)"
	mvn liquibase:rollback -Dliquibase.rollbackCount=1

# =============================================================================
# MONITORING ET LOGS
# =============================================================================

logs: ## 📋 Voir les logs de l'application
	@echo "$(GREEN)📋 Logs de l'application...$(NC)"
	tail -f logs/svs-backend.log

health: ## 💚 Vérifier l'état de l'application
	@echo "$(GREEN)💚 Vérification de l'état...$(NC)"
	@curl -s http://localhost:8080/api/management/health | jq '.' || echo "$(RED)❌ Application non accessible$(NC)"

swagger: ## 📚 Ouvrir Swagger dans le navigateur
	@echo "$(GREEN)📚 Ouverture de Swagger...$(NC)"
	open http://localhost:8080/api/swagger-ui.html || xdg-open http://localhost:8080/api/swagger-ui.html

test-api: ## 🧪 Tester l'API
	@echo "$(GREEN)🧪 Test de l'API...$(NC)"
	@curl -s http://localhost:8080/api/test/hello | jq '.' || echo "$(RED)❌ API non accessible$(NC)"

test-companies: ## 🏢 Tester l'API Companies
	@echo "$(GREEN)🏢 Test de l'API Companies...$(NC)"
	@echo "$(BLUE)GET /api/companies$(NC)"
	@curl -s http://localhost:8080/api/companies | jq '.' || echo "$(RED)❌ API Companies non accessible$(NC)"
	@echo ""
	@echo "$(BLUE)GET /api/companies/active$(NC)"
	@curl -s http://localhost:8080/api/companies/active | jq '.' || echo "$(RED)❌ API Companies active non accessible$(NC)"

status: ## 📊 Statut complet du projet
	@echo "$(BLUE)📊 Statut du projet SVS Backend$(NC)"
	@echo "$(GREEN)🔍 Vérification des composants...$(NC)"
	@echo ""
	@echo "🐳 Docker:"
	@docker-compose -f $(DOCKER_COMPOSE_FILE) ps
	@echo ""
	@echo "🗄️ Base de données:"
	@docker exec $(DB_CONTAINER) pg_isready -U postgres || echo "$(RED)❌ PostgreSQL non accessible$(NC)"
	@echo ""
	@echo "🚀 Application:"
	@curl -s http://localhost:8080/api/management/health > /dev/null && echo "$(GREEN)✅ Application accessible$(NC)" || echo "$(RED)❌ Application non accessible$(NC)"
	@echo ""
	@echo "🏢 API Companies:"
	@curl -s http://localhost:8080/api/companies > /dev/null && echo "$(GREEN)✅ API Companies accessible$(NC)" || echo "$(RED)❌ API Companies non accessible$(NC)"

# =============================================================================
# QUALITÉ DE CODE
# =============================================================================

lint: ## 🔍 Vérifier la qualité du code
	@echo "$(GREEN)🔍 Vérification de la qualité...$(NC)"
	mvn checkstyle:check

format: ## 🎨 Formater le code
	@echo "$(GREEN)🎨 Formatage du code...$(NC)"
	mvn spotless:apply

# =============================================================================
# TESTS SPÉCIFIQUES COMPANIES
# =============================================================================

test-companies-crud: ## 🧪 Tester CRUD Companies
	@echo "$(GREEN)🧪 Test CRUD Companies...$(NC)"
	@echo "$(BLUE)1. Création d'une compagnie de test$(NC)"
	@curl -X POST http://localhost:8080/api/v1/companies \
		-H "Content-Type: application/json" \
		-d '{"nom":"Test Company","raisonSociale":"Test SARL","adresse":"Dakar","ville":"Dakar","pays":"Sénégal","telephone":"+221123456789","email":"test@company.sn"}' \
		| jq '.' || echo "$(RED)❌ Création échouée$(NC)"
	@echo ""
	@echo "$(BLUE)2. Liste des compagnies$(NC)"
	@curl -s http://localhost:8080/api/v1/companies | jq '.companies[] | {id, nom, email}' || echo "$(RED)❌ Liste échouée$(NC)"


test-ships: ## 🚢 Tester l'API Ships
	@echo "$(GREEN)🚢 Test de l'API Ships...$(NC)"
	@curl -s http://localhost:8080/api/v1/ships | jq '.ships[] | {id, nom, numeroIMO, typeNavire}' || echo "$(RED)❌ API Ships non accessible$(NC)"

# =============================================================================
# DÉPLOIEMENT
# =============================================================================

package: ## 📦 Créer le package de déploiement
	@echo "$(GREEN)📦 Création du package...$(NC)"
	mvn clean package -DskipTests
	@echo "$(GREEN)✅ Package créé : target/$(APP_NAME)-$(VERSION).jar$(NC)"

# =============================================================================
# INSTALLATION COMPLÈTE
# =============================================================================

setup: ## ⚡ Installation complète du projet
	@echo "$(BLUE)⚡ Installation complète du projet SVS Backend$(NC)"
	$(MAKE) clean
	$(MAKE) install
	$(MAKE) docker-up
	@echo "$(YELLOW)⏳ Attente de 10 secondes pour PostgreSQL...$(NC)"
	@sleep 10
	$(MAKE) compile
	@echo "$(GREEN)✅ Installation terminée !$(NC)"
	@echo "$(BLUE)🚀 Vous pouvez maintenant lancer: make run$(NC)"

# =============================================================================
# WORKFLOW COMPLET
# =============================================================================

dev: ## 🚀 Workflow développement complet
	@echo "$(BLUE)🚀 Démarrage du workflow développement$(NC)"
	$(MAKE) docker-up
	@sleep 5
	$(MAKE) run-dev

ci: ## 🔄 Workflow CI (tests + build)
	@echo "$(BLUE)🔄 Workflow CI$(NC)"
	$(MAKE) clean
	$(MAKE) compile
	$(MAKE) test
	$(MAKE) build

demo: ## 🎯 Démonstration complète de l'API
	@echo "$(BLUE)🎯 Démonstration de l'API SVS$(NC)"
	$(MAKE) test-api
	@echo ""
	$(MAKE) test-companies
	@echo ""
	$(MAKE) test-ships
	@echo ""
	@echo "$(GREEN)✅ Démonstration terminée$(NC)"
	@echo "$(BLUE)📚 Consultez Swagger: http://localhost:8080/api/swagger-ui.html$(NC)"


# =============================================================================
# STAGING COMMANDES
# =============================================================================


staging-down: ## ⬇️ Arrêter l'environnement de staging
	@echo "$(YELLOW)⬇️ Arrêt de l'environnement de staging...$(NC)"
	docker-compose --env-file .env.staging down
	@echo "$(GREEN)✅ Environnement de staging arrêté$(NC)"

staging-up: ## ⬇️ Arrêter l'environnement de staging
	@echo "$(YELLOW)⬇️ Arrêt de l'environnement de staging...$(NC)"
	docker-compose --env-file .env.staging up -d --build
	@echo "$(GREEN)✅ Environnement de staging arrêté$(NC)"

staging-logs: ## 📋 Voir les logs de l'environnement de staging
	@echo "$(GREEN)📋 Logs de l'environnement de staging...$(NC)"
	docker-compose logs -f backend



# =============================================================================
# DOCKER DEV COMMANDES
# =============================================================================

docker-dev-up: ## 🐳 Démarrer tous les conteneurs Docker
	@echo "$(GREEN)🐳 Démarrage des conteneurs Docker...$(NC)"
	docker-compose -f $(DOCKER_COMPOSE_FILE) --env-file .env.dev up -d
	@echo "$(GREEN)✅ Conteneurs démarrés$(NC)"
	@echo "$(BLUE)📊 Adminer: http://localhost:8082$(NC)"
	@echo "$(BLUE)📊 API: http://localhost:8081$(NC)"

docker-dev-build: ## 🐳 Arrêter tous les conteneurs Docker
	@echo "$(YELLOW)🐳 Build des conteneurs Docker...$(NC)"
	docker-compose -f $(DOCKER_COMPOSE_FILE) --env-file .env.dev build
	@echo "$(GREEN)✅ Conteneurs arrêtés$(NC)"

docker-dev-down: ## 🐳 Arrêter tous les conteneurs Docker
	@echo "$(YELLOW)🐳 Arrêt des conteneurs Docker...$(NC)"
	docker-compose -f $(DOCKER_COMPOSE_FILE) --env-file .env.dev down
	@echo "$(GREEN)✅ Conteneurs arrêtés$(NC)"

docker-dev-restart: ## 🐳 Redémarrer les conteneurs Docker
	@echo "$(YELLOW)🐳 Redémarrage des conteneurs...$(NC)"
	$(MAKE) docker-down
	$(MAKE) docker-up

docker-dev-logs: ## 📋 Voir les logs des conteneurs
	@echo "$(GREEN)📋 Logs des conteneurs Docker...$(NC)"
	docker-compose -f $(DOCKER_COMPOSE_FILE) --env-file .env.dev logs -f

docker-dev-clean: ## 🧹 Nettoyer Docker (volumes, images, etc.)
	@echo "$(YELLOW)🧹 Nettoyage Docker...$(NC)"
	docker-compose -f $(DOCKER_COMPOSE_FILE) --env-file .env.dev down -v
	docker system prune -f
	@echo "$(GREEN)✅ Docker nettoyé$(NC)"


# =============================================================================
# DOCKER PROD COMMANDES
# =============================================================================

docker-up: ## 🐳 Démarrer tous les conteneurs Docker
	@echo "$(GREEN)🐳 Démarrage des conteneurs Docker...$(NC)"
	docker-compose -f $(DOCKER_COMPOSE_FILE) --env-file .env.prod up -d
	@echo "$(GREEN)✅ Conteneurs démarrés$(NC)"
	@echo "$(BLUE)📊 Adminer: http://localhost:8082$(NC)"
	@echo "$(BLUE)📊 API: http://localhost:8081$(NC)"

docker-dev-build: ## 🐳 Arrêter tous les conteneurs Docker
	@echo "$(YELLOW)🐳 Build des conteneurs Docker...$(NC)"
	docker-compose -f $(DOCKER_COMPOSE_FILE) --env-file .env.prod build
	@echo "$(GREEN)✅ Conteneurs arrêtés$(NC)"

docker-dev-down: ## 🐳 Arrêter tous les conteneurs Docker
	@echo "$(YELLOW)🐳 Arrêt des conteneurs Docker...$(NC)"
	docker-compose -f $(DOCKER_COMPOSE_FILE) --env-file .env.prod down
	@echo "$(GREEN)✅ Conteneurs arrêtés$(NC)"

docker-dev-restart: ## 🐳 Redémarrer les conteneurs Docker
	@echo "$(YELLOW)🐳 Redémarrage des conteneurs...$(NC)"
	$(MAKE) docker-down
	$(MAKE) docker-up

docker-dev-logs: ## 📋 Voir les logs des conteneurs
	@echo "$(GREEN)📋 Logs des conteneurs Docker...$(NC)"
	docker-compose -f $(DOCKER_COMPOSE_FILE) --env-file .env.prod logs -f

docker-dev-clean: ## 🧹 Nettoyer Docker (volumes, images, etc.)
	@echo "$(YELLOW)🧹 Nettoyage Docker...$(NC)"
	docker-compose -f $(DOCKER_COMPOSE_FILE) --env-file .env.prod down -v
	docker system prune -f
	@echo "$(GREEN)✅ Docker nettoyé$(NC)"


# =============================================================================
# AIDE DÉTAILLÉE
# =============================================================================

info: ## ℹ️ Informations sur le projet
	@echo "$(BLUE)ℹ️ Informations du projet$(NC)"
	@echo "📛 Nom: $(APP_NAME)"
	@echo "🏷️ Version: $(VERSION)"
	@echo "☕ Java: $(JAVA_VERSION)"
	@echo "🌍 Environnement: Développement"
	@echo "🏢 Entreprise: SVS - Dakar, Sénégal"
	@echo ""
	@echo "🔗 URLs importantes:"
	@echo "  • API: http://localhost:8080/api"
	@echo "  • Swagger: http://localhost:8080/api/swagger-ui.html"
	@echo "  • Health: http://localhost:8080/api/management/health"
	@echo "  • Companies: http://localhost:8080/api/companies"
	@echo "  • PgAdmin: http://localhost:5050"
	@echo ""
	@echo "🚀 Endpoints Companies disponibles:"
	@echo "  • GET    /api/companies"
	@echo "  • POST   /api/companies"
	@echo "  • GET    /api/companies/{id}"
	@echo "  • PUT    /api/companies/{id}"
	@echo "  • DELETE /api/companies/{id}"
	@echo "  • GET    /api/companies/active"
	@echo "  • POST   /api/companies/search"