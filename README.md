# Spring Boot + Apache Camel

This is a simple example to show you how Apache Camel works with Spring Boot.

# Libraqries and Tools
* [Module] `Spring Boot`
* [Module] `Apache Camel`

# How it works
It is just a maven Spring Boot application which you can build and run it like any other Spring Boot application. 
On startup, a periodical `Hello World from the Second Route` message will be printed out every 2 seconds by one of the routes.
In addition, when you open your localhost at `http://localhost:8080/first` a message will be produced in a camel route called 
`direct:firstCamelRoute` and will be consumed by a consumer.
