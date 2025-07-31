#!/bin/bash

# Docker-based test execution script
# Usage: ./docker-run.sh [command] [options]

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to build Docker image
build_image() {
    print_status "Building Docker image..."
    docker build -t playwright-framework:latest .
    print_success "Docker image built successfully"
}

# Function to run tests in Docker
run_tests() {
    local browser=${1:-chromium}
    local environment=${2:-test}
    local suite=${3:-smoke}
    local parallel=${4:-true}
    local threads=${5:-2}
    
    print_status "Running tests in Docker container..."
    print_status "Browser: $browser, Environment: $environment, Suite: $suite"
    
    # Create output directories
    mkdir -p screenshots videos logs traces downloads reports
    
    # Run Docker container
    docker run --rm \
        -e BROWSER_TYPE=$browser \
        -e ENVIRONMENT=$environment \
        -e TEST_SUITE=$suite \
        -e PARALLEL_EXECUTION=$parallel \
        -e THREAD_COUNT=$threads \
        -e BROWSER_HEADLESS=true \
        -e SCREENSHOT_ON_FAILURE=true \
        -e VIDEO_RECORDING=false \
        -v "$(pwd)/screenshots:/app/screenshots" \
        -v "$(pwd)/videos:/app/videos" \
        -v "$(pwd)/logs:/app/logs" \
        -v "$(pwd)/traces:/app/traces" \
        -v "$(pwd)/downloads:/app/downloads" \
        -v "$(pwd)/reports:/app/reports" \
        playwright-framework:latest \
        java -jar app.jar \
        -Dbrowser.type=$browser \
        -Denvironment=$environment \
        -Dtest.suite=$suite \
        -Dparallel.execution=$parallel \
        -Dthread.count=$threads
    
    print_success "Docker tests completed"
}

# Function to start full environment
start_environment() {
    print_status "Starting full test environment with Docker Compose..."
    docker-compose up -d
    
    # Wait for services to be ready
    print_status "Waiting for services to be ready..."
    sleep 30
    
    # Check service health
    if docker-compose ps | grep -q "Up"; then
        print_success "Test environment started successfully"
        print_status "Services available:"
        print_status "- Mock API: http://localhost:8080"
        print_status "- Database: localhost:5432"
        print_status "- Selenium Hub: http://localhost:4444"
        print_status "- Allure Reports: http://localhost:5050"
    else
        print_error "Failed to start test environment"
        docker-compose logs
        exit 1
    fi
}

# Function to stop environment
stop_environment() {
    print_status "Stopping test environment..."
    docker-compose down -v
    print_success "Test environment stopped"
}

# Function to run tests with full environment
run_with_environment() {
    local browser=${1:-chromium}
    local environment=${2:-test}
    local suite=${3:-smoke}
    
    print_status "Running tests with full Docker environment..."
    
    # Start environment
    start_environment
    
    # Wait a bit more for everything to be ready
    sleep 10
    
    # Run tests
    docker-compose exec playwright-tests \
        java -jar app.jar \
        -Dbrowser.type=$browser \
        -Denvironment=$environment \
        -Dtest.suite=$suite \
        -Dapi.base.url=http://api:1080 \
        -Ddb.url=jdbc:postgresql://database:5432/testdb \
        -Ddb.user=testuser \
        -Ddb.password=testpass
    
    # Stop environment
    stop_environment
}

# Function to show logs
show_logs() {
    local service=${1:-playwright-tests}
    print_status "Showing logs for service: $service"
    docker-compose logs -f $service
}

# Function to clean up Docker resources
cleanup() {
    print_status "Cleaning up Docker resources..."
    
    # Stop and remove containers
    docker-compose down -v --remove-orphans
    
    # Remove unused images
    docker image prune -f
    
    # Remove unused volumes
    docker volume prune -f
    
    print_success "Docker cleanup completed"
}

# Function to show usage
show_usage() {
    echo "Docker Test Execution Script"
    echo ""
    echo "Usage: $0 [command] [options]"
    echo ""
    echo "Commands:"
    echo "  build                           Build Docker image"
    echo "  test [browser] [env] [suite]    Run tests in Docker container"
    echo "  start                           Start full test environment"
    echo "  stop                            Stop test environment"
    echo "  run-env [browser] [env] [suite] Run tests with full environment"
    echo "  logs [service]                  Show service logs"
    echo "  cleanup                         Clean up Docker resources"
    echo ""
    echo "Examples:"
    echo "  $0 build                        Build the Docker image"
    echo "  $0 test firefox test regression Run regression tests on Firefox"
    echo "  $0 start                        Start all services"
    echo "  $0 run-env chromium staging api Run API tests with full environment"
    echo "  $0 logs playwright-tests        Show test execution logs"
    echo "  $0 cleanup                      Clean up all Docker resources"
}

# Main execution
case "$1" in
    "build")
        build_image
        ;;
    "test")
        run_tests "$2" "$3" "$4" "$5" "$6"
        ;;
    "start")
        start_environment
        ;;
    "stop")
        stop_environment
        ;;
    "run-env")
        run_with_environment "$2" "$3" "$4"
        ;;
    "logs")
        show_logs "$2"
        ;;
    "cleanup")
        cleanup
        ;;
    "help"|"-h"|"--help"|"")
        show_usage
        ;;
    *)
        print_error "Unknown command: $1"
        show_usage
        exit 1
        ;;
esac
