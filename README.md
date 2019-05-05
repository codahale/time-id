# time-id

[![CircleCI](https://circleci.com/gh/codahale/time-id.svg?style=svg)](https://circleci.com/gh/codahale/time-id)

Generates 27-character, time-ordered, k-sortable, URL-safe, globally unique identifiers.

## Add to your project

```xml
<dependency>
  <groupId>com.codahale</groupId>
  <artifactId>time-id</artifactId>
  <version>0.4.3</version>
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
1KnoOa5ebDQiUzMzhS_nhAOlDSB
1KnoOijrCjrXcDxRUxLE472qyv$
1KnoOX8k1FgWLo5Nbsk6Oyu00tg
1KnoOdLAQxAaOI9pLgcgiruVe$J
1KnoObqa4KtsvlsXuDOJevVfhmF
1KnoOjArbhB1tnOwSDUwN0nMULB
1KnoOjy$qAIUEyWphJzZZzbGCTR
1KnoOVQ6veXU8PF8HhbqXnc7JwF
1KnoOa5bOZVWkmwbze7ceKk_pnw
1KnoOjpZ1HCv0QqHUE2Em3U7C8k
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

``` 
Benchmark                                           Mode      Cnt        Score    Error   Units
Benchmarks.generate                               sample  7041460      270.418 ±  2.743   ns/op
Benchmarks.generate:generate·p0.00                sample               211.000            ns/op
Benchmarks.generate:generate·p0.50                sample               246.000            ns/op
Benchmarks.generate:generate·p0.90                sample               259.000            ns/op
Benchmarks.generate:generate·p0.95                sample               264.000            ns/op
Benchmarks.generate:generate·p0.99                sample               405.000            ns/op
Benchmarks.generate:generate·p0.999               sample              3516.000            ns/op
Benchmarks.generate:generate·p0.9999              sample             25472.000            ns/op
Benchmarks.generate:generate·p1.00                sample           2912256.000            ns/op
Benchmarks.generate:·gc.alloc.rate                sample       20      293.922 ±  4.292  MB/sec
Benchmarks.generate:·gc.alloc.rate.norm           sample       20       72.007 ±  0.001    B/op
Benchmarks.generate:·gc.churn.G1_Eden_Space       sample       20      293.985 ±  8.721  MB/sec
Benchmarks.generate:·gc.churn.G1_Eden_Space.norm  sample       20       72.017 ±  1.684    B/op
Benchmarks.generate:·gc.churn.G1_Old_Gen          sample       20        0.001 ±  0.001  MB/sec
Benchmarks.generate:·gc.churn.G1_Old_Gen.norm     sample       20       ≈ 10⁻⁴             B/op
Benchmarks.generate:·gc.count                     sample       20      334.000           counts
Benchmarks.generate:·gc.time                      sample       20      208.000               ms
```

It's pretty fast. Has a nicely flat latency profile. Doesn't generate a lot of garbage, either.
Mostly just the `String` allocation. All the internal state is either reused or stack-allocated.

## License

Copyright © 2019 Coda Hale

Distributed under the Apache License 2.0.
