package com.hyn.job;

/**
 * Created by hanyanan on 2015/5/31.
 */
public interface JobExecutor<J extends AsyncJob, R> {

    /**
     * Perform current request and return the result.It's a execution unit.
     * User can throw some specify Throwable to retry again or force delivery failed result.
     * <lu>
     *     <li>{@link DeliveryFailedException} delivery failed response, this will post response if has</li>
     *     <li>wewefwef</li>
     *
     * </lu>
     *
     * @see DeliveryFailedException
     * @see UnRetryable
     * @see UnRetryRunTimeException
     * @param asyncJob the specify job.
     * @return
     * @throws Throwable
     */
    R performRequest(J asyncJob) throws Throwable;
}
