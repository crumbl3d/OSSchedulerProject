package com.etf.os2.project.scheduler;

import com.etf.os2.project.process.Pcb;

public abstract class Scheduler {
    public abstract Pcb get(int cpuId);

    public abstract void put(Pcb pcb);

    public static Scheduler createScheduler(String[] args) {
        if (args.length == 0) return null;
        int parsedCount = 0;
        String type = args[parsedCount++];
        int cpuCount = 1;
        if (type.startsWith("lb")) {
            cpuCount = Integer.parseInt(args[parsedCount++]);
        }
        switch (type) {
            case "sjf" :
            case "lbsjf" : {
                double alpha = 0.5;
                boolean preemption = true;
                if (args.length > parsedCount) {
                    alpha = Double.parseDouble(args[parsedCount++]);
                }
                if (args.length > parsedCount) {
                    preemption = Boolean.parseBoolean(args[parsedCount]);
                }
                if (type.equals("sjf")) {
                    return new SJFScheduler(alpha, preemption);
                } else {
                    return LBScheduler.createSJF(cpuCount, alpha, preemption);
                }
            }
            case "mfqs" :
            case "lbmfqs" : {
                int queueCount = 1;
                long[] timeslices;
                if (args.length > parsedCount) {
                    queueCount = Integer.parseInt(args[parsedCount++]);
                    if (queueCount < 1 || args.length - parsedCount < queueCount) {
                        return null;
                    }
                    timeslices = new long[queueCount];
                    for (int i = 0; i < queueCount; i++) {
                        timeslices[i] = Long.parseLong(args[parsedCount++]);
                    }
                } else {
                    timeslices = new long[queueCount];
                    for (int i = 0; i < queueCount; i++) {
                        timeslices[i] = queueCount - i - 1;
                    }
                }
                if (type.equals("mfqs")) {
                    return new MFQScheduler(queueCount, timeslices);
                } else {
                    return LBScheduler.createMFQS(cpuCount, queueCount, timeslices);
                }
            }
            case "cfs": return new CFScheduler();
            case "lbcfs": return LBScheduler.createCFS(cpuCount);
            default: return null;
        }
    }
}