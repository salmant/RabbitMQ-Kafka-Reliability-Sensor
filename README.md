# Reliability Sensor
Reliability Sensor is a Java process which measures a set of specific metrics, puts all of them in a JSON message and then periodically sends it to the message broker (e.g. RabbitMQ or Kafka) via MQTT/AMQP. There are three different types of Reliability Sensor.

## Infrastructure Reliability Sensor
Infrastructure Reliability Sensor, which is a component running on an edge node (e.g. Raspberry Pi) or a cloud-based host, measures different infrastructure-level metrics (such as CPU, memory, disk, network and so on), and periodically sends system parameters to the message broker (e.g. RabbitMQ or Kafka) via MQTT/AMQP.

<br>If the message broker is Kafka, this Java file should be used: [Infrastructure_Reliability_Sensor_Kafka.java](https://github.com/salmant/RabbitMQ-Kafka-Reliability-Sensor/blob/master/src/main/java/com/xyz/Infrastructure_Reliability_Sensor_Kafka.java)
<br>If the message broker is RabbitMQ, this Java file should be used: [Infrastructure_Reliability_Sensor_RabbitMQ.java](https://github.com/salmant/RabbitMQ-Kafka-Reliability-Sensor/blob/master/src/main/java/com/xyz/Infrastructure_Reliability_Sensor_RabbitMQ.java)

In order to execute the Java class, the following command should be executed:
<br>`java -cp target/reliability-sensor-1.0.jar com.xyz.Infrastructure_Reliability_Sensor_Kafka "Broker_IP" "Device_Id" "Device_Type" "Interval_Sec" "Group_Id" "fragid" "res_inst"`
<br>
<br>`java -cp target/reliability-sensor-1.0.jar com.xyz.Infrastructure_Reliability_Sensor_RabbitMQ "Broker_IP" "Device_Id" "Device_Type" "Interval_Sec" "Group_Id" "fragid" "res_inst"`
<br><br>Example:
<br>`java -cp target/reliability-sensor-1.0.jar com.xyz.Infrastructure_Reliability_Sensor_Kafka  "3.120.91.124" "edge4" "rpi3edge" "10" "city-yt" "f1" "195.27.104.108"`
<br>
<br>`java -cp target/reliability-sensor-1.0.jar com.xyz.Infrastructure_Reliability_Sensor_RabbitMQ  "3.120.91.124" "edge4" "rpi3edge" "10" "city-yt" "f1" "195.27.104.108"`

<br>Note: In the above commands, it should be noted that:
* `Broker_IP` is the IP address of the broker.
* `Interval_Sec` is the monitoring interval in second.
* `Device_Id` is a unique identifier for a particular device (e.g asdfk248a).
* `Device_Type` is the generic device class this device belongs to (e.g Raspberry_Pi_3).
* `Group_Id` is a location/building/other attribute, which is common for a number of devices.
* `fragid` is the name of fragment being run on the device (e.g fragments_VideoStreamer).
* `res_inst` is a unique identifier for a particular device (same as Device_Id - but included in the payload of the monitoring event). For example, it could be the IP address of the device.

## Network Reliability Sensor
Network Reliability Sensor is a component which is able to measure different characteristics of the network connection between two nodes (e.g. IoT device and edge node) such as delay, jitter, throughput and packet loss. It periodically sends network parameters to the message broker (e.g. RabbitMQ or Kafka) via MQTT/AMQP.

<br>If the message broker is Kafka, this Java file should be used: [Network_Reliability_Sensor_Kafka.java](https://github.com/salmant/RabbitMQ-Kafka-Reliability-Sensor/blob/master/src/main/java/com/xyz/Network_Reliability_Sensor_Kafka.java)
<br>If the message broker is RabbitMQ, this Java file should be used: [Network_Reliability_Sensor_RabbitMQ.java](https://github.com/salmant/RabbitMQ-Kafka-Reliability-Sensor/blob/master/src/main/java/com/xyz/Network_Reliability_Sensor_RabbitMQ.java)

In order to execute the Java class, the following command should be executed:
<br>`java -cp target/reliability-sensor-1.0.jar com.xyz.Network_Reliability_Sensor_Kafka "Broker_IP" "Device_Id" "Device_Type" "Interval_Sec" "Group_Id" "fragid" "res_inst" "target_ip_address"`
<br>
<br>`java -cp target/reliability-sensor-1.0.jar com.xyz.Network_Reliability_Sensor_RabbitMQ "Broker_IP" "Device_Id" "Device_Type" "Interval_Sec" "Group_Id" "fragid" "res_inst" "target_ip_address"`
<br><br>Example:
<br>`java -cp target/reliability-sensor-1.0.jar com.xyz.Network_Reliability_Sensor_Kafka "3.120.91.124" "edge4" "rpi3edge" "10" "city-yt" "f1" "195.27.104.108" "157.20.20.34"`
<br>
<br>`java -cp target/reliability-sensor-1.0.jar com.xyz.Network_Reliability_Sensor_RabbitMQ "3.120.91.124" "edge4" "rpi3edge" "10" "city-yt" "f1" "195.27.104.108" "157.20.20.34"`

<br>Note: Network Reliability Sensor periodically measures all end-to-end network metrics of the connection between the current host where the Network Reliability Sensor is running and `"target_ip_address"`.

## Service Reliability Sensor
Service Reliability Sensor is a component running on an edge node (e.g. Raspberry Pi) or a cloud that is able to measure different custom application-level metrics (such as response time and so on), and periodically sends service parameters to the message broker (e.g. RabbitMQ or Kafka) via MQTT/AMQP.

<br>If the message broker is Kafka, this Java file should be used: [Service_Reliability_Sensor_Kafka.java](https://github.com/salmant/RabbitMQ-Kafka-Reliability-Sensor/blob/master/src/main/java/com/xyz/Service_Reliability_Sensor_Kafka.java)
<br>If the message broker is RabbitMQ, this Java file should be used: [Service_Reliability_Sensor_RabbitMQ.java](https://github.com/salmant/RabbitMQ-Kafka-Reliability-Sensor/blob/master/src/main/java/com/xyz/Service_Reliability_Sensor_RabbitMQ.java)

In order to execute the Java class, the following command should be executed:
<br>`java -cp target/reliability-sensor-1.0.jar com.xyz.Service_Reliability_Sensor_Kafka "Broker_IP" "Device_Id" "Device_Type" "Interval_Sec" "Group_Id" "fragid" "res_inst"`
<br>
<br>`java -cp target/reliability-sensor-1.0.jar com.xyz.Service_Reliability_Sensor_RabbitMQ "Broker_IP" "Device_Id" "Device_Type" "Interval_Sec" "Group_Id" "fragid" "res_inst"`
<br><br>Example:
<br>`java -cp target/reliability-sensor-1.0.jar com.xyz.Service_Reliability_Sensor_Kafka "3.120.91.124" "edge4" "rpi3edge" "10" "city-yt" "f1" "195.27.104.108"`
<br>
<br>`java -cp target/reliability-sensor-1.0.jar com.xyz.Service_Reliability_Sensor_RabbitMQ "3.120.91.124" "edge4" "rpi3edge" "10" "city-yt" "f1" "195.27.104.108"`

<br>Note: You should change the code according to your application. In the sample code written for the Service Reliability Sensor, three metrics named "metric1" (double), "metric2" (long) and "metric3" (int) are defined. If you need more application-level metrics, you should define more metrics in the code. If you have less number of application-level metrics, you should eliminate metrics in the code. For each metric, there is a procedure which measures the value of the metric. Therefore, you should write your own code in this procedure to measure the value of your custom application-level metric.
