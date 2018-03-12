# Almundo Challenge - Call Center Dispatcher

The main idea of this project is to solve a problem with concurrency in a common environment (Call Center), the problem is to receive multiple calls with a limited number of employees.

The solution proposal was developed using mainly threads in Java:

- The `Dispatcher` class implements the `Runnable` interface and has a `ConcurrentLinkedDeque`, this class is used in order to guarantee the use of synchronous resources between the threads, and assure the limit of calls concurrently in progress.

- The `Employee` class implements the `Runnable` interface and has a `ConcurrentLinkedDeque`, this structure is used to to keep updated the waiting calls to process for the `Employee`. For the `Employee` class, it was necessary to create an enumeration to control the availability status and manage it with locks into threads.

In this implementation, a log was created with descriptions for the most relevant cases, this log describes the process for a received call. An example is displayed in the animation below.

[Model of solution](/info/model.png)

## Getting Started

The following log shows the execution of `Dispatcher` service for a configuration of 10 employees who concurrently receive 30 calls with a random value between 5 and 10 seconds in duration.

![Example execution](/info/log.gif)

* Maven is necessary to compile this project.
* Clone the project and use your IDE for open it.
* Run maven clean install inside of the main path of the project.
```
mvn clean install
```

* In the principal package of the project you can find the main class called `CallCenter`, it is a Main java class for test with values for diferents configurations.

## Built With

* [Java 1.8](https://java.com/) - Language and version of compilation
* [Maven](https://maven.apache.org/) - Dependency Management

## Libraries
  
* [SLF4J](https://mvnrepository.com/artifact/org.slf4j) - Logger
* [Apache commons lang](https://mvnrepository.com/artifact/org.apache.commons/commons-lang3) - Commons utilities
* [Lombok](https://projectlombok.org/) - Manage boilerplate code
* [JUnit](https://junit.org/junit4/) - Testing framework
* [Mockito](http://site.mockito.org/) - Mocking framework for unit test

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/jzherran/Almundo-Challenge/tags). 

## Authors

* **Jhonatan Zambrano** - *Initial work* - [jzherran](https://github.com/jzherran)

## License

This project is licensed under the MIT License
