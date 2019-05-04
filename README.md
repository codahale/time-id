# time-id

[![CircleCI](https://circleci.com/gh/codahale/time-id.svg?style=svg)](https://circleci.com/gh/codahale/time-id)

Generates 27-character, time-ordered, k-sortable, URL-safe, globally unique identifiers.

## Add to your project

```xml
<dependency>
  <groupId>com.codahale</groupId>
  <artifactId>time-id</artifactId>
  <version>0.4.1</version>
</dependency>
```

*Note: module name for Java 9+ is `com.codahale.timeid`.*

## Use the thing

```java
import com.codahale.timeid.IdGenerator;

class Example {
  void doIt() {
    final IdGenerator generator = new IdGenerator();
    for (int i = 0; i < 100; i++) {
      System.out.println(generator.generate()); 
    }
  } 
}
```

``` 
1KKK6IIJF7TqCnck5XAgqi$UMVR
1KKK6HdQPiMRn6no$9feUO5_99w
1KKK6Kan5ELIxd$Uc82dTmXk2rc
1KKK6Del_PkT8kr9y5K2yKbs1IV
```

etc. etc.

## How does it work?

Each ID consists of a 32-bit, big-endian timestamp (the number of seconds since 1.4e9 seconds after
the Unix epoch), plus 128 bits of random data, for a total of 160 bits of information.

The random data is produced using ChaCha20 in a
[fast-key-erasure](https://blog.cr.yp.to/20170723-random.html) construction, with a per-ID iteration
of the ChaCha20 block transform. The first 256 bits of the result are used as the key for the next
ID; the next 128 bits are used in the ID; the remaining state is discarded. This construction is
an order of magnitude faster than the fastest `java.util.SecureRandom` implementation, is
nonblocking, has a very small memory footprint, operates in constant time, offers forward secrecy,
requires no hardware support, and has performance characteristics independent of JVM configuration.

The timestamp and the random data are encoded with Radix-64 using an alphabet which is both URL-safe
and which preserves lexical ordering. The result is a 27-character, URL-safe string which can be
used in systems which are unaware of its internal structure (e.g., databases, file systems) to store
time-ordered data with unique IDs.

## Is it fast?

### Latency

```
Result "com.codahale.timeid.benchmarks.Benchmarks.generate":
  N = 8247423
  mean =    287.138 ±(99.9%) 2.628 ns/op

  Histogram, ns/op:
    [      0.000,  250000.000) = 8247404 
    [ 250000.000,  500000.000) = 2 
    [ 500000.000,  750000.000) = 1 
    [ 750000.000, 1000000.000) = 13 
    [1000000.000, 1250000.000) = 1 
    [1250000.000, 1500000.000) = 0 
    [1500000.000, 1750000.000) = 0 
    [1750000.000, 2000000.000) = 0 
    [2000000.000, 2250000.000) = 0 
    [2250000.000, 2500000.000) = 0 
    [2500000.000, 2750000.000) = 0 
    [2750000.000, 3000000.000) = 0 
    [3000000.000, 3250000.000) = 0 
    [3250000.000, 3500000.000) = 0 
    [3500000.000, 3750000.000) = 1 

  Percentiles, ns/op:
      p(0.0000) =    231.000 ns/op
     p(50.0000) =    260.000 ns/op
     p(90.0000) =    276.000 ns/op
     p(95.0000) =    284.000 ns/op
     p(99.0000) =    431.000 ns/op
     p(99.9000) =   7040.000 ns/op
     p(99.9900) =  28800.000 ns/op
     p(99.9990) =  68169.892 ns/op
     p(99.9999) = 895598.789 ns/op
    p(100.0000) = 3801088.000 ns/op
```

### Throughput

```
Result "com.codahale.timeid.benchmarks.Benchmarks.generate":
  4248401.562 ±(99.9%) 299471.264 ops/s [Average]
  (min, avg, max) = (2331661.139, 4248401.562, 4358395.064), stdev = 399785.569
  CI (99.9%): [3948930.299, 4547872.826] (assumes normal distribution)
```

It's pretty fast.

## License

Copyright © 2019 Coda Hale

Distributed under the Apache License 2.0.
