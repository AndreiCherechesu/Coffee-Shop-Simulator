package coffeeshop;

import java.util.LinkedList;
import java.util.Queue;

/**
 * A Machine is used to make a particular Food.  Each Machine makes
 * just one kind of Food.  Each machine has a capacity: it can make
 * that many food items in parallel; if the machine is asked to
 * produce a food item beyond its capacity, the requester blocks.
 * Each food item takes at least item.cookTimeMS milliseconds to
 * produce.
 */
public class Machine {
    public final String machineName;
    public final Food machineFoodType;
    int capacity;
    Queue<Food> foodList;

    public Machine(String nameIn, Food foodIn, int capacityIn) {
        this.machineName = nameIn;
        this.machineFoodType = foodIn;
        this.capacity = capacityIn;
        this.foodList = new LinkedList<Food>();
    }

    public String getMachineName() {
        return machineName;
    }

    public Food getMachineFoodType() {
        return machineFoodType;
    }

    public int getCapacity() {
        return capacity;
    }

    public Queue<Food> getFoodList() {
        return foodList;
    }

    /**
     * This method is called by a Cook in order to make the Machine's
     * food item.
     */
    public void makeFood(Cook name, int orderNum) throws InterruptedException {
        foodList.add(machineFoodType);
        Thread curr = new Thread(new CookAnItem(name, orderNum));
        curr.start();
    }

    public String toString() {
        return machineName;
    }

    private class CookAnItem implements Runnable {
        Cook currCook;
        int orderNum;

        public CookAnItem(Cook currCook, int orderNum) {
            this.currCook = currCook;
            this.orderNum = orderNum;
        }

        public void run() {
            try {
                /* Machine starts to cook food -> log event */
                Simulation.logEvent(SimulationEvent.machineCookingFood(Machine.this, machineFoodType));

                /* Sleep to simulate different cooking time per each food type */
                Thread.sleep(machineFoodType.cookTimeMS);

                /* Cooking is done -> log event that machine and cook finished the food */
                Simulation.logEvent(SimulationEvent.machineDoneFood(Machine.this, machineFoodType));
                Simulation.logEvent(SimulationEvent.cookFinishedFood(currCook, machineFoodType, orderNum));

                /* Remove the cooked food from the to-do list */
                synchronized (foodList) {
                    foodList.remove();
                    foodList.notifyAll();
                }

                /* Add food to finished food list */
                synchronized (currCook.finishedFood) {
                    currCook.finishedFood.add(machineFoodType);
                    currCook.finishedFood.notifyAll();
                }


            } catch (InterruptedException e) {
                /* Machine gets interrupted -> shut down */
            }
        }
    }
}