/*
	(c) wintermadnezz (2012)

	This file is part of the game Battle Beavers.

	Battle Beavers is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	Battle Beavers is distributed in the hope that it will be fun,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with Battle Beavers. If not, see <http://www.gnu.org/licenses/>.
*/

package de.winniehell.battlebeavers.ingame;

import de.winniehell.battlebeavers.R;
import de.winniehell.battlebeavers.gameplay.GameActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class WaitTimeDialog {

	private final GameActivity activity;
	private WayPoint waypoint;
	private SeekBar seeker;
	private int wait;
	/** default constructor */
	public WaitTimeDialog(final GameActivity activity, final WayPoint wp)
	{
		this.activity=activity;
		waypoint=wp;
		createWaitDialog();
	}

	private void createWaitDialog(){
		
		final LayoutInflater inflater= LayoutInflater.from(activity);
		final View seekView = inflater.inflate(R.layout.dialog, null);
		seeker=(SeekBar)seekView.findViewById(R.id.seekBar1);
		seeker.setProgress(waypoint.getWait()*10);
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
							waypoint.setWait(wait);
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
							wait=seeker.getProgress()/10;
							text.setText("Wartezeit: "+seeker.getProgress()/10+" Sekunden");
						}
					});
			    
			   
			    	
			    	dialog.create();
			    	dialog.show();
			    	
			    }
			   
	}

	
	
	
