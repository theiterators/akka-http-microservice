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

## Proposal

There are reported several methods for shorten urls. The basic ideas are:

* Unique sequence of numbers
* Function of the url

The sequence method generate a new shorten every time independent of the url
while the second allows to find the shortened given the url.
What is better depends on the objectives, for example the sequence method is more secure
because the users always get different shortened url but also is easier discover other shortened url
if the sequence has consecutive numbers.

### Encoding

Given the sequence we need to encode in a valid url path.
RFC 3986 allows [a-Z0-9_-], so, we have 64 characters for encoding.
Given than the shorted url can have a maximum of 8 character,
the numbers of available bits will depend on the encoded method. For example,
we can encode in base 64, and we have 64^8=2^48 combinations.
The problem of this solution is that the length is not minimal.

### Server level scalability

One server must be able to scalate until exhaust the machine resources.
The server is implemented with Akka Http without blocking thread in order to
warrant no waiting cpu and limited threads use.

Because there will be several thread computing the
sequence they have to synchronize to don't generate the same number. The server has an Akka actor
that is responsible to generate a crescent sequence very fast. This is the only sequential part of the project
and avoids for example a DB call that is more time costly.

For avoid repetition, the server reserve 2 block
of the sequence space is in use. Also, periodically save the last counter in use and when the server finishes.
If the finish mark is not available when the server start, a new block must be reserved. When the server finish is
first block, begin to use the second and ask another to Redis.

The used block are marked with a timestamp. When the configured time to live of the shortened expires, they are
liberated.
If the used block are exhausted, the server answer with unavailability of the service.

The shortened url -> original url relation is stored in Redis. It is a memory base DB, so the answer are fast.
Also, allow to create a log of the writing operations and recover from it when Redis is stopped,
allowing the persistence of the relation.

### Cluster level scalability

One machine can not have the resources for compute the many request that can have the service.
The solution is increase the number of servers. This can be possible using a redis server(cluster for resiliency)
that keeps the used blocks. A load balancer in front of WebServers is needed to distribute the requests.

Also, the Redis server associated with each server can also be escalated to cluster when needed.

### Encoding

The Encoding used is https://github.com/pico-works/pico-hashids.
It produces minimal shorted urls and has another goodies like personalization,
custom alphabet and unguessable. This has a cost in bits.

Available bits determined experimentally are 40. So, 8 bits are lost.
Each block is 16 bit. The others 24 will be used for the block identification.
If no block is lost, the system has 2^40 avalaible request, more or less 10^12.
Supposing 10^6 requests per day, the system can set a shortened live time of 27397 years.  
