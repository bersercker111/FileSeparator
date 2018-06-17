package fileSeparator.Actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WriterTest {

    static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void WriterTests() {
        final TestKit testProbe = new TestKit(system);
        String writerName = "one";
        ActorRef writerActor = system.actorOf(Writer.props(writerName, testProbe.getRef()));
        writerActor.tell(new WriterManager.Line("aaaaaa"), testProbe.getRef());
        Statistics.Data data = testProbe.expectMsgClass(Statistics.Data.class);
        assertEquals(data.group, writerName);
    }
}