package com.software.api.Managers;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public enum  StageManager {
    INSTANCE;
    Map<String,Stage> controllers;

    StageManager(){
        controllers = new HashMap<>();
    }

    public void register(){

    }

    public void remove(){

    }

    public Stage create(Stage stage,Parent node,String title,String css, int width,int height){
        Stage newStage = newStage(stage,node,title,css,width,height);
        register();
        return newStage;
    }

    public Stage create(Parent node,String title,String css, int width,int height){
        Stage stage = newStage(null,node,title,css,width,height);
        register();
        return stage;
    }

    private Stage newStage(Stage stage,Parent node,String title,String css, int width,int height){
        if(stage == null)  stage=new Stage();

        Scene scene=new Scene(node,width,height);
        scene.getStylesheets().add(css);

        stage.setScene(scene);
        stage.setTitle(title);

        return stage;
    }
}
