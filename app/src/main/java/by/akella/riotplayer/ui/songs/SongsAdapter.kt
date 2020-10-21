package by.akella.riotplayer.ui.songs

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import by.akella.riotplayer.R
import by.akella.riotplayer.databinding.ItemSongBinding
import by.akella.riotplayer.ui.base.model.SongUiModel
import by.akella.riotplayer.ui.custom.SafeClickListener
import by.akella.riotplayer.util.loadAlbumIcon

class SongsAdapter(
    private val onItemClickListener: SafeClickListener<SongUiModel>
) : ListAdapter<SongUiModel, SongsViewHolder>(
    object : DiffUtil.ItemCallback<SongUiModel>() {
        override fun areItemsTheSame(oldItem: SongUiModel, newItem: SongUiModel): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: SongUiModel, newItem: SongUiModel): Boolean =
            oldItem == newItem
    }
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongsViewHolder {
        val binding = ItemSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongsViewHolder(binding).also { holder ->
            binding.root.setOnClickListener { onItemClickListener.onClick(getItem(holder.adapterPosition)) }
        }
    }

    override fun onBindViewHolder(holder: SongsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class SongsViewHolder(private val binding: ItemSongBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(song: SongUiModel) {
        with(binding) {
            name.text = song.title
            artist.text = song.artist
            songIcon.loadAlbumIcon(song.albumArtPath, R.drawable.ic_musical_note)
        }
    }
}
