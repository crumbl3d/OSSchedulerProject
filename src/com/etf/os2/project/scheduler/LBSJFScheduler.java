package com.etf.os2.project.scheduler;

import com.etf.os2.project.process.Pcb;

public class LBSJFScheduler extends Scheduler {

    public LBSJFScheduler(int cpuCount, double alpha, boolean preemption) {
        super();
    }

    @Override
    public synchronized Pcb get(int cpuId) {
        return null;
    }

    @Override
    public synchronized void put(Pcb pcb) {

    }
}