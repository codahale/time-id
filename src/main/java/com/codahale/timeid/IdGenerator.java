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
 * {@link IdGenerator} generates 26-character, time-ordered, k-sortable, URL-safe, globally unique
 * identifiers.
 *
 * <p>The identifiers are encoded with Radix-64, using an alphabet which is both URL-safe and which
 * preserves lexical ordering. Each ID consists of a 32-bit, big-endian timestamp (the number of
 * seconds since 1.4e9 seconds after the Unix epoch), plus 128 bits of random data.
 */
public class IdGenerator implements Serializable {
  private static final char[] ALPHABET =
      "$0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz".toCharArray();
  private static final long serialVersionUID = 5133358267293287137L;
  private final SecureRandom random;
  private final Clock clock;

  /** Creates a new {@link IdGenerator}. */
  public IdGenerator() {
    this(new SecureRandom(), Clock.systemUTC());
  }

  IdGenerator(SecureRandom random, Clock clock) {
    this.random = random;
    this.clock = clock;
  }

  /**
   * Generates a new ID.
   *
   * @return a new 26-character ID
   */
  public String generate() {
    final int timestamp = (int) ((clock.millis() / 1000) - 1_400_000_000L);
    final byte[] rand = new byte[16];
    random.nextBytes(rand);
    final byte[] data = ByteBuffer.allocate(20).putInt(timestamp).put(rand).array();
    return encode(data);
  }

  private static String encode(byte[] b) {
    final char[] out = new char[26];
    int idx = 0;
    for (int i = 0; i < 18; i += 3) {
      final int v = (b[i] << 16) + (b[i + 1] << 8) + (b[i + 2]);
      out[idx++] = ALPHABET[(v >> 18) & 63];
      out[idx++] = ALPHABET[(v >> 12) & 63];
      out[idx++] = ALPHABET[(v >> 6) & 63];
      out[idx++] = ALPHABET[v & 63];
    }
    final int v = b[19] << 16;
    out[idx++] = ALPHABET[(v >> 18) & 63];
    out[idx] = ALPHABET[(v >> 12) & 63];
    return new String(out);
  }
}
