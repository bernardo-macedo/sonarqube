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
package org.sonar.server.computation.measure;

import javax.annotation.Nullable;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.sonar.server.computation.measure.MeasureVariations.newMeasureVariationsBuilder;

public class MeasureVariationsTest {
  public static final String NO_VARIATION_ERROR_MESSAGE = "There must be at least one variation";
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void constructor_throws_IAE_if_array_arg_has_more_than_5_elements() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("There can not be more than 5 variations");

    new MeasureVariations(1d, 2d, 3d, 4d, 5d, 6d);
  }

  @Test
  public void constructor_throws_IAE_if_no_arg() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage(NO_VARIATION_ERROR_MESSAGE);

    new MeasureVariations();
  }

  @Test
  public void constructor_throws_IAE_if_single_arg_is_null() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage(NO_VARIATION_ERROR_MESSAGE);

    new MeasureVariations((Double) null);
  }

  @Test
  public void constructor_throws_IAE_if_two_args_are_null() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage(NO_VARIATION_ERROR_MESSAGE);

    new MeasureVariations((Double) null, (Double) null);
  }

  @Test
  public void constructor_throws_IAE_if_three_args_are_null() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage(NO_VARIATION_ERROR_MESSAGE);

    new MeasureVariations((Double) null, (Double) null, (Double) null);
  }

  @Test
  public void constructor_throws_IAE_if_four_args_are_null() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage(NO_VARIATION_ERROR_MESSAGE);

    new MeasureVariations((Double) null, (Double) null, (Double) null, (Double) null);
  }

  @Test
  public void constructor_throws_IAE_if_five_args_are_null() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage(NO_VARIATION_ERROR_MESSAGE);

    new MeasureVariations((Double) null, (Double) null, (Double) null, (Double) null, (Double) null);
  }

  @Test
  public void verify_has_variationX_and_getVariationX() {
    verifyAsVariations(new MeasureVariations(1d), 1d, null, null, null, null);

    verifyAsVariations(new MeasureVariations(1d, 2d), 1d, 2d, null, null, null);
    verifyAsVariations(new MeasureVariations(null, 2d), null, 2d, null, null, null);
    verifyAsVariations(new MeasureVariations(1d, null), 1d, null, null, null, null);

    verifyAsVariations(new MeasureVariations(1d, 2d, 3d), 1d, 2d, 3d, null, null);
    verifyAsVariations(new MeasureVariations(null, 2d, 3d), null, 2d, 3d, null, null);
    verifyAsVariations(new MeasureVariations(1d, null, 3d), 1d, null, 3d, null, null);
    verifyAsVariations(new MeasureVariations(null, null, 3d), null, null, 3d, null, null);
    verifyAsVariations(new MeasureVariations(1d, 2d, null), 1d, 2d, null, null, null);

    verifyAsVariations(new MeasureVariations(1d, 2d, 3d, 4d), 1d, 2d, 3d, 4d, null);
    verifyAsVariations(new MeasureVariations(null, 2d, 3d, 4d), null, 2d, 3d, 4d, null);
    verifyAsVariations(new MeasureVariations(null, 2d, null, 4d), null, 2d, null, 4d, null);
    verifyAsVariations(new MeasureVariations(null, null, 3d, 4d), null, null, 3d, 4d, null);
    verifyAsVariations(new MeasureVariations(null, null, null, 4d), null, null, null, 4d, null);
    verifyAsVariations(new MeasureVariations(1d, null, 3d, 4d), 1d, null, 3d, 4d, null);
    verifyAsVariations(new MeasureVariations(1d, 2d, null, 4d), 1d, 2d, null, 4d, null);
    verifyAsVariations(new MeasureVariations(1d, 2d, 3d, null), 1d, 2d, 3d, null, null);

    verifyAsVariations(new MeasureVariations(1d, 2d, 3d, 4d, 5d), 1d, 2d, 3d, 4d, 5d);
    verifyAsVariations(new MeasureVariations(null, 2d, 3d, 4d, 5d), null, 2d, 3d, 4d, 5d);
    verifyAsVariations(new MeasureVariations(1d, null, 3d, 4d, 5d), 1d, null, 3d, 4d, 5d);
    verifyAsVariations(new MeasureVariations(1d, 2d, null, 4d, 5d), 1d, 2d, null, 4d, 5d);
    verifyAsVariations(new MeasureVariations(1d, 2d, 3d, null, 5d), 1d, 2d, 3d, null, 5d);
    verifyAsVariations(new MeasureVariations(1d, 2d, 3d, 4d, null), 1d, 2d, 3d, 4d, null);
  }

  private static void verifyAsVariations(MeasureVariations measureVariations,
    @Nullable Double variation1, @Nullable Double variation2, @Nullable Double variation3, @Nullable Double variation4, @Nullable Double variation5) {
    assertThat(measureVariations.hasVariation1()).isEqualTo(variation1 != null);
    try {
      if (variation1 == null) {
        measureVariations.getVariation1();
        fail("An exception should have been raised");
      }
      assertThat(measureVariations.getVariation1()).isEqualTo(variation1);
    } catch (IllegalStateException e) {
      assertThat(e.getMessage()).isEqualTo("Variation 1 has not been set");
    }
    assertThat(measureVariations.hasVariation2()).isEqualTo(variation2 != null);
    try {
      if (variation2 == null) {
        measureVariations.getVariation2();
        fail("An exception should have been raised");
      }
      assertThat(measureVariations.getVariation2()).isEqualTo(variation2);
    } catch (IllegalStateException e) {
      assertThat(e.getMessage()).isEqualTo("Variation 2 has not been set");
    }
    assertThat(measureVariations.hasVariation3()).isEqualTo(variation3 != null);
    try {
      if (variation3 == null) {
        measureVariations.getVariation3();
        fail("An exception should have been raised");
      }
      assertThat(measureVariations.getVariation3()).isEqualTo(variation3);
    } catch (IllegalStateException e) {
      assertThat(e.getMessage()).isEqualTo("Variation 3 has not been set");
    }
    assertThat(measureVariations.hasVariation4()).isEqualTo(variation4 != null);
    try {
      if (variation4 == null) {
        measureVariations.getVariation4();
        fail("An exception should have been raised");
      }
      assertThat(measureVariations.getVariation4()).isEqualTo(variation4);
    } catch (IllegalStateException e) {
      assertThat(e.getMessage()).isEqualTo("Variation 4 has not been set");
    }
    assertThat(measureVariations.hasVariation5()).isEqualTo(variation5 != null);
    try {
      if (variation5 == null) {
        measureVariations.getVariation5();
        fail("An exception should have been raised");
      }
      assertThat(measureVariations.getVariation5()).isEqualTo(variation5);
    } catch (IllegalStateException e) {
      assertThat(e.getMessage()).isEqualTo("Variation 5 has not been set");
    }
  }

  @Test
  public void verify_toString() {
    assertThat(new MeasureVariations(1d).toString()).isEqualTo("MeasureVariations{1=1.0, 2=null, 3=null, 4=null, 5=null}");
    assertThat(new MeasureVariations(1d, 2d, 3d, 4d, 5d).toString()).isEqualTo("MeasureVariations{1=1.0, 2=2.0, 3=3.0, 4=4.0, 5=5.0}");
  }

  @Test
  public void equals_takes_values_into_account() {
    MeasureVariations variations = new MeasureVariations(1d);

    assertThat(variations).isEqualTo(variations);
    assertThat(variations).isEqualTo(new MeasureVariations(1d));
    assertThat(new MeasureVariations(null, 1d)).isEqualTo(new MeasureVariations(null, 1d));

    assertThat(new MeasureVariations(null, 2d)).isNotEqualTo(new MeasureVariations(null, 1d));
  }

  @Test
  public void equals_does_not_depend_on_constructor_argument() {
    assertThat(new MeasureVariations(null, 1d, null)).isEqualTo(new MeasureVariations(null, 1d));
  }

  @Test
  public void builder_throws_IAE_if_index_is_0() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("Variation index must be >= 1 and <= 5");

    newMeasureVariationsBuilder().setVariation(0, 12d);
  }

  @Test
  public void builder_throws_IAE_if_index_is_less_than_0() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("Variation index must be >= 1 and <= 5");

    newMeasureVariationsBuilder().setVariation(-965, 12d);
  }

  @Test
  public void builder_throws_IAE_if_index_is_6() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("Variation index must be >= 1 and <= 5");

    newMeasureVariationsBuilder().setVariation(6, 12d);
  }

  @Test
  public void builder_throws_IAE_if_index_is_more_than_6() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("Variation index must be >= 1 and <= 5");

    newMeasureVariationsBuilder().setVariation(75, 12d);
  }

  @Test
  public void builder_throws_ISE_if_variation_has_already_been_set() {
    MeasureVariations.Builder builder = newMeasureVariationsBuilder().setVariation(4, 12d);

    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage("Variation for index 4 has already been set");

    builder.setVariation(4, 1d);
  }

  @Test
  public void verify_MeasureVariations_built_by_builder() {
    MeasureVariations variations = newMeasureVariationsBuilder()
      .setVariation(1, 1d)
      .setVariation(2, 2d)
      .setVariation(3, 3d)
      .setVariation(4, 4d)
      .setVariation(5, 5d)
      .build();

    verifyAsVariations(variations, 1d, 2d, 3d, 4d, 5d);
  }
}
