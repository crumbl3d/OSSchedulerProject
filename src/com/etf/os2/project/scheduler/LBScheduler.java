package com.etf.os2.project.scheduler;

import com.etf.os2.project.process.Pcb;
import com.etf.os2.project.process.Pcb.ProcessState;

public class LBScheduler extends CountingScheduler {

    private int cpuCount;
    private CountingScheduler[] queues;

    private LBScheduler(int cpuCount) {
        this.cpuCount = cpuCount;
        queues = new CountingScheduler[cpuCount];
    }

    static LBScheduler createSJF(int cpuCount, double alpha, boolean preemption) {
        LBScheduler scheduler = new LBScheduler(cpuCount);
        for (int i = 0; i < cpuCount; i++) {
            scheduler.queues[i] = new SJFScheduler(alpha, preemption);
        }
        return scheduler;
    }

    static LBScheduler createMFQS(int cpuCount, int queueCount, long[] timeslices) {
        LBScheduler scheduler = new LBScheduler(cpuCount);
        for (int i = 0; i < cpuCount; i++) {
            scheduler.queues[i] = new MFQScheduler(queueCount, timeslices);
        }
        return scheduler;
    }

    static LBScheduler createCFS(int cpuCount) {
        LBScheduler scheduler = new LBScheduler(cpuCount);
        for (int i = 0; i < scheduler.cpuCount; i++) {
            scheduler.queues[i] = new CFScheduler();
        }
        return scheduler;
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
        if (pcb != null) {
            processCount--;
        }
        return pcb;
    }

    @Override
    public synchronized void put(Pcb pcb) {
        boolean done = false;
        if (pcb.getPreviousState() != ProcessState.CREATED) {
            int affinity = pcb.getAffinity();
//            double avgProcCount = Pcb.getProcessCount() / (double) cpuCount;
            double avgProcCount = processCount / (double) cpuCount;
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
        processCount++;
    }
}