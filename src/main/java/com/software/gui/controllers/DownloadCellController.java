package com.software.gui.controllers;

import com.jfoenix.controls.JFXProgressBar;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;

public class DownloadCellController implements Initializable {

    @FXML
    private AnchorPane root;

    private DecimalFormat df = new DecimalFormat("0.00");

    @FXML
    private JFXProgressBar progress_bar;
    @FXML
    private Label title_text;
    @FXML
    private Text speed_text;
    @FXML
    private Text state_text;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        title_text.setText("unknown");
        speed_text.setText("-");
        state_text.setText("-");
        progress_bar.setProgress(0);
    }

    public void setState(String state){
        state_text.setText(state);
    }

    public void setStateColor(Color color){
        state_text.setFill(color);
    }

    public void setTitle(String title) {
        this.title_text.setText(title);
    }

    public void setSpeed(double speed) {
        this.speed_text.setText(df.format(speed)+"KB/S");
    }

    public void setSpeedDisable(){
        this.speed_text.setText(null);
    }

    public void setProgress(double progress) {
        this.progress_bar.setProgress(progress);
    }

    public AnchorPane getRoot() {
        return root;
    }
}
