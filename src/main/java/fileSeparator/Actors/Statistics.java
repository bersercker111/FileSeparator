package fileSeparator.Actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Statistics extends AbstractActor {

    private final Map<String, Integer> groups;
    private Integer sum = 0;
    private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public Statistics() {
        groups = new HashMap<>();
    }

    public static Props props() {
        return Props.create(Statistics.class, Statistics::new);
    }

    private StatsMessage getStatsMessage() {
        StringBuilder builder = new StringBuilder();
        builder.append("Statistics : ").append(System.lineSeparator()).append(System.lineSeparator());
        for (String key : groups.keySet()) {
            builder.append("Group ").append(key).append(": ").append(groups.get(key)).append(" lines.")
                    .append(" ").append(groups.get(key) * 100 / sum).append("% of total.").append(System.lineSeparator());
        }
        builder.append(" Total : ").append(sum).append(" lines.");
        return new StatsMessage(builder.toString());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Data.class, data -> {
                    Integer value;
                    if (groups.containsKey(data.group)) value = groups.get(data.group) + 1;
                    else value = 1;
                    groups.put(data.group, value);
                    sum++;

                })
                .match(GetStringMessage.class, s -> sender().tell(getStatsMessage(), getSelf()))
                .match(LogMessage.class, s -> log.info(getStatsMessage().message))
                .match(GetMapMessage.class, s -> sender().tell(Collections.unmodifiableMap(new HashMap<>(groups)), getSelf()))
                .build();
    }

    //messages
    public static class GetStringMessage {
    }

    public static class LogMessage {
    }

    public static class GetMapMessage {

    }

    public static class Data {
        public final String group;

        public Data(String group) {
            this.group = group;
        }
    }

    public static class StatsMessage {
        public final String message;

        public StatsMessage(String message) {
            this.message = message;
        }
    }
}
