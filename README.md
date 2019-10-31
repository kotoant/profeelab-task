# Account Service
Java test task for Profee.Lab.

## Installation Prerequisites
* Git
* Maven 3.x
* Java 1.8

## Get latest sources from the repository
You can clone the repository using git with SSH:
```
git clone git@github.com:kotoant/profeelab-task.git
cd profeelab-task
```
Or use HTTPS:
```
git clone https://github.com/kotoant/profeelab-task.git
cd profeelab-task
```
Alternatively can just download [profeelab-task-master.zip](https://github.com/kotoant/profeelab-task/archive/master.zip) and unzip it:
```
wget https://github.com/kotoant/profeelab-task/archive/master.zip -O profeelab-task-master.zip
unzip profeelab-task-master.zip
cd profeelab-task-master
```

## Build the project from the source code
The build phase include unit tests:
```
mvn clean verify
```
Release project (generate sources and javadocs):
```
mvn -P release clean install
```
Running integration test:
```
mvn -P integration-test clean verify
```
Whole release cycle with unit- and integration-tests with generating sources and javadocs:
```
mvn -P release,integration-test clean install
```

## Running the application
In your project directory, run this:
```
mvn spring-boot:run
```
You should see something like the following:
```
...
2019-10-30 23:01:13.111  INFO 7924 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8080 (http)
2019-10-30 23:01:13.120  INFO 7924 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2019-10-30 23:01:13.121  INFO 7924 --- [           main] org.apache.catalina.core.StandardEngine  : Starting Servlet engine: [Apache Tomcat/9.0.27]
2019-10-30 23:01:13.211  INFO 7924 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2019-10-30 23:01:13.211  INFO 7924 --- [           main] o.s.web.context.ContextLoader            : Root WebApplicationContext: initialization completed in 1697 ms
2019-10-30 23:01:13.885  INFO 7924 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''
2019-10-30 23:01:13.947  WARN 7924 --- [           main] org.apache.tomcat.util.modeler.Registry  : The MBean registry cannot be disabled because it has already been initialised
2019-10-30 23:01:13.956  INFO 7924 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8081 (http)
2019-10-30 23:01:13.957  INFO 7924 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2019-10-30 23:01:13.957  INFO 7924 --- [           main] org.apache.catalina.core.StandardEngine  : Starting Servlet engine: [Apache Tomcat/9.0.27]
2019-10-30 23:01:13.976  INFO 7924 --- [           main] o.a.c.c.C.[Tomcat-1].[localhost].[/]     : Initializing Spring embedded WebApplicationContext
2019-10-30 23:01:13.977  INFO 7924 --- [           main] o.s.web.context.ContextLoader            : Root WebApplicationContext: initialization completed in 89 ms
2019-10-30 23:01:13.994  INFO 7924 --- [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 3 endpoint(s) beneath base path ''
2019-10-30 23:01:14.073  INFO 7924 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8081 (http) with context path ''
2019-10-30 23:01:14.076  INFO 7924 --- [           main] task.AccountServiceApplication           : Started AccountServiceApplication in 2.944 seconds (JVM running for 3.348)

```
The application is now listening on ports `8080` for application requests and `8081` for administration requests. If you press `^C`, the application will shut down gracefully, first closing the server socket, then waiting for in-flight requests to be processed, then shutting down the process itself.

However, while it’s up, let’s give it a whirl! [Click here to get first account][1]! [Click here to get second one][2]!

[1]: http://localhost:8080/accounts/1
[2]: http://localhost:8080/accounts/2

So, we’re getting accounts. Awesome. But that’s not all our application can do. One of the main reasons for using Spring Boot Actuator is the out-of-the-box operational tools it provides, all of which can be found [on the admin port](http://localhost:8081/).

If you click through to the [metrics resource](http://localhost:8081/metrics), you can see all of your application’s metrics.

The [threads resource](http://localhost:8081/threads) allows you to quickly get a thread dump of all the threads running in that process.

The [healthcheck resource](http://localhost:8081/healthcheck) runs preconfigured health check indicators. You should see something like this:
```
{
    "status": "UP",
    "components": {
        "db": {
            "status": "UP",
            "details": {
                "database": "HSQL Database Engine",
                "result": 1,
                "validationQuery": "SELECT COUNT(*) FROM INFORMATION_SCHEMA.SYSTEM_USERS"
            }
        },
        "diskSpace": {
            "status": "UP",
            "details": {
                "total": 640027717632,
                "free": 26515034112,
                "threshold": 10485760
            }
        },
        "ping": {
            "status": "UP"
        }
    }
}
```

### REST API
Account service exposes three operations: 1 GET method to get account by id and 2 POST methods: create and transfer:

#### GET method: /accounts/{accountId}
Sample request:
```
$ curl http://localhost:8080/accounts/1
```
Sample response:
```
{"accountId":1,"amount":123.45000000}
```

#### POST method: /accounts/create
Sample request:
```
$ curl -H "Content-Type: application/json" -d '{"amount": 100.500}' http://localhost:8080/accounts/create
```
Sample response:
```
{"accountId":3}
```

#### POST method: /accounts/transfer
Sample request:
```
$ curl -H "Content-Type: application/json" -d '{"from": 1, "to": 2, "amount": 3.45}' http://localhost:8080/accounts/transfer
```
Sample response:
```
OK
```

## Used Frameworks and Tools
* Git as version control system
* Maven to build project
* Spring Boot for application bootstrapping
* Guava for caching of locks
* MyBatis for JDBC operations
* HSQLDB for In-Memory database
* JUnit, Mockito, AssertJ, Hamcrest for testing

## Implementation Notes
The implementation assumes that there is only one instance of system-writers to the database and it is the developed service.
So all processing threads are correctly synchronized within this one service.

If we want to complicate the solution and add at least one more instance of the service that can write to the database we should use service-to-service synchronization.
It can be achieved with help of external system - for example Hazelcast or it can be done on database level.
For example optimistic locking can be used: when only updated row is locked. It can be achieved by adding one more filed to the table structure: version.
So when we want to update the row we should always check its version.
There is out-of-the-box solution for that from Hibernate.

## Further Enhancement
* We can add Swagger that enriches our service with comprehensive documentation as well as allows to automate testing of our API.
* We can provide more dao methods for CRUD operations: getAllAccounts (with offset and/or limit), deleteById, etc.
* We can also add audit information to the database the will contain whole transfer log plus history for all accounts.
There is Hibernate Envers module for that purpose.
