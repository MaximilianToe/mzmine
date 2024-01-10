/*
 * Copyright (c) 2004-2023 The MZmine Development Team
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

package io.github.mzmine.modules.dataprocessing.group_imagecorrelate;

import io.github.mzmine.datamodel.features.FeatureListRow;
import io.github.mzmine.datamodel.features.ModularFeature;
import io.github.mzmine.gui.chartbasics.chartgroups.ChartGroup;
import io.github.mzmine.gui.chartbasics.gui.javafx.EChartViewer;
import io.github.mzmine.gui.chartbasics.gui.wrapper.ChartViewWrapper;
import io.github.mzmine.main.MZmineCore;
import io.github.mzmine.modules.visualization.image.ImageVisualizerModule;
import io.github.mzmine.modules.visualization.image.ImageVisualizerParameters;
import io.github.mzmine.modules.visualization.image.ImageVisualizerTab;
import io.github.mzmine.parameters.ParameterSet;
import io.github.mzmine.util.color.ColorScaleUtil;
import io.github.mzmine.util.javafx.FxColorUtil;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * Displays all correlated images in a grid
 */
public class ColocatedImagePane extends GridPane {

  private static final Logger logger = Logger.getLogger(ColocatedImagePane.class.getName());
  public static final Color MAX_COS_COLOR = MZmineCore.getConfiguration().getDefaultColorPalette()
      .getPositiveColor();
  public static final Color MIN_COS_COLOR = MZmineCore.getConfiguration().getDefaultColorPalette()
      .getNegativeColor();
  public static final double MIN_COS_COLOR_VALUE = 0.5;
  public static final double MAX_COS_COLOR_VALUE = 1.0;
  private final ChartGroup chartGroup;

  public ColocatedImagePane(Map<FeatureListRow, Double> sortedCorrelatedRows) {
    super();
    this.setHgap(10);
    this.setVgap(10);
    chartGroup = new ChartGroup(false, false, true, true);
    updateContent(sortedCorrelatedRows);
  }

  public void updateContent(Map<FeatureListRow, Double> sortedCorrelatedRows) {
    this.getChildren().clear();
    if(sortedCorrelatedRows == null || sortedCorrelatedRows.isEmpty()) {
      return;
    }

    int i = 0;
    for (Entry<FeatureListRow, Double> entry : sortedCorrelatedRows.entrySet()) {
      FeatureListRow row = entry.getKey();
      Node imagePlot = createImagePlotForRow(row, entry.getValue());
      int rowPosition = i / 4;
      int colPosition = i % 4;
      this.add(imagePlot, colPosition, rowPosition);
      if (i > 100) {
        logger.log(Level.WARNING, "Only show first 100 correlations");
        break;
      }
      i++;
    }
  }

  public Node createImagePlotForRow(FeatureListRow row, Double score) {
    BorderPane borderPane = new BorderPane();
    ParameterSet params = MZmineCore.getConfiguration()
        .getModuleParameters(ImageVisualizerModule.class).cloneParameterSet();
    params.setParameter(ImageVisualizerParameters.imageNormalization,
        MZmineCore.getConfiguration().getImageNormalization()); // same as in the feature table.
    params.setParameter(ImageVisualizerParameters.imageTransformation,
        MZmineCore.getConfiguration().getImageTransformation());
    ImageVisualizerTab imageVisualizerTab = new ImageVisualizerTab(
        (ModularFeature) row.getBestFeature(), params);
    EChartViewer chart = imageVisualizerTab.getImagingPlot().getChart();
    chart.setMinSize(200, 200);
    chart.getChart().getXYPlot().setBackgroundPaint(java.awt.Color.BLACK);
    chartGroup.add(new ChartViewWrapper(chart));
    GridPane.setHgrow(chart, Priority.ALWAYS);
    GridPane.setVgrow(chart, Priority.ALWAYS);
    borderPane.setCenter(chart);
    borderPane.setTop(createTitlePane(row, score));
    GridPane.setHgrow(borderPane, Priority.ALWAYS);
    GridPane.setVgrow(borderPane, Priority.ALWAYS);

    return borderPane;
  }

  private Pane createTitlePane(FeatureListRow row, Double score) {
    String styleWhiteScoreSmall = "white-score-label-small";
    var featureInfo = new VBox(0, createLabel("Feature", styleWhiteScoreSmall),
        createLabel("#" + row.getID(), styleWhiteScoreSmall));
    featureInfo.setAlignment(Pos.CENTER_RIGHT);
    Color gradientCol = FxColorUtil.awtColorToFX(
        ColorScaleUtil.getColor(FxColorUtil.fxColorToAWT(MIN_COS_COLOR),
            FxColorUtil.fxColorToAWT(MAX_COS_COLOR), MIN_COS_COLOR_VALUE, MAX_COS_COLOR_VALUE,
            score));
    String name = extractName(row);
    Label lblHit = createLabel(name, "white-larger-label");
    String simScoreTooltip =
        "Similarity with selected image: " + MZmineCore.getConfiguration().getScoreFormat()
            .format(score);
    Label lblScore = createLabel(MZmineCore.getConfiguration().getScoreFormat().format(score),
        simScoreTooltip, "white-score-label");
    var scoreBox = new HBox(5, featureInfo, lblScore);
    scoreBox.setPadding(new Insets(0, 5, 0, 10));
    scoreBox.setAlignment(Pos.CENTER);
    var titlePane = new BorderPane(lblHit);
    titlePane.setRight(scoreBox);
    titlePane.setPadding(new Insets(2));
    titlePane.setStyle("-fx-background-color: " + FxColorUtil.colorToHex(gradientCol));
    return titlePane;
  }

  private String extractName(FeatureListRow row) {
    String mz = "m/z " + MZmineCore.getConfiguration().getMZFormat().format(row.getAverageMZ());
    if (row.getPreferredAnnotationName() != null) {
      return row.getPreferredAnnotationName() + " " + mz;
    } else if (row.getBestIonIdentity() != null) {
      return row.getBestIonIdentity().getAdduct() + " " + row.getBestIonIdentity()
          .getBestMolFormula().getFormulaAsString() + " " + mz;
    } else {
      return mz;
    }
  }

  private Label createLabel(final String label, final String styleClass) {
    return createLabel(label, null, styleClass);
  }

  private Label createLabel(final String label, String tooltip, final String styleClass) {
    Label lbl = new Label(label);
    lbl.getStyleClass().add(styleClass);
    if (tooltip != null) {
      lbl.setTooltip(new Tooltip(tooltip));
    }
    return lbl;
  }

}
