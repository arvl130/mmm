# 😱 Meme Manager MMM

MMM manages memes.

![Screenshot of the home page](https://raw.githubusercontent.com/arvl130/mmm/master/screenshots/1.png)
![Screenshot of semantic search](https://raw.githubusercontent.com/arvl130/mmm/master/screenshots/2.png)
![Screenshot of AI tag suggestions](https://raw.githubusercontent.com/arvl130/mmm/master/screenshots/3.png)

This is a personal project I built to help me search memes I have saved
from  the Internet.

I also built this project to gain a deeper understanding on how Spring Boot,
Spring Security, Spring Session, Spring Data JPA, Spring AI, AWS SDK for Java,
Lombok, Hibernate and a bunch of other Java libraries work to build a scalable,
fully functioning web application.

If this application is useful or interesting to you, consider giving it a
star on GitHub.

This application comes with a frontend web UI available [here](https://github.com/arvl130/mmm-ui).

## Features

- Semantic Search with [Cohere Embed](https://cohere.com/embed) and PostgreSQL [`pgvector`](https://github.com/pgvector/pgvector)
- Full Text Search with PostgreSQL [`tsvector`](https://www.postgresql.org/docs/current/textsearch.html)
- AI Tag Suggestions with [Anthropic Claude Sonnet 3](https://www.anthropic.com/news/claude-3-family)
- Authentication with Session management with [Spring Security](https://spring.io/projects/spring-security) and [Spring Session](https://spring.io/projects/spring-session)

## Setup

1. Clone this repository. 

```shell
git clone https://github.com/arvl130/mmm
```

2. Configure the environment variables.

```shell
export AWS_REGION=
export AWS_ACCESS_KEY_ID=
export AWS_SECRET_ACCESS_KEY=
export AWS_S3_BUCKET=
export AWS_S3_BUCKET_BASE_URL=
export AWS_BEDROCK_MODEL_ID=
export DATABASE_URL=
```

3. Install the project dependencies, compile the code, and build the application.

```shell
mvn clean install
```

4. Run the application.

```shell
mvn spring-boot:run
```

5. (Optional) Build and deploy to an AWS Lambda environment with the SAM CLI.

```shell
cp samconfig.toml.example samconfig.toml
vi samconfig.toml # Enter the values for your environment variables, then press :wq to quit.
sam build -u
sam deploy
```

## License

This project is licensed under the MIT License.

Copyright © 2025 Angelo Geulin
