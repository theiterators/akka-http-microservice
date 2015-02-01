# Akka HTTP microservice example

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

## Usage

Start services using `~ reStart` command:

    $ ./activator
    [akka-http-microservice]> ~ reStart

With the service up, you can start sending HTTP requests:

    $ http http://localhost:9000/ip/8.8.8.8
    HTTP/1.1 200 OK
    Content-Length: 126
    Content-Type: application/json; charset=UTF-8
    Date: Sun, 01 Feb 2015 09:28:08 GMT
    Server: akka-http/2.3.9
    
    {
        "city": "Mountain View",
        "country": "United States",
        "ip": "8.8.8.8",
        "latitude": 37.386,
        "longitude": -122.0838
    }

    $ http http://localhost:9000/ip ip1=8.8.8.8 ip2=8.8.4.4
    HTTP/1.1 200 OK
    Content-Length: 306
    Content-Type: application/json; charset=UTF-8
    Date: Sun, 01 Feb 2015 09:29:55 GMT
    Server: akka-http/2.3.9
    
    {
        "distance": 2201.448386715217,
        "ip1Info": {
            "city": "Mountain View",
            "country": "United States",
            "ip": "8.8.8.8",
            "latitude": 37.386,
            "longitude": -122.0838
        },
        "ip2Info": {
            "country": "United States",
            "ip": "8.8.4.4",
            "latitude": 38.0,
            "longitude": -97.0
        }
    }

### Testing

Execute tests using `test` command:

    $ ./activator
    > test

## Author & license

If you have any questions regarding this project contact:

Łukasz Sowa <lukasz@theiterators.com> from [Iterators](http://www.theiterators.com).

For licensing info see LICENSE file in project's root directory.