SHEL = /bin/bash

dev-start:;
	mvn spring-boot:run -Dspring-boot.run.profiles=dev

clean:;
	mvn clean

compile-quick: clean;
	mvn compile package -Dmaven.test.skip=true

compile-start: compile-quick;
	docker compose down
	docker compose build
	docker compose up -d

test: clean;
	mvn test