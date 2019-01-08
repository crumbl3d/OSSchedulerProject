package com.etf.os2.project.scheduler;

import com.etf.os2.project.process.Pcb;
import com.etf.os2.project.process.Pcb.ProcessState;
import com.etf.os2.project.process.PcbData;

import java.util.LinkedList;
import java.util.Queue;

public class LBMFQScheduler extends Scheduler {

    private class LBMFQSData extends PcbData {

        int priority;

        LBMFQSData(int priority) { this.priority = priority; }
    }

    private class LBMFQSQueue {

        private int queueCount;
        private int processCount = 0;
        private long[] timeslices;
        private Queue<Pcb>[] queues;

        @SuppressWarnings("unchecked")
        LBMFQSQueue(int queueCount, long[] timeslices) {
            this.queueCount = queueCount;
            this.timeslices = timeslices;
            queues = new Queue[queueCount];
            for (int i = 0; i < queueCount; i++) {
                queues[i] = new LinkedList<>();
            }
        }

        long getProcessCount() { return processCount; }

        Pcb get() {
            Pcb pcb = null;
            for (Queue<Pcb> queue : queues) {
                pcb = queue.poll();
                if (pcb != null) break;
            }
            if (pcb != null) {
                LBMFQSData data = (LBMFQSData) pcb.getPcbData();
                pcb.setTimeslice(timeslices[data.priority]);
                processCount--;
            }
            return pcb;
        }

        void put(Pcb pcb) {
            ProcessState prevState = pcb.getPreviousState();
            LBMFQSData data = (LBMFQSData) pcb.getPcbData();
            if (prevState == ProcessState.CREATED) {
                pcb.setPcbData(data = new LBMFQSData(pcb.getPriority()));
                if (data.priority >= queueCount) {
                    data.priority = queueCount - 1; // limit process priority
                }
            } else if (prevState == ProcessState.BLOCKED) {
                if (data.priority > 0) {
                    data.priority--; // increase process priority
                }
            } else {
                if (data.priority < queueCount - 1) {
                    data.priority++; // decrease process priority
                }
            }
            queues[data.priority].offer(pcb);
            processCount++;
        }
    }

    private int cpuCount;
    private LBMFQSQueue[] queues;

    LBMFQScheduler(int cpuCount, int queueCount, long[] timeslices) {
        this.cpuCount = cpuCount;
        queues = new LBMFQSQueue[cpuCount];
        for (int i = 0; i < cpuCount; i++) {
            queues[i] = new LBMFQSQueue(queueCount, timeslices);
        }
    }

    @Override
    public synchronized Pcb get(int cpuId) {
        Pcb pcb = queues[cpuId].get();
        if (pcb == null) {
            for (LBMFQSQueue queue : queues) {
                if (queue.getProcessCount() > 0) {
                    pcb = queue.get();
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
            LBMFQSQueue selectedQueue = queues[0];
            long minProcessCount = Long.MAX_VALUE;
            for (LBMFQSQueue queue : queues) {
                if (queue.getProcessCount() < minProcessCount) {
                    selectedQueue = queue;
                    minProcessCount = queue.getProcessCount();
                }
            }
            selectedQueue.put(pcb);
        }
    }
}