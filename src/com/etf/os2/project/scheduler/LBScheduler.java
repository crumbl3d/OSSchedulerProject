package com.etf.os2.project.scheduler;

import com.etf.os2.project.process.Pcb;
import com.etf.os2.project.process.Pcb.ProcessState;

public class LBScheduler extends Scheduler {

    private int cpuCount;
    private CountingScheduler[] queues;

    private LBScheduler(int cpuCount) {
        this.cpuCount = cpuCount;
        queues = new CountingScheduler[cpuCount];
    }

    LBScheduler(String type, int cpuCount, double alpha, boolean preemption) {
        this(cpuCount);
        if (!type.equals("lbsjf")) return;
        for (int i = 0; i < cpuCount; i++) {
            queues[i] = new SJFScheduler(alpha, preemption);
        }
    }

    LBScheduler(String type, int cpuCount, int queueCount, long[] timeslices) {
        this(cpuCount);
        if (!type.equals("lbmfqs")) return;
        for (int i = 0; i < cpuCount; i++) {
            queues[i] = new MFQScheduler(queueCount, timeslices);
        }
    }

    LBScheduler(String type, int cpuCount) {
        this(cpuCount);
        if (!type.equals("lbcfs")) return;
        for (int i = 0; i < cpuCount; i++) {
            queues[i] = new CFScheduler();
        }
    }

    @Override
    public synchronized Pcb get(int cpuId) {
        Pcb pcb = queues[cpuId].get(cpuId);
        if (pcb == null) {
            for (CountingScheduler queue : queues) {
                if (queue.getProcessCount() > 0) {
                    pcb = queue.get(cpuId);
                    break;
                }
            }
        }
        return pcb;
    }

    @Override
    public synchronized void put(Pcb pcb) {
        boolean done = false;
        if (pcb.getPreviousState() != ProcessState.CREATED) {
            int affinity = pcb.getAffinity();
            double avgProcCount = Pcb.getProcessCount() / (double) cpuCount;
            if (queues[affinity].getProcessCount() < avgProcCount) {
                queues[affinity].put(pcb);
                done = true;
            }
        }
        if (!done) {
            CountingScheduler selectedQueue = queues[0];
            long minProcessCount = Long.MAX_VALUE;
            for (CountingScheduler queue : queues) {
                if (queue.getProcessCount() < minProcessCount) {
                    selectedQueue = queue;
                    minProcessCount = queue.getProcessCount();
                }
            }
            selectedQueue.put(pcb);
        }
    }
}