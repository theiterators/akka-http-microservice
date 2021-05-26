# Akka HTTP microservice

This template lets you learn about:

*   starting standalone HTTP server,
*   handling simple, file-based configuration,
*   logging,
*   routing,
*   deconstructing requests,
*   unmarshaling JSON entities to Scala's case classes,
*   marshaling Scala's case classes to JSON responses,
*   error handling,
*   issuing requests to external services,
*   testing with mocking of external services.

It focuses on the HTTP part of the microservices and doesn't talk about database connection handling, etc.

Check out the code and don't forget to comment or ask questions on [Github](https://github.com/theiterators/akka-http-microservice) and [Twitter](https://twitter.com/luksow).

Below you will find a brief tutorial about how the service works. You can:

*   learn what a microservice is,
*   check what our microservice does,
*   or you can go straight to the code.

## What is a microservice?

Microservice is a tiny standalone program that can be used as a component of a bigger distributed system. Microservices:

*   are short and concise,
*   process only one bounded domain.

In order to be readable and rewritable, code in microservices is usually very short and brief. It's usually responsible for processing only one type of data (in this project it is IP location data). They rarely use the high level of abstraction over databases, networking, and other components. It all makes them easier to understand and easier to reuse in multiple projects.

Next: What does the example microservice do?

## Geolocation of IP addresses

Our example microservice has two main features. It should:

*   locate an IP address,
*   compute distances between locations of two IP addresses.

It should do all that by exposing two HTTP JSON endpoints:

*   `GET /ip/X.X.X.X` — which returns given IP's geolocation data,
*   `POST /ip` — which returns distance between two IPs geolocations given JSON request `{"ip1": "X.X.X.X", "ip2": "Y.Y.Y.Y"}`.

Next: Let's see how to run it!

## Running the template

Issue `$ sbt "~reStart"` to see the microservice compiling and running.

