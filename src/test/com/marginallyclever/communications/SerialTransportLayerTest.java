package com.marginallyclever.communications;

import org.junit.jupiter.api.Test;

import com.marginallyclever.communications.serial.SerialTransportLayer;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SerialTransportLayerTest {
	@Test
	public void scanConnections() {
		SerialTransportLayer layer = new SerialTransportLayer();

		String [] connectionNames = SerialTransportLayer.listConnections();
		if(connectionNames.length<=0) {
			System.out.println("No serial connections found.  Test inconclusive.");
			return;
		}

		for (String connectionName : connectionNames) {
			NetworkSession c = layer.openConnection(connectionName);
			if (c != null) {
				assertNotNull(c.getName());
				System.out.println("Found SerialTransportLayer " + c.getName());
				c.closeConnection();
			}
		}
	}
}

