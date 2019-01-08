package com.etf.os2.project.scheduler;

import com.etf.os2.project.process.Pcb;
import com.etf.os2.project.process.Pcb.ProcessState;
import com.etf.os2.project.process.PcbData;

import java.util.LinkedList;
import java.util.Queue;

public class LBMFQScheduler extends Scheduler {

    private class MFQSData extends PcbData {

        int priority;

        MFQSData(int priority) { this.priority = priority; }
    }

    private class CPUQueue {

        private int queueCount;
        private int processCount = 0;
        private long[] timeslices;
        private Queue<Pcb>[] queues;

        @SuppressWarnings("unchecked")
        CPUQueue(int queueCount, long[] timeslices) {
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
                MFQSData data = (MFQSData) pcb.getPcbData();
                pcb.setTimeslice(timeslices[data.priority]);
                processCount--;
            }
            return pcb;
        }

        void put(Pcb pcb) {
            ProcessState prevState = pcb.getPreviousState();
            MFQSData data = (MFQSData) pcb.getPcbData();
            if (prevState == ProcessState.CREATED) {
                pcb.setPcbData(data = new MFQSData(pcb.getPriority()));
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
    private int processCount = 0;
    private CPUQueue[] queues;

    LBMFQScheduler(int cpuCount, int queueCount, long[] timeslices) {
        this.cpuCount = cpuCount;
        queues = new CPUQueue[cpuCount];
        for (int i = 0; i < cpuCount; i++) {
            queues[i] = new CPUQueue(queueCount, timeslices);
        }
    }

    @Override
    public Pcb get(int cpuId) {
        Pcb pcb = null;
        if (queues[cpuId].processCount > 0) {
            pcb = queues[cpuId].get();
        } else {
            for (CPUQueue queue : queues) {
                if (queue.processCount > 0) {
                    pcb = queue.get();
                }
            }
        }
        if (pcb != null) {
            processCount--;
        }
        return pcb;
    }

    @Override
    public void put(Pcb pcb) {
        boolean done = false;
        if (pcb.getPreviousState() != ProcessState.CREATED) {
            int affinity = pcb.getAffinity();
            double avgProcCount = processCount / (double) cpuCount;
            if (queues[affinity].processCount < avgProcCount) {
                queues[affinity].put(pcb);
                done = true;
            }
        }
        if (!done) {
            CPUQueue selectedQueue = null;
            long minProcessCount = Long.MAX_VALUE;
            for (CPUQueue queue : queues) {
                if (queue.getProcessCount() < minProcessCount) {
                    selectedQueue = queue;
                    minProcessCount = queue.getProcessCount();
                }
            }
            if (selectedQueue != null) {
                selectedQueue.put(pcb);
                processCount++;
            }
        }
    }
}