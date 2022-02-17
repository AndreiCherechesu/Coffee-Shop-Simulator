package coffeeshop;

import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;

/**
 * Simulation is the main class used to run the simulation.
 */
public class Simulation {
    // List to track simulation events during simulation
    public static List<SimulationEvent> events;

    public static Queue<Customer> orderList = new LinkedList<Customer>();
    public static Queue<Customer> currCapacity = new LinkedList<Customer>();
    public static Map<Customer, Boolean> completedOrder = new HashMap<Customer, Boolean>();
    public static Machine grill;
    public static Machine fryer;
    public static Machine coffeeMaker2000;


    /**
     * Used by other classes in the simulation to log events
     *
     * @param event
     */
    public static void logEvent(SimulationEvent event) {
        events.add(event);
        System.out.println(event);
    }


    /**
     * Function responsible for performing the simulation. Returns a List of
     * SimulationEvent objects, constructed any way you see fit. This method is
     * called from Simulation.main().
     * <p>
     * Parameters:
     *
     * @param numCustomers    the number of customers wanting to enter the coffee shop
     * @param numCooks        the number of cooks in the simulation
     * @param numTables       the number of tables in the coffee shop (i.e. coffee shop capacity)
     * @param machineCapacity the capacity of all machines in the coffee shop
     * @param randomOrders    a flag say whether to give each customer a random order
     */
    public static List<SimulationEvent> runSimulation(
            int numCustomers, int numCooks,
            int numTables,
            int machineCapacity,
            boolean randomOrders
    ) {

        // Create a synchronizedList to log all events
        events = Collections.synchronizedList(new ArrayList<SimulationEvent>());

        // Start the simulation
        logEvent(SimulationEvent.startSimulation(numCustomers,
                numCooks,
                numTables,
                machineCapacity));

        // Create Machine threads
        grill = new Machine("Grill", FoodType.burger, machineCapacity);
        fryer = new Machine("Fryer", FoodType.fries, machineCapacity);
        coffeeMaker2000 = new Machine("CoffeeMaker2000", FoodType.coffee, machineCapacity);
        logEvent(SimulationEvent.machineStarting(grill, FoodType.burger, machineCapacity));
        logEvent(SimulationEvent.machineStarting(coffeeMaker2000, FoodType.coffee, machineCapacity));
        logEvent(SimulationEvent.machineStarting(fryer, FoodType.fries, machineCapacity));


        // Create Cook threads
        Thread[] cooks = new Thread[numCooks];
        for (int index = 0; index < numCooks; index++) {
            cooks[index] = new Thread(
                    new Cook("Cook " + index));
        }
        for (int index = 0; index < numCooks; index++) {
            cooks[index].start();
        }


        // Build the customers, each with an order assigned.
        Thread[] customers = new Thread[numCustomers];
        LinkedList<Food> order;
        if (!randomOrders) {
            order = new LinkedList<Food>();
            order.add(FoodType.burger);
            order.add(FoodType.fries);
            order.add(FoodType.fries);
            order.add(FoodType.coffee);

            for (int i = 0; i < customers.length; i++) {
                customers[i] = new Thread(
                        new Customer("Customer " + i, order)
                );
            }

        } else {
            for (int i = 0; i < customers.length; i++) {
                Random rnd = new Random();
                int burgerCount = rnd.nextInt(3);
                int friesCount = rnd.nextInt(3);
                int coffeeCount = rnd.nextInt(3);

                order = new LinkedList<Food>();
                for (int b = 0; b < burgerCount; b++) {
                    order.add(FoodType.burger);
                }
                for (int f = 0; f < friesCount; f++) {
                    order.add(FoodType.fries);
                }
                for (int c = 0; c < coffeeCount; c++) {
                    order.add(FoodType.coffee);
                }

                customers[i] = new Thread(
                        new Customer("Customer " + (i), order)
                );
            }
        }

        // Start the customers -> they will try to enter the Coffee Shop
        for (Thread customer : customers) {
            customer.start();
        }

        try {
            // Wait for the customer threads to end
            for (Thread customer : customers) {
                customer.join();
            }

            // Interrupt cooks and wait for them to end
            for (Thread cook : cooks) cook.interrupt();
            for (Thread cook : cooks) cook.join();

        } catch (InterruptedException e) {
            System.out.println("Simulation thread interrupted.");
        }

        // Shut down machines
        logEvent(SimulationEvent.machineEnding(grill));
        logEvent(SimulationEvent.machineEnding(fryer));
        logEvent(SimulationEvent.machineEnding(coffeeMaker2000));

        // Done with simulation
        logEvent(SimulationEvent.endSimulation());

        return events;
    }

    /*
     * Entry point for the simulation.
     */
    public static void main(String[] args) throws InterruptedException {

        int numCustomers = 100;
        int numCooks = 20;
        int numTables = 50;
        int machineCapacity = 4;
        boolean randomOrders = true;

        runSimulation(numCustomers, numCooks, numTables, machineCapacity, randomOrders);
    }

}



