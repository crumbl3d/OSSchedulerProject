package com.etf.os2.project.scheduler;

import com.etf.os2.project.process.Pcb;
import com.etf.os2.project.process.Pcb.ProcessState;
import com.etf.os2.project.process.PcbData;

import java.util.LinkedList;
import java.util.Queue;

public class MFQScheduler extends CountingScheduler {

    private class MFQSData extends PcbData {

        int priority;

        MFQSData(int priority) { this.priority = priority; }
    }

    private int queueCount;
    private long[] timeslices;
    private Queue<Pcb>[] queues;

    @SuppressWarnings("unchecked")
    MFQScheduler(int queueCount, long[] timeslices) {
        this.queueCount = queueCount;
        this.timeslices = timeslices;
        queues = new Queue[queueCount];
        for (int i = 0; i < queueCount; i++) {
            queues[i] = new LinkedList<>();
        }
    }

    @Override
    public synchronized Pcb get(int cpuId) {
        Pcb pcb = null;
        for (Queue<Pcb> queue : queues) {
            pcb = queue.poll();
            if (pcb != null) break;
        }
        if (pcb != null) {
            MFQSData data = (MFQSData) pcb.getPcbData();
            pcb.setTimeslice(timeslices[data.priority]);
            processCount--;
//            System.out.println("GET CPU" + cpuId + " timeslice = " + pcb.getTimeslice() + ": " + pcb.getId());
//        } else {
//            System.out.println("GET CPU" + cpuId + ": IDLE");
        }
        return pcb;
    }

    @Override
    public synchronized void put(Pcb pcb) {
        MFQSData data = (MFQSData) pcb.getPcbData();
        if (pcb.getPreviousState() == ProcessState.CREATED) {
            pcb.setPcbData(data = new MFQSData(pcb.getPriority()));
            if (data.priority >= queueCount) {
                data.priority = queueCount - 1; // limit process priority
            }
        } else if (pcb.getPreviousState() == ProcessState.BLOCKED) {
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
//        System.out.println("PUT " + data.priority + ": " + pcb.getId());
    }
}