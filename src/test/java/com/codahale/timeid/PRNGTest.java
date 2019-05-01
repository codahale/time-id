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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PRNGTest {
  private final PRNG prng = new PRNG(new byte[32]);

  @Test
  void prng() {
    final byte[] buf = new byte[10_000];
    prng.generate(buf, 4, buf.length - 4);

    assertThat(buf[0]).isZero();
    assertThat(buf[1]).isZero();
    assertThat(buf[2]).isZero();
    assertThat(buf[3]).isZero();
  }
}
