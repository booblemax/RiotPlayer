package by.akella.riotplayer.ui.albums

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import by.akella.riotplayer.R
import by.akella.riotplayer.databinding.ItemAlbumBinding
import by.akella.riotplayer.repository.albums.AlbumModel
import by.akella.riotplayer.ui.custom.SafeClickListener
import com.bumptech.glide.Glide

class AlbumAdapter(
    private val onClick: SafeClickListener<AlbumModel>
) : ListAdapter<AlbumModel, AlbumViewHolder>(AlbumDiff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val binding = ItemAlbumBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlbumViewHolder(binding).apply {
            itemView.setOnClickListener { onClick(getItem(adapterPosition)) }
        }
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private class AlbumDiff : DiffUtil.ItemCallback<AlbumModel>() {
        override fun areItemsTheSame(oldItem: AlbumModel, newItem: AlbumModel): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: AlbumModel, newItem: AlbumModel): Boolean =
            oldItem == newItem
    }
}

class AlbumViewHolder(
    private val binding: ItemAlbumBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(album: AlbumModel) {
        with(binding) {
            val context = albumName.context
            albumName.text = album.name
            albumArtist.text = album.artist

            Glide.with(context)
                .load(album.artUrl)
                .into(albumImage)
        }
    }
}
