# Akka HTTP microservice example

This project is intended to demonstrate how to use akka-http and Scala to write a simple REST (micro)service. Typical tasks accomplished in this project includes:

* starting standalone HTTP server,
* handling simple, file-based configuration,
* logging,
* routing,
* deconstructing requests,
* unmarshaling JSON entities to Scala's case classes,
* marshaling Scala's case classes to JSON responses,
* error handling,
* issuing requests to external services,
* testing with mocking of external services.

Service provides two endpoints - one which gives GeoIP info for given IP and another for calculating geographical distance between given pair of IPs. Project uses great service named [Telize](http://www.telize.com/) which offers JSON IP and GeoIP REST API for free.

## Usage

Start services with sbt:

```
$ sbt
> ~re-start
```

then you can start making requests:

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

Run tests with sbt:

```
$ sbt
> test
```

## Author & license

If you have any questions regarding this project contact with:

Łukasz Sowa <lukasz@theiterators.com> from [Iterators](http://www.theiterators.com).

For licensing info see LICENSE file in project's root directory.