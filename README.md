# Spring Boot + Apache Camel

This is a series of examples to show you how Apache Camel works.

# Libraries and Tools
* [Module] [`Spring Boot`](https://spring.io/projects/spring-boot)
* [Module] [`Apache Camel`](https://camel.apache.org/)
* [Library for auto-generating getters, setters, constructors and others] [`Lombok`](https://projectlombok.org/)
* [Database] `H2`

# How it works
* Start `SpringBootApacheCamelApplication`: It will initiates a `H2` database with a `tbl_user` table filled
with four mock users. There is a column `status` which is assigned `NEW` for all the mock data.
* Go to [`http://localhost:8080/throw`](http://localhost:8080/throw): It will starts a camel route which
tries to change the first two users' `status` to `IN-C` and the second two users' `status` to `IN-D`.
In the middle of this process, we throw an `IllegalArgumentException` and we expect that camel rollbacks
the transactions and all the records remains unchanged to `NEW`.
* Go to [`http://localhost:8080/log`](http://localhost:8080/log) to log the users on the console and
verify the rollback mechanism.

* These is a lot of other test which have been provided as main classes. You can start them simply as java 
applications. For example  `CamelInMemorySagaExample` is a sample that shows how `Saga` EIP works.

