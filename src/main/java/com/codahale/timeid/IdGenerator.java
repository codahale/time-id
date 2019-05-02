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
    final byte[] id = ByteBuffer.allocate(20).putInt(timestamp).array();
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

  @SuppressWarnings("Duplicates")
  private static String encode(byte[] b) {
    // Encode a 20-byte array using Radix-64.
    final char[] out = new char[27];

    // Split data into seven 24-bit blocks and encode them as seven 32-bit blocks.
    // This loop is unrolled as a performance optimization.

    // Block 0
    int v =
        (Byte.toUnsignedInt(b[0]) << 16)
            + (Byte.toUnsignedInt(b[1]) << 8)
            + Byte.toUnsignedInt(b[2]);
    // Encode the 24 bits over 4 characters.
    out[0] = ALPHABET[(v >> 18) & 63];
    out[1] = ALPHABET[(v >> 12) & 63];
    out[2] = ALPHABET[(v >> 6) & 63];
    out[3] = ALPHABET[v & 63];

    // Block 1
    v =
        (Byte.toUnsignedInt(b[3]) << 16)
            + (Byte.toUnsignedInt(b[4]) << 8)
            + Byte.toUnsignedInt(b[5]);
    out[4] = ALPHABET[(v >> 18) & 63];
    out[5] = ALPHABET[(v >> 12) & 63];
    out[6] = ALPHABET[(v >> 6) & 63];
    out[7] = ALPHABET[v & 63];

    // Block 2
    v =
        (Byte.toUnsignedInt(b[6]) << 16)
            + (Byte.toUnsignedInt(b[7]) << 8)
            + Byte.toUnsignedInt(b[8]);
    out[8] = ALPHABET[(v >> 18) & 63];
    out[9] = ALPHABET[(v >> 12) & 63];
    out[10] = ALPHABET[(v >> 6) & 63];
    out[11] = ALPHABET[v & 63];

    // Block 3
    v =
        (Byte.toUnsignedInt(b[9]) << 16)
            + (Byte.toUnsignedInt(b[10]) << 8)
            + Byte.toUnsignedInt(b[11]);
    out[12] = ALPHABET[(v >> 18) & 63];
    out[13] = ALPHABET[(v >> 12) & 63];
    out[14] = ALPHABET[(v >> 6) & 63];
    out[15] = ALPHABET[v & 63];

    // Block 4
    v =
        (Byte.toUnsignedInt(b[12]) << 16)
            + (Byte.toUnsignedInt(b[13]) << 8)
            + Byte.toUnsignedInt(b[14]);
    out[16] = ALPHABET[(v >> 18) & 63];
    out[17] = ALPHABET[(v >> 12) & 63];
    out[18] = ALPHABET[(v >> 6) & 63];
    out[19] = ALPHABET[v & 63];

    // Block 5
    v =
        (Byte.toUnsignedInt(b[15]) << 16)
            + (Byte.toUnsignedInt(b[16]) << 8)
            + Byte.toUnsignedInt(b[17]);
    out[20] = ALPHABET[(v >> 18) & 63];
    out[21] = ALPHABET[(v >> 12) & 63];
    out[22] = ALPHABET[(v >> 6) & 63];
    out[23] = ALPHABET[v & 63];

    // Block 6
    // Last block is only 16 bits of input, 24 bits of output.
    v = (Byte.toUnsignedInt(b[18]) << 16) + (Byte.toUnsignedInt(b[19]) << 8);
    out[24] = ALPHABET[(v >> 18) & 63];
    out[25] = ALPHABET[(v >> 12) & 63];
    out[26] = ALPHABET[(v >> 6) & 63];

    return new String(out);
  }
}
