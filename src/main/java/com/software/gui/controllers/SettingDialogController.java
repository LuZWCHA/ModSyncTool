package com.software.gui.controllers;

import com.google.common.base.Strings;
import com.jfoenix.controls.*;
import com.jfoenix.validation.RequiredFieldValidator;
import com.jfoenix.validation.base.ValidatorBase;
import com.rxcode.rxdownload.api.ANY;
import com.software.gui.Config;
import com.software.gui.logic.CacheManager;
import com.software.gui.logic.DirInfoCache;
import com.software.gui.scheduler.JavaFxScheduler;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.BiConsumer;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextInputControl;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

public class SettingDialogController implements Initializable {
    private Logger logger = Logger.getLogger(getClass().getSimpleName());

    @FXML
    private AnchorPane root;
    @FXML
    private JFXComboBox<Integer> download_num_combox;
    @FXML
    private JFXToggleButton relative_path_togbtn;
    @FXML
    private JFXTextField path_tv;//
    @FXML
    private JFXTextField address_tv;//
    @FXML
    private JFXButton save_btn;
    @FXML
    private JFXButton cancel_btn;
    @FXML
    private JFXButton travel_btn;

    private JFXAlert dialog;

    private int port;

    private String address;

    private JFXSnackbar snackbar;

    private static String IP_REX = "((?:(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d))))";
    private static String  DOMAIN_NAME_REX = "^(?=^.{3,255}$)(http(s)?://)?(www\\.)?[a-zA-Z0-9][-a-zA-Z0-9]{0,62}(\\.[a-zA-Z0-9][-a-zA-Z0-9]{0,62})+(:\\d+)*(/\\w+\\.\\w+)*$";

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        ObservableList<Integer> comboxList = FXCollections.observableArrayList();
        Observable.<Integer>range(1,Runtime.getRuntime().availableProcessors() + 1)
                .collectInto(comboxList, List::add).subscribe();
        download_num_combox.setItems(comboxList);
        download_num_combox.setValue(Config.DOWNLOAD_THREAD_NUM);

        path_tv.setEditable(false);
        path_tv.setText(Config.PATH);
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.LIGHTGREY);
        snackbar = new JFXSnackbar(root);
        snackbar.setEffect(shadow);
        snackbar.setPrefWidth(200);

        relative_path_togbtn.setSelected(Config.USE_RELATIVE_PATH);
        RequiredFieldValidator requiredFieldValidator = new RequiredFieldValidator();
        requiredFieldValidator.setMessage("内容为空");

        path_tv.getValidators().add(requiredFieldValidator);
        path_tv.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                path_tv.validate();
            }
        });

        AddressValidator addressValidator = new AddressValidator();
        addressValidator.setMessage("地址格式错误");
        address_tv.getValidators().addAll(requiredFieldValidator,addressValidator);

        address_tv.focusedProperty().addListener(((observable, oldValue, newValue) -> {
            if(!newValue){
                address_tv.validate();
            }
        }));
        address_tv.setText(Config.SERVER_ADDRESS);

        cancel_btn.setOnMouseClicked(event -> {close();});
        save_btn.setOnMouseClicked(event -> {
            if (address_tv.validate() && path_tv.validate())
                Single.just(ANY.product())
                        .map(s -> {
                            if (!Config.getFormatURL().equals(path_tv.getText()) && !Config.PATH.equals(path_tv.getText())) {
                                Config.PATH = path_tv.getText();

                                DirInfoCache cache = CacheManager.INSTANCE.getCache(0);
                                MainController2.stopServer();
                                cache.resetPath(Config.PATH);
                                cache.syncFromDisk();
                                MainController2.startServer(cache);
                            }

                            if (!Config.SERVER_ADDRESS.equals(address)) {
                                Config.SERVER_ADDRESS = address;
                            }

                            if(Config.DOWNLOAD_THREAD_NUM != download_num_combox.getValue()){
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

        travel_btn.setOnMouseClicked(event -> {

            Task<File> task = new Task<File>() {
                @Override
                protected File call() throws Exception {
                    DirectoryChooser directoryChooser = new DirectoryChooser();
                    directoryChooser.setTitle("选择Mods文件夹");
                    File initFile = new File(Config.PATH).getParentFile();
                    if(!initFile.exists())
                        directoryChooser.setInitialDirectory( new File(Config.PATH));
                    else
                        directoryChooser.setInitialDirectory(initFile);
                    return directoryChooser.showDialog(travel_btn.getScene().getWindow());
                }
            };
            task.run();
            try {
                File file = task.get();
                if(file != null)
                    path_tv.setText(file.getAbsolutePath());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });

    }

    private boolean checkAddress(String address){
        if(Strings.isNullOrEmpty(address)) return false;
        String[] temp = address.split(":");

        if(temp.length > 3)
            return false;

        String sPort = "";
        if(temp.length > 1){

            sPort = temp[temp.length - 1].trim();
            try {
                port = Integer.parseInt(sPort);
                if(port<=0 || port>65535){
                    return false;
                }
            }catch (NumberFormatException e){
                return false;
            }
            if(temp.length == 3)
                address = temp[0] + ":" + temp[1];
            else
                address = temp[0];
        }
        this.address = address.trim();
        boolean result = address.matches(IP_REX) || address.matches(DOMAIN_NAME_REX);
        if(result){
            if(!sPort.isEmpty()){
                this.address += ":"+ port;
            }
        }
        return result;
    }

    public void setDialog(JFXAlert dialog) {
        this.dialog = dialog;
    }

    public void close(){
        if(!Objects.isNull(dialog)){
            dialog.close();
        }
    }

    private class AddressValidator extends ValidatorBase{
        AddressValidator(){
            super();
        }
        @Override
        protected void eval() {
            if(srcControl.get() instanceof TextInputControl){
                TextInputControl textInputControl = (TextInputControl) srcControl.get();
                if(!checkAddress(textInputControl.getText())){
                    hasErrors.set(true);
                }else {
                    hasErrors.set(false);
                }
            }
        }
    }
}
