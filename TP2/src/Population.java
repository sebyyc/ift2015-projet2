import java.util.ArrayList;
import java.util.PriorityQueue;

public class Population {
    public ArrayList<Sim> population;
    public PriorityQueue<Sim> population_f;
    public PriorityQueue<Sim> population_m;

    public Population() {
        this.population = new ArrayList<>();
        this.population_f = new PriorityQueue<>();
        this.population_m = new PriorityQueue<>();
    }

    public Sim get(int index) {
        return this.population.get(index);
    }

    public int size() {
        return this.population.size();
    }

    public boolean contains(Sim sim) {
        return this.population.contains(sim);
    }

    public void add(Sim sim) {
        this.population.add(sim);
        if(sim.getSex().equals(Sim.Sex.F)) {
            this.population_f.add(sim);
        } else {
            this.population_m.add(sim);
        }
    }

    public void remove(Sim sim) {
        this.population.remove(sim);
//        if (sim.getSex().equals(Sim.Sex.F)) {
//            this.population_f.remove(sim);
//        } else {
//            this.population_m.remove(sim);
//        }
    }

    public Sim getYoungest(Sim.Sex sex) {
        if (Sim.Sex.F.equals(sex)) {
            return this.population_f.poll();
        } else {
            return this.population_m.poll();
        }
    }
}
