/*
 * SonarQube, open source software quality management tool.
 * Copyright (C) 2008-2014 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * SonarQube is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * SonarQube is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.server.computation.debt;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class DebtModelHolderImplTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private static final Characteristic PORTABILITY = new Characteristic(1, "PORTABILITY", null);
  private static final Characteristic COMPILER_RELATED_PORTABILITY = new Characteristic(2, "COMPILER_RELATED_PORTABILITY", 1);
  private static final Characteristic HARDWARE_RELATED_PORTABILITY = new Characteristic(3, "HARDWARE_RELATED_PORTABILITY", 1);

  private static final Characteristic MAINTAINABILITY = new Characteristic(4, "MAINTAINABILITY", null);
  private static final Characteristic READABILITY = new Characteristic(5, "READABILITY", null);

  DebtModelHolderImpl sut = new DebtModelHolderImpl();

  @Test
  public void add_and_get_characteristics() {
    sut.addCharacteristics(PORTABILITY, Arrays.asList(COMPILER_RELATED_PORTABILITY, HARDWARE_RELATED_PORTABILITY));
    sut.addCharacteristics(MAINTAINABILITY, singletonList(READABILITY));

    assertThat(sut.getRootCharacteristics()).hasSize(2);
    assertThat(sut.getCharacteristicById(PORTABILITY.getId()).getKey()).isEqualTo("PORTABILITY");
    assertThat(sut.getCharacteristicById(COMPILER_RELATED_PORTABILITY.getId()).getKey()).isEqualTo("COMPILER_RELATED_PORTABILITY");
  }

  @Test
  public void add_characteristics_fail_with_NPE_if_root_characteristic_is_null() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("rootCharacteristic cannot be null");

    sut.addCharacteristics(null, singletonList(COMPILER_RELATED_PORTABILITY));
  }

  @Test
  public void add_characteristics_fail_with_NPE_if_sub_characteristics_are_null() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("subCharacteristics cannot be null");

    sut.addCharacteristics(PORTABILITY, null);
  }

  @Test
  public void add_characteristics_fail_with_IAE_if_sub_characteristics_are_empty() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("subCharacteristics cannot be empty");

    sut.addCharacteristics(PORTABILITY, Collections.<Characteristic>emptyList());
  }

  @Test
  public void get_root_characteristics() {
    sut.addCharacteristics(PORTABILITY, Arrays.asList(COMPILER_RELATED_PORTABILITY, READABILITY));
    sut.addCharacteristics(MAINTAINABILITY, singletonList(READABILITY));

    assertThat(sut.getRootCharacteristics()).hasSize(2);
  }

  @Test
  public void getCharacteristicById_throws_ISE_when_not_initialized() {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("Characteristics have not been initialized yet");

    sut.getCharacteristicById(1);
  }

  @Test
  public void getRootCharacteristics_throws_ISE_when_not_initialized() {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("Characteristics have not been initialized yet");

    sut.getRootCharacteristics();
  }
}
