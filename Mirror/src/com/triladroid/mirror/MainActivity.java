package com.triladroid.mirror;



import com.google.ads.AdRequest;
import com.google.ads.AdView;




import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.Camera;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.ZoomControls;


public class MainActivity extends Activity {

	private boolean frontcamerapresent;
	PowerManager pm;
	PowerManager.WakeLock wl;
	private Camera mCamera;
	private int currentZoomLevel = 0, maxZoomLevel = 0;
	private boolean stopped = false;
	private Spinner EffectSpinner;
	private CharSequence selected = "nothing";
	private static boolean isinproc = false;


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
		if (!frontcamerapresent)
		{
			
			finish();
			new AlertDialog.Builder(this)
		    .setTitle("No front camera")
		    .setMessage("No front camera found on this device. Application will be closed :(")
		    .setPositiveButton("Okay ", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) { 
		        	finish();
		        }
		     })

		     .show();

		}

		final Button StopButton = (Button) findViewById(R.id.button2);
		StopButton.setBackgroundResource(R.drawable.pause3);
		StopButton.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v) {
				if (!stopped)
				{
					stopped = true;
					StopButton.setBackgroundResource(R.drawable.go);
					mCamera.stopPreview();
				}
				else
				{
					stopped = false;
					StopButton.setBackgroundResource(R.drawable.pause3);
					mCamera.startPreview();
				}
			}
		}
				);
		
		EffectSpinner = (Spinner)findViewById(R.id.effects);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.Effects, android.R.layout.simple_spinner_item); 
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        EffectSpinner.setAdapter(adapter);
        
		EffectSpinner.setOnItemSelectedListener(
				new AdapterView.OnItemSelectedListener() {
				
					public void onItemSelected(AdapterView<?> arg0, View v, int position, long id)
			        {
			           
						
						selected = (CharSequence) arg0.getItemAtPosition(position);
						Log.i("test", "something selected" + selected);
						//not sure
						releaseCamera();
						
						mCamera = getfrontCameraInstance(getApplicationContext(), selected);
				        Camera.Parameters mCameraparams = mCamera.getParameters();
				        
				        Camera.Size pictureSize = getBiggestPictureSize(mCameraparams);
				        Camera.Size previewSize = getBiggestPreviewSize(mCameraparams);
				                
				        FrameLayout rl = (FrameLayout) findViewById(R.id.camera_preview);
				        Display display = getWindowManager().getDefaultDisplay();
				        
				        int dwidth =  display.getWidth();
				        int dheight = display.getHeight();
				        int rheight;
				        Log.i("test", "This is DISPLAY width " + dwidth + " This is height " + dheight  );
				   
				        Log.i("test", "This is PREVIEW WIDTH  " + previewSize.width + " This is DISPLAY WIDTH  " + dwidth  );
				        
				        
				        if (previewSize.height < dwidth || previewSize.width < dheight)
				        {
				        	 double piccoef = 1.0*pictureSize.width/pictureSize.height;
				             rheight = (int) (dwidth*piccoef);
				             Log.i("test", "2 This is width " + dwidth + " This is height " + rheight  );	
				        	
				        }
				        
				        else
				        {
				        	double piccoef = 1.0*previewSize.width/previewSize.height;
				            rheight = (int) (dwidth*piccoef);
				            Log.i("test", "2 This is width " + dwidth + " This is height " + rheight  );
				        	
				        }
				        
				        rl.getLayoutParams().height = rheight;
				        rl.getLayoutParams().width = dwidth;
				        
				        //rl.getLayoutParams().height = pictureSize.width;
				        //rl.getLayoutParams().width = pictureSize.height;
				        
				        CameraPreview mPreview = new CameraPreview(getApplicationContext(), mCamera);
				        //FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
				        rl.removeAllViews();
				        rl.addView(mPreview);
						//not sure
					
			        }

			        public void onNothingSelected(AdapterView<?> arg0)
			        {
			            Log.v("test", "nothing selected");
			        }
				
				});
        

		AdView ad = (AdView) findViewById(R.id.adView);
        ad.loadAd(new AdRequest());
        
   

	}

	@Override
	protected void onStart() {
		super.onStart();
		if (frontcamerapresent)
		{

//			pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//			wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
//			wl.acquire();


		}
		else
		{
			new AlertDialog.Builder(this)
		    .setTitle("No front camera")
		    .setMessage("No front camera found on this device. Application will be closed :(")
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
        mCamera = getfrontCameraInstance(getApplicationContext(), selected);
        Camera.Parameters mCameraparams = mCamera.getParameters();
        
        Camera.Size pictureSize = getBiggestPictureSize(mCameraparams);
        Camera.Size previewSize = getBiggestPreviewSize(mCameraparams);
                
        FrameLayout rl = (FrameLayout) findViewById(R.id.camera_preview);
        Display display = getWindowManager().getDefaultDisplay();
        
        int dwidth =  display.getWidth();
        int dheight = display.getHeight();
        int rheight;
        Log.i("test", "This is DISPLAY width " + dwidth + " This is height " + dheight  );
   
        Log.i("test", "This is PREVIEW WIDTH  " + previewSize.width + " This is DISPLAY WIDTH  " + dwidth  );
        
        
        if (previewSize.height < dwidth || previewSize.width < dheight)
        {
        	 double piccoef = 1.0*pictureSize.width/pictureSize.height;
             rheight = (int) (dwidth*piccoef);
             Log.i("test", "2 This is width " + dwidth + " This is height " + rheight  );	
        	
        }
        
        else
        {
        	double piccoef = 1.0*previewSize.width/previewSize.height;
            rheight = (int) (dwidth*piccoef);
            Log.i("test", "2 This is width " + dwidth + " This is height " + rheight  );
        	
        }
        
        rl.getLayoutParams().height = rheight;
        rl.getLayoutParams().width = dwidth;
        
        //rl.getLayoutParams().height = pictureSize.width;
        //rl.getLayoutParams().width = pictureSize.height;
        
        CameraPreview mPreview = new CameraPreview(this, mCamera);
        //FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        rl.removeAllViews();
        rl.addView(mPreview);
        
//        final Button PhotoButton = (Button) findViewById(R.id.button1);
//        PhotoButton.setBackgroundResource(R.drawable.pause2);
//        PhotoButton.setOnClickListener(new OnClickListener()
//		{
//
//			@Override
//			public void onClick(View v) {
//				
//				if (! isinproc) {
//		 			isinproc = true;
//		 			mCamera.takePicture(null, null, mPicture);
//
//		 		}	
//				
//			}
//		}
//				);
        
//        ZoomControls zoomControls = (ZoomControls) findViewById(R.id.CAMERA_ZOOM_CONTROLS);
//
//    	Camera.Parameters params = mCamera.getParameters();
//    	
//        if(params.isZoomSupported()){    
//        maxZoomLevel = params.getMaxZoom();
//
//        zoomControls.setIsZoomInEnabled(true);
//            zoomControls.setIsZoomOutEnabled(true);
//
//            zoomControls.setOnZoomInClickListener(new OnClickListener(){
//            	@Override
//            	public void onClick(View v){
//            		Log.i("Mirror", "zoom level" + currentZoomLevel );
//                        if(currentZoomLevel < 100){
//                            currentZoomLevel++;
//                            //mCamera.stopPreview();
//                            mCamera.startSmoothZoom(currentZoomLevel);
//                            //mCamera.startPreview();
//                        }
//                }
//
//				
//            });
//
//        zoomControls.setOnZoomOutClickListener(new OnClickListener(){
//                public void onClick(View v){
//                        if(currentZoomLevel > 0){
//                            currentZoomLevel--;
//                            mCamera.startSmoothZoom(currentZoomLevel);
//                        }
//                }
//            });    
//       }
//       else
//         zoomControls.setVisibility(View.GONE);
//        
//        
//         
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
//	

	public static Camera getfrontCameraInstance(Context context, CharSequence selected){
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
        parameters.setRotation(270);
        
        //CharSequence negativeeffect = (CharSequence) "Negative effect";
        //Log.i("test", "this is sequence" + negativeeffect);
        Log.i("test", "this is selected" + selected);
        if (selected.toString().contentEquals("Negative effect"))
        {
        Log.i("test", "Negative effect");
        parameters.setColorEffect(Camera.Parameters.EFFECT_NEGATIVE);
        }
        if (selected.toString().contentEquals("Sepia effect"))
        {
        Log.i("test", "Sepia effect");
        parameters.setColorEffect(Camera.Parameters.EFFECT_SEPIA);
        }
        if (selected.toString().contentEquals("Aqua effect"))
        {
        Log.i("test", "Aqua effect");
        parameters.setColorEffect(Camera.Parameters.EFFECT_AQUA);
        }
        if (selected.toString().contentEquals("Mono effect"))
        {
        Log.i("test", "Mono effect");
        parameters.setColorEffect(Camera.Parameters.EFFECT_MONO);
        }
        if (selected.toString().contentEquals("Posterize effect"))
        {
        Log.i("test", "Posterize effect");
        parameters.setColorEffect(Camera.Parameters.EFFECT_POSTERIZE);
        }
        if (selected.toString().contentEquals("Solarize effect"))
        {
        Log.i("test", "Solarize effect");
        parameters.setColorEffect(Camera.Parameters.EFFECT_SOLARIZE);
        }
        // doesn;t work, don't know why
        if (selected.toString().contentEquals("Whiteboard effect"))
        {
        Log.i("test", "Whiteboard effect");
        parameters.setColorEffect(Camera.Parameters.EFFECT_WHITEBOARD);
        }
        if (selected.toString().contentEquals("Blackboard effect"))
        {
        Log.i("test", "Blackboard effect");
        parameters.setColorEffect(Camera.Parameters.EFFECT_BLACKBOARD);
        }
        
        
        
        else
        {
        Log.i("test", "IF DIDNT WORK");
        }
        //List<String> EffectsList = parameters.getSupportedColorEffects();
        
        
        c.setParameters(parameters);
        return c; // returns null if camera is unavailable
    }

	/** Check if this device has a camera */
	private boolean checkCameraHardware(Context context) {
	    if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)){
	        // this device has a camera
	    	Log.i("test", "FRONT TRUE");
	    	return true;
	        
	    } else {
	        // no camera on this device
	    	Log.i("test", "FRONT FALSE");
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

        Log.i("test", "This is BIGGEST width " + result.width +"This is  height" + result.height );
        return(result);
       
      }

private static Camera.Size getBiggestPreviewSize(Camera.Parameters parameters) {
    
	Camera.Size result=null;

    for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
    	
    	
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

    Log.i("test", "This is BIGGEST width " + result.width +"This is  height" + result.height );
    return(result);
   
  }

}