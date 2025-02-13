package net.techpoint.server;

import net.techpoint.template.TemplateEngine;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

public class WebTemplate {
    private static final int DEFAULT_BUFFER_SIZE = 4 * 1024;
    private final String templateText;
    private final TemplateEngine templateEngine;

    public WebTemplate(String resourceName, Class<?> loader) {
        templateText = getTemplate(resourceName, loader);
        templateEngine = new TemplateEngine(templateText);
    }

    public TemplateEngine pullEngine() {
        return templateEngine;
    }

    private String getTemplate(String resource, Class<?> loader) {
        InputStream inputStream = loader.getResourceAsStream(resource);

        if (inputStream == null) {
            return new WebTemplateService(resource).invoke();
        }

        return readStream(inputStream, resource);
    }

    private static String readStream(InputStream inputStream, String resource) {
        StringWriter writer = new StringWriter();
        InputStreamReader reader = new InputStreamReader(inputStream);

        try {
            int n;
            char[] buffer = new char[DEFAULT_BUFFER_SIZE];

            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read: " + resource, e);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                //noinspection ThrowFromFinallyBlock
                throw new IllegalArgumentException("Unexpected trouble closing reader", e);
            }
        }

        return writer.toString();
    }

    private class WebTemplateService {
        private String resource;

        public WebTemplateService(String resource) {
            this.resource = resource;
        }

        public String invoke() {
            throw new IllegalArgumentException("Can not find resource " + resource);
        }
    }
}
