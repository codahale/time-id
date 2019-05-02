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

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/** PRNG generates pseudo-random data using AES-256-CTR in a fast-key-erasure construction. */
class PRNG {
  static final int KEY_LEN = 32;
  private static final int BLOCK_SIZE = 16;
  private static final IvParameterSpec IV = new IvParameterSpec(new byte[BLOCK_SIZE]);
  private static final int MAX_BLOCKS = 256;
  private static final int BUF_SIZE = BLOCK_SIZE * MAX_BLOCKS;
  private final Cipher aes;
  private final byte[] buffer;
  private int offset;

  PRNG(byte[] key) {
    this.buffer = new byte[BUF_SIZE];
    try {
      // Initialize the cipher using the initial key.
      this.aes = Cipher.getInstance("AES/CTR/NoPadding");
      aes.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), IV);
      // Prefill the buffer.
      cycle();
    } catch (NoSuchAlgorithmException
        | NoSuchPaddingException
        | InvalidKeyException
        | InvalidAlgorithmParameterException e) {
      throw new UnsupportedOperationException(e);
    }
  }

  void generate(byte[] out) {
    // Refill the buffer, if needed.
    if (offset == BUF_SIZE) {
      cycle();
    }

    // Copy a block from the buffer into the output.
    System.arraycopy(buffer, offset, out, 4, BLOCK_SIZE);
    // Zero out the used block.
    Arrays.fill(buffer, offset, offset + BLOCK_SIZE, (byte) 0);
    // Increment the offset.
    offset += BLOCK_SIZE;
  }

  private void cycle() {
    try {
      // Use the current key to encrypt a number of zero blocks.
      aes.update(buffer, 0, BUF_SIZE, buffer, 0);
      // Use the first two encrypted blocks as the new key.
      aes.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(buffer, 0, KEY_LEN, "AES"), IV);
      // Don't use the key as output.
      offset = KEY_LEN;
      // Zero out the key.
      Arrays.fill(buffer, 0, KEY_LEN, (byte) 0);
    } catch (ShortBufferException | InvalidKeyException | InvalidAlgorithmParameterException e) {
      throw new IllegalStateException(e);
    }
  }
}
