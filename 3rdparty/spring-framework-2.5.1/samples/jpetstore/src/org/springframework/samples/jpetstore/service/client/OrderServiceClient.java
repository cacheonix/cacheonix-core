package org.springframework.samples.jpetstore.service.client;

import java.util.Iterator;
import java.util.Map;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.samples.jpetstore.domain.LineItem;
import org.springframework.samples.jpetstore.domain.Order;
import org.springframework.samples.jpetstore.domain.logic.OrderService;
import org.springframework.util.StopWatch;

/**
 * Demo client class for remote OrderServices, to be invoked as standalone
 * program from the command line, e.g. via "client.bat" or "run.xml".
 *
 * <p>You need to specify an order ID and optionally a number of calls,
 * e.g. for order ID 1000: 'client 1000' for a single call per service or
 * 'client 1000 10' for 10 calls each".
 *
 * <p>Reads in the application context from a "clientContext.xml" file in
 * the VM execution directory, calling all OrderService proxies defined in it.
 * See that file for details.
 *
 * @author Juergen Hoeller
 * @since 26.12.2003
 * @see org.springframework.samples.jpetstore.domain.logic.OrderService
 */
public class OrderServiceClient {

	public static final String CLIENT_CONTEXT_CONFIG_LOCATION = "clientContext.xml";


	private final ListableBeanFactory beanFactory;

	public OrderServiceClient(ListableBeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	public void invokeOrderServices(int orderId, int nrOfCalls) {
		StopWatch stopWatch = new StopWatch(nrOfCalls + " OrderService call(s)");
		Map orderServices = this.beanFactory.getBeansOfType(OrderService.class);
		for (Iterator it = orderServices.keySet().iterator(); it.hasNext();) {
			String beanName = (String) it.next();
			OrderService orderService = (OrderService) orderServices.get(beanName);
			System.out.println("Calling OrderService '" + beanName + "' with order ID " + orderId);
			stopWatch.start(beanName);
			Order order = null;
			for (int i = 0; i < nrOfCalls; i++) {
				order = orderService.getOrder(orderId);
			}
			stopWatch.stop();
			if (order != null) {
				printOrder(order);
			}
			else {
				System.out.println("Order with ID " + orderId + " not found");
			}
			System.out.println();
		}
		System.out.println(stopWatch.prettyPrint());
	}

	protected void printOrder(Order order) {
		System.out.println("Got order with order ID " + order.getOrderId() +
				" and order date " + order.getOrderDate());
		System.out.println("Shipping address is: " + order.getShipAddress1());
		for (Iterator lineItems = order.getLineItems().iterator(); lineItems.hasNext();) {
			LineItem lineItem = (LineItem) lineItems.next();
			System.out.println("LineItem " + lineItem.getLineNumber() + ": " + lineItem.getQuantity() +
					" piece(s) of item " + lineItem.getItemId());
		}
	}


	public static void main(String[] args) {
		if (args.length == 0 || "".equals(args[0])) {
			System.out.println(
					"You need to specify an order ID and optionally a number of calls, e.g. for order ID 1000: " +
					"'client 1000' for a single call per service or 'client 1000 10' for 10 calls each");
		}
		else {
			int orderId = Integer.parseInt(args[0]);
			int nrOfCalls = 1;
			if (args.length > 1 && !"".equals(args[1])) {
				nrOfCalls = Integer.parseInt(args[1]);
			}
			ListableBeanFactory beanFactory = new FileSystemXmlApplicationContext(CLIENT_CONTEXT_CONFIG_LOCATION);
			OrderServiceClient client = new OrderServiceClient(beanFactory);
			client.invokeOrderServices(orderId, nrOfCalls);
		}
	}

}
