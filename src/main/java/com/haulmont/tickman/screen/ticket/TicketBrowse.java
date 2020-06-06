package com.haulmont.tickman.screen.ticket;

import com.haulmont.tickman.TickmanProperties;
import com.haulmont.tickman.entity.Team;
import com.haulmont.tickman.entity.Ticket;
import com.haulmont.tickman.service.TicketService;
import io.jmix.core.DataManager;
import io.jmix.ui.Dialogs;
import io.jmix.ui.Notifications;
import io.jmix.ui.UiComponents;
import io.jmix.ui.action.DialogAction;
import io.jmix.ui.component.*;
import io.jmix.ui.executor.BackgroundTask;
import io.jmix.ui.executor.BackgroundWorker;
import io.jmix.ui.executor.TaskLifeCycle;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.LookupComponent;
import io.jmix.ui.screen.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@UiController("tickman_Ticket.browse")
@UiDescriptor("ticket-browse.xml")
@LookupComponent("ticketsTable")
@LoadDataBeforeShow
public class TicketBrowse extends StandardLookup<Ticket> {

    private static final Logger log = LoggerFactory.getLogger(TicketBrowse.class);

    @Autowired
    private TicketService ticketService;

    @Autowired
    private Notifications notifications;

    @Autowired
    private Dialogs dialogs;

    @Autowired
    private UiComponents uiComponents;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private BackgroundWorker backgroundWorker;

    @Autowired
    private TickmanProperties properties;

    @Autowired
    private ProgressBar progressBar;

    @Autowired
    private ComboBox<String> milestoneFilterField;

    @Autowired
    private CollectionLoader<Ticket> ticketsDl;
    @Autowired
    private CollectionContainer<Ticket> ticketsDc;

    @Subscribe
    public void onAfterShow(AfterShowEvent event) {
        List<String> milestones = ticketsDc.getItems().stream()
                .map(Ticket::getMilestone)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        milestoneFilterField.setOptionsList(milestones);
    }


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

        ticketService.updateTicketsFromZenHub(tickets);
        notifications.create(Notifications.NotificationType.HUMANIZED).withCaption("Imported " + tickets.size() + " tickets").show();
        getScreenData().loadAll();

//        progressBar.setVisible(true);
//        BackgroundTaskHandler<Void> handler = backgroundWorker.handle(new UpdateFromZenHubTask(tickets));
//        handler.execute();
    }

    @Install(to = "ticketsTable.num", subject = "columnGenerator")
    private Component ticketsTableNumColumnGenerator(Ticket ticket) {
        Link link = uiComponents.create(Link.class);
        link.setCaption(String.valueOf(ticket.getNum()));
        link.setUrl(ticket.getHtmlUrl());
        link.setTarget("_blank");
        return link;
    }

    @Subscribe("teamFilterField")
    public void onTeamFilterFieldValueChange(HasValue.ValueChangeEvent<Team> event) {
        ticketsDl.setParameter("team", event.getValue());
        ticketsDl.load();
    }

    @Subscribe("milestoneFilterField")
    public void onMilestoneFilterFieldValueChange(HasValue.ValueChangeEvent<String> event) {
        ticketsDl.setParameter("milestone", event.getValue());
        ticketsDl.load();
    }

    private class UpdateFromZenHubTask extends BackgroundTask<Integer, Void> {

        private List<Ticket> tickets;

        protected UpdateFromZenHubTask(List<Ticket> tickets) {
            super(600, TicketBrowse.this);
            this.tickets = tickets;
        }

        @Override
        public Void run(TaskLifeCycle<Integer> taskLifeCycle) throws Exception {
            int i = 0;
            for (Ticket ticket : tickets) {
                long resetSec = ticketService.loadZenHubInfo(null, ticket);
                dataManager.save(ticket);
                long waitSec = resetSec - Instant.now().getEpochSecond();
                if (waitSec >= 0) {
                    log.info("Sleeping for " + (waitSec + 1) + " sec to satisfy ZenHub rate limit");
                    Thread.sleep((waitSec + 1) * 1000);
                }
                taskLifeCycle.publish(++i);
            }
            return null;
        }

        @Override
        public void done(Void result) {
            notifications.create(Notifications.NotificationType.HUMANIZED).withCaption("Updated " + tickets.size() + " tickets from ZenHub").show();
            getScreenData().loadAll();
            progressBar.setVisible(false);
        }

        @Override
        public void progress(List<Integer> changes) {
            progressBar.setValue(Double.valueOf(changes.get(changes.size() - 1)));
        }
    }
}