package com.airscout.airscout32.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.airscout.airscout32.databinding.FragmentSettingsBinding
import com.airscout.airscout32.viewmodel.AirDataViewModel

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: AirDataViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(requireActivity())[AirDataViewModel::class.java]
        
        setupButtons()
    }
    
    private fun setupButtons() {
        binding.btnResetCurrent.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Aktuelle Werte zurücksetzen")
                .setMessage("Möchten Sie die aktuell angezeigten Werte wirklich zurücksetzen?")
                .setPositiveButton("Zurücksetzen") { _, _ ->
                    viewModel.resetCurrentValues()
                    Toast.makeText(requireContext(), "Aktuelle Werte zurückgesetzt", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Abbrechen", null)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
