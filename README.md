## QEats Backend Service
## Overview
The QEats Backend Service is a crucial component of the QEats application ecosystem, responsible for managing restaurant data and serving restaurant information to the QEats frontend application. It provides a set of RESTful APIs that allow clients to perform operations such as retrieving restaurants based on proximity and search queries. The backend is implemented in Java using the Spring Boot framework, providing scalability, flexibility, and robustness.

## Features
**Restaurant Management:** CRUD (Create, Read, Update, Delete) operations for managing restaurant data, including restaurant details such as name, location, opening hours, and menu items.
**Restaurant Search:** The backend offers endpoints to retrieve restaurants based on various parameters, including geographical proximity, restaurant name, cuisine, and menu items.
**Multithreaded Search:** To optimize search query performance, the backend utilizes multithreading techniques to parallelize and expedite the processing of search queries, resulting in faster response times for clients.
**Geographical Utilities:** The backend includes utility methods for geographical calculations, such as distance calculation between two geographic coordinates, facilitating location-based search functionalities.

## Setup
Clone Repository: Begin by cloning the repository to your local development environment.

git clone <repository_url>

# Build Project: Use Gradle to build the project.
./gradlew build

# Run Application: Start the backend service by running the following command.
./gradlew bootRun

# The application will start running on http://localhost:8080.


## API Endpoints
The backend service exposes the following RESTful API endpoints:

**GET /restaurants:** Retrieve all restaurants.
**GET /restaurants/{restaurantId}:** Retrieve details of a specific restaurant.
**POST /restaurants:** Create a new restaurant.
**PUT /restaurants/{restaurantId}:** Update details of a restaurant.
**DELETE /restaurants/{restaurantId}:** Delete a restaurant.
**GET /restaurants/search:** Search for restaurants based on proximity and search queries.

For detailed information about request and response formats, please refer to the API documentation or Swagger UI integrated with the application.

## Configuration
Ensure to configure the following properties in the application.properties file:

**MongoDB URI:** Configure the URI for connecting to MongoDB, where restaurant data is stored.


## Dependencies

- **Java 8+**: The programming language used for backend development.
  - [Download](https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html)

- **Spring Boot 2.0+**: Framework for building robust and scalable Java applications.
  - Group ID: `org.springframework.boot`
  - Artifact ID: `spring-boot-starter-web`
  - Version: 2.0.+
  - [Documentation](https://spring.io/projects/spring-boot)

- **Spring Data MongoDB**: Spring Data module for MongoDB integration.
  - Group ID: `org.springframework.boot`
  - Artifact ID: `spring-boot-starter-data-mongodb`
  - Version: 2.0.+
  - [Documentation](https://spring.io/projects/spring-data-mongodb)

- **Spring Web**: Spring module for building web applications.
  - Group ID: `org.springframework`
  - Artifact ID: `spring-web`
  - Version: 5.0.+
  - [Documentation](https://spring.io/guides/gs/spring-boot/)

- **ModelMapper**: A mapping framework to convert one Java object to another.
  - Group ID: `org.modelmapper`
  - Artifact ID: `modelmapper`
  - Version: Latest
  - [Documentation](http://modelmapper.org/getting-started/)

- **Lombok**: Library to reduce boilerplate code in Java.
  - Group ID: `org.projectlombok`
  - Artifact ID: `lombok`
  - Version: Latest
  - [Documentation](https://projectlombok.org/)

- **Apache Commons Lang**: Provides helper utilities for the Java programming language.
  - Group ID: `org.apache.commons`
  - Artifact ID: `commons-lang3`
  - Version: Latest
  - [Documentation](https://commons.apache.org/proper/commons-lang/)

- **Guava**: Google's core libraries for Java.
  - Group ID: `com.google.guava`
  - Artifact ID: `guava`
  - Version: Latest
  - [Documentation](https://github.com/google/guava)

- **Jackson Core**: JSON processor for Java.
  - Group ID: `com.fasterxml.jackson.core`
  - Artifact ID: `jackson-core`
  - Version: Latest
  - [Documentation](https://github.com/FasterXML/jackson-core)

- **Spring Boot Starter Test**: Starter for testing Spring Boot applications.
  - Group ID: `org.springframework.boot`
  - Artifact ID: `spring-boot-starter-test`
  - Version: 2.0.+
  - [Documentation](https://spring.io/guides/gs/testing-web/)

## Contributors
The following individuals have contributed to the development of the QEats Backend Service:









# Food_Ordering_Application-QEats
Languages :- Java
Technologies/Frameworks :-Jackson, Lombok, Unit testing, HTTP, REST, Spring Boot, Interfaces, Mockito, Mongo DB,Spring Data, Interfaces, Redis, JMeter, Scientific Debugging

QEats is a popular food ordering app that allows users to browse and order their favorite dishes from nearby restaurants. During the course of this project,

1. Built different parts of the QEats backend which is a Spring Boot application.
2. Several REST API endpoints are implemented to query restaurant information and place food orders.
3. Improved the app performance under large load scenarios as well as included an advanced search feature in the app.

Link:- https://www.crio.do/learn/portfolio/kishorethalisetty/ME_QEATS_V2/?edit=true
