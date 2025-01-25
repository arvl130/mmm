FROM openjdk:21
COPY --from=public.ecr.aws/awsguru/aws-lambda-adapter:0.9.0 /lambda-adapter /opt/extensions/lambda-adapter
RUN groupadd app && useradd -m -g app app
USER app
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]