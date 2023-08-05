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

package io.github.mzmine.modules.visualization.spectra.matchedlipid;

import io.github.mzmine.main.MZmineCore;
import io.github.mzmine.modules.dataprocessing.id_lipididentification.common.lipids.LipidAnnotationLevel;
import io.github.mzmine.modules.dataprocessing.id_lipididentification.common.lipids.LipidFragment;
import java.util.List;
import org.jfree.chart.fx.ChartViewer;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.data.xy.XYDataset;

public class MatchedLipidLabelGenerator implements XYItemLabelGenerator {

  /*
   * Number of screen pixels to reserve for each label, so that the labels do not overlap
   */
 private static final int POINTS_RESERVE_X = 100;

  private final ChartViewer plot;
  private final List<LipidFragment> fragments;

  public MatchedLipidLabelGenerator(ChartViewer plot,List<LipidFragment> fragments) {
    this.plot = plot;
    this.fragments = fragments;
  }


  /**
   * @see org.jfree.chart.labels.XYItemLabelGenerator#generateLabel(org.jfree.data.xy.XYDataset,
   *      int, int)
   */
  @Override
  public String generateLabel(XYDataset dataset, int series, int item) {
    String label = null;
    if (plot.getCanvas().getWidth() >= 600 && plot.getCanvas().getHeight() >= 400) {
      if (dataset.getSeriesKey(1).equals("Matched Signals")) {
        if (fragments != null) {
          label = buildFragmentAnnotation(fragments.get(item));
        }
      }
    }
    return label;
  }

  private String buildFragmentAnnotation(LipidFragment lipidFragment) {
    StringBuilder sb = new StringBuilder();
    if (lipidFragment.getLipidFragmentInformationLevelType()
        .equals(LipidAnnotationLevel.MOLECULAR_SPECIES_LEVEL)) {
      sb.append(lipidFragment.getLipidChainType().getName()).append(" ")
          .append(lipidFragment.getChainLength()).append(":")
          .append(lipidFragment.getNumberOfDBEs()).append("\n")
          .append(lipidFragment.getIonFormula()).append("\n")
          .append(MZmineCore.getConfiguration().getMZFormat().format(lipidFragment.getMzExact()));
    } else {
      sb.append(lipidFragment.getRuleType().toString()).append("\n")
          .append(lipidFragment.getIonFormula()).append("\n")
          .append(MZmineCore.getConfiguration().getMZFormat().format(lipidFragment.getMzExact()));
    }
    return sb.toString();
  }
}
