package org.xresource.internal.intent.core.parser.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Metadata for a join clause in an Intent definition.
 * 
 * @author soumya
 * @since xresource-core 0.2
 */
@Data
@Getter
@Setter
public class JoinMeta {
    private String resource;
    private String alias;
    private String on;
    private List<JoinFilterMeta> filters;
    private boolean autoChain;
    private String parent;
}
