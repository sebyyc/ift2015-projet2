import java.util.*;

public class Simulation {
    AgeModel AM; // age model with default values
    public PriorityQueue<Event> queue;
    public Population population;
    public double length_sim; // length of the simulation in years
    public int initial_pop; // initial population size for the simulation

    private static final double DEFAULT_REPRODUCTION_RATE = 2;
    private static final double DEFAULT_LOYALTY_RATE = 0.90;

    public Simulation(int pop, double time){
        this.AM = new AgeModel();
        this.queue = new PriorityQueue<>();
        this.population = new Population();
        this.length_sim = time;
        this.initial_pop = pop;
        double MATING_SPAN = new AgeModel().expectedParenthoodSpan(Sim.MIN_MATING_AGE_F, Sim.MAX_MATING_AGE_F);
    }

    /**
     * Method to randomly choose a sex for the Sim
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
        this.population.add(sim);
    }

    /**
     * Removes a Sim from the current population
     * @param sim the Sim that is being removed
     */
    public void remove_sim(Sim sim) {
        this.population.remove(sim);
    }

    /**
     * Find a mate from a female Sim that has a mating event
     * @param time time at which the event was triggered
     * @return mate for the female Sim
     */
    public Sim findBachelor(double time) {
        Random RND = new Random();

        Sim tempSim =  population.get(RND.nextInt(population.size()));
        while (tempSim.getSex() != Sim.Sex.M || !tempSim.isMatingAge(time) || tempSim.getMate() != null){
            if(tempSim.getSex() == Sim.Sex.M && RND.nextDouble() >= DEFAULT_LOYALTY_RATE) return tempSim;
            tempSim =  population.get(RND.nextInt(population.size()));
        }
            return tempSim;
    }

    /**
     * Methode for generating the events for the founding Sims
     * @param sim founding Sim
     * @param RND Random number generator used to generate life span; also used for generating mating event for female Sims
     */
    public void addFoundingEvent(Sim sim, Random RND){
        // add event for death
        this.queue.add(new Event(sim, Event.event_type.death, sim.getDeathTime() ));
        if (sim.getSex() == Sim.Sex.F) {
            this.queue.add(new Event(sim, Event.event_type.mating,
                    + AgeModel.randomWaitingTime(RND, DEFAULT_REPRODUCTION_RATE)));
        }
    }

    private void mating(Sim tempSim, double currentTime, Random RND) {
        if(population.contains(tempSim)) {
            // calculate Sim Age
            double age = currentTime - tempSim.getBirthTime();
            // check if Sim of mating Age
            if (tempSim.isMatingAge(currentTime)) {
                // if single, find bachelor
                if(tempSim.getMate() == null) {
                    // make couple
                    tempSim.setMate(this.findBachelor(currentTime));
                    tempSim.getMate().setMate(tempSim);
                    // birth event
                    this.queue.add(new Event(tempSim, Event.event_type.birth, currentTime
                            + AgeModel.randomWaitingTime(RND, DEFAULT_REPRODUCTION_RATE)));
                    // birth event
                    this.queue.add(new Event(tempSim, Event.event_type.birth, currentTime
                            + AgeModel.randomWaitingTime(RND, DEFAULT_REPRODUCTION_RATE)));
                    // birth event
                    if(population.size() < 9500) {
                        this.queue.add(new Event(tempSim, Event.event_type.birth, currentTime
                                + AgeModel.randomWaitingTime(RND, DEFAULT_REPRODUCTION_RATE)));
                    }
                }
            }
            else {
                if (age < Sim.MIN_MATING_AGE_F) {
                    this.queue.add(new Event(tempSim, Event.event_type.mating, currentTime
                            +  AgeModel.randomWaitingTime(RND, DEFAULT_REPRODUCTION_RATE)));
                }
            }
        }
    }

    private void birth(Sim tempSim, double currentTime, Random RND) {
        Sim child = new Sim(tempSim, tempSim.getMate(), currentTime, randomSex());
        this.add_sim(child);
        child.setDeath(this.AM.randomAge(RND) + currentTime);
        this.queue.add(new Event(child, Event.event_type.death, child.getDeathTime()));
        if (child.getSex() == Sim.Sex.F) {
            this.queue.add(new Event(child, Event.event_type.mating, currentTime
                    + AgeModel.randomWaitingTime(RND, DEFAULT_REPRODUCTION_RATE)));
        }
    }

    /**
     * Method that handles evens being drawun from the priortiy queue
     * @param e event being handled
     */
    public void handleEvent(Event e) {
        double currentTime = e.getTime();
        Sim tempSim = e.getSubject();
        Random RND = new Random();
        switch (e.getType()) {
           case death:
                this.remove_sim(tempSim);
                break;
            case mating:
                // check if Sim alive;
                this.mating(tempSim, currentTime, RND);
                break;
           case birth:
                // create Sim
                this.birth(tempSim, currentTime, RND);
                break;
        }
    }

    /**
     * Code to perform coalescence calculation
     * @param sex sex used for coalescence
     * @return ArrayList of coalescence points
     */
    public ArrayList<double[]> coalescence(Sim.Sex sex) {
        ArrayList<double[]> coalescencePointsList = new ArrayList<>();
        HashSet<Sim> simHashSet = new HashSet<>();
        Sim youngest = this.population.getYoungest(sex);
        while(youngest != null) {
            if (simHashSet.contains(youngest.getFather())) {
                double[] coalescencePoint =  new double[]{youngest.getBirthTime(), simHashSet.size()};
                coalescencePointsList.add(coalescencePoint);
            } else {
                simHashSet.add(youngest.getFather());
            }
            youngest = this.population.getYoungest(sex);
        }
        return coalescencePointsList;
    }


    /**
     * Main for testing
     * @param args no args
     */
    public static void main(String[] args) {

        Random RND = new Random();
        Simulation SIM = new Simulation(1000, 20000);
        for ( int i = 0;  i <= SIM.initial_pop; i++){
            Sim temp = new Sim(randomSex());
            temp.setDeathTime(SIM.AM.randomAge(RND));
            SIM.add_sim(temp);
            SIM.addFoundingEvent(temp, RND);
            // TODO remove var for avg lifespan

        }
        // REMOVE ALL EVENTS IN ORDER
        int iter = 1 ; //iterator
        ArrayList<double[]> coal;
        while(!SIM.queue.isEmpty()){

            if(SIM.queue.peek().getTime() >1000) break;
            if(SIM.queue.peek().getTime() > iter*100){

                System.out.print("Year: " + iter*100);
                coal = SIM.coalescence(Sim.Sex.F);
                System.out.print(" | coalF: " + coal.size());
                coal = SIM.coalescence(Sim.Sex.M);
                System.out.print(" | coalM: " + coal.size());
                System.out.println(" | popSize: " + SIM.population.size());
                iter++;
            }
            SIM.handleEvent(SIM.queue.poll());
        }

        //coal = SIM.coalescence(Sim.Sex.F);

        System.out.println(SIM.population.size() + " / " + SIM.queue.size());
    }
}