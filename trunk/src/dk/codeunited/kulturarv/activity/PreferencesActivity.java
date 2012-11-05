package dk.codeunited.kulturarv.activity;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.widget.Toast;
import dk.codeunited.kulturarv.R;

/**
 * @author Kostas Rutkauskas
 */
public class PreferencesActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(getString(R.string.preferences));
		addPreferencesFromResource(R.layout.preferences);

		refreshMinValuePreferenceSummary();
		Preference minValuePreference = findPreference(getString(R.string.pref_minimum_value));
		minValuePreference
				.setOnPreferenceChangeListener(minValueChangedListener);
	}

	private void refreshMinValuePreferenceSummary() {
		Preference minValuePreference = findPreference(getString(R.string.pref_minimum_value));
		minValuePreference
				.setSummary(getString(R.string.pref_minimum_value_summary));
		this.onContentChanged();
	}

	Preference.OnPreferenceChangeListener minValueChangedListener = new OnPreferenceChangeListener() {

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			boolean approve = true;
			try {
				int newValueInt = Integer.parseInt(newValue.toString());
				if (newValueInt < 1 || newValueInt > 9) {
					throw new Exception(
							"Min building value is outside boundaries");
				}
			} catch (Exception e) {
				approve = false;
				Toast.makeText(PreferencesActivity.this,
						getString(R.string.building_value_between),
						Toast.LENGTH_LONG).show();
			}
			return approve;
		}
	};
}