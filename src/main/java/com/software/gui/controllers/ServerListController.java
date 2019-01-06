package com.software.gui.controllers;

import com.jfoenix.controls.JFXAlert;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListCell;
import com.jfoenix.controls.JFXListView;
import com.software.gui.controllers.beans.ServerInf;
import com.software.gui.controllers.cells.ServerCellController;
import com.software.gui.logic.CacheManager;
import com.software.gui.logic.ServersCache;
import com.software.gui.utils.FileHelper;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class ServerListController implements Initializable {

    private Logger logger = Logger.getLogger(getClass().getSimpleName());

    @FXML
    public JFXListView serverList;
    @FXML
    private JFXButton addBtn;

    private ServersCache serversCache;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        serversCache = new ServersCache();
        CacheManager.INSTANCE.registerCache(CacheManager.Key.createKey(1,ServersCache.class),serversCache);

        addBtn.setText("新建");
        addBtn.setOnAction(new EventHandler<ActionEvent>() {
            Node content;
            ServerEditDialogController controller;
            JFXAlert dialog;
            @Override
            public void handle(ActionEvent event) {
                startServerEditor(content,controller,dialog);
            }
        });
        serverList.getStylesheets()
                .add(Objects.requireNonNull(getClass().getClassLoader().getResource("css/listview_cell.css")).toExternalForm());
        serverList.setCellFactory(param -> {
            ServerCell cell = new ServerCell();
            cell.prefWidthProperty().bind(serverList.widthProperty().subtract(15));
            return cell;
        });

        serverList.getItems().addAll(serversCache.getList());
    }

    private void startServerEditor(Node content,
            ServerEditDialogController controller,
            JFXAlert dialog){

        //start a dialog to create a server
        try {
            if(content == null) {
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(Objects.requireNonNull(getClass().getClassLoader().getResource("server_edit_dialog.fxml")));
                content = loader.load();
                controller = loader.getController();
                dialog = new JFXAlert((Stage) addBtn.getScene().getWindow());
                dialog.setContent(content);
                dialog.initModality(Modality.APPLICATION_MODAL);
                dialog.setOverlayClose(true);
                controller.setDialog(dialog);
                controller.setSuccessActionEventHandler(new EventHandler() {
                    @Override
                    public void handle(Event event) {

                        ServerEditDialogController.ServerEvent serverEvent = (ServerEditDialogController.ServerEvent) event;
                        if(((ServerEditDialogController.ServerEvent) event).getServerInf() == null)
                            return;
                        serversCache.addServerInf(serverEvent.getServerInf());
                        serverList.getItems().add(serverEvent.getServerInf());
                        serverList.refresh();
                    }
                });
            }
            dialog.show();
        } catch (IOException e) {
            logger.throwing(getClass().getSimpleName(),"onClick",e);
        }
    }



    private class ServerCell extends JFXListCell<ServerInf>{

        private ServerCellController serverCellController;
        private FXMLLoader loader = new FXMLLoader();

        private void initListViewCell(){

            loader.setLocation(getClass().getClassLoader().getResource("server_listview_cell.fxml"));
            try {
                loader.load();
                serverCellController = loader.getController();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        ServerCell(){
            super();
            initListViewCell();
        }

        @Override
        public void updateSelected(boolean selected) {
            super.updateSelected(selected);
        }

        @Override
        protected void updateItem(ServerInf item, boolean empty) {
            super.updateItem(item, empty);
            setText(null);
            if(!empty && item != null ){
                serverCellController.setServerName(item.getName());
                Node pane = serverCellController.getRoot();
                setGraphic(pane);
            }else{
                setGraphic(null);
            }
        }
    }
}


