package com.software.gui.controllers.cells;

import com.jfoenix.controls.JFXRippler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;


public class ListViewCellController {
    @FXML
    private Label mark_text;
    @FXML
    private Text sub_text;
    @FXML
    private Label main_text;

    @FXML
    private ImageView cell_imageview;

    @FXML
    private Pane root;


    public void setMainText(String text){
        main_text.setText(text);
    }

    public void setMainTextColor(Color color){
        main_text.setTextFill(color);
    }

    public void setSubTextColor(Color color){
        sub_text.setFill(color);
    }

    public void setCell_imageview(Image cellImage) {
        this.cell_imageview.setImage(cellImage);
    }

    public Pane getRoot() {
        return root;
    }

    public void setSubtext(String sub_text) {
        this.sub_text.setText(sub_text);
    }

    public Label getMarkText() {
        return mark_text;
    }

    public void setMarkText(String mark_text) {
        this.mark_text.setText(mark_text);
    }
}
