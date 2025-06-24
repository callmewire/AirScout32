package com.airscout.airscout32.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airscout.airscout32.bluetooth.TimestampedSensorData
import com.airscout.airscout32.databinding.FragmentHistoricalBinding
import com.airscout.airscout32.viewmodel.AirDataViewModel
import java.text.SimpleDateFormat
import java.util.*

class HistoricalFragment : Fragment() {
    private var _binding: FragmentHistoricalBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: AirDataViewModel
    private lateinit var adapter: HistoricalDataAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoricalBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(requireActivity())[AirDataViewModel::class.java]
        
        setupRecyclerView()
        setupButtons()
        loadData()
    }
    
    private fun setupRecyclerView() {
        adapter = HistoricalDataAdapter()
        binding.rvHistoricalData.adapter = adapter
        binding.rvHistoricalData.layoutManager = LinearLayoutManager(requireContext())
    }
    
    private fun setupButtons() {
        binding.btnExportCsv.setOnClickListener {
            exportToCsv()
        }
        
        binding.btnClearHistory.setOnClickListener {
            showClearConfirmationDialog()
        }
    }
    
    private fun loadData() {
        val history = viewModel.getStoredDataHistory()
        adapter.updateData(history)
        binding.tvDataCount.text = "${history.size} Datensätze"
    }
    
    private fun exportToCsv() {
        val file = viewModel.exportStoredDataToCsv()
        if (file != null) {
            Toast.makeText(requireContext(), "CSV exportiert: ${file.name}", Toast.LENGTH_LONG).show()
            
            // Share the file
            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "CSV Datei teilen"))
        } else {
            Toast.makeText(requireContext(), "Fehler beim CSV Export", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showClearConfirmationDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Daten löschen")
            .setMessage("Möchten Sie alle gespeicherten Daten wirklich löschen?")
            .setPositiveButton("Löschen") { _, _ ->
                viewModel.clearStoredHistory()
                loadData()
                Toast.makeText(requireContext(), "Daten gelöscht", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class HistoricalDataAdapter : RecyclerView.Adapter<HistoricalDataAdapter.ViewHolder>() {
    
    private var data = listOf<TimestampedSensorData>()
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
    
    fun updateData(newData: List<TimestampedSensorData>) {
        data = newData.sortedByDescending { it.timestamp }
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        val date = Date(item.timestamp)
        
        holder.title.text = dateFormat.format(date)
        holder.subtitle.text = "T: ${String.format("%.1f", item.data.temperature)}°C, " +
                "H: ${String.format("%.1f", item.data.humidity)}%, " +
                "G1: ${String.format("%.0f", item.data.gas1)}, " +
                "Bat: ${String.format("%.2f", item.data.battery)}V"
    }
    
    override fun getItemCount() = data.size
    
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: android.widget.TextView = view.findViewById(android.R.id.text1)
        val subtitle: android.widget.TextView = view.findViewById(android.R.id.text2)
    }
}
