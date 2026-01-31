package io.dobermoney.launchpool.config;

import org.springframework.retry.RetryContext;
import org.springframework.retry.backoff.BackOffContext;
import org.springframework.retry.backoff.BackOffInterruptedException;
import org.springframework.retry.backoff.BackOffPolicy;

/**
 * BackOffPolicy that waits for the duration specified by the Retry-After header
 * from the 429 response. The value is set in RetryContext by the RetryListener.
 * Falls back to defaultSeconds when header is absent or unparseable.
 */
class RetryAfterBackOffPolicy implements BackOffPolicy {

    private static final String RETRY_AFTER_SECONDS_ATTR = "retryAfterSeconds";
    private static final long DEFAULT_SECONDS = 60;

    @Override
    public BackOffContext start(RetryContext context) {
        return new RetryContextHolder(context);
    }

    @Override
    public void backOff(BackOffContext backOffContext) throws BackOffInterruptedException {
        var holder = (RetryContextHolder) backOffContext;
        Long seconds = (Long) holder.context.getAttribute(RETRY_AFTER_SECONDS_ATTR);
        if (seconds == null) {
            seconds = DEFAULT_SECONDS;
        }
        long ms = seconds * 1000L;
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BackOffInterruptedException("Interrupted while waiting for Retry-After", e);
        }
    }

    /**
     * Stores the retry-after duration in the RetryContext for use during backoff.
     *
     * @param context the retry context to store the value in
     * @param seconds number of seconds to wait before retrying
     */
    static void setRetryAfterSeconds(RetryContext context, long seconds) {
        context.setAttribute(RETRY_AFTER_SECONDS_ATTR, seconds);
    }

    private static class RetryContextHolder implements BackOffContext {
        final RetryContext context;

        RetryContextHolder(RetryContext context) {
            this.context = context;
        }
    }
}
