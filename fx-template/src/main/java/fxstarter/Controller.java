package fxstarter;

public final class Controller {
    private final DynamicViewHandler dynamicViewHandler;

    public Controller(DynamicViewHandler dynamicViewHandler) {
        this.dynamicViewHandler = dynamicViewHandler;
    }

    void switchToPrimary() {
        dynamicViewHandler.setDynamicView(new View1(this));
    }

    void switchToSecondary() {
        dynamicViewHandler.setDynamicView(new View2(this));
    }
}
