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
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/** PRNG generates pseudo-random data using AES-256-CTR in a fast-key-erasure construction. */
class PRNG {
  private static final int BLOCK_SIZE = 16;
  private static final IvParameterSpec IV = new IvParameterSpec(new byte[BLOCK_SIZE]);
  private static final int MAX_BLOCKS = 256;
  private static final int BUF_SIZE = BLOCK_SIZE * MAX_BLOCKS;
  private static final byte[] ZEROS = new byte[BUF_SIZE];
  static final int KEY_LEN = 32;
  private final Cipher aes;
  private final byte[] buffer;
  private int offset;

  PRNG(byte[] key) {
    this.buffer = new byte[BUF_SIZE];
    try {
      // Initialize the cipher using the initial key.
      this.aes = Cipher.getInstance("AES/CTR/NoPadding");
      aes.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), IV);
      cycle();
    } catch (NoSuchAlgorithmException
        | NoSuchPaddingException
        | InvalidKeyException
        | InvalidAlgorithmParameterException e) {
      throw new UnsupportedOperationException(e);
    }
  }

  void generate(byte[] out) {
    if (offset == BUF_SIZE) {
      cycle();
    }
    System.arraycopy(buffer, offset, out, 4, BLOCK_SIZE);
    offset += BLOCK_SIZE;
  }

  private void cycle() {
    try {
      // Use the current key to encrypt a number of zero blocks.
      aes.update(ZEROS, 0, BUF_SIZE, buffer, 0);
      // Use the first two encrypted blocks as the new key.
      aes.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(buffer, 0, KEY_LEN, "AES"), IV);
      // Don't use the key as output.
      offset = KEY_LEN;
    } catch (ShortBufferException | InvalidKeyException | InvalidAlgorithmParameterException e) {
      throw new IllegalStateException(e);
    }
  }
}
