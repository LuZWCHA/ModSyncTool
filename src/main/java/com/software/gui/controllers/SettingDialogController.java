package com.software.gui.controllers;

import com.jfoenix.controls.*;
import com.rxcode.rxdownload.api.ANY;
import com.software.gui.Config;
import com.software.api.Managers.CacheManager;
import com.software.api.SyncController;
import com.software.gui.beans.ServerInf;
import com.software.gui.logic.DirInfoCache;
import com.software.gui.logic.ServersCache;
import com.software.gui.scheduler.JavaFxScheduler;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class SettingDialogController implements SyncController {

    private Logger logger = Logger.getLogger(getClass().getSimpleName());

    @FXML
    private AnchorPane root;
    @FXML
    public JFXComboBox server_name_combox;
    @FXML
    private JFXComboBox<Integer> download_num_combox;
    @FXML
    private JFXToggleButton relative_path_togbtn;
    @FXML
    private JFXButton save_btn;
    @FXML
    private JFXButton cancel_btn;

    private JFXAlert dialog;

    private JFXSnackbar snackbar;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        ServersCache serversCache = CacheManager.INSTANCE.getCache(1);
        DirInfoCache cache = CacheManager.INSTANCE.getCache(0);
        updateServers(serversCache);

        ObservableList<Integer> comboxList = FXCollections.observableArrayList();
        Observable.<Integer>range(1,Runtime.getRuntime().availableProcessors() + 1)
                .collectInto(comboxList, List::add).subscribe();
        download_num_combox.setItems(comboxList);
        download_num_combox.setValue(Config.DOWNLOAD_THREAD_NUM);

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.LIGHTGREY);
        snackbar = new JFXSnackbar(root);
        snackbar.setEffect(shadow);
        snackbar.setPrefWidth(200);

        relative_path_togbtn.setSelected(Config.USE_RELATIVE_PATH);

        cancel_btn.setOnMouseClicked(event -> {close();});
        save_btn.setOnMouseClicked(event -> {
            Single.just(ANY.product())
                    .map(s -> {
                        ServerInf si = serversCache.getServerInfByName(server_name_combox.getValue().toString());
                        //if path is not valid,reset as a Default Server
                        if (si == null || !new File(si.getPath()).isDirectory()) {
                            si = ServerInf.defaultServer();
                            server_name_combox.setValue(si);
                            throw new RuntimeException("路径已失效");
                        }

                        if(!Config.SERVER_NAME.equals(si.getName()))
                            Config.SERVER_NAME = si.getName();

                        if (!Config.getFormatURL().equals(si.getPath()) && !Config.PATH.equals(si.getPath())) {
                            Config.PATH = si.getPath();
                            MainController.stopServer();
                            cache.resetPath(Config.PATH);
                            cache.syncFromDisk();
                            MainController.startServer(cache);
                        }

                        if (!Config.SERVER_ADDRESS.equals(si.getAddress())) {
                            Config.SERVER_ADDRESS = si.getAddress();
                        }

                        if (Config.DOWNLOAD_THREAD_NUM != download_num_combox.getValue()) {
                            Config.DOWNLOAD_THREAD_NUM = download_num_combox.getValue();
                        }

                        Config.USE_RELATIVE_PATH = relative_path_togbtn.isSelected();

                        return s;
                    }).observeOn(Schedulers.io())
                    .map(any -> {
                        Config.save();
                        return any;
                    }).observeOn(JavaFxScheduler.platform())
                    .subscribe(new DisposableSingleObserver<Object>() {
                        @Override
                        public void onSuccess(Object o) {
                            close();
                        }

                        @Override
                        public void onError(Throwable e) {
                            String errorName = e.getMessage();
                            if(e instanceof Config.PathConvertException){
                                errorName = "错误:无法转化为绝对路径";
                                relative_path_togbtn.setSelected(false);
                            }else if(e instanceof IOException){
                                errorName = "错误:存储时发生了错误";
                            }


                            snackbar.show(errorName, 2000);

                            logger.warning(e.toString());
                            e.printStackTrace();
                        }
                    });

            });

    }

    public void updateServers(ServersCache serversCache){
        ServerInf serverInf = serversCache.getServerInfByName(Config.SERVER_NAME);

        if(serverInf == null) {
            serverInf = ServerInf.defaultServer();
            serversCache.getList().add(serverInf);
        }

        ObservableList<ServerInf> nameList = FXCollections.observableArrayList(serversCache.getList());
        server_name_combox.setItems(nameList);
        server_name_combox.setValue(serverInf);
    }

    public void setDialog(JFXAlert dialog) {
        this.dialog = dialog;
    }

    public void close(){
        if(!Objects.isNull(dialog)){
            dialog.close();
        }
    }

    @Override
    public void postInitialize() {

    }
}
