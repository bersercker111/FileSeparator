package fileSeparator.services;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import fileSeparator.Actors.Statistics;
import fileSeparator.Actors.WriterManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;

public class FileSeparator {
    private ActorSystem system;

    public FileSeparator(ActorSystem actorSystem) {
        this.system = actorSystem;
    }

    public void processFile(String input) {


        final ActorRef statisticsActor = system.actorOf(Statistics.props(), "statisticsAuthor");
        final ActorRef writerManager = system.actorOf(WriterManager.props(statisticsActor), "readerActor");
        final File inputFile;
        try {
            URL fileResource = this.getClass().getResource(input);
            if (fileResource == null) throw new FileNotFoundException("File not found " + input);
            inputFile = new File(URLDecoder.decode(fileResource.getPath(), "UTF-8"));
            Files.lines(inputFile.toPath())
                    .filter(line -> !line.isEmpty())
                    .forEach(line -> writerManager.tell(new WriterManager.Line(line), ActorRef.noSender()));
            writerManager.tell(new WriterManager.StopMessage(), ActorRef.noSender());

        } catch (IOException e) {
            e.printStackTrace();
            system.terminate();
        }
    }


}
