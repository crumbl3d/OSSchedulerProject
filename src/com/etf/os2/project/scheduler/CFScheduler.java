package com.etf.os2.project.scheduler;

import com.etf.os2.project.process.Pcb;
import com.etf.os2.project.process.PcbData;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

public class CFScheduler extends Scheduler {

	private class CFSData extends PcbData {

		private long entryTime;

		CFSData() {
			this.entryTime = Pcb.getCurrentTime();
		}

		double getEntryTime() { return entryTime; }

		void setEntryTime(long entryTime) { this.entryTime = entryTime; }

		long getWaitTime() {
			return Pcb.getCurrentTime() - entryTime;
		}
	}

	private Queue<Pcb> queue;

	CFScheduler() {
		queue = new PriorityQueue<>(Comparator.comparingDouble(Pcb::getExecutionTime));
	}

	@Override
	public Pcb get(int cpuId) {
		Pcb nextPcb = queue.poll();
		long timeslice = 0;
		if (nextPcb == null) {
			nextPcb = Pcb.IDLE;
		} else {
			CFSData data = (CFSData) nextPcb.getPcbData();
			if (Pcb.getProcessCount() == 0) {
				timeslice = 0; // this should never happen!!!
			} else {
				int processCount = Pcb.getProcessCount();
				timeslice = (data.getWaitTime() + processCount - 1) / processCount; // ceil...
			}
		}
		System.out.println("GET CPU" + cpuId + " timeslice = " + timeslice + ": " + nextPcb.getId());
		nextPcb.setTimeslice(timeslice);
		return nextPcb;
	}

	@Override
	public void put(Pcb pcb) {
		Pcb.ProcessState prevState = pcb.getPreviousState();
		CFSData data = (CFSData) pcb.getPcbData();

		if (prevState == Pcb.ProcessState.CREATED) {
			pcb.setPcbData(data = new CFSData()); // automatically sets entryTime
		} else {
			data.setEntryTime(Pcb.getCurrentTime());
		}

		queue.offer(pcb);
		pcb.setTimeslice(0);

		System.out.println("PUT entryTime = " + data.getEntryTime() + ": " + pcb.getId());
	}
}