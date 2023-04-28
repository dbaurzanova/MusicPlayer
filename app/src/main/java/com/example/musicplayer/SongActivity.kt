package com.example.musicplayer

import android.annotation.SuppressLint
import android.app.Activity
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.example.musicplayer.databinding.ActivitySongBinding
import java.util.concurrent.TimeUnit

class SongActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySongBinding
    private var mediaPlayer: MediaPlayer? = null
    private var seekLength : Int = 0
    private var countPrevious : Int = 0
    private var oldPosition : Int = 0
    private var newPosition : Int = 0
    private var isPlaying : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySongBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val songDuration = intent.getStringExtra("songDuration")
        val imageArray = intent.getIntArrayExtra("imageArray")
        val songNameArray = intent.getStringArrayExtra("songNameArray")
        val artistNameArray = intent.getStringArrayExtra("artistNameArray")
        val songIdArray = intent.getIntArrayExtra("songIdArray")
        val result = intent.getIntExtra("result", 0 )
        oldPosition = intent.getIntExtra("oldPosition", -1)
        newPosition = intent.getIntExtra("newPosition", 0)
        isPlaying = intent.getBooleanExtra("isPlaying", true)

        binding.songName.text = songNameArray!![newPosition]
        binding.artistName.text  =artistNameArray!![newPosition]
        binding.imageId.setImageResource(imageArray!![newPosition])
        binding.songDuration.text = songDuration

        if(newPosition == oldPosition) {
            mediaPlayer = MediaPlayer.create(this, songIdArray!![newPosition])
            mediaPlayer!!.seekTo(result)
            isPlaying = if(isPlaying) {
                mediaPlayer!!.start()
                binding.play.setBackgroundResource(R.drawable.baseline_pause_24)
                true
            } else {
                binding.play.setBackgroundResource(R.drawable.baseline_play_arrow_24)
                false
            }
            updateSeekBar()
        }
        else if(newPosition != oldPosition) {
            mediaPlayer = MediaPlayer.create(this, songIdArray!![newPosition])
            isPlaying = if (isPlaying) {
                mediaPlayer!!.start()
                binding.play.setBackgroundResource(R.drawable.baseline_pause_24)
                true
            } else {
                binding.play.setBackgroundResource(R.drawable.baseline_play_arrow_24)
                false
            }
            updateSeekBar()
        }
        mediaPlayer!!.setOnCompletionListener {
            playNext(songNameArray, artistNameArray, imageArray, songIdArray)
        }

        binding.play.setOnClickListener {
            playSong(songNameArray, artistNameArray, imageArray, songIdArray)
        }

        binding.forward.setOnClickListener {

            playNext(songNameArray, artistNameArray, imageArray, songIdArray)

        }

        binding.rewind.setOnClickListener {

            playPrevious(songNameArray, artistNameArray, imageArray, songIdArray)
        }


        binding.backButt.setOnClickListener{

            onBackPressed()

        }


    }



    private fun updateSeekBar(){

        if (mediaPlayer!= null){
            binding.songTime.text = durationConverter(
                mediaPlayer!!.currentPosition.toLong()
            )
        }

        seekBarSetup()
        Handler().postDelayed(runnable, 50)
    }

    private var runnable = Runnable { updateSeekBar() }

    private fun seekBarSetup() {

        if (mediaPlayer!= null){
            binding.seekbar.progress = mediaPlayer!!.currentPosition
            binding.seekbar.max = mediaPlayer!!.duration
        }

        binding.seekbar.setOnSeekBarChangeListener(
            @SuppressLint(/* ...value = */ "AppCompatCustomView")
        object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if (p2){
                        mediaPlayer!!.seekTo(p1)
                        binding.songTime.text = durationConverter(p1.toLong())
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) { }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                if(mediaPlayer != null && mediaPlayer!!.isPlaying){

                    if (p0 != null){
                        mediaPlayer!!.seekTo(p0.progress)
                    }
                }

            }

        })
    }

    private fun playSong(songNameArray: Array<String>, artistNameArray: Array<String>,
                         imageArray: IntArray?, songIdArray: IntArray?){
        if (!mediaPlayer!!.isPlaying) {
            mediaPlayer!!.seekTo(seekLength)
            mediaPlayer!!.start()
            binding.play.setBackgroundResource(R.drawable.baseline_pause_24)
            updateSeekBar()
            mediaPlayer!!.setOnCompletionListener {
                playNext(songNameArray, artistNameArray, imageArray, songIdArray)
            }
        }
        else{
            mediaPlayer!!.pause()
            seekLength = mediaPlayer!!.currentPosition
            binding.play.setBackgroundResource(R.drawable.baseline_play_arrow_24)
        }

    }

    private fun playNext(
        songNameArray: Array<String>, artistNameArray: Array<String>,
        imageArray: IntArray?, songIdArray: IntArray?
    ){
        mediaPlayer!!.release()
        mediaPlayer = null

        if (newPosition < songNameArray!!.size - 1) {
            newPosition += 1
        } else{
            newPosition = 0
        }

        binding.songName.text = songNameArray!![newPosition]
        binding.artistName.text  = artistNameArray!![newPosition]
        binding.imageId.setImageResource(imageArray!![newPosition])
        mediaPlayer = MediaPlayer.create(this, songIdArray!![newPosition])
        val duration = mediaPlayer!!.duration
        val durationLong = duration.toLong()
        binding.songDuration.text = durationConverter(durationLong)
        mediaPlayer!!.start()
        updateSeekBar()
        binding.play.setBackgroundResource(R.drawable.baseline_pause_24)
    }

    private fun playPrevious(
        songNameArray: Array<String>, artistNameArray: Array<String>,
        imageArray: IntArray?, songIdArray: IntArray?
    ){
        mediaPlayer!!.release()
        mediaPlayer = null
        if (countPrevious == 0){
            updateSeekBar()
            countPrevious++
        }
        else {
            if (newPosition > 0) {
                newPosition -= 1
            } else {
                newPosition = songNameArray.size - 1
            }
            countPrevious = 0
        }
        binding.songName.text = songNameArray!![newPosition]
        binding.artistName.text  = artistNameArray!![newPosition]
        binding.imageId.setImageResource(imageArray!![newPosition])
        mediaPlayer = MediaPlayer.create(this, songIdArray!![newPosition])
        val duration = mediaPlayer!!.duration
        val durationLong = duration.toLong()
        binding.songDuration.text = durationConverter(durationLong)
        mediaPlayer!!.start()
        updateSeekBar()
        binding.play.setBackgroundResource(R.drawable.baseline_pause_24)

    }

    private fun clearMediaPlayer() {
        if(mediaPlayer!!.isPlaying){
            mediaPlayer!!.stop()
        }
        mediaPlayer!!.release()
        mediaPlayer = null
    }

    private fun durationConverter(duration : Long): String {
        return String.format(
            "%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(duration),
            TimeUnit.MILLISECONDS.toSeconds(duration) -
                    TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(duration)
                    )
        )
    }
    override fun onBackPressed() {
        intent.putExtra("result", mediaPlayer!!.currentPosition)
        intent.putExtra("newPosition", newPosition)
        intent.putExtra("isPlaying", mediaPlayer!!.isPlaying)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        clearMediaPlayer()
    }

}