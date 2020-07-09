package io.mozen.sign;

import com.documents4j.api.DocumentType;
import com.documents4j.api.IConverter;
import com.documents4j.job.LocalConverter;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

@MultipartConfig
public class TheServlet extends HttpServlet {

    public static final Logger logger = LoggerFactory.getLogger(TheServlet.class);


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html");
        resp.getWriter().println(IOUtils.resourceToString("index.html", StandardCharsets.UTF_8, ClassLoader.getSystemClassLoader()));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse resp) throws IOException {

        File docxFile = File.createTempFile("docx2pdf", ".docx");

        try {
            final Collection<Part> fileParts = request.getParts();

            // получение jks
            for (Part fp : fileParts) {
                if ("docx".equals(fp.getName())) {
                    docxFile.deleteOnExit();
                    logger.debug("docx-file: {}", docxFile.getAbsolutePath());

                    try (FileOutputStream output = new FileOutputStream(docxFile)) {
                        IOUtils.copy(fp.getInputStream(), output);
                    }
                }
            }

        } catch (Throwable e) {
            logger.error("", e);
            e.printStackTrace(resp.getWriter());
            return;
        }

        try (InputStream docxInputStream = new FileInputStream(docxFile)) {
            IConverter converter = LocalConverter.builder().build();

            resp.setContentType("application/pdf");

            OutputStream outputStream = resp.getOutputStream();
            converter.convert(docxInputStream).as(DocumentType.DOCX).to(outputStream).as(DocumentType.PDF).execute();
            outputStream.close();
            System.out.println("success");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String getNotEmptyParameter(HttpServletRequest request, String param) {
        String val = request.getParameter(param);
        if (StringUtil.isEmpty(val)) throw new RuntimeException("Not empty param expected: " + param);
        return val;
    }
}
