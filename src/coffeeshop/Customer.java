package coffeeshop;

import java.util.List;

/**
 * Customers are simulation actors that have two fields: a name, and a list
 * of Food items that constitute the Customer's order.  When running, an
 * customer attempts to enter the coffee shop (only successful if the
 * coffee shop has a free table), place its order, and then leave the
 * coffee shop when the order is complete.
 */
public class Customer implements Runnable {
    private static int runningCounter = 0;
    private final String name;
    private final List<Food> order;
    private final int orderNum;

    public Customer(String name, List<Food> order) {
        this.name = name;
        this.order = order;
        this.orderNum = runningCounter++;
    }

    public static void setRunningCounter(int runningCounter) {
        Customer.runningCounter = runningCounter;
    }

    public String toString() {
        return name;
    }

    public List<Food> getOrder() {
        return this.order;
    }

    public int getOrderNum() {
        return this.orderNum;
    }

    public void run() {
        /* Log customer starting event */
        Simulation.logEvent(SimulationEvent.customerStarting(this));

        /* Wait to enter the Coffee Shop */
        synchronized (Simulation.currCapacity) {
            while (!(Simulation.currCapacity.size() < Simulation.events.get(0).simParams[2])) {
                try {
                    Simulation.currCapacity.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            /* Customer entered -> log event */
            Simulation.currCapacity.add(this);
            Simulation.logEvent(SimulationEvent.customerEnteredCoffeeShop(this));
            Simulation.currCapacity.notifyAll();
        }

        /* Wait to place order */
        synchronized (Simulation.orderList) {
            Simulation.orderList.add(this);
            Simulation.logEvent(SimulationEvent.customerPlacedOrder(this, this.order, this.orderNum));
            Simulation.orderList.notifyAll();
        }

        /* Initialize order in completedOrder HashMap as not completed yet */
        synchronized (Simulation.completedOrder) {
            Simulation.completedOrder.put(this, false);
        }

        /* Wait for order to be completed */
        synchronized (Simulation.completedOrder) {
            while (!(Simulation.completedOrder.get(this))) {
                try {
                    Simulation.completedOrder.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            /* Order complete -> log customer received order event */
            Simulation.logEvent(SimulationEvent.customerReceivedOrder(this, this.order, this.orderNum));
            Simulation.completedOrder.notifyAll();
        }

        /* Exit the Coffee Shop */
        synchronized (Simulation.currCapacity) {
            Simulation.currCapacity.remove(this);
            Simulation.logEvent(SimulationEvent.customerLeavingCoffeeShop(this));
            Simulation.currCapacity.notifyAll();
        }
    }
}