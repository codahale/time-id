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
import java.util.Arrays;

/** PRNG generates pseudo-random data using ChaCha20 in a fast-key-erasure construction. */
class PRNG {
  private static final int BLOCK_SIZE = 16; // ChaCha20 uses 512-bit blocks
  private static final int MAX_BLOCKS = 64; // buffer 4K of output
  private static final int SIGMA0 = 0x61707865;
  private static final int SIGMA1 = 0x3320646e;
  private static final int SIGMA2 = 0x79622d32;
  private static final int SIGMA3 = 0x6b206574;
  private final int[] key;
  private final int[] buffer;
  private int offset;

  PRNG(SecureRandom random) {
    // Pick a random 256-bit key for ChaCha20.
    this.key =
        new int[] {
          random.nextInt(),
          random.nextInt(),
          random.nextInt(),
          random.nextInt(),
          random.nextInt(),
          random.nextInt(),
          random.nextInt(),
          random.nextInt(),
        };

    // Allocate a keystream buffer.
    this.buffer = new int[BLOCK_SIZE * MAX_BLOCKS];

    // Fill the keystream buffer and rekey.
    cycle();
  }

  void append(ByteBuffer b) {
    // Refill the buffer, if needed.
    if (offset == buffer.length) {
      cycle();
    }

    // Add 128 bits of data to the ID and zero out the used data.
    for (int i = 0; i < 4; i++) {
      final int v = buffer[offset];
      buffer[offset++] = 0;
      b.putInt(v);
    }
  }

  private void cycle() {
    // Generate N blocks of ChaCha20 output.
    for (int i = 0; i < MAX_BLOCKS; i++) {
      chacha20(key, i, buffer, i * BLOCK_SIZE);
    }

    // Use the first 256 bits as the new key.
    System.arraycopy(buffer, 0, key, 0, 8);

    // Zero out the key in the buffer..
    Arrays.fill(buffer, 0, 8, 0);

    // Skip the key in the buffer.
    offset = 8;
  }

  @SuppressWarnings("Duplicates")
  private static void chacha20(int[] key, int counter, int[] out, int outPos) {
    // Initialize the block transform's state.
    int x00 = SIGMA0;
    int x01 = SIGMA1;
    int x02 = SIGMA2;
    int x03 = SIGMA3;
    int x04 = key[0];
    int x05 = key[1];
    int x06 = key[2];
    int x07 = key[3];
    int x08 = key[4];
    int x09 = key[5];
    int x10 = key[6];
    int x11 = key[7];
    int x12 = counter;
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

    // Write output.
    out[outPos++] = x00 + SIGMA0;
    out[outPos++] = x01 + SIGMA1;
    out[outPos++] = x02 + SIGMA2;
    out[outPos++] = x03 + SIGMA3;
    out[outPos++] = x04 + key[0];
    out[outPos++] = x05 + key[1];
    out[outPos++] = x06 + key[2];
    out[outPos++] = x07 + key[3];
    out[outPos++] = x08 + key[4];
    out[outPos++] = x09 + key[5];
    out[outPos++] = x10 + key[6];
    out[outPos++] = x11 + key[7];
    out[outPos++] = x12 + counter;
    out[outPos++] = x13; // always use a zero nonce
    out[outPos++] = x14;
    out[outPos] = x15;
  }
}
