package fileSeparator.Actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class StatisticsTest {
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
    public void testStatisticsActor() {
        final TestKit testProbe = new TestKit(system);
        final ActorRef statistics = system.actorOf(Statistics.props());
        String one = "one";
        String two = "two";
        statistics.tell(new Statistics.Data(one), ActorRef.noSender());
        statistics.tell(new Statistics.Data(two), ActorRef.noSender());
        statistics.tell(new Statistics.Data(two), ActorRef.noSender());
        statistics.tell(new Statistics.GetMapMessage(), testProbe.getRef());
        Map statsMap = testProbe.expectMsgClass(Map.class);
        assertEquals(statsMap.get(one), 1);
        assertEquals(statsMap.get(two), 2);
        assertEquals(statsMap.size(), 2);
    }
}

