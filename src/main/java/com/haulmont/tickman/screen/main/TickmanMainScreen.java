package com.haulmont.tickman.screen.main;

import com.haulmont.tickman.screen.ticket.TicketBrowse;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.component.AppWorkArea;
import io.jmix.ui.component.Window;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("tickman_MainScreen")
@UiDescriptor("tickman-main-screen.xml")
public class TickmanMainScreen extends Screen implements Window.HasWorkArea {

    @Autowired
    private ScreenBuilders screenBuilders;

    @Override
    public AppWorkArea getWorkArea() {
        return (AppWorkArea) getWindow().getComponent("workArea");
    }

    @Subscribe
    public void onAfterShow(AfterShowEvent event) {
        screenBuilders.screen(this)
                .withScreenClass(TicketBrowse.class)
                .show();
    }
}
