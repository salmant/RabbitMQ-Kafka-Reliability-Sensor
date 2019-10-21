// ------------------------------------------------------------------------
// Author: Salman Taherizadeh - Jozef Stefan Institute (JSI)
// This code is published under the Apache 2 license
// ------------------------------------------------------------------------
package com.xyz;

import producer.MqttProducer;
import org.json.JSONObject;
import java.util.Random;
import java.io.IOException;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.OutputStreamWriter;

class Network_Reliability_Sensor_RabbitMQ {
	
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
			
			String target_ip_address = argv [7];
			int l = 500; //ICMPPacketLength  
			int m = 10; //NumberOfPacketsPerTest
			int c = 5; //NumberOfTests
			int p = 0; //IntervalBetweenTests

			while(true){
				long lasttime = System.currentTimeMillis();
				/////////////////////////////////////////////////////////
				JSONObject jsonObject;
				jsonObject = new JSONObject();
				///////////////////
				long unixTime = System.currentTimeMillis() / 1000L;
				jsonObject.put("timestamp", unixTime);
				jsonObject.put("fragid", fragid);
				jsonObject.put("res_inst", res_inst);
				
				///////////////////measuring metrics
				double packet_loss=0.0;
				double average_rtt=0.0;
				double network_throughput=0.0;
				double jitter=0.0;
				String[] command={"ping", "-c", String.valueOf(m), "-w", String.valueOf(((int)(0.2 * m))), "-s", String.valueOf(l-8), target_ip_address, "-i", "0.2"};
				double nums[]=new double[m];
				String line; //read the input stream line by line
				try {
					int count = 0; 
					while(count < c){
						int pings = 0; //it will be the number of received ping packets out of m; it should be set zero every try
						Process sysProcess = Runtime.getRuntime().exec(command); //create and execute a system command
						sysProcess.waitFor();
						BufferedReader streamReader = new BufferedReader(new InputStreamReader(sysProcess.getInputStream())); //create an input stream reader 
						while((line = streamReader.readLine()) != null) {
							if(line.startsWith(String.valueOf(l) + " bytes from ")) {
								line=line.substring(line.indexOf("time=") + 5, line.indexOf(" ms"));
								nums[pings]=Double.parseDouble(line);
								pings++; //This is a positive response so we increment pings
							} else 
							if(line.startsWith(String.valueOf(m) + " packets transmitted, ")) {
								line=line.substring(line.indexOf("received, ") + 9, line.indexOf("%")); //packet loss
								packet_loss = packet_loss + Double.parseDouble(line);
							} else 
							if((line.startsWith("rtt"))||(line.startsWith("round-trip"))) {
								line=line.replace("rtt min/avg/max/mdev = ", "");
								line=line.replace("round-trip min/avg/max/stddev = ", "");
								line=line.replace(" ms", ""); 
								String[] splitted_metrics = line.split("/");
								double temp_average_rtt=Double.parseDouble(splitted_metrics[1]); //average time | splitted_metrics[0] -> minimum time | splitted_metrics[2] -> maximum time | splitted_metrics[3] -> mean deviation
								average_rtt = average_rtt + temp_average_rtt; 
								network_throughput= network_throughput + ((l * 1000 / (temp_average_rtt/2)) / 1024); //throughput (KB/s)
								//////jitter//////
								double sum_temp = 0;
								for (int i = 0; i < pings; i++) sum_temp += nums[i];
								double mean = sum_temp / pings;
								double squareSum = 0;
								for (int i = 0; i < pings; i++) squareSum += Math.pow(nums[i] - mean, 2);
								jitter = jitter + (Math.sqrt((squareSum) / (pings))); //maybe "pings - 1" instead of "pings"
							}
						}
						count++;
						Thread.sleep(p);
					}
					packet_loss = packet_loss/c;
					average_rtt = average_rtt/c;
					network_throughput = network_throughput/c;
					jitter = jitter/c;
					jsonObject.put("packet_loss", packet_loss);
					jsonObject.put("average_rtt", average_rtt);
					jsonObject.put("network_throughput", network_throughput);
					jsonObject.put("jitter", jitter);
					jsonObject.put("target_ip_address", target_ip_address);
				} catch (Exception ex){ System.err.println(ex.getMessage()); }
				/////////////////////////////////////////////////////////
				sc.publish(jsonObject.toString());
				/////////////////////////////////////////////////////////
				long curtime = System.currentTimeMillis();
				Thread.sleep(Math.abs((1000*Integer.parseInt(Interval_Sec)) - (curtime-lasttime)));
			}
		} catch (InterruptedException | IOException e1) {e1.printStackTrace();}
	}
}
