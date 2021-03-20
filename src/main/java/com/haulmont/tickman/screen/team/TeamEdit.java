package com.haulmont.tickman.screen.team;

import com.haulmont.tickman.entity.Team;
import io.jmix.ui.screen.EditedEntityContainer;
import io.jmix.ui.screen.StandardEditor;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;

@UiController("tickman_Team.edit")
@UiDescriptor("team-edit.xml")
@EditedEntityContainer("teamDc")
public class TeamEdit extends StandardEditor<Team> {
}