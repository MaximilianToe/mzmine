/*
 * Copyright 2006-2020 The MZmine Development Team
 *
 * This file is part of MZmine.
 *
 * MZmine is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * MZmine is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MZmine; if not,
 * write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301
 * USA
 */

package io.github.mzmine.parameters.parametertypes.colorpalette;

import io.github.mzmine.main.MZmineCore;
import java.util.regex.Pattern;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import io.github.mzmine.util.ExitCode;
import io.github.mzmine.util.color.SimpleColorPalette;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

/**
 * Dialog to pick colors for a color palette.
 *
 * @author SteffenHeu steffen.heuckeroth@gmx.de / s_heuc03@uni-muenster.de
 */
public class ColorPalettePickerDialog extends Stage {

  protected ExitCode exitCode;

  //  protected BorderPane pnSuper;
//  protected BorderPane pnPicker;
  protected GridPane pnMain;
  protected ColorPalettePreviewField pnPalette;
  protected Button btnAccept;
  protected Button btnCancel;
  protected Button btnAddColor;
  protected Button btnRemoveColor;
  protected ColorPicker colorPickerPalette;
  protected ColorPicker colorPickerPositive;
  protected ColorPicker colorPickerNegative;
  protected TextField txtName;

  protected SimpleColorPalette palette;
  protected int selected;

  public ColorPalettePickerDialog(@Nullable SimpleColorPalette palette) {
    super();

    exitCode = ExitCode.CANCEL;

    if (palette == null) {
      palette = new SimpleColorPalette();
    }
    this.palette = palette;
    selected = 0;

    // Create gui components
    pnMain = new GridPane();
    pnMain.setHgap(5);
    pnMain.setVgap(5);
    pnPalette = new ColorPalettePreviewField(palette);
    btnAccept = new Button("Accept");
    btnCancel = new Button("Cancel");
    btnAddColor = new Button("Add");
    btnRemoveColor = new Button("Remove");
    colorPickerPalette = new ColorPicker();
    colorPickerPositive = new ColorPicker();
    colorPickerNegative = new ColorPicker();
    txtName = new TextField(palette.getName());
    txtName.setMaxWidth(250);

    // organize gui components
    pnMain.add(new Label("Name"), 0, 0);
    pnMain.add(txtName, 1, 0, 4, 1);

    pnMain.add(new Label("Palette"), 0, 1);
    pnMain.add(pnPalette, 1, 1, 4, 1);

    pnMain.add(new Label("Color"), 0, 2);
    pnMain.add(colorPickerPalette, 1, 2, 2, 1);
    pnMain.add(btnAddColor, 3, 2);
    pnMain.add(btnRemoveColor, 4, 2);

    pnMain.add(new Label("Positive"), 0, 3);
    pnMain.add(colorPickerPositive, 1, 3, 2, 1);
    pnMain.add(new Label("Negative"), 0, 4);
    pnMain.add(colorPickerNegative, 1, 4, 2, 1);

    pnMain.add(btnAccept, 1, 3);
    pnMain.add(btnCancel, 2, 3);

    colorPickerPalette.setOnAction(e -> {
      if (colorPickerPalette.getValue() != null) {
        int selected = pnPalette.getSelected();
        if (selected > 0 && selected < this.palette.size()) {
          this.palette.set(selected, colorPickerPalette.getValue());
        }
      }
    });
    colorPickerPalette.setValue(palette.get(pnPalette.getSelected()));

    colorPickerPositive.setOnAction(e -> {
      if (colorPickerPositive.getValue() != null) {
        this.palette.setPositiveColor(colorPickerPositive.getValue());
      }
    });

    colorPickerNegative.setOnAction(e -> {
      if (colorPickerNegative.getValue() != null) {
        this.palette.setNegativeColor(colorPickerNegative.getValue());
      }
    });

    pnPalette.addListener((Color newColor, int newIndex) -> {
      colorPickerPalette.setValue(newColor);
    });

    // set button actions
    btnAddColor.setOnAction(e -> btnAddColorAction());
    btnRemoveColor.setOnAction(e -> btnRemoveColorAction());
    btnAccept.setOnAction(e -> hideWindow(ExitCode.OK));
    btnCancel.setOnAction(e -> hideWindow(ExitCode.CANCEL));

    Scene scene = new Scene(pnMain);
    setScene(scene);
  }

  private void btnAddColorAction() {
    if (palette.size() == 0) {
      this.setHeight(this.getHeight() + 17);
    }
    palette.add(colorPickerPalette.getValue());
//    pnPalette.updatePreview();
  }

  private void btnRemoveColorAction() {
    palette.remove(pnPalette.getSelected());
//    pnPalette.updatePreview();
  }

  private void hideWindow(ExitCode exitCode) {
    String name = txtName.getText();
    if (name == null || name == "" || name.replaceAll("\\s+", "").equals("")) {
      MZmineCore.getDesktop().displayErrorMessage("Please set a name for the color palette.");
      return;
    }
    palette.setName(name);
    this.exitCode = exitCode;
    hide();
  }

  public ExitCode getExitCode() {
    return exitCode;
  }

  public @Nonnull
  SimpleColorPalette getPalette() {
    return palette;
  }
}
