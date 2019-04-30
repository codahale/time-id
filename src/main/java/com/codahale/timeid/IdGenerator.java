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
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * {@link IdGenerator} generates 27-character, time-ordered, k-sortable, URL-safe, globally unique
 * identifiers.
 *
 * <p>The identifiers are encoded with Radix-64, using an alphabet which is both URL-safe and which
 * preserves lexical ordering. Each ID consists of a 32-bit, big-endian timestamp (the number of
 * seconds since 1.4e9 seconds after the Unix epoch), plus 128 bits of random data.
 *
 * <p>Random data is produced via AES-256-CTR output using a random key and initial counter state.
 */
public class IdGenerator implements Serializable {
  private static final char[] ALPHABET =
      "$0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz".toCharArray();
  private static final byte[] ZEROS = new byte[16];
  private static final long serialVersionUID = 5133358267293287137L;
  private final SecureRandom random;
  private final Clock clock;
  private transient Cipher aes;

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
   * @return a new 27-character ID
   */
  public String generate() {
    final int timestamp = (int) ((clock.millis() / 1000) - 1_400_000_000L);
    final byte[] id = ByteBuffer.allocate(21).putInt(timestamp).array();
    generate(id);
    return encode(id);
  }

  /**
   * Reseeds the generator with random data, resetting its internal state.
   */
  public void reseed() {
    final byte[] key = new byte[32];
    final byte[] iv = new byte[16];
    random.nextBytes(key);
    random.nextBytes(iv);
    try {
      aes.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
    } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
      throw new IllegalStateException(e);
    }
  }

  private void checkState() {
    if (aes != null) {
      return;
    }

    try {
      this.aes = Cipher.getInstance("AES/CTR/NoPadding");
    } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
      throw new UnsupportedOperationException(e);
    }
    reseed();
  }

  private void generate(byte[] out) {
    checkState();
    try {
      aes.update(ZEROS, 0, 16, out, 4);
    } catch (ShortBufferException e) {
      throw new IllegalStateException(e);
    }
  }

  private static String encode(byte[] b) {
    final char[] out = new char[28];
    int idx = 0;
    for (int i = 0; i < b.length - 1; i += 3) {
      int v = (b[i] << 16) + (b[i + 1] << 8) + (b[i + 2]);
      out[idx++] = ALPHABET[(v >> 18) & 63];
      out[idx++] = ALPHABET[(v >> 12) & 63];
      out[idx++] = ALPHABET[(v >> 6) & 63];
      out[idx++] = ALPHABET[v & 63];
    }
    return new String(out, 0, 27);
  }
}
