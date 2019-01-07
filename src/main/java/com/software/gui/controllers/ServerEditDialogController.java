package com.software.gui.controllers;

import com.google.common.base.Strings;
import com.jfoenix.controls.JFXAlert;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.validation.RequiredFieldValidator;
import com.jfoenix.validation.base.ValidatorBase;
import com.software.gui.Config;
import com.software.gui.controllers.beans.ServerInf;
import com.software.gui.logic.CacheManager;
import com.software.gui.logic.ServersCache;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

public class ServerEditDialogController implements Initializable {

    private Logger logger = Logger.getLogger(getClass().getSimpleName());

    @FXML
    public JFXTextField address_tv;
    @FXML
    public JFXTextField path_tv;
    @FXML
    public JFXButton save_btn;
    @FXML
    public JFXButton cancel_btn;
    @FXML
    public JFXButton travel_btn;
    @FXML
    public JFXTextField name_tv;
    @FXML
    public AnchorPane root;

    private JFXAlert dialog;

    private EventHandler actionEvent;


    private String address;
    private int port;
    private static String IP_REX = "((?:(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d))))";
    private static String  DOMAIN_NAME_REX = "^(?=^.{3,255}$)(http(s)?://)?(www\\.)?[a-zA-Z0-9][-a-zA-Z0-9]{0,62}(\\.[a-zA-Z0-9][-a-zA-Z0-9]{0,62})+(:\\d+)*(/\\w+\\.\\w+)*$";
    private ServerInf serverInf;
    private int type = 0;//0: new a server 1:edit a server

    private boolean checkAddress(String address){
        if(Strings.isNullOrEmpty(address)) return false;
        String[] temp = address.split(":");

        if(temp.length > 3)
            return false;

        String sPort = "";
        if(temp.length > 1){

            sPort = temp[temp.length - 1].trim();
            try {
                this.port = Integer.parseInt(sPort);
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        if(serverInf != null) {
            name_tv.setText(serverInf.getName());
            address_tv.setText(serverInf.getAddress());
            path_tv.setText(serverInf.getPath());
        }

        RequiredFieldValidator requiredFieldValidator = new RequiredFieldValidator();
        requiredFieldValidator.setMessage("内容为空");

        path_tv.getValidators().add(requiredFieldValidator);
        path_tv.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                path_tv.validate();
            }
        });

        NameValidator nameValidator = new NameValidator();
        nameValidator.setMessage("名称已存在");
        name_tv.getValidators().addAll(requiredFieldValidator,nameValidator);
        name_tv.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                name_tv.validate();
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

        cancel_btn.setOnMouseClicked(event -> {close();});

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

        save_btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (address_tv.validate() && path_tv.validate()&& name_tv.validate()){
                    ServerInf serverInf = new ServerInf();
                    serverInf.setName(name_tv.getText());
                    serverInf.setAddress(address_tv.getText());
                    serverInf.setPath(path_tv.getText());

                    ServerEvent serverEvent = new ServerEvent(serverInf);
                    actionEvent.handle(serverEvent);
                    close();
                }
            }
        });
    }

    public void close(){
        if(!Objects.isNull(dialog)){
            dialog.close();
        }
    }

    public void setContent(@NonNull ServerInf serverInf){
        this.serverInf = serverInf;
        name_tv.setText(serverInf.getName());
        address_tv.setText(serverInf.getAddress());
        path_tv.setText(serverInf.getPath());
        type = 1;
    }

    public void setDialog(JFXAlert dialog) {
        this.dialog = dialog;
    }

    public void setSuccessActionEventHandler(EventHandler actionEvent) {
        this.actionEvent = actionEvent;
    }

    private class NameValidator extends ValidatorBase {

        private boolean findSameName(String name){
            return ((ServersCache) CacheManager.INSTANCE.getCache(1)).getServerInfByName(name) != null;
        }

        @Override
        protected void eval() {
            if(srcControl.get() instanceof TextInputControl){
                TextInputControl textInputControl = (TextInputControl) srcControl.get();
                if((type == 0 && findSameName(textInputControl.getText())) ||
                        (type ==1 && (findSameName(textInputControl.getText()) && !serverInf.getName().equals(textInputControl.getText())))){
                    hasErrors.set(true);
                }else {
                    hasErrors.set(false);
                }
            }
        }
    }

    private class AddressValidator extends ValidatorBase {
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

    public class ServerEvent extends ActionEvent{

        private ServerInf serverInf;

        public ServerEvent(ServerInf serverInf) {
            super();
            this.serverInf = serverInf;
        }

        public ServerInf getServerInf() {
            return serverInf;
        }
    }
}
