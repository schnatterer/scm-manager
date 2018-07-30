package sonia.scm.it;

import com.google.common.io.Resources;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;

public class RestUtil {
  public static final String BASE_URL = "http://localhost:8081/scm/";
  public static final String REST_BASE_URL = BASE_URL.concat("api/rest/v2/");

  public static URI createResourceUrl(String url)
  {
    return URI.create(REST_BASE_URL).resolve(url);
  }

  public static String readJson(String jsonFileName) throws IOException {
    URL url = Resources.getResource(jsonFileName);
    return Resources.toString(url, Charset.forName("UTF-8"));
  }
}
