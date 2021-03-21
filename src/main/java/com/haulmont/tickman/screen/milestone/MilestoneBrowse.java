package com.haulmont.tickman.screen.milestone;

import io.jmix.ui.screen.*;
import com.haulmont.tickman.entity.Milestone;

@UiController("tickman_Milestone.browse")
@UiDescriptor("milestone-browse.xml")
@LookupComponent("milestonesTable")
public class MilestoneBrowse extends StandardLookup<Milestone> {
}