package org.xresource.internal.intent.core.parser.model;

import lombok.*;

/**
 * Metadata for selected fields in an Intent query.
 * 
 * @author soumya
 * @since xresource-core 0.2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelectAttributeMeta {

    /**
     * Alias of the table/entity in the query (e.g., "a").
     */
    private String alias;

    /**
     * Field name to select (e.g., "assetId").
     */
    private String field;

    /**
     * Optional alias for the field in the final result (e.g., "asset_id").
     */
    private String aliasAs;
}
