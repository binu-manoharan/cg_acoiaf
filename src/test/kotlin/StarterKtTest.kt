import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*
import java.util.Collections.singletonList

internal class StarterKtTest {
    @Test
    internal fun piece_cost_should_be_1() {
        val level1 = StarterUtils().calculateCost(singletonList(Piece(1, true, 1)));
        val level2 = StarterUtils().calculateCost(singletonList(Piece(1, true, 2)));
        val level3 = StarterUtils().calculateCost(singletonList(Piece(1, true, 3)));
        assertEquals(level1, 1, "Piece cost is 1")
        assertEquals(level2, 4, "Piece cost is 4")
        assertEquals(level3, 20, "Piece cost is 20")

        val allThree = StarterUtils().calculateCost(
                Arrays.asList(
                        Piece(1, true, 1),
                        Piece(1, true, 2),
                        Piece(1, true, 3)
                )
        )
        assertEquals(allThree, 25, "Total piece cost is 25")
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
        StarterUtils().printBoard(GameData.emptyBoard())
    }
}

