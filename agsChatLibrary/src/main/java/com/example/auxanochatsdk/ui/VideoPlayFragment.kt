package com.example.auxanochatsdk.ui

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import com.example.auxanochatsdk.Activity.LibraryActivity
import com.example.auxanochatsdk.R
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.extractor.ExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.ui.SimpleExoPlayerView
import com.google.android.exoplayer2.upstream.BandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import kotlinx.android.synthetic.main.fragment_video_play.*
import kotlinx.android.synthetic.main.fragment_video_play.view.*

class VideoPlayFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_video_play, container, false)
        val videoString = requireArguments().getString("videoString")
        val videoType = requireArguments().getString("videoType")
        Log.e("getVideoString", ": "+videoString )

            view.idExoPlayerView.visibility=View.VISIBLE
            var exoPlayer: SimpleExoPlayer
            try {
                val bandwidthMeter: BandwidthMeter = DefaultBandwidthMeter()
                val trackSelector: TrackSelector =
                    DefaultTrackSelector(AdaptiveTrackSelection.Factory(bandwidthMeter))
                exoPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector)
                val videouri: Uri = Uri.parse(videoString)
                val dataSourceFactory = DefaultHttpDataSourceFactory("exoplayer_video")
                val extractorsFactory: ExtractorsFactory = DefaultExtractorsFactory()
                val mediaSource: MediaSource = ExtractorMediaSource(
                    videouri,
                    dataSourceFactory,
                    extractorsFactory,
                    null,
                    null
                )
                view.idExoPlayerView.setPlayer(exoPlayer)
                exoPlayer.prepare(mediaSource)
                exoPlayer.setPlayWhenReady(true)
            } catch (e: Exception) {
                Log.e("TAG", "Error : $e")
            }

        view.closevideoshowdialogue.setOnClickListener(View.OnClickListener {
            LibraryActivity.navController?.navigateUp()
        })
        return view
    }

}