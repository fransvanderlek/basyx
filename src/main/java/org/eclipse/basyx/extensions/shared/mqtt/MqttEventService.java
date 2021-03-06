/*******************************************************************************
 * Copyright (C) 2021 the Eclipse BaSyx Authors
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.basyx.extensions.shared.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of common parts of MQTT event propagation services.
 * Extend this class to make a service MQTT extendable
 *  
 * @author haque
 *
 */
public class MqttEventService {
	private static Logger logger = LoggerFactory.getLogger(MqttEventService.class);

	// The MQTTClient
	protected MqttClient mqttClient;

	// QoS for MQTT messages (1, 2 or 3).
	protected int qos = 1;
	
	/**
	 * Constructor for creating an MqttClient (no authentication)
	 * @param serverEndpoint
	 * @param clientId
	 * @throws MqttException
	 */
	public MqttEventService(String serverEndpoint, String clientId) throws MqttException {
		this.mqttClient = new MqttClient(serverEndpoint, clientId, new MqttDefaultFilePersistence());
		mqttClient.connect();
	}
	
	/**
	 * Constructor for creating an MqttClient with authentication
	 * @param serverEndpoint
	 * @param clientId
	 * @param user
	 * @param pw
	 * @throws MqttException
	 */
	public MqttEventService(String serverEndpoint, String clientId, String user, char[] pw)
			throws MqttException {
		this.mqttClient = new MqttClient(serverEndpoint, clientId, new MqttDefaultFilePersistence());
		MqttConnectOptions options = new MqttConnectOptions();
		options.setUserName(user);
		options.setPassword(pw);
		mqttClient.connect(options);
	}
	
	/**
	 * Constructor for creating an MqttClient with existing client
	 * @param client
	 * @throws MqttException
	 */
	public MqttEventService(MqttClient client) throws MqttException {
		this.mqttClient = client;
		mqttClient.connect();
	}

	/**
	 * Sets the QoS for MQTT messages
	 * 
	 * @param qos
	 */
	public void setQoS(int qos) {
		if (qos >= 0 && qos <= 3) {
			this.qos = qos;
		} else {
			throw new IllegalArgumentException("Invalid QoS: " + qos);
		}
	}

	/**
	 * Gets the QoS for MQTT messages
	 */
	public int getQoS() {
		return this.qos;
	}
	
	/**
	 * Sends MQTT message to connected broker
	 * @param topic in which the message will be published
	 * @param payload the actual message
	 */
	protected void sendMqttMessage(String topic, String payload) {
		MqttMessage msg = new MqttMessage(payload.getBytes());
		if (this.qos != 1) {
			msg.setQos(this.qos);
		}
		try {
			logger.debug("Send MQTT message to " + topic + ": " + payload);
			mqttClient.publish(topic, msg);
		} catch (MqttPersistenceException e) {
			logger.error("Could not persist mqtt message", e);
		} catch (MqttException e) {
			logger.error("Could not send mqtt message", e);
		}
	}
}
