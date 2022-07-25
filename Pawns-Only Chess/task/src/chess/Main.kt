package chess

import kotlin.math.abs

fun main() {
    val players = arrayOf("", "")
    val colors = arrayOf("white", "black")
    val colorMarks = arrayOf('W', 'B')
    val withinBoard = "[a-h][1-8][a-h][1-8]".toRegex()
    val board = mutableListOf(
        mutableListOf(' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '),
        mutableListOf('W', 'W', 'W', 'W', 'W', 'W', 'W', 'W'),
        mutableListOf(' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '),
        mutableListOf(' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '),
        mutableListOf(' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '),
        mutableListOf(' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '),
        mutableListOf('B', 'B', 'B', 'B', 'B', 'B', 'B', 'B'),
        mutableListOf(' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '),
    )

    // Fields on rank 3 and 6 available for en passant hit:
    val enPassant = Array(2) { BooleanArray(8) { false } }

    fun clearEnpassant(color: Int) {
        for (field in 0..7)
            enPassant[color][field] = false
    }

    fun drawBoard() {
        for (i in 7 downTo 0) {
            println("  +" + "---+".repeat(8))
            print("${i + 1} |")
            for (j in 0..7) {
                print(" ${board[i][j]} |")
            }
            println()
        }
        println("  +" + "---+".repeat(8))
        println("    a   b   c   d   e   f   g   h")
    }

    fun correctPosition(player: Int, move: String): Boolean {
        val x = move[0].code - 'a'.code
        val y = move[1].code - '1'.code
        return ((player == 0 && board[y][x] == 'W')
                or(player == 1 && board[y][x] == 'B')
                )
    }

    fun correctMove(color: Int, move: String): Boolean {
        val fromRank = move[1].code - '1'.code
        val toFile = move[2].code - 'a'.code
        val toRank = move[3].code - '1'.code
        if (board[toRank][toFile] != ' ') return false
        return when (color) {
            0 -> toRank - fromRank == 1  || fromRank == 1 && toRank == 3 && board[toRank][toFile] == ' '
            1 -> toRank - fromRank == -1 || fromRank == 6 && toRank == 4 && board[toRank + 1][toFile] == ' '
            else -> false
        }
    }

    fun correctCapture(player: Int, move: String): Boolean {
        if (move[2] !in 'a'..'h' || move[3] !in '1'..'8') return false
        val fromFile = move[0].code - 'a'.code
        val fromRank = move[1].code - '1'.code
        val toFile = move[2].code - 'a'.code
        val toRank = move[3].code - '1'.code
        if (abs(toFile - fromFile) != 1) return false
        return when (player) {
            0 -> toRank - fromRank == 1 && (board[toRank][toFile] == 'B' || fromRank == 4 && enPassant[1][toFile])
            1 -> toRank - fromRank == -1  && (board[toRank][toFile] == 'W' || fromRank == 3 && enPassant[0][toFile])
            else -> false
        }
    }

    fun checkMove(player: Int, move: String): String {
        val invalidInput = "Invalid Input"
        return when {
            !withinBoard.matches(move) -> invalidInput
            !correctPosition(player, move) -> "No ${colors[player]} pawn at ${move.substring(0, 2)}"
            move[0] == move[2] && !correctMove(player, move) -> invalidInput
            move[0] != move[2] && !correctCapture(player, move) -> invalidInput
            else -> ""
        }
    }

    fun movePawn(color: Char, move: String) {
        val from = move[1].code - '1'.code
        val to = move[3].code - '1'.code
        val x = move[0].code - 'a'.code
        board[from][x] = ' '
        board[to][x] = color
        if (color == 'W' && to - from == 2) enPassant[0][x] = true
        if (color == 'B' && to - from == -2) enPassant[1][x] = true
    }

    fun capture(color: Char, move: String) {
        val fromFile = move[0].code - 'a'.code
        val fromRank = move[1].code - '1'.code
        val toFile = move[2].code - 'a'.code
        val toRank = move[3].code - '1'.code
        board[fromRank][fromFile] = ' '
        board[toRank][toFile] = color
        when {
            color == 'W' && fromRank == 4 && enPassant[1][toFile] -> board[toRank - 1][toFile] = ' '
            color == 'B' && fromRank == 3 && enPassant[0][toFile] -> board[toRank + 1][toFile] = ' '
        }
    }

    fun isAPawn(color: Char): Boolean {
        for (rank in board) {
            if (color in rank) return true
        }
        return false
    }

    fun canMove(color: Int): Boolean {
        val incr = if (color == 0) 1 else -1
        for (rank in 2..7) {
            for (file in 'a'..'h') {
                if (board[rank - 1][file - 'a'] == colorMarks[color] &&
                    correctMove(color, "$file$rank$file${rank+incr}")) return true
            }
        }
        return false
    }

    fun canCapture(color: Int): Boolean {
        val incr = if (color == 0) 1 else -1
        for (rank in 2..7) {
            for (file in 'a'..'h') {
                if (board[rank - 1][file - 'a'] == colorMarks[color] &&
                    (correctCapture(color, "$file$rank${file - 1}${rank+incr}") ||
                    correctCapture(color, "$file$rank${file + 1}${rank+incr}"))) return true
            }
        }
        return false
    }

    fun gameFinished(color: Int): String {
        val contestColor = (color + 1) % 2
        val winField = if (color == 0) 7 else 0
        return when {
            colorMarks[color] in board[winField] || !isAPawn(colorMarks[contestColor]) -> "${colors[color].replaceFirstChar{it.uppercase()}} wins!"
            !canMove(contestColor) && !canCapture(contestColor) -> "Stalemate!"
            else -> ""
        }
    }

    println("Pawns-Only Chess")
    print("First Player's name:\n> ")
    players[0] = readln()
    print("Second Player's name:\n> ")
    players[1] = readln()
    drawBoard()
    var i = 0
    do {
        var chkMove: String
        var move: String
        print("${players[i]}'s turn:\n> ")
        do {
            move = readln()
            if (move == "exit") {
                println("Bye!")
                return
            }
            chkMove = checkMove(i, move)
            if (chkMove.isNotEmpty()) {
                println(chkMove)
                print("${players[i]}'s turn:\n> ")
            }
        }
        while (chkMove.isNotEmpty())
        if (move[0] == move[2]) {
            movePawn(colors[i][0].uppercaseChar(), move)
        }
        else capture(colors[i][0].uppercaseChar(), move)
        drawBoard()
        val isFinish = gameFinished(i)
        if (isFinish != "") {
            println(isFinish)
            println("Bye!")
            return
        }
        i = (i + 1) % 2
        clearEnpassant(i)
    }
    while (true)
}
