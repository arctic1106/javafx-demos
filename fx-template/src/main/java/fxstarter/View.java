package fxstarter;

import javafx.scene.Parent;

public sealed interface View permits View1, View2 {
    Parent getParent();
}
