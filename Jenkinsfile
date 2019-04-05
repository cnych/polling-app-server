def label = "worker-${UUID.randomUUID().toString()}"

node(label) {
    stage('Prepare') {
        echo "1.Prepare Stage"
        checkout scm
        script {
            build_tag = sh(returnStdout: true, script: 'git rev-parse --short HEAD').trim()
            if (env.BRANCH_NAME != 'master') {
                build_tag = "${env.BRANCH_NAME}-${build_tag}"
            }
        }
    }
    stage('Test') {
      echo "2.Test Stage"
    }
    stage('Build') {
        echo "3.Build Docker Image Stage"
        sh "docker build -t registry.qikqiak.com/course/polling-server:${build_tag} ."
    }
    stage('Push') {
        echo "4.Push Docker Image Stage"
        withCredentials([usernamePassword(credentialsId: 'dockerHub', passwordVariable: 'dockerHubPassword', usernameVariable: 'dockerHubUser')]) {
            sh "docker login -u ${dockerHubUser} -p ${dockerHubPassword}"
            sh "docker push registry.qikqiak.com/course/polling-server:${build_tag}"
        }
    }
    stage('Deploy') {
        echo "5. Deploy Stage"
        if (env.BRANCH_NAME == 'master') {
            input "确认要部署线上环境吗？"
        }
        sh "sed -i 's/<BUILD_TAG>/${build_tag}/' manifests/deployment.yaml"
        sh "sed -i 's/<CI_ENV>/${env.BRANCH_NAME}/' manifests/deployment.yaml manifests/service.yaml"
        sh "kubectl apply -f manifests/deployment.yaml --record"
        sh "kubectl apply -f manifests/service.yaml"
        sh "kubectl rollout status -f manifests/deployment.yaml"
        sh "kubectl get all,ing -l ref=${env.BRANCH_NAME}"
    }
}
