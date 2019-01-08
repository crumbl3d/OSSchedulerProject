package com.etf.os2.project.scheduler;

import com.etf.os2.project.process.Pcb;
import com.etf.os2.project.process.PcbData;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

public class CFScheduler extends Scheduler {

    private class CFSData extends PcbData {

        long entryTime;

        CFSData() {
            this.entryTime = Pcb.getCurrentTime();
        }

        long getWaitTime() {
            return Pcb.getCurrentTime() - entryTime;
        }
    }

    private Queue<Pcb> queue;

    CFScheduler() {
        queue = new PriorityQueue<>(Comparator.comparingDouble(Pcb::getExecutionTime));
    }

    @Override
    public synchronized Pcb get(int cpuId) {
        Pcb pcb = queue.poll();
        if (pcb != null) {
            CFSData data = (CFSData) pcb.getPcbData();
            long timeslice = 0;
            if (Pcb.getProcessCount() > 0) {
                timeslice = (data.getWaitTime() + Pcb.getProcessCount() - 1) / Pcb.getProcessCount();
            }
            pcb.setTimeslice(timeslice);
//            System.out.println("GET CPU" + cpuId + " timeslice = " + pcb.getTimeslice() + ": " + pcb.getId());
//        } else {
//            System.out.println("GET CPU" + cpuId + ": IDLE");
        }
        return pcb;
    }

    @Override
    public synchronized void put(Pcb pcb) {
        CFSData data = (CFSData) pcb.getPcbData();
        if (pcb.getPreviousState() == Pcb.ProcessState.CREATED) {
            pcb.setPcbData(new CFSData()); // automatically sets entryTime...
        } else {
            data.entryTime = Pcb.getCurrentTime();
        }
        queue.offer(pcb);
//        System.out.println("PUT entryTime = " + data.getEntryTime() + ": " + pcb.getId());
    }
}