package org.xresource.internal.query;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class XQueryMetadata {
    private String name;
    private String whereClause;
    private String[] contextParams;
}