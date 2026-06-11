package me.inkdye.zuxos.wallpaper

import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

class ZuxosVideoWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = VideoEngine()

    private inner class VideoEngine : Engine() {
        private var player: ExoPlayer? = null
        private var surfaceHolderRef: SurfaceHolder? = null
        private var surfaceReady = false
        private var visible = false

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            surfaceReady = true
            surfaceHolderRef = holder
            preparePlayer(holder)
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            surfaceHolderRef = holder
            player?.setVideoSurfaceHolder(holder)
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            surfaceReady = false
            surfaceHolderRef = null
            releasePlayer()
            super.onSurfaceDestroyed(holder)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            this.visible = visible
            val currentPlayer = player
            if (visible) {
                if (surfaceReady && currentPlayer == null) {
                    surfaceHolderRef?.let { preparePlayer(it) }
                } else {
                    currentPlayer?.playWhenReady = true
                    currentPlayer?.play()
                }
            } else {
                currentPlayer?.pause()
                currentPlayer?.playWhenReady = false
            }
        }

        override fun onDestroy() {
            releasePlayer()
            super.onDestroy()
        }

        private fun preparePlayer(holder: SurfaceHolder) {
            releasePlayer()
            val video = LenovoLockVideoWallpaperManager.activeDesktopVideoFile(this@ZuxosVideoWallpaperService)
            if (!video.isFile) {
                Log.w(TAG, "No active desktop live wallpaper video: ${video.absolutePath}")
                drawFallbackFrame(holder)
                return
            }

            runCatching {
                player = ExoPlayer.Builder(this@ZuxosVideoWallpaperService)
                    .build()
                    .apply {
                        repeatMode = Player.REPEAT_MODE_ONE
                        volume = 0f
                        playWhenReady = visible
                        setVideoSurfaceHolder(holder)
                        setMediaItem(MediaItem.fromUri(Uri.fromFile(video)))
                        addListener(
                            object : Player.Listener {
                                override fun onPlaybackStateChanged(playbackState: Int) {
                                    Log.i(
                                        TAG,
                                        "Playback state=$playbackState playWhenReady=$playWhenReady " +
                                            "isPlaying=$isPlaying video=${video.absolutePath}",
                                    )
                                }

                                override fun onPlayerError(error: PlaybackException) {
                                    Log.e(TAG, "ExoPlayer error code=${error.errorCodeName}", error)
                                    releasePlayer()
                                    drawFallbackFrame(holder)
                                }
                            },
                        )
                        prepare()
                        if (visible) play()
                    }
                Log.i(TAG, "Prepared ExoPlayer desktop live wallpaper video: ${video.absolutePath}")
            }.onFailure { error ->
                Log.e(TAG, "Failed to prepare ExoPlayer desktop live wallpaper video", error)
                releasePlayer()
                drawFallbackFrame(holder)
            }
        }

        private fun drawFallbackFrame(holder: SurfaceHolder) {
            val canvas = runCatching { holder.lockCanvas() }.getOrNull() ?: return
            try {
                canvas.drawColor(Color.BLACK)
                val preview = LenovoLockVideoWallpaperManager.activeDesktopPreviewFile(this@ZuxosVideoWallpaperService)
                    ?.takeIf { it.isFile }
                    ?.let { BitmapFactory.decodeFile(it.absolutePath) }
                    ?: return
                try {
                    val source = Rect(0, 0, preview.width, preview.height)
                    val target = cropCenter(preview.width, preview.height, canvas.width, canvas.height)
                    canvas.drawBitmap(preview, source, target, null)
                } finally {
                    preview.recycle()
                }
            } finally {
                runCatching { holder.unlockCanvasAndPost(canvas) }
            }
        }

        private fun cropCenter(sourceWidth: Int, sourceHeight: Int, targetWidth: Int, targetHeight: Int): Rect {
            val sourceRatio = sourceWidth.toFloat() / sourceHeight.toFloat()
            val targetRatio = targetWidth.toFloat() / targetHeight.toFloat()
            return if (sourceRatio > targetRatio) {
                val width = (targetHeight * sourceRatio).toInt()
                val left = (targetWidth - width) / 2
                Rect(left, 0, left + width, targetHeight)
            } else {
                val height = (targetWidth / sourceRatio).toInt()
                val top = (targetHeight - height) / 2
                Rect(0, top, targetWidth, top + height)
            }
        }

        private fun releasePlayer() {
            player?.let { currentPlayer ->
                runCatching { currentPlayer.clearVideoSurface() }
                runCatching { currentPlayer.release() }
            }
            player = null
        }
    }

    private companion object {
        const val TAG = "ZuxosVideoWallpaper"
    }
}
