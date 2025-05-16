package org.xresource.core.response;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
/**
 * Registry to hold controller-specific response transformers for different
 * types of resource operations. This allows developers to hook into the
 * response lifecycle and apply custom transformation logic just before
 * the response is sent to the client.
 *
 * <p>
 * Each transformer is applied conditionally only if it's present. These
 * transformers operate on `ObjectNode` or `List<ObjectNode>` representations
 * of the resource, enabling mutation, augmentation, or filtering as needed.
 * </p>
 *
 * <p>
 * This pattern provides flexibility while ensuring separation of concerns
 * between business logic and response shaping.
 * </p>
 *
 * @author YourName
 */
public class XResponseTranformersRegistry {
    /**
     * Transformer applied to all records returned from a `findAll` (GET
     * /{resourceName}) operation.
     */
    private XResponseTransformer<List<ObjectNode>> findAllTransformer;

    /**
     * Transformer applied to a single record returned from a `findOne` (GET
     * /{resourceName}/{id}) operation.
     */
    private XResponseTransformer<ObjectNode> findOneTransformer;

    /**
     * Transformer applied to a single record after a successful `createOne` (POST
     * /{resourceName}) operation.
     */
    private XResponseTransformer<ObjectNode> createOneTransformer;

    /**
     * Transformer applied after a successful update of a single record (PUT
     * /{resourceName}/{id}).
     */
    private XResponseTransformer<ObjectNode> updateOneTransformer;

    /**
     * Transformer applied to the JSON schema or form returned from `getJsonForm`
     * (GET /jsonform/{resourceName}).
     */
    private XResponseTransformer<List<Map<String, Object>>> getJsonFormTransformer;

    /**
     * Transformer applied to custom query results (GET
     * /{resourceName}/query/{queryName}).
     */
    private XResponseTransformer<List<ObjectNode>> namedQueryTransformer;

    /**
     * Transformer applied to a specific field access (GET
     * /{resourceName}/{id}/{field}).
     */
    private XResponseTransformer<ArrayNode> fieldAccessTransformerForList;

    /**
     * Transformer applied to a specific field access (GET
     * /{resourceName}/{id}/{field}).
     */
    private XResponseTransformer<ObjectNode> fieldAccessTransformerForOne;

}
