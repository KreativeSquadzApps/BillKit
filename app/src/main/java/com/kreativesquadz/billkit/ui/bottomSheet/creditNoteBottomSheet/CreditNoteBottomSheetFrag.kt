package com.kreativesquadz.billkit.ui.bottomSheet.creditNoteBottomSheet

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kreativesquadz.billkit.BR
import com.kreativesquadz.billkit.R
import com.kreativesquadz.billkit.adapter.GenericAdapter
import com.kreativesquadz.billkit.databinding.FragmentCreditNoteBottomSheetBinding
import com.kreativesquadz.billkit.interfaces.OnItemClickListener
import com.kreativesquadz.billkit.model.Category
import com.kreativesquadz.billkit.model.CreditNote
import com.kreativesquadz.billkit.ui.home.tab.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreditNoteBottomSheetFrag : BottomSheetDialogFragment() {
    var _binding: FragmentCreditNoteBottomSheetBinding? = null
    val binding get() = _binding!!
    val viewModel: CreditNoteBottomSheetViewModel by activityViewModels()
    val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var adapter: GenericAdapter<CreditNote>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme)
        viewModel.getcreditNoteList()
    }

    override fun onStart() {
        super.onStart()
        dialog?.let { dialog ->
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
            val behavior = BottomSheetBehavior.from(bottomSheet)

            // Calculate the height of the ActionBar
            val actionBarHeight = getActionBarHeight()

            // Get the display height
            val displayMetrics = DisplayMetrics()
            requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
            val screenHeight = displayMetrics.heightPixels

            // Set the height of the BottomSheet to be the screen height minus the ActionBar height
            val layoutParams = bottomSheet.layoutParams
            layoutParams.height = screenHeight - actionBarHeight
            bottomSheet.layoutParams = layoutParams
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
            bottomSheet.background = ContextCompat.getDrawable(requireContext(), R.drawable.corner_top)

        }
    }

    private fun getActionBarHeight(): Int {
        var actionBarHeight = 0
        val styledAttributes = requireContext().theme.obtainStyledAttributes(intArrayOf(android.R.attr.actionBarSize))
        actionBarHeight = styledAttributes.getDimensionPixelSize(0, 0)
        styledAttributes.recycle()
        return actionBarHeight
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreditNoteBottomSheetBinding.inflate(inflater, container, false)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        onClickListeners()
        observers()
    }

    private fun onClickListeners() {
        binding.etCreditNote.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.creditNoteList.value?.data?.let {
                    adapter.submitList(it.filter { creditNote ->
                        creditNote.invoiceNumber.contains(s.toString(), ignoreCase = true)})
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        binding.btnDismiss.setOnClickListener {
            dismiss()
        }
    }

    private fun observers(){
        viewModel.creditNoteList.observe(viewLifecycleOwner){
            it.data?.let {
                adapter.submitList(it.sortedByDescending{ it.dateTime })
            }
        }
    }


    private fun setupRecyclerView() {
        adapter = GenericAdapter(
            viewModel.creditNoteList.value?.data ?: emptyList(),
            object : OnItemClickListener<CreditNote> {
                override fun onItemClick(item: CreditNote) {
                    // Handle item click
                    if (item.status.equals("Active")){
                        sharedViewModel.addCreditNote(item)
                        dismiss()
                    }

                }
            },
            R.layout.item_credit_note,
            BR.creditNote // Variable ID generated by data binding
        )
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}