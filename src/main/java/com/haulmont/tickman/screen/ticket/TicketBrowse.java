package com.haulmont.tickman.screen.ticket;

import com.haulmont.tickman.service.TicketService;
import io.jmix.core.LoadContext;
import io.jmix.ui.screen.*;
import com.haulmont.tickman.entity.Ticket;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@UiController("tickman_Ticket.browse")
@UiDescriptor("ticket-browse.xml")
@LookupComponent("ticketsTable")
@LoadDataBeforeShow
public class TicketBrowse extends StandardLookup<Ticket> {

    @Autowired
    private TicketService ticketService;

    @Install(to = "ticketsDl", target = Target.DATA_LOADER)
    private List<Ticket> ticketsDlLoadDelegate(LoadContext<Ticket> loadContext) {
        return ticketService.loadTickets();
    }
}