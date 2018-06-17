package fileSeparator.Actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import static fileSeparator.Actors.WriterManager.Line;

public class Writer extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final String name;
    private final ActorRef statisticsActor;
    private BufferedWriter bufferedWriter = null;
    private boolean canWriteToFile = false;
    private String originalFilePath;


    public Writer(String name, String originalFilePath, ActorRef statisticsActor) {
        this.name = name;
        this.statisticsActor = statisticsActor;
        this.originalFilePath = originalFilePath;
        try {
            URL url = this.getClass().getResource(originalFilePath.replace(".csv", "-" + name + ".csv"));
            if (url != null) {
                String path = URLDecoder.decode(url.getPath(), "UTF-8");
                File file = new File(path);
                if (file.exists()) file.delete();
                bufferedWriter = Files.newBufferedWriter(file.toPath(), StandardOpenOption.CREATE_NEW);
                canWriteToFile = true;
            } else log.error("Unable to write to file");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Props props(String name, String originalFilePath, ActorRef statisticsActor) {
        return Props.create(Writer.class, () -> new Writer(name, originalFilePath, statisticsActor));
    }

    private void writeLine(Line line) throws IOException {
        bufferedWriter.write(line.text + System.lineSeparator());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Line.class, line -> {
                    log.info(name.toUpperCase() + " |||  " + line.text);
                    if (canWriteToFile) writeLine(line);
                    statisticsActor.tell(new Statistics.Data(name), getSelf());
                })
                .match(WriterManager.StopMessage.class, s -> {
                    log.info("Writer " + name + " actor received stop message");
                    getContext().stop(getSelf());
                })
                .build();
    }

    @Override
    public void postStop() throws IOException {
        bufferedWriter.flush();
        bufferedWriter.close();
        if (!canWriteToFile) log.error("Unable to write to file");
    }
}
