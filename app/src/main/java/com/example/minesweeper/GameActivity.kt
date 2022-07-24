package com.example.minesweeper
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.game_main.*
import java.util.*

class GameActivity : AppCompatActivity() {
    private var id = 0;
    private var row = 13
    private var col = 6
    private var MinesCount = 10
    private val MINE = -1
    private var count: Int = 0
    private var flagCount: Int = 0
    private var time = "0"
    private lateinit var board: Array<Array<MineButton>>
    private var flag = false
    private val movement = intArrayOf(-1, 0, 1)
    private var seconds = 0
    private var running = false
    private var wasRunning = false
    private val handler = Handler()
    private lateinit var runnable: Runnable
    private var bestScore = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()
        setContentView(R.layout.game_main)
        window.statusBarColor = ContextCompat.getColor(this, R.color.teal_700)
        startTimer()
        id = intent.getIntExtra("id",1)
        if(id==1){
            row = 13
            col = 6
            MinesCount = 10
        }
        if(id==2){
            row = 20
            col = 9
            MinesCount = 35
        }
        if(id==3){
            row = 28
            col = 13
            MinesCount = 75
        }
        board = Array(row) { Array(col) { MineButton() } }
        mines.text = MinesCount.toString()
        flagCount = MinesCount
        setUPborad()
        resetgame.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            with(builder){
                setTitle("RESET GAME")
                setMessage("Are you sure?")
                setPositiveButton("YES"
                ) { dialog, which -> restartGame()}
                setNegativeButton("NO"){
                        dialog,which ->
                }
            }
            builder.create().show()
        }

        flagButton.setOnClickListener {
            if (flag)
                flagButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.mine))
            else
                flagButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.flag))

            flag = !flag
        }
    }
    private fun gotoPvsActivity(){
        val intent  = Intent(this,MainActivity::class.java)
        finish()
        startActivity(intent)
    }
    private fun gameback(){
        val builder = AlertDialog.Builder(this)
        with(builder){
            setTitle("EXIT")
            setMessage("Do you want exit the game?")
            setPositiveButton("YES") { dialog, which -> gotoPvsActivity()}
            setNegativeButton("NO"){
                    dialog,which ->
            }
        }
        builder.create().show()
    }
    override fun onKeyDown(keyCode: Int, event: KeyEvent?):Boolean{
        if(keyCode==KeyEvent.KEYCODE_BACK){
            gameback()
        }
        return false
    }

    private fun startTimer() {
        runnable = Runnable { doJob() }
        handler.post(runnable)
    }

    private fun doJob() {
        val secs: Int = seconds
        time = String
            .format(
                Locale.getDefault(),
                "%d",secs
            )
        crntTime.text = time
        if (running)
            seconds++

        handler.postDelayed(runnable, 1000)
    }
    private fun setUPborad(){
        val params1 = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0
        )
        val params2 = LinearLayout.LayoutParams(
            0,LinearLayout.LayoutParams.MATCH_PARENT
        )
        for (i in 0 until row) {
            val linearLayout = LinearLayout(this)
            linearLayout.orientation = LinearLayout.HORIZONTAL
            linearLayout.layoutParams = params1
            params1.weight = 1.0F

            for (j in 0 until col) {
                val button = Button(this)
                button.layoutParams = params2
                params2.weight = 1.0F
                setButtonColor(i,j, button)
                button.setOnClickListener {
                    recordMove(i, j)
                }
                linearLayout.addView(button)
            }
            gameboard.addView(linearLayout)
        }
    }
    private fun setButtonColor( i:Int,j:Int,button: Button){
        if(i%2==0){
            if(j%2==0){
                button.setBackgroundResource(R.drawable.bg3)
            }
            else{
                button.setBackgroundResource(R.drawable.bg4)
            }
        }
        else{
            if(j%2==0){
                button.setBackgroundResource(R.drawable.bg4)
            }
            else{
                button.setBackgroundResource(R.drawable.bg3)
            }
        }
    }
    private fun recordMove(x: Int, y: Int) {
        val button = getButton(x, y)
        if (count == 0) {
            count++
            running = true
            setMines(x, y)
            for (i in movement)
                for (j in movement)
                    if (!(i == 0 && j == 0) && ((x + i) in 0 until row) && ((y + j) in 0 until col))
                        reveal(x + i, y + j)
        }
        if (flag) {
            if (board[x][y].isMarked) {
                board[x][y].isMarked = !board[x][y].isMarked
                button.text = "" //set the button as blank as it is not flagged and not revealed now
                setButtonColor(x,y,button)
                flagCount++
                mines.text = "$flagCount"
            } else {
                if (flagCount > 0) {
                    //Mark the button as flag
                    board[x][y].isMarked = !board[x][y].isMarked
                    //update the button UI
                    button.setBackgroundResource(R.drawable.flag_button)
                    button.background = ContextCompat.getDrawable(this, R.drawable.flag)
                    //Decrease the flag count
                    flagCount--
                    mines.text = "$flagCount"
                }
            }
        }
        else {
            if (board[x][y].isMarked || board[x][y].isRevealed) {
                //If the button is marked as flag or has been already revealed, then do nothing
                return
            }
            if (board[x][y].value == MINE) {
                gameLost()
            } else {
                reveal(x, y)
            }
        }
        //check whether user has completed the game
        if (isComplete()) {
            //If true, it means user has won the game
            running = false //stop the timer
            disableAllButtons() //disable the board for further moves
            //update the user that he has won
            Toast.makeText(this, "Congratulations! You won.", Toast.LENGTH_LONG).show()
            if(bestScore==0){
                bestScore = seconds
            }else if(seconds<bestScore){
                bestScore = seconds
            }
            showDialog() //show a share dialog
        }
    }

    private fun getButton(x: Int, y: Int): Button {
        //get the x row of the board
        val rowLayout = gameboard.getChildAt(x) as LinearLayout

        //return the y button in the x row
        return rowLayout.getChildAt(y) as Button
    }

    private fun setMines(r: Int, c: Int) {
        var i = 1
        while (i <= MinesCount) {
            var x = (0 until row).random()
            var y = (0 until col).random()
            if (x != r && y != c && board[x][y].value != MINE) {
                board[x][y].value = MINE
                updateNeighbours(x, y)
                i++
            }
        }
    }

    private fun updateNeighbours(r: Int, c: Int) {

        for (i in movement) {
            for (j in movement) {
                if (((r + i) in 0 until row) && ((c + j) in 0 until col) && board[r + i][c + j].value != MINE)
                    board[r + i][c + j].value++
            }
        }
    }

    private fun reveal(x: Int, y: Int) {
        if (!board[x][y].isRevealed && !board[x][y].isMarked && board[x][y].value != MINE) {
            val button = getButton(x, y)
            button.isEnabled = false //disable the button for future clicks
            board[x][y].isRevealed = true //set the button isRevealed property to true
            //update the disabled button's UI
            button.setBackgroundResource(R.drawable.disabled_button)
            button.setTextColor(ContextCompat.getColor(this, R.color.black))

            if (board[x][y].value == 0) {
                button.text = " "
                for (i in movement)
                    for (j in movement)
                        if (!(i == 0 && j == 0) && ((x + i) in 0 until row) && ((y + j) in 0 until col))
                            reveal(x + i, y + j)
            }else{
                if(id==3){
                    button.textSize = 7.0F
                }
                button.text = board[x][y].value.toString()
            }
        }
    }
    private fun gameLost() {
        revealAllMines()
        disableAllButtons()
        running = false
        //Update the user that he has lost the round
        Toast.makeText(this, "You Loose. Keep trying!", Toast.LENGTH_LONG).show()
        val builder = AlertDialog.Builder(this)
        with(builder){
            setTitle("RESTART GAME")
            setMessage("Do you want another game?")
            setPositiveButton("YES"
            ) { dialog, which -> restartGame()}
            setNegativeButton("NO"){
                    dialog,which -> gotoPvsActivity()
            }
        }
        builder.create().show()
    }
    private fun revealAllMines() {
        for (i in 0 until row) {
            for (j in 0 until col) {
                if (board[i][j].value == MINE) {
                    val button = getButton(i, j)
                    button.setBackgroundResource(R.drawable.mine)
                }
            }
        }
    }
    private fun disableAllButtons() {
        for (x in 0 until row) {
            for (y in 0 until col) {
                val button = getButton(x, y) //get the required button
                button.isEnabled = false
                button.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
        }
    }
    private fun isComplete(): Boolean {
        var minesMarked = true
        board.forEach { row ->
            row.forEach {
                if (it.value == MINE) {
                    if (!it.isMarked)
                        minesMarked = false
                }
            }
        }
        var valuesRevealed = true
        board.forEach { row ->
            row.forEach {
                if (it.value != MINE) {
                    if (!it.isRevealed)
                        valuesRevealed = false
                }
            }
        }

        return minesMarked || valuesRevealed
    }

    private fun showDialog() {
        var lvl = ""
        if(id==1){
            lvl = "Easy"
        }else if(id==2){
            lvl = "Medium"
        }else{
            lvl = "Hard"
        }
        val builder = AlertDialog.Builder(this)
        with(builder) {
            setTitle("\t\t\t\t\t\t\t\t\t\tYour score - $seconds\n\t\t\t\t\t\t\t\t\t\tBest Score - $bestScore")
            setMessage(" \nDo you wish to share your best score with your friends?\n")
            setPositiveButton("Yes") { dialog, which ->
                //if user wants to share his score, launch the required intent
                val intent = Intent(Intent.ACTION_SEND)
                val body =
                    "Hello, I won the Minesweeper game 😎. I finished $lvl level in $bestScore seconds." +
                            "Show your best,Download the game from here 👇👇 \n https://drive.google.com/drive/folders/1ys5Ck945Cq7vX2933KzR9n-KU_HxmSxb?usp=sharing"
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_SUBJECT, "I won !")
                intent.putExtra(Intent.EXTRA_TEXT, body)
                startActivity(Intent.createChooser(intent, "Share your win on..."))
            }
            setNegativeButton(
                "No"
            ) { dialog, which -> restartGame()}

            val alertDialog = builder.create()
            alertDialog.show()
        }
    }

    private fun restartGame() {
        count = 0 //to record user first move
        running = false //to stop the timer
        seconds = 0 //reset the time to 0
        flagCount = MinesCount //reset the number of remaining mines
        flag = false //by default mine button is selected
        for (x in 0 until row) {
            for (y in 0 until col) {
                board[x][y].value = 0
                board[x][y].isMarked = false
                board[x][y].isRevealed = false

                val button = getButton(x, y)
                button.text = ""
                button.isEnabled = true
                setButtonColor(x,y, button)
            }
        }
        mines.text = "$flagCount"
        flagButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.mine))
    }

    override fun onPause() {
        super.onPause()
        wasRunning = running
        running = false
    }
    override fun onResume() {
        super.onResume()
        if (wasRunning)
            running = true
    }
}