package com.rontoking.rontotd.editor.controls;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;

public class FloatField extends TextField {
    public FloatField(float num){
        this(num, -Float.MAX_VALUE);
    }

    public FloatField(float num, float minNum){
        this(num, minNum, Float.MAX_VALUE);
    }

    public FloatField(float num, final float minNum, final float maxNum){
        super(num + "");

        setMaxWidth(50);
        this.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try {
                    Float.parseFloat(newValue);
                    if (Float.parseFloat(newValue) < minNum) {
                        set(minNum);
                    } else if (Float.parseFloat(newValue) > maxNum) {
                        set(maxNum);
                    }
                } catch (NumberFormatException ex) {
                    setText(oldValue);
                    positionCaret(getLength());
                }
            }
        });
    }

    public float num(){
        return Float.parseFloat(getText());
    }

    public void set(float num){
        setText(num + "");
    }
}
