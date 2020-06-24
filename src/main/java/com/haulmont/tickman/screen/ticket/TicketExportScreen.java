package com.haulmont.tickman.screen.ticket;

import com.haulmont.tickman.entity.Ticket;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.RadioButtonGroup;
import io.jmix.ui.component.TextArea;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@UiController("tickman_TicketExportScreen")
@UiDescriptor("ticket-export-screen.xml")
public class TicketExportScreen extends Screen {

    public static final String URL = "URL";
    public static final String MARKDOWN = "Markdown";

    @Autowired
    private TextArea<String> textArea;

    @Autowired
    private RadioButtonGroup<String> radioGroup;

    private Collection<Ticket> tickets;

    public void setTickets(Collection<Ticket> tickets) {
        this.tickets = tickets;
        radioGroup.setValue(URL);
    }

    @Subscribe
    public void onInit(InitEvent event) {
        radioGroup.setOptionsList(Arrays.asList(URL, MARKDOWN));
    }

    @Subscribe("radioGroup")
    public void onRadioGroupValueChange(HasValue.ValueChangeEvent event) {
        render();
    }

    private void render() {
        String text = tickets.stream()
                .map(ticket ->
                        radioGroup.getValue().equals(URL) ?
                                ticket.getHtmlUrl() :
                                "[" + ticket.getTitle() + "](" + ticket.getHtmlUrl() + ")"
                )
                .collect(Collectors.joining("\n"));
        textArea.setValue(text);
    }
}