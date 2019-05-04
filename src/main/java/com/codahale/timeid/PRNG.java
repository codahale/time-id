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

import java.nio.ByteBuffer;
import java.security.SecureRandom;

/**
 * PRNG generates pseudo-random data using ChaCha20 in a fast-key-erasure construction. For each ID,
 * it runs a ChaCha20 block transform using a 256-bit key, zero counter, and zero nonce. The first
 * 256 bits of the 512-bit results are used as the new key; the next 128 bits are used for the ID;
 * the remaining state is discarded.
 *
 * @see <a href="https://blog.cr.yp.to/20170723-random.html">Fast-key-erasure random-number
 *     generators</a>
 */
class PRNG {
  private static final int SIGMA0 = 0x61707865;
  private static final int SIGMA1 = 0x3320646e;
  private static final int SIGMA2 = 0x79622d32;
  private static final int SIGMA3 = 0x6b206574;
  private int k0, k1, k2, k3, k4, k5, k6, k7;

  PRNG(SecureRandom random) {
    // Pick a random 256-bit key for ChaCha20.
    this.k0 = random.nextInt();
    this.k1 = random.nextInt();
    this.k2 = random.nextInt();
    this.k3 = random.nextInt();
    this.k4 = random.nextInt();
    this.k5 = random.nextInt();
    this.k6 = random.nextInt();
    this.k7 = random.nextInt();
  }

  @SuppressWarnings("Duplicates")
  void append(ByteBuffer b) {
    // Initialize the block transform's state.
    int x00 = SIGMA0;
    int x01 = SIGMA1;
    int x02 = SIGMA2;
    int x03 = SIGMA3;
    int x04 = k0;
    int x05 = k1;
    int x06 = k2;
    int x07 = k3;
    int x08 = k4;
    int x09 = k5;
    int x10 = k6;
    int x11 = k7;
    int x12 = 0; // always use a zero counter
    int x13 = 0; // always use a zero nonce
    int x14 = 0;
    int x15 = 0;

    // Perform 10 double rounds.
    for (int round = 0; round < 10; round++) {
      x00 += x04;
      x12 = Integer.rotateLeft(x12 ^ x00, 16);

      x08 += x12;
      x04 = Integer.rotateLeft(x04 ^ x08, 12);

      x00 += x04;
      x12 = Integer.rotateLeft(x12 ^ x00, 8);

      x08 += x12;
      x04 = Integer.rotateLeft(x04 ^ x08, 7);

      x01 += x05;
      x13 = Integer.rotateLeft(x13 ^ x01, 16);

      x09 += x13;
      x05 = Integer.rotateLeft(x05 ^ x09, 12);

      x01 += x05;
      x13 = Integer.rotateLeft(x13 ^ x01, 8);

      x09 += x13;
      x05 = Integer.rotateLeft(x05 ^ x09, 7);

      x02 += x06;
      x14 = Integer.rotateLeft(x14 ^ x02, 16);

      x10 += x14;
      x06 = Integer.rotateLeft(x06 ^ x10, 12);

      x02 += x06;
      x14 = Integer.rotateLeft(x14 ^ x02, 8);

      x10 += x14;
      x06 = Integer.rotateLeft(x06 ^ x10, 7);

      x03 += x07;
      x15 = Integer.rotateLeft(x15 ^ x03, 16);

      x11 += x15;
      x07 = Integer.rotateLeft(x07 ^ x11, 12);

      x03 += x07;
      x15 = Integer.rotateLeft(x15 ^ x03, 8);

      x11 += x15;
      x07 = Integer.rotateLeft(x07 ^ x11, 7);

      x00 += x05;
      x15 = Integer.rotateLeft(x15 ^ x00, 16);

      x10 += x15;
      x05 = Integer.rotateLeft(x05 ^ x10, 12);

      x00 += x05;
      x15 = Integer.rotateLeft(x15 ^ x00, 8);

      x10 += x15;
      x05 = Integer.rotateLeft(x05 ^ x10, 7);

      x01 += x06;
      x12 = Integer.rotateLeft(x12 ^ x01, 16);

      x11 += x12;
      x06 = Integer.rotateLeft(x06 ^ x11, 12);

      x01 += x06;
      x12 = Integer.rotateLeft(x12 ^ x01, 8);

      x11 += x12;
      x06 = Integer.rotateLeft(x06 ^ x11, 7);

      x02 += x07;
      x13 = Integer.rotateLeft(x13 ^ x02, 16);

      x08 += x13;
      x07 = Integer.rotateLeft(x07 ^ x08, 12);

      x02 += x07;
      x13 = Integer.rotateLeft(x13 ^ x02, 8);

      x08 += x13;
      x07 = Integer.rotateLeft(x07 ^ x08, 7);

      x03 += x04;
      x14 = Integer.rotateLeft(x14 ^ x03, 16);

      x09 += x14;
      x04 = Integer.rotateLeft(x04 ^ x09, 12);

      x03 += x04;
      x14 = Integer.rotateLeft(x14 ^ x03, 8);

      x09 += x14;
      x04 = Integer.rotateLeft(x04 ^ x09, 7);
    }

    // Use words 8-11 as the output.
    b.putInt(x08 + k4);
    b.putInt(x09 + k5);
    b.putInt(x10 + k6);
    b.putInt(x11 + k7);

    // Use words 0-7 as the new key. This is out-of-order to allow for in-place modification.
    this.k4 = x04 + k0;
    this.k5 = x05 + k1;
    this.k6 = x06 + k2;
    this.k7 = x07 + k3;
    this.k0 = x00 + SIGMA0;
    this.k1 = x01 + SIGMA1;
    this.k2 = x02 + SIGMA2;
    this.k3 = x03 + SIGMA3;

    // Discard words 12-15.
  }
}
