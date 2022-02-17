package coffeeshop;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CoffeeShopReportStreams {
    public List<List<Food>> getOrdersByCook(List<SimulationEvent> events, Cook cook) {
        return events
                .stream()
                .filter(event -> event.getEvent() == SimulationEvent.EventType.CookReceivedOrder)
                .filter(event -> event.getCook() == cook)
                .map(SimulationEvent::getOrderFood)
                .collect(Collectors.toList());
    }

    public Map<Cook, Collection<List<Food>>> getMapOrdersByCook(List<SimulationEvent> events) {
        return events
                .stream()
                .filter(event -> event.getEvent() == SimulationEvent.EventType.CookReceivedOrder)
                .collect(Collectors.groupingBy(SimulationEvent::getCook, Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> list.stream().map(SimulationEvent::getOrderFood).collect(Collectors.toList()))));
    }

    public long getNumBurgersCooked(List<SimulationEvent> events) {
        return events
                .stream()
                .filter(event -> event.getEvent() == SimulationEvent.EventType.CookReceivedOrder)
                .map(SimulationEvent::getOrderFood)
                .flatMap(List::stream)
                .filter(food -> food == FoodType.burger)
                .count();
    }

    public long getNumFriesCooked(List<SimulationEvent> events) {
        return events
                .stream()
                .filter(event -> event.getEvent() == SimulationEvent.EventType.CookReceivedOrder)
                .map(SimulationEvent::getOrderFood)
                .flatMap(List::stream)
                .filter(food -> food == FoodType.fries)
                .count();
    }

    public long getNumCoffeeCooked(List<SimulationEvent> events) {
        return events
                .stream()
                .filter(event -> event.getEvent() == SimulationEvent.EventType.CookReceivedOrder)
                .map(SimulationEvent::getOrderFood)
                .flatMap(List::stream)
                .filter(food -> food == FoodType.coffee)
                .count();
    }

    public Map<String, Long> getFoodCountByType(List<SimulationEvent> events) {
        return events
                .stream()
                .filter(event -> event.getEvent() == SimulationEvent.EventType.CookReceivedOrder)
                .map(SimulationEvent::getOrderFood)
                .flatMap(List::stream)
                .collect(Collectors.groupingBy(Food::toString, Collectors.counting()));
    }

    public Map<Cook, Long> getOrdersNumByCook(List<SimulationEvent> events) {
        return events
                .stream()
                .filter(event -> event.getEvent() == SimulationEvent.EventType.CookCompletedOrder)
                .map(SimulationEvent::getCook)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

    public Map<Cook, Double> getPercentageOrdersByCook(List<SimulationEvent> events) {
        Map<Cook, Long> ordersNumByCook = getOrdersNumByCook(events);
        Long numTotalOrders = ordersNumByCook
                .values()
                .stream()
                .reduce(Long::sum)
                .orElse(0L);

        return ordersNumByCook
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().doubleValue() * 100 / numTotalOrders));
    }

    public Map<Cook, Double> getAverageCookingTimeByCook(List<SimulationEvent> events) {
        return events
                .stream()
                .filter(event -> event.getEvent() == SimulationEvent.EventType.CookEnding)
                .map(SimulationEvent::getCook)
                .collect(Collectors.toMap(Function.identity(), cook -> {
                            Map<Integer, Pair<Instant, Instant>> orderStartEnd = cook.getOrderStartEnd();
                            return orderStartEnd
                                    .values()
                                    .stream()
                                    .mapToLong(pair -> Duration.between(pair.getK(), pair.getV()).toMillis())
                                    .average()
                                    .orElse(Double.MAX_VALUE);
                        }
                ));
    }
}

