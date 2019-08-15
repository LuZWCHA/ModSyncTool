import com.jfoenix.animation.alert.JFXAlertAnimation;
import com.jfoenix.controls.JFXAlert;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDecorator;
import com.jfoenix.controls.JFXDialogLayout;
import com.software.gui.Config;
import com.software.api.AppInfo;
import com.software.api.Managers.CacheManager;
import com.software.api.Managers.ControllerManager;
import com.software.api.Managers.StageManager;
import com.software.api.SyncContext;
import com.software.gui.controllers.AboutDialogController;
import com.software.gui.controllers.MyDecorator;
import com.software.gui.controllers.SettingDialogController;
import com.software.gui.logic.DirInfoCache;
import com.software.gui.logic.ServersCache;
import com.software.gui.utils.DrawUtil;
import com.software.gui.utils.UIString;
import com.software.gui.utils.VersionCompareHelper;
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.plugins.RxJavaPlugins;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Created by 陆正威 on 2018/7/11.
 */
public class ScanMain extends Application {

    private static String NEED_JAVA_VERSION = "1.8.0_60";
    private  static Logger logger = Logger.getLogger(ScanMain.class.getSimpleName());
    private SyncContext context;

    public static void main(String[] args) {
        //to avoid the UndeliverableException or any other error in the rxjava chain(cause crash)
        RxJavaPlugins.setErrorHandler(e -> {
            if (e instanceof UndeliverableException) {
                e = e.getCause();
            }
            if (e instanceof IOException) {
                // fine, irrelevant network problem or API that throws on cancellation
                return;
            }
            if (e instanceof InterruptedException) {
                // fine, some blocking code was interrupted by a dispose call
                return;
            }
            if ((e instanceof NullPointerException) || (e instanceof IllegalArgumentException)) {
                // that's likely a bug in the application
                return;
            }
            if (e instanceof IllegalStateException) {
                // that's a bug in RxJava or in a custom operator
                return;
            }
            //Log.warning("Undeliverable exception received, not sure what to do", e);
        });



        try {
            String path;
            if(args.length>1 && "-d".equals(args[0]) && args[1] != null) {
                path = args[1].trim();

                File oldVersion = new File(path);
                oldVersion.delete();
            }
        } catch (Exception e) {
            logger.warning(e.getMessage());
        }

        launch(args);
    }


    @Override
    public void start(Stage primaryStage) throws Exception {
        String javaVersion = System.getProperty("java.version");
        AppInfo.VERSION = "1.5";
        AppInfo.VERSION_DESCRIPTION = "1.5";
        context = new SyncContext(ControllerManager.INSTANCE,CacheManager.INSTANCE);

        construction(AppInfo.VERSION,javaVersion);

        if(VersionCompareHelper.compareVersion(NEED_JAVA_VERSION,javaVersion) > 0){
            JFXAlert alert = new JFXAlert();
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.initStyle(StageStyle.TRANSPARENT);
            JFXButton button = new JFXButton(UIString.ok1);
            button.setTextFill(Color.WHITE);
            button.setButtonType(JFXButton.ButtonType.RAISED);
            button.setDisableVisualFocus(true);
            button.setOnAction(event -> alert.close());
            alert.setOverlayClose(false);
            alert.setHideOnEscape(false);

            JFXDialogLayout layout = new JFXDialogLayout();
            Label label = new Label(String.format("当前版本为%s,请升级到Java%s及以上",javaVersion,NEED_JAVA_VERSION));
            label.setStyle("-fx-font-size: 13;-fx-text-fill: white;");
            layout.setBody(new VBox(label));
            layout.setActions(button);
            layout.setBackground(new Background(new BackgroundFill(Color.GRAY,CornerRadii.EMPTY,Insets.EMPTY)));
            alert.setTitle("Java版本错误");
            alert.setAnimation(JFXAlertAnimation.RIGHT_ANIMATION);
            alert.setContent(layout);

            alert.showAndWait();
            System.exit(-1);
        }

        try {
            Config.read();
        }catch (Exception e){
            logger.info("config read failed,skip");
        }

        CacheManager.INSTANCE.registerCache(CacheManager.Key.createKey(0,DirInfoCache.class),new DirInfoCache(Config.PATH));

        initialize(context);

        URL url = getClass().getResource("main.fxml");

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(url);
        Pane root = loader.load();

        JFXDecorator decorator = new MyDecorator(primaryStage,root,false,false,true);

        DrawUtil.setJFXDecorator(decorator,Color.valueOf("#F4371E"),Color.WHITE,UIString.main_full_screen,
                UIString.min_screen,UIString.max_screen,UIString.close_main);
        primaryStage.setTitle(UIString.main_title);

        Scene scene = new Scene(decorator,330,560);
        scene.getStylesheets().add(getClass().getResource("css/global.css").toExternalForm());
        primaryStage.setScene(scene);

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                CacheManager.INSTANCE.saveAll();
                close();
            }
        });

        ((MyDecorator) decorator).setBtnListener(new EventHandler<MouseEvent>() {
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
                        dialog = new JFXAlert(primaryStage);
                        dialog.setContent(content);
                        dialog.initModality(Modality.APPLICATION_MODAL);
                        dialog.setOverlayClose(true);
                        controller.setDialog(dialog);
                    }else{
                        ServersCache serversCache = CacheManager.INSTANCE.getCache(1);
                        controller.updateServers(serversCache);
                    }
                    dialog.show();
                } catch (IOException e) {
                    logger.throwing(getClass().getSimpleName(),"onClick",e);
                }
            }
        },new EventHandler<MouseEvent>() {
            Node content;
            AboutDialogController controller;
            Stage stage;
            @Override
            public void handle(MouseEvent event) {
                try {
                    if(content == null) {
                        FXMLLoader loader = new FXMLLoader();
                        loader.setLocation(Objects.requireNonNull(getClass().getClassLoader().getResource("about_dialog.fxml")));
                        content = loader.load();
                        controller = loader.getController();
                    }
                    if(stage == null){
                        stage = newStage(content);
                    }
                    if(!stage.isShowing())
                        stage.show();
                    if(!stage.isFocused())
                        stage.requestFocus();
                } catch (IOException e) {
                    logger.throwing(getClass().getSimpleName(),"onClick",e);
                }
            }
        });

        postInitialize();
        primaryStage.show();
    }

    private Stage newStage(Node node){
        Stage secondaryStage = new Stage();

        JFXDecorator decorator = new JFXDecorator(secondaryStage,node,false,false,true);

        DrawUtil.setJFXDecorator(decorator,Color.valueOf("#F4371E"),Color.WHITE,UIString.main_full_screen,
                UIString.min_screen,UIString.max_screen,UIString.close_main);

        return StageManager.INSTANCE.create(secondaryStage,decorator,UIString.main_title,
                getClass().getResource("css/global.css").toExternalForm(),400,450);
    }

    public void construction(String syncVersion,String javaVersion) {

    }

    public boolean initialize(SyncContext context) {
        return false;
    }

    public boolean postInitialize() {
        return false;
    }

    public boolean close() {
        return false;
    }

    public void crash() throws Throwable {

    }
}
