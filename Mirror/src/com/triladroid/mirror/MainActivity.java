package com.triladroid.mirror;



import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.util.Log;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

public class MainActivity extends Activity {
	
	private boolean frontcamerapresent;
	PowerManager pm;
	PowerManager.WakeLock wl;
	private Camera mCamera;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
		setContentView(R.layout.activity_main);
		
		WindowManager.LayoutParams layout = getWindow().getAttributes();
		layout.screenBrightness = 1F;
		getWindow().setAttributes(layout);
        
		
		frontcamerapresent = checkCameraHardware(getApplicationContext());
		
		
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		if (frontcamerapresent)
		{
			
			pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
			wl.acquire();
		
		
		}
		else
		{
			new AlertDialog.Builder(this)
		    .setTitle("No front camera")
		    .setMessage("No front camera fuond on this device. Application will be closed :(")
		    .setPositiveButton("Okay ", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) { 
		        	finish();
		        }
		     })
		    
		     .show();
			
		}
		
	}
	
	
	@Override
    protected void onResume() {
        super.onResume();
        mCamera = getfrontCameraInstance();
        Camera.Parameters mCameraparams = mCamera.getParameters();
        Camera.Size pictureSize = getBiggestPictureSize(mCameraparams);
        //mCameraparams.setPictureSize(pictureSize.width, pictureSize.height);
        //mCamera.setParameters(mCameraparams);
        //FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(pictureSize.width, pictureSize.height, 80);
        //preview.setLayoutParams(params);
        
        RelativeLayout rl = (RelativeLayout) findViewById(R.id.layoutid);
        rl.getLayoutParams().height = pictureSize.width;
        rl.getLayoutParams().width = pictureSize.height;
        
        CameraPreview mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.removeAllViews();
        preview.addView(mPreview);
         
	}
	
	@Override
    protected void onPause() {
        super.onPause();
        releaseCamera(); 
        
        // sanity check for null as this is a public method
            if (wl != null) {
                Log.v("Mirror:", "Releasing wakelock");
                try {
                        	wl.release();
                } catch (Throwable th) {
                    // ignoring this exception, probably wakeLock was already released
                }
            	} else {
            		// should never happen during normal workflow
            		Log.e("Mirror", "Wakelock reference is null");
            }
        }
    

	private void releaseCamera(){
        if (mCamera != null){
        	
        	mCamera.stopPreview(); 
        	//mCamera.setPreviewCallback(null);
        	//mPreview.getHolder().removeCallback(mPreview);
        	mCamera.lock();
           	mCamera.release();        // release the camera
            mCamera = null;
        }
    }

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main, menu);
//		return true;
//	}
	
	
	public static Camera getfrontCameraInstance(){
	        Camera c = null;
	        int cameraCount = 0;
	        cameraCount = Camera.getNumberOfCameras();
	        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
	        
	        for ( int camIdx = 0; camIdx < cameraCount; camIdx++ ) {
	            Camera.getCameraInfo(camIdx, cameraInfo);
	            if ( cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT  ) {
	                try {
	                    c = Camera.open(camIdx);
	                } catch (RuntimeException e) {
	                    Log.e("1", "Camera failed to open: " + e.getLocalizedMessage());
	                }
	            }
	        }
	        
	        Camera.Parameters parameters = c.getParameters();
	        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
	        //parameters.setColorEffect(Camera.Parameters.EFFECT_NEGATIVE);
	        
	        c.setParameters(parameters);
	        
	        return c; // returns null if camera is unavailable
	    }

	
	/** Check if this device has a camera */
	private boolean checkCameraHardware(Context context) {
	    if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)){
	        // this device has a camera
	        return true;
	    } else {
	        // no camera on this device
	        return false;
	    }
	}
	
private static Camera.Size getBiggestPictureSize(Camera.Parameters parameters) {
        
    	Camera.Size result=null;

        for (Camera.Size size : parameters.getSupportedPictureSizes()) {
        	
        	
        	if (result == null) {
            result=size;
          }
          else {
            int resultArea=result.width * result.height;
            int newArea=size.width * size.height;

            //Log.i("test", "This is resultArea " + resultArea );
            //Log.i("test", "This is newArea " + newArea );
            
            if (newArea >= resultArea) {
              result=size;
              Log.i("test", "This is width" + result.width);
            }
          }
        }

        Log.i("test", "This is width " + result.width +"This is  height" + result.height );
        return(result);
       
      }

}