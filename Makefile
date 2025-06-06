# Makefile pour le projet Maritime Backend - SVS Dakar
# 🚢 API Backend pour la gestion des factures maritimes

# Variables
APP_NAME = svs-backend
VERSION = 1.0.0
JAVA_VERSION = 17
MAVEN_OPTS = -Dmaven.test.skip=false
DOCKER_COMPOSE_FILE = docker-compose.yml
DB_CONTAINER = maritime-postgres
PGADMIN_CONTAINER = maritime-pgadmin

# Couleurs pour les messages
GREEN = \033[0;32m
YELLOW = \033[1;33m
RED = \033[0;31m
NC = \033[0m # No Color
BLUE = \033[0;34m

.PHONY: help install clean build test run run-dev run-prod stop logs docker-up docker-down docker-restart db-init db-reset lint format check-deps swagger health status

# Commande par défaut
help: ## 📋 Afficher l'aide
	@echo "$(BLUE)🚢 Makefile pour Maritime Backend - SVS Dakar$(NC)"
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
	@echo "$(GREEN)🚀 Démarrage de l'application Maritime Backend...$(NC)"
	@echo "$(BLUE)📍 URL: http://localhost:8080/api$(NC)"
	@echo "$(BLUE)📚 Swagger: http://localhost:8080/api/swagger-ui.html$(NC)"
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
	pkill -f "maritime-backend" || true
	@echo "$(GREEN)✅ Application arrêtée$(NC)"

# =============================================================================
# DOCKER ET BASE DE DONNÉES
# =============================================================================

docker-up: ## 🐳 Démarrer tous les conteneurs Docker
	@echo "$(GREEN)🐳 Démarrage des conteneurs Docker...$(NC)"
	docker-compose -f $(DOCKER_COMPOSE_FILE) up -d
	@echo "$(GREEN)✅ Conteneurs démarrés$(NC)"
	@echo "$(BLUE)📊 PgAdmin: http://localhost:5050$(NC)"

docker-down: ## 🐳 Arrêter tous les conteneurs Docker
	@echo "$(YELLOW)🐳 Arrêt des conteneurs Docker...$(NC)"
	docker-compose -f $(DOCKER_COMPOSE_FILE) down
	@echo "$(GREEN)✅ Conteneurs arrêtés$(NC)"

docker-restart: ## 🐳 Redémarrer les conteneurs Docker
	@echo "$(YELLOW)🐳 Redémarrage des conteneurs...$(NC)"
	$(MAKE) docker-down
	$(MAKE) docker-up

docker-logs: ## 📋 Voir les logs des conteneurs
	@echo "$(GREEN)📋 Logs des conteneurs Docker...$(NC)"
	docker-compose -f $(DOCKER_COMPOSE_FILE) logs -f

docker-clean: ## 🧹 Nettoyer Docker (volumes, images, etc.)
	@echo "$(YELLOW)🧹 Nettoyage Docker...$(NC)"
	docker-compose -f $(DOCKER_COMPOSE_FILE) down -v
	docker system prune -f
	@echo "$(GREEN)✅ Docker nettoyé$(NC)"

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
	docker exec -it $(DB_CONTAINER) psql -U postgres -d maritime_db

db-reset: ## 🗄️ Réinitialiser la base de données
	@echo "$(YELLOW)🗄️ Réinitialisation de la base...$(NC)"
	docker exec -it $(DB_CONTAINER) psql -U postgres -c "DROP DATABASE IF EXISTS maritime_db;"
	docker exec -it $(DB_CONTAINER) psql -U postgres -c "CREATE DATABASE maritime_db;"
	@echo "$(GREEN)✅ Base de données réinitialisée$(NC)"

db-backup: ## 💾 Sauvegarder la base de données
	@echo "$(GREEN)💾 Sauvegarde de la base...$(NC)"
	docker exec -t $(DB_CONTAINER) pg_dump -U postgres maritime_db > backup_$(shell date +%Y%m%d_%H%M%S).sql
	@echo "$(GREEN)✅ Sauvegarde créée$(NC)"

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
	tail -f logs/maritime-backend.log

health: ## 💚 Vérifier l'état de l'application
	@echo "$(GREEN)💚 Vérification de l'état...$(NC)"
	@curl -s http://localhost:8080/api/management/health | jq '.' || echo "$(RED)❌ Application non accessible$(NC)"

swagger: ## 📚 Ouvrir Swagger dans le navigateur
	@echo "$(GREEN)📚 Ouverture de Swagger...$(NC)"
	open http://localhost:8080/api/swagger-ui.html || xdg-open http://localhost:8080/api/swagger-ui.html

test-api: ## 🧪 Tester l'API
	@echo "$(GREEN)🧪 Test de l'API...$(NC)"
	@curl -s http://localhost:8080/api/test/hello | jq '.' || echo "$(RED)❌ API non accessible$(NC)"

status: ## 📊 Statut complet du projet
	@echo "$(BLUE)📊 Statut du projet Maritime Backend$(NC)"
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
	@echo "$(BLUE)⚡ Installation complète du projet Maritime Backend$(NC)"
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

# =============================================================================
# AIDE DÉTAILLÉE
# =============================================================================

info: ## ℹ️ Informations sur le projet
	@echo "$(BLUE)ℹ️ Informations du projet$(NC)"
	@echo "📛 Nom: $(APP_NAME)"
	@echo "🏷️ Version: $(VERSION)"
	@echo "☕ Java: $(JAVA_VERSION)"
	@echo "🌍 Environnement: Développement"
	@echo "🏢 Entreprise: SVS Maritime - Dakar, Sénégal"
	@echo ""
	@echo "🔗 URLs importantes:"
	@echo "  • API: http://localhost:8080/api"
	@echo "  • Swagger: http://localhost:8080/api/swagger-ui.html"
	@echo "  • Health: http://localhost:8080/api/management/health"
	@echo "  • PgAdmin: http://localhost:5050"