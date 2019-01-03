pipeline {
  agent any
  environment {
      output = ""
      tag =""
      qg=""
      image=""
  }
  stages {
    stage('Source') { // Get code
      steps {
        // get code from our Git repository
        git 'https://github.com/arunneoz/springbootsample.git'
        
         sh 'gcloud components update' // needed to update for bin auth
         sh 'gcloud components install beta'
         sh 'mkdir tmp-docker-build-context'
         sh "mkdir -p tmp-docker-build-context/cdbg"
         sh "cp -r Dockerfile tmp-docker-build-context"
         sh "cp -r cdbg tmp-docker-build-context/cdbg"
         script
         {
         output = sh returnStdout: true, script: 'ls -l tmp-docker-build-context'
         }
         echo output
      }
    }
    stage('Build & Test') { // Compile and do unit testing
      tools {
        maven 'M3'
      }
      steps {
        // run Gradle to execute compile and unit testing
        sh 'mvn -Dmaven.test.failure.ignore clean package'
        junit '**/target/surefire-reports/TEST-*.xml'
       // tag = """${sh(scirpt: 'git rev-parse --short HEAD',returnStdout: true).trim()}"""

      }
    }
    
    stage('Code Quality') {
    
     tools {
        maven 'M3'
      }
      steps {
    
      withSonarQubeEnv('DevopCQ') {
     // requires SonarQube Scanner for Maven 3.2+
        sh "mvn org.sonarsource.scanner.maven:sonar-maven-plugin:3.2:sonar -Dsonar.projectKey=devops -Dsonar.jacoco.reportPaths='target/jacoco.exec' -Dsonar.test.exclusions='**/test/**/*.*' -Dsonar.exclusions='**/ai/**/*.*,**/jdbc/**/*.*,**/mpt/**/*.*,**/jcr/**/*.*,**/JDBC*'"
       }
      }
    }
    
    stage ('Push to GCR') {
      // prepare docker build context
      //sh "cp target/project.war ./tmp-docker-build-context"

      // Build and push image with Jenkins' docker-plugin
      steps {
      script
        {
        tag = sh (
               script: 'git rev-parse --short HEAD',
               returnStdout: true
             ).trim()
         }
       

      sh "cp target/MicroServiceSample-0.0.1-SNAPSHOT.jar tmp-docker-build-context"

       // withDockerRegistry([credentialsId: 'source:inboundrunedevopsgke', url: 'https://us.gcr.io']) {
          // we give the image the same version as the .war package
          withDockerRegistry([credentialsId: 'gcr:inbound-rune-cicdtaw', url: "https://us.gcr.io"]) {
          script {
          image = docker.build("us.gcr.io/inbound-rune-cicdtaw/microservicesample:${tag}", "--build-arg PACKAGE_VERSION=${tag} tmp-docker-build-context")
          image.push()
          }
          }
     
      }
     }
   
      // No need to occupy a node
     stage("Quality Gate - Code Coverage") {
         
         when {
           not {
             buildingTag()
           }
          }
         steps{
             
       
         timeout(time: 1, unit: 'HOURS') { // Just in case something goes wrong, pipeline will be killed after a timeout
         script
         {
         qg = waitForQualityGate() // Reuse taskId previously collected by withSonarQubeEnv
       
         if (qg.status != 'SUCCESS') {
           error "Pipeline aborted due to quality gate failure: ${qg.status}"
            }
            else
            {
                sh "./binauth/generate_signature.sh"
              
            }
          } 
             
         }
      }  
     }
     
     stage ("Deploy Application in Dev") {
         
      steps
      {
      script
      {
      imageTag = sh (
        script: 'git rev-parse --short HEAD',
        returnStdout: true
        ).trim()
      }
          
          // Create namespace if it doesn't exist
        sh("kubectl get ns dev || kubectl create ns dev")
        // Don't use public load balancing for development branches
       // sh("sed -i.bak 's#LoadBalancer#ClusterIP#' ./k8s/services/frontend.yaml")
        sh("sed -i.bak 's#:1.0.0#:${imageTag}#' ./k8s/dev/*.yaml")
        sh("kubectl --namespace=dev apply -f k8s/services/")
        sh("kubectl --namespace=dev apply -f k8s/dev/")
        echo 'To access your environment run `kubectl proxy`'
        echo "Then access your service via http://localhost:8001/api/v1/proxy/namespaces/dev/services/msvcapp:80/"
          
      }
     }
     
  }
}
