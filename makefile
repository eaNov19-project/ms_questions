# DOCKERHUBREPO=islamahmad
# IMAGE=${DOCKERHUBREPO}/eaproj-ms_questions:1.0.1
img="islamahmad/eaproj-questionms:1.0.5"
# ===== Maven =====()
# maven-rebuild:
# ===== Docker =====
# docker-run:
# 	docker run -p 8080:8092 islamahmad/eaproj-questionms:1.0.2
# docker-login:
# 	docker login
# k8-repush-restart: k8-delete docker-push k8-install

build:
	 mvn clean && mvn install && docker build -t ${img} . && docker push $(img)

config:
	kubectl apply -f k8s-config.yaml

deploy:
	kubectl apply -f k8s-deploy.yaml
