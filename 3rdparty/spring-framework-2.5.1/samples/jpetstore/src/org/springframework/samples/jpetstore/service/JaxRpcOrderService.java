package org.springframework.samples.jpetstore.service;

import org.springframework.remoting.jaxrpc.ServletEndpointSupport;
import org.springframework.samples.jpetstore.domain.Order;
import org.springframework.samples.jpetstore.domain.logic.OrderService;

/**
 * JAX-RPC OrderService endpoint that simply delegates to the OrderService
 * implementation in the root web application context. Implements the plain
 * OrderService interface as service interface, just like the target bean does.
 *
 * <p>This proxy class is necessary because JAX-RPC/Axis requires a dedicated
 * endpoint class to instantiate. If an existing service needs to be exported,
 * a wrapper that extends ServletEndpointSupport for simple application context
 * access is the simplest JAX-RPC compliant way.
 *
 * <p>This is the class registered with the server-side JAX-RPC implementation.
 * In the case of Axis, this happens in "server-config.wsdd" respectively via
 * deployment calls. The Web Service tool manages the lifecycle of instances
 * of this class: A Spring application context can just be accessed here.
 *
 * <p>Note that this class does <i>not</i> implement an RMI port interface,
 * despite the JAX-RPC spec requiring this for service endpoints. Axis and
 * other JAX-RPC implementations are known to accept non-RMI endpoint classes
 * too, so there's no need to maintain an RMI port interface in addition to
 * the existing non-RMI service interface (OrderService).
 *
 * <p>If your JAX-RPC implementation imposes a strict requirement on a service
 * endpoint class to implement an RMI port interface, then let your endpoint
 * class implement both the non-RMI service interface and the RMI port interface.
 * This will work as long as the methods in both interfaces just differ in the
 * declared RemoteException. Of course, this unfortunately involves double
 * maintenance: one interface for your business logic, one for JAX-RPC.
 * Therefore, it is usually preferable to avoid this if not absolutely necessary.
 *
 * @author Juergen Hoeller
 * @since 26.12.2003
 */
public class JaxRpcOrderService extends ServletEndpointSupport implements OrderService {

	private OrderService orderService;

	protected void onInit() {
		this.orderService = (OrderService) getWebApplicationContext().getBean("petStore");
	}

	public Order getOrder(int orderId) {
		return this.orderService.getOrder(orderId);
	}

}
