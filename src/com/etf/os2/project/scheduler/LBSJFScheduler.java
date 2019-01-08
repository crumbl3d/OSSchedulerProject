package com.etf.os2.project.scheduler;

import com.etf.os2.project.process.Pcb;

public class LBSJFScheduler extends Scheduler {

    public LBSJFScheduler(int cpuCount, double alpha, boolean preemption) {
        super();
    }

    @Override
    public Pcb get(int cpuId) {
        return null;
    }

    @Override
    public void put(Pcb pcb) {

    }
}