// ------------------------------------------------------------------------
// Author: Salman Taherizadeh - Jozef Stefan Institute (JSI)
// This code is published under the Apache 2 license
// ------------------------------------------------------------------------
package com.xyz;

import producer.MqttProducer;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.io.IOException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import java.util.HashMap;
import java.util.Map.Entry;

import java.text.DecimalFormat;

class Infrastructure_Reliability_Sensor_RabbitMQ {

	static final String PATHCPU = "/proc/stat";
	static final String PATHMEM = "/proc/meminfo";
	static final String PATHNET = "/proc/net/dev";
	static double cpu_user = 0.0;
	static double cpu_sys = 0.0;
	static double cpu_io_wait = 0.0;
	static double cpu_perc = 0.0;
	static int mem_free = 0;
	static double mem_perc = 0.0;
	static long net_rec = 0;
	static long net_sent = 0;
	static HashMap<String,Long> lastValuesCPU;
	static HashMap<String,Long> lastValuesNET;
	static long lasttime_NET;
  
	public static void main(String argv[]) throws Exception {
		try{
			String Broker_IP = argv [0]; //for example: "3.120.91.124"
			String Device_Id = argv [1]; //for example: "edge4"
			String Device_Type = argv [2]; //for example: "rpi3edge"
			String Interval_Sec = argv [3]; //for example: "10"
			String Group_Id = argv [4]; //for example: "city-yt"
			String fragid = argv [5]; //for example: "f1"
			String res_inst = argv [6]; //for example: "95.87.154.150"
			String topic = "monitoring" + "/" + Group_Id + "/" + Device_Type + "/" + Device_Id;
			MqttProducer sc = new MqttProducer(Broker_IP, topic);
			lastValuesCPU = getValuesCPU();
			lastValuesNET = getValuesNET();
			lasttime_NET = System.currentTimeMillis()/1000;
			while(true){
				long lasttime = System.currentTimeMillis();
				/////////////////////////////////////////////////////////
				long unixTime = System.currentTimeMillis() / 1000L;
				Process P;
				BufferedReader StdInput;
				String resultCom;
				JSONObject jsonObject;
				///////////////////
				jsonObject = new JSONObject();
				jsonObject.put("timestamp", unixTime);
				///////////////////
				jsonObject.put("fragid", fragid);
				jsonObject.put("res_inst", res_inst);
				///////////////////
				///////CPU
				measureValuesCPU();
				DecimalFormat numberFormat = new DecimalFormat("0.00");
				jsonObject.put("cpu_user", Double.parseDouble(numberFormat.format(cpu_user)));
				jsonObject.put("cpu_sys", Double.parseDouble(numberFormat.format(cpu_sys)));
				jsonObject.put("cpu_io_wait", Double.parseDouble(numberFormat.format(cpu_io_wait)));
				jsonObject.put("cpu_perc", Double.parseDouble(numberFormat.format(cpu_perc)));
				///////NET
				measureValuesNET();
				jsonObject.put("net_rec", net_rec);
				jsonObject.put("net_sent", net_sent);
				///////mem_free
				measureValuesMem();
				jsonObject.put("mem_free", mem_free);
				jsonObject.put("mem_perc", mem_perc);
				///////disk_available
				String[] disk_available={"/bin/bash","-c","df -k /tmp | tail -1 | awk \'{print $4}\'"};
				P = Runtime.getRuntime().exec(disk_available);
				P.waitFor();
				StdInput = new BufferedReader(new InputStreamReader(P.getInputStream()));
				resultCom= StdInput.readLine();
				double tmp_disk = Double.parseDouble(resultCom);
				tmp_disk = tmp_disk/1024;
				tmp_disk = tmp_disk/1024;
				jsonObject.put("disk_available", Double.parseDouble(numberFormat.format(tmp_disk)));
				/////////////////////////////////////////////////////////
				sc.publish(jsonObject.toString());
				/////////////////////////////////////////////////////////
				long curtime = System.currentTimeMillis();
				Thread.sleep(Math.abs((1000*Integer.parseInt(Interval_Sec)) - (curtime-lasttime)));
			}
		} catch (InterruptedException | IOException e1) {e1.printStackTrace();}
	}
	
