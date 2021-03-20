package com.haulmont.tickman.screen.team;

import io.jmix.ui.screen.*;
import com.haulmont.tickman.entity.Team;

@UiController("tickman_Team.browse")
@UiDescriptor("team-browse.xml")
@LookupComponent("teamsTable")
public class TeamBrowse extends StandardLookup<Team> {
}