package com.hyn.job;

/**
 * Created by hanyanan on 2015/7/8.
 */
public interface JobActivityLifecycleCallbacks {

    public void onJobPrepareRunning(AsyncJob asyncJob);

    public void obJobRetried(AsyncJob asyncJob);

    public void onJobFinished(AsyncJob asyncJob);
}
