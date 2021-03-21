package com.haulmont.tickman.screen.assignee;

import io.jmix.ui.screen.*;
import com.haulmont.tickman.entity.Assignee;

@UiController("tickman_Assignee.edit")
@UiDescriptor("assignee-edit.xml")
@EditedEntityContainer("assigneeDc")
public class AssigneeEdit extends StandardEditor<Assignee> {
}