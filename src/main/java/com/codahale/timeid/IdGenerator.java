/*
 * Copyright © 2019 Coda Hale (coda.hale@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.codahale.timeid;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.time.Clock;

/**
 * {@link IdGenerator} generates 27-character, time-ordered, k-sortable, URL-safe, globally unique
 * identifiers.
 *
 * <p>The identifiers are encoded with Radix-64, using an alphabet which is both URL-safe and which
 * preserves lexical ordering. Each ID consists of a 32-bit, big-endian timestamp (the number of
 * seconds since 1.4e9 seconds after the Unix epoch), plus 128 bits of random data.
 *
 * <p>Random data is produced via AES-256-CTR in a fast-key-erasure construction.
 */
public class IdGenerator implements Serializable {
  private static final char[] ALPHABET =
      "$0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz".toCharArray();
  private static final long serialVersionUID = 5133358267293287137L;
  private final SecureRandom random;
  private final Clock clock;
  private transient PRNG prng;

  /** Creates a new {@link IdGenerator}. */
  public IdGenerator() {
    this(new SecureRandom(), Clock.systemUTC());
  }

  IdGenerator(SecureRandom random, Clock clock) {
    this.random = random;
    this.clock = clock;
    checkState();
  }

  /**
   * Generates a new ID.
   *
   * @return a new 27-character ID
   */
  public synchronized String generate() {
    checkState();
    // Calculate the timestamp — number of seconds since 1.4e9 seconds past the Unix epoch.
    final int timestamp = (int) ((clock.millis() / 1000) - 1_400_000_000L);
    // Encode the timestamp as the first 4 big-endian bytes of the ID. The buffer is an extra byte
    // long to make it divisible by three, which simplifies the Radix-64 encoding.
    final byte[] id = ByteBuffer.allocate(21).putInt(timestamp).array();
    // Append 16 bytes of random data.
    prng.generate(id);
    // Encode the data with Radix-64.
    return encode(id);
  }

  private void checkState() {
    if (prng == null) {
      // Initialize the PRNG.
      this.prng = new PRNG(random);
    }
  }

  private static String encode(byte[] b) {
    // Encode a 21-byte array using Radix-64.
    final char[] out = new char[28];
    int idx = 0;
    // Split data into 24-bit blocks.
    for (int i = 0; i < b.length - 1; i += 3) {
      // Load 24-bit integer from big-endian data.
      final int v =
          (Byte.toUnsignedInt(b[i]) << 16)
              + (Byte.toUnsignedInt(b[i + 1]) << 8)
              + Byte.toUnsignedInt(b[i + 2]);
      // Encode the 24 bits over 4 characters.
      out[idx++] = ALPHABET[(v >> 18) & 63];
      out[idx++] = ALPHABET[(v >> 12) & 63];
      out[idx++] = ALPHABET[(v >> 6) & 63];
      out[idx++] = ALPHABET[v & 63];
    }

    // The underlying data is only 20 bytes, but that's padded out to 21 bytes to make the above
    // algorithm simpler. As a result, we can skip the 28th character, since the lower 6 bits of the
    // last chunk of input will always be zero. This also means the 27th character technically only
    // encodes 4 bits of information, not 6.
    return new String(out, 0, 27);
  }
}
