package gcu.mpd.mpdapp;
/*   Nikolaj Alexander Gilstrøm - S1630425  */

import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RoadworkRetriever extends AsyncTask<RoadworkType, Void, List<RoadworkRecord>> {
    private Map callee;
    private RoadworkType type;

    RoadworkRetriever(Map callee, RoadworkType type) {
        this.callee = callee;
        this.type = type;
    }


    @Override
    protected List<RoadworkRecord> doInBackground(RoadworkType... roadworkTypes) {

        // determine url by RoadworkType passed
        URL url = getUrl(type);

        // Create a stream based on chosen URL
        InputStream stream = getStream(url);


        // Parse stream data
        List<RoadworkRecord> records = getParsedStreamData(stream);

        // Return data
        return records;
    }

    private URL getUrl(RoadworkType type) {
        URL url;
        try {
            switch (type) {
                case INCIDENT:
                    url = new URL("https://trafficscotland.org/rss/feeds/currentincidents.aspx");
                    break;
                case PLANNED:
                    url = new URL("https://trafficscotland.org/rss/feeds/plannedroadworks.aspx");
                    break;
                case CURRENT:
                    url = new URL("https://trafficscotland.org/rss/feeds/roadworks.aspx");
                    break;
                default:
                    url = new URL("https://trafficscotland.org/rss/feeds/roadworks.aspx");
            }

            return url;
        } catch (Exception e) {
            System.out.println("Failed in roadwork retriever; failed to determine url from enum passed");
            e.printStackTrace();
            return null;
        }
    }

    private InputStream getStream(URL url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(500000);
            connection.setConnectTimeout(200000);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.connect();
            InputStream stream = connection.getInputStream();
            return stream;
        } catch (Exception e) {
            System.out.println("Failed in roadwork retriever; failed to establish stream connection");
            e.printStackTrace();
            return null;
        }
    }

    private List<RoadworkRecord> getParsedStreamData(InputStream stream) {
        List<RoadworkRecord> records = new ArrayList<>();

        try {
            XmlPullParserFactory xmlFactoryObject = XmlPullParserFactory.newInstance();
            XmlPullParser XMLParser = xmlFactoryObject.newPullParser();
            XMLParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            XMLParser.setInput(stream, null);

            int event = XMLParser.getEventType();
            String text = null;
            RoadworkRecord record = new RoadworkRecord();

            while(event != XmlPullParser.END_DOCUMENT) {
                String name = XMLParser.getName();

                switch(event) {
                    case XmlPullParser.TEXT:
                        text = XMLParser.getText();
                        break;
                    case XmlPullParser.END_TAG:
                        switch(name) {
                            case "title":
                                record.setTitle(text);
                                break;
                            case "description":
                                // Get duration
                                record.setDuration(getDuration(text));

                                // Clean up description (remove br, and 00:00 and insert line breaks)
                                String temp = text.replace("<br />", "\n");
                                temp = temp.replace("00:00", "");
                                temp = temp.replace(" - ", "\n");

                                // Change terminology (Start date -> From, End date -> to)
                                temp = temp.replace("Start Date", "From");
                                temp = temp.replace("End Date", "To");

                                record.setDescription(temp);
                                break;
                            case "georss:point":
                                // parse string to latLng
                                String[] location_arr = text.split(" ");
                                double lat = Double.parseDouble(location_arr[0]);
                                double lng = Double.parseDouble(location_arr[1]);
                                LatLng location = new LatLng(lat, lng);

                                record.setLocation(location);
                                break;
                            case "link":
                                record.setUrl(text);
                                break;

                        }
                        break;
                    default: break;
                }

                if(record.isValid()) {
                    records.add(record);
                    record = new RoadworkRecord();
                }

                event = XMLParser.next();
            }

            // close stream
            stream.close();


        } catch (Exception e) {
            System.out.println("Failed in roadwork retriever; failed to parse XML stream");
            e.printStackTrace();
            return null;
        }

        return records;
    }

    @Override
    protected void onPostExecute(List<RoadworkRecord> records) {
        callee.updateMarkers(records);
    }

    private int getDuration(String description) {
        String tempDate = description.replace("Start Date:", "");
        tempDate = tempDate.replace(" - 00:00<br />", " ");
        tempDate = tempDate.replace("End Date: ", "");
        tempDate = tempDate.replace("Monday,", "");
        tempDate = tempDate.replace("Tuesday,", "");
        tempDate = tempDate.replace("Wednesday,", "");
        tempDate = tempDate.replace("Thursday,", "");
        tempDate = tempDate.replace("Friday,", "");
        tempDate = tempDate.replace("Saturday,", "");
        tempDate = tempDate.replace("Sunday,", "");
        tempDate = tempDate.trim();
        tempDate = tempDate.replace("  ", " ");

        String[] dateArr = tempDate.split(" ");

        String endDay = dateArr[3];
        String endMonth = dateArr[4];
        String endYear = dateArr[5];

        String fullEndDate = endDay + "/" + endMonth + "/" + endYear;

        String startDay = dateArr[0];
        String startMonth = dateArr[1];
        String startYear = dateArr[2];

        String fullStartDate = startDay + "/" + startMonth + "/" + startYear;
        try {
            Date start = new SimpleDateFormat("dd/MMMM/yyyy", Locale.US).parse(fullStartDate);
            Date end = new SimpleDateFormat("dd/MMMM/yyyy", Locale.US).parse(fullEndDate);

            long differenceInMilliseconds = Math.abs(end.getTime() - start.getTime());
            final int ONE_DAY = 86400000;
            int differenceInDays = (int) Math.floor((int) (differenceInMilliseconds / ONE_DAY));

            return differenceInDays;

        } catch (Exception e) {
            System.out.println("Failed to parse dates");
            return 0;
        }
    }
}
