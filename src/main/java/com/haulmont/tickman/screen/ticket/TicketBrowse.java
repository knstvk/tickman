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
import java.util.ArrayList;
import java.util.List;

@UiController("tickman_Ticket.browse")
@UiDescriptor("ticket-browse.xml")
@LookupComponent("ticketsTable")
@LoadDataBeforeShow
public class TicketBrowse extends StandardLookup<Ticket> {

    private static final Logger log = LoggerFactory.getLogger(TicketBrowse.class);
    public static final String NO_MILESTONE = "<no milestone>";
    public static final String NO_ASSIGNEE = "<no assignee>";

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
    private ComboBox<String> assigneeFilterField;

    @Autowired
    private CollectionLoader<Ticket> ticketsDl;
    @Autowired
    private CollectionContainer<Ticket> ticketsDc;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        ticketsDl.setParameter("epic", false);
    }

    @Subscribe
    public void onAfterShow(AfterShowEvent event) {
        List<String> milestones = new ArrayList<>();
        List<String> assignees = new ArrayList<>();

        for (Ticket ticket : ticketsDc.getItems()) {
            if (ticket.getMilestone() != null && !milestones.contains(ticket.getMilestone())) {
                milestones.add(ticket.getMilestone());
            }
            if (ticket.getAssignee() != null && !assignees.contains(ticket.getAssignee())) {
                assignees.add(ticket.getAssignee());
            }
        }
        milestones.sort(null);
        assignees.sort(null);

        milestones.add(0, NO_MILESTONE);
        milestoneFilterField.setOptionsList(milestones);

        assignees.add(0, NO_ASSIGNEE);
        assigneeFilterField.setOptionsList(assignees);
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
        String value = event.getValue();
        if (NO_MILESTONE.equals(value)) {
            ticketsDl.setParameter("milestoneIsNull", true);
            ticketsDl.removeParameter("milestone");
        } else {
            ticketsDl.setParameter("milestone", value);
            ticketsDl.removeParameter("milestoneIsNull");
        }
        ticketsDl.load();
    }

    @Subscribe("assigneeFilterField")
    public void onAssigneeFilterFieldValueChange(HasValue.ValueChangeEvent<String> event) {
        String value = event.getValue();
        if (NO_ASSIGNEE.equals(value)) {
            ticketsDl.setParameter("assigneeIsNull", true);
            ticketsDl.removeParameter("assignee");
        } else {
            ticketsDl.setParameter("assignee", value);
            ticketsDl.removeParameter("assigneeIsNull");
        }
        ticketsDl.load();
    }

    @Subscribe("epicFilterField")
    public void onEpicFilterFieldValueChange(HasValue.ValueChangeEvent<Boolean> event) {
        if (event.getValue() == null || !event.getValue()) {
            ticketsDl.setParameter("epic", false);
        } else {
            ticketsDl.removeParameter("epic");
        }
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