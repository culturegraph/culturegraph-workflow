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
import org.culturegraph.workflow.core.IterativeCsvHeadMapper
import org.culturegraph.workflow.core.IterativeXmlCommentMapper
import org.culturegraph.workflow.plugin.io.DecompressedInputStream

def summary = '\n' +
        'Workflow that replaces the idn with a  cluster number for each CSV row.' +
        '\n' +
        'Each ouput line consists of a key and a MARC21 record, separated by a custom separator.' +
        '\n'

def usage = this.class.getSimpleName() + ' ' +
        '[-h] --input FILE --map FILE [--separator DELIMITER] [--output FILE]'

def cli = new CliBuilder(usage: usage, header: '\nOptions:', footer: summary)
cli.with {
    i argName: 'file', longOpt: 'input', 'Input file with excerpts. Each excerpt is prefixed with a key.', type: String.class, required: true
    m argName: 'file', longOpt: 'map', 'Input file with idn-to-cluster mapping.', type: String.class, required: true
    o argName: 'file', longOpt: 'output', 'Output file. Use - for stdout. Default: stdout.', type: String.class, required: false, defaultValue: '-'
    s argName: 'delimiter', longOpt: 'separator', 'Separator that separates key and record in the output file. Default: SPACE.', type: String.class, defaultValue: ' '
    h longOpt: 'help', 'Show usage information'
}

def options = cli.parse(args)

if (!options) {
    return
}

if (options.h) {
    cli.usage()
    return
}

def input = options.i as File
def map = options.m as File
def separator = options.s as String
def utf8 = 'UTF-8'

boolean useStdout = (options.o as String) == '-'
OutputStream outputStream = useStdout ? System.out : new FileOutputStream(options.o as String)

outputStream.withWriter utf8, { new BufferedWriter(it).withCloseable { writer ->

    InputStream inputStream = DecompressedInputStream.of(new FileInputStream(input))
    InputStream mapStream = DecompressedInputStream.of(new FileInputStream(map))

    inputStream.withReader utf8, { new BufferedReader(it).withCloseable {inputReader ->
        mapStream.withReader utf8, { new BufferedReader(it).withCloseable { mapReader ->

            Iterator<String> inputIterator = inputReader.iterator()
            Iterator<String> mapIterator = mapReader.iterator()

            IterativeCsvHeadMapper mapper = new IterativeCsvHeadMapper(inputIterator, separator, mapIterator)
            while (mapper.hasNext()) {
                String mappedExcerpt = mapper.next()
                writer.write(mappedExcerpt + '\n')
            }

        }}  // mapReader end

    }}  // inputReader end

}} // writer end
