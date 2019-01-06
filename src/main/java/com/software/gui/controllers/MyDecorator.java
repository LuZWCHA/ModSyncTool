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
    public MyDecorator(Stage stage, Node node) {
        super(stage, node);
    }

    public MyDecorator(Stage stage, Node node, boolean fullScreen, boolean max, boolean min) {
        super(stage, node, fullScreen, max, min);
    }

    public void setSettingBtnListener(@NonNull EventHandler eventHandler) {
        Image image = new Image(getClass().getClassLoader().getResourceAsStream("images/setting.png"));
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(16);
        imageView.setFitHeight(16);
        settingBtn.setGraphic(imageView);
        settingBtn.setButtonType(JFXButton.ButtonType.FLAT);
        settingBtn.setRipplerFill(new Color(1,1,1,0.2));
        settingBtn.setCursor(Cursor.HAND);
        ((HBox)(getChildren().get(0))).getChildren().add(1,settingBtn);
        settingBtn.setDisableVisualFocus(true);
        settingBtn.setOnMouseClicked(eventHandler);
    }
}
