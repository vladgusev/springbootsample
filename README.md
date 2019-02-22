```
#Setup on Ubuntu 
PROJECT_ID=$PROJECT_ID
GCP_REGION=us-central1
INSTANCE=binauth-1
ZONE=us-central1-c

#Create a VM
#gcloud compute --project=${PROJECT_ID} instances create ${INSTANCE} --zone=${ZONE} --machine-type=n1-standard-1 --scopes=https://www.googleapis.com/auth/cloud-platform --image-family=ubuntu-1804-lts --image-project=gce-uefi-images

#Update with required packages (Set all the above variables on the box)
sudo apt-get install zip git -y
sudo apt-get update && sudo apt-get install -y apt-transport-https google-cloud-sdk
curl -s https://packages.cloud.google.com/apt/doc/apt-key.gpg | sudo apt-key add -
echo "deb https://apt.kubernetes.io/ kubernetes-xenial main" | sudo tee -a /etc/apt/sources.list.d/kubernetes.list
sudo apt-get update
sudo apt-get install -y kubectl

#Workaround --Do Not Do this in real environment.
#gcloud container clusters create deletme --machine-type=n1-standard-1 --zone=us-central1

#Set all required vars
PROJECT_ID=$PROJECT_ID
GCP_REGION=us-central1
ZONE=us-central1-c
CLUSTER_NAME=binauth-cluster
CLUSTER_REGION=us-central1
MASTER_PASS="my123passAbCdEfG"

#Install Helm
wget https://storage.googleapis.com/kubernetes-helm/helm-v2.12.3-linux-amd64.tar.gz
tar zxfv helm-v2.12.3-linux-amd64.tar.gz
sudo cp linux-amd64/helm /usr/bin/.
helm init
helm update


#Install Terraform
wget https://releases.hashicorp.com/terraform/0.11.11/terraform_0.11.11_linux_amd64.zip
unzip terraform_0.11.11_linux_amd64.zip
sudo cp terraform /usr/bin/.


##Install GKE, Jenkins, Sonar

git clone https://github.com/arunneoz/tf-gke-jenkins-sq-artifactory.git
cd tf-gke-jenkins-sq-artifactory/cicd

#Run value sub for k8cluster
M_FILE=main.tf
sed -i "s/gcp_project.*\$/gcp_project = \""${PROJECT_ID}"\"/" ${M_FILE}
sed -i "s/gcp_region.*\$/gcp_region = \""${GCP_REGION}"\"/" ${M_FILE}
sed -i "s/master_version.*\$/master_version = \"1.11.6-gke.2\"/" ${M_FILE}
sed -i "s/cluster_name.*\$/cluster_name = \""${CLUSTER_NAME}"\"/" ${M_FILE}
sed -i "s/cluster_region.*\$/cluster_region = \""${CLUSTER_REGION}"\"/" ${M_FILE}
sed -i "s/master_password.*\$/master_password = \""${MASTER_PASS}"\"/" ${M_FILE}

#Execute build via terraform
terraform init
terraform plan -out gke_jenkins_sonar.plan
terraform apply gke_jenkins_sonar.plan

#Login in to your cluster
gcloud container clusters get-credentials gke-cluster --region $GCP_REGION --project $PROJECT_ID

#Get Jenkins credentials
kubectl get secret --namespace cicd jenkins -o jsonpath="{.data.jenkins-admin-password}" | base64 --decode;echo
```
