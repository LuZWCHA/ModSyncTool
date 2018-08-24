package com.software.gui.observablebeans;

import com.rxcode.rxdownload.obervables.DTask;
import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.beans.property.adapter.JavaBeanObjectProperty;
import javafx.beans.property.adapter.JavaBeanObjectPropertyBuilder;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.List;

public class DTaskObservableCallBack implements Callback<DTask,Observable[]> {
    /**
     * The <code>call</code> method is called when required, and is given a
     * single argument of type P, with a requirement that an object of type R
     * is returned.
     *
     * @param param The single argument upon which the returned value should be
     *              determined.
     * @return An object of type R that may be determined based on the provided
     * parameter value.
     */
    List<Property> properties = new ArrayList<>();
    @Override
    public Observable[] call(DTask arg0) {
        JavaBeanObjectProperty progress = null;
        JavaBeanObjectProperty state = null;
        JavaBeanObjectProperty speed = null;
        JavaBeanObjectProperty fileName = null;
        JavaBeanObjectProperty downloadInfo = null;
        try {
            downloadInfo= JavaBeanObjectPropertyBuilder.create()
                    .bean(arg0)
                    .name("downloadInfo")
                    .build();
            progress = JavaBeanObjectPropertyBuilder.create()
                    .bean(arg0.getDownloadInfo())
                    .name("progress")
                    .build();
            state = JavaBeanObjectPropertyBuilder.create()
                    .bean(arg0.getDownloadInfo())
                    .name("downloadStatus")
                    .build();
            speed = JavaBeanObjectPropertyBuilder.create()
                    .bean(arg0.getDownloadInfo())
                    .name("downloadSpeed")
                    .build();
            fileName = JavaBeanObjectPropertyBuilder.create()
                    .bean(arg0.getDownloadInfo())
                    .name("realFileName")
                    .build();
            // hack around loosing weak references .
            properties.add(downloadInfo);
            properties.add(progress);
            properties.add(state);
            properties.add(speed);
            properties.add(fileName);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return new Observable[] {downloadInfo,progress, state,speed,fileName};
    }
}
