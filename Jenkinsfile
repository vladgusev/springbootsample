node {
   def mvnHome
   stage('Preparation') { // for display purposes
      // Get some code from a GitHub repository
      git 'https://github.com/arunneoz/springbootsample.git'
      // Get the Maven tool.
      // ** NOTE: This 'M3' Maven tool must be configured
      // **       in the global configuration.
      mvnHome = tool 'M3'
      sh 'gcloud components update'
      sh 'gcloud components install beta'
      sh 'mkdir tmp-docker-build-context'
      sh "mkdir -p tmp-docker-build-context/cdbg"
      sh "cp -r Dockerfile tmp-docker-build-context"
      sh "cp -r cdbg tmp-docker-build-context/cdbg"
      def output = sh returnStdout: true, script: 'ls -l tmp-docker-build-context'
      echo output
   }
   stage('UnitTest & Build') {
      // Run the maven build
      if (isUnix()) {
         sh "'${mvnHome}/bin/mvn' -Dmaven.test.failure.ignore clean package"
      } else {
         bat(/"${mvnHome}\bin\mvn" -Dmaven.test.failure.ignore clean package/)
      }
      junit '**/target/surefire-reports/TEST-*.xml'
   }



 stage ('Push to GCR') {
      // prepare docker build context
      //sh "cp target/project.war ./tmp-docker-build-context"

      // Build and push image with Jenkins' docker-plugin
       tag = sh (
      script: 'git rev-parse --short HEAD',
      returnStdout: true
      ).trim()

      sh "cp target/MicroServiceSample-0.0.1-SNAPSHOT.jar tmp-docker-build-context"

       // withDockerRegistry([credentialsId: 'source:inboundrunedevopsgke', url: 'https://us.gcr.io']) {
          // we give the image the same version as the .war package
          withDockerRegistry([credentialsId: 'gcr:inbound-rune-cicdtaw', url: "https://us.gcr.io"]) {
          def image = docker.build("us.gcr.io/inbound-rune-cicdtaw/microservicesample:${tag}", "--build-arg PACKAGE_VERSION=${tag} tmp-docker-build-context")
          image.push()
          }

     }

     
     stage('Attest Branch Image') {

          sh "./binauth/generate_signature.sh"


    }

}
