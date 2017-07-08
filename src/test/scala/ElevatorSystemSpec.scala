import ElevatorControlSystem._
import org.scalatest._

class ElevatorSystemSpec extends FreeSpecLike with Matchers {
  "Should work in a simple scenario" - {
    val elevators = new NaiveElevators

    "should report their status" in {
      assert(elevators.status().contains(ElevatorId(1)))
      assert(elevators.status().contains(ElevatorId(2)))
      assert(!elevators.status().contains(ElevatorId(4)))
    }
  }
}
