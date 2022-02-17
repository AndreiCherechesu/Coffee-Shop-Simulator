package coffeeshop;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * Cooks are simulation actors that have at least one field, a name.
 * When running, a cook attempts to retrieve outstanding orders placed
 * by Eaters and process them.
 */
public class Cook implements Runnable {
    private final String name;
    private final Map<Integer, Pair<Instant, Instant>> orderStartEnd = new HashMap<>();
    public List<Food> finishedFood = new LinkedList<Food>();
    private Customer currCustomer;

    public Cook(String name) {
        this.name = name;
    }

    public Customer getCurrCustomer() {
        return currCustomer;
    }

    public List<Food> getFinishedFood() {
        return finishedFood;
    }

    public Map<Integer, Pair<Instant, Instant>> getOrderStartEnd() {
        return orderStartEnd;
    }

    public String toString() {
        return name;
    }

    /**
     * This method executes as follows.  The cook tries to retrieve
     * orders placed by Customers.  For each order, a List<Food>, the
     * cook submits each Food item in the List to an appropriate
     * Machine, by calling makeFood().  Once all machines have
     * produced the desired Food, the order is complete, and the Customer
     * is notified.  The cook can then go to process the next order.
     * If during its execution the cook is interrupted (i.e., some
     * other thread calls the interrupt() method on it, which could
     * raise InterruptedException if the cook is blocking), then it
     * terminates.
     */
    public void run() {

        Simulation.logEvent(SimulationEvent.cookStarting(this));
        try {
            while (true) {

                // Get the customer currently up next and take its order
                synchronized (Simulation.orderList) {

                    while (Simulation.orderList.isEmpty()) {
                        Simulation.orderList.wait();
                    }
                    currCustomer = Simulation.orderList.remove();
                    Simulation.logEvent(SimulationEvent.cookReceivedOrder(this,
                            currCustomer.getOrder(), currCustomer.getOrderNum()));
                    Simulation.orderList.notifyAll();
                }
                //sends food to specific machine
                for (int index = 0; index < currCustomer.getOrder().size(); index++) {
                    Food currFood = currCustomer.getOrder().get(index);
                    if (currFood.equals(FoodType.burger)) {
                        synchronized (Simulation.grill.getFoodList()) {
                            while (!(Simulation.grill.getFoodList().size() < Simulation.grill.getCapacity())) {
                                Simulation.grill.getFoodList().wait();
                            }
                            Simulation.logEvent(SimulationEvent.cookStartedFood(this,
                                    FoodType.burger, currCustomer.getOrderNum()));
                            Simulation.grill.makeFood(this, currCustomer.getOrderNum());
                            Simulation.grill.getFoodList().notifyAll();

                        }

                    } else if (currFood.equals(FoodType.fries)) {
                        synchronized (Simulation.fryer.getFoodList()) {
                            while (!(Simulation.fryer.getFoodList().size() < Simulation.fryer.getCapacity())) {
                                Simulation.fryer.getFoodList().wait();
                            }
                            Simulation.logEvent(SimulationEvent.cookStartedFood(this,
                                    FoodType.fries, currCustomer.getOrderNum()));
                            Simulation.fryer.makeFood(this, currCustomer.getOrderNum());
                            Simulation.fryer.getFoodList().notifyAll();

                        }

                    } else {
                        synchronized (Simulation.coffeeMaker2000.getFoodList()) {
                            while (!(Simulation.coffeeMaker2000.getFoodList().size() < Simulation.coffeeMaker2000.getCapacity())) {
                                Simulation.coffeeMaker2000.getFoodList().wait();
                            }
                            Simulation.logEvent(SimulationEvent.cookStartedFood(this,
                                    FoodType.coffee, currCustomer.getOrderNum()));
                            Simulation.coffeeMaker2000.makeFood(this, currCustomer.getOrderNum());
                            Simulation.coffeeMaker2000.getFoodList().notifyAll();

                        }
                    }
                }
                synchronized (finishedFood) {
                    while (!(finishedFood.size() == currCustomer.getOrder().size())) {
                        finishedFood.wait();
                        finishedFood.notifyAll();
                    }
                }
                Simulation.logEvent(SimulationEvent.cookCompletedOrder(this, currCustomer.getOrderNum()));

                synchronized (Simulation.completedOrder) {
                    Simulation.completedOrder.put(currCustomer, true);
                    Simulation.completedOrder.notifyAll();
                }
                finishedFood = new LinkedList<Food>();

            }
        } catch (InterruptedException e) {
            // Cooks are interrupted after all customers leave
            Simulation.logEvent(SimulationEvent.cookEnding(this));
        }
    }
}