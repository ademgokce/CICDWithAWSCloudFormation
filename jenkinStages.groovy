pipeline {
    agent any
    environment {
        ECR_REPO_URI = 'YOUR_ECR_REPO_URI'
        AWS_REGION = 'YOUR_AWS_REGION'
        SONARQUBE_SERVER = 'YOUR_SONARQUBE_SERVER_URL'
    }
    stages {
        stage('Clone Repository') {
            steps {
                git 'https://git-codecommit.YOUR_AWS_REGION.amazonaws.com/v1/repos/YOUR_REPO_NAME'
            }
        }
        stage('Build and Test') {
            steps {
                sh 'mvn clean install'
            }
        }
        stage('Code Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh 'mvn sonar:sonar'
                }
            }
        }
        stage('Build Docker Image') {
            steps {
                script {
                    docker.build("${ECR_REPO_URI}:latest")
                }
            }
        }
        stage('Push Docker Image to ECR') {
            steps {
                script {
                    docker.withRegistry("https://${ECR_REPO_URI}", 'ecr:YOUR_AWS_REGION:YOUR_ECR_CREDENTIALS_ID') {
                        docker.image("${ECR_REPO_URI}:latest").push()
                    }
                }
            }
        }
        stage('Deploy to ECS') {
            steps {
                sh 'ecs-deploy --region ${AWS_REGION} --cluster YOUR_ECS_CLUSTER --service-name YOUR_SERVICE_NAME --image ${ECR_REPO_URI}:latest'
            }
        }
    }
}
