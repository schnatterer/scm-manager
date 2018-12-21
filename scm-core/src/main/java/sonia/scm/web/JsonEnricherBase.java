package sonia.scm.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Map;

public abstract class JsonEnricherBase implements JsonEnricher {

  private final ObjectMapper objectMapper;

  protected JsonEnricherBase(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  protected boolean resultHasMediaType(String mediaType, JsonEnricherContext context) {
    return mediaType.equalsIgnoreCase(context.getResponseMediaType().toString());
  }

  protected JsonNode value(Object object) {
    return objectMapper.convertValue(object, JsonNode.class);
  }

  protected ObjectNode createObject() {
    return objectMapper.createObjectNode();
  }

  protected ObjectNode createObject(Map<String, Object> values) {
    ObjectNode object = createObject();

    values.forEach((key, value) -> object.set(key, value(value)));

    return object;
  }

  protected void addPropertyNode(JsonNode parent, String newKey, JsonNode child) {
    ((ObjectNode) parent).set(newKey, child);
  }

  protected void addArrayNode(JsonNode parent, String propertyKey, List<ObjectNode> objectNodes) {
    ArrayNode arrayNode = objectMapper.createArrayNode();
    arrayNode.addAll(objectNodes);
    addPropertyNode(parent,propertyKey,arrayNode);
  }
}
