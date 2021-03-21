package com.haulmont.tickman.screen.ticket;

import com.haulmont.tickman.TickmanProperties;
import com.haulmont.tickman.entity.Milestone;
import com.haulmont.tickman.entity.Ticket;
import com.haulmont.tickman.service.TicketService;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.component.Label;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@UiController("tickman_Ticket.edit")
@UiDescriptor("ticket-edit.xml")
@EditedEntityContainer("ticketDc")
public class TicketEdit extends StandardEditor<Ticket> {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private TickmanProperties properties;

    @Autowired
    private ComboBox<Integer> estimateField;

    @Autowired
    private Label<String> numLab;
    @Autowired
    private CollectionLoader<Milestone> milestonesDl;

    @Subscribe
    public void onInit(InitEvent event) {
        estimateField.setOptionsList(properties.getEstimateValues());
    }

    @Subscribe
    public void onAfterShow(AfterShowEvent event) {
        numLab.setValue("<a href='" + getEditedEntity().getHtmlUrl() + "' target='_blank'>" + getEditedEntity().getName() + "</a>");
    }

    @Subscribe
    public void onBeforeCommitChanges(BeforeCommitChangesEvent event) {
        ticketService.updateTicket(getEditedEntity());
    }

    @Subscribe(id = "ticketDc", target = Target.DATA_CONTAINER)
    public void onTicketDcItemChange(InstanceContainer.ItemChangeEvent<Ticket> event) {
        milestonesDl.setParameter("repository", event.getItem() == null ? null : event.getItem().getRepository());
        milestonesDl.load();
    }
}