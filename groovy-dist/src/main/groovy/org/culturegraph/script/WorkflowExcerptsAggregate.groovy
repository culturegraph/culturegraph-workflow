/*
 * MIT License
 *
 * Copyright (c) 2018 Deutsche Nationalbibliothek
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * Groovy: Version 2.5.0
 */
package org.culturegraph.script

import groovy.cli.picocli.CliBuilder
import org.culturegraph.recordaggregator.plugin.AggregatedRecordBuilderImpl
import org.culturegraph.workflow.core.BundleBuilder
import org.culturegraph.workflow.core.entities.ChainedTransformer
import org.culturegraph.workflow.core.entities.InputFormat
import org.culturegraph.workflow.core.entities.OutputFormat
import org.culturegraph.workflow.core.entities.Transformer
import org.culturegraph.workflow.core.entities.TransformerFactory
import org.culturegraph.workflow.plugin.io.DecompressedInputStream
import org.culturegraph.workflow.plugin.io.MarcStreamFactory
import org.culturegraph.workflow.plugin.metafacture.ExcerptTransformerFactory
import org.culturegraph.workflow.plugin.metafacture.MetamorphTransformerFactory
import org.culturegraph.workflow.plugin.metafacture.XslTransformerFactory
import org.marc4j.MarcReader
import org.marc4j.MarcStreamReader
import org.marc4j.MarcXmlWriter
import org.marc4j.marc.Record

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import java.util.stream.Stream

def summary = '\n' +
        'Workflow that generates a MARCXML collection that aggregates grouped excerpts from MARC21 records.' +
        '\n'

def usage = this.class.getSimpleName() + ' ' +
        '[-h] --input FILE [--separator delimiter] [--output FILE] --morph FILE [--morph FILE ...] --xsl FILE [--xsl FILE ...]'

def cli = new CliBuilder(usage: usage, header: '\nOptions:', footer: summary)
cli.with {
    i argName: 'file', longOpt: 'input', 'Input file with mapped excerpts.', type: String.class, required: true
    o argName: 'file', longOpt: 'output', 'Output file. Use - for stdout. Default: stdout.', type: String.class, required: false, defaultValue: '-'
    s argName: 'delimiter', longOpt: 'separator', 'Separator used for input csv to split key and record. Default: SPACE.', type: String.class, defaultValue: ' '
    h longOpt: 'help', 'Show usage information.'
}

def options = cli.parse(args)

if (!options) {
    return
}

if (options.h) {
    cli.usage()
    return
}

boolean useStdout = (options.o as String) == '-'
def input = options.i as File
String separator = options.s as String

AggregatedRecordBuilderImpl builder = new AggregatedRecordBuilderImpl()
builder.setCatalogingAgency('DE-101')
builder.setBuildNumberPrefix('CG_')

LocalDateTime now = LocalDateTime.now();
DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
builder.setBuildNumberSuffix('_' + now.format(formatter))

Charset utf8 = StandardCharsets.UTF_8

OutputStream outputStream = useStdout ? System.out : new FileOutputStream(options.o as String)
outputStream.withCloseable { out ->
    MarcXmlWriter marcXmlWriter = new MarcXmlWriter(out, utf8.name(), true)
    InputStream inputStream = DecompressedInputStream.of(new FileInputStream(input))
    inputStream.withReader { new BufferedReader(it).withCloseable { reader ->

        String lastLabel = ''
        Iterator<String> mappedExcerptsIterator = reader.lines().iterator()
        while (mappedExcerptsIterator.hasNext()) {
            String line = mappedExcerptsIterator.next()
            String[] row = line.split(separator, 2)
            String label = row[0]
            String record = row[1]

            byte[] bytes = record.getBytes(utf8)
            MarcReader marcReader = new MarcStreamReader(new ByteArrayInputStream(bytes))
            Record rec = marcReader.next()

            if (label.equals(lastLabel) || lastLabel.isEmpty()) {
                builder.add(rec)
            } else {
                Record aggregatedRecords = builder.build()
                builder.add(rec)
                marcXmlWriter.write(aggregatedRecords)
            }
            lastLabel = label
        }
        Record aggregatedRecords = builder.build()
        marcXmlWriter.write(aggregatedRecords)
    }}  // reader end

    marcXmlWriter.close()
}  // outputstream end