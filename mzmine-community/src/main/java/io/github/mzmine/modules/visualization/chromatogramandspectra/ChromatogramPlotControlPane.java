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

package io.github.mzmine.modules.visualization.chromatogramandspectra;

import com.google.common.collect.Range;
import io.github.mzmine.datamodel.PolarityType;
import io.github.mzmine.main.MZmineCore;
import io.github.mzmine.modules.tools.batchwizard.subparameters.custom_parameters.WizardMsPolarity;
import io.github.mzmine.parameters.ParameterSet;
import io.github.mzmine.parameters.parametertypes.ranges.MZRangeComponent;
import io.github.mzmine.util.ExitCode;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.function.Consumer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ChromatogramPlotControlPane extends VBox {

  protected final MZRangeComponent mzRangeNode;
  protected final CheckBox cbXIC;
  protected final Button btnUpdateXIC;

  protected final ObjectProperty<Range<Double>> mzRange;
  private final Button btnParam;
  private final ParameterSet parameters;
  private final ComboBox<WizardMsPolarity> polarityCombo;
  protected NumberFormat mzFormat;
  protected Number min;
  protected Number max;

  protected Consumer<ParameterSet> parameterListener;


  public ChromatogramPlotControlPane(final ParameterSet parameters) {
    super(5);
    this.parameters = parameters;

    setPadding(new Insets(5));

    getStyleClass().add("region-match-chart-bg");

//    setAlignment(Pos.CENTER);
    cbXIC = new CheckBox("Show XIC");
    btnUpdateXIC = new Button("Update chromatogram(s)");
    btnUpdateXIC.setTooltip(new Tooltip("Applies the current m/z range to the TIC/XIC plot."));
    btnParam = new Button("Setup");
    btnParam.setTooltip(new Tooltip("Setup parameters"));
    btnParam.setOnAction(event -> showChromParameterSetup());
    mzRangeNode = new MZRangeComponent();

    // disable mz range and button if xic is not selected
    cbXIC.selectedProperty().addListener((observable, oldValue, newValue) -> {
      // only works if button is active
      if (!btnUpdateXIC.isDisable()) {
        btnUpdateXIC.fire();
      }
      btnUpdateXIC.setDisable(!newValue);
      mzRangeNode.setDisable(!newValue);
      if (!btnUpdateXIC.isDisable()) {
        btnUpdateXIC.fire();
      }
    });

    polarityCombo = new ComboBox<>(FXCollections.observableList(
        List.of(WizardMsPolarity.No_filter, WizardMsPolarity.Positive, WizardMsPolarity.Negative)));
    selectParameterPolarityFilter();
    polarityCombo.getSelectionModel().selectedItemProperty().subscribe(
        (_, polarity) -> setScanPolarityFilterToParameters(polarity.toScanPolaritySelection()));

    Tooltip.install(polarityCombo, new Tooltip("Scan polarity filter"));

    HBox controlsWrap = new HBox(5, cbXIC, btnUpdateXIC, btnParam, polarityCombo);
    controlsWrap.setAlignment(Pos.CENTER);
    mzRangeNode.setAlignment(Pos.CENTER);
    getChildren().addAll(controlsWrap, mzRangeNode);

    addListenersToMzRangeNode();
    mzRange = new SimpleObjectProperty<>();
    mzFormat = MZmineCore.getConfiguration().getMZFormat();
    min = null;
    max = null;
  }

  private void setScanPolarityFilterToParameters(final PolarityType newPolarity) {
    var scanSelection = parameters.getValue(
        ChromatogramAndSpectraVisualizerParameters.scanSelection);
    PolarityType currentPolarity = scanSelection.getPolarity();
    if (newPolarity != currentPolarity) {
      parameters.setParameter(ChromatogramAndSpectraVisualizerParameters.scanSelection,
          scanSelection.withPolarity(newPolarity));
      parametersChanged();
    }
  }

  private void selectParameterPolarityFilter() {
    var scanSel = parameters.getValue(ChromatogramAndSpectraVisualizerParameters.scanSelection);

    var polarity = scanSel == null ? WizardMsPolarity.No_filter
        : WizardMsPolarity.valueOf(scanSel.getPolarity());
    polarityCombo.getSelectionModel().select(polarity);
  }

  public void setParameterListener(Consumer<ParameterSet> parameterListener) {
    this.parameterListener = parameterListener;
  }

  public void showChromParameterSetup() {
    ExitCode code = parameters.showSetupDialog(true);
    if (code == ExitCode.OK) {
      parametersChanged();

      setQuickControls();
    }
  }

  private void parametersChanged() {
    // keep last settings
    MZmineCore.getConfiguration()
        .setModuleParameters(ChromatogramAndSpectraVisualizerModule.class, parameters);
    if (parameterListener != null) {
      parameterListener.accept(parameters);
    }
  }

  private void setQuickControls() {
    selectParameterPolarityFilter();
  }

  public CheckBox getCbXIC() {
    return cbXIC;
  }

  public Range<Double> getMzRange() {
    return mzRange.get();
  }

  public void setMzRange(Range<Double> mzRange) {
    this.mzRange.set(mzRange);
    if (mzRange != null) {
      mzRangeNode.getMinTxtField().setText(mzFormat.format(mzRange.lowerEndpoint()));
      mzRangeNode.getMaxTxtField().setText(mzFormat.format(mzRange.upperEndpoint()));
    } else {
      mzRangeNode.getMinTxtField().setText("");
      mzRangeNode.getMaxTxtField().setText("");
    }
  }

  public ObjectProperty<Range<Double>> mzRangeProperty() {
    return mzRange;
  }

  public Button getBtnUpdateXIC() {
    return btnUpdateXIC;
  }

  private void addListenersToMzRangeNode() {
    mzRangeNode.getMinTxtField().textProperty().addListener(((observable, oldValue, newValue) -> {
      if (newValue == null) {
        return;
      }
      try {
        min = mzFormat.parse(newValue.trim());
      } catch (ParseException e) {
//        e.printStackTrace();
        min = null;
      }
      if (min != null && max != null && min.doubleValue() < max.doubleValue()
          && min.doubleValue() >= 0.0) {
        mzRange.set(Range.closed(min.doubleValue(), max.doubleValue()));
      } else {
        mzRange.set(null);
      }
    }));
    mzRangeNode.getMaxTxtField().textProperty().addListener(((observable, oldValue, newValue) -> {
      if (newValue == null) {
        return;
      }
      try {
        max = mzFormat.parse(newValue.trim());
      } catch (ParseException e) {
//        e.printStackTrace();
        max = null;
      }
      if (min != null && max != null && min.doubleValue() < max.doubleValue()
          && min.doubleValue() >= 0.0) {
        mzRange.set(Range.closed(min.doubleValue(), max.doubleValue()));
      } else {
        mzRange.set(null);
      }
    }));
  }
}
