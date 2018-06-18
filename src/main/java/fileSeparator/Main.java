package fileSeparator;


import akka.actor.ActorSystem;
import fileSeparator.services.FileSeparator;

public class Main {

    public static void main(String[] args) {
        String inputFilePath;
        if (args.length > 0) {
            //output files will be written to the same directory
            inputFilePath = args[0];
            final ActorSystem system = ActorSystem.create("fileSeparator");
            FileSeparator fileSeparator = new FileSeparator(system);
            fileSeparator.processFile(inputFilePath);
        } else {
            System.out.println("Missing input file argument");
        }
    }
}


