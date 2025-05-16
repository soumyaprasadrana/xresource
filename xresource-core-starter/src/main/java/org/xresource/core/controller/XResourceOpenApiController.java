package org.xresource.core.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xresource.core.openapi.XOpenApiGenerator;

import io.swagger.v3.oas.annotations.Hidden;

@RestController
@RequestMapping("${xresource.openapi.base-path:/api/xresources-openapi}")
@ConditionalOnProperty(prefix = "xresource.openapi", name = "enabled", havingValue = "true")
@Hidden
public class XResourceOpenApiController {
        @Value("${xresource.openapi.base-path:/api/xresources-openapi}")
        private String basePath;

        @Value("${springdoc.swagger-ui.path:/swagger-ui}")
        private String swaggerUiPath;

        @Value("${xresource.api.base-path:/api/resources}")
        private String xResourceAPIBasePath;

        @Autowired
        XOpenApiGenerator xOpenApiGenerator;

        @GetMapping("/openapi.json")
        public ResponseEntity<Map<String, Object>> openAPI() {
                return ResponseEntity.ok(xOpenApiGenerator.generateOpenApiSpec());
        }

        @GetMapping("/swagger-config")
        public ResponseEntity<Map<String, Object>> getOpenApiConfigJson() {
                // This should return a valid OpenAPI v3 JSON structure
                Map<String, Object> openApiSpec = Map.of(
                                "configUrl", this.basePath + "/swagger-config",
                                "url", this.basePath + "/openapi.json", "filter", true);

                return ResponseEntity.ok(openApiSpec);
        }

        @GetMapping(path = "/index.html", produces = MediaType.TEXT_HTML_VALUE)
        public String getIndexHtml() {

                StringBuilder html = new StringBuilder();
                html.append("<!DOCTYPE html>\n");
                html.append("<html lang=\"en\">\n");
                html.append("<head>\n");
                html.append("    <meta charset=\"UTF-8\">\n");
                html.append("    <title>XResource API Docs</title>\n");
                html.append("    <link rel=\"stylesheet\" type=\"text/css\" href=\"").append(swaggerUiPath)
                                .append("/swagger-ui.css\" />\n");
                html.append("</head>\n");
                html.append("<body>\n");
                html.append("    <div id=\"swagger-ui\"></div>\n");
                html.append("    <script src=\"").append(swaggerUiPath).append("/swagger-ui-bundle.js\"></script>\n");
                html.append("    <script src=\"").append(swaggerUiPath)
                                .append("/swagger-ui-standalone-preset.js\"></script>\n");
                html.append("    <script src=\"").append(basePath).append("/swagger-initializer.js\"></script>\n");
                html.append("</body>\n");
                html.append("</html>\n");

                return html.toString();
        }

        @GetMapping(path = "swagger-initializer.js", produces = "application/javascript")
        public String getSwaggerInitializerJS() {
                StringBuilder js = new StringBuilder();
                js.append("window.onload = function() {\n");
                js.append("    window.ui = SwaggerUIBundle({\n");
                js.append("        url: '/openapi.json',\n");
                js.append("        dom_id: '#swagger-ui',\n");
                js.append("        deepLinking: true,\n");
                js.append("        filter: true,\n");
                js.append("        presets: [\n");
                js.append("            SwaggerUIBundle.presets.apis,\n");
                js.append("            SwaggerUIStandalonePreset\n");
                js.append("        ],\n");
                js.append("        configUrl: " + "\"" + this.basePath + "/swagger-config" + "\",");
                js.append("        layout: 'StandaloneLayout'\n");
                js.append("    });\n");
                js.append("};\n");

                return js.toString();
        }
}
