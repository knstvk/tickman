package com.haulmont.tickman.screen.main;

import io.jmix.ui.component.AppWorkArea;
import io.jmix.ui.component.Window;
import io.jmix.ui.screen.LoadDataBeforeShow;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;

@UiController("tickman_MainScreen")
@UiDescriptor("tickman-main-screen.xml")
@LoadDataBeforeShow
public class TickmanMainScreen extends Screen implements Window.HasWorkArea {
    @Override
    public AppWorkArea getWorkArea() {
        return (AppWorkArea) getWindow().getComponent("workArea");
    }
}
