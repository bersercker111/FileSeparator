package fileSeparator.Actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        //output file is written to the same directory
        try {
            Path path = Paths.get(originalFilePath.replace(".csv", "-" + name + ".csv"));
            File file = path.toFile();
            if (file.exists()) file.delete();
            bufferedWriter = Files.newBufferedWriter(file.toPath(), StandardOpenOption.CREATE_NEW);
            canWriteToFile = true;
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Unable to write to file");
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
        if (bufferedWriter != null) {
            bufferedWriter.flush();
            bufferedWriter.close();
        }
        if (!canWriteToFile) log.error("Unable to write to file");
    }
}
