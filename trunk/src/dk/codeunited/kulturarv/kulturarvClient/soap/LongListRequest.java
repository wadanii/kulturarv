package dk.codeunited.kulturarv.kulturarvClient.soap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import org.ksoap2.serialization.KvmSerializable;
import org.ksoap2.serialization.PropertyInfo;

/**
 * Enables support for the list of Longs in the SOAP request in ksoap2.
 * 
 * @author Maksim Sorokin
 */
public class LongListRequest implements KvmSerializable {

	private List<Long> ids;

	public LongListRequest(List<Long> ids) {
		this.ids = ids;
	}

	@Override
	public Object getProperty(int index) {
		return ids.get(index);
	}

	@Override
	public int getPropertyCount() {
		return ids.size();
	}

	@Override
	public void getPropertyInfo(int arg0,
			@SuppressWarnings("rawtypes") Hashtable arg1, PropertyInfo info) {
		info.type = PropertyInfo.OBJECT_TYPE;
		info.name = "ids";
	}

	@Override
	public void setProperty(int arg0, Object arg1) {
		if (arg1 != null) {
			ids = new ArrayList<Long>(Arrays.asList((Long[]) arg1));
		}
	}
}