package test;

import coffeeshop.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class CoffeeShopReportStreamsTest {
    static private List<SimulationEvent> simulationEventList;
    static private final CoffeeShopReportStreams coffeeShopReportStreams = new CoffeeShopReportStreams();
    static int numCustomers = 10;
    static int numCooks = 5;
    static int numTables = 5;
    static int machineCapacity = 2;
    static boolean randomOrders = true;

    @BeforeAll
    static void initialize() {
        Customer.setRunningCounter(0);
        if (simulationEventList == null)
            simulationEventList = Simulation.runSimulation(
                    numCustomers,
                    numCooks,
                    numTables,
                    machineCapacity,
                    randomOrders
            );

    }


    @Test
    void testGetMapOrdersByCook() {
        Map<Cook, Collection<List<Food>>> ordersByCook = coffeeShopReportStreams.getMapOrdersByCook(
                simulationEventList);
        assertEquals(numCooks, ordersByCook.size());


        int numOrders = ordersByCook.values().stream().mapToInt(Collection::size).sum();
        assertEquals(numCustomers, numOrders);
    }

    @Test
    void testGetOrdersByCook() {
        int sum = 0;
        Set<Cook> cookSet = new HashSet<>();
        for (SimulationEvent e : simulationEventList) {
            if (e.getEvent() == SimulationEvent.EventType.CookReceivedOrder
                && !cookSet.contains(e.getCook())) {
                sum += coffeeShopReportStreams.getOrdersByCook(simulationEventList, e.getCook()).size();
                cookSet.add(e.getCook());
            }
        }

        assertEquals(sum, numCustomers);
    }

    @Test
    void testGetFoodByType() {
        long totalFoodOrdered = coffeeShopReportStreams.getFoodCountByType(simulationEventList)
                .values()
                .stream()
                .reduce(Long::sum)
                .orElse(0L);

        long totalBurgersOrdered = coffeeShopReportStreams.getNumBurgersCooked(simulationEventList);
        long totalFriesOrdered = coffeeShopReportStreams.getNumFriesCooked(simulationEventList);
        long totalCoffeeOrdered = coffeeShopReportStreams.getNumCoffeeCooked(simulationEventList);

        assertEquals(totalCoffeeOrdered + totalBurgersOrdered + totalFriesOrdered, totalFoodOrdered);
    }

    @Test
    void testGetPercentageOrdersByCook() {
        Map<Cook, Double> ordersPercentageByCook = coffeeShopReportStreams
                .getPercentageOrdersByCook(simulationEventList);

        Double totalOrdersPercentage = ordersPercentageByCook
                .values()
                .stream()
                .reduce(Double::sum)
                .orElse(0.0);

        System.out.println(totalOrdersPercentage);
        assertEquals(100.0, totalOrdersPercentage, 0.01);
    }

    @Test
    void testGetAverageCookingTimeByCook() {
        Map<Cook, Double> averageCookingTime = coffeeShopReportStreams
                .getAverageCookingTimeByCook(simulationEventList);
        Long timeThreshold = 3000L; // 3000 ms average cooking time

        System.out.println(averageCookingTime);
        Map<Cook, Double> concerningCookingTimes = averageCookingTime
                .entrySet()
                .stream()
                .filter(e -> e.getValue() > timeThreshold)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        concerningCookingTimes.forEach(((k, v) -> System.out.println(k + "'s average time is slow! (" + v + "ms)")));

        assertTrue(concerningCookingTimes.isEmpty());
    }
}
