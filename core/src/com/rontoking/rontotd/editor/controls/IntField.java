package com.rontoking.rontotd.editor.controls;

import com.rontoking.rontotd.editor.components.EnemyEditor;
import com.rontoking.rontotd.editor.components.LevelEditor;
import com.rontoking.rontotd.editor.components.TileEditor;
import com.rontoking.rontotd.editor.components.TowerEditor;
import com.rontoking.rontotd.game.systems.networking.packets.EditorPacket;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;

public class IntField extends TextField {

    public IntField(int num){
        this(num, Integer.MIN_VALUE);
    }

    public IntField(int num, int minNum){
        this(num, minNum, Integer.MAX_VALUE);
    }

    public IntField(int num, final int minNum, final int maxNum){
        super(num + "");

        setMaxWidth(50);
        this.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                //if (!newValue.matches("\\d*")) {
                //    setText(newValue.replaceAll("[^\\d]", ""));
                //    positionCaret(getLength());
                //}
                try {
                    Integer.parseInt(newValue);
                    if(Integer.parseInt(newValue) < minNum){
                        set(minNum);
                    }
                    else if(Integer.parseInt(newValue) > maxNum){
                        set(maxNum);
                    }
                }
                catch (NumberFormatException ex) {
                    setText(oldValue);
                    positionCaret(getLength());
                }
            }
        });
    }

    public int num(){
        return Integer.parseInt(getText());
    }

    public void set(int num){
        setText(num + "");
    }
}
