package demo;

import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.ReaderNotOpenException;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.ResourceAwareItemReaderItemStream;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DefaultFieldSet;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.batch.item.support.AbstractItemCountingItemStreamItemReader;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

/**
 * RobustCsvFileItemReader
 *
 * The parser is using the com.univocity.parsers.csv.CsvParser implementation that can handle various oddities,
 * e.g. single quote with a quote column value.
 *
 */
public class RobustCsvFileItemReader<T> extends AbstractItemCountingItemStreamItemReader<T> implements
    ResourceAwareItemReaderItemStream<T>, InitializingBean {
    private static final Log LOG = LogFactory.getLog(FlatFileItemReader.class);

    private Resource resource;

    private boolean noInput = false;

    private char delimiter = ',';

    private char quoteCharacter = '"';

    private boolean strict = true;

    private int lineCount = 0;

    private int linesToSkip = 0;

    private CsvParser csvParser;

    private String lineSeparator = "\r\n";

    private FieldSetMapper<T> fieldSetMapper;

    public RobustCsvFileItemReader() {
        setName(ClassUtils.getShortName(FlatFileItemReader.class));
    }

    public void setStrict(boolean strict) {
        this.strict = strict;
    }

    public void setDelimiter(char delimiter) {
        this.delimiter = delimiter;
    }

    public void setQuoteCharacter(char quoteCharacter) {
        this.quoteCharacter = quoteCharacter;
    }

    @Override
    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public void setLinesToSkip(int linesToSkip) {
        this.linesToSkip = linesToSkip;
    }

    public void setFieldSetMapper(FieldSetMapper<T> fieldSetMapper) {
        this.fieldSetMapper = fieldSetMapper;
    }

    @Override
    protected T doRead() throws Exception {
        if (noInput) {
            return null;
        }

        String[] line = readLine();

        if (line == null) {
            return null;
        } else {
            FieldSet fieldSet = new DefaultFieldSet(line);
            return fieldSetMapper.mapFieldSet(fieldSet);
        }
    }

    @Override
    protected void doOpen() throws Exception {
        Assert.notNull(resource, "Input resource must be set");

        noInput = true;
        if (!resource.exists()) {
            if (strict) {
                throw new IllegalStateException("Input resource must exist (reader is in 'strict' mode): " + resource);
            }
            LOG.warn("Input resource does not exist " + resource.getDescription());
            return;
        }

        if (!resource.isReadable()) {
            if (strict) {
                throw new IllegalStateException("Input resource must be readable (reader is in 'strict' mode): "+ resource);
            }
            LOG.warn("Input resource is not readable " + resource.getDescription());
            return;
        }

        csvParser = new CsvParser(createSettings());
        csvParser.beginParsing(new InputStreamReader(resource.getInputStream()));
        for (int i = 0; i < linesToSkip; i++) {
            String[] line = readLine();
        }
        noInput = false;
    }

    private CsvParserSettings createSettings() {
        CsvParserSettings settings = new CsvParserSettings();
        settings.getFormat().setLineSeparator(lineSeparator);
        settings.getFormat().setDelimiter(delimiter);
        settings.getFormat().setQuote(quoteCharacter);
        settings.setEmptyValue("");
        settings.setParseUnescapedQuotes(true); // THIS IS IMPORTANT FOR YOU
        return settings;
    }


    @Override
    protected void doClose() throws Exception {
        lineCount = 0;
        if (csvParser != null) {
            csvParser.stopParsing();
        }
    }

    /**
     * @return next line (skip comments).getCurrentResource
     */
    private String[] readLine() {

        if (csvParser == null) {
            throw new ReaderNotOpenException("Parser must be open before it can be read.");
        }

        String line[] = null;
        line = this.csvParser.parseNext();
        if (line == null) {
            return null;
        }
        lineCount++;
        return line;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // Assert.notNull(lineMapper, "LineMapper is required");
    }


}