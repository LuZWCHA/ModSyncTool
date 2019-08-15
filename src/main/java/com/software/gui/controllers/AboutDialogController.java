package com.software.gui.controllers;

import com.jfoenix.animation.alert.JFXAlertAnimation;
import com.jfoenix.controls.JFXAlert;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXSnackbar;
import com.rxcode.rxdownload.obervables.DownloadInfo;
import com.software.beans.jsonbean.VersionJsonData;
import com.software.api.AppInfo;
import com.software.api.SyncController;
import com.software.gui.scheduler.JavaFxScheduler;
import com.software.gui.utils.FileHelper;
import com.software.gui.utils.UIString;
import com.software.gui.utils.Updater;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
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

public class AboutDialogController implements SyncController {
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

    //垃圾代码看看就好(0_0),考虑把匿名函数内容拉出来
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
                check_update_btn.setDisable(true);
                check_update_btn.setText("获取中...");
                Updater.INSTANCE.checkUpdate()
                        .observeOn(JavaFxScheduler.platform())
                        .map(new Function<VersionJsonData, Object>() {
                            @Override
                            public Object apply(VersionJsonData versionJsonData) throws Exception {
                                if(!versionJsonData.isEmpty()){
                                    //has new version
                                    showGlobalDialog("发现新版本："+ versionJsonData.getVersion()+"，是否更新？",
                                            new EventHandler() {
                                                @Override
                                                public void handle(Event event) {
                                                    Disposable disposable =
                                                            Updater.INSTANCE.downloadNewVersion(versionJsonData.getVersion())
                                                                    .doFinally(() -> {
                                                                        if(alert != null && alert.isShowing())
                                                                            alert.close();
                                                                    })
                                                                    .observeOn(JavaFxScheduler.platform())
                                                                    .subscribe(rxCarrier -> {

                                                                        DownloadInfo downloadInfo = (DownloadInfo) rxCarrier;
                                                                        if(downloadInfo.getDownloadStatus() == DownloadInfo.DownloadStatus.DOWNLOAD_FINISHED) {
                                                                            label.setText("删除:" + FileHelper.getJarPath());
                                                                            //use jvm to delete old-version file
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
                        })
                        .subscribe(new Consumer<Object>() {
                            @Override
                            public void accept(Object o) throws Exception {
                                check_update_btn.setDisable(false);
                                check_update_btn.setText("检查版本");
                            }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        check_update_btn.setDisable(false);
                        check_update_btn.setText("检查版本");
                        snackBarShow(throwable.toString());
                    }
                });
            }
        });
    }

    private void snackBarShow(String msg){
        snackbar.fireEvent(new JFXSnackbar.SnackbarEvent(msg, "关闭", 3000, false,
                event -> snackbar.close()));
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

    @Override
    public void postInitialize() {

    }
}
