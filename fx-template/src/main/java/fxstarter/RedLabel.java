package fxstarter;

import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public final class RedLabel extends Label {

    public RedLabel(String text) {
        super(text);
        setTextFill(Color.RED);
        setFont(new Font(15));
    }
}
