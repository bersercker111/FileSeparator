package fileSeparator.Actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import fileSeparator.services.Utils;

import java.util.HashMap;
import java.util.Map;

public class WriterManager extends AbstractActor {

    private final String NAME_ONE = "one";
    private final String NAME_TWO = "two";
    private final String NAME_THREE = "three";
    private final String NAME_FOUR = "four";
    private final Map<String, ActorRef> writers;
    private final Map<ActorRef, Boolean> isTerminated;
    private final ActorRef statisticsActor;
    private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public WriterManager(ActorRef statisticsActor) {
        this.statisticsActor = statisticsActor;
        ActorContext context = getContext();
        writers = new HashMap<>();
        isTerminated = new HashMap<>();
        writers.put(NAME_ONE, context.actorOf(Writer.props(NAME_ONE, statisticsActor)));
        writers.put(NAME_TWO, context.actorOf(Writer.props(NAME_TWO, statisticsActor)));
        writers.put(NAME_THREE, context.actorOf(Writer.props(NAME_THREE, statisticsActor)));
        writers.put(NAME_FOUR, context.actorOf(Writer.props(NAME_FOUR, statisticsActor)));
        for (ActorRef writer : writers.values()) {
            context.watch(writer);
            isTerminated.put(writer, false);
        }
    }

    public static Props props(ActorRef statisticsActor) {
        return Props.create(WriterManager.class, () -> new WriterManager(statisticsActor));
    }

    public boolean areAllTerminated() {
        boolean result = true;
        for (Boolean b : isTerminated.values()) {
            result = result && b;
        }
        return result;
    }

    @Override
    public Receive createReceive() {

        return receiveBuilder()
                .match(Line.class, line -> {
                    ActorRef writer = null;
                    String group = Utils.getGroup(line.text);
                    try {
                        if (group.isEmpty()) throw new IllegalArgumentException("no comma in input line " + line.text);
                        writer = writers.get(group);
                        if (writer != null) writer.tell(new Line(line.text), getSelf());
                        else throw new IllegalArgumentException("Invalid group name: " + group);
                    } catch (IllegalArgumentException ex) {
                        ex.printStackTrace();
                    }
                })
                .match(StopMessage.class, s -> {
                    log.info("Reader actor received stop message");
                    for (ActorRef writer : writers.values()) writer.tell(new StopMessage(), getSelf());
                })
                .match(Terminated.class, t -> {
                    ActorRef actorRef = t.getActor();
                    if (isTerminated.containsKey(actorRef)) {
                        isTerminated.put(actorRef, true);
                        if (areAllTerminated()) {
                            statisticsActor.tell(new Statistics.LogMessage(), getSelf());
                            getContext().stop(getSelf());
                        }
                    } else log.error("Terminated map doesn't contain " + actorRef.toString());

                })
                .build();
    }

    //messages
    public static class StopMessage {
    }

    public static class Line {
        public final String text;

        public Line(String text) {
            this.text = text;
        }
    }

}