You can check out where are Google DNS servers by opening [`http://localhost:9000/ip/8.8.8.8`](http://localhost:9000/ip/8.8.8.8). As you can see in the URL, the browser will send GET request to the first endpoint.

You can also check our endpoints using `curl` command line tool:

    $ curl http://localhost:9000/ip/8.8.8.8

and

    $ curl -X POST -H 'Content-Type: application/json' http://localhost:9000/ip -d '{"ip1": "8.8.8.8", "ip2": "8.8.4.4"}'

for the second endpoint.

If you don't have curl installed you can install it [from the source](http://curl.haxx.se/docs/install.html), using your OS package manager or you can use Postman REST Client in your browser.

Next: Let's see how our responses look like

## The Geolocation IP responses

Responses should look like that:

    {
      "city": "Mountain View",
      "query": "8.8.8.8",
      "country": "United States",
      "lon": -122.0881,
      "lat": 37.3845
    }

for the first endpoint and

    {
      "distance": 4347.6243474947,
      "ip1Info": {
        "city": "Mountain View",
        "query": "8.8.8.8",
        "country": "United States",
        "lon": -122.0881,
        "lat": 37.3845
      },
      "ip2Info": {
        "city": "Norwell",
        "query": "93.184.216.34",
        "country": "United States",
        "lon": -70.8228,
        "lat": 42.1508
      }
    }

In the sbt output you can see the request/response logs the app generates.

Next: Now as we know what our microservice does, let's open up the code.

## Code overview

There are four significant parts of the code. These are:

*   [build.sbt](https://github.com/theiterators/akka-http-microservice/blob/master/build.sbt) and [plugins.sbt](https://github.com/theiterators/akka-http-microservice/blob/master/project/plugins.sbt) — the build scripts,
*   [application.conf](https://github.com/theiterators/akka-http-microservice/blob/master/src/main/resources/application.conf) — the seed configuration for our microservice,
*   [AkkaHttpMicroservice.scala](https://github.com/theiterators/akka-http-microservice/blob/master/src/main/scala/AkkaHttpMicroservice.scala) — our main Scala file.
*   [ServiceSpec.scala](https://github.com/theiterators/akka-http-microservice/blob/master/src/test/scala/ServiceSpec.scala) — an example `akka-http` server test.

The build scripts let SBT download all the dependencies for our project (including `akka-http`). They are described inside the build scripts part of the tutorial.

Configuration for our microservice is described in the configuration part of the tutorial.

The code implementing our microservice's logic is described in the "microservice's code" section.

## Build scripts

[build.sbt](https://github.com/theiterators/akka-http-microservice/blob/master/build.sbt) and [plugins.sbt](https://github.com/theiterators/akka-http-microservice/blob/master/project/plugins.sbt) hold the configuration for our build procedure.

### build.sbt

`build.sbt` provides our project with typical meta-data like project names and versions, declares Scala compiler flags and lists the dependencies.

*   `akka-actor` is the cornerstone of Actor system that `akka-http` and `akka-stream` are based on.
*   `akka-stream` is the library implementing Reactive Streams using Akka actors — a framework for building reactive applications.
*   `akka-http` is core library for creating reactive HTTP streams.
*   `circe-core` is a library for handling JSONs.
*   `circe-generic` is an extension to `circe-core` that offers auto generation of JSON encoders and decoders for case classes. 
*   `akka-http-circe` is a library for marshaling `circe`'s JSONs into requests and responses.
*   `akka-testkit` is a library that helps testing `akka`.
*   `akka-http-testkit` is a library that helps testing `akka-http` routing and responses.
*   `scalatest` is a standard Scala testing library.

### plugins.sbt

There are three plugins used in our project. These are:

*   `sbt-revolver` which is helpful for development. It recompiles and runs our microservice every time the code in files changes (`~reStart` sbt command). Notice that it is initialized inside `build.sbt`.
*   `sbt-assembly` is a great library that lets us deploy our microservice as a single .jar file.
*   `sbt-native-packager` is needed by Heroku to stage the app.

Next: As we know what are the dependencies of our project, let's see what is the minimal configuration needed for the project.

## Configuration

The seed configuration for our microservice is available in the [application.conf](https://github.com/theiterators/akka-http-microservice/blob/master/src/main/resources/application.conf). It consists of three things:

*   `akka` — Akka configuration,
*   `http` — HTTP server configuration,
*   `services` — external endpoints configuration.

The Akka part of the configuration will let us see more log messages on the console when developing the microservice.

HTTP interface needs to be given an interface that it will run on and port that will listen for new HTTP requests.

Our microservice uses external service `http://ip-api.com/` to find where the IP we're trying to find is.

When deploying microservice as a `.jar` file, one can overwrite the configuration values when running the jar.

    java -jar microservice.jar -Dservices.ip-api.port=8080

Using a configuration management system is also recommended as the amount of variables rises quickly. It is hard to maintain configuration files across the more complex microservice architecture.

Next: Let's see how is our configuration used in the code.

## Microservice's code

All of the code is held in [AkkaHttpMicroservice.scala](https://github.com/theiterators/akka-http-microservice/blob/master/src/main/scala/AkkaHttpMicroservice.scala). We can distinguish 6 parts of the code. These are:

*   the imports,
*   type declarations and business domain,
*   protocols,
*   networking logic,
*   routes,
*   main App declaration.

The names, order, and configuration are not standardized, but the list above will make it easier for us to reason about this code.

We won't get into many details about imports. The only thing worth remembering is that there are many `implicit values` imported and one should be cautious when removing the imports, as many of them can be marked as unused by one's IDE.

This section of the tutorial explains:

*   How to use Scala types in HTTP microservice?
*   How to do external HTTP requests?
*   How to declare HTTP routes?
*   What do our tests do?

## Scala types and protocols

To see the usage of Scala types and protocols inside our microservice open up the [AkkaHttpMicroservice.scala](https://github.com/theiterators/akka-http-microservice/blob/master/src/main/scala/AkkaHttpMicroservice.scala#L22). We have three type of types there:

*   `IpApiResponse` and `IpApiResponseStatus` — a case class (with dedicated enum) that models external API response. 
*   `IpPairSummaryRequest` — a case class that models our JSON HTTP request's body.
*   `IpInfo` and `IpInfoSummary` — case classes are used as an intermediate form of data that can be converted to response JSON.

### Modeling requests

`akka-http` can unmarshal any JSON request into a type. This way we can validate incoming requests and pass only the ones that are well-formed and complete. The easiest way to model requests is to create algebraic data types and instrument them with validations in a constructor (typical methods include using Scala's Predef library with its `require` method). Example:

     case class IpPairSummaryRequest(...) {
      ...
      require(ip1..split('.').map(_.toInt).map({s => s >= 0 && s <= 255}).fold(true)(_ && _), "wrong IP address")
      ...
    }

### Forming JSON response

One of the great features of `akka-http` is response marshaling. The responses will be implicitly converted into JSON whether they are `Option[T]`, `Future[T]`, etc. Proper errors and response codes will also be generated.

Using this feature requires:

*   Having contents of `ErrorAccumulatingCirceSupport` in scope,
*   declaring implicit JSON converters (here it's done inside `Protocols` trait).

Next: Making external HTTP requests.

## Making an external HTTP request

Handling communication with external HTTP services is done inside [`Service`](https://github.com/theiterators/akka-http-microservice/blob/master/src/main/scala/AkkaHttpMicroservice.scala#L64) trait.

### Making an HTTP request

Making a proper HTTP request using `akka-http` leverages the Reactive Streams approach. It requires:

*   defining an external service HTTP connection flow,
*   defining a proper HTTP request,
*   defining this request as a source,
*   connecting the request source through external service HTTP connection flow with so-called `Sink`.

In order for the flow to run, we also need `FlowMaterializer` and `ExecutionContext`. After the request is done, we get the standard `HttpResponse` that we need to handle.

### Handling the response

Handling `HttpResponse` consists of:

*   checking if the request was successful,
*   unmarshaling HTTP Entity into a case class.

The unmarshaling uses the protocol implicit values defined earlier. Unmarshaling works using `Future[T]`s so we can always handle any errors and exceptions raised by our validation logic.

Next: Declaring routes and responding to HTTP requests.

## Routing and running server

Routing directives can be found in the [`Service`](https://github.com/theiterators/akka-http-microservice/blob/master/src/main/scala/AkkaHttpMicroservice.scala#L85) trait.

`akka-http` provides lots of useful routing directives. One can use multiple directives by nesting them inside one another. The request will go deeper down the nested structure if only it complies with each of the directive's requirements. Some directives filter the requests while others help to deconstruct it. If the request passes all directives, the final `complete(...) {...}` block gets evaluated as a `HttpResponse`.

### Routing & filtering directives

Directives responsible for routing are:

*   `pathPrefix("ip")` — filters the request by its relative URI beginning,
*   `path("ip"/"my")`  — filters the request by its part of the URI relative to the hostname or `pathPrefix` directive in which it is nested,
*   `get` — filters GET requests,
*   `post` — filters POST requests,
*   and [many more.](https://doc.akka.io/docs/akka-http/current/routing-dsl/directives/index.html)

### Deconstructing request

Directives that let us deconstruct the request:

*   `entity(as[IpPairSummaryRequest])` — unmarshals HTTP entity into an object; useful for handling JSON requests,
*   `formFields('field1, 'field2)` — extracts form fields form POST request,
*   `headerValueByName("X-Auth-Token")` — extracts a header value by its name,
*   `path("member" / Segment / "books")` — the `Segment` part of the directive lets us extract a string from the URI,
*   and [many more.](https://doc.akka.io/docs/akka-http/current/routing-dsl/directives/index.html)

Directives can provide us with some values we can use later to prepare a response:

    headerValueByName("X-Requester-Name") { requesterName =>
      Ok("Hi " + requesterName)
    }

There are other directives like `logRequestResult` that don't change the flow of the request. We can also create our own directives whenever needed.

### Building a response

If we use JSON marshaling, it is very easy to build a JSON response. All we need to do is to return marshalable type in `complete` directive (ex. `String`, `Future[T]`, `Option[T]`, `StatusCode`, etc.). Most of the HTTP status codes are already implemented in `akka-http`. Some of them are:

*   `Ok` — 200 response
*   `NotFound` — 404 response which is automatically generated when `None` is returned.
*   `Unauthorized` — 401 response
*   `Bad Request` — 400 response

Next: Testing `akka-http`.

## Tests

Check out [simple tests that we prepared](https://github.com/theiterators/akka-http-microservice/blob/master/src/test/scala/ServiceSpec.scala) and don't forget to run them on your computer (`sbt test`)! 

The interesting parts of the tests are:

*   the syntax of route checking,
*   [the way external requests are mocked](https://github.com/theiterators/akka-http-microservice/blob/master/src/test/scala/ServiceSpec.scala#L19).

Next: Tutorial summary.

## Summary

And that's it! We hope you enjoyed this tutorial and learned how to write a small microservice that uses `akka-http`, responds to `GET` and `POST` requests with JSON, and connects with external services through HTTP endpoint.

Be sure to ping us on [Github](https://github.com/theiterators/akka-http-microservice) or [Twitter](https://twitter.com/luksow) if you liked it or if you have any questions.