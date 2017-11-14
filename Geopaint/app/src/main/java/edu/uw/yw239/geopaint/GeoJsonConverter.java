package edu.uw.yw239.geopaint;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;

import java.util.List;

/**
 * Created by yunwu on 11/13/17.
 */

public class GeoJsonConverter {
    public static String convertToGeoJson(List<Polyline> listOfPolylines) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("{ \"type\": \"FeatureCollection\", " +
                "\"features\": [");

        for (Polyline polyLine : listOfPolylines) {
            strBuilder.append("{ \"type\": \"Feature\", " +
                    "\"geometry\": { \"type\": \"LineString\", \"coordinates\": ");
            List<LatLng> points = polyLine.getPoints();
            strBuilder.append(points.toString());
            strBuilder.append("}, \"properties\": { \"color\" : \"" + polyLine.getColor() + "\"}");
            strBuilder.append("},");
        }

        // delete the extra comma
        strBuilder = strBuilder.deleteCharAt(strBuilder.length() - 1);
        strBuilder.append("]}");
        String str = strBuilder.toString().replace("lat/lng:", "").replace("(", "[").replace(")", "]");

        return str;
    }
}
