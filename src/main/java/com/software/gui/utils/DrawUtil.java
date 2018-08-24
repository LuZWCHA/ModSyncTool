package com.software.gui.utils;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDecorator;
import com.jfoenix.svg.SVGGlyph;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Tooltip;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * 拉伸工具类
 * @author Light
 */
public class DrawUtil {
    //窗体拉伸属性
    private static boolean isRight;// 是否处于右边界调整窗口状态
    private static boolean isBottomRight;// 是否处于右下角调整窗口状态
    private static boolean isBottom;// 是否处于下边界调整窗口状态
    private static boolean isLeft;// 是否处于右边界调整窗口状态
    private static boolean isBottomLeft;// 是否处于右下角调整窗口状态

    private final static int RESIZE_WIDTH = 12;// 判定是否为调整窗口状态的范围与边界距离
    private final static double MIN_WIDTH = 300;// 窗口最小宽度
    private final static double MIN_HEIGHT = 250;// 窗口最小高度

    public static void addDrawFunc(Stage stage, Pane root) {

        root.setOnMouseMoved((MouseEvent event) -> {
            double x = event.getSceneX();
            double y = event.getSceneY();
            double width = stage.getWidth();
            double height = stage.getHeight();
            Cursor cursorType = Cursor.DEFAULT;// 鼠标光标初始为默认类型，若未进入调整窗口状态，保持默认类型
            // 先将所有调整窗口状态重置
            isRight = isBottomRight = isBottom = isLeft = isBottomLeft = false;
            if (y >= height - RESIZE_WIDTH) {
                if (x <= RESIZE_WIDTH) {// 左下角调整窗口状态
                    isBottomLeft = true;
                    cursorType = Cursor.SW_RESIZE;
                } else if (x >= width - RESIZE_WIDTH) {// 右下角调整窗口状态
                    isBottomRight = true;
                    cursorType = Cursor.SE_RESIZE;
                } else {// 下边界调整窗口状态
                    isBottom = true;
                    cursorType = Cursor.S_RESIZE;
                }
            } else if (x >= width - RESIZE_WIDTH) {// 右边界调整窗口状态
                isRight = true;
                cursorType = Cursor.E_RESIZE;
            }else if(x <= RESIZE_WIDTH){
                isLeft = true;
                cursorType = Cursor.W_RESIZE;
            }
            // 最后改变鼠标光标
            root.setCursor(cursorType);
        });

        root.setOnMouseDragged(new EventHandler<MouseEvent>() {

            private boolean isFirst = true;
            private double beforeDragSX;
            private double beforeDragSY;

            @Override
            public void handle(MouseEvent event) {
                if(isFirst){
                    beforeDragSX = stage.getWidth() + stage.getX();
                    beforeDragSY = stage.getHeight() + stage.getY();
                    isFirst = false;
                }

                double x = event.getSceneX();
                double y = event.getSceneY();

                double sx = event.getScreenX();
                double sy = event.getScreenY();

                // 保存窗口改变后的x、y坐标和宽度、高度，用于预判是否会小于最小宽度、最小高度
                double nextX = stage.getX();
                double nextY = stage.getY();
                double nextWidth = stage.getWidth();
                double nextHeight = stage.getHeight();

                if (isRight || isBottomRight) {// 所有右边调整窗口状态
                    nextWidth = x;
                }
                if (isBottomRight || isBottom || isBottomLeft) {// 所有下边调整窗口状态
                    nextHeight = y;

                }
                if (isLeft || isBottomLeft) {
                    nextX = sx;
                    nextWidth = beforeDragSX - sx;
                }

                if (nextWidth <= MIN_WIDTH) {// 如果窗口改变后的宽度小于最小宽度，则宽度调整到最小宽度
                    nextWidth = MIN_WIDTH;
                }
                if (nextHeight <= MIN_HEIGHT) {// 如果窗口改变后的高度小于最小高度，则高度调整到最小高度
                    nextHeight = MIN_HEIGHT;
                }

                beforeDragSY = nextHeight+nextY;
                beforeDragSX = nextWidth+ nextX;

                // 最后统一改变窗口的x、y坐标和宽度、高度，可以防止刷新频繁出现的屏闪情况
                stage.setX(nextX);
                stage.setY(nextY);
                stage.setWidth(nextWidth);
                stage.setHeight(nextHeight);
                event.consume();
            }
        });
    }

    public static void setJFXDecorator(JFXDecorator decorator, Color color1, Color color2, String fullName, String minusName, String maxName, String closeName){
        HBox buttonsContainer = (HBox) decorator.getChildren().get(0);

        for (Node b :
                buttonsContainer.getChildren()) {
            if(b instanceof JFXButton) {
                String name = ((SVGGlyph)((JFXButton) b).getGraphic()).getName();
                if("FULLSCREEN".equals(name))
                    name = fullName;
                else if("MINUS".equals(name))
                    name = minusName;
                else if("RESIZE_MAX".equals(name))
                    name = maxName;
                else if("CLOSE".equals(name)) {
                    name = closeName;
                    ((JFXButton) b).setRipplerFill(color2);
                    b.setOnMouseEntered(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {
                            ((JFXButton) b).setBackground(new Background(new BackgroundFill(color1,
                                    new CornerRadii(2),
                                    Insets.EMPTY)));
                        }
                    });
                    b.setOnMouseExited(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {
                            ((JFXButton) b).setBackground(buttonsContainer.getBackground());
                        }
                    });
                }

                ((JFXButton) b).setTooltip(new Tooltip(name));
                ((JFXButton) b).setDisableVisualFocus(true);
            }
        }

    }
}