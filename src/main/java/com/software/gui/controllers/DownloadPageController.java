package com.software.gui.controllers;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.jfoenix.controls.*;
import com.jfoenix.utils.JFXNodeUtils;
import com.rxcode.rxdownload.DownloadConfig;
import com.rxcode.rxdownload.api.RxCarrier;
import com.rxcode.rxdownload.obervables.DTask;
import com.rxcode.rxdownload.obervables.DownloadInfo;
import com.software.beans.*;
import com.software.gui.logic.DirInfoCache;
import com.software.gui.logic.NLCompareRecorder;
import com.software.gui.utils.UIString;
import io.reactivex.FlowableSubscriber;
import io.reactivex.functions.Action;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ScrollToEvent;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.reactivestreams.Subscription;

import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.net.SocketException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Logger;

public class DownloadPageController implements Initializable {

    @FXML
    private AnchorPane root;

    private Logger logger = Logger.getLogger(getClass().getSimpleName());
    @FXML
    private JFXButton sync_btn;
    @FXML
    private JFXListView<DTask> download_listview;

    private JFXSnackbar snackbar;

    private NLCompareRecorder recorder;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        snackbar = new JFXSnackbar(root);
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.LIGHTGREY);
        snackbar.setEffect(shadow);
        snackbar.setPrefWidth(250);

        recorder = new NLCompareRecorder();
        recorder.bindCache(MainController2.getCache());
        download_listview.getStylesheets()
                .add(Objects.requireNonNull(getClass().getClassLoader().getResource("css/download_listview.css")).toExternalForm());
        download_listview.setCellFactory(param -> {
            DownloadTaskCell cell = new DownloadTaskCell();
            cell.prefWidthProperty().bind(download_listview.widthProperty().subtract(15));
            return cell;
        });
        download_listview.setDepth(1);
        initSyncBtn();
    }

    private void initSyncBtn(){
        sync_btn.setOnMouseClicked(new EventHandler<MouseEvent>() {

            Subscription subscription = null;
            @Override
            public void handle(MouseEvent event) {
                if(!MainController2.getCache().isAddedFilesEmpty() && subscription == null)
                {
                    snackBarShow(UIString.download_snack_bar_check);
                    return;
                }

                if(subscription == null ) {
                    try {
                        recorder.syncMods(new NLCompareRecorder.DownloadTaskCallBack() {

                            @Override
                            public void getList() {
                                snackBarShow(UIString.download_snack_bar_pull_list_success);
                            }

                            @Override
                            public void getTask(List<DTask> tasks) {
                                Platform.runLater(() -> {
                                    download_listview.getItems().clear();
                                    snackBarShow(UIString.download_snack_bar_no_item_update);

                                    download_listview.getItems().addAll(tasks);
                                });
                            }
                        })
                                .doOnCancel(new Action() {
                                    @Override
                                    public void run() throws Exception {
                                        sync_btn.setText(UIString.download_sync_btn_sync);
                                        download_listview.refresh();
                                    }
                                })
                                .doFinally(new Action() {
                                    @Override
                                    public void run() throws Exception {
                                        subscription = null;
                                    }
                                })
                                .subscribe(new FlowableSubscriber<RxCarrier>() {
                            DecimalFormat df = new DecimalFormat("0.00");

                            @Override
                            public void onSubscribe(Subscription s) {
                                sync_btn.setText(UIString.download_sync_btn_stop);
                                s.request(Integer.MAX_VALUE);
                                df.setRoundingMode(RoundingMode.HALF_UP);
                                snackBarShow(UIString.download_snack_bar_start_pull_list);
                                subscription = s;
                            }

                            @Override
                            public void onNext(RxCarrier o) {
                                DownloadInfo downloadInfo = (DownloadInfo) o;

                                download_listview.refresh();
                                if (downloadInfo.getDownloadStatus() == DownloadInfo.DownloadStatus.DOWNLOAD_FINISHED) {
                                    logger.info(downloadInfo.getData().toString() + " saved");
                                    TransMod mod = (TransMod) downloadInfo.getData();
                                    AbstractMod abstractMod = new ServerMod();
                                    abstractMod.setId(mod.getId());
                                    abstractMod.setVersion(mod.getVersion());
                                    abstractMod.setFilePath(DownloadConfig.getAbsolutePath(downloadInfo.getRealFileName()));

                                    try {
                                        abstractMod.setFileMD5(Files.asByteSource(new File(abstractMod.getFilePath())).hash(Hashing.goodFastHash(128)));
                                    } catch (IOException e) {
                                        logger.warning(abstractMod.getId() + " lost MD5");
                                    }

                                    //To fix:server mod can't get type.
                                    abstractMod.setMode(Mod.MODE.UNKNOWN_MOD_MODE);

                                    WrapperMod wrapperMod = WrapperMod.create(abstractMod);
                                    MainController2.getCache().addNewModAndCheck(wrapperMod);

                                } else if (downloadInfo.getDownloadStatus() == DownloadInfo.DownloadStatus.DOWNLOADING) {
                                    logger.info((downloadInfo.getProgress() / 100d) + ", " + df.format(downloadInfo.getDownloadSpeed()) + "kb/s");

                                } else if (downloadInfo.getDownloadStatus() == DownloadInfo.DownloadStatus.DOWNLOAD_FAILED) {
                                    snackBarShow(downloadInfo.getRealFileName() + "下载失败:"+downloadInfo.getThrowable().toString());
                                    logger.throwing(getClass().getCanonicalName(), "OnNext", downloadInfo.getThrowable());
                                }
                            }

                            @Override
                            public void onError(Throwable t) {
                                logger.info(t.toString());
                                sync_btn.setText(UIString.download_sync_btn_sync);
                                String errorName = "错误：" + t.getMessage();
                                if (t instanceof SocketException) {
                                    errorName = "网络连接出现问题：" + t.getMessage();
                                } else if (t.getClass() == IOException.class) {
                                    errorName = "存储时出错：" + t.getMessage();
                                }
                                snackBarShow(errorName);
                            }

                            @Override
                            public void onComplete() {
                                sync_btn.setText(UIString.download_sync_btn_sync);
                                download_listview.refresh();
                                try {
                                    MainController2.getCache().sync2Disk(DirInfoCache.FILE.INFORMATION);
                                } catch (IOException e) {
                                    logger.throwing(getClass().getCanonicalName(), "OnComplete", e);
                                    snackBarShow(UIString.download_snack_bar_sync_finished);
                                }
                            }
                        });
                    } catch (Exception e) {
                        throw new RuntimeException("not bind cache");
                    }
                }else {
                    subscription.cancel();
                    subscription = null;
                    download_listview.getItems().forEach(new Consumer<DTask>() {
                        @Override
                        public void accept(DTask task) {
                            if(task.getDownloadInfo().getDownloadStatus() == DownloadInfo.DownloadStatus.DOWNLOADING) {
                                task.getDownloadInfo().setDownloadStatus(DownloadInfo.DownloadStatus.DOWNLOAD_FORCE_STOP);
                                task.getDownloadInfo().setDownloadSpeed(0);
                            }
                        }
                    });
                    download_listview.getItems().removeIf(new Predicate<DTask>() {
                        @Override
                        public boolean test(DTask task) {
                            return task.getDownloadInfo().getDownloadStatus() != DownloadInfo.DownloadStatus.DOWNLOADING;
                        }
                    });
                    download_listview.refresh();
                }
            }
        });

    }

    private void snackBarShow(String msg){
        snackbar.fireEvent(new JFXSnackbar.SnackbarEvent(msg, "关闭", 2000, false, new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                snackbar.close();
            }
        }));
        //snackbar.show(msg,1500);
    }

    private class DownloadTaskCell extends JFXListCell<DTask> {

        private  DownloadCellController listViewCellController;
        private FXMLLoader loader = new FXMLLoader();

        private void initListViewCell(){

            loader.setLocation(getClass().getClassLoader().getResource("download_listview_cell.fxml"));
            try {
                loader.load();
                listViewCellController = loader.getController();
            } catch (IOException e) {
                e.printStackTrace();
            }

            cellRippler = new DownloadTaskCell.FixRippler(this);
        }

        DownloadTaskCell(){
            super();
            initListViewCell();
        }

        @Override
        protected void updateItem(DTask item, boolean empty) {
            super.updateItem(item, empty);
            //avoid the modify by super-class -- text will be draw with the method :item.toString()
            setText(null);
            //release ripple if possible
            ((FixRippler)cellRippler).releaseRipple();

            if(!empty && item != null ){
                listViewCellController.setState(item.getDownloadInfo().getDownloadStatus().getName());
                listViewCellController.setTitle(((TransMod)item.getDownloadInfo().getData()).getId()+"-"+((TransMod)item.getDownloadInfo().getData()).getVersion());
                listViewCellController.setProgress(item.getDownloadInfo().getProgress()/10000d);
                if(item.getDownloadInfo().getDownloadStatus() == DownloadInfo.DownloadStatus.DOWNLOADING){
                    listViewCellController.setSpeed(item.getDownloadInfo().getDownloadSpeed());
                }else{
                    listViewCellController.setSpeedDisable();
                }
                Node pane = listViewCellController.getRoot();
                setGraphic(pane);
            }else{
                setGraphic(null);
            }

        }

        //Make the release() in fx-api public so that when listView update and recycle an item the item can stop the animation soon
        //see the method updateItem(...),after super(...)method,it's necessary to release the animation or the rippler will make a
        //residual background(this just appened when listView add an item meanwhile user is click an item,the animation can't auto stop)
        private class FixRippler extends JFXRippler {
            protected Node getMask() {
                Region clip = new Region();
                JFXNodeUtils.updateBackground(DownloadTaskCell.this.getBackground(), clip);
                double width = control.getLayoutBounds().getWidth();
                double height = control.getLayoutBounds().getHeight();
                clip.resize(width, height);
                return clip;
            }

            @Override
            protected void positionControl(Node control) {
                // do nothing
            }

            public FixRippler(Node control){
                super(control);
            }

            @Override
            public void releaseRipple() {
                super.releaseRipple();
            }
        }
    }
}
