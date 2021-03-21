package com.haulmont.tickman.screen.assignee;

import io.jmix.ui.screen.*;
import com.haulmont.tickman.entity.Assignee;

@UiController("tickman_Assignee.browse")
@UiDescriptor("assignee-browse.xml")
@LookupComponent("assigneesTable")
public class AssigneeBrowse extends StandardLookup<Assignee> {
}