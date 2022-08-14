# Akka HTTP url shortener

## Objetive

Design and implement a URL shortener HTTP service that fulfills the following criteria:

* Provides an HTTP API to:
  * Shorten a URL
  * Redirect to the long URL from the shortened URL
* Shortened URL requirements:
  * The ID of the shortened URL needs to be unique (across past and concurrent requests)
  * The ID of the shortened URL should be as short as possible (max. 8 characters long)
  * The long/shortened URL mapping needs to be persisted and shouldn't be lost after a backend service restart

## Propousal

There are reported several methods for shorten urls. The basic ideas are:

* Generate unique sequence of numbers
* Generate a unique identifier based on the url
  The first method generate a new shorten every time independent of the 