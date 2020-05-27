package com.haulmont.tickman.screen.ticket;

import com.haulmont.tickman.entity.Ticket;
import com.haulmont.tickman.service.TicketService;
import io.jmix.ui.Dialogs;
import io.jmix.ui.Notifications;
import io.jmix.ui.action.DialogAction;
import io.jmix.ui.component.Button;
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

    @Subscribe("importBtn")
    public void onImportBtnClick(Button.ClickEvent event) {
        dialogs.createOptionDialog()
                .withCaption("Confirm")
                .withMessage("Import tickets?")
                .withActions(
                        new DialogAction(DialogAction.Type.OK).withHandler(e -> {
                            ticketService.deleteTickets();
                            List<Ticket> tickets = ticketService.loadTickets();
                            notifications.create().withCaption("Imported " + tickets.size() + " issues").show();
                            getScreenData().loadAll();
                        }),
                        new DialogAction(DialogAction.Type.CANCEL)
                )
                .show();
    }
}