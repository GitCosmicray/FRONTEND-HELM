pipeline {
  agent any

  environment {
    PROJECT_ID = 'consummate-rig-453502-q2'
    REGION = 'us-central1'
    REPO = 'private-image-repo'
    IMAGE_NAME = 'frontend-app'
    TAG = "v${BUILD_NUMBER}"
    GCP_KEY = credentials('GCP-jenkins-json') 
    // PAT_TOKEN = credentials('PAT-access-github')
  }

  stages {
    stage('Clone Repo') {
      steps {
        git url: 'https://github.com/GitCosmicray/frontend-repo.git', branch: 'main'
      }
    }

    stage('Build Docker Image') {
      steps {
        sh '''
          gcloud auth activate-service-account --key-file=${GCP_KEY}
          gcloud config set project ${PROJECT_ID}
          gcloud auth configure-docker ${REGION}-docker.pkg.dev

          docker build -t ${REGION}-docker.pkg.dev/${PROJECT_ID}/${REPO}/${IMAGE_NAME}:${TAG} .
        '''
      }
    }

    stage('Push Image to Artifact Registry') {
      steps {
        sh '''
          docker push ${REGION}-docker.pkg.dev/${PROJECT_ID}/${REPO}/${IMAGE_NAME}:${TAG}
        '''
      }
    }

     stage('Clone Helm Repo and Update Tag') {
      steps {
        withCredentials([string(credentialsId: 'PAT-access-github', variable: 'TOKEN')]) {
          sh '''
            git clone https://$TOKEN@github.com/GitCosmicray/FRONTEND-HELM.git
            cd FRONTEND-HELM/frontend-helmm
            sed -i 's|tag:.*|tag: "'$TAG'"|' values.yaml
            git config user.name "jenkins-bot"
            git config user.email "jenkins@yourcompany.com"
            git add values.yaml
            git commit -m "Update image tag to $TAG"
            git push https://$TOKEN@github.com/GitCosmicray/FRONTEND-HELM.git
          '''
        }
      }
    }
  }
}


