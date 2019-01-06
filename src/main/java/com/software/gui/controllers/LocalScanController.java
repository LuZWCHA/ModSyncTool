package com.software.gui.controllers;

import com.google.common.collect.Lists;
import com.jfoenix.controls.*;
import com.jfoenix.utils.JFXNodeUtils;
import com.software.beans.AbstractMod;
import com.software.beans.TransMod;
import com.software.beans.WrapperMod;
import com.software.gui.controllers.cells.ListViewCellController;
import com.software.gui.logic.CacheManager;
import com.software.gui.logic.DirInfoCache;
import com.software.gui.scheduler.JavaFxScheduler;
import com.software.gui.utils.UIString;
import com.software.scan.JarsView;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class LocalScanController implements Initializable {
    @FXML
    private AnchorPane root;

    private Logger logger = Logger.getLogger(getClass().getSimpleName());

    @FXML
    private Text scan_tip_text;
    @FXML
    private JFXProgressBar scan_progress_bar;
    @FXML
    private JFXButton scan_btn;
    @FXML
    private JFXListView<TransMod> scan_listview;

    private JFXSnackbar snackbar;

    private JarsView jarsView;

    private Subscriber<WrapperMod> subscriber;

    private ScanCache lastScanResult = ScanCache.NONE;

    private enum ScanCache{
        COMPLETE,
        ERROR,
        FORE_STOP,
        NONE
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        DirInfoCache cache = CacheManager.INSTANCE.getCache(0);
        snackbar = new JFXSnackbar(root);
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.LIGHTGREY);
        snackbar.setEffect(shadow);
        snackbar.setPrefWidth(250);

        jarsView = JarsView.create();
        scan_btn.setDisable(true);
        scan_progress_bar.setVisible(false);
        scan_tip_text.setText(UIString.scan_tip_text_ready);
        scan_progress_bar.visibleProperty().bind(scan_btn.disabledProperty().not());
        cache.addAddedFilesListener((observable, oldValue, newValue) -> scan_btn.setDisable(newValue));

        scan_listview.setCellFactory(param -> {
            TransModCell cell = new TransModCell();

            ContextMenu contextMenu = new ContextMenu();
            Insets insets = new Insets(12,32,12,32);
            JFXButton button0 = new JFXButton();
            button0.setPadding(insets);
            button0.setRipplerFill(Color.LIGHTGREY);
            button0.setButtonType(JFXButton.ButtonType.RAISED);

            MenuItem editItem = new CustomMenuItem(button0);
            editItem.setOnAction(event -> {
                boolean set = cell.getItem().isFromServer();
                scan_listview.getSelectionModel().getSelectedItems().forEach(mod -> mod.setFromServer(!set));
                scan_listview.refresh();
            });

            JFXButton button = new JFXButton(UIString.delete2);
            button.setRipplerFill(Color.LIGHTGREY);
            button.setPadding(insets);
            button.setButtonType(JFXButton.ButtonType.RAISED);

            MenuItem deleteItem = new CustomMenuItem(button);

            deleteItem.setOnAction(event -> {
                Lists.newArrayList(scan_listview.getSelectionModel().getSelectedItems())
                        .forEach(new Consumer<TransMod>() {
                            @Override
                            public void accept(TransMod mod) {
                                try {
                                    cache.removeMod((AbstractMod) mod,true);
                                }catch (Exception e){
                                    logger.warning("delete failed:" + e.getMessage());
                                }
                            }
                        });
            });
            contextMenu.getItems().addAll(editItem,deleteItem);

            cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                if (isNowEmpty) {
                    cell.setContextMenu(null);
                } else {
                    cell.setContextMenu(contextMenu);
                    cell.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
                        @Override
                        public void handle(ContextMenuEvent event) {
                            button0.setText(cell.getItem().isFromServer() ? "设置为客户端模组":"设置为服务端模组");
                        }
                    });
                }
            });
            cell.prefWidthProperty().bind(scan_listview.widthProperty().subtract(15 + scan_listview.getPadding().getLeft() + scan_listview.getPadding().getRight()));
            return cell;
        });
        scan_listview.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        scan_listview.setItems(cache.getTransMods());

        scan_btn.setOnMouseClicked(event -> {
            if(jarsView.isScanning()) {
                scan_btn.setText(UIString.scanbtn_scan);
                jarsView.stop();
                lastScanResult = ScanCache.FORE_STOP;
            }
            else {
                scan_btn.setText(UIString.scanbtn_pause);
                jarsView.setFiles(new HashSet<>(cache.getAddedFiles()))
                        .observeOn(JavaFxScheduler.platform())
                        .view(subscriber);

            }
        });

        subscriber = new Subscriber<WrapperMod>() {

            private int scanNum = 0;
            private int fileTotalNum = 0;
            @Override
            public void onSubscribe(Subscription s) {
                fileTotalNum = cache.getAddedFiles().size();
                scanNum = 0;
                scan_progress_bar.setProgress(-1d);
                scan_tip_text.setText(UIString.scan_tip_text_scan_start);
                scan_btn.setText(UIString.scanbtn_pause);
                snackBarShow("开始扫描");
            }


            @Override
            public void onNext(WrapperMod mod) {
                if(!mod.isEmpty()) {
                    cache.addNewModAndCheck(mod);
                }else {
                    cache.addToIgnoreAndCheck(mod.getFilePath());
                }
                scanNum++;
                scan_progress_bar.setProgress(scanNum/(double)fileTotalNum);
                scan_tip_text.setText(scanNum+"/"+fileTotalNum);
                int size = scan_listview.getItems().size();
                scan_listview.scrollTo(size > 0 ? size - 1:0);
            }

            @Override
            public void onError(Throwable t) {
                logger.throwing(getClass().getSimpleName(),"onError",t);
                lastScanResult = ScanCache.ERROR;
                scan_btn.setText(UIString.scanbtn_scan);
                scan_progress_bar.setVisible(false);
                scan_tip_text.setText(UIString.scan_tip_text_failed+" "+UIString.has_finided+" "+scanNum+"/"+fileTotalNum);
                snackBarShow("错误："+t.toString());
            }

            @Override
            public void onComplete() {
                logger.info("scan finished");
                try {
                    cache.sync2Disk(DirInfoCache.FILE.BOTH);
                    lastScanResult = ScanCache.COMPLETE;
                } catch (IOException e) {
                    e.printStackTrace();
                    lastScanResult = ScanCache.ERROR;
                }
                scan_btn.setText(UIString.scanbtn_scan);
                snackBarShow("扫描结束");
                scan_progress_bar.setProgress(0);
                scan_tip_text.setText(UIString.scan_tip_text_success);
            }
        };
    }

    private void snackBarShow(String msg){
        snackbar.fireEvent(new JFXSnackbar.SnackbarEvent(msg));
    }

    private class TransModCell extends JFXListCell<TransMod> {

        private ListViewCellController listViewCellController;
        private FXMLLoader loader = new FXMLLoader();
        private void initListViewCell(){

            loader.setLocation(getClass().getClassLoader().getResource("listview_cell.fxml"));
            try {
                loader.load();
                listViewCellController = loader.getController();
            } catch (IOException e) {
                logger.throwing(getClass().getSimpleName(),"initListViewCell",e);
            }

            cellRippler = new TransModCell.FixRippler(this);
        }

        TransModCell(){
            super();
            initListViewCell();
        }

        @Override
        public void updateSelected(boolean selected) {
            if(!selected){
                listViewCellController.setMainTextColor(Color.valueOf("#484848"));
                listViewCellController.setSubTextColor(Color.DARKGRAY);
            }else {
                listViewCellController.setMainTextColor(Color.WHITE);
                listViewCellController.setSubTextColor(Color.WHITE);
            }
            super.updateSelected(selected);
        }

        @Override
        protected void updateItem(TransMod item, boolean empty) {
            super.updateItem(item, empty);
            //avoid the modify by super-class -- text will be draw with the method :item.toString()
            setText(null);
            //release ripple if possible
            ((FixRippler)cellRippler).releaseRipple();

            if(!empty && item != null ){
                listViewCellController.setMainText(item.getId());
                listViewCellController.setSubtext(UIString.version + ":" + item.getVersion());
                listViewCellController.setCell_imageview(MainController2.getImages()[item.getMode()]);
                listViewCellController.setMarkText(item.isFromServer() ? "SERVER":"CLIENT");
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
                JFXNodeUtils.updateBackground(TransModCell.this.getBackground(), clip);
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
