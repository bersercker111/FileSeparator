package fileSeparator;


import akka.actor.ActorSystem;
import fileSeparator.services.FileSeparator;

public class Main {

    public static void main(String[] args) {

        final ActorSystem system = ActorSystem.create("fileSeparator");
        FileSeparator fileSeparator = new FileSeparator(system);
        fileSeparator.processFile("/jun-mid-task.csv");
    }
}


