package com.software.gui.controllers;

import com.jfoenix.controls.JFXAlert;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDecorator;
import io.reactivex.annotations.NonNull;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

public class MyDecorator extends JFXDecorator {
    private JFXButton settingBtn = new JFXButton();
    private JFXButton aboutBtn = new JFXButton();
    public MyDecorator(Stage stage, Node node) {
        super(stage, node);
    }

    public MyDecorator(Stage stage, Node node, boolean fullScreen, boolean max, boolean min) {
        super(stage, node, fullScreen, max, min);
    }

    public void setBtnListener(@NonNull EventHandler settingEventHandler, @NonNull EventHandler aboutEventHandler) {
        Image settingImage = new Image(getClass().getClassLoader().getResourceAsStream("images/setting.png"));
        ImageView settingImageView = new ImageView(settingImage);
        settingImageView.setFitWidth(16);
        settingImageView.setFitHeight(16);
        Image aboutImage = new Image(getClass().getClassLoader().getResourceAsStream("images/about.png"));
        ImageView aboutImageView = new ImageView(aboutImage);
        aboutImageView.setFitWidth(16);
        aboutImageView.setFitHeight(16);

        settingBtn.setGraphic(settingImageView);
        settingBtn.setButtonType(JFXButton.ButtonType.FLAT);
        settingBtn.setRipplerFill(new Color(1,1,1,0.2));
        settingBtn.setCursor(Cursor.HAND);

        aboutBtn.setGraphic(aboutImageView);
        aboutBtn.setButtonType(JFXButton.ButtonType.FLAT);
        aboutBtn.setRipplerFill(new Color(1,1,1,0.2));
        aboutBtn.setCursor(Cursor.HAND);

        ((HBox)(getChildren().get(0))).getChildren().add(1,settingBtn);
        ((HBox)(getChildren().get(0))).getChildren().add(1,aboutBtn);
        aboutBtn.setDisableVisualFocus(true);
        settingBtn.setDisableVisualFocus(true);

        settingBtn.setOnMouseClicked(settingEventHandler);
        aboutBtn.setOnMouseClicked(aboutEventHandler);
    }
}
