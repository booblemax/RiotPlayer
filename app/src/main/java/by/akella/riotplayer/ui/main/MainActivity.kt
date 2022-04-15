package by.akella.riotplayer.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import by.akella.riotplayer.databinding.MainActivityBinding
import by.akella.riotplayer.media.RiotMediaController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var riotMediaController: RiotMediaController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
