package com.kreativesquadz.billkit.ui.settings

import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.kreativesquadz.billkit.BR
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.R
import com.kreativesquadz.billkit.adapter.GenericAdapter
import com.kreativesquadz.billkit.adapter.showCustomAlertDialog
import com.kreativesquadz.billkit.databinding.FragmentSettingsBinding
import com.kreativesquadz.billkit.interfaces.OnItemClickListener
import com.kreativesquadz.billkit.model.Category
import com.kreativesquadz.billkit.model.DialogData
import com.kreativesquadz.billkit.ui.login.LoginActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFrag : Fragment() {
    var _binding: FragmentSettingsBinding? = null
    val binding get() = _binding!!
    private lateinit var adapter: GenericAdapter<String>
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        onClickListeners()
        setupRecyclerView()

        return binding.root
    }
    fun onClickListeners(){
        binding.signOut.setOnClickListener {
            viewModel.logout()
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        adapter = GenericAdapter(
            listOf(getString(R.string.settings_item_1),
                getString(R.string.settings_item_2),
                getString(R.string.settings_item_3),
                getString(R.string.settings_item_4),
                getString(R.string.settings_item_5),
                getString(R.string.settings_item_6)),
            object : OnItemClickListener<String> {
                override fun onItemClick(item: String) {
                    when (item) {
                        getString(R.string.settings_item_1) -> {
                            findNavController().navigate(R.id.action_settingsFrag_to_invoiceSettings)
                        }
                        getString(R.string.settings_item_2) -> {
                            findNavController().navigate(R.id.action_settingsFrag_to_taxSettingsFragment)
                        }
                        getString(R.string.settings_item_3) -> {
                            findNavController().navigate(R.id.action_settingsFrag_to_posSettingsFrag)
                        }
                        getString(R.string.settings_item_4) -> {
                            findNavController().navigate(R.id.action_settingsFrag_to_printerSettingsFragment)

                        }
                        getString(R.string.settings_item_5) -> {
                            findNavController().navigate(R.id.action_settingsFrag_to_themeFragment)
                        }
                        getString(R.string.settings_item_6) -> {
                            setupPopup{
                                viewModel.hardReset(Config.userId,requireContext())
                            }
                        }

                    }
                }
            },
            R.layout.item_settings,
            BR.itemSettings // Variable ID generated by data binding
        )
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
    }
    private fun setupPopup(action: () -> Unit){

        val dialogData = DialogData(
            title = "Hard Reset",
            info = "Are you sure you want to Reset the complete data ?",
            positiveButtonText = "YES",
            negativeButtonText = "Cancel"
        )

        showCustomAlertDialog(
            context = requireActivity(),
            dialogData = dialogData,
            positiveAction = {
                action()
            },
            negativeAction = {
                // Handle negative button action
                // E.g., dismiss the dialog
            }
        )
    }

}