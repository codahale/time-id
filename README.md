# time-id

[![CircleCI](https://circleci.com/gh/codahale/time-id.svg?style=svg)](https://circleci.com/gh/codahale/time-id)

Generates 26-character, time-ordered, k-sortable, URL-safe, globally unique identifiers.

## Add to your project

```xml
<dependency>
  <groupId>com.codahale</groupId>
  <artifactId>time-id</artifactId>
  <version>0.1.0</version>
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
1KJxM808$xmO4aiW$47L3GmXiV
1KJxMAwxkcF_QFw_SoUXA5qdqV
1KJxMCcNN_rv27PHbyZOq3GlPV
1KJxMDkvVuOrKoQZZen9a5LF0k
```

etc. etc.

## How it works

The identifiers are encoded with Radix-64 using an alphabet which is both URL-safe and which
preserves lexical ordering. Each ID consists of a 32-bit, big-endian timestamp (the number of
seconds since 1.4e9 seconds after the Unix epoch), plus 128 bits of random data.

(Technically, it's 118 bits of random data.)

## Is it fast?

```
Benchmark            Mode  Cnt    Score    Error  Units
Benchmarks.generate  avgt    5  918.981 ± 30.654  ns/op
```

It's pretty fast. About 90% of that time is spent generating the random data.

## License

Copyright © 2019 Coda Hale

Distributed under the Apache License 2.0.
