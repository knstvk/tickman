package com.haulmont.tickman.screen.team;

import io.jmix.ui.screen.*;
import com.haulmont.tickman.entity.Team;

@UiController("tickman_Team.edit")
@UiDescriptor("team-edit.xml")
@EditedEntityContainer("teamDc")
@LoadDataBeforeShow
public class TeamEdit extends StandardEditor<Team> {
}