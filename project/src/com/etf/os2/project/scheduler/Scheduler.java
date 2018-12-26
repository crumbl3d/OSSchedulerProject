package com.etf.os2.project.scheduler;

import com.etf.os2.project.process.Pcb;

public abstract class Scheduler {
    public abstract Pcb get(int cpuId);

    public abstract void put(Pcb pcb);

    public static Scheduler createScheduler(String[] args) {
        if (args.length == 0)
            return null;
        switch (args[0]) {
        case "sjf":
            double alpha = 0.5;
            boolean preemption = true;
            if (args.length > 1)
                alpha = Double.parseDouble(args[1]);
            if (args.length > 2)
                preemption = Boolean.parseBoolean(args[2]);
            return new SJFScheduler(alpha, preemption);
        case "mfqs":
            if (args.length < 3)
                return null;
            int numberOfQueues = Integer.parseInt(args[1]);
            if (args.length - 2 < numberOfQueues)
                return null;
            long timeslices[] = new long[numberOfQueues];
            for (int i = 0; i < numberOfQueues; i++) {
                timeslices[i] = Long.parseLong(args[2 + i]);
            }
            return new MFQScheduler(numberOfQueues, timeslices);
        case "cfs":
            return new CFScheduler();
        default:
            return null;
        }
    }
}