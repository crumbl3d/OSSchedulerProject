package com.etf.os2.project.system;

import com.etf.os2.project.process.Pcb;
import com.etf.os2.project.process.Process;
import com.etf.os2.project.scheduler.Scheduler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class System {
    private final int processorNumber;
    private final List<Process> processes;
    private final List<Process> finishedProcess = new ArrayList<Process>();
    private final Cpu[] cpus;
    private final Scheduler scheduler;
    private long time = -1;

    public System(Scheduler scheduler, int processorNumber, List<Process> processes) {
        this.processorNumber = processorNumber;
        this.processes = processes;
        this.scheduler = scheduler;

        Pcb.RUNNING = new Pcb[processorNumber];
        cpus = new Cpu[processorNumber];
        for (int i = 0; i < processorNumber; i++) {
            Pcb.RUNNING[i] = Pcb.IDLE;
            cpus[i] = new Cpu(i);
        }
    }

    public void work() {
        while (processes.size() > 0) {
            long nextActivation = getNextActivation();
            assert nextActivation != Long.MAX_VALUE;

            if (nextActivation > time) {
                time = nextActivation;
            } else {
                time++;
            }

            stepExecution();

            preempt();

            finishProcesses();
        }

        // TODO: restore this after testing
        double avgResponseTime = 0;
        double minResponseTime = Double.MAX_VALUE;
        double maxResponseTime = Double.MIN_VALUE;
        
        for (Process proc : finishedProcess) {
            proc.writeResults();
            avgResponseTime += proc.stats.getResponseTime();
            if (proc.stats.getResponseTime() < minResponseTime)
                minResponseTime = proc.stats.getResponseTime();
            if (proc.stats.getResponseTime() > maxResponseTime)
                maxResponseTime = proc.stats.getResponseTime();
        }

        avgResponseTime /= finishedProcess.size();

        java.lang.System.out.println("Min response time: " + minResponseTime);
        java.lang.System.out.println("Max response time: " + maxResponseTime);
        java.lang.System.out.println("Average response time: " + avgResponseTime);
        java.lang.System.out.println("System execution time: " + time);
    }

    private void finishProcesses() {
        Iterator<Process> iter = processes.iterator();
        while (iter.hasNext()) {
            Process p = iter.next();
            if (p.isFinished()) {
                iter.remove();
                finishedProcess.add(p);
                Process.removeProcess();
            }
        }
    }

    private void preempt() {
        for (Cpu cpu : cpus) {
            cpu.tryToPreempt(scheduler);
        }
    }

    private void stepExecution() {
        for (Process p : processes) {
            p.step(scheduler, time);
        }

        for (Cpu cpu : cpus) {
            cpu.step(scheduler, time);
        }
    }

    private long getNextActivation() {
        long nextActivation = Long.MAX_VALUE;
        for (Process p : processes) {
            long next = p.getNextEventTime();
            if (next < nextActivation) {
                nextActivation = next;
            }
        }

        for (Cpu cpu : cpus) {
            long next = cpu.getNextEventTime();
            if (next < nextActivation) {
                nextActivation = next;
            }
        }
        return nextActivation;
    }
}
