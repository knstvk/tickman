package com.haulmont.tickman.screen.main;

import com.haulmont.tickman.screen.ticket.TicketBrowse;
import com.vaadin.server.WebBrowser;
import io.jmix.core.Messages;
import io.jmix.ui.App;
import io.jmix.ui.AppUI;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.component.*;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;

import static io.jmix.ui.component.ComponentsHelper.setStyleName;

@UiController("tickman_MainScreen")
@UiDescriptor("tickman-main-screen.xml")
@LoadDataBeforeShow
public class TickmanMainScreen extends Screen implements Window.HasWorkArea {

    private static final String SIDEMENU_COLLAPSED_STATE = "sidemenuCollapsed";
    private static final String SIDEMENU_COLLAPSED_STYLENAME = "collapsed";

    @Autowired
    private Messages messages;
    @Autowired
    private ScreenBuilders screenBuilders;

    @Override
    public AppWorkArea getWorkArea() {
        return (AppWorkArea) getWindow().getComponent("workArea");
    }

    @Nullable
    protected Button getCollapseMenuButton() {
        return (Button) getWindow().getComponent("collapseMenuButton");
    }

    @Subscribe
    public void onInit(InitEvent event) {
        initCollapsibleMenu();
    }

    @Subscribe
    public void onAfterShow(AfterShowEvent event) {
        screenBuilders.screen(this)
                .withScreenClass(TicketBrowse.class)
                .show();
    }


    protected void initCollapsibleMenu() {
        Component sideMenuContainer = getWindow().getComponent("sideMenuContainer");
        if (sideMenuContainer instanceof CssLayout) {
            if (isMobileDevice()) {
                setSideMenuCollapsed(true);
            } else {
                String menuCollapsedCookie = App.getInstance().getCookieValue(SIDEMENU_COLLAPSED_STATE);
                boolean menuCollapsed = Boolean.parseBoolean(menuCollapsedCookie);
                setSideMenuCollapsed(menuCollapsed);
            }
            Button collapseMenuButton = getCollapseMenuButton();
            if (collapseMenuButton != null) {
                collapseMenuButton.addClickListener(event ->
                        setSideMenuCollapsed(!isMenuCollapsed()));
            }
        }
    }

    protected void setSideMenuCollapsed(boolean collapsed) {
        Component sideMenuContainer = getWindow().getComponent("sideMenuContainer");
        CssLayout sideMenuPanel = (CssLayout) getWindow().getComponent("sideMenuPanel");
        Button collapseMenuButton = getCollapseMenuButton();

        setStyleName(sideMenuContainer, SIDEMENU_COLLAPSED_STYLENAME, collapsed);
        setStyleName(sideMenuPanel, SIDEMENU_COLLAPSED_STYLENAME, collapsed);

        if (collapseMenuButton != null) {
            if (collapsed) {
                collapseMenuButton.setCaption(messages.getMessage("menuExpandGlyph"));
                collapseMenuButton.setDescription(messages.getMessage("sideMenuExpand"));
            } else {
                collapseMenuButton.setCaption(messages.getMessage("menuCollapseGlyph"));
                collapseMenuButton.setDescription(messages.getMessage("sideMenuCollapse"));
            }
        }

        App.getInstance().addCookie(SIDEMENU_COLLAPSED_STATE, String.valueOf(collapsed));
    }

    protected boolean isMenuCollapsed() {
        CssLayout sideMenuPanel = (CssLayout) getWindow().getComponent("sideMenuPanel");
        return sideMenuPanel != null
                && sideMenuPanel.getStyleName() != null
                && sideMenuPanel.getStyleName().contains(SIDEMENU_COLLAPSED_STYLENAME);
    }

    protected boolean isMobileDevice() {
        WebBrowser browser = AppUI.getCurrent()
                .getPage()
                .getWebBrowser();

        return browser.getScreenWidth() < 500
                || browser.getScreenHeight() < 800;
    }
}
