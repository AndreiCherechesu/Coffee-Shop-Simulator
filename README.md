# Coffee Shop Simulator

A Java multi-threaded application which simulates the workflow in a Coffee Shop, using Streams and JUnit5 for testing.

## Functional Specifications

There are 3 types of actor classes which implement the **Runnable** interface and run concurrently to perform their tasks:
    
- **Cook**: responsible for taking **Customers**' orders out of the order queue and use the **Machines** to prepare them,
    waiting until there are orders in the queue and until the required machines (depending on what food the order requires)
    are free to be used;

- **Customer**: attempts to firstly enter the Coffee Shop (waits until there are free spots), then submits the order
to the order queue, then waits until the order is completed and leaves the Coffee Shop

- **Machine**: instantiated by the main class (**Simulation**), like the previous 2 actors, waits until a **Cook** attempts
to use the machine and then cooks its food type (**Burger** if it's a **Grill**, **Coffee** if it's a **Coffee Machine** or **Fries**
if it's a **Fryer**); once the food is ready, each having a different cooking type, the responsible **Cook** takes it and
frees the **Machine** instance;

The **Simulation** class acts as a main class, in which all the runnable classes are instantiated, and who gives the **Customers**
randomly assigned order contents to enter the Coffee Shop with. After all the customers have entered the Coffee Shop,
submitted their orders, taken their food and then left, the running **Cook** and **Machine** threads are interrupted.

The **SimulationEvent** class is used by the other classes to log all the events that happen during the execution of the
application. It contains a private enum class **EventType** which contains all types of events than can be logged, namely:
_SimulationStarting_, _SimulationEnded_, _CustomerStarting_, _CustomerEnteredCoffeeShop_, _CustomerPlacedOrder_, _CustomerReceivedOrder_,
_CustomerLeavingCoffeeShop_, _CookStarting_, _CookReceivedOrder_, _CookStartedFood_, _CookFinishedFood_, _CookCompletedOrder_,
_CookEnding_, _MachineStarting_, _MachineStartingFood_, _MachineDoneFood_ and _MachineEnding_.

## Reports and Statistics
A **CoffeeShopReportStreams** class has also been implemented to generate various statistics regarding the Coffee Shop workflow,
entirely using Java 8 **Streams** API, looks into the event log and constructs Maps or Lists of objects. The reports include:
**getMapOrdersByCook**(), **getFoodCountByType**(), **getOrdersNumByCook**(), **getPercentageOrdersByCook**(), **getAverageCookingTimeByCook**().

## Testing
For testing the application we employed the **JUnit 5.8** library, and the tests with a 96% line coverage are in the _src/test/_
folder. The tests from **CoffeeShopReportStreamsTest** have been implemented completely using Java 8 **Streams** API.