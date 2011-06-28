package com.streams.server;

import com.streams.io.SInput;
import com.streams.io.SOutput;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Process Streams by applying the given processing algorithm on each input. Blocking Queues are provided
 * to handle Inputs and Outputs in a decoupled manner. The server blocks and waits if the input queue is empty
 * or the output queue is full. The server is started by calling startProcessing() and can be shutdown by
 * passing in a special input object where input.isShutdown() == true is set. The Server uses a Fixed Size
 * threadpool and Synchronous Queue tasklist to manage concurrent processing of inputs.
 *
 * @author isyed
 * @version 0.1
 */
public final class StreamProcessingServer<I extends SInput, O extends SOutput> {

    //Adjustable size of input and output queues
    private final int size;
    private final int noOfThreads;
    private final ProcessingAlgorithm<I, O> algo;
    private final ThreadPoolExecutor threadPool;
    /**
     * Input from downstream is stored in this queue asynchronously
     */
    private final BlockingQueue<I> inputQueue;
    /**
     * Output to upstream are stored in this queue for retrieval at a later time
     */
    private final BlockingQueue<O> outputQueue;

    /**
     * Algorithm used to process input data
     * @param <I>
     * @param <O>
     */
    public static interface ProcessingAlgorithm<I, O> {

        /**
         * Builds an output based on the passed in input.
         * @param input data
         * @return output data
         */
        O processStream(I input);
    }

    public StreamProcessingServer(int qSize, int tNoOfThreads, ProcessingAlgorithm tAlgo) {
        size = qSize;
        noOfThreads = tNoOfThreads;
        algo = tAlgo;
        threadPool = new ThreadPoolExecutor(noOfThreads, noOfThreads, 0L, TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>());
        threadPool.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        inputQueue = new LinkedBlockingQueue<I>(size);
        outputQueue = new LinkedBlockingQueue<O>(size);
    }

    /**
     * Starts processing stream
     */
    public void startProcessing() {
        while (!threadPool.isShutdown()) {
            I input = null;
            try {
                input = inputQueue.take();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
            /********DELAY IS FOR TESTING PURPOSES ONLY********/
            try {
                Thread.sleep(10);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
            /********DELAY IS FOR TESTING PURPOSES ONLY********/
            if (input != null && input.isShutdown()) {
                stopProcessing();
            } else if (input != null) {
                execute(input);
            }
        }
    }

    /**
     * Executes processing algorithm on input data and outputs data to be sent upstream.
     * Uses the Executor framework to handle multiple input data information concurrently
     * @param i Input
     */
    private void execute(final I input) {
        threadPool.execute(new Runnable() {

            public void run() {
                if (input != null) {
                    O t = algo.processStream(input);
                    if (t != null) {
                        try {
                            outputQueue.put(t);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }
        });
    }

    /**
     * Stops processing stream
     */
    public void stopProcessing() {
        threadPool.shutdown();
        try {
            threadPool.awaitTermination(8000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Puts Input data from upstream into Input Queue.
     * @param input from Downstream Providers
     * @return True if successful, otherwise false. Operation is non-blocking.
     */
    public Boolean offerInputQueue(I input) {
        return inputQueue.offer(input);
    }

    /**
     * Takes outputs to be sent to downstream into Output Queue.
     * @return output to be sent to output queue. Operation is non-blocking.
     */
    public O pollOutputQueue() {
        return outputQueue.poll();
    }

    @Override
    protected void finalize() throws Throwable {
        threadPool.shutdownNow();
    }
}



