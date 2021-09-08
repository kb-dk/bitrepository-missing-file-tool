package dk.kb.eventhandler;

import org.bitrepository.client.eventhandler.ContributorFailedEvent;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PutFileEventHandler implements EventHandler {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Object finishLock = new Object();
    private boolean finished = false;
    private boolean failed = false;


    @Override
    public void handleEvent(OperationEvent event) {
        log.info("Got event from client: {}", event.getEventType());
        switch (event.getEventType()) {
            case COMPLETE:
                log.info("Finished put fileID for file '{}'", event.getFileID());
                finish();
                break;
            case FAILED:
                log.info("Failed put fileID for file '{}'", event.getFileID());
                failed = true;
                finish();
                break;
            case COMPONENT_FAILED:
                ContributorFailedEvent failedEvent = (ContributorFailedEvent) event;
                log.info("Component '{}' failed due to: {}", failedEvent.getContributorID(), failedEvent.getInfo());
            default:
                break;
        }
    }

    /**
     * Method to indicate the operation have finished (regardless if is successful or not).
     */
    private void finish() {
        log.trace("Finish method invoked");
        synchronized (finishLock) {
            log.trace("Finish method entered synchronized block");
            finished = true;
            finishLock.notifyAll();
            log.trace("Finish method notified All");
        }
    }

    /**
     * Method to wait for the operation to complete. The method is blocking.
     * @throws InterruptedException if the thread is interrupted
     */
    public void waitForFinish() throws InterruptedException {
        synchronized (finishLock) {
            if (!finished) {
                log.trace("Thread waiting for client to finish");
                finishLock.wait();
            }
            log.trace("Client has indicated it's finished.");
        }
    }

    /**
     * Method to determine if the operation was successful.
     * The method should not be called prior to a call to {@link #waitForFinish()} have returned.
     * @return true if the operation succeeded, otherwise false.
     */
    public boolean isOperationSuccess() {
        return !failed;
    }
}
