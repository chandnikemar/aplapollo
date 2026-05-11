package com.example.aplapollo.view.ProductionEntry

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.aplapollo.api.RetrofitInstance
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.Resource
import com.example.aplapollo.helper.SessionManager
import com.example.aplapollo.model.LocationPaginationRequest
import com.example.aplapollo.view.Pickling.PicklingActivity
import com.example.aplapollo.view.coldpressing.CRMActivity
import com.example.aplapollo.view.slitting.SlittingActivity
import com.example.aplapollo.viewmodel.actiontype.ActionTypeViewModel
import com.example.aplapollo.viewmodel.actiontype.ActionTypeViewModelfactory
import com.example.aplapollo.viewmodel.location.LocationViewModel
import com.example.aplapollo.viewmodel.location.LocationViewModelFactory
import com.example.aplapollo.viewmodel.machine.MachineViewModel
import com.example.aplapollo.viewmodel.machine.MachineViewModelfactory
import com.example.apolloapl.databinding.ActivityInputProductionEntryBinding
import es.dmoral.toasty.Toasty

class InputProductionEntryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInputProductionEntryBinding
    private lateinit var progress: ProgressDialog
    private lateinit var locationViewModel: LocationViewModel
    private lateinit var machineViewModel: MachineViewModel
    private lateinit var actionTypeViewModel: ActionTypeViewModel
    private lateinit var session: SessionManager

    private var selectedLocationId: Int? = null
    private var selectedLocationName: String? = null
    private var selectedActionTypeId: Int? = null

    private var selectedProcessName: String = ""
    private var selectedMachineName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInputProductionEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.idLayoutHeader.tvTitle.text = "Production Entry"
        binding.idLayoutHeader.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        supportActionBar?.hide()

        progress = ProgressDialog(this)
        progress.setMessage("Please Wait...")

        val retrofitInstance = RetrofitInstance.getInstance(applicationContext)

        locationViewModel = ViewModelProvider(
            this,
            LocationViewModelFactory(application, retrofitInstance)
        )[LocationViewModel::class.java]

        actionTypeViewModel = ViewModelProvider(
            this,
            ActionTypeViewModelfactory(application, retrofitInstance)
        )[ActionTypeViewModel::class.java]

        machineViewModel = ViewModelProvider(
            this,
            MachineViewModelfactory(application, retrofitInstance)
        )[MachineViewModel::class.java]

        session = SessionManager(this)
        val user = session.getUserDetails()
        val userName = user["userName"] ?: ""

        binding.textOperator.text = userName.toString()
        binding.dropdownProcess.isEnabled = false
        binding.machineName.isEnabled = false
        binding.machineRW.visibility = View.GONE

        loadLocations()
        loadProcesses()
        observeMachines()

        binding.btnSubmit.setOnClickListener {
            validateAndNavigate()
        }
    }

    // ================= LOCATION =================

    private fun loadLocations() {

        val request = LocationPaginationRequest(
            locationId = 0,
            locationName = "",
            locationCode = "",
            locationType = null,
            displayName = null,
            parentLocationId = null,
            isActive = true,
            rowSize = 10,
            currentPage = 1
        )

        locationViewModel.getLocations(request)

        locationViewModel.locationListMutableLiveData.observe(this) { res ->
            when (res) {
                is Resource.Loading -> progress.show()

                is Resource.Success -> {
                    progress.dismiss()

                    val list = res.data ?: emptyList()
                    val adapter = ArrayAdapter(
                        this,
                        android.R.layout.simple_list_item_1,
                        list.map { it.locationName }
                    )
                    binding.dropdownStation.setAdapter(adapter)

                    binding.dropdownStation.setOnItemClickListener { _, _, position, _ ->
                        val selectedLocation  = list[position]
                        selectedLocationId = selectedLocation .locationId
                        selectedLocationName = selectedLocation .locationName

                        Log.d("LOCATION_DEBUG", "Selected = $selectedLocationId")
                        binding.dropdownProcess.isEnabled = true
                    }
                }

                is Resource.Error -> {
                    progress.dismiss()
                    Toast.makeText(this, res.message, Toast.LENGTH_SHORT).show()
                }

                else -> {}
            }
        }
    }

    // ================= PROCESS =================

    private fun loadProcesses() {

        actionTypeViewModel.getActionTypes()

        actionTypeViewModel.actionTypeListMutableLiveData.observe(this) { res ->
            when (res) {

                is Resource.Loading -> progress.show()

                is Resource.Success -> {
                    progress.dismiss()

                    val list = res.data ?: emptyList()

                    val adapter = ArrayAdapter(
                        this,
                        android.R.layout.simple_list_item_1,
                        list.map { it.displayName }
                    )

                    binding.dropdownProcess.setAdapter(adapter)

                    binding.dropdownProcess.setOnItemClickListener { _, _, pos, _ ->

                        val selected = list[pos]

                        selectedActionTypeId = selected.actionTypeId
                        selectedProcessName = selected.displayName

                        binding.machineRW.visibility = View.VISIBLE
                        binding.machineName.setText("")
                        binding.machineName.isEnabled = true

                        machineViewModel.getProcessMachine(selected.actionTypeId)
                    }
                }

                is Resource.Error -> {
                    progress.dismiss()
                    Toast.makeText(this, res.message, Toast.LENGTH_SHORT).show()
                }

                else -> {}
            }
        }
    }

    // ================= MACHINE =================

    private fun observeMachines() {

        machineViewModel.machineListMutableLiveData.observe(this) { res ->

            when (res) {

                is Resource.Loading -> progress.show()

                is Resource.Success -> {
                    progress.dismiss()

                    val list = res.data ?: emptyList()

                    val adapter = ArrayAdapter(
                        this,
                        android.R.layout.simple_list_item_1,
                        list.map { it.machineName }
                    )

                    binding.machineName.setAdapter(adapter)

                    // Auto select
                    if (list.size == 1) {
                        val machine = list[0]
                        selectedMachineName = machine.machineName
                        binding.machineName.setText(machine.machineName, false)
                    }

                    binding.machineName.setOnItemClickListener { _, _, pos, _ ->
                        val machine = list[pos]
                        selectedMachineName = machine.machineName
                        binding.machineName.setText(machine.machineName, false)
                    }
                }

                is Resource.Error -> {
                    progress.dismiss()
                    Toast.makeText(this, res.message, Toast.LENGTH_SHORT).show()
                }

                else -> {}
            }
        }
    }

    // ================= SUBMIT =================

    private fun validateAndNavigate() {

        when {
            selectedLocationId == null -> {
                Toasty.warning(this, "Select location").show()
            }

            selectedActionTypeId == null -> {
                Toasty.warning(this, "Select process").show()
            }

            selectedMachineName.isEmpty() -> {
                Toasty.warning(this, "Select machine").show()
            }

            selectedProcessName.equals("Slitting", true) -> {

                val intent = Intent(this, SlittingActivity::class.java)

                intent.putExtra(Constants.LocationId, selectedLocationId)
                intent.putExtra(Constants.LocationName, selectedLocationName)

                intent.putExtra("PROCESS_NAME", selectedProcessName)
                intent.putExtra("MACHINE_NAME", selectedMachineName)

                startActivity(intent)
            }

            selectedProcessName.equals("Pickling", true) -> {

                val intent = Intent(this, PicklingActivity::class.java)

                intent.putExtra(Constants.LocationId, selectedLocationId)
                intent.putExtra(Constants.LocationName, selectedLocationName)
                intent.putExtra("PROCESS_NAME", selectedProcessName)
                intent.putExtra("MACHINE_NAME", selectedMachineName)

                startActivity(intent)
            }
            selectedProcessName.equals("CRFH", true) -> {

                val intent = Intent(this, CRMActivity::class.java)

                intent.putExtra(Constants.LocationId, selectedLocationId)
                intent.putExtra(Constants.LocationName, selectedLocationName)
                intent.putExtra("PROCESS_NAME", selectedProcessName)
                intent.putExtra("MACHINE_NAME", selectedMachineName)

                startActivity(intent)
            }

            else -> {
                Toasty.info(this, "Select Valid Process").show()
            }
        }
    }
}