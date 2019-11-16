FROM openjdk:8
COPY target/ms_questions.jar ms_questions.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","ms_questions.jar"]
