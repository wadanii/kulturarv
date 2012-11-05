package dk.codeunited.kulturarv.toast;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import dk.codeunited.kulturarv.R;

/**
 * @author Kostas Rutkauskas
 */
public class ErrorToast extends Toast {

	View layout;

	public ErrorToast(Context context) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layout = inflater.inflate(R.layout.error_toast, null);

		setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		setDuration(Toast.LENGTH_LONG);
		setView(layout);
	}

	public void setErrorText(String text) {
		TextView textView = (TextView) layout.findViewById(R.id.text);
		textView.setText(text);
	}

	public String getErrorText() {
		TextView textView = (TextView) layout.findViewById(R.id.text);
		return textView.getText().toString();
	}
}