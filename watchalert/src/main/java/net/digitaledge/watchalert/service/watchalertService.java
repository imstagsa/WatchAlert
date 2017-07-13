package net.digitaledge.watchalert.service;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;

import net.digitaledge.watchalert.WatchAlertsWorker;

public class watchalertService extends AbstractLifecycleComponent<watchalertService> {

	private Thread thread;
	
    @Inject
    public watchalertService(final Settings settings) {
        super(settings);
        logger.info("CREATE watchalertService.");
    }

    @Override
    protected void doStart() throws ElasticsearchException {
        logger.info("START 12 watchalertService.");
        WatchAlertsWorker worker = new WatchAlertsWorker(settings, logger);
        logger.info("watchalertService  starting.");
        thread = new Thread(worker);
        thread.start();
        logger.info("watchalertService  started.");
    }

    @Override
    protected void doStop() throws ElasticsearchException {
        logger.info("Stopping watchalertService.");

        if(thread == null)
        {
        	logger.debug("Thread is null.");
            return;
        }
        try {
        	logger.debug("Shutting down thread.");
        	if(thread.isAlive())
        		thread.stop();
        
        	thread.destroy();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    protected void doClose() throws ElasticsearchException {
        logger.info("CLOSE watchalertService.");
    }

}
