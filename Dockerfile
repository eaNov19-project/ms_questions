FROM openjdk:8-jdk-alpine
EXPOSE 8080
COPY target/ms_questions.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
