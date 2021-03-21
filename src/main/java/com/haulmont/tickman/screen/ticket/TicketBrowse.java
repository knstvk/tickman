package com.haulmont.tickman.screen.ticket;

import com.google.common.base.Strings;
import com.haulmont.tickman.TickmanProperties;
import com.haulmont.tickman.entity.*;
import com.haulmont.tickman.service.AssigneeService;
import com.haulmont.tickman.service.MilestoneService;
import com.haulmont.tickman.service.TicketService;
import io.jmix.core.DataManager;
import io.jmix.core.Sort;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.Dialogs;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.UiComponents;
import io.jmix.ui.action.Action;
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
import org.springframework.context.ApplicationContext;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@UiController("tickman_Ticket.browse")
@UiDescriptor("ticket-browse.xml")
@LookupComponent("ticketsTable")
public class TicketBrowse extends StandardLookup<Ticket> {

    private static final Logger log = LoggerFactory.getLogger(TicketBrowse.class);

    public static final String NO_MILESTONE_CAPTION = "<no milestone>";
    public static final Milestone NO_MILESTONE = new Milestone();

    public static final String NO_ASSIGNEE_CAPTION = "<no assignee>";
    public static final Assignee NO_ASSIGNEE = new Assignee();

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
    private ComboBox<Milestone> milestoneFilterField;
    @Autowired
    private ComboBox<Assignee> assigneeFilterField;
    @Autowired
    private ComboBox<String> pipelineFilterField;

    @Autowired
    private CollectionLoader<Ticket> ticketsDl;
    @Autowired
    private CollectionContainer<Ticket> ticketsDc;

    @Autowired
    private ScreenBuilders screenBuilders;
    @Autowired
    private GroupTable<Ticket> ticketsTable;
    @Autowired
    private TextField<String> estimateFilterField;
    private List<Ticket> tickets;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private AssigneeService assigneeService;

    @Autowired
    private MilestoneService milestoneService;

    @Subscribe
    public void onInit(InitEvent event) {
        ticketsDc.setSorter(new TicketContainerSorter(ticketsDc, properties, applicationContext));
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        ticketsDl.setParameter("epic", false);
    }

    @Subscribe
    public void onAfterShow(AfterShowEvent event) {
        initMilestoneFilter();
        initAssigneeFilter();
        initPipelineFilter();
    }

    private void initAssigneeFilter() {
        LinkedHashMap<String, Assignee> assigneeMap = new LinkedHashMap<>();
        assigneeMap.put(NO_ASSIGNEE_CAPTION, NO_ASSIGNEE);

        dataManager.load(Assignee.class)
                .all()
                .list()
                .forEach(assignee -> assigneeMap.put(assignee.getLogin(), assignee));

        assigneeFilterField.setOptionsMap(assigneeMap);
    }

    private void initMilestoneFilter() {
        LinkedHashMap<String, Milestone> milestoneMap = new LinkedHashMap<>();
        milestoneMap.put(NO_MILESTONE_CAPTION, NO_MILESTONE);

        dataManager.load(Milestone.class)
                .all()
                .list()
                .forEach(milestone -> milestoneMap.put(milestone.getTitle(), milestone));

        milestoneFilterField.setOptionsMap(milestoneMap);
    }

