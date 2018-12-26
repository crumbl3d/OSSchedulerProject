package com.etf.os2.project.scheduler;

import com.etf.os2.project.process.Pcb;
import com.etf.os2.project.process.Pcb.ProcessState;
import com.etf.os2.project.process.PcbData;

import java.util.LinkedList;
import java.util.Queue;

public class MFQScheduler extends Scheduler {

	private class MFQData extends PcbData {

		private int priority;
		
		public MFQData(int priority) {
			this.priority = priority;
		}
	}
	
	private int numberOfQueues;
	private long timeslices[];
	private Queue<Pcb> queues[];

	@SuppressWarnings("unchecked")
	public MFQScheduler(int numberOfQueues, long timeslices[]) {
		this.numberOfQueues = numberOfQueues;
		this.timeslices = timeslices;
		queues = new Queue[numberOfQueues];
		for (int i = 0; i < numberOfQueues; i++) {
			queues[i] = new LinkedList<Pcb>();
		}
	}

	@Override
	public Pcb get(int cpuId) {
		Pcb nextPcb = null;
		for (int i = 0; i < numberOfQueues; i++) {
			nextPcb = queues[i].poll();
			if (nextPcb != null) break;
		}
		if (nextPcb == null) nextPcb = Pcb.IDLE;
//		System.out.println("GET CPU" + cpuId + ": " + nextPcb.getId());
		return nextPcb;
	}

	@Override
	public void put(Pcb pcb) {
		ProcessState prevState = pcb.getPreviousState();
		MFQData data = (MFQData) pcb.getPcbData();
		if (prevState == ProcessState.CREATED) {
			pcb.setPcbData(data = new MFQData(pcb.getPriority()));
			if (data.priority >= numberOfQueues) {
				data.priority = numberOfQueues - 1;
			}
		} else if (prevState == ProcessState.BLOCKED) {
            if (data.priority > 0) {
                data.priority--;
            }
		} else {
            if (data.priority < numberOfQueues - 1) {
                data.priority++;
            }
		}
		queues[data.priority].offer(pcb);
		pcb.setTimeslice(timeslices[data.priority]);
//		System.out.println("PUT " + data.priority + ": " + pcb.getId());
	}
}