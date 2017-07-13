package net.digitaledge.watchalert.module;

import org.elasticsearch.common.inject.AbstractModule;

import net.digitaledge.watchalert.service.watchalertService;

public class watchalertModule extends AbstractModule {

	@Override
    protected void configure() {
        bind(watchalertService.class).asEagerSingleton();
    }
}
