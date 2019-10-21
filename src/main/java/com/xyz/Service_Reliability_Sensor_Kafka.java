// ------------------------------------------------------------------------
// Author: Salman Taherizadeh - Jozef Stefan Institute (JSI)
// This code is published under the Apache 2 license
// ------------------------------------------------------------------------
package com.xyz;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import org.json.JSONObject;
import java.util.Random;
import java.io.IOException;

import java.util.Properties;


class Service_Reliability_Sensor_Kafka {
	
	public static void main(String argv[]) throws Exception {
		try{
			String Broker_IP = argv [0]; //for example: "3.120.91.124"
			String Device_Id = argv [1]; //for example: "edge4"
			String Device_Type = argv [2]; //for example: "rpi3edge"
			String Interval_Sec = argv [3]; //for example: "10"
			String Group_Id = argv [4]; //for example: "city-yt"
			String fragid = argv [5]; //for example: "f1"
			String res_inst = argv [6]; //for example: "95.87.154.150"
			String topic = "monitoring" + "-" + Group_Id + "-" + Device_Type + "-" + Device_Id;
			///////////////
			Properties props = new Properties();
			props.put("bootstrap.servers", Broker_IP + ":9092"); //props.put("bootstrap.servers", "l=localhost:9092,localhost2:9092");
			props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
			props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
			/*
			With linger.ms=0 the record is sent as soon as possible and with many requests this may impact the performance. Forcing a little wait by increasing linger.ms on moderate/high load will optimize the use of the batch and increase throughput.
			*/
			props.put("linger.ms", 0);
			KafkaProducer<String, String> producer = new KafkaProducer<>(props);
			///////////////

			while(true){
				long lasttime = System.currentTimeMillis();
				/////////////////////////////////////////////////////////
				double metric1 = 0.0;
				long metric2 = 0;
				int metric3 = 0;
				/////////////////////////////////////////////////////////
				JSONObject jsonObject;
				jsonObject = new JSONObject();
				///////////////////
				long unixTime = System.currentTimeMillis() / 1000L;
				jsonObject.put("timestamp", unixTime);
				jsonObject.put("fragid", fragid);
				jsonObject.put("res_inst", res_inst);
				
				///////////////////metric1
				metric1 = measure_metric1();
				jsonObject.put("metric1", metric1);
				
				///////////////////metric2
				metric2 = measure_metric2();
				jsonObject.put("metric2", metric2);
				
				///////////////////metric3
				metric3 = measure_metric3();
				jsonObject.put("metric3", metric3);
				
				/////////////////////////////////////////////////////////
				/*
				In general the key can be null. If the key is null a random partition will the selected.
				*/
				producer.send(new ProducerRecord<>(topic, null,jsonObject.toString()));
				System.out.println(jsonObject.toString());
				/////////////////////////////////////////////////////////
				long curtime = System.currentTimeMillis();
				Thread.sleep(Math.abs((1000*Integer.parseInt(Interval_Sec)) - (curtime-lasttime)));
			}
		} catch (Exception ex){ System.err.println(ex.getMessage()); }
	}
	
	public static double measure_metric1(){
		double metric_1;
		metric_1 = new Random().nextDouble(); // use your own code here
		return metric_1;
	}
	
	public static long measure_metric2(){
		long metric_2;
		metric_2 = new Random().nextLong(); // use your own code here
		return metric_2;
	}
	
	public static int measure_metric3(){
		int metric_3;
		metric_3 = new Random().nextInt(); // use your own code here
		return metric_3;
	}
	
}
