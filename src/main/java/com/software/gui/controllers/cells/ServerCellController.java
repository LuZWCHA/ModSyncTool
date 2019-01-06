package com.software.gui.controllers.cells;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

public class ServerCellController {

    @FXML
    public ImageView server_icon;
    @FXML
    public Label server_name;
    @FXML
    public AnchorPane root;

    public AnchorPane getRoot() {
        return root;
    }

    public void setServerIcon(){

    }

    public void setServerName(String serverName){
        server_name.setText(serverName);
    }

    public String getServerName() {
        return server_name.getText();
    }

    @Override
    public String toString() {
        return server_name.getText();
    }
}
