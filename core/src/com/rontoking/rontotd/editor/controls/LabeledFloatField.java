package com.rontoking.rontotd.editor.controls;


import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class LabeledFloatField extends HBox {
    public Label label;
    public FloatField field;

    public LabeledFloatField(String text, float num){
        this(text, num, -Float.MAX_VALUE);
    }

    public LabeledFloatField(String text, float num, float minNum){
        this(text, num, minNum, Float.MAX_VALUE);
    }

    public LabeledFloatField(String text, float num, float minNum, float maxNum){
        super();

        label = new Label(text);
        field = new FloatField(num, minNum, maxNum);

        setAlignment(Pos.CENTER);
        getChildren().addAll(label, field);
    }
}
