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
import org.culturegraph.workflow.plugin.io.DecompressedInputStream
import org.marc4j.MarcReader
import org.marc4j.MarcStreamReader
import org.marc4j.MarcXmlWriter
import org.marc4j.marc.Record

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

def summary = '\n' +
        'Workflow that generates a MARCXML collection that aggregates grouped excerpts from MARC21 records.' +
        '\n'

def usage = this.class.getSimpleName() + ' ' +
        '[-h] --input FILE [--separator delimiter] [--output FILE] --limit NUM FILE'

def cli = new CliBuilder(usage: usage, header: '\nOptions:', footer: summary)
cli.with {
    i argName: 'file', longOpt: 'input', 'Input file with mapped excerpts.', type: String.class, required: true
    o argName: 'file', longOpt: 'output', 'Output file. Use - for stdout. Default: stdout.', type: String.class, required: false, defaultValue: '-'
    s argName: 'delimiter', longOpt: 'separator', 'Separator used for input csv to split key and record. Default: SPACE.', type: String.class, defaultValue: ' '
    l argName: 'limit', longOpt: 'limit', 'Komponent limit. A component that exceeds this limit will not be exported. Default: unlimited.', type: Integer.class, defaultValue: '-1'
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
File input = options.i as File
String separator = options.s as String
Integer limit = options.l as Integer

AggregatedRecordBuilderImpl builder = new AggregatedRecordBuilderImpl()
builder.setCatalogingAgency('DE-101')
builder.setBuildNumberPrefix('CG_')

ZonedDateTime utcNow = LocalDateTime.now(Clock.systemUTC()).atZone(ZoneOffset.UTC)
DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT
builder.setBuildNumberSuffix('_' + formatter.format(utcNow))

Charset utf8 = StandardCharsets.UTF_8

OutputStream outputStream = useStdout ? System.out : new FileOutputStream(options.o as String)
outputStream.withCloseable { out ->
    MarcXmlWriter marcXmlWriter = new MarcXmlWriter(out, true)
    InputStream inputStream = DecompressedInputStream.of(new FileInputStream(input))
    inputStream.withReader { new BufferedReader(it).withCloseable { reader ->

        String lastLabel = ''
        Iterator<String> mappedExcerptsIterator = reader.lines().iterator()

        int size = 0
        String skipLabel = ''
        while (mappedExcerptsIterator.hasNext()) {
            String line = mappedExcerptsIterator.next()
            String[] row = line.split(separator, 2)
            String label = row[0]
            String record = row[1]

            if (label == skipLabel) {
                continue
            }

            byte[] bytes = record.getBytes(utf8)
            MarcReader marcReader = new MarcStreamReader(new ByteArrayInputStream(bytes))
            Record rec = marcReader.next()

            if (label.equals(lastLabel) || lastLabel.isEmpty()) {
                builder.add(rec)
            } else {
                Record aggregatedRecords = builder.build()
                size = 0
                builder.add(rec)
                marcXmlWriter.write(aggregatedRecords)
            }

            lastLabel = label

            if (limit > 0) {
                size += 1
                if (size > limit) {
                    builder.records.clear()
                    size = 0
                    skipLabel = label
                    lastLabel = ''
                }
            }
        }
        if (size > 0) Record aggregatedRecords = builder.build()
        marcXmlWriter.write(aggregatedRecords)
    }}  // reader end

    marcXmlWriter.close()
}  // outputstream end