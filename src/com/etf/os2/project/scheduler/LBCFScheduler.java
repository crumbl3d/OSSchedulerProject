package com.etf.os2.project.scheduler;

import com.etf.os2.project.process.Pcb;
import com.etf.os2.project.process.PcbData;

import java.util.*;

public class LBCFScheduler extends Scheduler {

    private class LBCFSData extends PcbData {

        long entryTime;

        LBCFSData() {
            this.entryTime = Pcb.getCurrentTime();
        }

        long getWaitTime() {
            return Pcb.getCurrentTime() - entryTime;
        }
    }

    private int cpuCount;
    private Queue<Pcb>[] queues;

    public LBCFScheduler(int cpuCount) {
//        this.cpuCount = cpuCount;
//        queues = new Queue[cpuCount];
//        for (int i = 0; i < cpuCount; i++) {
//            queues[i] = new PriorityQueue<>(Comparator.comparingDouble(Pcb::getExecutionTime)
//        }
    }

    @Override
    public synchronized Pcb get(int cpuId) {
//        Pcb pcb = queues[cpuId].poll();
//        if (pcb == null) {
//            for (Queue<Pcb> queue : queues) {
//                if (queue.getProcessCount() > 0) {
//                    pcb = queue.get();
//                    break;
//                }
//            }
//        }
//        return pcb;
//        Pcb pcb = queue.poll();
//        if (pcb != null) {
//            CFScheduler.CFSData data = (CFScheduler.CFSData) pcb.getPcbData();
//            long timeslice = 0;
//            if (Pcb.getProcessCount() > 0) {
//                timeslice = (data.getWaitTime() + Pcb.getProcessCount() - 1) / Pcb.getProcessCount();
//            }
//            pcb.setTimeslice(timeslice);
////            System.out.println("GET CPU" + cpuId + " timeslice = " + pcb.getTimeslice() + ": " + pcb.getId());
////        } else {
////            System.out.println("GET CPU" + cpuId + ": IDLE");
//        }
//        return pcb;
        return null;
    }

    @Override
    public synchronized void put(Pcb pcb) {
//        CFScheduler.CFSData data = (CFScheduler.CFSData) pcb.getPcbData();
//        if (pcb.getPreviousState() == Pcb.ProcessState.CREATED) {
//            pcb.setPcbData(new CFScheduler.CFSData()); // automatically sets entryTime...
//        } else {
//            data.entryTime = Pcb.getCurrentTime();
//        }
//        queue.offer(pcb);
////        System.out.println("PUT entryTime = " + data.getEntryTime() + ": " + pcb.getId());
    }
}