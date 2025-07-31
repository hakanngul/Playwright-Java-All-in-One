#!/bin/bash

# Playwright Test Execution Script
# Usage: ./run-tests.sh [browser] [environment] [suite] [parallel] [threads]

set -e

# Default values
BROWSER_TYPE=${1:-chromium}
ENVIRONMENT=${2:-test}
TEST_SUITE=${3:-smoke}
PARALLEL_EXECUTION=${4:-true}
THREAD_COUNT=${5:-2}

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check prerequisites
check_prerequisites() {
    print_status "Checking prerequisites..."
    
    # Check Java
    if ! command -v java &> /dev/null; then
        print_error "Java is not installed or not in PATH"
        exit 1
    fi
    
    # Check Maven
    if ! command -v mvn &> /dev/null; then
        print_error "Maven is not installed or not in PATH"
        exit 1
    fi
    
    # Check if Playwright browsers are installed
    if [ ! -d "$HOME/.cache/ms-playwright" ]; then
        print_warning "Playwright browsers not found. Installing..."
        mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install --with-deps"
    fi
    
    print_success "Prerequisites check completed"
}

# Function to set up test environment
setup_environment() {
    print_status "Setting up test environment..."
    
    # Create directories
    mkdir -p screenshots videos logs traces downloads
    
    # Set environment variables
    export BROWSER_TYPE=$BROWSER_TYPE
    export ENVIRONMENT=$ENVIRONMENT
    export BROWSER_HEADLESS=true
    export PARALLEL_EXECUTION=$PARALLEL_EXECUTION
    export THREAD_COUNT=$THREAD_COUNT
    export SCREENSHOT_ON_FAILURE=true
    export VIDEO_RECORDING=false
    
    print_success "Environment setup completed"
}

# Function to run tests based on suite
run_tests() {
    print_status "Running $TEST_SUITE tests with $BROWSER_TYPE browser on $ENVIRONMENT environment..."
    
    local suite_file=""
    local test_groups=""
    
    case $TEST_SUITE in
        "smoke")
            suite_file="src/test/resources/suites/ui-smoke.xml"
            ;;
        "regression")
            suite_file="src/test/resources/suites/regression.xml"
            ;;
        "api")
            suite_file="src/test/resources/suites/api-smoke.xml"
            ;;
        "hybrid")
            suite_file="src/test/resources/suites/hybrid-tests.xml"
            ;;
        "ui")
            test_groups="ui"
            ;;
        "performance")
            test_groups="performance"
            ;;
        *)
            print_error "Unknown test suite: $TEST_SUITE"
            print_status "Available suites: smoke, regression, api, hybrid, ui, performance"
            exit 1
            ;;
    esac
    
    # Build Maven command
    local mvn_cmd="mvn clean test"
    
    if [ -n "$suite_file" ]; then
        mvn_cmd="$mvn_cmd -DsuiteXmlFile=$suite_file"
    elif [ -n "$test_groups" ]; then
        mvn_cmd="$mvn_cmd -Dgroups=$test_groups"
    fi
    
    # Add system properties
    mvn_cmd="$mvn_cmd -Dbrowser.type=$BROWSER_TYPE"
    mvn_cmd="$mvn_cmd -Denvironment=$ENVIRONMENT"
    mvn_cmd="$mvn_cmd -Dbrowser.headless=true"
    mvn_cmd="$mvn_cmd -Dparallel.execution=$PARALLEL_EXECUTION"
    mvn_cmd="$mvn_cmd -Dthread.count=$THREAD_COUNT"
    mvn_cmd="$mvn_cmd -Dscreenshot.on.failure=true"
    
    # Execute tests
    print_status "Executing: $mvn_cmd"
    
    if eval $mvn_cmd; then
        print_success "Tests completed successfully"
        return 0
    else
        print_error "Tests failed"
        return 1
    fi
}

# Function to generate reports
generate_reports() {
    print_status "Generating test reports..."
    
    # Generate Surefire reports
    mvn surefire-report:report-only
    mvn site -DgenerateReports=false
    
    # Check if reports were generated
    if [ -f "target/site/surefire-report.html" ]; then
        print_success "Test reports generated successfully"
        print_status "Report location: target/site/surefire-report.html"
    else
        print_warning "Test reports could not be generated"
    fi
}

# Function to cleanup
cleanup() {
    print_status "Cleaning up..."
    
    # Kill any remaining processes
    pkill -f "java.*playwright" || true
    pkill -f "chrome" || true
    pkill -f "firefox" || true
    
    print_success "Cleanup completed"
}

# Function to display usage
show_usage() {
    echo "Usage: $0 [browser] [environment] [suite] [parallel] [threads]"
    echo ""
    echo "Parameters:"
    echo "  browser     : chromium, firefox, webkit, chrome, edge (default: chromium)"
    echo "  environment : dev, test, staging, prod (default: test)"
    echo "  suite       : smoke, regression, api, hybrid, ui, performance (default: smoke)"
    echo "  parallel    : true, false (default: true)"
    echo "  threads     : number of parallel threads (default: 2)"
    echo ""
    echo "Examples:"
    echo "  $0                                    # Run smoke tests with defaults"
    echo "  $0 firefox test regression true 3    # Run regression tests on Firefox with 3 threads"
    echo "  $0 chromium staging api false 1      # Run API tests on staging sequentially"
}

# Main execution
main() {
    print_status "Starting Playwright test execution..."
    print_status "Browser: $BROWSER_TYPE, Environment: $ENVIRONMENT, Suite: $TEST_SUITE"
    print_status "Parallel: $PARALLEL_EXECUTION, Threads: $THREAD_COUNT"
    
    # Set up trap for cleanup
    trap cleanup EXIT
    
    # Execute steps
    check_prerequisites
    setup_environment
    
    if run_tests; then
        generate_reports
        print_success "Test execution completed successfully!"
        exit 0
    else
        generate_reports
        print_error "Test execution failed!"
        exit 1
    fi
}

# Check if help is requested
if [[ "$1" == "-h" || "$1" == "--help" ]]; then
    show_usage
    exit 0
fi

# Run main function
main "$@"
