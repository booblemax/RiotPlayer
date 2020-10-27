package by.akella.riotplayer.ui.albumdetails

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.akella.riotplayer.R
import by.akella.riotplayer.databinding.AlbumDetailsFragmentBinding
import by.akella.riotplayer.ui.base.BaseFragment
import by.akella.riotplayer.ui.base.OffsetItemDecoration
import by.akella.riotplayer.ui.custom.SafeClickListener
import by.akella.riotplayer.ui.songs.BottomOffsetItemDecoration
import by.akella.riotplayer.ui.songs.SongsAdapter
import by.akella.riotplayer.util.TimeUtils
import by.akella.riotplayer.util.gone
import by.akella.riotplayer.util.info
import by.akella.riotplayer.util.visible
import com.babylon.orbit2.livedata.state
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlbumDetailsFragment : BaseFragment() {

    private val viewModel: AlbumDetailsViewModel by viewModels()
    private lateinit var binding: AlbumDetailsFragmentBinding
    private lateinit var adapter: SongsAdapter
    private val args: AlbumDetailsFragmentArgs by lazy {
        AlbumDetailsFragmentArgs.fromBundle(
            requireArguments()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = AlbumDetailsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = SongsAdapter(SafeClickListener { navigateToPlayer(it.id) })
        with(binding.songs) {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            addItemDecoration(
                OffsetItemDecoration(
                    top = resources.getDimensionPixelOffset(R.dimen.size_26),
                    bottom = resources.getDimensionPixelOffset(R.dimen.size_8)
                )
            );
            adapter = this@AlbumDetailsFragment.adapter
        }
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        viewModel.container.state.observe(viewLifecycleOwner, this::renderState)
        viewModel.loadSongs(args.albumModel)
    }

    private fun renderState(state: AlbumDetailsState) {
        info(state.toString())
        with(state) {
            binding.toolbar.title = album?.name
            Glide.with(requireContext())
                .asBitmap()
                .load(album?.artUrl)
                .addListener(AlbumRequestListener())
                .into(binding.expandingImage)
            adapter.submitList(songs)

            renderAlbumInfo(countSongs, durationSongs)
        }
    }

    private fun renderAlbumInfo(countSongs: Int, durationSongs: Long) {
        if (countSongs > 0 || durationSongs > 0) {
            binding.detailsLayout.visible()

            if (countSongs > 0) {
                binding.countSongs.visible()
                binding.countSongs.text =
                    resources.getQuantityString(R.plurals.count_songs, countSongs, countSongs)
            } else {
                binding.countSongs.gone()
            }
            if (durationSongs > 0) {
                binding.durationSongs.visible()
                val convertMillisToMinutes =
                    TimeUtils.convertMillisToMinutes(requireContext(), durationSongs)
                info(convertMillisToMinutes)
                binding.durationSongs.text =
                    convertMillisToMinutes
            } else {
                binding.durationSongs.gone()
            }
        } else {
            binding.detailsLayout.gone()
        }
    }

    private fun navigateToPlayer(mediaId: String) {
        findNavController().navigate(
            AlbumDetailsFragmentDirections.actionAlbumDetailsFragmentToPlayerFragment(mediaId)
        )
    }

    private inner class AlbumRequestListener : RequestListener<Bitmap> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Bitmap>?,
            isFirstResource: Boolean
        ): Boolean = false

        override fun onResourceReady(
            resource: Bitmap?,
            model: Any?,
            target: Target<Bitmap>?,
            dataSource: DataSource?,
            isFirstResource: Boolean
        ): Boolean {
            resource?.let {
                Palette.Builder(resource)
                    .maximumColorCount(16)
                    .generate { p ->
                        val rgb = p?.dominantSwatch?.rgb
                        rgb?.let { color -> binding.collapsingLayout.setExpandedTitleColor(color) }
                    }
            }
            return false
        }
    }
}
