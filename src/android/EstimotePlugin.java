package org.apache.cordova.estimote;

import java.util.ArrayList;
import java.util.List;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import android.util.Log;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.annotation.TargetApi;

import com.estimote.sdk.Region;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class EstimotePlugin extends CordovaPlugin 
{	

	private static final String ESTIMOTE_PROXIMITY_UUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
	private static final Region ALL_ESTIMOTE_BEACONS = new Region("regionId", ESTIMOTE_PROXIMITY_UUID, null, null);
  
	private static final String LOG_TAG					= "EstimotePlugin";
	
	private static final String ACTION_START_RANGING	= "startRanging";

	/**
	 * Callback context for device ranging actions.
	 */
	private CallbackContext rangingCallback;
	
	/**
	 * Is set to true when a ranging process is cancelled or a new one is started when
	 * there is a ranging process still in progress (cancels the old one).
	 */
	private boolean wasRangingCancelled;
	
	
	/**
	 * Initialize the Plugin, Cordova handles this.
	 * 
	 * @param cordova	Used to get register Handler with the Context accessible from this interface 
	 * @param view		Passed straight to super's initialization.
	 */
	public void initialize(CordovaInterface cordova, CordovaWebView view)
	{
		super.initialize(cordova, view);	
		wasRangingCancelled = false;
	}

	/**
	 * Executes the given action.
	 * 
	 * @param action		The action to execute.
	 * @param args			Potential arguments.
	 * @param callbackCtx	Babby call home.
	 */
	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackCtx)
	{	
		if(ACTION_START_RANGING.equals(action))
		{
			startRanging(args, callbackCtx);
		}
		else
		{
			Log.e(LOG_TAG, "Invalid Action[" + action + "]");
			callbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION));
		}
		
		return true;
	}
	
	private void startRanging(JSONArray args, final CallbackContext callbackCtx)
	{
		Log.d(LOG_TAG, "startRanging-method called");
		rangingCallback = callbackCtx;
		try
		{
			BeaconManager beaconManager = new BeaconManager(cordova.getActivity().getBaseContext());
			beaconManager.setRangingListener(new BeaconManager.RangingListener() {
				@Override
				public void onBeaconsDiscovered(Region region, List<Beacon> beacons) {
					Log.d(LOG_TAG, "Ranged beacons: " + beacons);
					for(Beacon b: beacons) {
						try {						
							String name = b.getName();
							String address = b.getMacAddress();
							String proximityUUID = b.getProximityUUID();
							JSONObject device = new JSONObject();
							device.put("name", name);
							device.put("address", address);
							device.put("proximityUUID", proximityUUID);
							
							// Send one device at a time, keeping callback to be used again
							if(rangingCallback != null) {
								PluginResult result = new PluginResult(PluginResult.Status.OK, device);
								result.setKeepCallback(true);
								rangingCallback.sendPluginResult(result);
							} else {
								Log.e(LOG_TAG, "CallbackContext for discovery doesn't exist.");
							}
						} catch(JSONException e) {
							if(rangingCallback != null) {
								EstimotePlugin.this.error(callbackCtx,
									e.getMessage(),
									BluetoothError.ERR_UNKNOWN
								);
								rangingCallback = null;
							}
						}
					}
				}
			});		
			
			beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
				@Override
				public void onServiceReady() {
					try {
						beaconManager.startRanging(ALL_ESTIMOTE_BEACONS);
					} catch (Throwable e) {
						Log.e(LOG_TAG, "Cannot start ranging", e);
					}
				}
			});					
		}
		catch(Exception e)
		{
			this.error(callbackCtx, e.getMessage(), BluetoothError.ERR_UNKNOWN);
		}
	}
	
	/**
	 * Send an error to given CallbackContext containing the error code and message.
	 * 
	 * @param ctx	Where to send the error.
	 * @param msg	What seems to be the problem.
	 * @param code	Integer value as a an error "code"
	 */
	private void error(CallbackContext ctx, String msg, int code)
	{
		try
		{
			JSONObject result = new JSONObject();
			result.put("message", msg);
			result.put("code", code);
			
			ctx.error(result);
		}
		catch(Exception e)
		{
			Log.e(LOG_TAG, "Error with... error raising, " + e.getMessage());
		}
	}	
		
}
