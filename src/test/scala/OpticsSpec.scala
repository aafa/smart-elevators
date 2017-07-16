import core.ElevatorControlSystemModel._
import monocle.macros.syntax.lens._
import org.scalatest._

class OpticsSpec extends FreeSpecLike with Matchers {
  val model = Model()

  "Optics spec here" - {

    "simple stuff" in {
      val r = model.lens(_.elevatorsSystemState).modify(_ + (ElevatorId(0) -> Elevator(0, 0)))
      r should be(Model(elevatorsSystemState = Map(ElevatorId(0) -> Elevator(0, 0))))

      r.elevatorById(ElevatorId(0)).get should be(Some(Elevator(0, 0)))
      r.elevatorById(ElevatorId(0))
        .set(Some(Elevator(0, 1)))
        .elevatorById(ElevatorId(0))
        .get should be(Some(Elevator(0, 1)))

      r.elevatorById(ElevatorId(1))
        .set(Some(Elevator(1, 0)))
        .elevatorById(ElevatorId(1))
        .get should be(Some(Elevator(1, 0)))

    }
  }
}
