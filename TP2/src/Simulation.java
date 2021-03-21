import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Random;

public class Simulation {
    AgeModel AM; // age model with default values
    public PriorityQueue<Event> queue;
    public HashMap<Integer, Sim> population; // current living population
    public double length_sim; // lengh of the simulation in years
    public int initial_pop; // initial population size for the simulation

    private static double DEFAULT_REPRODUCTION_RATE = 2;
    private static final double DEFAULT_LOYALTY_RATE = 0.90;
    private static double MATING_SPAN;

    public Simulation(int pop, double time){
        this.AM = new AgeModel();
        this.queue = new PriorityQueue<>();
        this.population = new HashMap<>();
        this.length_sim = time;
        this.initial_pop = pop;
        MATING_SPAN = new AgeModel().expectedParenthoodSpan(Sim.MIN_MATING_AGE_F, Sim.MAX_MATING_AGE_F);
    }

    /**
     * Methode to randomly choose a sex for the Sim
     * @return Sex
     */
    public static Sim.Sex randomSex(){
        Random rand = new Random();
        if(rand.nextDouble() >= 0.5){
            return Sim.Sex.F;
        } else {
            return Sim.Sex.M;
        }
    }

    /**
     * Adds a Sim to the current population
     * @param sim the Sim that is being added
     */
    public void add_sim(Sim sim) {
        this.population.put(sim.getID(),sim);
    }

    /**
     * Removes a Sim from the current population
     * @param sim the Sim that is being removed
     */
    public void remove_sim(Sim sim) {
        this.population.remove(sim.getID());
    }

    /**
     * Find a mate from a female Sim that has a mating event
     * @param time time at which the event was triggered
     * @return mate for the female Sim
     */
    public Sim findBachelor(double time) {
        Random RND = new Random();
        Object[] values = population.values().toArray();
        Sim tempSim =  (Sim) values[RND.nextInt(values.length)];
        while (tempSim.getSex() != Sim.Sex.M || !tempSim.isMatingAge(time) || tempSim.getMate() != null){
            if(tempSim.getSex() == Sim.Sex.M && RND.nextDouble() >= DEFAULT_LOYALTY_RATE) return tempSim;
            tempSim =  (Sim) values[RND.nextInt(values.length)];
        }
            return tempSim;
    }

    /**
     * Methode for generating the events for the founding Sims
     * @param sim founding Sim
     * @param RND Random number generator used to generate life span; also used for generating mating event for female Sims
     */
    public void add_fouding_event(Sim sim, Random RND){
        // add event for death
        this.queue.add(new Event(sim, Event.event_type.death, sim.getDeathTime() ));
        if (sim.getSex() == Sim.Sex.F) {
            this.queue.add(new Event(sim, Event.event_type.mating,
                    + AgeModel.randomWaitingTime(RND, DEFAULT_REPRODUCTION_RATE)));
        }
    }

    /**
     * Method that handels evens being drawun from the priortiy queue
     * @param e event being handled
     */
    public void handleEvent(Event e) {
        double current_time = e.getTime();
        Sim tempSim = e.getSubject();
        Random RND = new Random();
        switch (e.getType()) {
           case death:
                this.remove_sim(tempSim);
                // TODO: remove this SYSTEM.OUT.PRINT
                System.out.println(e + " age: " + (current_time - e.getSubject().getBirthTime()));
                break;
            case mating:
                // TODO: remove this SYSTEM.OUT.PRINT
                System.out.println(e);
                // check if Sim alive;
                if(population.containsValue(tempSim)) {
                    System.out.println("Still Alive");
                    // calcualte Sim Age
                    double age = current_time - tempSim.getBirthTime();
                    // check if Sim of mating Age
                    if (tempSim.isMatingAge(current_time)) {
                        // if single, find bachelor
                        if(tempSim.getMate() == null) {
                            // make couple
                            tempSim.setMate(this.findBachelor(current_time));
                            tempSim.getMate().setMate(tempSim);
                            // birth event
                            this.queue.add(new Event(tempSim, Event.event_type.birth, current_time
                                    + AgeModel.randomWaitingTime(RND, DEFAULT_REPRODUCTION_RATE)));
                            // birth event
                            this.queue.add(new Event(tempSim, Event.event_type.birth, current_time
                                    + AgeModel.randomWaitingTime(RND, DEFAULT_REPRODUCTION_RATE)));
                            // birth event
                            if(population.size() < 9500) {
                                this.queue.add(new Event(tempSim, Event.event_type.birth, current_time
                                        + AgeModel.randomWaitingTime(RND, DEFAULT_REPRODUCTION_RATE)));
                            }
                        }
                    }
                    else {
                        if (age < Sim.MIN_MATING_AGE_F) {
                            this.queue.add(new Event(tempSim, Event.event_type.mating, current_time
                                     +  AgeModel.randomWaitingTime(RND, DEFAULT_REPRODUCTION_RATE)));
                        }
                        System.out.println("Not of mating age");
                    }
                    break;
                }
                System.out.println("DEAD");
                break;
           case birth:
                // create Sim
                System.out.println(e);
                Sim child = new Sim(tempSim, tempSim.getMate(), current_time, randomSex());
                this.add_sim(child);
                child.setDeath(this.AM.randomAge(RND) + current_time);
                System.out.println("New SIM " + child);
                this.queue.add(new Event(child, Event.event_type.death, child.getDeathTime()));
                if (child.getSex() == Sim.Sex.F) {
                    this.queue.add(new Event(child, Event.event_type.mating, current_time
                            + AgeModel.randomWaitingTime(RND, DEFAULT_REPRODUCTION_RATE)));


                }




        }
    }

    /**
     * Main for testing
     * @param args no args
     */
    public static void main(String[] args) {
        double avgtest = 0;
        Random RND = new Random();
        Simulation SIM = new Simulation(1000, 20000);
        for ( int i = 0;  i <= SIM.initial_pop; i++){
            Sim temp = new Sim(randomSex());
            temp.setDeathTime(SIM.AM.randomAge(RND));
            SIM.add_sim(temp);
            SIM.add_fouding_event(temp, RND);
            // TODO remove var for avg lifespan
            avgtest += temp.getDeathTime();
        }
        // REMOVE ALL EVENTS IN ORDER
        while(!SIM.queue.isEmpty()){
            if(SIM.queue.peek().getTime() > 1000) break;
            SIM.handleEvent(SIM.queue.poll());
            System.out.println("events: " + SIM.queue.size() + " pop: " + SIM.population.size());

        }

        // Print the HASHMAP OF CURRENT POP
        for(Integer key : SIM.population.keySet()){
         //   System.out.println("key: " + key + " - " + SIM.population.get(key));
        }
        // TODO: remove this SYSTEM.OUT.PRINT
        // System.out.println(avgtest/ SIM.population.size());
    }
}