package fileSeparator.services;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import fileSeparator.Actors.Statistics;
import fileSeparator.Actors.WriterManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileSeparator {
    private ActorSystem system;


    public FileSeparator(ActorSystem actorSystem) {
        this.system = actorSystem;
    }

    public void processFile(String originalFilePath) {


        final ActorRef statisticsActor = system.actorOf(Statistics.props(), "statisticsAuthor");
        final ActorRef writerManager = system.actorOf(WriterManager.props(statisticsActor, originalFilePath), "readerActor");
        final File inputFile;
        try {
            Files.lines(Paths.get(originalFilePath))
                    .filter(line -> !line.isEmpty())
                    .forEach(line -> writerManager.tell(new WriterManager.Line(line), ActorRef.noSender()));
            writerManager.tell(new WriterManager.StopMessage(), ActorRef.noSender());

        } catch (IOException e) {
            e.printStackTrace();
            system.terminate();
        }
    }


}
