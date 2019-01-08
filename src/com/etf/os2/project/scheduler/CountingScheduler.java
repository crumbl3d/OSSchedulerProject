package com.etf.os2.project.scheduler;

public abstract class CountingScheduler extends Scheduler{
    protected int processCount = 0;

    long getProcessCount() { return processCount; }
}