#!/bin/bash

PROJECT_ID=inbound-rune-cicdtaw
ATTESTOR="secops-attestor"
ATTESTOR_EMAIL=arundevsecops@gmail.com #the email associated with the project
GENERATED_PAYLOAD="generated_payload.json"
GENERATED_SIGNATURE="generated_signature.pgp"
PRIVATE_KEY_FILE="/etc/attest-volume/my-private-key.asc"
PUBLIC_KEY_FILE="generated-key.pgp"
SVC_ACCT="/etc/sa-volume/key.json"

IMAGE_PATH="us.gcr.io/${PROJECT_ID}/microservicesample"
IMAGE_DIGEST="$(gcloud container images list-tags --format='get(digest)' $IMAGE_PATH | head -1)"

rm ${GENERATED_PAYLOAD}

gcloud auth activate-service-account --key-file=${SVC_ACCT} --no-user-output-enabled

gcloud beta container binauthz create-signature-payload \
    --artifact-url="${IMAGE_PATH}@${IMAGE_DIGEST}" > ${GENERATED_PAYLOAD}

cat "${GENERATED_PAYLOAD}"

#echo -n $BINAUTH_PRIVATE_KEY | cat /tmp/my-base64-private-key.asc


export GNUPGHOME="$(mktemp -d)"

echo "IMPORTING PUBLIC AND PRIVATE KEYS"
gpg --import "$PUBLIC_KEY_FILE"
gpg --import "$PRIVATE_KEY_FILE"

PGP_FINGERPRINT="$(gpg --list-keys ${ATTESTOR_EMAIL} | head -2 | tail -1 | awk '{print $1}')"
echo "PGPFINGERPRINT IS ${PGP_FINGERPRINT}"


rm ${GENERATED_SIGNATURE}
gpg --local-user "${ATTESTOR_EMAIL}" \
    --armor \
    --output ./${GENERATED_SIGNATURE} \
    --sign ./${GENERATED_PAYLOAD}

gcloud beta container binauthz attestations create \
    --artifact-url="${IMAGE_PATH}@${IMAGE_DIGEST}" \
    --attestor="projects/${PROJECT_ID}/attestors/${ATTESTOR}" \
    --signature-file=${GENERATED_SIGNATURE} \
    --pgp-key-fingerprint="${PGP_FINGERPRINT}"

gcloud beta container binauthz attestations list \
    --attestor="projects/${PROJECT_ID}/attestors/${ATTESTOR}"

echo "Attestation created by Attestor: ${ATTESTOR} for Image: ${IMAGE_PATH}@${IMAGE_DIGEST}"

#echo "DELETING PODS AND EVENTS..."
#kubectl delete pod --all
#kubectl delete event --all


#echo "SLEEPING FOR 40 SECS..."
#sleep 40
#echo "DONE DONE DONE"

#echo "DEPLOYING THE NEW VERSION"

#cat << EOF | kubectl create -f -
#apiVersion: v1
#kind: Pod
#metadata:
#  name: containerdemo
#spec:
#  containers:
#  - name: containerdemo
#    image: "${IMAGE_PATH}@${IMAGE_DIGEST}"
#    ports:
#    - containerPort: 3000
#EOF

#kubectl rollout status deployment/nginx
#kubectl get pods
