# DOCKERHUBREPO=islamahmad
# IMAGE=${DOCKERHUBREPO}/eaproj-ms_questions:1.0.1

# ===== Maven =====
maven-rebuild:
	mvn clean && mvn install

# ===== Docker =====
docker-build: maven-rebuild
	docker build -t islamahmad/eaproj-questionms:1.0.2 .

docker-run:
	docker run -p 8080:8092 islamahmad/eaproj-questionms:1.0.2

docker-login:
	docker login

docker-push: docker-login docker-build
	docker push islamahmad/eaproj-questionms:1.0.2

k8-install:
	kubectl apply -f manifests/

k8-delete:
	kubectl delete -f manifests/

k8-repush-restart: k8-delete docker-push k8-install

