/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.client;


import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.HttpClient;

/**
 * Class that embodies a Reaper thread that reaps idle connections. Note that
 * the thread won't be started if the value of the idleCheckInterval parameter
 * is equal to 0.
 * 
 * @author Sanjay Acharya
 */
public class HttpIdleConnectionReaper {

    /**
     * Thread that reaps idle and expired connections.
     */
    private class ReaperThread extends Thread {
        /** Indicates if the thread is shut down. */
        private volatile boolean shutdown;

        /** CountDownLatch used when stopping the thread. */
        private final CountDownLatch shutdownLatch = new CountDownLatch(1);

        /** CountDownLatch used when starting the thread. */
        private final CountDownLatch startupLatch = new CountDownLatch(1);

        @Override
        public void run() {
            try {
                startupLatch.countDown();
                // While shutdown has not been called and the thread has not
                // been interrupted do the following.
                while (!shutdown && !isInterrupted()) {
                    try {
                        Thread.sleep(idleCheckInterval);
                    } catch (InterruptedException interrupted) {
                        continue;
                    }

                    httpClient.getConnectionManager().closeExpiredConnections();
                    httpClient.getConnectionManager().closeIdleConnections(
                            idleTimeOut, TimeUnit.MILLISECONDS);
                }
            } finally {
                shutdownLatch.countDown();
            }
        }

        /**
         * Tells the reaper thread the maximum time to wait before starting.
         * 
         * @param millis
         *            The maximum time to wait before starting the thread.
         * @throws InterruptedException
         *             If the current thread was interrupted.
         */
        void waitForStart(long millis) throws InterruptedException {
            startupLatch.await(millis, TimeUnit.MILLISECONDS);
        }

        /**
         * Tells the reaper thread the maximum time to wait before stopping.
         * 
         * @param millis
         *            The maximum time to wait before stopping the thread.
         * @throws InterruptedException
         *             If the current thread was interrupted.
         */
        void waitForStop(long millis) throws InterruptedException {
            shutdownLatch.await(millis, TimeUnit.MILLISECONDS);
        }
    }

    /** The HttpClient for which this is the reaper. */
    private final HttpClient httpClient;

    /** The time to sleep between checks for idle connections. */
    private final long idleCheckInterval;

    /** The age of connections to reap. */
    private final long idleTimeOut;

    /** The thread that gleans the idle connections. */
    private final ReaperThread reaperThread;

    /**
     * Constructor.
     * 
     * @param httpClient
     *            The HttpClient for which this is the reaper.
     * @param idleCheckInterval
     *            The time to sleep between checks for idle connections. Note
     *            that if this is 0, then reaping won't occur.
     * @param idleTimeout
     *            The age of connections to reap.
     */
    public HttpIdleConnectionReaper(HttpClient httpClient,
            long idleCheckInterval, long idleTimeout) {
        if (httpClient == null) {
            throw new IllegalArgumentException(
                    "HttpClient is a required parameter");
        }
        this.httpClient = httpClient;
        this.idleCheckInterval = idleCheckInterval;
        this.idleTimeOut = idleTimeout;

        this.reaperThread = idleCheckInterval > 0L ? new ReaperThread() : null;

        if (reaperThread != null) {
            reaperThread.start();
        }
    }

    /**
     * Returns {@code true} if the reaper is started.
     * 
     * @return {@code true} If the reaper is started.
     */
    public boolean isStarted() {
        return reaperThread != null && reaperThread.isAlive();
    }

    /**
     * Returns {@code true} if the reaper is stopped.
     * 
     * @return {@code true} if the reaper is stopped.
     */
    public boolean isStopped() {
        return (reaperThread != null || !reaperThread.isAlive());
    }

    /**
     * Stops the Idle Connection Reaper if running.
     * 
     * @throws InterruptedException
     *             If the call to stop was interrupted
     */
    public void stop() throws InterruptedException {
        if (reaperThread == null) {
            return;
        }

        reaperThread.shutdown = true;
        reaperThread.interrupt();
        // Wait for a second to join
        reaperThread.join(1000L);
    }

    /**
     * Tells the reaper thread the maximum time to wait before starting.
     * 
     * @param millis
     *            The maximum time to wait before starting the thread.
     * @throws InterruptedException
     *             If the current thread was interrupted.
     */
    public void waitForReaperStart(long millis) throws InterruptedException {
        reaperThread.waitForStart(millis);
    }

    /**
     * Tells the reaper thread the maximum time to wait before stopping.
     * 
     * @param millis
     *            The maximum time to wait before stopping the thread.
     * @throws InterruptedException
     *             If the current thread was interrupted.
     */
    public void waitForReaperStop(long millis) throws InterruptedException {
        reaperThread.waitForStop(millis);
    }
}
