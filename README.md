# Almundo Challenge - Call Center Dispatcher

The main idea of this project is resolve a problem with concurrency in a common environment (Call Center), the problem is to receive multiple calls with a limited number of employees.

The proposed solution is developed using mainly the threads in java:

- The `Dispatcher` class developed implement the `Runnable` interface and has 2 `ConcurrentLinkedDeque`, this is used in order to guarantee the use of synchronous resources between the threads, and with that assure the limit of calls in progress concurrently.

- The `Employee` class developed implement the `Runnable` interface and has once `ConcurrentLinkedDeque`, this structure is used to maintain updated the waiting calls to processing for the 'Employee'. For `Employee`, it was necessary to create an enumeration to control the status of availability and manage with locks into threads.

[Model of solution](/info/model.png)

## Getting Started

![Example execution](/info/log.gif)

Create a copy of this repository and open with your IDE, you need a Maven for compilation.

When you are ready for start go to the folder and run the follow command:

```
mvn clean install
```

## Built With

* [Java 1.8](https://java.com/) - Language and version of compilation
* [Maven](https://maven.apache.org/) - Dependency Management

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/your/project/tags). 

## Authors

* **Jhonatan Zambrano** - *Initial work* - [jzherran](https://github.com/jzherran)

## License

This project is licensed under the MIT License
