public class Event implements Comparable<Event> {

    /**
     * Order by time event occurs
     *
     * @param e event being compard to
     * @return comparison result
     */
    @Override
    public int compareTo(Event e) {
        return Double.compare(this.time, e.time);
    }

    public enum event_type {birth, death, mating}

    private Sim subject; // subject that is the subject of the event
    private event_type type; // the type of event
    private double time; // when the event takes place

    public Event(Sim sim, event_type event, double time_event) {
        this.subject = sim;
        this.type = event;
        this.time = time_event;
    }

    public event_type getType() {
        return this.type;
    }

    public Sim getSubject() {
        return this.subject;
    }

    public double getTime() {
        return this.time;
    }
    @Override
    public String toString() {
        return "Event{" +
                "subject=" + subject +
                ", type=" + type +
                ", time=" + time +
                '}';
    }
}
