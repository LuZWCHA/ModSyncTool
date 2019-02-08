package com.software.gui.controllers;

import com.jfoenix.animation.alert.JFXAlertAnimation;
import com.jfoenix.controls.JFXAlert;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXSnackbar;
import com.rxcode.rxdownload.obervables.DownloadInfo;
import com.software.beans.jsonbean.VersionData;
import com.software.gui.logic.UpdaterManager;
import com.software.gui.scheduler.JavaFxScheduler;
import com.software.gui.utils.AppInfo;
import com.software.gui.utils.FileHelper;
import com.software.gui.utils.UIString;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.DialogEvent;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.StageStyle;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class AboutDialogController implements Initializable {
    @FXML
    public Label current_vs_lab;
    @FXML
    public Label vs_des_lab;
    @FXML
    public JFXButton check_update_btn;
    @FXML
    public AnchorPane root;

    private JFXAlert alert;
    private JFXDialogLayout layout;
    private Label label = new Label();
    private JFXSnackbar snackbar;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        snackbar = new JFXSnackbar(root);
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.LIGHTGREY);
        snackbar.setEffect(shadow);
        snackbar.setPrefWidth(250);

        current_vs_lab.setText(AppInfo.VERSION);
        vs_des_lab.setText(AppInfo.VERSION_DESCRIPTION);
        check_update_btn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                UpdaterManager.INSTANCE.checkUpdate()
                        .observeOn(JavaFxScheduler.platform())
                        .map(new Function<VersionData, Object>() {
                            @Override
                            public Object apply(VersionData versionData) throws Exception {
                                if(!versionData.isEmpty()){
                                    //has new version
                                    showGlobalDialog("发现新版本："+versionData.getVersion()+"，是否更新？",
                                            new EventHandler() {
                                                @Override
                                                public void handle(Event event) {
                                                    Disposable disposable = UpdaterManager.INSTANCE.downloadNewVersion(versionData.getVersion())
                                                            .doFinally(() -> {
                                                                if(alert != null && alert.isShowing())
                                                                    alert.close();
                                                            })
                                                            .observeOn(JavaFxScheduler.platform())
                                                            .subscribe(rxCarrier -> {

                                                                DownloadInfo downloadInfo = (DownloadInfo) rxCarrier;
                                                                if(downloadInfo.getDownloadStatus() == DownloadInfo.DownloadStatus.DOWNLOAD_FINISHED) {
                                                                    label.setText("删除:" + FileHelper.getJarPath());
                                                                    ProcessBuilder pb = new ProcessBuilder("java", "-jar", downloadInfo.getRealFileName(),
                                                                            "-d",FileHelper.getJarPath());
                                                                    pb.directory(new File(downloadInfo.getDownloadPath()));
                                                                    Process p = pb.start();
                                                                    System.exit(0);
                                                                }
                                                                else if(downloadInfo.getDownloadStatus() == DownloadInfo.DownloadStatus.DOWNLOAD_FAILED){
                                                                    snackBarShow(downloadInfo.getThrowable().getMessage());
                                                                }
                                                                else if(downloadInfo.getDownloadStatus() == DownloadInfo.DownloadStatus.DOWNLOADING) {
                                                                    System.out.println(downloadInfo.getProgress());
                                                                    label.setText(downloadInfo.getProgress()+"/10000");
                                                                }
                                                            }, throwable -> snackBarShow(throwable.getMessage()));

                                                    if(label != null){
                                                        label.setText("开始下载");
                                                    }
                                                    alert.setOnCloseRequest(new EventHandler<DialogEvent>() {
                                                        @Override
                                                        public void handle(DialogEvent event) {
                                                            if(disposable != null && !disposable.isDisposed())
                                                                disposable.dispose();
                                                        }
                                                    });
                                                }
                                    });

                                }else {
                                    //it's the latest version
                                    showGlobalDialog("已经是最新版本",null);
                                }
                                return new Object();
                            }
                        }).subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        snackBarShow(throwable.getMessage());
                    }
                });
            }
        });
    }

    private void snackBarShow(String msg){
        snackbar.fireEvent(new JFXSnackbar.SnackbarEvent(msg, "关闭", 3000, false, new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                snackbar.close();
            }
        }));
    }

    private void showGlobalDialog(String content,EventHandler eventHandler){
        alert = new JFXAlert();

        alert.initModality(Modality.APPLICATION_MODAL);
        alert.initStyle(StageStyle.TRANSPARENT);

        JFXButton button = new JFXButton(UIString.cancel1);
        JFXButton button1 = new JFXButton(UIString.ok1);

        button.setTextFill(Color.WHITE);
        button.setButtonType(JFXButton.ButtonType.RAISED);
        button.setDisableVisualFocus(true);
        button.setOnAction(event -> alert.close());

        button1.setTextFill(Color.WHITE);
        button1.setButtonType(JFXButton.ButtonType.RAISED);
        button1.setDisableVisualFocus(true);
        button1.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(eventHandler == null) {
                    alert.close();
                }else {
                    eventHandler.handle(event);
                    button1.setDisable(true);
                }
            }
        });

        alert.setOverlayClose(false);
        alert.setHideOnEscape(false);

        if(layout == null)
            layout = new JFXDialogLayout();

        label.setText(content);
        label.setStyle("-fx-font-size: 13;-fx-text-fill: white;");
        layout.setBody(new VBox(label));
        layout.setActions(button,button1);
        layout.setBackground(new Background(new BackgroundFill(Color.GRAY,CornerRadii.EMPTY,Insets.EMPTY)));
        alert.setAnimation(JFXAlertAnimation.RIGHT_ANIMATION);
        alert.setContent(layout);

        alert.show();
    }
}
