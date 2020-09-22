package by.akella.riotplayer.ui.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import by.akella.riotplayer.R
import by.akella.riotplayer.databinding.MainActivityBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}