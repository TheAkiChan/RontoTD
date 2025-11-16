package com.rontoking.rontotd.editor.controls;


import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class LabeledIntField extends HBox {
    public Label label;
    public IntField field;

    public LabeledIntField(String text, int num){
        this(text, num, Integer.MIN_VALUE);
    }

    public LabeledIntField(String text, int num, int minNum){
        this(text, num, minNum, Integer.MAX_VALUE);
    }

    public LabeledIntField(String text, int num, int minNum, int maxNum){
        super();

        label = new Label(text);
        field = new IntField(num, minNum, maxNum);

        setAlignment(Pos.CENTER);
        getChildren().addAll(label, field);
    }
}
