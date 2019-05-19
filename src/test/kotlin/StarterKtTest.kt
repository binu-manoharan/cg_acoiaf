import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*
import java.util.Collections.singletonList

internal class StarterKtTest {
    private val starterUtils = StarterUtils()

    @Test
    internal fun piece_cost_should_be_1() {
        val level1 = starterUtils.calculateCost(singletonList(Piece(1, true, 1)));
        val level2 = starterUtils.calculateCost(singletonList(Piece(1, true, 2)));
        val level3 = starterUtils.calculateCost(singletonList(Piece(1, true, 3)));
        assertThat("Piece cost is 1", level1, `is`(1))
        assertThat("Piece cost is 4", level2, `is`(4))
        assertThat("Piece cost is 20", level3, `is`(20))

        val allThree = starterUtils.calculateCost(
                Arrays.asList(
                        Piece(1, true, 1),
                        Piece(1, true, 2),
                        Piece(1, true, 3)
                )
        )
        assertThat("Total piece cost is 25", allThree, `is`(25))
    }

    @Test
    internal fun test_bit_count() {
        val startTime = System.currentTimeMillis()

        val count = (2047..4192256).filter { Integer.bitCount(it) == 11 }.count()
        println(count)
        val endTime = System.currentTimeMillis()
        println(endTime - startTime)
    }

    @Test
    internal fun print_empty_board() {
        starterUtils.printBoard(GameData.emptyBoard())
    }

    @Test
    internal fun move_value() {
        val testBoard = GameData.emptyBoard()
        val myPiece = testBoard.get(10).get(10)
        val enemyHQ = testBoard.get(11).get(11)

        starterUtils.printBoard(testBoard)
        val flatBoard = testBoard.flatten()

        val bestValueMove = bestValueMove(myPiece, enemyHQ, flatBoard)
        println("best move $bestValueMove")
    }

    @Test
    internal fun all_training_spots() {
        val testBoard = GameData.emptyBoard()
        val allTrainingSpots = getAllTrainingSpots(testBoard.flatten())
        assertThat("There should be 2 trainig spots on an empty board", allTrainingSpots, hasSize(2))
    }

    @Test
    internal fun test_nearby_location() {
        assertThat("There are two neighbours",
                Location(0, 0).getNeighbours(),
                containsInAnyOrder(
                        Location(1,0),
                        Location(0, 1)
                )
        )

        assertThat("There are two neighbours",
                Location(11, 11).getNeighbours(),
                containsInAnyOrder(
                        Location(11,10),
                        Location(10, 11)
                )
        )

        assertThat("There are two neighbours",
                Location(5, 5).getNeighbours(),
                containsInAnyOrder(
                        Location(5,4),
                        Location(5,6),
                        Location(4,5),
                        Location(6, 5)
                )
        )
    }
}

