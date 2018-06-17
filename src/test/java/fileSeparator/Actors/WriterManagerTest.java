package fileSeparator.Actors;


import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Terminated;
import akka.testkit.javadsl.TestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WriterManagerTest {
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
    public void TestStopMessage() {
        final TestKit testProbe = new TestKit(system);
        ActorRef writerManager = system.actorOf(WriterManager.props(testProbe.getRef()));
        testProbe.watch(writerManager);
        writerManager.tell(new WriterManager.StopMessage(), ActorRef.noSender());
        Statistics.LogMessage logMessage = testProbe.expectMsgClass(Statistics.LogMessage.class);
        final Terminated msg = testProbe.expectMsgClass(Terminated.class);
        assertEquals(msg.getActor(), writerManager);

    }
}