    private void initPipelineFilter() {
        List<String> pipelines = ticketsDc.getItems().stream()
                .map(Ticket::getPipeline)
                .distinct()
                .collect(Collectors.toList());
        pipelineFilterField.setOptionsList(pipelines);
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

        List<Repository> repositories = dataManager.load(Repository.class)
                .all()
                .sort(Sort.by("orgName", "repoName"))
                .list();

        List<Ticket> importedTickets = new ArrayList<>();

        for (Repository repository : repositories) {
            List<Assignee> assignees = assigneeService.loadAssignees(repository);
            List<Milestone> milestones = milestoneService.loadMilestones(repository);
            List<Ticket> tickets = ticketService.loadIssues(repository, assignees, milestones);
            ticketService.updateTicketsFromZenHub(repository, tickets);

            importedTickets.addAll(tickets);
        }

        notifications.create(Notifications.NotificationType.HUMANIZED).withCaption("Imported " + importedTickets.size() + " tickets").show();
        getScreenData().loadAll();

        initAssigneeFilter();
        initMilestoneFilter();
        initPipelineFilter();

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
    public void onMilestoneFilterFieldValueChange(HasValue.ValueChangeEvent<Milestone> event) {
        Milestone value = event.getValue();
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
    public void onAssigneeFilterFieldValueChange(HasValue.ValueChangeEvent<Assignee> event) {
        Assignee value = event.getValue();
        if (NO_ASSIGNEE.equals(value)) {
            ticketsDl.setParameter("assigneeIsNull", true);
            ticketsDl.removeParameter("assignee");
        } else {
            ticketsDl.setParameter("assignee", value);
            ticketsDl.removeParameter("assigneeIsNull");
        }
        ticketsDl.load();
    }

    @Subscribe("pipelineFilterField")
    public void onPipelineFilterFieldValueChange(HasValue.ValueChangeEvent<String> event) {
        ticketsDl.setParameter("pipeline", event.getValue());
        ticketsDl.load();
    }

    @Subscribe("estimateFilterField")
    public void onEstimateFilterFieldValueChange(HasValue.ValueChangeEvent<String> event) {
        ticketsDc.setItems(filterByEstimate(tickets));
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

    @Subscribe(id = "ticketsDl", target = Target.DATA_LOADER)
    public void onTicketsDlPostLoad(CollectionLoader.PostLoadEvent<Ticket> event) {
        tickets = event.getLoadedEntities();
        ticketsDc.setItems(filterByEstimate(tickets));

        Table.SortInfo sortInfo = ticketsTable.getSortInfo();
        if (sortInfo != null && ((MetaPropertyPath) sortInfo.getPropertyId()).toPathString().equals("pipeline")) {
            ticketsDc.getSorter().sort(Sort.by(sortInfo.getAscending() ? Sort.Direction.ASC : Sort.Direction.DESC, "pipeline"));
        }
    }

    private List<Ticket> filterByEstimate(List<Ticket> tickets) {
        String filterExpr = Strings.nullToEmpty(estimateFilterField.getValue()).trim();
        if (filterExpr.equals("")) {
            return tickets;
        } else {
            return tickets.stream()
                    .filter(ticket -> EstimateMatcher.matches(ticket, filterExpr))
                    .collect(Collectors.toList());
        }
    }

    @Subscribe("ticketsTable.export")
    public void onTicketsTableExport(Action.ActionPerformedEvent event) {
        TicketExportScreen screen = screenBuilders.screen(this)
                .withScreenClass(TicketExportScreen.class)
                .build();
        screen.setTickets(ticketsTable.getSelected());
        screen.show();
    }

//    private class UpdateFromZenHubTask extends BackgroundTask<Integer, Void> {
//
//        private List<Ticket> tickets;
//
//        protected UpdateFromZenHubTask(List<Ticket> tickets) {
//            super(600, TicketBrowse.this);
//            this.tickets = tickets;
//        }
//
//        @Override
//        public Void run(TaskLifeCycle<Integer> taskLifeCycle) throws Exception {
//            int i = 0;
//            for (Ticket ticket : tickets) {
//                long resetSec = ticketService.loadZenHubInfo(repository, null, ticket);
//                dataManager.save(ticket);
//                long waitSec = resetSec - Instant.now().getEpochSecond();
//                if (waitSec >= 0) {
//                    log.info("Sleeping for " + (waitSec + 1) + " sec to satisfy ZenHub rate limit");
//                    Thread.sleep((waitSec + 1) * 1000);
//                }
//                taskLifeCycle.publish(++i);
//            }
//            return null;
//        }
//
//        @Override
//        public void done(Void result) {
//            notifications.create(Notifications.NotificationType.HUMANIZED).withCaption("Updated " + tickets.size() + " tickets from ZenHub").show();
//            getScreenData().loadAll();
//            progressBar.setVisible(false);
//        }
//
//        @Override
//        public void progress(List<Integer> changes) {
//            progressBar.setValue(Double.valueOf(changes.get(changes.size() - 1)));
//        }
//    }
}