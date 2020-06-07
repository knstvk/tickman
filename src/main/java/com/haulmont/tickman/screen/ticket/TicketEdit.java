package com.haulmont.tickman.screen.ticket;

import com.haulmont.tickman.TickmanProperties;
import com.haulmont.tickman.entity.Ticket;
import com.haulmont.tickman.service.TicketService;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@UiController("tickman_Ticket.edit")
@UiDescriptor("ticket-edit.xml")
@EditedEntityContainer("ticketDc")
@LoadDataBeforeShow
public class TicketEdit extends StandardEditor<Ticket> {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private TickmanProperties properties;

    @Autowired
    private ComboBox<Integer> estimateField;

    @Subscribe
    public void onInit(InitEvent event) {
        estimateField.setOptionsList(properties.getEstimateValues());
    }

    @Subscribe
    public void onBeforeCommitChanges(BeforeCommitChangesEvent event) {
        ticketService.updateTicket(getEditedEntity());
    }
}