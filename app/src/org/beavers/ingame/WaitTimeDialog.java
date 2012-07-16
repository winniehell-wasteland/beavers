package org.beavers.ingame;

import org.beavers.R;
import org.beavers.gameplay.GameActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class WaitTimeDialog {

	private final GameActivity activity;
	private WayPoint waypoint;
	private Dialog dialog;
	private SeekBar seeker;
	private int wait;
	/** default constructor */
	public WaitTimeDialog(final GameActivity activity)
	{
		this.activity=activity;
		createWaitDialog();
	}

	private void createWaitDialog(){
		
		final LayoutInflater inflater= LayoutInflater.from(activity);
		final View seekView = inflater.inflate(R.layout.dialog, null);
		seeker=(SeekBar)seekView.findViewById(R.id.seekBar1);
	
		final TextView text=(TextView)seekView.findViewById(R.id.textView1);
		final AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
		dialog.setView(seekView);
		dialog.setTitle("Wartezeit");
		dialog.setCancelable(true);
			    //OK Button
		 dialog.setPositiveButton(R.string.button_ok,new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(final DialogInterface dialog, final int which) {
						if(waypoint!=null){
						//	waypoint.setWait(wait);
						}
						dialog.dismiss();
					}
				});
				//Abort Button
			    dialog.setNegativeButton(R.string.button_cancel,new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(final DialogInterface dialog, final int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
					}
				});
    
			    text.setText("Wartezeit: "+seeker.getProgress()/10);
			    	seeker.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
						
						@Override
						public void onStopTrackingTouch(final SeekBar seekBar) {
							// TODO Auto-generated method stub
							
						}
						
						@Override
						public void onStartTrackingTouch(final SeekBar seekBar) {
							// TODO Auto-generated method stub
							
						}
						
						@Override
						public void onProgressChanged(final SeekBar seekBar, final int progress,
								final boolean fromUser) {
							wait=seeker.getProgress();
							text.setText("Wartezeit: "+seeker.getProgress()/10);
						}
					});
			    
			   
			    	
			    	dialog.create();
			    	dialog.show();
			    	
			    }
	
		public void show(/*final WayPoint waypoint*/){
			waypoint=waypoint;
			//waypoint.setWaiting(false);
			seeker.setProgress(waypoint.getWait());
			dialog.show();
		}
			   
	}

	
	
	
