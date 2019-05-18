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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class IdGeneratorTest {

  @Test
  void generating() {
    final SecureRandom random = mock(SecureRandom.class);
    when(random.nextInt()).thenReturn(1, 2, 3, 4, 5, 6, 7, 8);
    final Clock clock = new FakeClock(1556474813000L);

    final IdGenerator generator = new IdGenerator(random, clock);

    assertThat(generator.generate()).isEqualTo("1KDSjKyxshmGcaS2CWctXqY0wEB").hasSize(27);
    for (int i = 0; i < 100_000; i++) {
      assertThat(generator.generate()).hasSize(27);
    }
    assertThat(generator.generate()).isEqualTo("1KDT7Ov7ZJ4BKneIK5PrtYkWQUs").hasSize(27);
  }

  @Test
  void serializing() throws IOException, ClassNotFoundException {
    final IdGenerator genOut = new IdGenerator();
    final ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
    try (ObjectOutputStream out = new ObjectOutputStream(bytesOut)) {
      out.writeObject(genOut);
    }

    final ByteArrayInputStream bytesIn = new ByteArrayInputStream(bytesOut.toByteArray());
    try (ObjectInputStream in = new ObjectInputStream(bytesIn)) {
      final IdGenerator genIn = (IdGenerator) in.readObject();
      assertThat(genIn.generate()).hasSize(27);
    }
  }

  private static class FakeClock extends Clock {
    private long timestamp;

    private FakeClock(long timestamp) {
      this.timestamp = timestamp;
    }

    @Override
    public ZoneId getZone() {
      return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zone) {
      return this;
    }

    @Override
    public Instant instant() {
      return Instant.ofEpochMilli(millis());
    }

    @Override
    public long millis() {
      return timestamp++;
    }
  }
}
