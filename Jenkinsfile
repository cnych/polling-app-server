def label = "worker-${UUID.randomUUID().toString()}"

podTemplate(label: label, containers: [
  containerTemplate(name: 'maven', image: 'maven:3.6-alpine', command: 'cat', ttyEnabled: true),
  containerTemplate(name: 'docker', image: 'docker', command: 'cat', ttyEnabled: true),
  containerTemplate(name: 'kubectl', image: 'cnych/kubectl', command: 'cat', ttyEnabled: true),
  containerTemplate(name: 'helm', image: 'cnych/helm', command: 'cat', ttyEnabled: true)
], volumes: [
  hostPathVolume(mountPath: '/root/.m2', hostPath: '/var/run/m2'),
  hostPathVolume(mountPath: '/home/jenkins/.kube', hostPath: '/root/.kube'),
  hostPathVolume(mountPath: '/var/run/docker.sock', hostPath: '/var/run/docker.sock')
]) {
  node(label) {
    def myRepo = checkout scm
    def gitCommit = myRepo.GIT_COMMIT
    def gitBranch = myRepo.GIT_BRANCH
    def shortGitCommit = "${gitCommit[0..10]}"
    def previousGitCommit = sh(script: "git rev-parse ${gitCommit}~", returnStdout: true)
    def imageTag = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
    def image = "${dockerRegistryUrl}/${imageEndpoint}"
 
    stage('Test') {
      echo "Test stage"
      echo "${myRepo}"
      echo "${gitCommit}-${gitBranch}-${shortGitCommit}-${previousGitCommit}-${imageTag}-${image}"
    }
    stage('Build') {
      echo "Build stage"
      echo "${currentBuild}"
      try {
        container('maven') {
          sh """
            mvn clean package -Dmaven.test.skip=true
            """
        }
      }
      catch (exc) {
        println "Failed to build - ${currentBuild.fullDisplayName}"
        throw(exc)
      }
    }
    stage('Create Docker images') {
      container('docker') {
        withCredentials([[$class: 'UsernamePasswordMultiBinding',
          credentialsId: 'dockerhub',
          usernameVariable: 'DOCKER_HUB_USER',
          passwordVariable: 'DOCKER_HUB_PASSWORD']]) {
          sh """
            docker login ${dockerRegistryUrl} -u ${DOCKER_HUB_USER} -p ${DOCKER_HUB_PASSWORD}
            docker build -t ${image}:${imageTag} .
            docker tag ${image}:${imageTag} ${image}
            docker push ${image}:${imageTag}
            docker push ${image}
            """
        }
      }
    }
    stage('Run kubectl') {
      container('kubectl') {
        sh "kubectl get pods --all-namespaces"
      }
    }
    stage('Run helm') {
      container('helm') {
        sh "helm list"
      }
    }
  }
}