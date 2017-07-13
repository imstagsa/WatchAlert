package net.digitaledge.watchalert;

import java.util.Collection;

import org.elasticsearch.common.component.LifecycleComponent;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.rest.RestModule;
import org.elasticsearch.threadpool.ThreadPoolModule;

import com.google.common.collect.Lists;

import net.digitaledge.watchalert.module.watchalertModule;
import net.digitaledge.watchalert.rest.watchalertRestAction;
import net.digitaledge.watchalert.service.watchalertService;


public class watchalertPlugin extends Plugin {
	
	ThreadPoolModule md;
	
    @Override
    public String name() {
        return "watchalertPlugin";
    }

    @Override
    public String description() {
        return "This is a watchalert plugin.";
    }

    @Override
    public Collection<Module> nodeModules() {
        final Collection<Module> modules = Lists.newArrayList();
        modules.add(new watchalertModule());
        return modules;
    }

   @SuppressWarnings("rawtypes")
    @Override
    public Collection<Class<? extends LifecycleComponent>> nodeServices() {
        final Collection<Class<? extends LifecycleComponent>> services = Lists.newArrayList();
        services.add(watchalertService.class);
        return services;
    }

}
