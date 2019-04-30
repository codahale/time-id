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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class IdGeneratorTest {

  @Test
  void generating() {
    final SecureRandom random = mock(SecureRandom.class);
    doAnswer(
            invocation -> {
              final byte[] b = invocation.getArgument(0);
              for (int i = 0; i < b.length; i++) {
                b[i] = (byte) i;
              }
              return null;
            })
        .when(random)
        .nextBytes(any());
    final Clock clock = Clock.fixed(Instant.ofEpochMilli(1556474813000L), ZoneOffset.UTC);
    final IdGenerator generator = new IdGenerator(random, clock);

    assertThat(generator.generate()).isEqualTo("1K9SjKdi04R7yr1Lw1tKEF62dO7").hasSize(27);
  }
}
