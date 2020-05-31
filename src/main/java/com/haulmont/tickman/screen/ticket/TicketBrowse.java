package com.haulmont.tickman.screen.ticket;

import com.haulmont.tickman.entity.Ticket;
import com.haulmont.tickman.service.TicketService;
import io.jmix.ui.Dialogs;
import io.jmix.ui.Notifications;
import io.jmix.ui.UiComponents;
import io.jmix.ui.action.DialogAction;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.Label;
import io.jmix.ui.component.Link;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@UiController("tickman_Ticket.browse")
@UiDescriptor("ticket-browse.xml")
@LookupComponent("ticketsTable")
@LoadDataBeforeShow
public class TicketBrowse extends StandardLookup<Ticket> {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private Notifications notifications;

    @Autowired
    private Dialogs dialogs;
    @Autowired
    private UiComponents uiComponents;

    @Subscribe("importBtn")
    public void onImportBtnClick(Button.ClickEvent event) {
        dialogs.createOptionDialog()
                .withCaption("Confirm")
                .withMessage("Import tickets?")
                .withActions(
                        new DialogAction(DialogAction.Type.OK).withHandler(e -> importTickets()),
                        new DialogAction(DialogAction.Type.CANCEL)
                )
                .show();
    }

    private void importTickets() {
        ticketService.deleteTickets();

        List<Ticket> tickets = ticketService.loadTicketsFromGitHub();
        notifications.create(Notifications.NotificationType.HUMANIZED).withCaption("Loaded " + tickets.size() + " GitHub issues").show();

        ticketService.updateTicketsFromZenHub(tickets);
        notifications.create(Notifications.NotificationType.HUMANIZED).withCaption("Updated " + tickets.size() + " tickets from ZenHub").show();

        getScreenData().loadAll();
    }

    @Install(to = "ticketsTable.num", subject = "columnGenerator")
    private Component ticketsTableNumColumnGenerator(Ticket ticket) {
        Link link = uiComponents.create(Link.class);
        link.setCaption(String.valueOf(ticket.getNum()));
        link.setUrl(ticket.getHtmlUrl());
        link.setTarget("_blank");
        return link;
    }
}