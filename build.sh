#!/bin/bash
# Script de build et démarrage Mediscreen - Compatible Linux/Mac
# Usage: ./build.sh [clean|start|stop|restart|logs]
# NOUVEAU: Compilation intégrée dans Docker (pas besoin de Maven local!)

set -e

# Détection de l'OS pour compatibilité Mac/Linux
OS="$(uname -s)"
case "${OS}" in
    Linux*)     MACHINE=Linux;;
    Darwin*)    MACHINE=Mac;;
    *)          MACHINE="UNKNOWN:${OS}"
esac

# Couleurs pour les logs
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

# Fonction pour afficher les logs colorés
log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')] $1${NC}"
}

error() {
    echo -e "${RED}[ERROR] $1${NC}"
}

warning() {
    echo -e "${YELLOW}[WARNING] $1${NC}"
}

info() {
    echo -e "${BLUE}[INFO] $1${NC}"
}

success() {
    echo -e "${PURPLE}[SUCCESS] $1${NC}"
}

# Fonction pour vérifier les prérequis (SIMPLIFIÉE!)
check_prerequisites() {
    log "🔍 Vérification des prérequis sur $MACHINE..."

    if ! command -v docker &> /dev/null; then
        error "Docker n'est pas installé ou accessible"
        info "Installation : https://docs.docker.com/get-docker/"
        exit 1
    fi

    # Vérification Docker Compose (version intégrée ou standalone)
    if ! command -v docker-compose &> /dev/null; then
        if ! docker compose version &> /dev/null; then
            error "Docker Compose n'est pas installé ou accessible"
            info "Installation : https://docs.docker.com/compose/install/"
            exit 1
        else
            info "Utilisation de 'docker compose' (version intégrée)"
            COMPOSE_CMD="docker compose"
        fi
    else
        info "Utilisation de 'docker-compose' (version standalone)"
        COMPOSE_CMD="docker-compose"
    fi

    success "✅ Tous les prérequis sont satisfaits!"
    info "🐳 Docker va s'occuper de Maven + Java + compilation"
}

