package org.xresource.core.response;

/**
 * Functional interface representing a transformer for resource responses.
 * <p>
 * Implementations can modify or enhance the response object for a given
 * resource
 * before it is returned from the API layer.
 * <p>
 * The generic type {@code T} represents the type of the response object.
 * 
 * @param <T> the type of the response object to transform
 * 
 * @author soumya
 * @since xresource-core 0.1
 */
@FunctionalInterface
public interface XResponseTransformer<T> {

    /**
     * Transforms the given response object for the specified resource.
     * <p>
     * This method can be used to apply modifications, filtering, or enrichment
     * of the response before it is sent to the client.
     * 
     * @param response     the original response object
     * @param resourceName the canonical name of the resource the response is for
     * @return the transformed response object
     */
    T tranform(T response, String resourceName);
}
