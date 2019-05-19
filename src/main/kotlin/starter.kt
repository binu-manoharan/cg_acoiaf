import java.lang.System.err
import java.util.*
import kotlin.math.abs

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/

data class Location(val x: Int, val y: Int)

data class Cell(val x: Int, val y: Int, var ownership: Int, var piece: Piece? = null) {
    // doesn't account for holes in the map!
    fun distance(other: Cell) = abs(x - other.x) + abs(y - other.y)

    fun distance(xx: Int, yy: Int) = abs(x - xx) + abs(y - yy)
}

data class Piece(val id: Int, val isFriendly: Boolean, val level: Int) {
    fun pieceCost(): Int {
        return if (this.level == 1) return 1 else if (this.level == 2) return 4 else return 20;
    }
}

interface IAction

data class MoveAction(val id: Int, val newX: Int, val newY: Int) : IAction {
    override fun toString() = "MOVE $id $newX $newY"
}

data class TrainAction(val level: Int, val newX: Int, val newY: Int) : IAction {
    override fun toString() = "TRAIN $level $newX $newY"
}

data class BuildAction(val newX: Int, val newY: Int) : IAction {
    override fun toString() = "BUILD MINE $newX $newY"
}

class StarterUtils {
    fun calculateCost(pieces: List<Piece>): Int {
        return pieces.map { it.pieceCost() }.sum()
    }

    fun printBoard(board: List<List<Cell>>) {
        board.forEach { y ->
            y.forEach {
                err.printf("%3d", it.ownership)
            }
            err.println()
        }
    }
}

fun main(args: Array<String>) {
    val input = Scanner(System.`in`)
    val mines = List(input.nextInt()) {
        val x = input.nextInt()
        val y = input.nextInt()
        Location(x, y)
    }

    val utils = StarterUtils()
    val voidCellValue = 99;
    // game loop
    while (true) {
        val gold = input.nextInt()
        val income = input.nextInt()
        val opponentGold = input.nextInt()
        val opponentIncome = input.nextInt()

        val board: List<List<Cell>> = List(12) { y ->
            input.next().mapIndexed { x: Int, ch ->
                val type = when (ch) {
                    '#' -> voidCellValue
                    '.' -> -1
                    'O' -> 2; 'o' -> 1
                    'X' -> -3; 'x' -> -2
                    else -> throw Exception("Unexpected")
                }
                Cell(x, y, type)
            }
        }

        // Gives us a flat list of the non-void cells
        val boardCells = board.flatten().filterNot { it.ownership == voidCellValue }

        lateinit var myHQ: Cell
        lateinit var enemyHQ: Cell

        val buildingCount = input.nextInt()
        val builtMines = mutableListOf<Location>()
        for (i in 0 until buildingCount) {
            val owner = input.nextInt()
            val buildingType = input.nextInt()
            val x = input.nextInt()
            val y = input.nextInt()
            if (buildingType == 0) {
                // HQ
                val hq = board[y][x]!!
                when (owner) {
                    0 -> myHQ = hq
                    1 -> enemyHQ = hq
                }
            } else {
                builtMines.add(Location(x, y))
            }
        }

        repeat(input.nextInt()) {
            val owner = input.nextInt()
            val unitId = input.nextInt()
            val level = input.nextInt()
            val x = input.nextInt()
            val y = input.nextInt()
            val cell = board[y][x]!!
            cell.piece = Piece(unitId, owner == 0, level)
        }

        val actions = mutableListOf<IAction>()
//        TODO Move better to ensure they can move in a line add tests possibly
        val myPiecesCells = boardCells.filter { it.piece?.isFriendly == true }
        myPiecesCells.map {
            Pair(it, bestValueMove(it, enemyHQ, boardCells))
        }.forEach {
            actions += MoveAction(it.first.piece!!.id, it.second.x, it.second.y)
            it.first.piece = null
        }

//        TODO Add more training locations
        val trainingSpot = boardCells
                .firstOrNull { it.distance(myHQ) == 1 && it.piece == null }
        err.println("Training $trainingSpot")


        val myPiecesCost = utils.calculateCost(myPiecesCells.map { it.piece }.filterNotNull())

        // If we have enough cash, build a new piece!
        val numPieces = myPiecesCells.size
        if (gold > 10 && trainingSpot != null && numPieces < 5) {
            actions += TrainAction(1, 0, 1)
        } else if (income - myPiecesCost - numPieces > 10 && trainingSpot != null) {
//          TODO Add better logic for training people
            actions += TrainAction(2, 0, 1)
        }

        if (gold > 40) {
            mines.filterNot { builtMines.contains(it) }
                    .map { board[it.y][it.x] }
                    .filter { it.ownership == 1 || it.ownership == 2 }
                    .filterNot { it.piece != null }
                    .firstOrNull {
                        actions.add(BuildAction(it.x, it.y))
                    }
        }

        if (actions.any()) {
            println(actions.joinToString(";"))
        } else {
            println("WAIT")
        }
    }
}

fun bestValueMove(myPiece: Cell, enemyHQ: Cell, flatBoard: List<Cell>): Cell {
    val possibleMoves = flatBoard.filterNot {
        it.ownership > 10
    }.filter {
        (it.x == myPiece.x - 1 && it.y == myPiece.y && it.x > 0) ||
                (it.x == myPiece.x + 1 && it.y == myPiece.y && it.x < 12) ||
                (it.x == myPiece.x && it.y == myPiece.y - 1 && it.y > 0) ||
                (it.x == myPiece.x && it.y == myPiece.y + 1 && it.y < 12)
    }
    return possibleMoves.maxBy {
        val distanceToEnemyHQ = it.distance(enemyHQ)
        val distanceScore = if (distanceToEnemyHQ == 1) -100 else distanceToEnemyHQ
        100 - it.ownership - distanceScore
    }!!
}