# Fonction pour vérifier la structure du projet
check_project_structure() {
    log "📁 Vérification de la structure du projet..."

    local missing_dirs=()

    if [ ! -d "patient-service" ]; then
        missing_dirs+=("patient-service")
    fi

    if [ ! -d "gateway-service" ]; then
        missing_dirs+=("gateway-service")
    fi

    if [ ! -d "frontend-service" ]; then
        missing_dirs+=("frontend-service")
    fi

    if [ ! -f "docker-compose.yml" ]; then
        missing_dirs+=("docker-compose.yml")
    fi

    if [ ${#missing_dirs[@]} -ne 0 ]; then
        error "Structure de projet incomplète. Éléments manquants :"
        for dir in "${missing_dirs[@]}"; do
            error "  - $dir"
        done
        exit 1
    fi

    success "Structure du projet validée ✅"
}

# Fonction pour démarrer les services (SIMPLIFIÉE!)
start_services() {
    log "🚀 Démarrage de l'application Mediscreen..."
    info "🔨 Docker va compiler ET démarrer tous les services"

    # Build et démarrage en une commande
    $COMPOSE_CMD up -d --build

    log "⏳ Attente du démarrage des services..."
    info "Compilation en cours dans Docker (peut prendre 2-3 minutes la première fois)"

    # Attente plus longue pour la compilation
    local max_wait=180  # 3 minutes
    local wait_time=0

    while [ $wait_time -lt $max_wait ]; do
        if $COMPOSE_CMD ps | grep -q "Up"; then
            break
        fi
        echo -n "."
        sleep 5
        wait_time=$((wait_time + 5))
    done
    echo ""

    log "📊 Vérification de l'état des services..."
    $COMPOSE_CMD ps

    log "🔍 Test des health checks..."

    # Test Patient Service
    for i in {1..12}; do
        if curl -f http://localhost:8081/actuator/health &> /dev/null; then
            success "Patient Service démarré ✅"
            break
        fi
        info "Tentative $i/12 - Attente Patient Service..."
        sleep 10
    done

    # Test Gateway Service
    for i in {1..8}; do
        if curl -f http://localhost:8888/actuator/health &> /dev/null; then
            success "Gateway Service démarré ✅"
            break
        fi
        info "Tentative $i/8 - Attente Gateway Service..."
        sleep 5
    done

    # Test Frontend Service
    for i in {1..8}; do
        if curl -f http://localhost:8080/actuator/health &> /dev/null; then
            success "Frontend Service démarré ✅"
            break
        fi
        info "Tentative $i/8 - Attente Frontend Service..."
        sleep 5
    done

    success "🎉 Application Mediscreen démarrée avec succès !"
    echo ""
    info "🌐 Services disponibles :"
    info "  • 🖥️  Frontend:    http://localhost:8080"
    info "  • 🌉 Gateway:     http://localhost:8888"
    info "  • 👥 Patient API: http://localhost:8081"
    info "  • 🗄️  MySQL:       localhost:3307"
    info "  • 🍃 MongoDB:     localhost:27018"
    echo ""
    info "📚 Documentation API: http://localhost:8081/swagger-ui.html"
    info "📊 Health Checks: http://localhost:8081/actuator/health"
    info "📋 Pour voir les logs: ./build.sh logs"
}

# Fonction pour arrêter les services
stop_services() {
    # Déterminer la commande Docker Compose
    if ! command -v docker-compose &> /dev/null; then
        if ! docker compose version &> /dev/null; then
            error "Docker Compose n'est pas accessible"
            exit 1
        else
            COMPOSE_CMD="docker compose"
        fi
    else
        COMPOSE_CMD="docker-compose"
    fi

    log "🛑 Arrêt des services Docker..."
    $COMPOSE_CMD down
    success "Services arrêtés ✅"
}

# Fonction pour nettoyer complètement
clean_all() {
    # Déterminer la commande Docker Compose
    if ! command -v docker-compose &> /dev/null; then
        if ! docker compose version &> /dev/null; then
            error "Docker Compose n'est pas accessible"
            exit 1
        else
            COMPOSE_CMD="docker compose"
        fi
    else
        COMPOSE_CMD="docker-compose"
    fi

    warning "⚠️  Nettoyage complet (suppression des volumes de données)..."
    echo -e "${YELLOW}Cette action supprimera :"
    echo "  • Tous les containers Mediscreen"
    echo "  • Tous les volumes de données (MySQL + MongoDB)"
    echo "  • Toutes les données de patients et notes"
    echo "  • Les images Docker créées${NC}"
    echo ""
    read -p "Êtes-vous sûr de vouloir continuer ? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        log "Nettoyage en cours..."
        $COMPOSE_CMD down -v --remove-orphans --rmi local
        docker system prune -f
        success "Nettoyage terminé ✅"
    else
        info "Nettoyage annulé"
    fi
}

# Fonction pour afficher les logs
show_logs() {
    # Déterminer la commande Docker Compose
    if ! command -v docker-compose &> /dev/null; then
        if ! docker compose version &> /dev/null; then
            error "Docker Compose n'est pas accessible"
            exit 1
        else
            COMPOSE_CMD="docker compose"
        fi
    else
        COMPOSE_CMD="docker-compose"
    fi

    log "📋 Affichage des logs en temps réel..."
    info "Appuyez sur Ctrl+C pour arrêter"
    $COMPOSE_CMD logs -f
}

# Fonction pour redémarrer
restart_services() {
    log "🔄 Redémarrage des services..."
    check_prerequisites
    check_project_structure
    stop_services
    sleep 3
    start_services
}

# Fonction d'état des services
status_services() {
    # Déterminer la commande Docker Compose
    if ! command -v docker-compose &> /dev/null; then
        if ! docker compose version &> /dev/null; then
            error "Docker Compose n'est pas accessible"
            exit 1
        else
            COMPOSE_CMD="docker compose"
        fi
    else
        COMPOSE_CMD="docker-compose"
    fi

    log "📊 État actuel des services..."
    $COMPOSE_CMD ps
    echo ""

    info "🔍 Tests de connectivité :"

    # Test Frontend
    if curl -s -f http://localhost:8080/actuator/health &> /dev/null; then
        success "Frontend (8080) : ✅ Healthy"
    else
        error "Frontend (8080) : ❌ Unhealthy"
    fi

    # Test Gateway
    if curl -s -f http://localhost:8888/actuator/health &> /dev/null; then
        success "Gateway (8888) : ✅ Healthy"
    else
        error "Gateway (8888) : ❌ Unhealthy"
    fi

    # Test Patient API
    if curl -s -f http://localhost:8081/actuator/health &> /dev/null; then
        success "Patient API (8081) : ✅ Healthy"
    else
        error "Patient API (8081) : ❌ Unhealthy"
    fi

    # Test MySQL
    if $COMPOSE_CMD exec mysql-db mysqladmin ping -h localhost -u root -proot &> /dev/null; then
        success "MySQL (3307) : ✅ Accessible"
    else
        error "MySQL (3307) : ❌ Non accessible"
    fi

    # Test MongoDB
    if $COMPOSE_CMD exec mongodb mongosh --eval "db.adminCommand('ping')" &> /dev/null; then
        success "MongoDB (27018) : ✅ Accessible"
    else
        warning "MongoDB (27018) : ⚠️ Non testé (normal si pas encore utilisé)"
    fi

    echo ""
    info "📝 Test fonctionnel :"
    if curl -s -f http://localhost:8081/api/v1/patients &> /dev/null; then
        success "API Patients : ✅ Répond correctement"
    else
        error "API Patients : ❌ Ne répond pas"
    fi
}

# Fonction pour générer les rapports JaCoCo et Surefire
generate_reports() {
    log "📊 Génération des rapports JaCoCo et Surefire..."
    info "🐳 Utilisation d'un conteneur Maven temporaire"

    # Vérifier que Docker est disponible
    if ! command -v docker &> /dev/null; then
        error "Docker n'est pas accessible"
        exit 1
    fi

    # Nettoyer les anciens rapports
    warning "🧹 Nettoyage des anciens rapports..."
    rm -rf */target/site/jacoco 2>/dev/null || true
    rm -rf */target/surefire-reports 2>/dev/null || true
    rm -rf report-aggregate/target 2>/dev/null || true

    # Lancer Maven dans un conteneur Docker avec accès Docker pour TestContainers
    log "🔨 Compilation et exécution des tests avec Maven..."
    info "⏳ Cela peut prendre 2-3 minutes..."
    info "🐳 Montage du socket Docker pour les tests d'intégration TestContainers"
    echo ""

    docker run --rm \
        -v "$(pwd)":/workspace \
        -v /var/run/docker.sock:/var/run/docker.sock \
        -v "$HOME/.m2":/root/.m2 \
        -w /workspace \
        maven:3.9-eclipse-temurin-21 \
        mvn clean verify -Dmaven.test.failure.ignore=false

    if [ $? -eq 0 ]; then
        success "✅ Tests et rapports générés avec succès!"
        echo ""
        info "📊 Rapports générés :"
        echo ""
        success "  🎯 Rapport agrégé global :"
        info "     report-aggregate/target/site/jacoco-aggregate/index.html"
        echo ""
        success "  📁 Rapports individuels par service :"
        info "     patient-service/target/site/jacoco/index.html"
        info "     notes-service/target/site/jacoco/index.html"
        info "     assessment-service/target/site/jacoco/index.html"
        info "     frontend-service/target/site/jacoco/index.html"
        info "     gateway-service/target/site/jacoco/index.html"
        echo ""
        success "  📋 Rapports Surefire (résultats tests) :"
        info "     */target/surefire-reports/"
        echo ""

        # Proposer d'ouvrir le rapport agrégé
        if [ -f "report-aggregate/target/site/jacoco-aggregate/index.html" ]; then
            echo ""
            read -p "Voulez-vous ouvrir le rapport agrégé dans le navigateur ? (y/N): " -n 1 -r
            echo
            if [[ $REPLY =~ ^[Yy]$ ]]; then
                if command -v xdg-open &> /dev/null; then
                    xdg-open report-aggregate/target/site/jacoco-aggregate/index.html
                elif command -v open &> /dev/null; then
                    open report-aggregate/target/site/jacoco-aggregate/index.html
                else
                    warning "Impossible d'ouvrir automatiquement. Ouvrez manuellement le fichier :"
                    info "file://$(pwd)/report-aggregate/target/site/jacoco-aggregate/index.html"
                fi
            fi
        fi
    else
        error "❌ Échec de la génération des rapports"
        warning "Vérifiez les logs ci-dessus pour identifier le problème"
        exit 1
    fi
}

# Menu principal avec gestion d'erreur
main() {
    case "${1:-}" in
        "clean")
            clean_all
            ;;
        "start")
            check_prerequisites
            check_project_structure
            start_services
            ;;
        "stop")
            stop_services
            ;;
        "restart")
            restart_services
            ;;
        "logs")
            show_logs
            ;;
        "status")
            status_services
            ;;
        "reports")
            generate_reports
            ;;
        *)
            echo "🏥 Mediscreen - Script de gestion Docker"
            echo ""
            echo "Usage: $0 {start|stop|restart|logs|status|reports|clean}"
            echo ""
            echo "🚀 Commandes disponibles:"
            echo "  start   - Démarre tous les services (compile automatiquement)"
            echo "  stop    - Arrête tous les services"
            echo "  restart - Redémarre tous les services"
            echo "  logs    - Affiche les logs en temps réel"
            echo "  status  - Vérifie l'état des services"
            echo "  reports - Génère les rapports JaCoCo et Surefire"
            echo "  clean   - Nettoyage complet (⚠️ supprime les données)"
            echo ""
            echo "💡 Nouveauté: Docker compile tout automatiquement!"
            echo "   Plus besoin d'installer Maven ou Java sur votre machine"
            echo ""
            echo "Exemples:"
            echo "  ./build.sh start    # Démarrage complet"
            echo "  ./build.sh logs     # Voir les logs"
            echo "  ./build.sh status   # Vérifier l'état"
            echo "  ./build.sh reports  # Générer rapports tests"
            echo ""
            echo "Plateforme détectée: $MACHINE"
            exit 1
            ;;
    esac
}

# Gestion des erreurs
trap 'error "Script interrompu par erreur à la ligne $LINENO"' ERR

# Message de bienvenue
echo -e "${PURPLE}🏥 Mediscreen Docker Manager${NC}"
echo -e "${BLUE}Plateforme: $MACHINE | Docker: Multi-stage build${NC}"
echo ""

# Exécution
main "$@"