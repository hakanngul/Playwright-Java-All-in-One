pipeline {
    agent any
    
    parameters {
        choice(
            name: 'BROWSER_TYPE',
            choices: ['chromium', 'firefox', 'webkit', 'all'],
            description: 'Browser to run tests on'
        )
        choice(
            name: 'ENVIRONMENT',
            choices: ['dev', 'test', 'staging', 'prod'],
            description: 'Environment to run tests against'
        )
        choice(
            name: 'TEST_SUITE',
            choices: ['smoke', 'regression', 'api', 'ui', 'hybrid'],
            description: 'Test suite to execute'
        )
        booleanParam(
            name: 'PARALLEL_EXECUTION',
            defaultValue: true,
            description: 'Enable parallel test execution'
        )
        string(
            name: 'THREAD_COUNT',
            defaultValue: '3',
            description: 'Number of parallel threads'
        )
    }
    
    environment {
        JAVA_HOME = tool 'JDK-21'
        MAVEN_HOME = tool 'Maven-3.9'
        PATH = "${JAVA_HOME}/bin:${MAVEN_HOME}/bin:${PATH}"
        MAVEN_OPTS = '-Xmx2048m -XX:MaxPermSize=512m'
    }
    
    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 2, unit: 'HOURS')
        timestamps()
        ansiColor('xterm')
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    env.BUILD_VERSION = sh(
                        script: "echo '1.0.${BUILD_NUMBER}'",
                        returnStdout: true
                    ).trim()
                }
            }
        }
        
        stage('Build') {
            steps {
                sh '''
                    echo "Building with Maven..."
                    mvn clean compile -B -V
                '''
            }
        }
        
        stage('Unit Tests') {
            steps {
                sh '''
                    echo "Running unit tests..."
                    mvn test -Dtest="**/unit/**" -B
                '''
            }
            post {
                always {
                    publishTestResults testResultsPattern: 'target/surefire-reports/TEST-*.xml'
                }
            }
        }
        
        stage('Install Playwright') {
            steps {
                sh '''
                    echo "Installing Playwright browsers..."
                    mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install --with-deps"
                '''
            }
        }
        
        stage('Integration Tests') {
            parallel {
                stage('UI Tests') {
                    when {
                        anyOf {
                            params.TEST_SUITE == 'ui'
                            params.TEST_SUITE == 'regression'
                        }
                    }
                    steps {
                        script {
                            def browsers = params.BROWSER_TYPE == 'all' ? ['chromium', 'firefox', 'webkit'] : [params.BROWSER_TYPE]
                            
                            browsers.each { browser ->
                                stage("UI Tests - ${browser}") {
                                    sh """
                                        echo "Running UI tests on ${browser}..."
                                        mvn test \\
                                            -DsuiteXmlFile=src/test/resources/suites/ui-smoke.xml \\
                                            -Dbrowser.type=${browser} \\
                                            -Denvironment=${params.ENVIRONMENT} \\
                                            -Dbrowser.headless=true \\
                                            -Dparallel.execution=${params.PARALLEL_EXECUTION} \\
                                            -Dthread.count=${params.THREAD_COUNT} \\
                                            -Dscreenshot.on.failure=true \\
                                            -Dvideo.recording=true \\
                                            -B
                                    """
                                }
                            }
                        }
                    }
                }
                
                stage('API Tests') {
                    when {
                        anyOf {
                            params.TEST_SUITE == 'api'
                            params.TEST_SUITE == 'regression'
                        }
                    }
                    steps {
                        sh """
                            echo "Running API tests..."
                            mvn test \\
                                -DsuiteXmlFile=src/test/resources/suites/api-smoke.xml \\
                                -Denvironment=${params.ENVIRONMENT} \\
                                -Dparallel.execution=${params.PARALLEL_EXECUTION} \\
                                -Dthread.count=${params.THREAD_COUNT} \\
                                -B
                        """
                    }
                }
                
                stage('Hybrid Tests') {
                    when {
                        anyOf {
                            params.TEST_SUITE == 'hybrid'
                            params.TEST_SUITE == 'regression'
                        }
                    }
                    steps {
                        sh """
                            echo "Running hybrid tests..."
                            mvn test \\
                                -DsuiteXmlFile=src/test/resources/suites/hybrid-tests.xml \\
                                -Dbrowser.type=${params.BROWSER_TYPE == 'all' ? 'chromium' : params.BROWSER_TYPE} \\
                                -Denvironment=${params.ENVIRONMENT} \\
                                -Dbrowser.headless=true \\
                                -Dparallel.execution=false \\
                                -B
                        """
                    }
                }
            }
            post {
                always {
                    publishTestResults testResultsPattern: 'target/surefire-reports/TEST-*.xml'
                    
                    // Archive test artifacts
                    archiveArtifacts artifacts: '''
                        target/surefire-reports/**,
                        screenshots/**,
                        videos/**,
                        logs/**,
                        traces/**
                    ''', allowEmptyArchive: true
                }
            }
        }
        
        stage('Generate Reports') {
            steps {
                sh '''
                    echo "Generating test reports..."
                    mvn surefire-report:report-only
                    mvn site -DgenerateReports=false
                '''
            }
            post {
                always {
                    publishHTML([
                        allowMissing: false,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'target/site',
                        reportFiles: 'surefire-report.html',
                        reportName: 'Test Report'
                    ])
                }
            }
        }
        
        stage('Package') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                }
            }
            steps {
                sh '''
                    echo "Packaging application..."
                    mvn package -DskipTests -B
                '''
                
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }
        
        stage('Docker Build') {
            when {
                branch 'main'
            }
            steps {
                script {
                    def image = docker.build("playwright-framework:${env.BUILD_VERSION}")
                    
                    docker.withRegistry('https://registry.hub.docker.com', 'docker-hub-credentials') {
                        image.push()
                        image.push('latest')
                    }
                }
            }
        }
        
        stage('Performance Tests') {
            when {
                anyOf {
                    params.TEST_SUITE == 'regression'
                    branch 'main'
                }
            }
            steps {
                sh '''
                    echo "Running performance tests..."
                    mvn test \\
                        -Dtest.groups=performance \\
                        -Dbrowser.type=chromium \\
                        -Denvironment=${ENVIRONMENT} \\
                        -Dbrowser.headless=true \\
                        -Dparallel.execution=false \\
                        -B
                '''
            }
        }
    }
    
    post {
        always {
            // Clean workspace
            cleanWs()
        }
        
        success {
            emailext (
                subject: "✅ Build Success: ${env.JOB_NAME} - ${env.BUILD_NUMBER}",
                body: """
                    <h2>Build Successful!</h2>
                    <p><strong>Job:</strong> ${env.JOB_NAME}</p>
                    <p><strong>Build Number:</strong> ${env.BUILD_NUMBER}</p>
                    <p><strong>Browser:</strong> ${params.BROWSER_TYPE}</p>
                    <p><strong>Environment:</strong> ${params.ENVIRONMENT}</p>
                    <p><strong>Test Suite:</strong> ${params.TEST_SUITE}</p>
                    <p><strong>Build URL:</strong> <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
                """,
                to: "${env.CHANGE_AUTHOR_EMAIL}, qa-team@company.com",
                mimeType: 'text/html'
            )
        }
        
        failure {
            emailext (
                subject: "❌ Build Failed: ${env.JOB_NAME} - ${env.BUILD_NUMBER}",
                body: """
                    <h2>Build Failed!</h2>
                    <p><strong>Job:</strong> ${env.JOB_NAME}</p>
                    <p><strong>Build Number:</strong> ${env.BUILD_NUMBER}</p>
                    <p><strong>Browser:</strong> ${params.BROWSER_TYPE}</p>
                    <p><strong>Environment:</strong> ${params.ENVIRONMENT}</p>
                    <p><strong>Test Suite:</strong> ${params.TEST_SUITE}</p>
                    <p><strong>Build URL:</strong> <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
                    <p><strong>Console Output:</strong> <a href="${env.BUILD_URL}console">${env.BUILD_URL}console</a></p>
                """,
                to: "${env.CHANGE_AUTHOR_EMAIL}, qa-team@company.com",
                mimeType: 'text/html'
            )
        }
        
        unstable {
            emailext (
                subject: "⚠️ Build Unstable: ${env.JOB_NAME} - ${env.BUILD_NUMBER}",
                body: """
                    <h2>Build Unstable!</h2>
                    <p>Some tests failed, but the build completed.</p>
                    <p><strong>Job:</strong> ${env.JOB_NAME}</p>
                    <p><strong>Build Number:</strong> ${env.BUILD_NUMBER}</p>
                    <p><strong>Build URL:</strong> <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
                """,
                to: "${env.CHANGE_AUTHOR_EMAIL}, qa-team@company.com",
                mimeType: 'text/html'
            )
        }
    }
}