	public static HashMap<String,Long> getValuesCPU(){
		HashMap<String,Long> stats = new HashMap<String,Long>();
		try {
			BufferedReader objReader = new BufferedReader(new FileReader(new File(PATHCPU)));
			String line;
			while((line = objReader.readLine())!=null){
				String[] metricParts = null;
				if (line.startsWith("cpu ")){
					metricParts = line.split("\\W+");
					stats.put("cpuUser", Long.parseLong(metricParts[1]));
					stats.put("cpuNice", Long.parseLong(metricParts[2]));
					stats.put("cpuSystem", Long.parseLong(metricParts[3]));
					stats.put("cpuIdle", Long.parseLong(metricParts[4]));
					stats.put("cpuIOWait", Long.parseLong(metricParts[5]));
					stats.put("cpuIrq", Long.parseLong(metricParts[6]));
					stats.put("cpuSoftIrq", Long.parseLong(metricParts[7]));
					if(metricParts.length>8)
						stats.put("cpuSteal", Long.parseLong(metricParts[8]));
					else stats.put("cpuSteal", 0L);
					break;
				}	
			}
	        objReader.close();
		} catch (FileNotFoundException e){
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    long TotalCPU = 0;
	    for (Long value : stats.values())
	        TotalCPU += value;
	    stats.put("TotalCPU", TotalCPU);	
		return stats;
	}
	
	public static void measureValuesCPU() {
		HashMap<String,Long> curValuesCPU = getValuesCPU();
		HashMap<String,Long> diffValuesCPU = new HashMap<String,Long>();
		for (Entry<String,Long> entry : lastValuesCPU.entrySet()) {
	        String key = entry.getKey();
	        Long val = entry.getValue();
			diffValuesCPU.put(key, (Long)curValuesCPU.get(key) - val);
	    }
		double CPU_Total;
	    double CPU_User;
	    double CPU_Nice;
	    double CPU_System;
	    double CPU_Idle;
	    double CPU_IOwait;
	    double TotalCPU = 1.0 * diffValuesCPU.get("TotalCPU");
	    if(TotalCPU != 0){
		    CPU_User = diffValuesCPU.get("cpuUser") / TotalCPU * 100;
	        CPU_Nice = diffValuesCPU.get("cpuNice") / TotalCPU * 100;
	        CPU_System = diffValuesCPU.get("cpuSystem") / TotalCPU * 100;
	        CPU_Total = CPU_User + CPU_Nice + CPU_System;
	        CPU_Idle = diffValuesCPU.get("cpuIdle") / TotalCPU * 100;
	        CPU_IOwait = diffValuesCPU.get("cpuIOWait") /TotalCPU * 100;
	    }
	    else{
	       	CPU_Total = 0.0;
	 	    CPU_User = 0.0;
	 	    CPU_Nice = 0.0;
	 	    CPU_System = 0.0;
	 	    CPU_Idle = 0.0;
	 	    CPU_IOwait = 0.0;
	    }
		cpu_user = CPU_User;
		cpu_sys = CPU_System;
		cpu_io_wait = CPU_IOwait;
		cpu_perc = CPU_Total;
	    lastValuesCPU = curValuesCPU;
	}
	
	public static HashMap<String,Long> getValuesNET(){
		HashMap<String,Long> stats = new HashMap<String,Long>();
		long netIn = 0;
		long netOut = 0;
		try {
			BufferedReader objReader = new BufferedReader(new FileReader(new File(PATHNET)));
			String line;
			int count_line = 0;
			while((line = objReader.readLine())!=null){
				if (count_line<2){
					count_line++;
					continue;
				}
				String[] metricParts = null;
				metricParts = line.split(":");
				if (metricParts[0].endsWith(" lo") || metricParts[0].endsWith(" bond")) continue;
				metricParts = metricParts[1].trim().split("\\W+");
				netIn += Long.parseLong(metricParts[0]);
				netOut += Long.parseLong(metricParts[8]);
			}
			objReader.close();
			stats.put("netIn", netIn);
			stats.put("netOut", netOut);
		} catch (FileNotFoundException e){
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return stats;
	}
	
	public static void measureValuesNET() {
		HashMap<String,Long> curValuesNET = getValuesNET();
		HashMap<String,Long> diffValuesNET = new HashMap<String,Long>();
		long curtime_NET = System.currentTimeMillis()/1000;
		for (Entry<String,Long> entry : lastValuesNET.entrySet()) {
	        String key = entry.getKey();
	        Long val = entry.getValue();
			diffValuesNET.put(key, (Long)curValuesNET.get(key) - val);
	    }
		long timediff_NET = curtime_NET - lasttime_NET;
		double NET_REC;
	    double NET_SENT;
		if(timediff_NET > 0){
			NET_REC = (1.0 * diffValuesNET.get("netIn")) / timediff_NET;
			NET_SENT = (1.0 * diffValuesNET.get("netOut")) / timediff_NET;
		} else{
	       	NET_REC = 0;
			NET_SENT = 0;
	    }
		net_rec = (long) NET_REC;
		net_sent = (long) NET_SENT;
		lastValuesNET = curValuesNET;
		lasttime_NET = curtime_NET;
	}
	
	public static void measureValuesMem(){
		int mem_total = 0;
		int mem_cached = 0;	
		int mem_used = 0;
		HashMap<String,Long> stats = new HashMap<String,Long>();
		try {
			BufferedReader objReader = new BufferedReader(new FileReader(new File(PATHMEM)));
			String line;
			while((line = objReader.readLine())!=null){
				if (line.startsWith("MemFree")){
					mem_free = Integer.parseInt((line.split("\\W+")[1]));
					continue;
				} else
				if (line.startsWith("MemTotal")){
					mem_total = Integer.parseInt((line.split("\\W+")[1]));
					continue;
				} else
				if (line.startsWith("memCached")){
					mem_cached = Integer.parseInt((line.split("\\W+")[1]));
					continue;
				} else
				if (line.startsWith("SwapFree")) break;
			}
	        objReader.close();
			mem_used = mem_total - mem_free - mem_cached;
			mem_perc = (100.0 * mem_used) / mem_total;
		} catch (FileNotFoundException e){
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
