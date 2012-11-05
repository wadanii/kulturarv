package dk.codeunited.kulturarv.kulturarvClient.soap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.ksoap2.HeaderProperty;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.content.Context;
import dk.codeunited.kulturarv.KulturarvApplication;
import dk.codeunited.kulturarv.R;
import dk.codeunited.kulturarv.kulturarvClient.KulturarvService;
import dk.codeunited.kulturarv.kulturarvClient.util.jhlabs.Point2D;

/**
 * Client that knows how to use Kulturarv SOAP service. This client should not
 * be accessed directly. Use {@link KulturarvService} instead.
 * 
 * @author Maksim Sorokin
 */
public class KulturarvClient {

	private static final String SOAP_ACTION = "https://www.kulturarv.dk/fbb-ws/services/FbbWebService";
	private static final String METHOD_NAME_FIND_BUILDINGS = "findBygningerIRektangel";
	private static final String METHOD_NAME_EXPORT_BUILDINGS_SIMPLE_XMLS = "eksporterBygningerSimpelXml";
	private static final String METHOD_NAME_EXPORT_BUILDINGS_XMLS = "eksporterBygningerXml";
	private static final String NAMESPACE = "https://www.kulturarv.dk/fbb-ws/services/";
	private static final String URL = "https://www.kulturarv.dk/fbb-ws/services/FbbService?wsdl";

	/**
	 * Gets ids of buildings that are in specified sqare.
	 * 
	 * @param minLocation
	 *            min location presents one corner of "nearby" square
	 * @param maxLocation
	 *            max location presents the opposite corner of
	 *            {@code minLocation}
	 * @return ids of buildings that are in specified square
	 * @throws Exception
	 *             exception that may be thrown during SOAP request to Kulturarv
	 */
	public List<Long> getBuildingsIds(Point2D.Double minLocation,
			Point2D.Double maxLocation) throws Exception {
		SoapObject request = new SoapObject(NAMESPACE,
				METHOD_NAME_FIND_BUILDINGS);
		request.addProperty("epsgIgnore", "false");
		request.addProperty("x1", minLocation.x);
		request.addProperty("y1", minLocation.y);
		request.addProperty("x2", maxLocation.x);
		request.addProperty("y2", maxLocation.y);

		SoapSerializationEnvelope envelope = makeCallAndGetEnvelope(request);

		List<Long> buildingsIds = new ArrayList<Long>();

		@SuppressWarnings("rawtypes")
		Vector result = (Vector) envelope.getResponse();
		@SuppressWarnings("rawtypes")
		Iterator it = result.iterator();
		while (it.hasNext()) {
			buildingsIds.add(Long.parseLong(it.next() + ""));
		}

		return buildingsIds;
	}

	/**
	 * Gets "simple" xml information for provided buildings ids. The information
	 * contains only couple fields.
	 * 
	 * @param buildingsIds
	 *            buildings ids to get information for
	 * @return xml information for provided buildings ids
	 * @throws Exception
	 *             that can be thrown during SOAP request to Kulturarv
	 */
	public List<String> getSimpleBuildingXmls(List<Long> buildingsIds)
			throws Exception {
		return getBuildingsXmls(buildingsIds, true);
	}

	/**
	 * Gets "full" xml information for provided buldings ids. The xml contains
	 * all stored information about the building.
	 * 
	 * @param buildingsIds
	 *            buildings ids to get information for
	 * @return xml information for provided buildings ids
	 * @throws Exception
	 */
	public List<String> getFullBuildingsXmls(List<Long> buildingsIds)
			throws Exception {
		return getBuildingsXmls(buildingsIds, false);
	}

	/**
	 * Getting xmls for provided buildings ids. This method is actually capable
	 * of quirying two different kulturarv methods, depending on the
	 * {@code isSimple} parameter. When {@code isSimple} is {@code true}, the
	 * "simple" information about the buldings is acquired, which contains only
	 * couple of fields. When {@code isSimple} is {@code false}, "full"
	 * buildings information is received. These two methods have the same
	 * signature.
	 * 
	 * @param buildingsIds
	 *            ids of buildings to get information
	 * @param isSimple
	 *            whereas to get "simple" or "full" information about the
	 *            buildings.
	 * @return xml information for specified building ids
	 * @throws Exception
	 *             that can be thrown during SOAP request to Kulturarv
	 */
	private List<String> getBuildingsXmls(List<Long> buildingsIds,
			boolean isSimple) throws Exception {
		String methodName = null;
		if (isSimple) {
			methodName = METHOD_NAME_EXPORT_BUILDINGS_SIMPLE_XMLS;
		} else {
			methodName = METHOD_NAME_EXPORT_BUILDINGS_XMLS;
		}

		SoapObject request = new SoapObject(NAMESPACE, methodName);

		LongListRequest longListRequest = new LongListRequest(buildingsIds);

		PropertyInfo pi = new PropertyInfo();
		pi.setName("id");
		pi.setValue(longListRequest);
		request.addProperty(pi);

		SoapSerializationEnvelope envelope = makeCallAndGetEnvelope(request);

		List<String> xmls = new ArrayList<String>();

		if (envelope.getResponse() instanceof Vector) {
			@SuppressWarnings("rawtypes")
			Vector result = (Vector) envelope.getResponse();
			@SuppressWarnings("rawtypes")
			Iterator it = result.iterator();
			while (it.hasNext()) {
				xmls.add(it.next() + "");
			}
		} else {
			// There is a bit of magic here. When ksoap2 receives a list with
			// multiple objects as a response, it wraps it into Vector.
			// Otherwise, if a list contains only one element, then it is just
			// soap primitive.
			xmls.add(envelope.getResponse().toString());
		}

		return xmls;
	}

	/**
	 * Makes a call to the provided request and returns resulting envelope back.
	 * 
	 * @param request
	 *            soap request object
	 * @return resulting envelop
	 * @throws Exception
	 *             exception that could be thrown during the SOAP request
	 */
	private SoapSerializationEnvelope makeCallAndGetEnvelope(SoapObject request)
			throws Exception {
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);
		envelope.setOutputSoapObject(request);

		// enable double type serialization support
		MarshalDouble mdDouble = new MarshalDouble();
		mdDouble.register(envelope);

		envelope.dotNet = true;
		envelope.encodingStyle = SoapEnvelope.XSD;
		envelope.implicitTypes = true;

		HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);

		Context ctx = KulturarvApplication.getAppContext();
		List<HeaderProperty> headerList = new ArrayList<HeaderProperty>();
		headerList.add(new HeaderProperty("Authorization", "Basic "
				+ org.kobjects.base64.Base64.encode(String.format("%s:%s",
						ctx.getString(R.string.kulturarv_user),
						ctx.getString(R.string.kulturarv_pass)).getBytes())));

		androidHttpTransport.call(SOAP_ACTION, envelope, headerList);

		return envelope;
	}
}