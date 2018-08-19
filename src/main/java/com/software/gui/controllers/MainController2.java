package com.software.gui.controllers;

import com.jfoenix.controls.JFXAlert;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTabPane;
import com.rxcode.rxdownload.api.ANY;
import com.software.beans.Mod;
import com.software.gui.Config;
import com.software.gui.logic.DirInfoCache;
import com.software.gui.logic.FileWatchServer;
import com.software.gui.utils.UIString;
import com.software.gui.utils.VersionCompareHelper;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableMaybeObserver;
import io.reactivex.schedulers.Schedulers;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.DialogEvent;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class MainController2 implements Initializable {

    Logger logger = Logger.getLogger(getClass().getSimpleName());

    @FXML
    private Pane root;
    @FXML
    private JFXButton setting_btn;

    private static Image[] images;

    private static DirInfoCache cache = new DirInfoCache(Config.PATH);

    private static FileWatchServer server;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        try {
            Image image = new Image(getClass().getClassLoader().getResourceAsStream("images/setting.png"));
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(18);
            imageView.setFitHeight(18);
            setting_btn.setGraphic(imageView);

        } catch (Exception e) {
            logger.throwing(getClass().getSimpleName(),"initialize",e);
        }

        setting_btn.setDisableVisualFocus(true);
        setting_btn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            Node content;
            SettingDialogController controller;
            JFXAlert dialog;
            @Override
            public void handle(MouseEvent event) {
                try {
                    if(content == null) {
                        FXMLLoader loader = new FXMLLoader();
                        loader.setLocation(Objects.requireNonNull(getClass().getClassLoader().getResource("setting_dialog.fxml")));
                        content = loader.load();
                        controller = loader.getController();
                        Stage stage = (Stage) setting_btn.getScene().getWindow();
                        dialog = new JFXAlert(stage);
                        dialog.setContent(content);
                        dialog.initModality(Modality.APPLICATION_MODAL);
                        dialog.setOverlayClose(true);
                        controller.setDialog(dialog);
                    }
                    dialog.show();
                } catch (IOException e) {
                    logger.throwing(getClass().getSimpleName(),"onClick",e);
                }
            }
        });

        startServer();
        initImageSources();
        cache.syncFromDisk();
    }

    public static DirInfoCache getCache() {
        return cache;
    }

    private static void initImageSources(){
        Maybe.just(ANY.product())
                .observeOn(Schedulers.io())
                .map(new Function<ANY, ANY>() {
                    @Override
                    public ANY apply(ANY any) throws Exception {
                        images = new Image[8];
                        images[Mod.MODE.FORGE] = new Image(MainController2.class.getClassLoader().getResourceAsStream("images/forge_mark.png"));
                        images[Mod.MODE.SPONGE] = new Image(MainController2.class.getClassLoader().getResourceAsStream("images/spongie_mark.png"));
                        images[Mod.MODE.LITE] = new Image(MainController2.class.getClassLoader().getResourceAsStream("images/litemod_mark.png"));
                        images[Mod.MODE.BUKKIT] = new Image(MainController2.class.getClassLoader().getResourceAsStream("images/bukkit_mark.png"));
                        images[Mod.MODE.TWEAK] = new Image(MainController2.class.getClassLoader().getResourceAsStream("images/tweaker_mark.png"));
                        images[Mod.MODE.UNKNOWN_MOD_MODE] = new Image(MainController2.class.getClassLoader().getResourceAsStream("images/unknown_mark.png"));

                        return any;
                    }
                }).subscribe(new DisposableMaybeObserver<ANY>() {
            @Override
            public void onSuccess(ANY any) {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });

    }

    public static Image[] getImages(){
        return images;
    }

    public static void stopServer(){
        server.clearCache();
        server.interrupt();
    }

    public static void startServer(){
        server = null;
        server = new FileWatchServer(cache);
        server.setDaemon(true);
        server.start();
    }
}
