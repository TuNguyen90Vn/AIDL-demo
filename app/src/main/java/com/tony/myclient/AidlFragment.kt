package com.tony.myclient

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.os.Process
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.tony.myclient.databinding.FragmentAidlBinding
import com.tony.myserver.IIPCExample

class AidlFragment : Fragment(), ServiceConnection, View.OnClickListener {

    private var _binding: FragmentAidlBinding? = null
    private val binding get() = _binding!!
    private var iRemoteService: IIPCExample? = null
    private var connected = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAidlBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.btnConnect.setOnClickListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(v: View?) {
        connected = if (connected) {
            disconnectToRemoteService()
            binding.txtServerPid.text = ""
            binding.txtServerConnectionCount.text = ""
            binding.btnConnect.text = getString(R.string.connect)
            binding.linearLayoutClientInfo.visibility = View.INVISIBLE
            false
        } else {
            connectToRemoteService()
            binding.linearLayoutClientInfo.visibility = View.VISIBLE
            binding.btnConnect.text = getString(R.string.disconnect)
            true
        }
    }

    private fun connectToRemoteService() {
        val intent = Intent().apply {
            action = "com.tony.myserver.AIDL"
        }
        val pack = IIPCExample::class.java.`package`
        pack?.let {
            intent.setPackage(pack.name)
            activity?.applicationContext?.bindService(
                intent, this, Context.BIND_AUTO_CREATE
            )
        }
    }

    private fun disconnectToRemoteService() {
        if (connected) {
            activity?.applicationContext?.unbindService(this)
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        // Gets an instance of the AIDL interface named IIPCExample,
        // which we can use to call on the service
        Log.d("TONY", "onServiceConnected: ")
        iRemoteService = IIPCExample.Stub.asInterface(service)
        binding.txtServerPid.text = iRemoteService?.pid.toString()
        binding.txtServerConnectionCount.text = iRemoteService?.connectionCount.toString()
        iRemoteService?.setDisplayedValue(
            context?.packageName,
            Process.myPid(),
            binding.edtClientData.text.toString()
        )
        connected = true
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        Toast.makeText(context, "IPC server has disconnected unexpectedly", Toast.LENGTH_LONG)
            .show()
        iRemoteService = null
        connected = false
    }

    private fun convertImplicitIntentToExplicitIntent(implicitIntent: Intent?): Intent? {
        val pm: PackageManager = requireActivity().packageManager
        val resolveInfo = pm.queryIntentServices(implicitIntent!!, 0)
        if (resolveInfo == null || resolveInfo.size != 1) {
            return null
        }

        val serviceInfo = resolveInfo[0]
        val component =
            ComponentName(serviceInfo.serviceInfo.packageName, serviceInfo.serviceInfo.name)
        val explicitIntent = Intent(implicitIntent).apply {
            this.component = component
        }
        return explicitIntent
    }
}