package com.etf.os2.project.scheduler;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

import com.etf.os2.project.process.Pcb;
import com.etf.os2.project.process.PcbData;
import com.etf.os2.project.process.Pcb.ProcessState;

public class SJFScheduler extends Scheduler {

    private class SJFData extends PcbData {

        double prediction;
        double startTime;

        SJFData(double prediction) {
            this.prediction = prediction;
        }

        double getCurrentExecutionTime() {
            return Pcb.getCurrentTime() - startTime;
        }
    }

    private static final double MULTIPLIER = 10;

    private double alpha;
    private boolean preemption;
    private Queue<Pcb> queue;

    SJFScheduler(double alpha, boolean preemption) {
        if (alpha < 0) alpha = 0;
        if (alpha > 1) alpha = 1;
        this.alpha = alpha;
        this.preemption = preemption;
        queue = new PriorityQueue<>(Comparator.comparingDouble(x -> ((SJFData) x.getPcbData()).prediction));
    }

    @Override
    public synchronized Pcb get(int cpuId) {
        Pcb pcb = queue.poll();
        if (pcb != null) {
            SJFData data = (SJFData) pcb.getPcbData();
            data.startTime = Pcb.getCurrentTime();
            pcb.setTimeslice(0);
//            System.out.println("GET CPU" + cpuId + " prediction = " + data.prediction + ": " + pcb.getId());
//        } else {
//            System.out.println("GET CPU" + cpuId + ": IDLE");
        }
        return pcb;
    }

    @Override
    public synchronized void put(Pcb pcb) {
        ProcessState prevState = pcb.getPreviousState();
        SJFData data = (SJFData) pcb.getPcbData();
        if (prevState == ProcessState.CREATED) {
            pcb.setPcbData(data = new SJFData(pcb.getPriority() * MULTIPLIER));
        }
        data.prediction = alpha * pcb.getExecutionTime() + (1 - alpha) * data.prediction;
        queue.offer(pcb);
        if (preemption) {
            double maxRemaining = Double.MIN_VALUE;
            int preemptCpu = -1;
            for (int i = 0; i < Pcb.RUNNING.length; i++) {
                if (Pcb.RUNNING[i] == Pcb.IDLE) continue;
                SJFData tempData = (SJFData) Pcb.RUNNING[i].getPcbData();
                double timeLeft = tempData.prediction - tempData.getCurrentExecutionTime();
                if (timeLeft > maxRemaining) {
                    maxRemaining = timeLeft;
                    preemptCpu = i;
                }
            }
            if (preemptCpu > -1) {
                Pcb.RUNNING[preemptCpu].preempt();
//                System.out.print("Preempting CPU" + preemptCpu + "! ");
            }
        }
//        System.out.println("PUT prediction = " + data.prediction + ": " + pcb.getId());
    }
}