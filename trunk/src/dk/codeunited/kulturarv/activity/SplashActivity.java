package dk.codeunited.kulturarv.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import dk.codeunited.kulturarv.R;

/**
 * @author Kostas Rutkauskas
 */
public class SplashActivity extends Activity {

	private final int SPLASH_DELAY_LENGHT = 2000;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.splash);

		if (!isNetworkAvailable()) {
			showErrorDialog();
			return;
		}

		startAugmentedDisplayDelayed();
	}

	private void showErrorDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		Dialog d = builder
				.setMessage(R.string.data_connection_required)
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								SplashActivity.this.finish();
							}
						})
				.setNeutralButton(getString(R.string.connection_settings),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								startActivity(new Intent(
										Settings.ACTION_WIFI_SETTINGS));
								SplashActivity.this.finish();
							}
						}).setIcon(android.R.drawable.ic_dialog_alert).create();

		d.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				SplashActivity.this.finish();
			}
		});
		d.show();
	}

	void startAugmentedDisplay() {
		if (!isGPSAvailable()) {
			Toast.makeText(this,
					getString(R.string.enable_gps_for_better_experience),
					Toast.LENGTH_LONG).show();
		}

		Intent i = new Intent(this,
				dk.codeunited.kulturarv.activity.MixView.class);
		startActivity(i);
		SplashActivity.this.finish();
	}

	private void startAugmentedDisplayDelayed() {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				startAugmentedDisplay();
			}
		}, SPLASH_DELAY_LENGHT);
	}

	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null;
	}

	private boolean isGPSAvailable() {
		final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}
}