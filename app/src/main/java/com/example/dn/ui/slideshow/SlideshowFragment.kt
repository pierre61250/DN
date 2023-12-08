package com.example.dn.ui.slideshow

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.dn.R
import com.example.dn.databinding.FragmentSlideshowBinding

class SlideshowFragment : Fragment() {

    private var _binding: FragmentSlideshowBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false

    companion object {
        const val AUDIO_PERMISSION_REQUEST_CODE = 101
        const val MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 123 // Choose any unique number
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val slideshowViewModel =
            ViewModelProvider(this).get(SlideshowViewModel::class.java)

        _binding = FragmentSlideshowBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textSlideshow
        slideshowViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        // Demander la permission d'enregistrement audio si elle n'est pas accordée
        if (!checkAudioPermission()) {
            requestAudioPermission()
        }

        // Configurer le bouton d'enregistrement
        val recordButton: Button = binding.recordButton
        recordButton.setOnClickListener {
            if (isRecording) {
                stopRecording(recordButton)
            } else {
                startRecording(recordButton)
            }
        }
        
        return root
    }

    private fun startRecording(button: Button) {
        button.setBackgroundColor(Color.RED)
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE
            )
        }
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile("${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS)}/file.mp3")
            prepare()
            start()
        }

        isRecording = true
    }

    private fun stopRecording(button: Button) {
        button.setBackgroundColor(Color.MAGENTA)
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null

        isRecording = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        // Arrêtez l'enregistrement si l'activité est détruite
        if (isRecording) {
            stopRecording(binding.recordButton)
        }
    }

    private fun checkAudioPermission(): Boolean {
        val permission = Manifest.permission.RECORD_AUDIO
        return ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestAudioPermission() {
        val permission = Manifest.permission.RECORD_AUDIO
        ActivityCompat.requestPermissions(requireActivity(), arrayOf(permission), AUDIO_PERMISSION_REQUEST_CODE)
    }
}