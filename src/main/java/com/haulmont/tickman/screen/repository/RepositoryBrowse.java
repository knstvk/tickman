package com.haulmont.tickman.screen.repository;

import io.jmix.ui.screen.*;
import com.haulmont.tickman.entity.Repository;

@UiController("tickman_Repository.browse")
@UiDescriptor("repository-browse.xml")
@LookupComponent("table")
public class RepositoryBrowse extends MasterDetailScreen<Repository> {
}