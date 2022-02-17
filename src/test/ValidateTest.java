package test;

import coffeeshop.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ValidateTest {
    static private List<SimulationEvent> events;
    static private final CoffeeShopReportStreams coffeeShopReportStreams = new CoffeeShopReportStreams();
    static int numCustomers = 10;
    static int numCooks = 5;
    static int numTables = 5;
    static int machineCapacity = 2;
    static boolean randomOrders = true;

    @BeforeAll
    static void initialize() {
        Customer.setRunningCounter(0);
        if (events == null)
            events = Simulation.runSimulation(
                    numCustomers,
                    numCooks,
                    numTables,
                    machineCapacity,
                    randomOrders
            );
    }

    @Test
    void customerLeaveTest() {
        // Customer should not leave coffee shop until order is received test
        int orderReceivedIndex = 0;
        int customerLeftIndex = 0;
        for (int i = 0; i < numCustomers; i++) {
            for (int j = 0; j < events.size(); j++) {
                if (events.get(j).toString().contains("Customer " + i)) {
                    if (events.get(j).getEvent() == SimulationEvent.EventType.CustomerLeavingCoffeeShop) {
                        customerLeftIndex = j;
                    }
                    if (events.get(j).getEvent() == SimulationEvent.EventType.CustomerReceivedOrder) {
                        orderReceivedIndex = j;
                    }
                }
            }
            assertFalse(orderReceivedIndex > customerLeftIndex);
        }

    }

    @Test
    public void restaurantCapacityCheck() {

        int currNumCustomers = 0;
        // Max Customers Test
        for (SimulationEvent e : events) {
            if (e.getEvent() == SimulationEvent.EventType.CustomerEnteredCoffeeShop) {
                currNumCustomers++;
            }
            if (e.getEvent() == SimulationEvent.EventType.CustomerLeavingCoffeeShop) {
                currNumCustomers--;
            }
            assertFalse(currNumCustomers > numTables);
        }
    }

    @Test
    public void machineCapacityCheck() {
        int currNumBurgers = 0;
        int currNumFries = 0;
        int currNumCoffees = 0;
        // Machine Capacity Test
        for (SimulationEvent e : events) {
            if (e.getEvent() == SimulationEvent.EventType.MachineStartingFood) {
                if (e.getMachine().getMachineName().equals("Grill")) {
                    currNumBurgers++;
                } else if (e.getMachine().getMachineName().equals("Fryer")) {
                    currNumFries++;
                } else if (e.getMachine().getMachineName().equals("CoffeeMaker2000")) {
                    currNumCoffees++;
                }
            }
            else if (e.getEvent() == SimulationEvent.EventType.MachineDoneFood) {
                if (e.getMachine().getMachineName().equals("Grill")) {
                    currNumBurgers--;
                } else if (e.getMachine().getMachineName().equals("Fryer")) {
                    currNumFries--;
                } else if (e.getMachine().getMachineName().equals("CoffeeMaker2000")) {
                    currNumCoffees--;
                }
            }
            assertFalse(currNumBurgers > machineCapacity);
            assertFalse(currNumFries > machineCapacity);
            assertFalse(currNumCoffees > machineCapacity);
        }
    }

    @Test
    public void customerEnteredBeforePlacing() {
        boolean[] customerEntered = new boolean[numCustomers];
        boolean[] customerPlacedOrder = new boolean[numCustomers];

        for (SimulationEvent e : events) {
            if (e.getEvent() == SimulationEvent.EventType.CustomerEnteredCoffeeShop) {
                String name = e.getCustomer().toString();
                int custNum = Integer.parseInt(name.substring(9));
                customerEntered[custNum] = true;
            }
            if (e.getEvent() == SimulationEvent.EventType.CustomerPlacedOrder) {
                String name = e.getCustomer().toString();
                int custNum = Integer.parseInt(name.substring(9));
                customerPlacedOrder[custNum] = true;
                assertFalse(customerPlacedOrder[custNum] && !customerEntered[custNum]);
            }
        }
    }

    @Test
    public void machineFinishesFoodBeforeCookFinishesFood() {
        Map<Food, Integer> finishedFood = new HashMap<>();
        finishedFood.put(FoodType.fries, 0);
        finishedFood.put(FoodType.burger, 0);
        finishedFood.put(FoodType.coffee, 0);

        Map<Food, Integer> takenFood = new HashMap<>();
        takenFood.put(FoodType.fries, 0);
        takenFood.put(FoodType.burger, 0);
        takenFood.put(FoodType.coffee, 0);

        for (SimulationEvent e : events) {
            if (e.getEvent() == SimulationEvent.EventType.CookFinishedFood) {
                takenFood.put(e.getFood(), takenFood.get(e.getFood()) + 1);
                assertTrue(takenFood.get(e.getFood()) <= finishedFood.get(e.getFood()));
            } else if (e.getEvent() == SimulationEvent.EventType.MachineDoneFood) {
                finishedFood.put(e.getFood(), finishedFood.get(e.getFood()) + 1);
            }
        }
    }

    @Test
    public void cookCompletesBeforeCustomerReceives() {
        boolean[] customerReceived = new boolean[numCustomers];
        boolean[] cookCompleted = new boolean[numCustomers];
        boolean result = true;
        for (SimulationEvent e : events) {
            if (e.getEvent() == SimulationEvent.EventType.CustomerReceivedOrder) {
                int order = e.getOrderNumber();
                customerReceived[order] = true;
                if (customerReceived[order] && !cookCompleted[order]) {
                    result = false;
                }
            }
            if (e.getEvent() == SimulationEvent.EventType.CookCompletedOrder) {
                int order = e.getOrderNumber();
                cookCompleted[order] = true;
            }
        }
        assertTrue(result);
    }

    @Test
    public void customerReceivedBeforeLeaving() {
        boolean[] customerReceived = new boolean[numCustomers];
        boolean[] customerLeft = new boolean[numCustomers];
        boolean result = true;
        for (SimulationEvent e : events) {
            if (e.getEvent() == SimulationEvent.EventType.CustomerReceivedOrder) {
                String name = e.getCustomer().toString();
                int custNum = Integer.parseInt(name.substring(9));
                customerReceived[custNum] = true;
            }
            if (e.getEvent() == SimulationEvent.EventType.CustomerLeavingCoffeeShop) {
                String name = e.getCustomer().toString();
                int custNum = Integer.parseInt(name.substring(9));
                customerLeft[custNum] = true;
                if (customerLeft[custNum] && !customerReceived[custNum]) {
                    result = false;
                }
            }
        }
        assertTrue(result);
    }

    @Test
    public void allCustomersServed() {
        boolean result = true;
        boolean[] customerServed = new boolean[numCustomers];
        for (SimulationEvent e : events) {
            if (e.getEvent() == SimulationEvent.EventType.CustomerLeavingCoffeeShop) {
                String name = e.getCustomer().toString();
                int custNum = Integer.parseInt(name.substring(9));
                customerServed[custNum] = true;
            }
        }
        for (boolean served : customerServed) {
            if (!served) {
                result = false;
                break;
            }
        }
        assertTrue(result);
    }

    /**
     * Tests that cooks don't take orders before customers place them
     */
    @Test
    public void customerBeforeCookOrders() {
        HashSet<Integer> set = new HashSet<Integer>();
        boolean passed = true;

        for (SimulationEvent event : events) {
            if (event.getEvent() == SimulationEvent.EventType.CustomerPlacedOrder) {
                set.add(event.getOrderNumber());
            }
            if (event.getEvent() == SimulationEvent.EventType.CookReceivedOrder) {
                if (!set.contains(event.getOrderNumber())) {
                    passed = false;
                }
            }

        }
        assertTrue(passed);
    }

    /**
     * Tests that cooks dont start food for an order that hasn't been received
     */
    @Test
    public void cookTakesOrderBeforeCooksStartsFood() {
        HashSet<Integer> set = new HashSet<Integer>();
        boolean passed = true;

        for (SimulationEvent event : events) {
            if (event.getEvent() == SimulationEvent.EventType.CookReceivedOrder) {
                set.add(event.getOrderNumber());
            }
            if (event.getEvent() == SimulationEvent.EventType.CookStartedFood) {
                if (!set.contains(event.getOrderNumber())) {
                    passed = false;
                }
            }

        }
        assertTrue(passed);
    }

    /**
     * Tests that cooks dont start food for an order that hasn't been received
     */
    @Test
    public void cookStartsFoodBeforeMachine() {
        boolean passed = true;

        int numBurgers = 0;
        int numFries = 0;
        int numCoffee = 0;

        for (SimulationEvent event : events) {
            if (event.getEvent() == SimulationEvent.EventType.CookStartedFood) {
                if (event.getFood() == FoodType.burger) {
                    numBurgers++;
                }
                if (event.getFood() == FoodType.fries) {
                    numFries++;
                }
                if (event.getFood() == FoodType.coffee) {
                    numCoffee++;
                }
            }
            if (event.getEvent() == SimulationEvent.EventType.MachineStartingFood) {
                if (event.getFood() == FoodType.burger) {
                    numBurgers--;
                }
                if (event.getFood() == FoodType.fries) {
                    numFries--;
                }
                if (event.getFood() == FoodType.coffee) {
                    numCoffee--;
                }
            }

            if (numBurgers < 0 || numFries < 0 || numCoffee < 0) {
                passed = false;
            }
        }
        assertTrue(passed);
    }

    /**
     * Tests that cooks dont start food for an order that hasn't been received
     */
    @Test
    public void machineStartsFoodBeforeFinishes() {
        boolean passed = true;

        int numBurgers = 0;
        int numFries = 0;
        int numCoffee = 0;

        for (SimulationEvent event : events) {
            if (event.getEvent() == SimulationEvent.EventType.MachineStartingFood) {
                if (event.getFood() == FoodType.burger) {
                    numBurgers++;
                }
                if (event.getFood() == FoodType.fries) {
                    numFries++;
                }
                if (event.getFood() == FoodType.coffee) {
                    numCoffee++;
                }
            }
            if (event.getEvent() == SimulationEvent.EventType.MachineDoneFood) {
                if (event.getFood() == FoodType.burger) {
                    numBurgers--;
                }
                if (event.getFood() == FoodType.fries) {
                    numFries--;
                }
                if (event.getFood() == FoodType.coffee) {
                    numCoffee--;
                }
            }

            if (numBurgers < 0 || numFries < 0 || numCoffee < 0) {
                passed = false;
            }
        }
        assertTrue(passed);
    }

    /**
     * Tests that cooks dont start food for an order that hasn't been received
     */
    @Test
    public void cookFinishesFoodsBeforeCompletesOrder() {
        boolean passed = true;
        HashSet<Integer> set = new HashSet<Integer>();

        for (SimulationEvent event : events) {
            if (event.getEvent() == SimulationEvent.EventType.CookCompletedOrder) {
                set.add(event.getOrderNumber());
            }
            if (event.getEvent() == SimulationEvent.EventType.CookFinishedFood) {
                if (set.contains(event.getOrderNumber())) {
                    passed = false;
                }
            }
        }
        assertTrue(passed);
    }
}
