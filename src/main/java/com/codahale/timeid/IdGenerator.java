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

import java.io.Externalizable;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;

/**
 * {@link IdGenerator} generates 27-character, time-ordered, k-sortable, URL-safe, globally unique
 * identifiers.
 *
 * <p>The identifiers are encoded with Radix-64, using an alphabet which is both URL-safe and which
 * preserves lexical ordering. Each ID consists of a 32-bit, big-endian timestamp (the number of
 * seconds since 1.4e9 seconds after the Unix epoch), plus 128 bits of random data.
 *
 * <p>Random data is produced via ChaCha20 in a fast-key-erasure construction.
 */
public class IdGenerator implements Externalizable {
  /** Lexically, the first possible ID. */
  @SuppressWarnings("WeakerAccess")
  public static final String MIN_VALUE = "$$$$$$$$$$$$$$$$$$$$$$$$$$$";
  /** Lexically, the last possible ID. */
  @SuppressWarnings("WeakerAccess")
  public static final String MAX_VALUE = "zzzzzzzzzzzzzzzzzzzzzzzzzzz";

  private static final char[] ALPHABET =
      "$0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz".toCharArray();
  private static final long EPOCH_OFFSET = 1_400_000_000L;
  private static final long serialVersionUID = 5133358267293287137L;
  private final Clock clock;
  private final byte[] id;
  private final char[] out;
  private final PRNG prng;

  /** Creates a new {@link IdGenerator}. */
  public IdGenerator() {
    this(new SecureRandom(), Clock.systemUTC());
  }

  IdGenerator(SecureRandom random, Clock clock) {
    this.clock = clock;
    // The buffer is an extra byte long to make it divisible by three, which simplifies the
    // Radix-64 encoding.
    this.id = new byte[21];
    // Similarly, this is 28 characters, despite the last character always being "zero".
    this.out = new char[28];
    this.prng = new PRNG(random);
  }

  /**
   * Generates a new ID.
   *
   * @return a new 27-character ID
   */
  public synchronized String generate() {
    // Calculate the timestamp — number of seconds since 1.4e9 seconds past the Unix epoch.
    final int timestamp = (int) ((clock.millis() / 1000) - EPOCH_OFFSET);
    // Encode the timestamp as the first 4 big-endian bytes of the ID.
    // post-Java 9, this is replaceable with a VarHandle for a minor performance boost
    id[0] = (byte) (timestamp >> 24);
    id[1] = (byte) (timestamp >> 16);
    id[2] = (byte) (timestamp >> 8);
    id[3] = (byte) timestamp;
    // Append 16 bytes of random data.
    prng.append(id);
    // Encode the data with Radix-64.
    return encode(id);
  }

  /**
   * Returns the {@link Instant} at which the ID was created.
   *
   * @param id a time ID
   * @return the ID's timestamp
   */
  public static Instant createdAt(String id) {
    final int timestamp =
        (Arrays.binarySearch(ALPHABET, id.charAt(0)) << 26)
            + (Arrays.binarySearch(ALPHABET, id.charAt(1)) << 20)
            + (Arrays.binarySearch(ALPHABET, id.charAt(2)) << 14)
            + (Arrays.binarySearch(ALPHABET, id.charAt(3)) << 8)
            + (Arrays.binarySearch(ALPHABET, id.charAt(4)) << 2)
            + (Arrays.binarySearch(ALPHABET, id.charAt(5)) >> 4);
    return Instant.ofEpochMilli((timestamp + EPOCH_OFFSET) * 1000);
  }

  private String encode(byte[] b) {
    // Encode a 21-byte array using Radix-64.
    // Split data into 24-bit blocks.
    for (int i = 0, j = 0; i < b.length - 1; ) {
      // Load 24-bit integer from big-endian data.
      final int v =
          (Byte.toUnsignedInt(b[i++]) << 16)
              | (Byte.toUnsignedInt(b[i++]) << 8)
              | Byte.toUnsignedInt(b[i++]);
      // Encode the 24 bits over 4 characters.
      out[j++] = ALPHABET[(v >> 18) & 63];
      out[j++] = ALPHABET[(v >> 12) & 63];
      out[j++] = ALPHABET[(v >> 6) & 63];
      out[j++] = ALPHABET[v & 63];
    }

    // The underlying data is only 20 bytes, but that's padded out to 21 bytes to make the above
    // algorithm simpler. As a result, we can skip the 28th character, since the lower 6 bits of the
    // last chunk of input will always be zero. This also means the 27th character technically only
    // encodes 4 bits of information, not 6.
    return new String(out, 0, 27);
  }

  @Override
  public void writeExternal(ObjectOutput out) {}

  @Override
  public void readExternal(ObjectInput in) {}
}
