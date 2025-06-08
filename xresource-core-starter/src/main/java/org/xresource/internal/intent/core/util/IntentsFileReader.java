package org.xresource.internal.intent.core.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xresource.core.logging.XLogger;
import org.xresource.internal.exception.XInvalidConfigurationException;
import org.xresource.internal.intent.core.dsl.IntentDslCompiler;
import org.xresource.internal.intent.core.parser.model.IntentMeta;
import org.xresource.internal.intent.core.xml.XmlIntentParser;
import org.xresource.internal.models.XResourceMetadata;
import org.xresource.internal.registry.XResourceMetadataRegistry;

import static org.xresource.internal.config.XResourceConfigProperties.INTENTS_FILE_PATH;

@Component
public class IntentsFileReader {

    @Value(INTENTS_FILE_PATH)
    private String filePath;

    private final XLogger log = XLogger.forClass(IntentsFileReader.class);

    public void loadFiles(XResourceMetadataRegistry registry) {
        log.info("Starting IQL loading process...");

        if (filePath == null || filePath.isBlank()) {
            log.warn("No IQL path configured. Skipping file load.");
            return;
        }

        log.info("Configured IQL path: %s", filePath);

        if (filePath.startsWith("classpath:")) {
            log.debug("Detected classpath-based IQL loading.");
            loadFromClasspath(filePath, registry);
        } else {
            log.debug("Detected filesystem-based IQL loading.");
            loadFromFileSystem(filePath, registry);
        }

        log.info("Completed IQL loading process.");
    }

    private void loadFromClasspath(String path, XResourceMetadataRegistry registry) {
        log.trace("Entering loadFromClasspath with path:" + path);
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(path + "/**/*.iql");

            log.debug("Found %s IQL resources in classpath.", resources.length);

            for (Resource resource : resources) {
                log.trace("Processing resource from classpath: " + resource.getFilename());

                if (resource.exists() && resource.isReadable()) {
                    processIqlFile(resource, registry);
                } else {
                    log.warn("Skipped unreadable or non-existing classpath resource: %s", resource.getFilename());
                }
            }

            Resource[] xmlResources = resolver.getResources(path + "/**/*.xml");

            log.debug("Found %s XML resources in classpath.", resources.length);

            for (Resource resource : xmlResources) {
                log.trace("Processing resource from classpath: " + resource.getFilename());

                if (resource.exists() && resource.isReadable()) {
                    processXMLFile(resource, registry);
                } else {
                    log.warn("Skipped unreadable or non-existing classpath resource: %s", resource.getFilename());
                }
            }
        } catch (IOException e) {
            log.error("Error loading IQLs from classpath: " + e.getMessage(), e);
        }
    }

    private void loadFromFileSystem(String dirPath, XResourceMetadataRegistry registry) {
        log.trace("Entering loadFromFileSystem with path: " + dirPath);

        File dir = new File(dirPath);
        if (!dir.exists() || !dir.isDirectory()) {
            log.error("Invalid directory provided for IQL files: %s", dirPath);
            throw new XInvalidConfigurationException("Invalid IQL file path: " + dirPath);
        }

        File[] iqlFiles = dir.listFiles((d, name) -> name.endsWith(".iql"));

        if (iqlFiles == null || iqlFiles.length == 0) {
            log.warn("No IQL files found in directory: %s", dirPath);
            return;
        }

        log.info("Found %s IQL files in filesystem path.", iqlFiles.length);

        for (File file : iqlFiles) {
            log.debug("Processing IQL file: %s", file.getAbsolutePath());
            processIqlFile(new FileSystemResource(file), registry);
        }

        File[] xmlFiles = dir.listFiles((d, name) -> name.endsWith(".xml"));

        if (iqlFiles == null || iqlFiles.length == 0) {
            log.warn("No XML files found in directory: %s", dirPath);
            return;
        }

        log.info("Found %s XML files in filesystem path.", iqlFiles.length);

        for (File file : xmlFiles) {
            log.debug("Processing XML file: %s", file.getAbsolutePath());
            processXMLFile(new FileSystemResource(file), registry);
        }
    }

    private void processIqlFile(Resource resource, XResourceMetadataRegistry registry) {
        log.trace("Entering processIqlFile for: %s", resource.getFilename());
        try (InputStream is = resource.getInputStream()) {
            String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            log.debug("Read content from IQL file: %s", resource.getFilename());
            log.trace("IQL content: \n%s", content);

            IntentDslCompiler compiler = new IntentDslCompiler();
            Element xml = compiler.compile(content);
            log.debug("Compiled IQL to XML successfully for: %s", resource.getFilename());

            String xresource = xml.getAttribute("resource");
            log.trace("Extracted resource attribute: %s", xresource);

            if (xresource == null || xresource.isBlank()) {
                log.error("No resource attribute found in IQL XML.");
                throw new XInvalidConfigurationException("Requested resource not found");
            }

            XResourceMetadata xMeta = registry.get(xresource);
            if (xMeta == null) {
                log.error("No metadata found for resource: %s", xresource);
                throw new XInvalidConfigurationException("Metadata for requested resource not found");
            }

            log.debug("Metadata found for resource: %s", xresource);
            log.trace("Parsed XML Element:\n%s", XmlIntentParser.printXmlElement(xml));

            IntentMeta imeta = XmlIntentParser.compile(xml, xresource, xMeta);
            xMeta.addXIntent(imeta);
            log.info("Successfully loaded IQL: %s", resource.getFilename());
        } catch (Exception e) {
            log.error("Failed to process IQL file: %s | Error: %s", resource.getFilename(), e.getMessage(), e);
            throw new XInvalidConfigurationException("Failed to read IQL file: " + resource.getFilename());
        }
    }

    private void processXMLFile(Resource resource, XResourceMetadataRegistry registry) {
        log.trace("Entering processIqlFile for: %s", resource.getFilename());
        try (InputStream is = resource.getInputStream()) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            String xmlContent = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            Document doc = builder.parse(new InputSource(new StringReader(xmlContent)));

            Element xml = doc.getDocumentElement();

            log.debug("Parsed  the XML successfully for: %s", resource.getFilename());

            String xresource = xml.getAttribute("resource");
            log.trace("Extracted resource attribute: %s", xresource);

            if (xresource == null || xresource.isBlank()) {
                log.error("No resource attribute found in IQL XML.");
                throw new XInvalidConfigurationException("Requested resource not found");
            }

            XResourceMetadata xMeta = registry.get(xresource);
            if (xMeta == null) {
                log.error("No metadata found for resource: %s", xresource);
                throw new XInvalidConfigurationException("Metadata for requested resource not found");
            }

            log.debug("Metadata found for resource: %s", xresource);
            log.trace("Parsed XML Element:\n%s", XmlIntentParser.printXmlElement(xml));

            IntentMeta imeta = XmlIntentParser.compile(xml, xresource, xMeta);
            xMeta.addXIntent(imeta);
            log.info("Successfully loaded XML: %s", resource.getFilename());
        } catch (Exception e) {
            log.error("Failed to process XML file: %s | Error: %s", resource.getFilename(), e.getMessage(), e);
            throw new XInvalidConfigurationException("Failed to read XML file: " + resource.getFilename());
        }
    }
}
