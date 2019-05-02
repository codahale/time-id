# time-id

[![CircleCI](https://circleci.com/gh/codahale/time-id.svg?style=svg)](https://circleci.com/gh/codahale/time-id)

Generates 27-character, time-ordered, k-sortable, URL-safe, globally unique identifiers.

## Add to your project

```xml
<dependency>
  <groupId>com.codahale</groupId>
  <artifactId>time-id</artifactId>
  <version>0.3.0</version>
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

The identifiers are encoded with Radix-64 using an alphabet which is both URL-safe and which
preserves lexical ordering. Each ID consists of a 32-bit, big-endian timestamp (the number of
seconds since 1.4e9 seconds after the Unix epoch), plus 128 bits of random data.

The random data is produced using AES-256-CTR in a
[fast-key-erasure](https://blog.cr.yp.to/20170723-random.html) construction for performance reasons.

The result is a 27-character, URL-safe string which can be used in systems which are unaware of its
internal structure (e.g., databases, file systems) to store time-ordered data with unique IDs.

## Is it fast?

```
Benchmark            Mode  Cnt   Score   Error  Units
Benchmarks.generate  avgt    5  95.932 ± 5.546  ns/op
```

It's pretty fast.

## License

Copyright © 2019 Coda Hale

Distributed under the Apache License 2.0.
