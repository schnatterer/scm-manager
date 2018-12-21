package sonia.scm.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

public class JsonEnricherBaseTest {

  private ObjectMapper objectMapper = new ObjectMapper();
  private TestJsonEnricher enricher = new TestJsonEnricher(objectMapper);

  @Test
  public void testResultHasMediaType() {
    JsonEnricherContext context = new JsonEnricherContext(null, MediaType.APPLICATION_JSON_TYPE, null);

    assertThat(enricher.resultHasMediaType(MediaType.APPLICATION_JSON, context)).isTrue();
    assertThat(enricher.resultHasMediaType(MediaType.APPLICATION_XML, context)).isFalse();
  }

  @Test
  public void testResultHasMediaTypeWithCamelCaseMediaType() {
    String mediaType = "application/hitchhikersGuideToTheGalaxy";
    JsonEnricherContext context = new JsonEnricherContext(null, MediaType.valueOf(mediaType), null);

    assertThat(enricher.resultHasMediaType(mediaType, context)).isTrue();
  }

  @Test
  public void testAppendLink() {
    ObjectNode root = objectMapper.createObjectNode();
    ObjectNode links = objectMapper.createObjectNode();
    root.set("_links", links);
    JsonEnricherContext context = new JsonEnricherContext(null, MediaType.APPLICATION_JSON_TYPE, root);
    enricher.enrich(context);

    assertThat(links.get("awesome").get("href").asText()).isEqualTo("/my/awesome/link");
  }

  @Test
  public void shouldAddArray() {
    ObjectNode root = objectMapper.createObjectNode();
    ObjectNode links = objectMapper.createObjectNode();
    root.set("_links", links);
    JsonEnricherContext context = new JsonEnricherContext(null, MediaType.APPLICATION_JSON_TYPE, root);
    enricher.enrich(context);

    JsonNode array = links.get("array");
    assertThat(array).hasSize(2);
    ArrayList<JsonNode> jsonNodes = Lists.newArrayList(array.elements());


    assertThat(jsonNodes.get(0).get("prop").asText()).isEqualTo("v1");
    assertThat(jsonNodes.get(1).get("prop").asText()).isEqualTo("v2");

  }

  private static class TestJsonEnricher extends JsonEnricherBase {

    public TestJsonEnricher(ObjectMapper objectMapper) {
      super(objectMapper);
    }

    @Override
    public void enrich(JsonEnricherContext context) {
      JsonNode gitConfigRefNode = createObject(singletonMap("href", value("/my/awesome/link")));

      JsonNode links = context.getResponseEntity().get("_links");
      addPropertyNode(links, "awesome", gitConfigRefNode);
      HashMap<String, Object> map = Maps.newHashMap();
      map.put("prop","v1");
      ObjectNode objectNode = createObject(map);
      Map<String, Object> map2= Maps.newHashMap();
      map2.put("prop","v2");
      ObjectNode objectNode2 = createObject(map2);
      List<ObjectNode> objectNodes = new ArrayList<>();
      objectNodes.add(objectNode);
      objectNodes.add(objectNode2);
      addArrayNode(links, "array", objectNodes);
    }
  }

}
