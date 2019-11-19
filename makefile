img=islamahmad/eaproj-questionms:1.0.3

# ===== Maven =====
maven-rebuild:
	mvn clean && mvn install

# ===== Docker =====
docker-build: maven-rebuild
	docker build -t ${img} .

docker-run:
	docker run -p 8080:8092 ${img}

docker-login:
	docker login

docker-push: docker-login docker-build
	docker push ${img}

build:
	 mvn clean && mvn install && docker build -t ${img} . && docker push $(img)
k8-install:
	kubectl apply -f k8s-deploy.yaml

config:
	kubectl apply -f k8s-config.yaml
k8-delete:
	kubectl delete -f k8s-deploy.yaml

k8-restart: k8-delete k8-install

deploy:
	kubectl apply -f k8s-deploy.yaml
