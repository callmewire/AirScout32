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
import com.airscout.airscout32.data.SessionData
import com.airscout.airscout32.databinding.FragmentHistoricalBinding
import com.airscout.airscout32.viewmodel.AirDataViewModel

class HistoricalFragment : Fragment() {
    private var _binding: FragmentHistoricalBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: AirDataViewModel
    private lateinit var adapter: SessionAdapter

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
        observeData()
    }
    
    private fun setupRecyclerView() {
        adapter = SessionAdapter(
            onExportClick = { session -> exportSession(session) },
            onDeleteClick = { session -> deleteSession(session) }
        )
        binding.rvSessions.adapter = adapter
        binding.rvSessions.layoutManager = LinearLayoutManager(requireContext())
    }
    
    private fun setupButtons() {
        binding.btnClearAllSessions.setOnClickListener {
            showClearAllConfirmationDialog()
        }
    }
    
    private fun observeData() {
        viewModel.savedSessions.observe(viewLifecycleOwner) { sessions ->
            adapter.updateSessions(sessions)
            binding.tvSessionCount.text = "${sessions.size} sessions"
        }
    }
    
    private fun exportSession(session: SessionData) {
        val file = viewModel.exportSessionToCsv(session)
        if (file != null) {
            Toast.makeText(requireContext(), "CSV exportiert: ${file.name}", Toast.LENGTH_LONG).show()
            
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
            Toast.makeText(requireContext(), "Fehler beim Export", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun deleteSession(session: SessionData) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Sitzung löschen")
            .setMessage("Sitzung '${session.sessionName}' löschen?")
            .setPositiveButton("Löschen") { _, _ ->
                viewModel.deleteSession(session)
                Toast.makeText(requireContext(), "Sitzung gelöscht", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }
    
    private fun showClearAllConfirmationDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Alle Sitzungen löschen")
            .setMessage("Alle gespeicherten Sitzungen löschen? Dies kann nicht rückgängig gemacht werden.")
            .setPositiveButton("Alle löschen") { _, _ ->
                viewModel.clearAllSessions()
                Toast.makeText(requireContext(), "Alle Sitzungen gelöscht", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class SessionAdapter(
    private val onExportClick: (SessionData) -> Unit,
    private val onDeleteClick: (SessionData) -> Unit
) : RecyclerView.Adapter<SessionAdapter.ViewHolder>() {
    
    private var sessions = listOf<SessionData>()
    
    fun updateSessions(newSessions: List<SessionData>) {
        sessions = newSessions
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val session = sessions[position]
        
        holder.title.text = session.sessionName
        holder.subtitle.text = "${session.getFormattedDate()} | ${session.dataCount} Punkte | ${session.getDurationMinutes()} min"
        
        holder.itemView.setOnLongClickListener {
            showSessionOptionsDialog(holder.itemView.context, session)
            true
        }
    }
    
    private fun showSessionOptionsDialog(context: android.content.Context, session: SessionData) {
        val options = arrayOf("CSV exportieren", "Sitzung löschen")
        androidx.appcompat.app.AlertDialog.Builder(context)
            .setTitle(session.sessionName)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> onExportClick(session)
                    1 -> onDeleteClick(session)
                }
            }
            .show()
    }
    
    override fun getItemCount() = sessions.size
    
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: android.widget.TextView = view.findViewById(android.R.id.text1)
        val subtitle: android.widget.TextView = view.findViewById(android.R.id.text2)
    }
}
