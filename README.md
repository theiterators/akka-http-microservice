# Akka HTTP microservice example

[![Join the chat at https://gitter.im/theiterators/akka-http-microservice](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/theiterators/akka-http-microservice?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

This project demonstrates the [Akka HTTP](http://doc.akka.io/docs/akka-stream-and-http-experimental/current/scala.html) library and Scala to write a simple REST (micro)service. The project shows the following tasks that are typical for most Akka HTTP-based projects:

* starting standalone HTTP server,
* handling file-based configuration,
* logging,
* routing,
* deconstructing requests,
* unmarshalling JSON entities to Scala's case classes,
* marshaling Scala's case classes to JSON responses,
* error handling,
* issuing requests to external services,
* testing with mocking of external services.

The service in the template provides two REST endpoints - one which gives GeoIP info for given IP and another for calculating geographical distance between given pair of IPs. The project uses the service [Telize](http://www.telize.com/) which offers JSON IP and GeoIP REST API for free.

[![Deploy to Heroku](https://www.herokucdn.com/deploy/button.png)](https://heroku.com/deploy)

## Usage

Start services with sbt:

```
$ sbt
> ~re-start
```

With the service up, you can start sending HTTP requests:

```
$ curl http://localhost:9000/ip/8.8.8.8
{
  "city": "Mountain View",
  "ip": "8.8.8.8",
  "latitude": 37.386,
  "country": "United States",
  "longitude": -122.0838
}
```

```
$ curl -X POST -H 'Content-Type: application/json' http://localhost:9000/ip -d '{"ip1": "8.8.8.8", "ip2": "8.8.4.4"}'
{
  "distance": 2201.448386715217,
  "ip1Info": {
    "city": "Mountain View",
    "ip": "8.8.8.8",
    "latitude": 37.386,
    "country": "United States",
    "longitude": -122.0838
  },
  "ip2Info": {
    "ip": "8.8.4.4",
    "country": "United States",
    "latitude": 38.0,
    "longitude": -97.0
  }
}
```

### Testing

Execute tests using `test` command:

```
$ sbt
> test
```

## Author & license

If you have any questions regarding this project contact:

Łukasz Sowa <lukasz@theiterators.com> from [Iterators](http://www.theiterators.com).

For licensing info see LICENSE file in project's root directory.
