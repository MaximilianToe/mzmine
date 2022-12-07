/*
 * Copyright (c) 2004-2022 The MZmine Development Team
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package io.github.mzmine.datamodel.features.types;

import io.github.mzmine.datamodel.features.types.numbers.MobilityAbsoluteDifferenceType;
import io.github.mzmine.datamodel.features.types.numbers.MzAbsoluteDifferenceType;
import io.github.mzmine.datamodel.features.types.numbers.MzPpmDifferenceType;
import io.github.mzmine.datamodel.features.types.numbers.RtAbsoluteDifferenceType;
import io.github.mzmine.datamodel.features.types.numbers.scores.RateType;
import java.util.List;

/**
 * Saves scores on the alignment
 *
 * @param rate          the aligned/total samples
 * @param extraFeatures features that fall within the alignment range but were not aligned with this
 *                      feature
 * @param mzDelta
 * @param rtDelta
 * @param mobilityDelta
 */
public record AlignmentScores(float rate, int alignedFeatures, int extraFeatures, Float mzPpmDelta,
                              Double mzDelta, Float rtDelta, Float mobilityDelta) {

  // Unmodifiable list of all subtypes
  public static final List<DataType> subTypes = List.of(new RateType(), new AlignedFeaturesNType(),
      new AlignExtraFeaturesType(), new MzPpmDifferenceType(), new MzAbsoluteDifferenceType(),
      new RtAbsoluteDifferenceType(), new MobilityAbsoluteDifferenceType());


  public AlignmentScores {
  }

  public AlignmentScores() {
    this(0, 0, 0, null, null, null, null);
  }

  /**
   * Provides data for data types
   *
   * @param sub data column
   * @return the score for this column
   */
  public Object getValue(final DataType sub) {
    return switch (sub) {
      case RateType ignored -> rate;
      case AlignedFeaturesNType ignored -> alignedFeatures;
      case AlignExtraFeaturesType ignored -> extraFeatures;
      case MzPpmDifferenceType ignored -> mzPpmDelta;
      case MzAbsoluteDifferenceType ignored -> mzDelta;
      case RtAbsoluteDifferenceType ignored -> rtDelta;
      case MobilityAbsoluteDifferenceType ignored -> mobilityDelta;
      default -> throw new IllegalStateException("Unexpected value: " + sub);
    };
  }

  /**
   * Modify one value
   *
   * @param sub   data column
   * @param value new value
   * @return a new object with one modified value
   */
  public <T> AlignmentScores modify(final DataType<T> sub, T value) {
    return switch (sub) {
      case RateType ignored ->
          new AlignmentScores((Float) value, alignedFeatures, extraFeatures, mzPpmDelta, mzDelta,
              rtDelta, mobilityDelta);
      case AlignedFeaturesNType ignored ->
          new AlignmentScores(rate, (Integer) value, extraFeatures, mzPpmDelta, mzDelta, rtDelta,
              mobilityDelta);
      case AlignExtraFeaturesType ignored ->
          new AlignmentScores(rate, alignedFeatures, (Integer) value, mzPpmDelta, mzDelta, rtDelta,
              mobilityDelta);
      case MzPpmDifferenceType ignored ->
          new AlignmentScores(rate, alignedFeatures, extraFeatures, (Float) value, mzDelta, rtDelta,
              mobilityDelta);
      case MzAbsoluteDifferenceType ignored ->
          new AlignmentScores(rate, alignedFeatures, extraFeatures, mzPpmDelta, (Double) value,
              rtDelta, mobilityDelta);
      case RtAbsoluteDifferenceType ignored ->
          new AlignmentScores(rate, alignedFeatures, extraFeatures, mzPpmDelta, mzDelta,
              (Float) value, mobilityDelta);
      case MobilityAbsoluteDifferenceType ignored ->
          new AlignmentScores(rate, alignedFeatures, extraFeatures, mzPpmDelta, mzDelta, rtDelta,
              (Float) value);
      default -> throw new IllegalStateException("Unexpected value: " + sub);
    };
  }
}
